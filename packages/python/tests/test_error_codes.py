import json
import re
from email.utils import formatdate
from pathlib import Path
from types import SimpleNamespace
import time

import pytest
from urllib3._collections import HTTPHeaderDict

from imzala import Imzala
from imzala.error_codes import IMZALA_ERROR_CODES, describe_error_code, is_known_error_code
from imzala.errors import (
    ImzalaError,
    ImzalaRateLimitError,
    ImzalaRateLimitInfo,
    extract_error_code,
    map_api_exception,
)
from imzala_client.api.templates_api import TemplatesApi
from imzala_client.exceptions import ApiException

from .helpers import patch_api

REPO_ROOT = Path(__file__).resolve().parents[3]
SPEC_TEXT = (REPO_ROOT / "spec" / "openapi.v1.yaml").read_text(encoding="utf-8")
NODE_CATALOGUE_TEXT = (REPO_ROOT / "packages" / "node" / "src" / "errorCodes.ts").read_text(encoding="utf-8")

# Same list as packages/node/src/__tests__/errorCodes.test.ts.
NOT_ERROR_CODES = {
    "ANCHOR_TEXT_NOT_FOUND": "field layout diagnostic code inside a response body",
    "CREATE_FAILED": "per-row result code in the bulk 200 response",
    "DISPATCH_FAILED": "per-party invitation result code in a 200 response",
    "DISPATCH_SKIPPED": "invitation result code in a 200 response",
    "RECIPIENT_QUOTA_EXCEEDED": "per-party invitation result code in a 200 response",
    "ON_ANCHOR_MISS_NOT_RELAXED": "warning in a 200 response, not an error",
    "DEAD_LETTER": "webhook delivery status",
    "DEMAND_ID": "shell variable in a curl example",
    "ENVELOPE_DECISION_ENFORCE": "server feature flag name",
    "FIELD_LAYOUT": "template kind value",
    "FILLABLE_TYPES": "server constant name in prose",
    "IMZALA_WEBHOOK_SECRET": "environment variable in a code sample",
    "KVKK_CONSENT": "doc_kind value",
    "KVKK_NOTICE": "doc_kind value",
    "PRICE_LIST": "doc_kind value",
    "WEBHOOK_TIMEOUT_MS": "server environment variable",
}

# Prose also contains single upper-case words (API, PDF, KVKK), so the
# spec-to-catalogue direction only considers underscore tokens. Single-word
# codes such as UNAUTHORIZED are still checked the other way round.
CODE_TOKEN = re.compile(r"\b[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)+\b")


def _in_spec(token):
    return re.search(r"\b" + re.escape(token) + r"\b", SPEC_TEXT) is not None


class TestCatalogueMatchesSpec:
    def test_every_catalogued_code_appears_in_the_public_spec(self):
        assert [c for c in IMZALA_ERROR_CODES if not _in_spec(c)] == []

    def test_every_code_like_token_in_the_spec_is_catalogued_or_excluded(self):
        tokens = sorted(set(CODE_TOKEN.findall(SPEC_TEXT)))
        unclassified = [t for t in tokens if not is_known_error_code(t) and t not in NOT_ERROR_CODES]
        assert unclassified == []

    def test_the_exclusion_list_has_no_stale_or_overlapping_entries(self):
        for token in NOT_ERROR_CODES:
            assert _in_spec(token), token
            assert not is_known_error_code(token), token

    def test_every_description_is_a_non_empty_single_line_without_an_em_dash(self):
        for code, text in IMZALA_ERROR_CODES.items():
            assert text.strip(), code
            assert "\n" not in text, code
            assert "\u2014" not in text, code

    def test_unknown_or_missing_codes_return_none_instead_of_raising(self):
        assert describe_error_code("NEVER_A_REAL_CODE") is None
        assert describe_error_code(None) is None
        assert is_known_error_code("__class__") is False
        assert is_known_error_code(42) is False


class TestCatalogueMatchesNodeSdk:
    """Locks the two SDKs to one catalogue: same codes, same text."""

    def _node_catalogue(self):
        body = NODE_CATALOGUE_TEXT.split("export const IMZALA_ERROR_CODES = {", 1)[1].split("} as const;", 1)[0]
        entries = re.findall(r"^\s*([A-Z][A-Z0-9_]*):\s*'([^']*)',", body, re.MULTILINE)
        assert entries, "could not read any entry from errorCodes.ts"
        return dict(entries)

    def test_same_code_set_as_the_node_catalogue(self):
        assert set(IMZALA_ERROR_CODES) == set(self._node_catalogue())

    def test_same_descriptions_as_the_node_catalogue(self):
        node = self._node_catalogue()
        drifted = [code for code, text in IMZALA_ERROR_CODES.items() if node.get(code) != text]
        assert drifted == []


def fake_api_exception(status, data, headers=None):
    exc = ApiException(status=status, reason=f"status {status}", body=json.dumps(data))
    exc.headers = headers if headers is not None else {}
    return exc


