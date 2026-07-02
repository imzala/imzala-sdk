from types import SimpleNamespace
from unittest.mock import patch

from imzala import Imzala
from imzala_client.api.templates_api import TemplatesApi


def page(templates, total: int, page_num: int, limit: int) -> SimpleNamespace:
    return SimpleNamespace(
        success=True,
        data={"templates": templates, "total": total, "page": page_num, "limit": limit},
    )


class TestTemplatesListAllPaginationIterator:
    def test_walks_two_full_pages_then_a_short_page_yielding_every_item_and_stopping(self):
        with patch.object(
            TemplatesApi,
            "api_v1_templates_get",
            side_effect=[
                page([{"id": "t1"}, {"id": "t2"}], 5, 1, 2),
                page([{"id": "t3"}, {"id": "t4"}], 5, 2, 2),
                page([{"id": "t5"}], 5, 3, 2),  # short page (1 < limit 2) -> stop
            ],
        ) as mocked:
            client = Imzala(api_key="imz_test")

            ids = [t["id"] for t in client.templates.list_all(limit=2)]

        assert ids == ["t1", "t2", "t3", "t4", "t5"]
        assert mocked.call_count == 3
        assert mocked.call_args_list[0].kwargs["page"] == 1
        assert mocked.call_args_list[0].kwargs["limit"] == 2
        assert mocked.call_args_list[1].kwargs["page"] == 2
        assert mocked.call_args_list[2].kwargs["page"] == 3

    def test_stops_as_soon_as_total_reached_even_when_last_page_is_exactly_full(self):
        with patch.object(
            TemplatesApi,
            "api_v1_templates_get",
            side_effect=[
                page([{"id": "t1"}, {"id": "t2"}], 4, 1, 2),
                page([{"id": "t3"}, {"id": "t4"}], 4, 2, 2),
            ],
        ) as mocked:
            # If the total-reached check didn't exist, a 3rd call would happen
            # here (the 2nd page was exactly `limit` items, not "short") and
            # the side_effect list would be exhausted -> StopIteration from the
            # mock, catching a regression (no infinite loop).
            client = Imzala(api_key="imz_test")

            ids = [t["id"] for t in client.templates.list_all(limit=2)]

        assert ids == ["t1", "t2", "t3", "t4"]
        assert mocked.call_count == 2

    def test_stops_immediately_on_an_empty_first_page_no_infinite_loop(self):
        with patch.object(
            TemplatesApi, "api_v1_templates_get", side_effect=[page([], 0, 1, 20)]
        ) as mocked:
            client = Imzala(api_key="imz_test")

            ids = [t["id"] for t in client.templates.list_all()]

        assert ids == []
        assert mocked.call_count == 1

    def test_list_single_page_is_unaffected_still_returns_one_page_not_an_iterator(self):
        with patch.object(
            TemplatesApi,
            "api_v1_templates_get",
            side_effect=[page([{"id": "t1"}, {"id": "t2"}], 5, 1, 2)],
        ):
            client = Imzala(api_key="imz_test")

            result = client.templates.list(page=1, limit=2)

        assert result["templates"] == [{"id": "t1"}, {"id": "t2"}]
        assert result["total"] == 5
