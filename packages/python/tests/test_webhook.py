import hashlib
import hmac
import json

from imzala.webhook import verify_webhook

# Fixture mirrors the backend's WebhookEnvelope shape
# (src/services/webhook/WebhookSigner.ts in imzala-service) — HMAC-SHA256
# (raw_body, secret) hex digest, 'sha256=' prefixed. Computed here with
# Python's own hmac module to prove interop with the backend's Node
# implementation (same algorithm, different language).
SECRET = "whsec_1e6f1c2b3a4d5e6f7081920a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6"
RAW_BODY = json.dumps(
    {
        "id": "evt_abc123",
        "type": "demand.completed",
        "created_at": "2026-07-01T00:00:00.000Z",
        "data": {"demand_id": "d1"},
    }
)


def sign(body, key: str = SECRET) -> str:
    body_bytes = body.encode("utf-8") if isinstance(body, str) else bytes(body)
    return "sha256=" + hmac.new(key.encode("utf-8"), body_bytes, hashlib.sha256).hexdigest()


def test_returns_true_for_correctly_signed_string_body():
    assert verify_webhook(SECRET, RAW_BODY, sign(RAW_BODY)) is True


def test_returns_true_for_correctly_signed_bytes_body():
    buf = RAW_BODY.encode("utf-8")
    assert verify_webhook(SECRET, buf, sign(buf)) is True
    # cross-check: signature computed over the bytes verifies the string form too
    assert verify_webhook(SECRET, RAW_BODY, sign(buf)) is True


def test_returns_false_for_wrong_secret():
    wrong_secret = "whsec_wrong_00000000000000000000000000000000000000000000000000"
    assert verify_webhook(wrong_secret, RAW_BODY, sign(RAW_BODY)) is False


def test_returns_false_when_body_tampered_with_after_signing():
    tampered = RAW_BODY.replace("d1", "d2-evil")
    assert verify_webhook(SECRET, tampered, sign(RAW_BODY)) is False


def test_returns_false_for_malformed_header_missing_prefix():
    digest_only = sign(RAW_BODY).replace("sha256=", "")
    assert verify_webhook(SECRET, RAW_BODY, digest_only) is False


def test_returns_false_for_garbage_header_same_length_no_throw():
    valid = sign(RAW_BODY)
    garbage = "sha256=" + "0" * (len(valid) - len("sha256="))
    assert verify_webhook(SECRET, RAW_BODY, garbage) is False


def test_returns_false_never_throws_for_missing_or_empty_header():
    assert verify_webhook(SECRET, RAW_BODY, None) is False
    assert verify_webhook(SECRET, RAW_BODY, "") is False


def test_returns_false_for_empty_secret_rather_than_signing_with_empty_key():
    assert verify_webhook("", RAW_BODY, sign(RAW_BODY, "")) is False
