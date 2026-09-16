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
from typing import Any, Callable, Iterator, Literal, Mapping, Optional, Sequence, Union

from imzala_client.api.account_api import AccountApi
from imzala_client.api.contacts_api import ContactsApi
from imzala_client.api.demands_api import DemandsApi
from imzala_client.api.reminders_api import RemindersApi
from imzala_client.api.reports_api import ReportsApi
from imzala_client.api.templates_api import TemplatesApi
from imzala_client.api.timestamps_api import TimestampsApi
from imzala_client.api_client import ApiClient
from imzala_client.models.api_v1_demands_demand_id_dispatch_post_request_send_invitations import (
    ApiV1DemandsDemandIdDispatchPostRequestSendInvitations,
)
from imzala_client.models.api_v1_demands_demand_id_documents_post201_response_data import (
    ApiV1DemandsDemandIdDocumentsPost201ResponseData,
)
from imzala_client.configuration import Configuration

from .errors import (
    ImzalaError,
    ImzalaRateLimitError,
    extract_error_code,
    extract_error_message,
    map_api_exception,
    ImzalaValidationError,
)
from .files import FileInput, UploadPartyInput, to_multipart_tuple

__all__ = [
    "Imzala",
    "DEFAULT_BASE_URL",
    "DEFAULT_TIMEOUT_S",
    "DEFAULT_MAX_RETRIES",
    "DEFAULT_RETRY_BASE_DELAY_S",
    "MAX_IDEMPOTENT_RETRY_WAIT_S",
]

DEFAULT_BASE_URL = "https://api-prd.imzala.org"
DEFAULT_TIMEOUT_S = 30.0
DEFAULT_MAX_RETRIES = 2
DEFAULT_RETRY_BASE_DELAY_S = 0.3
#: Longest `Retry-After` an idempotent write waits out before its one retry.
MAX_IDEMPOTENT_RETRY_WAIT_S = 60.0

SendInvitations = Literal["true", "all", "email", "sms", "false"]
OnAnchorMiss = Literal["block", "drop"]
EnvelopeDocumentKind = Literal["CONTRACT", "KVKK_NOTICE", "KVKK_CONSENT", "PREINFO", "PRICE_LIST", "OTHER"]
DispatchSendInvitations = Union[
    bool, Literal["true", "1", "all", "email", "sms", "false", "0", "off", "no", "hayir", "hayır"]
]


def _body_mapping(response: Any) -> Any:
    """A `{success: false}` body arrives as a generated model (or, in tests,
    a plain object); read its fields as a mapping for the error helpers."""
    if response is None or isinstance(response, Mapping):
        return response
    for dump in ("to_dict", "model_dump"):
        fn = getattr(response, dump, None)
        if callable(fn):
            try:
                dumped = fn()
            except Exception:  # pragma: no cover - defensive
                continue
            if isinstance(dumped, Mapping):
                return dumped
    try:
        return vars(response)
    except TypeError:
        return None


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
        mapping = _body_mapping(response)
        raise ImzalaError(
            extract_error_message(mapping) or "imzala.org API request failed",
            body=response,
            code=extract_error_code(mapping),
        )

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


def _retry_delay_s(error: ImzalaError, attempt: int, base_delay_s: float) -> Optional[float]:
    """Delay before the next GET retry, or None when the server asks for a
    longer wait than `MAX_IDEMPOTENT_RETRY_WAIT_S`: the error is then raised
    instead of blocking the caller for a long, silent sleep."""
    delay_s = _compute_delay_s(error, attempt, base_delay_s)
    return None if delay_s > MAX_IDEMPOTENT_RETRY_WAIT_S else delay_s


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
            delay_s = _retry_delay_s(err, attempt, retry.base_delay_s)
            if delay_s is None:
                raise
            time.sleep(delay_s)
            attempt += 1


