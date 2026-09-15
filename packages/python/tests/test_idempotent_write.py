import json
from unittest.mock import MagicMock

import pytest

from imzala.client import MAX_IDEMPOTENT_RETRY_WAIT_S, _unwrap_idempotent_write
from imzala.errors import ImzalaError, ImzalaRateLimitError
from imzala_client.exceptions import ApiException


def rate_limited(retry_after_seconds):
    exc = ApiException(
        status=429,
        reason="Too Many Requests",
        body=json.dumps(
            {
                "success": False,
                "error": "Çok fazla istek",
                "code": "RATE_LIMIT_EXCEEDED",
                "retry_after_seconds": retry_after_seconds,
            }
        ),
    )
    exc.headers = {"Retry-After": str(retry_after_seconds)}
    return exc


def api_error(status, body=None):
    exc = ApiException(status=status, reason=f"status {status}", body=json.dumps(body or {"success": False}))
    exc.headers = {}
    return exc


class Envelope:
    def __init__(self, data):
        self.success = True
        self.data = data


def run(call, sleep, **kwargs):
    kwargs.setdefault("retry_base_delay_s", 0.001)
    return _unwrap_idempotent_write(call, sleep=sleep, **kwargs)


def test_default_cap_is_sixty_seconds():
    assert MAX_IDEMPOTENT_RETRY_WAIT_S == 60.0


def test_a_write_without_a_key_is_never_retried_on_429():
    call = MagicMock(side_effect=rate_limited(0))
    sleep = MagicMock()
    with pytest.raises(ImzalaRateLimitError):
        run(call, sleep, idempotency_key=None)
    assert call.call_count == 1
    sleep.assert_not_called()


def test_an_empty_key_counts_as_no_key():
    call = MagicMock(side_effect=rate_limited(0))
    with pytest.raises(ImzalaRateLimitError):
        run(call, MagicMock(), idempotency_key="")
    assert call.call_count == 1


def test_a_write_with_a_key_is_retried_exactly_once_on_429():
    call = MagicMock(side_effect=[rate_limited(0), Envelope({"id": "ok"})])
    assert run(call, MagicMock(), idempotency_key="k-1") == {"id": "ok"}
    assert call.call_count == 2


def test_a_second_429_is_raised_not_retried_again():
    call = MagicMock(side_effect=rate_limited(0))
    with pytest.raises(ImzalaRateLimitError):
        run(call, MagicMock(), idempotency_key="k-2")
    assert call.call_count == 2


def test_non_429_errors_are_not_retried_even_5xx():
    conflict = MagicMock(side_effect=api_error(409, {"success": False, "error": "x", "code": "TEMPLATE_IN_USE"}))
    with pytest.raises(ImzalaError) as info:
        run(conflict, MagicMock(), idempotency_key="k-3")
    assert info.value.code == "TEMPLATE_IN_USE"
    assert conflict.call_count == 1

    server_error = MagicMock(side_effect=api_error(503))
    with pytest.raises(ImzalaError):
        run(server_error, MagicMock(), idempotency_key="k-4")
    assert server_error.call_count == 1


def test_waits_for_retry_after_before_the_retry():
    order = []
    call = MagicMock(side_effect=[rate_limited(2), Envelope({"id": "later"})])
    sleep = MagicMock(side_effect=lambda s: order.append(("sleep", s)))

    def tracked():
        order.append("call")
        return call()

    assert run(tracked, sleep, idempotency_key="k-5") == {"id": "later"}
    assert order == ["call", ("sleep", 2.0), "call"]


def test_without_retry_after_it_waits_the_base_delay():
    exc = api_error(429, {"success": False, "code": "RATE_LIMIT_EXCEEDED"})
    call = MagicMock(side_effect=[exc, Envelope({"id": "x"})])
    sleep = MagicMock()
    run(call, sleep, idempotency_key="k-8", retry_base_delay_s=0.25)
    sleep.assert_called_once_with(0.25)


def test_raises_instead_of_waiting_when_retry_after_exceeds_the_cap():
    call = MagicMock(side_effect=rate_limited(3600))
    sleep = MagicMock()
    with pytest.raises(ImzalaRateLimitError):
        run(call, sleep, idempotency_key="k-6")
    assert call.call_count == 1
    sleep.assert_not_called()

    retried = MagicMock(side_effect=[rate_limited(2), Envelope({"id": "x"})])
    with pytest.raises(ImzalaRateLimitError):
        run(retried, sleep, idempotency_key="k-7", max_wait_s=1.0)
    assert retried.call_count == 1
    sleep.assert_not_called()


def test_a_wait_equal_to_the_cap_is_still_retried():
    call = MagicMock(side_effect=[rate_limited(60), Envelope({"id": "x"})])
    sleep = MagicMock()
    assert run(call, sleep, idempotency_key="k-9") == {"id": "x"}
    sleep.assert_called_once_with(60.0)
