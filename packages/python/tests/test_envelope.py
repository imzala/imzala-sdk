"""Multi-document envelope endpoints and dispatch. Mirrors
`packages/node/src/__tests__/envelope.test.ts`.

These tests run the real generated client against a local HTTP server, so
they check what actually goes over the wire: the path slots, the multipart
field that carries the idempotency key, and how a 409 replay body is read.
"""

from __future__ import annotations

import json
import re
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

import pytest
import urllib3

from imzala import FileInput, Imzala
from imzala.errors import ImzalaError, ImzalaRateLimitError, ImzalaValidationError

# The generated client targets urllib3 2.x (pyproject); on an older local
# urllib3 the real transport cannot even be constructed. CI runs 2.x.
pytestmark = pytest.mark.skipif(
    int(urllib3.__version__.split(".")[0]) < 2, reason="generated client requires urllib3 >= 2"
)

DEMAND = "11111111-1111-4111-8111-111111111111"
DOC = "22222222-2222-4222-8222-222222222222"
DOC_B = "33333333-3333-4333-8333-333333333333"
PARTY = "44444444-4444-4444-8444-444444444444"

PDF = FileInput(content=b"%PDF-1.7 test", filename="kira.pdf", content_type="application/pdf")
DOCUMENT = {"id": DOC, "order": 1, "title": "Kira sözleşmesi", "doc_kind": "CONTRACT"}


def ok(data, status=200):
    return {"status": status, "body": {"success": True, "data": data}}


RATE_LIMITED = {
    "status": 429,
    "body": {"success": False, "error": "Çok fazla istek", "code": "RATE_LIMIT_EXCEEDED"},
    "headers": {"Retry-After": "0"},
}
DISABLED = {"status": 409, "body": {"success": False, "error": "Kapalı", "code": "ENVELOPE_MULTI_DOC_DISABLED"}}


class Server:
    def __init__(self, replies):
        self.replies = replies
        self.calls = []
        outer = self

        class Handler(BaseHTTPRequestHandler):
            def _handle(self):
                length = int(self.headers.get("Content-Length") or 0)
                body = self.rfile.read(length) if length else b""
                outer.calls.append(
                    {"method": self.command, "path": self.path, "headers": dict(self.headers), "body": body}
                )
                reply = outer.replies[min(len(outer.calls) - 1, len(outer.replies) - 1)]
                payload = json.dumps(reply["body"]).encode("utf-8")
                self.send_response(reply["status"])
                self.send_header("Content-Type", "application/json")
                self.send_header("Content-Length", str(len(payload)))
                for key, value in reply.get("headers", {}).items():
                    self.send_header(key, value)
                self.end_headers()
                self.wfile.write(payload)

            do_GET = do_POST = do_PUT = do_PATCH = do_DELETE = _handle

            def log_message(self, *args):  # keep test output quiet
                pass

        self.httpd = ThreadingHTTPServer(("127.0.0.1", 0), Handler)
        self.thread = threading.Thread(target=self.httpd.serve_forever, daemon=True)
        self.thread.start()
        self.base_url = f"http://127.0.0.1:{self.httpd.server_address[1]}"

    def close(self):
        self.httpd.shutdown()
        self.httpd.server_close()


@pytest.fixture
def serve():
    servers = []

    def start(*replies):
        server = Server(list(replies))
        servers.append(server)
        return server

    yield start
    for server in servers:
        server.close()


def client(server):
    return Imzala(api_key="imz_test", base_url=server.base_url, max_retries=2, retry_base_delay=0.001)


def form_field(body: bytes, name: str):
    match = re.search(
        rb'name="' + name.encode() + rb'"\r\n(?:[^\r\n]+\r\n)*\r\n(.*?)\r\n--', body, re.DOTALL
    )
    return match.group(1).decode("utf-8") if match else None


def as_dict(value):
    """Generated models as plain JSON (UUIDs as strings, unset optional
    fields dropped) so results compare against the served body."""
    if not hasattr(value, "to_json"):
        return value
    return _drop_none(json.loads(value.to_json()))