def _assert_header_value(value: Optional[str], header_name: str) -> None:
    """Rejects a header value that is not printable ASCII before anything is
    sent, as the same `ImzalaValidationError` in every SDK language (the HTTP
    stack would otherwise fail with an unrelated error, or in other clients
    send an altered key)."""
    if value is None:
        return
    if any(ord(ch) < 0x20 or ord(ch) > 0x7E for ch in value):
        raise ImzalaValidationError(
            f"{header_name} may only contain printable ASCII characters (no line breaks, no non-ASCII letters)."
        )


def _assert_idempotency_key(value: Any, name: str) -> None:
    """A required idempotency key sent as a body field: a non-empty printable
    ASCII string. Checked before the request is built."""
    if not isinstance(value, str) or value == "":
        raise ImzalaValidationError(f"{name} is required and must be a non-empty string.")
    _assert_header_value(value, name)


def _replayed_document(err: ImzalaError) -> Optional[Any]:
    """The earlier document from a 409 `IDEMPOTENT_REPLAY` upload answer, as
    the same generated model a successful upload returns; None otherwise."""
    if err.status_code != 409 or err.code != "IDEMPOTENT_REPLAY":
        return None
    body = err.body if isinstance(err.body, Mapping) else None
    data = body.get("data") if body else None
    if not isinstance(data, Mapping) or not data.get("document"):
        return None
    return ApiV1DemandsDemandIdDocumentsPost201ResponseData.from_dict(dict(data))


def _unwrap_idempotent_write(
    call: Callable[[], Any],
    *,
    idempotency_key: Optional[str],
    retry_base_delay_s: float,
    max_wait_s: float = MAX_IDEMPOTENT_RETRY_WAIT_S,
    sleep: Optional[Callable[[float], None]] = None,
) -> Any:
    """Bounded, safe retry for write calls.

    Writes are normally never retried: a repeated create produces a second
    demand. The one exception is a write sent with an `Idempotency-Key`. The
    server does not treat a second request with the same key as a new
    record, so after a 429 it is safe to wait for `Retry-After` and try
    exactly once more. A second 429, and any other error including 5xx, is
    raised.

    If the server asks for a longer wait than `max_wait_s` (default 60 s),
    the 429 is raised instead of blocking the caller.

    Only use this for endpoints whose Idempotency-Key the server honours
    (demand create, document upload, timestamp create). On any other
    endpoint the "safe" retry could create a duplicate.

    Without a key this behaves exactly like `_unwrap`. `sleep` defaults to
    `time.sleep` and can be injected by tests.
    """
    _assert_header_value(idempotency_key, "Idempotency-Key")
    try:
        return _unwrap(call)
    except ImzalaError as err:
        if not idempotency_key or err.status_code != 429:
            raise
        if isinstance(err, ImzalaRateLimitError) and err.retry_after is not None:
            wait_s = max(0.0, float(err.retry_after))
        else:
            wait_s = retry_base_delay_s
        if wait_s > max_wait_s:
            raise
        (sleep or time.sleep)(wait_s)
    return _unwrap(call)


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
            delay_s = _retry_delay_s(err, attempt, retry.base_delay_s)
            if delay_s is None:
                raise
            time.sleep(delay_s)
            attempt += 1
            continue
        except Exception as exc:  # ApiException, urllib3/network errors, ...
            err = map_api_exception(exc)
            if attempt >= retry.max_retries or not _is_retryable_status(err.status_code):
                raise err from exc
            delay_s = _retry_delay_s(err, attempt, retry.base_delay_s)
            if delay_s is None:
                raise err from exc
            time.sleep(delay_s)
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
        if party.template_party_id is not None:
            data["template_party_id"] = party.template_party_id
        return data
    return dict(party)


