from types import SimpleNamespace
from unittest.mock import patch

import pytest

from imzala import Imzala
from imzala.errors import ImzalaError
from imzala.files import FileInput, UploadPartyInput
from imzala_client.api.account_api import AccountApi
from imzala_client.api.demands_api import DemandsApi
from imzala_client.api.reminders_api import RemindersApi
from imzala_client.api.templates_api import TemplatesApi
from imzala_client.api.timestamps_api import TimestampsApi


def envelope(data, success: bool = True) -> SimpleNamespace:
    """Stand-in for a generated `*200Response` pydantic model — the facade
    only reads `.success` / `.data` off it, so a plain namespace is enough
    and keeps these tests decoupled from the generated model internals."""
    return SimpleNamespace(success=success, data=data)


class TestEnvelopeUnwrap:
    def test_demands_get_unwraps_to_inner_data(self):
        with patch.object(
            DemandsApi, "api_v1_demands_id_get", return_value=envelope({"id": "d1", "status": "PENDING"})
        ) as mocked:
            client = Imzala(api_key="imz_test")
            result = client.demands.get("d1")

        assert result == {"id": "d1", "status": "PENDING"}
        assert mocked.call_args.kwargs["id"] == "d1"

    def test_me_calls_account_api_and_unwraps(self):
        with patch.object(
            AccountApi, "api_v1_me_get", return_value=envelope({"id": "u1", "email": "a@b.com"})
        ) as mocked:
            client = Imzala(api_key="imz_test")
            result = client.me()

        assert result == {"id": "u1", "email": "a@b.com"}
        mocked.assert_called_once()

    def test_templates_list_forwards_page_limit_and_unwraps(self):
        data = {"templates": [{"id": "t1"}], "total": 1, "page": 2, "limit": 10}
        with patch.object(TemplatesApi, "api_v1_templates_get", return_value=envelope(data)) as mocked:
            client = Imzala(api_key="imz_test")
            result = client.templates.list(page=2, limit=10)

        assert mocked.call_args.kwargs["page"] == 2
        assert mocked.call_args.kwargs["limit"] == 10
        assert result["templates"] == [{"id": "t1"}]

    def test_send_reminder_routes_through_reminders_api_not_demands_api(self):
        with patch.object(
            RemindersApi,
            "api_v1_demands_id_reminders_post",
            return_value=envelope({"demand_id": "d1", "dispatched": [], "skipped": []}),
        ) as reminders_mock, patch.object(DemandsApi, "api_v1_demands_id_get") as demands_get_mock:
            client = Imzala(api_key="imz_test")
            result = client.demands.send_reminder("d1", {"force": True})

        demands_get_mock.assert_not_called()
        assert reminders_mock.call_args.kwargs["id"] == "d1"
        assert reminders_mock.call_args.kwargs["trigger_reminder_request"] == {"force": True}
        assert result == {"demand_id": "d1", "dispatched": [], "skipped": []}

    def test_embed_create_session_maps_party_id_and_unwraps(self):
        with patch.object(
            DemandsApi,
            "api_v1_demands_id_embed_session_post",
            return_value=envelope(
                {
                    "embed_token": "tok",
                    "expires_at": "2026-07-01T00:10:00.000Z",
                    "embed_url": "https://e.imzala.org/embed/sign?token=tok",
                }
            ),
        ) as mocked:
            client = Imzala(api_key="imz_test")
            result = client.embed.create_session("d1", party_id="p1")

        assert mocked.call_args.kwargs["id"] == "d1"
        assert mocked.call_args.kwargs["api_v1_demands_id_embed_session_post_request"] == {"party_id": "p1"}
        assert result["embed_token"] == "tok"

    def test_upload_document_json_encodes_parties_order_and_builds_file_tuples(self):
        with patch.object(
            DemandsApi,
            "api_v1_demands_upload_post",
            return_value=envelope({"id": "d1", "pages": [{"id": 1, "order": 1}]}),
        ) as mocked:
            client = Imzala(api_key="imz_test")
            result = client.demands.upload_document(
                files=[FileInput(content=b"hello", filename="a.pdf", content_type="application/pdf")],
                parties=[UploadPartyInput(first_name="Ada", last_name="Lovelace", email="ada@example.com")],
                order=[0],
                title="Test",
            )

        kwargs = mocked.call_args.kwargs
        assert kwargs["files"] == [("a.pdf", b"hello")]
        assert kwargs["parties"] == '[{"first_name": "Ada", "last_name": "Lovelace", "email": "ada@example.com"}]'
        assert kwargs["order"] == "[0]"
        assert kwargs["title"] == "Test"
        assert result["id"] == "d1"

    def test_upload_document_accepts_plain_dict_parties(self):
        with patch.object(
            DemandsApi,
            "api_v1_demands_upload_post",
            return_value=envelope({"id": "d1", "pages": []}),
        ) as mocked:
            client = Imzala(api_key="imz_test")
            client.demands.upload_document(
                files=[FileInput(content=b"x", filename="b.pdf")],
                parties=[{"first_name": "Ada", "last_name": "Lovelace", "phone": "+905551234567"}],
            )

        assert mocked.call_args.kwargs["parties"] == (
            '[{"first_name": "Ada", "last_name": "Lovelace", "phone": "+905551234567"}]'
        )
        assert mocked.call_args.kwargs["order"] is None

    def test_timestamps_create_builds_file_tuple_from_bytes_and_unwraps(self):
        with patch.object(
            TimestampsApi, "api_v1_timestamps_post", return_value=envelope({"id": "ts1", "file_sha256": "abc"})
        ) as mocked:
            client = Imzala(api_key="imz_test")
            result = client.timestamps.create(content=b"hello", filename="eser.pdf", idempotency_key="idem-1")

        kwargs = mocked.call_args.kwargs
        assert kwargs["file"] == ("eser.pdf", b"hello")
        assert kwargs["idempotency_key"] == "idem-1"
        assert result == {"id": "ts1", "file_sha256": "abc"}

    def test_raises_imzala_error_when_server_returns_success_false_on_2xx(self):
        with patch.object(TemplatesApi, "api_v1_templates_id_get", return_value=envelope(None, success=False)):
            client = Imzala(api_key="imz_test")
            with pytest.raises(ImzalaError):
                client.templates.get("t1")


class TestConstruction:
    def test_requires_an_api_key(self):
        with pytest.raises(ValueError, match="api_key is required"):
            Imzala(api_key="")

    def test_defaults_base_url_to_prod(self):
        client = Imzala(api_key="imz_test")
        assert client._account_api.api_client.configuration.host == "https://api-prd.imzala.org"

    def test_honors_a_custom_base_url(self):
        client = Imzala(api_key="imz_test", base_url="https://test-api.imzala.org")
        assert client._account_api.api_client.configuration.host == "https://test-api.imzala.org"

    def test_default_timeout_is_threaded_into_every_call(self):
        with patch.object(AccountApi, "api_v1_me_get", return_value=envelope({})) as mocked:
            client = Imzala(api_key="imz_test", timeout=5)
            client.me()

        assert mocked.call_args.kwargs["_request_timeout"] == 5.0
