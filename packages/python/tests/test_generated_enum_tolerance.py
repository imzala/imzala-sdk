import json

import pytest

from imzala_client.models.page_item import PageItem
from imzala_client.models.template_variable import TemplateVariable

# item_type grows with the API (contract_number in 1.14.5, stamp in 1.14.6).
# A value the client was generated without must not break deserialization.


def _page_item(item_type):
    return json.dumps({
        "page_id": 1, "item_type": item_type,
        "position_x": 0.1, "position_y": 0.2, "width": 0.3, "height": 0.1,
    })


@pytest.mark.parametrize("value", ["contract_number", "stamp", "future_type"])
def test_page_item_keeps_item_type(value):
    assert PageItem.from_json(_page_item(value)).item_type == value


@pytest.mark.parametrize("value", ["stamp", "future_type"])
def test_template_variable_keeps_item_type(value):
    body = json.dumps({"slug": "a", "item_type": value})
    assert TemplateVariable.from_json(body).item_type == value