class TemplatesResource:
    """`imzala.templates.*` — list/inspect your active templates."""

    def __init__(self, api: TemplatesApi, timeout: float, retry: _RetryConfig) -> None:
        self._api = api
        self._timeout = timeout
        self._retry = retry

    def list(self, *, page: Optional[int] = None, limit: Optional[int] = None) -> Any:
        """Lists your active templates (one page). `limit` is clamped to
        1..100; `page` below 1 raises `INVALID_PAGE`. GET, safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._api.api_v1_templates_get(page=page, limit=limit, _request_timeout=self._timeout),
            self._retry,
        )

    def get(self, template_id: str) -> Any:
        """Returns a template's parties + fillable variables, plus its
        envelope documents under `documents`: `id`, `order`, `title`,
        `doc_kind`, `is_required`, `signature_required`, `default_included`
        and `assigned_template_party_ids`. Those ids are what
        `demands.create`'s `documents.include` / `documents.exclude` expect.
        A copied template has NEW document ids, so read them back from here
        for the copy. GET — safe to auto-retry."""
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
        """Deletes a template. The record is not erased immediately: it is
        marked deleted and kept for 30 days. Existing demands created from it
        are unaffected. A template with active (draft or pending) demands
        cannot be deleted and raises `TEMPLATE_IN_USE`. DELETE, never
        auto-retried."""
        return _unwrap(
            lambda: self._api.api_v1_templates_id_delete(
                id=template_id, _request_timeout=self._timeout
            )
        )


class EnvelopeDocumentsResource:
    """`imzala.demands.documents.*`: documents of a multi-document envelope.

    While multi-document envelopes are not enabled for the account, every
    method here raises `ENVELOPE_MULTI_DOC_DISABLED` (409), `list` included;
    it never returns an empty list in that case. These methods spend no
    credit: credit is charged on `demands.dispatch`.
    """

    def __init__(self, api: DemandsApi, timeout: float, retry: _RetryConfig) -> None:
        self._api = api
        self._timeout = timeout
        self._retry = retry

    def list(self, demand_id: str, *, view: Optional[Literal["wizard"]] = None) -> Any:
        """Lists the envelope's documents. `view="wizard"` returns the full
        shape (`assigned_party_ids`, `decision_count`). GET, safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._api.api_v1_demands_demand_id_documents_get(
                demand_id=demand_id, view=view, _request_timeout=self._timeout
            ),
            self._retry,
        )

    def create(self, demand_id: str, body: Mapping[str, Any]) -> Any:
        """Adds a document without a file (metadata only: `title` required,
        `doc_kind`, `is_required`, `signature_required`). No idempotency key,
        so never retried. POST."""
        return _unwrap(
            lambda: self._api.api_v1_demands_demand_id_documents_post(
                demand_id=demand_id,
                api_v1_demands_demand_id_documents_post_request=dict(body),
                _request_timeout=self._timeout,
            )
        )

    def upload(
        self,
        demand_id: str,
        *,
        file: FileInput,
        title: str,
        idempotency_key: str,
        doc_kind: Optional[EnvelopeDocumentKind] = None,
        is_required: Optional[bool] = None,
    ) -> Any:
        """Uploads one file as one document.

        `idempotency_key` is required and checked before anything is sent
        (missing, empty, or not printable ASCII raises
        `ImzalaValidationError`); it is sent as the `idempotency_key` form
        field, not as a header. Because the server keeps the key, one retry is
        made after a 429 (waiting at most 60 seconds). If a document was
        already uploaded with the same key, the server answers 409
        `IDEMPOTENT_REPLAY` with that document; it is returned as a normal
        result. Any other 409 is raised.
        """
        _assert_idempotency_key(idempotency_key, "idempotency_key")
        required = None if is_required is None else ("true" if is_required else "false")
        try:
            return _unwrap_idempotent_write(
                lambda: self._api.api_v1_demands_demand_id_documents_upload_post(
                    demand_id=demand_id,
                    file=to_multipart_tuple(file),
                    idempotency_key=idempotency_key,
                    title=title,
                    doc_kind=doc_kind,
                    is_required=required,
                    _request_timeout=self._timeout,
                ),
                idempotency_key=idempotency_key,
                retry_base_delay_s=self._retry.base_delay_s,
            )
        except ImzalaError as err:
            replayed = _replayed_document(err)
            if replayed is None:
                raise
            return replayed

    def update(self, demand_id: str, doc_id: str, body: Mapping[str, Any]) -> Any:
        """Updates only the fields you send (`title`, `doc_kind`,
        `is_required`, `signature_required`). Never retried. PATCH."""
        return _unwrap(
            lambda: self._api.api_v1_demands_demand_id_documents_doc_id_patch(
                demand_id=demand_id,
                doc_id=doc_id,
                api_v1_demands_demand_id_documents_doc_id_patch_request=dict(body),
                _request_timeout=self._timeout,
            )
        )

    def delete(self, demand_id: str, doc_id: str) -> Any:
        """Deletes a document. The last document of an envelope cannot be
        deleted (`CANNOT_DELETE_LAST_DOCUMENT`). Never retried. DELETE."""
        return _unwrap(
            lambda: self._api.api_v1_demands_demand_id_documents_doc_id_delete(
                demand_id=demand_id, doc_id=doc_id, _request_timeout=self._timeout
            )
        )

    def reorder(self, demand_id: str, document_ids: Sequence[str]) -> Any:
        """Sets the order of all documents. `document_ids` must contain exactly
        the envelope's documents (`ORDER_SET_MISMATCH`). Never retried. PUT."""
        return _unwrap(
            lambda: self._api.api_v1_demands_demand_id_documents_order_put(
                demand_id=demand_id,
                api_v1_demands_demand_id_documents_order_put_request={"document_ids": list(document_ids)},
                _request_timeout=self._timeout,
            )
        )

    def set_assignments(self, demand_id: str, doc_id: str, party_ids: Sequence[str]) -> Any:
        """Replaces the set of parties assigned to a document. Never retried. PUT."""
        return _unwrap(
            lambda: self._api.api_v1_demands_demand_id_documents_doc_id_assignments_put(
                demand_id=demand_id,
                doc_id=doc_id,
                api_v1_demands_demand_id_documents_doc_id_assignments_put_request={"party_ids": list(party_ids)},
                _request_timeout=self._timeout,
            )
        )