def _drop_none(value):
    if isinstance(value, dict):
        return {k: _drop_none(v) for k, v in value.items() if v is not None}
    if isinstance(value, list):
        return [_drop_none(v) for v in value]
    return value


def json_body(call):
    return json.loads(call["body"]) if call["body"] else {}


class TestWire:
    def test_list_sends_view_query_and_unwraps(self, serve):
        srv = serve(ok({"documents": [DOCUMENT]}))
        result = client(srv).demands.documents.list(DEMAND, view="wizard")
        assert as_dict(result) == {"documents": [DOCUMENT]}
        assert srv.calls[0]["method"] == "GET"
        assert srv.calls[0]["path"] == f"/api/v1/demands/{DEMAND}/documents?view=wizard"

    def test_create_posts_json_body(self, serve):
        srv = serve(ok({"document": DOCUMENT}, status=201))
        result = client(srv).demands.documents.create(
            DEMAND, {"title": "KVKK aydınlatma", "doc_kind": "KVKK_NOTICE", "is_required": False, "signature_required": False}
        )
        assert as_dict(result) == {"document": DOCUMENT}
        assert srv.calls[0]["method"] == "POST"
        assert srv.calls[0]["path"] == f"/api/v1/demands/{DEMAND}/documents"
        assert json_body(srv.calls[0]) == {
            "title": "KVKK aydınlatma",
            "doc_kind": "KVKK_NOTICE",
            "is_required": False,
            "signature_required": False,
        }

    def test_update_keeps_demand_and_doc_in_their_slots(self, serve):
        srv = serve(ok({"document": DOCUMENT}))
        client(srv).demands.documents.update(DEMAND, DOC, {"title": "Yeni başlık"})
        assert srv.calls[0]["method"] == "PATCH"
        assert srv.calls[0]["path"] == f"/api/v1/demands/{DEMAND}/documents/{DOC}"
        assert json_body(srv.calls[0]) == {"title": "Yeni başlık"}

    def test_delete_keeps_demand_and_doc_in_their_slots(self, serve):
        srv = serve(ok({"id": DOC, "deleted": True}))
        result = client(srv).demands.documents.delete(DEMAND, DOC)
        assert as_dict(result) == {"id": DOC, "deleted": True}
        assert srv.calls[0]["method"] == "DELETE"
        assert srv.calls[0]["path"] == f"/api/v1/demands/{DEMAND}/documents/{DOC}"

    def test_reorder_sends_document_ids_in_order(self, serve):
        srv = serve(ok({"documents": [DOCUMENT]}))
        client(srv).demands.documents.reorder(DEMAND, [DOC_B, DOC])
        assert srv.calls[0]["method"] == "PUT"
        assert srv.calls[0]["path"] == f"/api/v1/demands/{DEMAND}/documents/order"
        assert json_body(srv.calls[0]) == {"document_ids": [DOC_B, DOC]}

    def test_set_assignments_keeps_slots_and_sends_party_ids(self, serve):
        srv = serve(ok({"document": DOCUMENT}))
        client(srv).demands.documents.set_assignments(DEMAND, DOC, [PARTY])
        assert srv.calls[0]["method"] == "PUT"
        assert srv.calls[0]["path"] == f"/api/v1/demands/{DEMAND}/documents/{DOC}/assignments"
        assert json_body(srv.calls[0]) == {"party_ids": [PARTY]}

    def test_dispatch_sends_no_send_invitations_by_default(self, serve):
        srv = serve(ok({"demand_id": DEMAND, "status": "PENDING", "dispatched": True}))
        result = client(srv).demands.dispatch(DEMAND)
        assert as_dict(result)["dispatched"] is True
        assert srv.calls[0]["method"] == "POST"
        assert srv.calls[0]["path"] == f"/api/v1/demands/{DEMAND}/dispatch"
        assert "send_invitations" not in json_body(srv.calls[0])

    @pytest.mark.parametrize("value", [False, True, "email", "hayır"])
    def test_dispatch_forwards_send_invitations_unchanged(self, serve, value):
        srv = serve(ok({"dispatched": True}))
        client(srv).demands.dispatch(DEMAND, send_invitations=value)
        assert json_body(srv.calls[0]) == {"send_invitations": value}


