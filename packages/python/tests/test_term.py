"""Contract term tracking and archive helpers, checked on the wire against a
local HTTP server: path slots, the partial-update body (None is sent, an
omitted key is not), the archive filter on list, and that none of these
writes is retried."""

from __future__ import annotations

from urllib.parse import parse_qs, urlparse

import pytest

from imzala.errors import ImzalaError, ImzalaRateLimitError

from .test_envelope import DEMAND, RATE_LIMITED, as_dict, client, json_body, ok, serve  # noqa: F401

TERM = {
    "start_mode": "ON_COMPLETION",
    "duration_months": 12,
    "renewal_type": "AUTO_RENEW",
    "renewal_period_months": 12,
    "notice_days": 30,
    "reminder_offsets": [30, 7],
    "notify_counterparty": False,
    "state": "UNTRACKED",
}


class TestUpdateTerm:
    def test_patches_only_the_sent_keys_and_sends_none(self, serve):
        srv = serve(ok({"term": TERM}))
        result = client(srv).demands.update_term(
            DEMAND,
            {
                "term_start_mode": "ON_COMPLETION",
                "term_duration_months": 12,
                "renewal_type": "AUTO_RENEW",
                "notice_days": None,
            },
        )
        assert as_dict(result)["term"]["renewal_type"] == "AUTO_RENEW"
        assert srv.calls[0]["method"] == "PATCH"
        assert srv.calls[0]["path"] == f"/api/v1/demands/{DEMAND}/term"
        # None clears a key on the server; keys that were not passed stay out.
        assert json_body(srv.calls[0]) == {
            "term_start_mode": "ON_COMPLETION",
            "term_duration_months": 12,
            "renewal_type": "AUTO_RENEW",
            "notice_days": None,
        }

    def test_dates_are_sent_as_iso_strings(self, serve):
        srv = serve(ok({"term": TERM}))
        client(srv).demands.update_term(DEMAND, {"term_fixed_end_date": "2027-01-31"})
        assert json_body(srv.calls[0]) == {"term_fixed_end_date": "2027-01-31"}

    def test_notify_counterparty_is_only_sent_when_given(self, serve):
        srv = serve(ok({"term": TERM}), ok({"term": TERM}))
        client(srv).demands.update_term(DEMAND, {"notice_days": 30})
        client(srv).demands.update_term(DEMAND, {"notify_counterparty": False})
        assert json_body(srv.calls[0]) == {"notice_days": 30}
        assert json_body(srv.calls[1]) == {"notify_counterparty": False}

    def test_raises_term_invalid_with_the_rejected_field(self, serve):
        srv = serve(
            {
                "status": 400,
                "body": {"success": False, "error": "x", "code": "TERM_INVALID", "field": "term_fixed_end_date"},
            }
        )
        with pytest.raises(ImzalaError) as info:
            client(srv).demands.update_term(
                DEMAND, {"term_fixed_end_date": "2027-01-31", "term_duration_months": 12}
            )
        assert info.value.code == "TERM_INVALID"
        assert info.value.body["field"] == "term_fixed_end_date"
        assert len(srv.calls) == 1


class TestArchive:
    def test_archive_posts_to_the_archive_path(self, serve):
        srv = serve(ok({"archived_at": "2026-09-28T09:00:00.000Z"}))
        result = client(srv).demands.archive(DEMAND)
        assert as_dict(result)["archived_at"].startswith("2026-09-28T09:00:00")
        assert srv.calls[0]["method"] == "POST"
        assert srv.calls[0]["path"] == f"/api/v1/demands/{DEMAND}/archive"

    def test_unarchive_posts_to_the_unarchive_path(self, serve):
        srv = serve(ok({"archived_at": None}))
        client(srv).demands.unarchive(DEMAND)
        assert srv.calls[0]["method"] == "POST"
        assert srv.calls[0]["path"] == f"/api/v1/demands/{DEMAND}/unarchive"

    @pytest.mark.parametrize("code", ["DEMAND_NOT_ARCHIVABLE", "DEMAND_REJECTED_CANCEL_FIRST"])
    def test_archive_raises(self, serve, code):
        srv = serve({"status": 409, "body": {"success": False, "error": "x", "code": code}})
        with pytest.raises(ImzalaError) as info:
            client(srv).demands.archive(DEMAND)
        assert info.value.code == code
        assert len(srv.calls) == 1

    def test_delete_of_an_archived_demand_raises_demand_archived(self, serve):
        srv = serve({"status": 409, "body": {"success": False, "error": "x", "code": "DEMAND_ARCHIVED"}})
        with pytest.raises(ImzalaError) as info:
            client(srv).demands.delete(DEMAND)
        assert info.value.code == "DEMAND_ARCHIVED"


class TestListArchiveFilter:
    def test_sends_archived_query(self, serve):
        srv = serve(ok({"demands": [], "pagination": {"page": 1, "limit": 20, "total": 0}}))
        client(srv).demands.list(archived="exclude")
        assert parse_qs(urlparse(srv.calls[0]["path"]).query)["archived"] == ["exclude"]

    def test_leaves_archived_out_when_not_set(self, serve):
        srv = serve(ok({"demands": [], "pagination": {"page": 1, "limit": 20, "total": 0}}))
        client(srv).demands.list()
        assert "archived" not in parse_qs(urlparse(srv.calls[0]["path"]).query)


class TestNoRetry:
    @pytest.mark.parametrize(
        "call",
        [
            lambda c: c.demands.update_term(DEMAND, {"notice_days": 30}),
            lambda c: c.demands.archive(DEMAND),
            lambda c: c.demands.unarchive(DEMAND),
        ],
        ids=["update_term", "archive", "unarchive"],
    )
    def test_rate_limited_write_is_not_retried(self, serve, call):
        srv = serve(RATE_LIMITED, ok({}))
        with pytest.raises(ImzalaRateLimitError):
            call(client(srv))
        assert len(srv.calls) == 1
