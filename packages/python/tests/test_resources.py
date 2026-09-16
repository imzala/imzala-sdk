"""Field templates, contacts, reports, timestamp listing and the write
options that mirror `packages/node/src/__tests__/resources.test.ts`."""

import json
from types import SimpleNamespace
from unittest.mock import patch

import pytest

from imzala import FileInput, Imzala
from imzala.errors import ImzalaError, ImzalaRateLimitError
from imzala_client.api.contacts_api import ContactsApi
from imzala_client.api.demands_api import DemandsApi
from imzala_client.api.reports_api import ReportsApi
from imzala_client.api.templates_api import TemplatesApi
from imzala_client.api.timestamps_api import TimestampsApi
from imzala_client.exceptions import ApiException

from .helpers import patch_api

FT_ID = "3f1c2b9a-6d2e-4b8f-9a31-2c5d7e8f9a10"
PDF = FileInput(content=b"%PDF-1.7", filename="sozlesme.pdf", content_type="application/pdf")
PARTY = {"first_name": "Ayşe", "last_name": "Yılmaz", "email": "ayse@example.com"}


def ok(data):
    return SimpleNamespace(success=True, data=data)


def rate_limited():
    exc = ApiException(
        status=429,
        reason="Too Many Requests",
        body=json.dumps(
            {"success": False, "error": "Çok fazla istek", "code": "RATE_LIMIT_EXCEEDED", "retry_after_seconds": 0}
        ),
    )
    exc.headers = {"Retry-After": "0"}
    return exc


@pytest.fixture(autouse=True)
def no_sleep():
    with patch("imzala.client.time.sleep") as mocked:
        yield mocked


def client():
    return Imzala(api_key="imz_test", max_retries=0, retry_base_delay=0.001)


class TestFieldTemplates:
    def test_list_forwards_paging_and_unwraps_the_envelope(self):
        data = {"field_templates": [{"id": "ft1"}], "total": 1, "page": 2, "limit": 10}
        with patch_api(TemplatesApi, "api_v1_field_templates_get", return_value=ok(data)) as mocked:
            result = client().field_templates.list(page=2, limit=10)
        assert result["total"] == 1
        assert mocked.call_args.kwargs["page"] == 2
        assert mocked.call_args.kwargs["limit"] == 10

    def test_get_unwraps_the_detail(self):
        with patch_api(TemplatesApi, "api_v1_field_templates_id_get", return_value=ok({"id": FT_ID, "name": "Kira"})) as mocked:
            assert client().field_templates.get(FT_ID) == {"id": FT_ID, "name": "Kira"}
        assert mocked.call_args.kwargs["id"] == FT_ID

    def test_preview_layout_sends_files_and_on_anchor_miss(self):
        with patch_api(
            DemandsApi, "api_v1_field_templates_id_preview_layout_post", return_value=ok({"placements_summary": {"total": 2}})
        ) as mocked:
            result = client().field_templates.preview_layout(FT_ID, files=[PDF], on_anchor_miss="drop")
        kwargs = mocked.call_args.kwargs
        assert kwargs["id"] == FT_ID
        assert kwargs["files"] == [("sozlesme.pdf", b"%PDF-1.7")]
        assert kwargs["on_anchor_miss"] == "drop"
        assert result == {"placements_summary": {"total": 2}}

    def test_preview_layout_is_not_retried_on_429(self):
        with patch_api(DemandsApi, "api_v1_field_templates_id_preview_layout_post", side_effect=rate_limited()) as mocked:
            with pytest.raises(ImzalaRateLimitError):
                Imzala(api_key="imz_test", max_retries=2).field_templates.preview_layout(FT_ID, files=[PDF])
        assert mocked.call_count == 1


