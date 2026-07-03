"""imzala.org server-side SDK — an ergonomic, hand-written facade over the
generated (python, urllib3+pydantic) client in `imzala_client`.

Mirrors the `@imzala/node` TypeScript facade
(`packages/node/src/index.ts` in this monorepo) method-for-method, so
server code reads the same regardless of language.
"""

from __future__ import annotations

import json
import random
import time
from dataclasses import dataclass
from typing import Any, Callable, Iterator, Mapping, Optional, Sequence, Union

from imzala_client.api.account_api import AccountApi
from imzala_client.api.demands_api import DemandsApi
from imzala_client.api.reminders_api import RemindersApi
from imzala_client.api.templates_api import TemplatesApi
from imzala_client.api.timestamps_api import TimestampsApi
from imzala_client.api_client import ApiClient
from imzala_client.configuration import Configuration

from .errors import ImzalaError, ImzalaRateLimitError, map_api_exception
from .files import FileInput, UploadPartyInput, to_multipart_tuple

__all__ = [
    "Imzala",
    "DEFAULT_BASE_URL",
    "DEFAULT_TIMEOUT_S",
    "DEFAULT_MAX_RETRIES",
    "DEFAULT_RETRY_BASE_DELAY_S",
]

DEFAULT_BASE_URL = "https://api-prd.imzala.org"
DEFAULT_TIMEOUT_S = 30.0
DEFAULT_MAX_RETRIES = 2
DEFAULT_RETRY_BASE_DELAY_S = 0.3


def _unwrap(call: Callable[[], Any]) -> Any:
    """Every imzala.org API response uses the same envelope:
    `{success: true, data: {...}}` on success, or a non-2xx status with
    `{success: false, error/message: ...}` on failure.

    Calls the given generated-client thunk, unwraps `.data`, and
    normalizes any failure (HTTP error status, network error, or an
    unexpected `success=False` on an otherwise-2xx response) into a typed
    `ImzalaError` — see `.errors`. Every facade method routes through this.
    """
    try:
        response = call()
    except ImzalaError:
        raise
    except Exception as exc:  # ApiException, urllib3/network errors, pydantic validation errors, ...
        raise map_api_exception(exc) from exc

    success = getattr(response, "success", None)
    if response is None or success is False:
        raise ImzalaError("imzala.org API request failed", body=response)

    return response.data


@dataclass(frozen=True)
class _RetryConfig:
    """Max retry attempts (not counting the initial try) + base delay
    (seconds) for exponential backoff. `max_retries=0` disables retry."""

    max_retries: int
    base_delay_s: float


def _is_retryable_status(status_code: Optional[int]) -> bool:
    """429 (rate limited) and 5xx (server error) are treated as transient.
    Everything else (4xx) is a client error and is never retried."""
    if status_code == 429:
        return True
    return isinstance(status_code, int) and 500 <= status_code <= 599


def _compute_delay_s(error: ImzalaError, attempt: int, base_delay_s: float) -> float:
    """Exponential backoff with jitter, honoring `Retry-After` on 429s
    (already parsed onto `ImzalaRateLimitError.retry_after` by
    `map_api_exception`)."""
    if isinstance(error, ImzalaRateLimitError) and error.retry_after is not None:
        return max(0.0, float(error.retry_after))
    backoff = base_delay_s * (2**attempt)
    jitter = random.random() * base_delay_s
    return backoff + jitter


def _unwrap_retryable_get(call: Callable[[], Any], retry: _RetryConfig) -> Any:
    """Like `_unwrap`, but adds safe auto-retry for **GET-only, idempotent**
    facade methods (`templates.list/get/usage`, `demands.get`, `me()`).
    Retries on 429 (rate limited — honors `Retry-After`) and 5xx (server
    error) with exponential backoff + jitter; any other status (400, 401,
    404, 409, 422, ...) is raised immediately, same as `_unwrap`.

    **SAFETY — never call this with a non-GET request.** There is
    deliberately no `method` parameter and no way to opt a POST/PUT/PATCH/
    DELETE call into retrying — this is not a caller-configurable
    behavior. Retrying a write (e.g. `demands.create`,
    `demands.send_reminder`) could duplicate a demand or double-send a
    reminder — those facade methods must keep using the plain `_unwrap()`
    above, once, with no retry loop.

    `call` is a thunk (not an already-evaluated value) because retrying
    means re-issuing the underlying HTTP request — a settled result can't
    be replayed.
    """
    attempt = 0
    while True:
        try:
            return _unwrap(call)
        except ImzalaError as err:
            if attempt >= retry.max_retries or not _is_retryable_status(err.status_code):
                raise
            time.sleep(_compute_delay_s(err, attempt, retry.base_delay_s))
            attempt += 1


