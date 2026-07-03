import json
from types import SimpleNamespace
from unittest.mock import patch

import pytest

from imzala import Imzala
from imzala.errors import ImzalaError, ImzalaRateLimitError
from imzala_client.api.demands_api import DemandsApi
from imzala_client.api.templates_api import TemplatesApi
from imzala_client.exceptions import ApiException


def envelope(data, success: bool = True) -> SimpleNamespace:
    """Stand-in for a generated `*200Response` pydantic model — the facade
    only reads `.success` / `.data` off it, so a plain namespace is enough
    and keeps these tests decoupled from the generated model internals."""
    return SimpleNamespace(success=success, data=data)


def api_exception(status: int, body: dict = None) -> ApiException:
    """Shaped like the generated client's `ApiException` — enough for
    `map_api_exception()` to read `.status` / `.body` / `.headers` off it.
    Mirrors `retry.test.ts`'s `fakeAxiosError` fixture in the node package."""
    return ApiException(status=status, body=json.dumps(body if body is not None else {"success": False}))


# Keep retries near-instant in tests — real prod default is 0.3s (300ms).
FAST_RETRY = {"max_retries": 2, "retry_base_delay": 0.001}


@pytest.fixture(autouse=True)
def no_sleep():
    """Retry backoff calls `time.sleep` — patch it out so these tests run
    instantly instead of actually waiting out the (tiny, but nonzero)
    backoff delays."""
    with patch("imzala.client.time.sleep") as mocked:
        yield mocked


class TestSafeAutoRetryGetRequests:
    def test_retries_a_get_twice_on_429_then_succeeds_on_3rd_attempt(self):
        with patch.object(
            TemplatesApi,
            "api_v1_templates_get",
            side_effect=[
                api_exception(429),
                api_exception(429),
                envelope({"templates": [{"id": "t1"}], "total": 1, "page": 1, "limit": 10}),
            ],
        ) as mocked:
            client = Imzala(api_key="imz_test", **FAST_RETRY)
            result = client.templates.list()

        assert mocked.call_count == 3
        assert result["templates"] == [{"id": "t1"}]

    def test_retries_a_get_on_5xx_server_error_and_succeeds(self):
        with patch.object(
            DemandsApi,
            "api_v1_demands_id_get",
            side_effect=[api_exception(503), envelope({"id": "d1", "status": "PENDING"})],
        ) as mocked:
            client = Imzala(api_key="imz_test", **FAST_RETRY)
            result = client.demands.get("d1")

        assert mocked.call_count == 2
        assert result == {"id": "d1", "status": "PENDING"}

    def test_does_not_retry_a_get_on_non_429_4xx(self):
        with patch.object(
            TemplatesApi,
            "api_v1_templates_id_get",
            side_effect=api_exception(404, {"success": False, "error": "TEMPLATE_NOT_FOUND"}),
        ) as mocked:
            client = Imzala(api_key="imz_test", **FAST_RETRY)
            with pytest.raises(ImzalaError):
                client.templates.get("missing")

        assert mocked.call_count == 1

    def test_max_retries_zero_disables_retry_entirely_even_on_429(self):
        with patch.object(
            TemplatesApi, "api_v1_templates_get", side_effect=api_exception(429)
        ) as mocked:
            client = Imzala(api_key="imz_test", max_retries=0, retry_base_delay=0.001)
            with pytest.raises(ImzalaRateLimitError):
                client.templates.list()

        assert mocked.call_count == 1

    def test_exhausts_retries_and_raises_the_typed_error_when_every_attempt_fails(self):
        with patch.object(
            TemplatesApi, "api_v1_templates_get", side_effect=api_exception(503)
        ) as mocked:
            client = Imzala(api_key="imz_test", max_retries=2, retry_base_delay=0.001)
            with pytest.raises(ImzalaError):
                client.templates.list()

        # initial attempt + 2 retries = 3 calls total
        assert mocked.call_count == 3


class TestSafeAutoRetrySafetyWritesAreNeverRetried:
    def test_post_create_returning_429_raises_immediately_no_retry(self):
        with patch.object(
            DemandsApi,
            "api_v1_demands_post",
            side_effect=api_exception(429, {"success": False, "error": "RATE_LIMITED"}),
        ) as mocked:
            client = Imzala(api_key="imz_test", **FAST_RETRY)
            with pytest.raises(ImzalaRateLimitError):
                client.demands.create({"template_id": "t1", "party_mapping": []})

        # A retried demands.create() POST would create a DUPLICATE demand — must never retry.
        assert mocked.call_count == 1

    def test_post_create_returning_503_server_error_also_raises_immediately_no_retry(self):
        with patch.object(
            DemandsApi, "api_v1_demands_post", side_effect=api_exception(503)
        ) as mocked:
            client = Imzala(api_key="imz_test", **FAST_RETRY)
            with pytest.raises(ImzalaError):
                client.demands.create({"template_id": "t1", "party_mapping": []})

        assert mocked.call_count == 1

    def test_delete_demand_returning_429_raises_immediately_no_retry(self):
        with patch.object(
            DemandsApi, "api_v1_demands_id_delete", side_effect=api_exception(429)
        ) as mocked:
            client = Imzala(api_key="imz_test", **FAST_RETRY)
            with pytest.raises(ImzalaRateLimitError):
                client.demands.delete("d1")

        assert mocked.call_count == 1

    def test_cancel_demand_returning_503_raises_immediately_no_retry(self):
        with patch.object(
            DemandsApi, "api_v1_demands_id_cancel_post", side_effect=api_exception(503)
        ) as mocked:
            client = Imzala(api_key="imz_test", **FAST_RETRY)
            with pytest.raises(ImzalaError):
                client.demands.cancel("d1", {"reason": "x"})

        assert mocked.call_count == 1

    def test_template_update_returning_429_raises_immediately_no_retry(self):
        with patch.object(
            TemplatesApi, "api_v1_templates_id_patch", side_effect=api_exception(429)
        ) as mocked:
            client = Imzala(api_key="imz_test", **FAST_RETRY)
            with pytest.raises(ImzalaRateLimitError):
                client.templates.update("t1", {"name": "x"})

        assert mocked.call_count == 1


class TestBinaryGetDownloadsRetryLikeOtherGets:
    def test_get_pdf_retries_on_5xx_then_returns_bytes(self):
        with patch.object(
            DemandsApi,
            "api_v1_demands_id_pdf_get",
            side_effect=[api_exception(503), b"%PDF-1.7 fake"],
        ) as mocked:
            client = Imzala(api_key="imz_test", **FAST_RETRY)
            out = client.demands.get_pdf("d1")

        assert mocked.call_count == 2
        assert out == b"%PDF-1.7 fake"

    def test_get_certificate_retries_on_429_honoring_retry_after(self):
        with patch.object(
            DemandsApi,
            "api_v1_demands_id_certificate_get",
            side_effect=[api_exception(429), b"%PDF cert"],
        ) as mocked:
            client = Imzala(api_key="imz_test", **FAST_RETRY)
            out = client.demands.get_certificate("d1", lang="en")

        assert mocked.call_count == 2
        assert out == b"%PDF cert"

    def test_get_pdf_does_not_retry_on_404(self):
        with patch.object(
            DemandsApi,
            "api_v1_demands_id_pdf_get",
            side_effect=api_exception(404, {"success": False, "error": "DEMAND_NOT_FOUND"}),
        ) as mocked:
            client = Imzala(api_key="imz_test", **FAST_RETRY)
            with pytest.raises(ImzalaError):
                client.demands.get_pdf("missing")

        assert mocked.call_count == 1
