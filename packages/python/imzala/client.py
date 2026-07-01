"""imzala.org server-side SDK — an ergonomic, hand-written facade over the
generated (python, urllib3+pydantic) client in `imzala_client`.

Mirrors the `@imzala/node` TypeScript facade
(`packages/node/src/index.ts` in this monorepo) method-for-method, so
server code reads the same regardless of language.
"""

from __future__ import annotations

import json
from typing import Any, Callable, Mapping, Optional, Sequence, Union

from imzala_client.api.account_api import AccountApi
from imzala_client.api.demands_api import DemandsApi
from imzala_client.api.reminders_api import RemindersApi
from imzala_client.api.templates_api import TemplatesApi
from imzala_client.api.timestamps_api import TimestampsApi
from imzala_client.api_client import ApiClient
from imzala_client.configuration import Configuration

from .errors import ImzalaError, map_api_exception
from .files import FileInput, UploadPartyInput, to_multipart_tuple

__all__ = ["Imzala", "DEFAULT_BASE_URL", "DEFAULT_TIMEOUT_S"]

DEFAULT_BASE_URL = "https://api-prd.imzala.org"
DEFAULT_TIMEOUT_S = 30.0


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

    def __init__(self, api: TemplatesApi, timeout: float) -> None:
        self._api = api
        self._timeout = timeout

    def list(self, *, page: Optional[int] = None, limit: Optional[int] = None) -> Any:
        """Lists your active templates."""
        return _unwrap(
            lambda: self._api.api_v1_templates_get(page=page, limit=limit, _request_timeout=self._timeout)
        )

    def get(self, template_id: str) -> Any:
        """Returns a template's parties + fillable variables."""
        return _unwrap(
            lambda: self._api.api_v1_templates_id_get(id=template_id, _request_timeout=self._timeout)
        )

    def usage(self, template_id: str) -> Any:
        """Returns a ready-to-use integration guide (endpoint, required
        headers, example curl+JSON) for a template."""
        return _unwrap(
            lambda: self._api.api_v1_templates_id_usage_get(id=template_id, _request_timeout=self._timeout)
        )


class DemandsResource:
    """`imzala.demands.*` — create/inspect demands (contracts) and trigger reminders."""

    def __init__(self, api: DemandsApi, reminders_api: RemindersApi, timeout: float) -> None:
        self._api = api
        self._reminders_api = reminders_api
        self._timeout = timeout

    def create(self, body: Mapping[str, Any]) -> Any:
        """Creates a new demand (contract) from a template."""
        return _unwrap(
            lambda: self._api.api_v1_demands_post(
                create_demand_request=dict(body), _request_timeout=self._timeout
            )
        )

    def get(self, demand_id: str) -> Any:
        """Returns a demand's status + per-party signing progress."""
        return _unwrap(
            lambda: self._api.api_v1_demands_id_get(id=demand_id, _request_timeout=self._timeout)
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
        reminders per channel (not overridable)."""
        return _unwrap(
            lambda: self._reminders_api.api_v1_demands_id_reminders_post(
                id=demand_id,
                trigger_reminder_request=dict(body) if body else {},
                _request_timeout=self._timeout,
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
    ) -> None:
        """
        Args:
            api_key: `imz_<64 hex>` — from Dashboard -> Geliştirici -> API
                Anahtarları, or Hesap Ayarları -> API Anahtarları.
            base_url: defaults to `https://api-prd.imzala.org`. Use
                `https://test-api.imzala.org` for the test environment.
            timeout: per-request timeout, in seconds. Defaults to 30.0.
        """
        if not api_key:
            raise ValueError("Imzala(api_key=...) — api_key is required.")

        self._timeout = float(timeout)

        configuration = Configuration(host=base_url, api_key={"ApiKeyAuth": api_key})
        api_client = ApiClient(configuration)

        self._account_api = AccountApi(api_client)
        demands_api = DemandsApi(api_client)
        reminders_api = RemindersApi(api_client)
        templates_api = TemplatesApi(api_client)
        timestamps_api = TimestampsApi(api_client)

        self.templates = TemplatesResource(templates_api, self._timeout)
        self.demands = DemandsResource(demands_api, reminders_api, self._timeout)
        self.embed = EmbedResource(demands_api, self._timeout)
        self.timestamps = TimestampsResource(timestamps_api, self._timeout)

    def me(self) -> Any:
        """Returns the calling API key's owner info (id, email, name,
        workspace, remaining credits). Requires the `timestamps` scope."""
        return _unwrap(lambda: self._account_api.api_v1_me_get(_request_timeout=self._timeout))