class TestContacts:
    def test_list_forwards_filters(self):
        with patch_api(ContactsApi, "api_v1_contacts_get", return_value=ok({"contacts": [], "total": 0})) as mocked:
            client().contacts.list(q="Ayşe", archived=False, company_id="c1", sort="createdAt:desc")
        kwargs = mocked.call_args.kwargs
        assert kwargs["q"] == "Ayşe"
        assert kwargs["archived"] is False
        assert kwargs["company_id"] == "c1"
        assert kwargs["sort"] == "createdAt:desc"
        assert kwargs["page"] is None
        assert kwargs["limit"] is None

    def test_list_all_walks_every_page(self):
        with patch_api(
            ContactsApi,
            "api_v1_contacts_get",
            side_effect=[
                ok({"contacts": [{"id": "a"}, {"id": "b"}], "total": 3, "page": 1, "limit": 2}),
                ok({"contacts": [{"id": "c"}], "total": 3, "page": 2, "limit": 2}),
            ],
        ) as mocked:
            ids = [c["id"] for c in client().contacts.list_all(limit=2, q="Ayşe")]
        assert ids == ["a", "b", "c"]
        assert mocked.call_count == 2
        assert [call.kwargs["page"] for call in mocked.call_args_list] == [1, 2]
        assert all(call.kwargs["q"] == "Ayşe" for call in mocked.call_args_list)

    def test_create_sends_the_body_and_is_not_retried_on_429(self):
        with patch_api(ContactsApi, "api_v1_contacts_post", side_effect=rate_limited()) as mocked:
            with pytest.raises(ImzalaRateLimitError):
                Imzala(api_key="imz_test", max_retries=2).contacts.create(PARTY)
        assert mocked.call_count == 1
        assert mocked.call_args.kwargs["api_v1_contacts_post_request"]["email"] == "ayse@example.com"

    def test_create_takes_no_idempotency_key(self):
        with pytest.raises(TypeError):
            client().contacts.create(PARTY, idempotency_key="k")


class TestReports:
    def test_get_unwraps_the_counts(self):
        with patch_api(ReportsApi, "api_v1_reports_get", return_value=ok({"contracts": {"pending": 2}})):
            assert client().reports.get() == {"contracts": {"pending": 2}}


class TestTimestamps:
    def test_list_forwards_filters_and_get_unwraps_one_record(self):
        with patch_api(TimestampsApi, "api_v1_timestamps_get", return_value=ok({"timestamps": [], "total": 0})) as listed, patch_api(
            TimestampsApi, "api_v1_timestamps_id_get", return_value=ok({"id": "ts1"})
        ) as got:
            client().timestamps.list(limit=10, status="COMPLETED", from_="2026-01-01", to="2026-02-01", q="eser", sort="createdAt:desc", page=1)
            assert client().timestamps.get("ts1") == {"id": "ts1"}
        kwargs = listed.call_args.kwargs
        assert kwargs["limit"] == 10
        assert kwargs["status"] == "COMPLETED"
        assert kwargs["var_from"] == "2026-01-01"
        assert kwargs["to"] == "2026-02-01"
        assert got.call_args.kwargs["id"] == "ts1"

    def test_create_with_an_idempotency_key_is_retried_once_after_429(self):
        with patch_api(TimestampsApi, "api_v1_timestamps_post", side_effect=[rate_limited(), ok({"id": "ts1"})]) as mocked:
            result = client().timestamps.create(content=b"%PDF", filename="eser.pdf", idempotency_key="k-ts")
        assert result == {"id": "ts1"}
        assert mocked.call_count == 2
        assert all(call.kwargs["idempotency_key"] == "k-ts" for call in mocked.call_args_list)

    def test_create_without_an_idempotency_key_is_not_retried(self):
        with patch_api(TimestampsApi, "api_v1_timestamps_post", side_effect=rate_limited()) as mocked:
            with pytest.raises(ImzalaRateLimitError):
                client().timestamps.create(content=b"%PDF", filename="eser.pdf")
        assert mocked.call_count == 1

    def test_create_with_a_key_is_not_retried_on_5xx(self):
        exc = ApiException(status=503, reason="Service Unavailable", body=json.dumps({"success": False}))
        with patch_api(TimestampsApi, "api_v1_timestamps_post", side_effect=exc) as mocked:
            with pytest.raises(Exception):
                client().timestamps.create(content=b"%PDF", filename="eser.pdf", idempotency_key="k-ts")
        assert mocked.call_count == 1


