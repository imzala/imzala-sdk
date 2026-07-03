"""v1 lifecycle facade tests — the 9 demand/template management methods.

Mirrors `packages/node/src/__tests__/lifecycle.test.ts` in this monorepo:
each facade method is asserted to call the right *generated* client method
(with the right kwargs) and to unwrap the `{success, data}` envelope. The
two binary downloads (`get_pdf` / `get_certificate`) are asserted to return
raw `bytes` straight through, since the generated client deserializes an
`application/pdf` 200 response to `bytes` (no envelope to unwrap).
"""

from types import SimpleNamespace
from unittest.mock import patch

from imzala import Imzala
from imzala_client.api.demands_api import DemandsApi
from imzala_client.api.templates_api import TemplatesApi


def envelope(data, success: bool = True) -> SimpleNamespace:
    """Stand-in for a generated `*200Response` pydantic model — the facade
    only reads `.success` / `.data` off it."""
    return SimpleNamespace(success=success, data=data)


class TestDemandsLifecycle:
    def test_list_forwards_filters_and_unwraps_counts_only_data(self):
        data = {
            "demands": [{"id": "d1", "parties_total": 2, "parties_signed": 1}],
            "total": 1,
            "page": 1,
            "limit": 20,
        }
        with patch.object(DemandsApi, "api_v1_demands_get", return_value=envelope(data)) as mocked:
            client = Imzala(api_key="imz_test")
            result = client.demands.list(
                status="PENDING",
                q="kira",
                from_="2026-01-01",
                to="2026-12-31",
                template_id="t1",
                page=1,
                limit=20,
                sort="createdAt:desc",
            )

        kwargs = mocked.call_args.kwargs
        assert kwargs["status"] == "PENDING"
        assert kwargs["q"] == "kira"
        # `from` is a Python keyword — facade takes `from_`, forwards `var_from`.
        assert kwargs["var_from"] == "2026-01-01"
        assert kwargs["to"] == "2026-12-31"
        assert kwargs["template_id"] == "t1"
        assert kwargs["page"] == 1
        assert kwargs["limit"] == 20
        assert kwargs["sort"] == "createdAt:desc"
        assert result["demands"][0] == {"id": "d1", "parties_total": 2, "parties_signed": 1}

    def test_list_defaults_all_filters_to_none(self):
        with patch.object(
            DemandsApi, "api_v1_demands_get", return_value=envelope({"demands": [], "total": 0})
        ) as mocked:
            client = Imzala(api_key="imz_test")
            client.demands.list()

        kwargs = mocked.call_args.kwargs
        for key in ("status", "q", "var_from", "to", "template_id", "page", "limit", "sort"):
            assert kwargs[key] is None

    def test_get_timeline_unwraps_masked_events(self):
        events = {"events": [{"id": "e1", "event_type": "SIGNED", "ip_masked": "1.2.3.***"}]}
        with patch.object(
            DemandsApi, "api_v1_demands_id_timeline_get", return_value=envelope(events)
        ) as mocked:
            client = Imzala(api_key="imz_test")
            result = client.demands.get_timeline("d1")

        assert mocked.call_args.kwargs["id"] == "d1"
        assert result["events"][0]["ip_masked"] == "1.2.3.***"

    def test_cancel_posts_the_reason_body_and_unwraps(self):
        with patch.object(
            DemandsApi,
            "api_v1_demands_id_cancel_post",
            return_value=envelope({"id": "d1", "status": "CANCELLED"}),
        ) as mocked:
            client = Imzala(api_key="imz_test")
            result = client.demands.cancel("d1", {"reason": "vazgeçildi"})

        kwargs = mocked.call_args.kwargs
        assert kwargs["id"] == "d1"
        assert kwargs["api_v1_demands_id_cancel_post_request"] == {"reason": "vazgeçildi"}
        assert result["status"] == "CANCELLED"

    def test_cancel_without_a_body_sends_an_empty_object(self):
        with patch.object(
            DemandsApi,
            "api_v1_demands_id_cancel_post",
            return_value=envelope({"id": "d1", "status": "CANCELLED"}),
        ) as mocked:
            client = Imzala(api_key="imz_test")
            client.demands.cancel("d1")

        assert mocked.call_args.kwargs["api_v1_demands_id_cancel_post_request"] == {}

    def test_resend_party_targets_a_single_party(self):
        with patch.object(
            DemandsApi,
            "api_v1_demands_id_parties_party_id_resend_post",
            return_value=envelope({"sent": ["email"]}),
        ) as mocked:
            client = Imzala(api_key="imz_test")
            result = client.demands.resend_party("d1", "p9")

        kwargs = mocked.call_args.kwargs
        assert kwargs["id"] == "d1"
        assert kwargs["party_id"] == "p9"
        assert result["sent"] == ["email"]

    def test_delete_unwraps_deletion_result(self):
        with patch.object(
            DemandsApi, "api_v1_demands_id_delete", return_value=envelope({"id": "d1", "deleted": True})
        ) as mocked:
            client = Imzala(api_key="imz_test")
            result = client.demands.delete("d1")

        assert mocked.call_args.kwargs["id"] == "d1"
        assert result == {"id": "d1", "deleted": True}

    def test_get_pdf_returns_raw_bytes(self):
        pdf_bytes = b"%PDF-1.7 fake signed contract"
        with patch.object(
            DemandsApi, "api_v1_demands_id_pdf_get", return_value=pdf_bytes
        ) as mocked:
            client = Imzala(api_key="imz_test")
            out = client.demands.get_pdf("d1")

        assert isinstance(out, bytes)
        assert out == pdf_bytes
        assert out.startswith(b"%PDF-1.7")
        assert mocked.call_args.kwargs["id"] == "d1"

    def test_get_pdf_coerces_a_bytearray_to_bytes(self):
        with patch.object(
            DemandsApi, "api_v1_demands_id_pdf_get", return_value=bytearray(b"%PDF cert")
        ):
            client = Imzala(api_key="imz_test")
            out = client.demands.get_pdf("d1")

        assert type(out) is bytes
        assert out == b"%PDF cert"

    def test_get_certificate_forwards_lang_and_returns_bytes(self):
        cert_bytes = b"%PDF cert"
        with patch.object(
            DemandsApi, "api_v1_demands_id_certificate_get", return_value=cert_bytes
        ) as mocked:
            client = Imzala(api_key="imz_test")
            out = client.demands.get_certificate("d1", lang="en")

        assert isinstance(out, bytes)
        assert out == cert_bytes
        kwargs = mocked.call_args.kwargs
        assert kwargs["id"] == "d1"
        assert kwargs["lang"] == "en"

    def test_get_certificate_defaults_lang_to_none(self):
        with patch.object(
            DemandsApi, "api_v1_demands_id_certificate_get", return_value=b"%PDF"
        ) as mocked:
            client = Imzala(api_key="imz_test")
            client.demands.get_certificate("d1")

        assert mocked.call_args.kwargs["lang"] is None


class TestTemplatesLifecycle:
    def test_update_patches_metadata_and_unwraps(self):
        with patch.object(
            TemplatesApi,
            "api_v1_templates_id_patch",
            return_value=envelope({"id": "t1", "name": "Yeni Ad"}),
        ) as mocked:
            client = Imzala(api_key="imz_test")
            result = client.templates.update("t1", {"name": "Yeni Ad"})

        kwargs = mocked.call_args.kwargs
        assert kwargs["id"] == "t1"
        assert kwargs["api_v1_templates_id_patch_request"] == {"name": "Yeni Ad"}
        assert result["name"] == "Yeni Ad"

    def test_delete_soft_deletes_and_unwraps(self):
        with patch.object(
            TemplatesApi, "api_v1_templates_id_delete", return_value=envelope({"id": "t1", "deleted": True})
        ) as mocked:
            client = Imzala(api_key="imz_test")
            result = client.templates.delete("t1")

        assert mocked.call_args.kwargs["id"] == "t1"
        assert result == {"id": "t1", "deleted": True}