class TestErrorBodyShapes:
    def test_error_code_with_message_reads_the_code_from_error(self):
        body = {"success": False, "error": "PAGE_ID_REQUIRED", "message": "Each item must have an integer page_id"}
        assert extract_error_code(body) == "PAGE_ID_REQUIRED"

    def test_human_text_with_code_reads_the_code_never_the_text(self):
        err = map_api_exception(
            fake_api_exception(
                400, {"success": False, "error": "Geçersiz sayfa numarası (page >= 1 olmalı)", "code": "INVALID_PAGE"}
            )
        )
        assert err.code == "INVALID_PAGE"
        assert str(err) == "Geçersiz sayfa numarası (page >= 1 olmalı)"
        assert err.code_description == IMZALA_ERROR_CODES["INVALID_PAGE"]

    def test_nested_error_object_reads_the_nested_code(self):
        body = {"success": False, "error": {"code": "DEMAND_NOT_DISPATCHABLE", "message": "x"}}
        assert extract_error_code(body) == "DEMAND_NOT_DISPATCHABLE"

    def test_a_single_word_code_in_error_is_still_a_code(self):
        assert extract_error_code({"error": "UNAUTHORIZED"}) == "UNAUTHORIZED"

    def test_a_plain_human_readable_error_string_is_not_reported_as_a_code(self):
        err = map_api_exception(fake_api_exception(404, {"success": False, "error": "Sözleşme bulunamadı"}))
        assert err.code is None
        assert str(err) == "Sözleşme bulunamadı"
        assert err.code_description is None

    def test_unknown_code_is_kept_without_a_description(self):
        err = map_api_exception(fake_api_exception(409, {"success": False, "code": "SOME_FUTURE_CODE"}))
        assert err.code == "SOME_FUTURE_CODE"
        assert err.code_description is None

    def test_constructing_an_error_directly_fills_code_description(self):
        assert ImzalaError("x", code="TEMPLATE_IN_USE").code_description == IMZALA_ERROR_CODES["TEMPLATE_IN_USE"]
        assert ImzalaError("x").code_description is None

    def test_success_false_on_a_2xx_carries_the_code_and_message(self):
        response = SimpleNamespace(success=False, data=None, error="Geçersiz istek", code="VALIDATION_FAIL")
        with patch_api(TemplatesApi, "api_v1_templates_id_get", return_value=response):
            client = Imzala(api_key="imz_test")
            with pytest.raises(ImzalaError) as info:
                client.templates.get("t1")
        assert info.value.code == "VALIDATION_FAIL"
        assert str(info.value) == "Geçersiz istek"
        assert info.value.code_description == IMZALA_ERROR_CODES["VALIDATION_FAIL"]


class TestRateLimitErrors:
    def test_attaches_the_description_and_reads_retry_after_from_the_header(self):
        err = map_api_exception(
            fake_api_exception(
                429,
                {"success": False, "error": "Çok fazla istek", "code": "RATE_LIMIT_EXCEEDED"},
                headers={"retry-after": "30"},
            )
        )
        assert isinstance(err, ImzalaRateLimitError)
        assert err.code == "RATE_LIMIT_EXCEEDED"
        assert err.code_description == IMZALA_ERROR_CODES["RATE_LIMIT_EXCEEDED"]
        assert err.retry_after == 30

    def test_an_http_date_retry_after_is_converted_to_seconds(self):
        at = formatdate(time.time() + 30, usegmt=True)
        err = map_api_exception(
            fake_api_exception(429, {"success": False, "code": "RATE_LIMIT_EXCEEDED"}, headers={"Retry-After": at})
        )
        assert 28 <= err.retry_after <= 30

    def test_a_past_http_date_retry_after_is_zero_not_negative(self):
        at = formatdate(time.time() - 120, usegmt=True)
        err = map_api_exception(fake_api_exception(429, {"success": False}, headers={"Retry-After": at}))
        assert err.retry_after == 0

    def test_reads_retry_after_seconds_from_the_body_when_there_is_no_header(self):
        err = map_api_exception(
            fake_api_exception(
                429,
                {
                    "success": False,
                    "error": "Çok fazla istek gönderildi. Lütfen bir dakika bekleyin.",
                    "code": "RATE_LIMIT_EXCEEDED",
                    "retry_after_seconds": 60,
                },
                headers={"ratelimit-limit": "60", "ratelimit-policy": "60;w=60"},
            )
        )
        assert err.code == "RATE_LIMIT_EXCEEDED"
        assert err.retry_after == 60

    def test_reads_the_standard_ratelimit_headers_and_ignores_x_ratelimit(self):
        headers = HTTPHeaderDict(
            {
                "RateLimit-Limit": "5",
                "RateLimit-Remaining": "0",
                "RateLimit-Reset": "42",
                "RateLimit-Policy": "5;w=60",
                "X-RateLimit-Limit": "999",
            }
        )
        err = map_api_exception(
            fake_api_exception(429, {"success": False, "code": "RATE_LIMIT_EXCEEDED", "retry_after_seconds": 60}, headers)
        )
        assert err.rate_limit == ImzalaRateLimitInfo(limit=5, remaining=0, reset=42, policy="5;w=60")

    def test_header_names_are_read_case_insensitively_from_a_plain_dict(self):
        err = map_api_exception(
            fake_api_exception(429, {"success": False}, headers={"ratelimit-limit": "60", "RETRY-AFTER": "7"})
        )
        assert err.rate_limit == ImzalaRateLimitInfo(limit=60)
        assert err.retry_after == 7

    def test_only_x_ratelimit_headers_leave_rate_limit_none(self):
        err = map_api_exception(
            fake_api_exception(429, {"success": False}, headers={"X-RateLimit-Limit": "60", "X-RateLimit-Remaining": "0"})
        )
        assert err.rate_limit is None

    def test_leaves_rate_limit_none_when_the_server_sent_no_ratelimit_headers(self):
        err = map_api_exception(fake_api_exception(429, {"success": False, "error": {"code": "RATE_LIMITED"}}))
        assert err.rate_limit is None
