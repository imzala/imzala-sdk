import json

import pytest

from imzala_client.models.api_v1_demands_post400_response import ApiV1DemandsPost400Response
from imzala_client.models.coded_error import CodedError
from imzala_client.models.document_selection_error import DocumentSelectionError


@pytest.mark.parametrize("branch", [
    CodedError(success=False, error="Synthetic validation", code="FUTURE_COLLECTION_ERROR", field="documents"),
    DocumentSelectionError(success=False, error="Synthetic selection", code="INVALID_DOCUMENT_SELECTION"),
])
def test_published_single400_branch_constructor_and_serialization(branch):
    wrapper = ApiV1DemandsPost400Response(branch)
    assert wrapper.actual_instance is branch
    assert wrapper.any_of_schemas == {"CodedError", "DocumentSelectionError"}
    assert json.loads(wrapper.to_json()) == json.loads(branch.to_json())
    restored = ApiV1DemandsPost400Response.from_json(wrapper.to_json())
    assert restored.actual_instance.code == branch.code