class TestDemandsWriteOptions:
    def test_create_passes_the_idempotency_key_and_retries_once_after_429(self):
        with patch_api(DemandsApi, "api_v1_demands_post", side_effect=[rate_limited(), ok({"id": "d1"})]) as mocked:
            result = client().demands.create({"template_id": "t1", "party_mapping": []}, idempotency_key="order-42")
        assert result == {"id": "d1"}
        assert mocked.call_count == 2
        assert mocked.call_args_list[0].kwargs["idempotency_key"] == "order-42"

    def test_create_without_a_key_keeps_the_single_attempt_behaviour(self):
        with patch_api(DemandsApi, "api_v1_demands_post", side_effect=rate_limited()) as mocked:
            with pytest.raises(ImzalaRateLimitError):
                client().demands.create({"template_id": "t1", "party_mapping": []})
        assert mocked.call_count == 1
        assert mocked.call_args.kwargs["idempotency_key"] is None

    def test_upload_document_forwards_the_new_optional_fields(self):
        with patch_api(DemandsApi, "api_v1_demands_upload_post", return_value=ok({"id": "d2"})) as mocked:
            client().demands.upload_document(
                files=[PDF],
                parties=[PARTY],
                idempotency_key="up-1",
                field_template_id=FT_ID,
                on_anchor_miss="block",
                send_invitations="email",
                force=True,
            )
        kwargs = mocked.call_args.kwargs
        assert kwargs["idempotency_key"] == "up-1"
        assert kwargs["field_template_id"] == FT_ID
        assert kwargs["on_anchor_miss"] == "block"
        assert kwargs["send_invitations"] == "email"
        assert kwargs["force"] == "true"

    def test_upload_document_omits_the_new_fields_when_not_requested(self):
        with patch_api(DemandsApi, "api_v1_demands_upload_post", return_value=ok({"id": "d2"})) as mocked:
            client().demands.upload_document(files=[PDF], parties=[PARTY], force=False)
        kwargs = mocked.call_args.kwargs
        assert kwargs["force"] is None
        assert kwargs["send_invitations"] is None
        assert kwargs["idempotency_key"] is None
        assert kwargs["field_template_id"] is None
        assert kwargs["on_anchor_miss"] is None

    def test_upload_document_typed_party_carries_template_party_id(self):
        from imzala.files import UploadPartyInput

        with patch_api(DemandsApi, "api_v1_demands_upload_post", return_value=ok({"id": "d2"})) as mocked:
            client().demands.upload_document(
                files=[PDF],
                field_template_id=FT_ID,
                parties=[
                    UploadPartyInput(
                        first_name="Ayşe", last_name="Yılmaz", email="ayse@example.com", template_party_id="role-1"
                    )
                ],
            )
        parties = json.loads(mocked.call_args.kwargs["parties"])
        assert parties[0]["template_party_id"] == "role-1"

    def test_upload_document_with_a_key_is_retried_once_after_429(self):
        with patch_api(DemandsApi, "api_v1_demands_upload_post", side_effect=[rate_limited(), ok({"id": "d2"})]) as mocked:
            client().demands.upload_document(files=[PDF], parties=[PARTY], idempotency_key="up-2")
        assert mocked.call_count == 2

    def test_upload_document_without_a_key_is_not_retried(self):
        with patch_api(DemandsApi, "api_v1_demands_upload_post", side_effect=rate_limited()) as mocked:
            with pytest.raises(ImzalaRateLimitError):
                client().demands.upload_document(files=[PDF], parties=[PARTY])
        assert mocked.call_count == 1

    def test_create_bulk_sends_the_body_and_is_not_retried_on_429(self):
        with patch_api(DemandsApi, "api_v1_demands_bulk_post", side_effect=rate_limited()) as mocked:
            with pytest.raises(ImzalaRateLimitError):
                Imzala(api_key="imz_test", max_retries=2).demands.create_bulk({"template_id": "t1", "rows": []})
        assert mocked.call_count == 1
        assert mocked.call_args.kwargs["api_v1_demands_bulk_post_request"]["template_id"] == "t1"

    def test_create_bulk_takes_no_idempotency_key(self):
        with pytest.raises(TypeError):
            client().demands.create_bulk({"template_id": "t1", "rows": []}, idempotency_key="k")

    def test_get_document_pdf_returns_the_raw_bytes_of_one_envelope_document(self):
        with patch_api(DemandsApi, "api_v1_demands_id_belge_document_id_pdf_get", return_value=b"%PDF-1.7") as mocked:
            out = client().demands.get_document_pdf("d1", "doc1")
        assert out[:5] == b"%PDF-"
        assert mocked.call_args.kwargs["id"] == "d1"
        assert mocked.call_args.kwargs["document_id"] == "doc1"