def _retryable_binary_get(call: Callable[[], Any], retry: _RetryConfig) -> bytes:
    """Like `_unwrap_retryable_get`, but for **binary GET** endpoints that
    return raw file bytes (a signed PDF / a completion certificate) rather
    than the JSON `{success, data}` envelope. There is nothing to unwrap —
    the generated client already deserializes a `200 application/pdf`
    response straight to `bytes` — so this only layers the same safe,
    GET-only auto-retry (429 / 5xx, exponential backoff + jitter) around
    the call and coerces the result to `bytes`.

    Mirrors the node facade's `demands.getPdf`/`getCertificate`, which read
    the raw body (`responseType: 'arraybuffer'` → `Buffer`) instead of
    routing through the envelope `unwrap`.

    **SAFETY — never call this with a non-GET request**, for the same
    reason as `_unwrap_retryable_get`: a retried write could duplicate an
    effect. There is deliberately no way to opt a write into this path.
    """
    attempt = 0
    while True:
        try:
            result = call()
        except ImzalaError as err:
            if attempt >= retry.max_retries or not _is_retryable_status(err.status_code):
                raise
            time.sleep(_compute_delay_s(err, attempt, retry.base_delay_s))
            attempt += 1
            continue
        except Exception as exc:  # ApiException, urllib3/network errors, ...
            err = map_api_exception(exc)
            if attempt >= retry.max_retries or not _is_retryable_status(err.status_code):
                raise err from exc
            time.sleep(_compute_delay_s(err, attempt, retry.base_delay_s))
            attempt += 1
            continue
        return bytes(result)


def _get_field(obj: Any, name: str, default: Any = None) -> Any:
    """Reads `name` off a response payload that may be a plain dict (as
    used throughout this SDK's test suite) or a generated-client pydantic
    model instance (this SDK's real runtime shape) — `list_all()` needs to
    read `total`/`page`/`limit` regardless of which one it got."""
    if isinstance(obj, Mapping):
        return obj.get(name, default)
    return getattr(obj, name, default)


def _party_to_dict(party: Union[UploadPartyInput, Mapping[str, Any]]) -> dict:
    if isinstance(party, UploadPartyInput):
        data: dict = {"first_name": party.first_name, "last_name": party.last_name}
        if party.email is not None:
            data["email"] = party.email
        if party.phone is not None:
            data["phone"] = party.phone
        return data
    return dict(party)


