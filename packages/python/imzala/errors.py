"""Typed exceptions for the imzala.org Python SDK.

Mirrors the TypeScript `@imzala/node` facade's error taxonomy
(`packages/node/src/errors.ts` in this monorepo) so server code behaves
the same regardless of language.
"""

from __future__ import annotations

import json
from typing import Any, Mapping, Optional

from imzala_client.exceptions import ApiException

__all__ = [
    "ImzalaError",
    "ImzalaAuthError",
    "ImzalaRateLimitError",
    "ImzalaValidationError",
    "extract_error_message",
    "extract_error_code",
    "map_api_exception",
]


class ImzalaError(Exception):
    """Base exception raised by every `imzala` facade method.

    Normalizes the generated client's `ApiException` (and any other
    failure — network error, an unexpected `success=False` body on an
    otherwise-2xx response, ...) into a single, consistently-shaped
    exception. Raised directly (not as a subclass) for statuses that
    don't have a dedicated subclass below (400, 404, 409, 500, ...).
    """

    def __init__(
        self,
        message: str,
        *,
        status_code: Optional[int] = None,
        body: Any = None,
        code: Optional[str] = None,
    ) -> None:
        super().__init__(message)
        self.status_code = status_code
        self.body = body
        self.code = code

    def __repr__(self) -> str:  # pragma: no cover - cosmetic only
        return (
            f"{type(self).__name__}({str(self)!r}, "
            f"status_code={self.status_code!r}, code={self.code!r})"
        )


class ImzalaAuthError(ImzalaError):
    """Missing/invalid API key (401) or disabled key / insufficient scope (403)."""


class ImzalaRateLimitError(ImzalaError):
    """Rate limited (429). `retry_after` is seconds, when the server provided one."""

    def __init__(
        self,
        message: str,
        *,
        status_code: Optional[int] = None,
        body: Any = None,
        code: Optional[str] = None,
        retry_after: Optional[float] = None,
    ) -> None:
        super().__init__(message, status_code=status_code, body=body, code=code)
        self.retry_after = retry_after


class ImzalaValidationError(ImzalaError):
    """Request payload failed validation (422)."""


def _as_mapping(value: Any) -> Optional[Mapping[str, Any]]:
    return value if isinstance(value, Mapping) else None


def extract_error_message(body: Any) -> Optional[str]:
    """imzala.org error envelopes aren't fully uniform across endpoints:
    most are `{success: false, error: "<code>", message: "<text>"}`, but
    some (e.g. the reminders 429) nest a `{code, message,
    retry_after_seconds}` object under `error` instead of a plain string.
    Handles both shapes.
    """
    b = _as_mapping(body)
    if not b:
        return None
    if isinstance(b.get("message"), str):
        return b["message"]
    if isinstance(b.get("error"), str):
        return b["error"]
    nested = _as_mapping(b.get("error"))
    if nested:
        if isinstance(nested.get("message"), str):
            return nested["message"]
        if isinstance(nested.get("code"), str):
            return nested["code"]
    return None


def extract_error_code(body: Any) -> Optional[str]:
    b = _as_mapping(body)
    if not b:
        return None
    if isinstance(b.get("error"), str):
        return b["error"]
    nested = _as_mapping(b.get("error"))
    if nested and isinstance(nested.get("code"), str):
        return nested["code"]
    return None


def _extract_retry_after(body: Any, headers: Any) -> Optional[float]:
    b = _as_mapping(body)
    direct = b.get("retry_after_seconds") if b else None
    if isinstance(direct, (int, float)) and not isinstance(direct, bool):
        return float(direct)

    nested = _as_mapping(b.get("error")) if b else None
    nested_retry = nested.get("retry_after_seconds") if nested else None
    if isinstance(nested_retry, (int, float)) and not isinstance(nested_retry, bool):
        return float(nested_retry)

    if headers is not None and hasattr(headers, "get"):
        header = headers.get("Retry-After")
        if header is not None:
            try:
                return float(header)
            except (TypeError, ValueError):
                pass
    return None


def _parse_body(raw_body: Any) -> Any:
    """`ApiException.body` is the raw response text — try to JSON-decode
    it, falling back to the raw value so callers still get *something*
    useful even for a non-JSON error body."""
    if raw_body is None:
        return None
    if isinstance(raw_body, (dict, list)):
        return raw_body
    if isinstance(raw_body, (bytes, bytearray)):
        try:
            raw_body = raw_body.decode("utf-8")
        except UnicodeDecodeError:
            return raw_body
    if isinstance(raw_body, str):
        try:
            return json.loads(raw_body)
        except (ValueError, TypeError):
            return raw_body
    return raw_body


def map_api_exception(err: BaseException) -> ImzalaError:
    """Maps a raw `ApiException` from the generated client (or any other
    thrown value) to the appropriate `ImzalaError` subclass, based on HTTP
    status code. Mirrors `mapAxiosError` in the TypeScript facade.
    """
    if isinstance(err, ImzalaError):
        return err

    if isinstance(err, ApiException):
        status = err.status
        body = _parse_body(err.body)
        headers = err.headers
        message = (
            extract_error_message(body)
            or (str(err.reason) if err.reason else None)
            or "imzala.org API request failed"
        )
        code = extract_error_code(body)

        if status in (401, 403):
            return ImzalaAuthError(message, status_code=status, body=body, code=code)
        if status == 429:
            return ImzalaRateLimitError(
                message,
                status_code=status,
                body=body,
                code=code,
                retry_after=_extract_retry_after(body, headers),
            )
        if status == 422:
            return ImzalaValidationError(message, status_code=status, body=body, code=code)
        return ImzalaError(message, status_code=status, body=body, code=code)

    if isinstance(err, Exception):
        return ImzalaError(str(err) or "imzala.org API request failed")

    return ImzalaError("Unknown error calling the imzala.org API")
