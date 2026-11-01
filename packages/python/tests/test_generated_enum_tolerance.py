import json

import pytest

from imzala_client.models.demand_status import DemandStatus
from imzala_client.models.page_item import PageItem
from imzala_client.models.standard_error import StandardError
from imzala_client.models.template_variable import TemplateVariable
from imzala_client.models.webhook_envelope import WebhookEnvelope

# Response enums grow with the API (item_type gained contract_number in
# 1.14.5 and stamp in 1.14.6; status, decision, doc_kind, error codes and
# webhook types grow the same way). Since SDK 1.2.0 every enum the server can
# return is an open string: a value the client was generated without must not
# break deserialization.


def _page_item(item_type):
    return json.dumps({
        "page_id": 1, "item_type": item_type,
        "position_x": 0.1, "position_y": 0.2, "width": 0.3, "height": 0.1,
    })


DEMAND = json.dumps({
    "id": "00000000-0000-4000-8000-000000000001",
    "title": "Ornek",
    "status": "future_value",
    "parties": [{
        "party_id": "00000000-0000-4000-8000-000000000002",
        "locale": "future_value", "signed_locale": "future_value",
    }],
    "stamp_items": [{"item_id": 1, "source": "future_value"}],
    "documents": [{
        "id": "00000000-0000-4000-8000-000000000003",
        "doc_kind": "future_value",
        "progress": {"status": "future_value"},
        "sealing": {"status": "future_value"},
        "decisions": [{"party_id": "00000000-0000-4000-8000-000000000002", "decision": "future_value"}],
    }],
})


@pytest.mark.parametrize("value", ["contract_number", "stamp", "future_value"])
def test_page_item_keeps_item_type(value):
    assert PageItem.from_json(_page_item(value)).item_type == value


@pytest.mark.parametrize("value", ["stamp", "future_value"])
def test_template_variable_keeps_item_type(value):
    body = json.dumps({"slug": "a", "item_type": value})
    assert TemplateVariable.from_json(body).item_type == value


def test_demand_status_keeps_unknown_values():
    d = DemandStatus.from_json(DEMAND)
    assert d.status == "future_value"
    assert d.parties[0].locale == "future_value"
    assert d.parties[0].signed_locale == "future_value"
    assert d.stamp_items[0].source == "future_value"
    doc = d.documents[0]
    assert doc.doc_kind == "future_value"
    assert doc.progress.status == "future_value"
    assert doc.sealing.status == "future_value"
    assert doc.decisions[0].decision == "future_value"
    assert json.loads(d.to_json())["status"] == "future_value"


def test_webhook_type_and_error_code_keep_unknown_values():
    event = WebhookEnvelope.from_json(json.dumps({
        "id": "evt_0123456789abcdef0123456789abcdef", "type": "future_value",
        "created_at": "2026-11-01T00:00:00Z", "data": {},
    }))
    assert event.type == "future_value"
    error = StandardError.from_json(json.dumps({"success": False, "error": {"code": "future_value", "message": "Ornek"}}))
    assert error.error.code == "future_value"
