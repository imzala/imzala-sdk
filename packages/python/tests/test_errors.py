import json

from imzala.errors import (
    ImzalaAuthError,
    ImzalaError,
    ImzalaRateLimitError,
    ImzalaValidationError,
    map_api_exception,
)
from imzala_client.exceptions import ApiException


def fake_api_exception(status: int, data, headers=None) -> ApiException:
    """Shaped like what the generated client actually raises: `.status`,
    `.reason`, `.body` (raw JSON text), `.headers` — see
    `imzala_client.exceptions.ApiException` / `ApiClient.response_deserialize`."""
    exc = ApiException(status=status, reason=f"status {status}", body=json.dumps(data))
    exc.headers = headers if headers is not None else {}
    return exc


def test_maps_401_to_auth_error():
    err = map_api_exception(
        fake_api_exception(401, {"success": False, "error": "INVALID_API_KEY", "message": "Invalid API key"})
    )
    assert isinstance(err, ImzalaAuthError)
    assert isinstance(err, ImzalaError)
    assert err.status_code == 401
    assert err.code == "INVALID_API_KEY"
    assert str(err) == "Invalid API key"


def test_maps_403_to_auth_error():
    err = map_api_exception(fake_api_exception(403, {"success": False, "error": "INSUFFICIENT_SCOPE"}))
    assert isinstance(err, ImzalaAuthError)
    assert err.status_code == 403
    assert err.code == "INSUFFICIENT_SCOPE"


def test_maps_429_to_rate_limit_error_reading_retry_after_from_nested_object():
    err = map_api_exception(
        fake_api_exception(
            429,
            {
                "success": False,
                "error": {"code": "RATE_LIMITED", "message": "Too many requests", "retry_after_seconds": 42},
            },
        )
    )
    assert isinstance(err, ImzalaRateLimitError)
    assert isinstance(err, ImzalaError)
    assert err.retry_after == 42
    assert err.code == "RATE_LIMITED"
    assert str(err) == "Too many requests"


def test_maps_429_retry_after_from_header_when_body_omits_it():
    err = map_api_exception(
        fake_api_exception(429, {"success": False, "error": "RATE_LIMITED"}, headers={"Retry-After": "30"})
    )
    assert isinstance(err, ImzalaRateLimitError)
    assert err.retry_after == 30.0


def test_maps_422_to_validation_error():
    err = map_api_exception(fake_api_exception(422, {"success": False, "message": "Invalid payload"}))
    assert isinstance(err, ImzalaValidationError)
    assert isinstance(err, ImzalaError)
    assert err.status_code == 422
    assert str(err) == "Invalid payload"


def test_falls_back_to_base_error_for_unmapped_statuses():
    err404 = map_api_exception(fake_api_exception(404, {"success": False, "error": "DEMAND_NOT_FOUND"}))
    assert isinstance(err404, ImzalaError)
    assert not isinstance(err404, ImzalaAuthError)
    assert not isinstance(err404, ImzalaRateLimitError)
    assert not isinstance(err404, ImzalaValidationError)
    assert err404.status_code == 404
    assert err404.code == "DEMAND_NOT_FOUND"

    err500 = map_api_exception(fake_api_exception(500, {"success": False}))
    assert err500.status_code == 500


def test_wraps_a_plain_exception_as_a_base_imzala_error():
    err = map_api_exception(ConnectionError("ECONNREFUSED"))
    assert isinstance(err, ImzalaError)
    assert not isinstance(err, ImzalaAuthError)
    assert str(err) == "ECONNREFUSED"
    assert err.status_code is None


def test_passes_an_already_typed_imzala_error_through_unchanged():
    original = ImzalaValidationError("bad payload", status_code=422)
    assert map_api_exception(original) is original


def test_every_subclass_is_instance_of_imzala_error_and_exception():
    for err in (ImzalaAuthError("a"), ImzalaRateLimitError("b"), ImzalaValidationError("c")):
        assert isinstance(err, ImzalaError)
        assert isinstance(err, Exception)