class TestUpload:
    def test_key_travels_as_body_field_not_header(self, serve):
        srv = serve(ok({"document": DOCUMENT}))
        result = client(srv).demands.documents.upload(
            DEMAND, file=PDF, title="Kira sözleşmesi", idempotency_key="siparis-42", doc_kind="CONTRACT", is_required=False
        )
        assert as_dict(result) == {"document": DOCUMENT}
        call = srv.calls[0]
        assert call["path"] == f"/api/v1/demands/{DEMAND}/documents/upload"
        assert "Idempotency-Key" not in call["headers"]
        assert form_field(call["body"], "idempotency_key") == "siparis-42"
        assert form_field(call["body"], "title") == "Kira sözleşmesi"
        assert form_field(call["body"], "doc_kind") == "CONTRACT"
        assert form_field(call["body"], "is_required") == "false"
        assert b'filename="kira.pdf"' in call["body"]
        assert b"%PDF-1.7 test" in call["body"]

    @pytest.mark.parametrize("key", [None, "", "sipariş-1", "a\r\nX-Evil: 1"])
    def test_bad_key_is_rejected_locally(self, serve, key):
        srv = serve(ok({"document": DOCUMENT}))
        with pytest.raises(ImzalaValidationError):
            client(srv).demands.documents.upload(DEMAND, file=PDF, title="T", idempotency_key=key)
        assert srv.calls == []

    def test_retries_once_after_429_and_sends_the_file_again(self, serve):
        srv = serve(RATE_LIMITED, ok({"document": DOCUMENT}))
        result = client(srv).demands.documents.upload(DEMAND, file=PDF, title="T", idempotency_key="k-1")
        assert as_dict(result) == {"document": DOCUMENT}
        assert len(srv.calls) == 2
        assert b"%PDF-1.7 test" in srv.calls[1]["body"]
        assert form_field(srv.calls[1]["body"], "idempotency_key") == "k-1"

    def test_no_retry_when_retry_after_exceeds_cap(self, serve):
        srv = serve({**RATE_LIMITED, "headers": {"Retry-After": "61"}})
        with pytest.raises(ImzalaRateLimitError):
            client(srv).demands.documents.upload(DEMAND, file=PDF, title="T", idempotency_key="k-1")
        assert len(srv.calls) == 1

    def test_idempotent_replay_returns_earlier_document(self, serve):
        srv = serve(
            {
                "status": 409,
                "body": {
                    "success": False,
                    "error": "Bu belge bu anahtarla zaten yüklendi",
                    "code": "IDEMPOTENT_REPLAY",
                    "data": {"document": DOCUMENT},
                },
            }
        )
        result = client(srv).demands.documents.upload(DEMAND, file=PDF, title="T", idempotency_key="k-1")
        assert type(result).__name__ == "ApiV1DemandsDemandIdDocumentsPost201ResponseData"
        assert as_dict(result) == {"document": DOCUMENT}
        assert len(srv.calls) == 1

    def test_replay_after_429_is_success(self, serve):
        srv = serve(
            RATE_LIMITED,
            {"status": 409, "body": {"success": False, "error": "x", "code": "IDEMPOTENT_REPLAY", "data": {"document": DOCUMENT}}},
        )
        result = client(srv).demands.documents.upload(DEMAND, file=PDF, title="T", idempotency_key="k-1")
        assert as_dict(result) == {"document": DOCUMENT}
        assert len(srv.calls) == 2

    def test_other_409_is_raised(self, serve):
        srv = serve(
            {
                "status": 409,
                "body": {"success": False, "error": "İmza başladı", "code": "SIGNING_ALREADY_STARTED", "data": {"document": DOCUMENT}},
            }
        )
        with pytest.raises(ImzalaError) as info:
            client(srv).demands.documents.upload(DEMAND, file=PDF, title="T", idempotency_key="k-1")
        assert info.value.code == "SIGNING_ALREADY_STARTED"
        assert info.value.status_code == 409

    def test_disabled_is_raised(self, serve):
        srv = serve(DISABLED)
        with pytest.raises(ImzalaError) as info:
            client(srv).demands.documents.upload(DEMAND, file=PDF, title="T", idempotency_key="k-1")
        assert info.value.code == "ENVELOPE_MULTI_DOC_DISABLED"


