"""Typed exceptions for the imzala.org Python SDK.

Mirrors the TypeScript `@imzala/node` facade's error taxonomy
(`packages/node/src/errors.ts` in this monorepo) so server code behaves
the same regardless of language.
"""

from __future__ import annotations

import json
import math
import re
import time
from dataclasses import dataclass
from email.utils import parsedate_to_datetime
from typing import Any, Mapping, Optional

from imzala_client.exceptions import ApiException

from .error_codes import describe_error_code

__all__ = [
    "ImzalaError",
    "ImzalaAuthError",
    "ImzalaRateLimitError",
    "ImzalaValidationError",
    "ImzalaRateLimitInfo",
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
        #: Machine-readable code, e.g. `TEMPLATE_IN_USE`. None when the response carried none.
        self.code = code
        #: One-line explanation of `code` from the SDK's catalogue; None for codes it does not know.
        self.code_description = describe_error_code(code)

    def __repr__(self) -> str:  # pragma: no cover - cosmetic only
        return (
            f"{type(self).__name__}({str(self)!r}, "
            f"status_code={self.status_code!r}, code={self.code!r})"
        )


class ImzalaAuthError(ImzalaError):
    """Missing/invalid API key (401) or disabled key / insufficient scope (403)."""


@dataclass(frozen=True)
class ImzalaRateLimitInfo:
    """Standard `RateLimit-*` response headers. The server does not send `X-RateLimit-*`.

    Attributes:
        limit: `RateLimit-Limit`, requests allowed per window. Defaults to 60
            but can be lowered per API key, so read it rather than assuming.
        remaining: `RateLimit-Remaining`, requests left in the current window.
        reset: `RateLimit-Reset`, seconds until the window resets.
        policy: `RateLimit-Policy`, raw policy string, e.g. `60;w=60`.
    """

    limit: Optional[float] = None
    remaining: Optional[float] = None
    reset: Optional[float] = None
    policy: Optional[str] = None


class ImzalaRateLimitError(ImzalaError):
    """Rate limited (429). `retry_after` is seconds, when the server provided one.

    Several different limits answer with 429 and each has its own `code`
    (e.g. `RATE_LIMIT_EXCEEDED`, `TOO_MANY_REQUESTS`, `RATE_LIMITED`,
    `RECIPIENT_RESEND_LIMIT`, `MAX_SMS_REMINDERS_REACHED`), so branch on this
    class or on `status_code`, not on one particular code.
    `rate_limit` carries the `RateLimit-*` headers, or None when absent.
    """

    def __init__(
        self,
        message: str,
        *,
        status_code: Optional[int] = None,
        body: Any = None,
        code: Optional[str] = None,
        retry_after: Optional[float] = None,
        rate_limit: Optional[ImzalaRateLimitInfo] = None,
    ) -> None:
        super().__init__(message, status_code=status_code, body=body, code=code)
        self.retry_after = retry_after
        self.rate_limit = rate_limit


class ImzalaValidationError(ImzalaError):
    """Request payload failed validation (422)."""


def _as_mapping(value: Any) -> Optional[Mapping[str, Any]]:
    return value if isinstance(value, Mapping) else None


def extract_error_message(body: Any) -> Optional[str]:
    """imzala.org error envelopes come in three shapes:

    - `{success: false, error: "<CODE>", message: "<text>"}`
    - `{success: false, error: "<text>", code: "<CODE>"}` (rate limits, coded errors)
    - `{success: false, error: {code, message, retry_after_seconds}}` (reminders)

    and some errors carry only a human-readable `error` string with no code.
    This and `extract_error_code` handle all of them.
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


# Upper case, digits and underscores only, at least three characters: covers
# `TEMPLATE_IN_USE`, `BULK_MAX_10` and single-word codes such as `UNAUTHORIZED`,
# never a human-readable sentence.
_CODE_SHAPE = re.compile(r"^[A-Z][A-Z0-9_]{2,}$")


def extract_error_code(body: Any) -> Optional[str]:
    """Reads the machine-readable code: `code` first, then `error` only when
    it is shaped like a code, then a nested `error.code`. A human-readable
    `error` sentence is never returned as a code."""
    b = _as_mapping(body)
    if not b:
        return None
    if isinstance(b.get("code"), str):
        return b["code"]
    if isinstance(b.get("error"), str):
        return b["error"] if _CODE_SHAPE.match(b["error"]) else None
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

    header = _header(headers, "retry-after")
    if header is not None:
        try:
            return float(header)
        except (TypeError, ValueError):
            pass
        # Retry-After may also be an HTTP date.
        try:
            at = parsedate_to_datetime(str(header))
        except (TypeError, ValueError, IndexError):
            return None
        if at is None:
            return None
        return float(max(0, math.ceil(at.timestamp() - time.time())))
    return None


def _header(headers: Any, name: str) -> Any:
    """Case-insensitive header lookup that works for a plain dict and for
    urllib3's `HTTPHeaderDict` (which the generated client attaches to
    `ApiException.headers`)."""
    if headers is None or not hasattr(headers, "items"):
        return None
    wanted = name.lower()
    for key, value in headers.items():
        if isinstance(key, str) and key.lower() == wanted:
            return value
    return None


def _number(raw: Any) -> Optional[float]:
    if raw is None or raw == "":
        return None
    try:
        value = float(raw)
    except (TypeError, ValueError):
        return None
    if math.isnan(value):
        return None
    return int(value) if value.is_integer() else value


def _extract_rate_limit_info(headers: Any) -> Optional[ImzalaRateLimitInfo]:
    policy = _header(headers, "ratelimit-policy")
    info = ImzalaRateLimitInfo(
        limit=_number(_header(headers, "ratelimit-limit")),
        remaining=_number(_header(headers, "ratelimit-remaining")),
        reset=_number(_header(headers, "ratelimit-reset")),
        policy=policy if isinstance(policy, str) else None,
    )
    if info == ImzalaRateLimitInfo():
        return None
    return info


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
                rate_limit=_extract_rate_limit_info(headers),
            )
        if status == 422:
            return ImzalaValidationError(message, status_code=status, body=body, code=code)
        return ImzalaError(message, status_code=status, body=body, code=code)

    if isinstance(err, Exception):
        return ImzalaError(str(err) or "imzala.org API request failed")

    return ImzalaError("Unknown error calling the imzala.org API")
