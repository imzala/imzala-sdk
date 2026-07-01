"""Webhook signature verification for imzala.org webhook deliveries."""

from __future__ import annotations

import hashlib
import hmac
from typing import Optional, Union

__all__ = ["verify_webhook"]


def verify_webhook(
    secret: str,
    raw_body: Union[str, bytes, bytearray],
    signature_header: Optional[str],
) -> bool:
    """Verifies an imzala.org webhook delivery's `X-Imzala-Signature-256` header.

    Mirrors the backend algorithm exactly (`src/services/webhook/WebhookSigner.ts`
    in imzala-service): `'sha256=' + HMAC-SHA256(raw_body, secret)` as
    lowercase hex, compared with a timing-safe equality check.

    Args:
        secret: the `whsec_<64-hex>` secret shown once when the webhook was
            created in the dashboard.
        raw_body: the **exact, unparsed** request body bytes/text. Parsing
            the JSON and re-serializing it can change byte-for-byte content
            (key order, whitespace) and break verification — read the raw
            body before your framework parses it (e.g. `request.data` in
            Flask, `await request.body()` in FastAPI/Starlette,
            `request.body` in Django).
        signature_header: the raw `X-Imzala-Signature-256` header value
            (e.g. `"sha256=<hex>"`).

    Returns:
        `True` if the signature is valid. Returns `False` — never raises —
        for a wrong signature, a malformed/missing header, or any other
        verification failure.
    """
    if not secret or not signature_header:
        return False

    try:
        if isinstance(raw_body, str):
            body_bytes = raw_body.encode("utf-8")
        elif isinstance(raw_body, (bytes, bytearray)):
            body_bytes = bytes(raw_body)
        else:
            return False

        if not isinstance(signature_header, str):
            return False

        digest = hmac.new(secret.encode("utf-8"), body_bytes, hashlib.sha256).hexdigest()
        expected = f"sha256={digest}"

        expected_bytes = expected.encode("utf-8")
        actual_bytes = signature_header.encode("utf-8")

        # Python's hmac.compare_digest doesn't throw on a length mismatch
        # (unlike Node's crypto.timingSafeEqual), but checking first keeps
        # this function's control flow symmetric with the Node facade and
        # makes the "never throws" contract obvious at a glance.
        if len(expected_bytes) != len(actual_bytes):
            return False

        return hmac.compare_digest(expected_bytes, actual_bytes)
    except Exception:
        return False