DOCUMENT_WRITES = [
    ("documents.create", lambda c: c.demands.documents.create(DEMAND, {"title": "T"})),
    ("documents.update", lambda c: c.demands.documents.update(DEMAND, DOC, {"title": "T"})),
    ("documents.delete", lambda c: c.demands.documents.delete(DEMAND, DOC)),
    ("documents.reorder", lambda c: c.demands.documents.reorder(DEMAND, [DOC])),
    ("documents.set_assignments", lambda c: c.demands.documents.set_assignments(DEMAND, DOC, [PARTY])),
]


class TestFeatureSwitchedOff:
    def test_list_raises_instead_of_returning_empty(self, serve):
        srv = serve(DISABLED)
        with pytest.raises(ImzalaError) as info:
            client(srv).demands.documents.list(DEMAND)
        assert info.value.code == "ENVELOPE_MULTI_DOC_DISABLED"
        assert len(srv.calls) == 1

    @pytest.mark.parametrize("name,call", DOCUMENT_WRITES)
    def test_writes_raise_disabled(self, serve, name, call):
        srv = serve(DISABLED)
        with pytest.raises(ImzalaError) as info:
            call(client(srv))
        assert info.value.code == "ENVELOPE_MULTI_DOC_DISABLED"


class TestNoRetryWithoutKey:
    @pytest.mark.parametrize("name,call", DOCUMENT_WRITES + [("dispatch", lambda c: c.demands.dispatch(DEMAND))])
    def test_429_is_raised_after_one_request(self, serve, name, call):
        srv = serve(RATE_LIMITED, ok({}))
        with pytest.raises(ImzalaRateLimitError):
            call(client(srv))
        assert len(srv.calls) == 1

    def test_list_get_is_retried(self, serve):
        srv = serve(RATE_LIMITED, ok({"documents": []}))
        assert as_dict(client(srv).demands.documents.list(DEMAND)) == {"documents": []}
        assert len(srv.calls) == 2


class TestDispatchErrors:
    @pytest.mark.parametrize("code", ["DISPATCH_NO_PARTIES", "DISPATCH_TOO_MANY", "QES_NOT_SUPPORTED_MULTI_DOCUMENT"])
    def test_raises(self, serve, code):
        srv = serve({"status": 409, "body": {"success": False, "error": "x", "code": code}})
        with pytest.raises(ImzalaError) as info:
            client(srv).demands.dispatch(DEMAND)
        assert info.value.code == code
        assert len(srv.calls) == 1


class TestTransportDoesNotRetry:
    """urllib3's default Retry repeats 429/503 answers that carry Retry-After
    before the SDK sees them, writes included. The client turns it off."""

    @pytest.mark.parametrize("status", [429, 503])
    def test_a_create_with_retry_after_is_sent_once(self, serve, status):
        srv = serve({"status": status, "body": {"success": False, "code": "X"}, "headers": {"Retry-After": "0"}}, ok({}))
        with pytest.raises(ImzalaError):
            client(srv).demands.documents.create(DEMAND, {"title": "Ek"})
        assert len(srv.calls) == 1


class TestTimestampsWire:
    """POST /api/v1/timestamps also declares a JSON (base64) body; the
    generated client preferred it and sent an empty body. The facade forces
    multipart. Real client, real server."""

    def test_create_sends_the_file_as_multipart(self, serve):
        srv = serve(ok({"id": DOC, "file_sha256": "abc"}, status=201))
        client(srv).timestamps.create(content=b"%PDF-1.7 test", filename="eser.pdf", idempotency_key="damga-1")
        call = srv.calls[0]
        assert call["path"] == "/api/v1/timestamps"
        assert call["headers"]["Content-Type"].startswith("multipart/form-data")
        assert b'name="file"' in call["body"]
        assert b"%PDF-1.7 test" in call["body"]
        assert call["headers"]["Idempotency-Key"] == "damga-1"