class TestDemandsDocumentSelection:
    PARTY_MAPPING = [{"template_party_id": "role-1", **PARTY, "phone": "+905551112233"}]

    def test_create_forwards_the_document_selection_untouched(self):
        with patch_api(DemandsApi, "api_v1_demands_post", return_value=ok({"id": "d1"})) as mocked:
            client().demands.create(
                {
                    "template_id": "t1",
                    "party_mapping": self.PARTY_MAPPING,
                    "documents": {"include": ["doc-1"], "exclude": ["doc-2", "doc-3"]},
                }
            )
        body = mocked.call_args.kwargs["create_demand_request"]
        assert body == {
            "template_id": "t1",
            "party_mapping": self.PARTY_MAPPING,
            "documents": {"include": ["doc-1"], "exclude": ["doc-2", "doc-3"]},
        }

    def test_create_without_a_selection_sends_the_same_body_as_before(self):
        with patch_api(DemandsApi, "api_v1_demands_post", return_value=ok({"id": "d1"})) as mocked:
            client().demands.create({"template_id": "t1", "party_mapping": self.PARTY_MAPPING})
        body = mocked.call_args.kwargs["create_demand_request"]
        assert body == {"template_id": "t1", "party_mapping": self.PARTY_MAPPING}
        assert "documents" not in body

    def test_create_bulk_carries_the_selection_per_row(self):
        with patch_api(DemandsApi, "api_v1_demands_bulk_post", return_value=ok({"created": 1, "failed": 0})) as mocked:
            client().demands.create_bulk(
                {
                    "template_id": "t1",
                    "rows": [
                        {"party_mapping": self.PARTY_MAPPING, "documents": {"exclude": ["doc-2"]}},
                        {"party_mapping": self.PARTY_MAPPING},
                    ],
                }
            )
        rows = mocked.call_args.kwargs["api_v1_demands_bulk_post_request"]["rows"]
        assert rows[0]["documents"] == {"exclude": ["doc-2"]}
        assert "documents" not in rows[1]

    def test_templates_get_returns_the_documents_the_ids_come_from(self):
        data = {
            "id": "t1",
            "documents": [
                {
                    "id": "doc-1",
                    "order": 1,
                    "title": "Sözleşme",
                    "doc_kind": "CONTRACT",
                    "is_required": True,
                    "signature_required": True,
                    "default_included": True,
                    "assigned_template_party_ids": ["role-1"],
                }
            ],
        }
        with patch_api(TemplatesApi, "api_v1_templates_id_get", return_value=ok(data)):
            template = client().templates.get("t1")
        assert template["documents"][0]["id"] == "doc-1"
        assert template["documents"][0]["default_included"] is True

    def test_a_rejected_selection_raises_a_coded_imzala_error(self):
        exc = ApiException(
            status=400,
            reason="Bad Request",
            body=json.dumps(
                {
                    "success": False,
                    "error": "Belge seçimi geçersiz",
                    "code": "INVALID_DOCUMENT_SELECTION",
                    "details": {"reason": "unknown_document", "document_ids": ["doc-9"]},
                }
            ),
        )
        with patch_api(DemandsApi, "api_v1_demands_post", side_effect=exc):
            with pytest.raises(ImzalaError) as raised:
                client().demands.create(
                    {"template_id": "t1", "party_mapping": self.PARTY_MAPPING, "documents": {"include": ["doc-9"]}}
                )
        assert raised.value.status_code == 400
        assert raised.value.code == "INVALID_DOCUMENT_SELECTION"
        assert "unknown_document" in raised.value.code_description
        assert raised.value.body["details"] == {"reason": "unknown_document", "document_ids": ["doc-9"]}

    def test_a_party_left_without_documents_raises_a_coded_imzala_error(self):
        exc = ApiException(
            status=409,
            reason="Conflict",
            body=json.dumps(
                {
                    "success": False,
                    "error": "Eşlenen bir tarafa imzalayacak belge düşmüyor",
                    "code": "PARTY_WITHOUT_DOCUMENTS",
                    "template_party_ids": ["role-2"],
                }
            ),
        )
        with patch_api(DemandsApi, "api_v1_demands_post", side_effect=exc):
            with pytest.raises(ImzalaError) as raised:
                client().demands.create(
                    {"template_id": "t1", "party_mapping": self.PARTY_MAPPING, "documents": {"exclude": ["doc-2"]}}
                )
        assert raised.value.status_code == 409
        assert raised.value.code == "PARTY_WITHOUT_DOCUMENTS"