class TemplatesResource:
    """`imzala.templates.*` — list/inspect your active templates."""

    def __init__(self, api: TemplatesApi, timeout: float, retry: _RetryConfig) -> None:
        self._api = api
        self._timeout = timeout
        self._retry = retry

    def list(self, *, page: Optional[int] = None, limit: Optional[int] = None) -> Any:
        """Lists your active templates (one page). GET — safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._api.api_v1_templates_get(page=page, limit=limit, _request_timeout=self._timeout),
            self._retry,
        )

    def get(self, template_id: str) -> Any:
        """Returns a template's parties + fillable variables. GET — safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._api.api_v1_templates_id_get(id=template_id, _request_timeout=self._timeout),
            self._retry,
        )

    def usage(self, template_id: str) -> Any:
        """Returns a ready-to-use integration guide (endpoint, required
        headers, example curl+JSON) for a template. GET — safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._api.api_v1_templates_id_usage_get(id=template_id, _request_timeout=self._timeout),
            self._retry,
        )

    def list_all(self, *, page: Optional[int] = None, limit: Optional[int] = None) -> Iterator[Any]:
        """Walks every page of your active templates, transparently,
        yielding one template at a time. Internally calls
        `list(page=, limit=)` and increments `page` until a page comes
        back short (fewer items than the requested page size) or the
        response's `total` has been reached — whichever happens first —
        so it always terminates even against a misbehaving/empty result
        set.

        Example:
            >>> for template in client.templates.list_all():
            ...     print(template["id"], template["name"])
        """
        requested_limit = limit
        current_page = page if page is not None else 1
        yielded = 0

        while True:
            result = self.list(page=current_page, limit=requested_limit)
            templates = _get_field(result, "templates") or []

            for template in templates:
                yield template
            yielded += len(templates)

            if len(templates) == 0:
                break

            total = _get_field(result, "total")
            if isinstance(total, int) and yielded >= total:
                break

            effective_limit = _get_field(result, "limit", requested_limit)
            if isinstance(effective_limit, int) and len(templates) < effective_limit:
                break

            current_page = (_get_field(result, "page") or current_page) + 1

    def update(self, template_id: str, body: Mapping[str, Any]) -> Any:
        """Updates a template's metadata (`name` / `description` /
        `category`). The page/field/party structure can't be changed via
        the API — edit that in the dashboard. PATCH — never auto-retried."""
        return _unwrap(
            lambda: self._api.api_v1_templates_id_patch(
                id=template_id,
                api_v1_templates_id_patch_request=dict(body),
                _request_timeout=self._timeout,
            )
        )

    def delete(self, template_id: str) -> Any:
        """Deletes (soft-deletes) a template. Existing demands created from
        it are unaffected. DELETE — never auto-retried."""
        return _unwrap(
            lambda: self._api.api_v1_templates_id_delete(
                id=template_id, _request_timeout=self._timeout
            )
        )


class DemandsResource:
    """`imzala.demands.*` — create/inspect demands (contracts) and trigger reminders."""

    def __init__(self, api: DemandsApi, reminders_api: RemindersApi, timeout: float, retry: _RetryConfig) -> None:
        self._api = api
        self._reminders_api = reminders_api
        self._timeout = timeout
        self._retry = retry

    def create(self, body: Mapping[str, Any]) -> Any:
        """Creates a new demand (contract) from a template. POST — never
        auto-retried (a retried create would produce a duplicate demand)."""
        return _unwrap(
            lambda: self._api.api_v1_demands_post(
                create_demand_request=dict(body), _request_timeout=self._timeout
            )
        )

    def get(self, demand_id: str) -> Any:
        """Returns a demand's status + per-party signing progress. GET — safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._api.api_v1_demands_id_get(id=demand_id, _request_timeout=self._timeout),
            self._retry,
        )

    def add_items(self, demand_id: str, body: Mapping[str, Any]) -> Any:
        """Places (replaces) signature/form fields on a demand's pages. See
        `page_ids` in `body` for full-replace vs per-page-replace semantics."""
        return _unwrap(
            lambda: self._api.api_v1_demands_id_items_post(
                id=demand_id, upsert_items_request=dict(body), _request_timeout=self._timeout
            )
        )

    def upload_document(
        self,
        *,
        files: Sequence[FileInput],
        parties: Sequence[Union[UploadPartyInput, Mapping[str, Any]]],
        order: Optional[Sequence[int]] = None,
        title: Optional[str] = None,
        description: Optional[str] = None,
    ) -> Any:
        """Creates a demand directly from an uploaded document (no
        template) — a single PDF/DOC/DOCX/ODT/RTF/TXT, or 1-20 images
        merged into one PDF."""
        file_tuples = [to_multipart_tuple(f) for f in files]
        parties_json = json.dumps([_party_to_dict(p) for p in parties])
        order_json = json.dumps(list(order)) if order is not None else None
        return _unwrap(
            lambda: self._api.api_v1_demands_upload_post(
                files=file_tuples,
                parties=parties_json,
                order=order_json,
                title=title,
                description=description,
                _request_timeout=self._timeout,
            )
        )

    def send_reminder(self, demand_id: str, body: Optional[Mapping[str, Any]] = None) -> Any:
        """Triggers an immediate SMS/email reminder to a demand's unsigned
        parties. Independent of the template/demand's scheduled
        `reminder_settings`. Subject to a 5-minute anti-spam window
        (override with `{"force": True}`) and a hard per-person cap of 3
        reminders per channel (not overridable). POST — never
        auto-retried (a retried call could double-send)."""
        return _unwrap(
            lambda: self._reminders_api.api_v1_demands_id_reminders_post(
                id=demand_id,
                trigger_reminder_request=dict(body) if body else {},
                _request_timeout=self._timeout,
            )
        )

    def list(
        self,
        *,
        status: Optional[str] = None,
        q: Optional[str] = None,
        from_: Optional[str] = None,
        to: Optional[str] = None,
        template_id: Optional[str] = None,
        page: Optional[int] = None,
        limit: Optional[int] = None,
        sort: Optional[str] = None,
    ) -> Any:
        """Lists your demands — counts-only (id/title/status/timestamps +
        `parties_total`/`parties_signed`, NO party names/emails/phones).
        Filter by status/date/template, paginate with page/limit. GET —
        safe to auto-retry. For per-party detail use `get(demand_id)`.

        `from_` (trailing underscore) is the creation lower-bound — `from`
        is a Python keyword — and maps to the API's `from` query param;
        `to` is the upper bound. Both are ISO dates (`YYYY-MM-DD`).
        `template_id` filters to demands created from that template.
        `sort` is `field:direction`, e.g. `createdAt:desc`.
        """
        return _unwrap_retryable_get(
            lambda: self._api.api_v1_demands_get(
                status=status,
                q=q,
                var_from=from_,
                to=to,
                template_id=template_id,
                page=page,
                limit=limit,
                sort=sort,
                _request_timeout=self._timeout,
            ),
            self._retry,
        )

    def get_pdf(self, demand_id: str) -> bytes:
        """Downloads the signed contract PDF (only once
        `status == "COMPLETED"`). Returns the raw `bytes` — write them to
        disk or stream them on. Requires the API key's owner to own the
        demand. GET — safe to auto-retry."""
        return _retryable_binary_get(
            lambda: self._api.api_v1_demands_id_pdf_get(
                id=demand_id, _request_timeout=self._timeout
            ),
            self._retry,
        )

    def get_certificate(self, demand_id: str, *, lang: Optional[str] = None) -> bytes:
        """Downloads the completion certificate (PAdES B-T sealed audit
        document) as raw `bytes`. Only produced for `COMPLETED` demands.
        Pass `lang="en"` for English. GET — safe to auto-retry."""
        return _retryable_binary_get(
            lambda: self._api.api_v1_demands_id_certificate_get(
                id=demand_id, lang=lang, _request_timeout=self._timeout
            ),
            self._retry,
        )

    def get_timeline(self, demand_id: str) -> Any:
        """Returns the signing audit trail (view/sign/reject events).
        PII-masked: `ip_masked` (last octet hidden), actor name+email
        masked, no raw IP/device. GET — safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._api.api_v1_demands_id_timeline_get(
                id=demand_id, _request_timeout=self._timeout
            ),
            self._retry,
        )

    def cancel(self, demand_id: str, body: Optional[Mapping[str, Any]] = None) -> Any:
        """Cancels (voids) a pending demand — sets it to `CANCELLED` and
        stops any scheduled reminders. A `COMPLETED` (or already-cancelled)
        demand can't be cancelled (raises). Pass `{"reason": "..."}` to
        record why. POST — never auto-retried."""
        return _unwrap(
            lambda: self._api.api_v1_demands_id_cancel_post(
                id=demand_id,
                api_v1_demands_id_cancel_post_request=dict(body) if body else {},
                _request_timeout=self._timeout,
            )
        )

    def resend_party(self, demand_id: str, party_id: str) -> Any:
        """Re-sends the signing invitation to a single party (by `party_id`
        from the demand's create/get response). Can't resend to a party who
        has already signed or declined, or one whose turn hasn't come in
        ordered signing (raises). POST — never auto-retried."""
        return _unwrap(
            lambda: self._api.api_v1_demands_id_parties_party_id_resend_post(
                id=demand_id, party_id=party_id, _request_timeout=self._timeout
            )
        )

    def delete(self, demand_id: str) -> Any:
        """Deletes a demand and all its data. Only NON-completed demands can
        be deleted via the API — a `COMPLETED` demand (signed document +
        audit trail) returns 409 and must be removed from the dashboard.
        DELETE — never auto-retried."""
        return _unwrap(
            lambda: self._api.api_v1_demands_id_delete(
                id=demand_id, _request_timeout=self._timeout
            )
        )


class EmbedResource:
    """`imzala.embed.*` — mint embedded signing sessions for an `<iframe>`."""

    def __init__(self, api: DemandsApi, timeout: float) -> None:
        self._api = api
        self._timeout = timeout

    def create_session(self, demand_id: str, *, party_id: str) -> Any:
        """Mints a short-lived, single-use embed signing token for a
        demand's party. The returned `embed_url` is meant for an
        `<iframe>`.

        Signatures obtained this way are SES by default (AES if TC/
        biometric verification ran) — this flow never produces QES.
        """
        return _unwrap(
            lambda: self._api.api_v1_demands_id_embed_session_post(
                id=demand_id,
                api_v1_demands_id_embed_session_post_request={"party_id": party_id},
                _request_timeout=self._timeout,
            )
        )


class TimestampsResource:
    """`imzala.timestamps.*` — RFC 3161 timestamps."""

    def __init__(self, api: TimestampsApi, timeout: float) -> None:
        self._api = api
        self._timeout = timeout

    def create(
        self,
        *,
        content: bytes,
        filename: str,
        content_type: Optional[str] = None,
        idempotency_key: Optional[str] = None,
        description: Optional[str] = None,
        owner_first_name: Optional[str] = None,
        owner_last_name: Optional[str] = None,
    ) -> Any:
        """RFC 3161-timestamps a file via TÜBİTAK KAMU SM TSA (existence +
        integrity proof — not a signature; see the returned record for
        details). Pass `idempotency_key` to make retries safe (5-minute
        window, no duplicate credit spend). `content_type` is currently
        informational only — see `FileInput`."""
        del content_type
        return _unwrap(
            lambda: self._api.api_v1_timestamps_post(
                file=(filename, bytes(content)),
                idempotency_key=idempotency_key,
                description=description,
                owner_first_name=owner_first_name,
                owner_last_name=owner_last_name,
                _request_timeout=self._timeout,
            )
        )


class Imzala:
    """imzala.org server-side SDK — an ergonomic, hand-written facade over
    the generated (urllib3/pydantic) client in `imzala_client`. Every
    method unwraps the `{success, data}` response envelope and raises a
    typed `ImzalaError` (see `.errors`) on failure, instead of returning
    raw generated-client response objects.

    Example:
        >>> from imzala import Imzala
        >>> client = Imzala(api_key=os.environ["IMZALA_API_KEY"])
        >>> demand = client.demands.create({"template_id": tid, "party_mapping": mapping})
    """

    def __init__(
        self,
        api_key: str,
        base_url: str = DEFAULT_BASE_URL,
        timeout: float = DEFAULT_TIMEOUT_S,
        max_retries: int = DEFAULT_MAX_RETRIES,
        retry_base_delay: float = DEFAULT_RETRY_BASE_DELAY_S,
    ) -> None:
        """
        Args:
            api_key: `imz_<64 hex>` — from Dashboard -> Geliştirici -> API
                Anahtarları, or Hesap Ayarları -> API Anahtarları.
            base_url: defaults to `https://api-prd.imzala.org`. Use
                `https://test-api.imzala.org` for the test environment.
            timeout: per-request timeout, in seconds. Defaults to 30.0.
            max_retries: max auto-retry attempts for safe, idempotent
                **GET** requests that fail with 429 (rate limited) or 5xx
                (server error). Defaults to 2. Set to `0` to disable.
                Writes (`demands.create`, `send_reminder`, ...) are never
                retried, regardless of this setting — see the SDK README.
            retry_base_delay: base delay (seconds) for the exponential
                backoff between retries. Defaults to 0.3 (300ms).
        """
        if not api_key:
            raise ValueError("Imzala(api_key=...) — api_key is required.")

        self._timeout = float(timeout)
        self._retry = _RetryConfig(
            max_retries=max(0, int(max_retries)),
            base_delay_s=max(0.0, float(retry_base_delay)),
        )

        configuration = Configuration(host=base_url, api_key={"ApiKeyAuth": api_key})
        api_client = ApiClient(configuration)

        self._account_api = AccountApi(api_client)
        demands_api = DemandsApi(api_client)
        reminders_api = RemindersApi(api_client)
        templates_api = TemplatesApi(api_client)
        timestamps_api = TimestampsApi(api_client)

        self.templates = TemplatesResource(templates_api, self._timeout, self._retry)
        self.demands = DemandsResource(demands_api, reminders_api, self._timeout, self._retry)
        self.embed = EmbedResource(demands_api, self._timeout)
        self.timestamps = TimestampsResource(timestamps_api, self._timeout)

    def me(self) -> Any:
        """Returns the calling API key's owner info (id, email, name,
        workspace, remaining credits). Requires the `timestamps` scope.
        GET — safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._account_api.api_v1_me_get(_request_timeout=self._timeout),
            self._retry,
        )