class DemandsResource:
    """`imzala.demands.*` — create/inspect demands (contracts) and trigger reminders."""

    def __init__(self, api: DemandsApi, reminders_api: RemindersApi, timeout: float, retry: _RetryConfig) -> None:
        self._api = api
        self._reminders_api = reminders_api
        self._timeout = timeout
        self._retry = retry
        self.documents = EnvelopeDocumentsResource(api, timeout, retry)

    def dispatch(self, demand_id: str, *, send_invitations: Optional[DispatchSendInvitations] = None) -> Any:
        """Sends the demand for signing: reconciles credit, moves a `DRAFT`
        to `PENDING` and sends invitations. This is where credit is charged;
        the `documents` methods charge nothing. Calling it again for a demand
        that is already out charges nothing more (`dispatched` is false).

        `send_invitations` narrows the channels; omitted means the server
        default (invitations on). `False`, `"false"`, `"0"`, `"off"`, `"no"`,
        `"hayir"`, `"hayır"` send none; `"email"` / `"sms"` limit the channel.
        An unknown value raises `INVALID_SEND_INVITATIONS`.

        Raises for `DISPATCH_NO_PARTIES`, `DISPATCH_TOO_MANY`,
        `QES_NOT_SUPPORTED_MULTI_DOCUMENT`, `INSUFFICIENT_CREDITS` and
        others. No idempotency key, so never retried, not even after a 429.
        POST.
        """
        body: dict = {}
        if send_invitations is not None:
            # The generated model is a oneOf wrapper; a bare bool/str is rejected.
            body["send_invitations"] = ApiV1DemandsDemandIdDispatchPostRequestSendInvitations(
                actual_instance=send_invitations
            )
        return _unwrap(
            lambda: self._api.api_v1_demands_demand_id_dispatch_post(
                demand_id=demand_id,
                api_v1_demands_demand_id_dispatch_post_request=body,
                _request_timeout=self._timeout,
            )
        )

    def create(self, body: Mapping[str, Any], *, idempotency_key: Optional[str] = None) -> Any:
        """Creates a new demand (contract) from a template.

        Without `idempotency_key` this is a single attempt: a retried create
        would produce a duplicate demand. With a key, a repeated request does
        not create a second demand, so one retry is made after a 429. A key
        reused with a different body raises `IDEMPOTENCY_KEY_REUSED`.

        An `expiry_date` that is not a real calendar day raises
        `INVALID_EXPIRY_DATE`.

        The optional `documents` key picks which of the template's documents
        this request sends: `{"include": [...]}` adds a document the template
        leaves out by default, `{"exclude": [...]}` drops one it includes (at
        most 20 ids per list). The ids come from
        `templates.get(template_id)["documents"][i]["id"]`. Leave the key out
        and the template's own defaults are sent, exactly as before. An
        excluded document is not part of this signing process at all: it is
        not shown to the signer, not in the signed PDF or the completion
        certificate, and not charged for. A bad selection raises
        `INVALID_DOCUMENT_SELECTION` (see `details.reason`: `shape`,
        `unknown_document`, `conflict`, `empty`); a selection that leaves a
        mapped party with nothing to sign raises `PARTY_WITHOUT_DOCUMENTS`.
        """
        return _unwrap_idempotent_write(
            lambda: self._api.api_v1_demands_post(
                create_demand_request=dict(body),
                idempotency_key=idempotency_key,
                _request_timeout=self._timeout,
            ),
            idempotency_key=idempotency_key,
            retry_base_delay_s=self._retry.base_delay_s,
        )

    def create_bulk(self, body: Mapping[str, Any]) -> Any:
        """Creates up to 10 demands from one template in a single request.
        Rows are created independently; check `failed` and each result's
        `status`.

        This endpoint has no idempotency key, so it is never retried: a
        retried batch would create the demands again. Split larger lists into
        batches of 10 yourself.

        Document selection is per row: `rows[i]["documents"]` takes the same
        shape as `create`. It is not a batch-wide option, so
        `options.documents` raises `INVALID_DOCUMENT_SELECTION`. A row with a
        bad selection comes back `failed`; the other rows are unaffected."""
        return _unwrap(
            lambda: self._api.api_v1_demands_bulk_post(
                api_v1_demands_bulk_post_request=dict(body), _request_timeout=self._timeout
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
        `page_ids` in `body` for full-replace vs per-page-replace semantics.
        Every item needs an integer `page_id` (`PAGE_ID_REQUIRED`); an
        unknown `item_type` raises `INVALID_ITEM_TYPE`."""
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
        idempotency_key: Optional[str] = None,
        field_template_id: Optional[str] = None,
        on_anchor_miss: Optional[OnAnchorMiss] = None,
        send_invitations: Optional[SendInvitations] = None,
        force: bool = False,
    ) -> Any:
        """Creates a demand directly from an uploaded document (no
        template) — a single PDF/DOC/DOCX/ODT/RTF/TXT, or 1-20 images
        merged into one PDF.

        Args:
            idempotency_key: makes the upload safe to retry: a second request
                with the same key does not create a second demand. With a
                key, one retry is made after a 429.
            field_template_id: field template (`kind: FIELD_LAYOUT`) whose
                layout is applied to the upload. The upload must then be a
                single PDF. The layout is resolved before the demand is
                created or credit is spent; a 422 creates nothing. Dry-run
                first with `field_templates.preview_layout`.
            on_anchor_miss: only with `field_template_id`, `'block'` or
                `'drop'`. Can tighten the template, never relax it: omitted
                means `block`; `drop` applies only if every affected field is
                already set to drop in the template (otherwise `block` is used
                and the response carries an `ON_ANCHOR_MISS_NOT_RELAXED`
                warning). Signature fields are never dropped.
            send_invitations: sends signing invitations in the same request.
                **Off by default on this endpoint.** `'true'` or `'all'` uses
                every channel, `'email'` limits it to e-mail, `'sms'` to phone
                channels (SMS and WhatsApp), `'false'` sends nothing. It can
                only narrow: a channel switched off in the demand's or party's
                notification settings is not turned back on.
            force: `True` deliberately bypasses the duplicate check
                (`DUPLICATE_SUSPECTED`). Only meaningful for calls without
                `idempotency_key`.
        """
        file_tuples = [to_multipart_tuple(f) for f in files]
        parties_json = json.dumps([_party_to_dict(p) for p in parties], ensure_ascii=False)
        order_json = json.dumps(list(order)) if order is not None else None
        return _unwrap_idempotent_write(
            lambda: self._api.api_v1_demands_upload_post(
                files=file_tuples,
                parties=parties_json,
                order=order_json,
                title=title,
                description=description,
                idempotency_key=idempotency_key,
                field_template_id=field_template_id,
                on_anchor_miss=on_anchor_miss,
                send_invitations=send_invitations,
                force="true" if force else None,
                _request_timeout=self._timeout,
            ),
            idempotency_key=idempotency_key,
            retry_base_delay_s=self._retry.base_delay_s,
        )

    def send_reminder(self, demand_id: str, body: Optional[Mapping[str, Any]] = None) -> Any:
        """Triggers an immediate SMS/email reminder to a demand's unsigned
        parties. Independent of the template/demand's scheduled
        `reminder_settings`. Subject to a 5-minute anti-spam window (429
        `RATE_LIMITED`, override with `{"force": True}`) and a hard
        per-person cap of 3 reminders per channel (not overridable). A draft,
        completed, cancelled or expired demand raises 409
        (`DEMAND_NOT_DISPATCHED`, `DEMAND_NOT_DISPATCHABLE`,
        `DEMAND_EXPIRED`). POST, never auto-retried (a retried call could
        double-send)."""
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

    def get_document_pdf(self, demand_id: str, document_id: str) -> bytes:
        """Downloads the PDF of one document in a multi-document envelope as
        raw `bytes`. For the whole contract use `get_pdf(demand_id)`. GET,
        safe to auto-retry."""
        return _retryable_binary_get(
            lambda: self._api.api_v1_demands_id_belge_document_id_pdf_get(
                id=demand_id, document_id=document_id, _request_timeout=self._timeout
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

    def __init__(self, api: TimestampsApi, timeout: float, retry: _RetryConfig) -> None:
        self._api = api
        self._timeout = timeout
        self._retry = retry

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
        window, no duplicate credit spend); with a key, one retry is made
        after a 429. `content_type` is currently informational only, see
        `FileInput`."""
        del content_type
        return _unwrap_idempotent_write(
            # The endpoint also accepts a JSON (base64) body and the generated
            # client prefers JSON when both are declared, which drops the
            # multipart fields and sends an empty body. Force multipart.
            lambda: self._api.api_v1_timestamps_post(
                _content_type="multipart/form-data",
                file=(filename, bytes(content)),
                idempotency_key=idempotency_key,
                description=description,
                owner_first_name=owner_first_name,
                owner_last_name=owner_last_name,
                _request_timeout=self._timeout,
            ),
            idempotency_key=idempotency_key,
            retry_base_delay_s=self._retry.base_delay_s,
        )

    def list(
        self,
        *,
        q: Optional[str] = None,
        status: Optional[str] = None,
        from_: Optional[str] = None,
        to: Optional[str] = None,
        page: Optional[int] = None,
        limit: Optional[int] = None,
        sort: Optional[str] = None,
    ) -> Any:
        """Lists your timestamp records (one page). `from_` and `to` are ISO
        dates (`YYYY-MM-DD`); `from_` maps to the API's `from` query param.
        GET, safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._api.api_v1_timestamps_get(
                page=page,
                limit=limit,
                q=q,
                status=status,
                var_from=from_,
                to=to,
                sort=sort,
                _request_timeout=self._timeout,
            ),
            self._retry,
        )

    def get(self, timestamp_id: str) -> Any:
        """Returns one timestamp record. GET, safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._api.api_v1_timestamps_id_get(id=timestamp_id, _request_timeout=self._timeout),
            self._retry,
        )


class FieldTemplatesResource:
    """`imzala.field_templates.*`: field layouts applied to uploaded PDFs."""

    def __init__(self, templates_api: TemplatesApi, demands_api: DemandsApi, timeout: float, retry: _RetryConfig) -> None:
        self._templates_api = templates_api
        self._demands_api = demands_api
        self._timeout = timeout
        self._retry = retry

    def list(self, *, page: Optional[int] = None, limit: Optional[int] = None) -> Any:
        """Lists your field templates. A field template is separate from a
        contract template: it describes where fields land on an uploaded PDF,
        located by anchor text. `limit` is clamped to 1..100. GET, safe to
        auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._templates_api.api_v1_field_templates_get(
                page=page, limit=limit, _request_timeout=self._timeout
            ),
            self._retry,
        )

    def get(self, field_template_id: str) -> Any:
        """Returns a field template's roles and field counts. A contract
        template id raises `TEMPLATE_NOT_FOUND`: the two are different kinds.
        GET, safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._templates_api.api_v1_field_templates_id_get(
                id=field_template_id, _request_timeout=self._timeout
            ),
            self._retry,
        )

    def preview_layout(
        self,
        field_template_id: str,
        *,
        files: Sequence[FileInput],
        on_anchor_miss: Optional[OnAnchorMiss] = None,
    ) -> Any:
        """Dry run: tries the field template's layout on exactly one PDF without
        creating anything or spending credit, and reports resolved fields and
        unresolved anchors separately. The cheapest way to avoid surprises
        before `demands.upload_document(field_template_id=...)`.
        `on_anchor_miss` is `'block'` or `'drop'` (see `upload_document`).
        Rate limited per user. POST, but side-effect free; never
        auto-retried."""
        file_tuples = [to_multipart_tuple(f) for f in files]
        return _unwrap(
            lambda: self._demands_api.api_v1_field_templates_id_preview_layout_post(
                id=field_template_id,
                files=file_tuples,
                on_anchor_miss=on_anchor_miss,
                _request_timeout=self._timeout,
            )
        )


class ContactsResource:
    """`imzala.contacts.*`: the contacts in your workspace."""

    def __init__(self, api: ContactsApi, timeout: float, retry: _RetryConfig) -> None:
        self._api = api
        self._timeout = timeout
        self._retry = retry

    def list(
        self,
        *,
        q: Optional[str] = None,
        page: Optional[int] = None,
        limit: Optional[int] = None,
        sort: Optional[str] = None,
        company_id: Optional[str] = None,
        archived: Optional[bool] = None,
    ) -> Any:
        """Lists contacts in your workspace (one page). `limit` is 10 to 100,
        default 25. GET, safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._api.api_v1_contacts_get(
                page=page,
                limit=limit,
                q=q,
                sort=sort,
                company_id=company_id,
                archived=archived,
                _request_timeout=self._timeout,
            ),
            self._retry,
        )

    def list_all(
        self,
        *,
        q: Optional[str] = None,
        page: Optional[int] = None,
        limit: Optional[int] = None,
        sort: Optional[str] = None,
        company_id: Optional[str] = None,
        archived: Optional[bool] = None,
    ) -> Iterator[Any]:
        """Walks every page of contacts, yielding one contact at a time. Stops
        on an empty page, a short page or once `total` is reached."""
        current_page = page if page is not None else 1
        yielded = 0

        while True:
            result = self.list(
                q=q, page=current_page, limit=limit, sort=sort, company_id=company_id, archived=archived
            )
            contacts = _get_field(result, "contacts") or []

            for contact in contacts:
                yield contact
            yielded += len(contacts)

            if len(contacts) == 0:
                break

            total = _get_field(result, "total")
            if isinstance(total, int) and yielded >= total:
                break

            effective_limit = _get_field(result, "limit", limit)
            if isinstance(effective_limit, int) and len(contacts) < effective_limit:
                break

            current_page = (_get_field(result, "page") or current_page) + 1

    def create(self, body: Mapping[str, Any]) -> Any:
        """Adds a contact. An active contact with the same e-mail or phone
        raises `CONTACT_DUPLICATE`. This endpoint has no idempotency key, so
        it is never retried. POST."""
        return _unwrap(
            lambda: self._api.api_v1_contacts_post(
                api_v1_contacts_post_request=dict(body), _request_timeout=self._timeout
            )
        )


class ReportsResource:
    """`imzala.reports.*`: aggregate counts."""

    def __init__(self, api: ReportsApi, timeout: float, retry: _RetryConfig) -> None:
        self._api = api
        self._timeout = timeout
        self._retry = retry

    def get(self) -> Any:
        """Returns aggregate demand counts for your workspace (pending,
        completed, cancelled, expired, created this month). Counts only, no
        personal data. GET, safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._api.api_v1_reports_get(_request_timeout=self._timeout),
            self._retry,
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
                retried by this setting. A write sent with an idempotency key
                is retried once after a 429, independently of it; see the SDK
                README.
            retry_base_delay: base delay (seconds) for the exponential
                backoff between retries. Defaults to 0.3 (300ms).
        """
        if not api_key:
            raise ValueError("Imzala(api_key=...) — api_key is required.")
        # A key read from a file often ends with a newline; sent as a header
        # it would corrupt the request.
        if any(ord(ch) < 0x20 or ord(ch) > 0x7E for ch in api_key):
            raise ValueError("Imzala(api_key=...): api_key may only contain printable ASCII characters (check for a trailing newline).")

        self._timeout = float(timeout)
        self._retry = _RetryConfig(
            max_retries=max(0, int(max_retries)),
            base_delay_s=max(0.0, float(retry_base_delay)),
        )

        configuration = Configuration(
            host=base_url,
            api_key={"ApiKeyAuth": api_key},
            # urllib3's default Retry honours Retry-After on 429/503 and silently
            # repeats the request, writes included, before this SDK ever sees the
            # error. Retrying is this SDK's decision (GETs, and keyed writes only).
            retries=0,
        )
        api_client = ApiClient(configuration)

        self._account_api = AccountApi(api_client)
        demands_api = DemandsApi(api_client)
        reminders_api = RemindersApi(api_client)
        templates_api = TemplatesApi(api_client)
        timestamps_api = TimestampsApi(api_client)
        contacts_api = ContactsApi(api_client)
        reports_api = ReportsApi(api_client)

        self.templates = TemplatesResource(templates_api, self._timeout, self._retry)
        self.demands = DemandsResource(demands_api, reminders_api, self._timeout, self._retry)
        self.embed = EmbedResource(demands_api, self._timeout)
        self.timestamps = TimestampsResource(timestamps_api, self._timeout, self._retry)
        self.field_templates = FieldTemplatesResource(templates_api, demands_api, self._timeout, self._retry)
        self.contacts = ContactsResource(contacts_api, self._timeout, self._retry)
        self.reports = ReportsResource(reports_api, self._timeout, self._retry)

    def me(self) -> Any:
        """Returns the calling API key's owner info (id, email, name,
        workspace, remaining credits). Works with any valid key; no scope is
        required. GET, safe to auto-retry."""
        return _unwrap_retryable_get(
            lambda: self._account_api.api_v1_me_get(_request_timeout=self._timeout),
            self._retry,
        )
