# ApiV1DemandsDemandIdDocumentsDocIdPatchRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**title** | **str** | Gönderilirse boş olamaz. | [optional] 
**doc_kind** | **str** |  | [optional] 
**is_required** | **bool** | Gönderilmezse değişmez. | [optional] 
**signature_required** | **bool** | Gönderilmezse değişmez. | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_demand_id_documents_doc_id_patch_request import ApiV1DemandsDemandIdDocumentsDocIdPatchRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsDemandIdDocumentsDocIdPatchRequest from a JSON string
api_v1_demands_demand_id_documents_doc_id_patch_request_instance = ApiV1DemandsDemandIdDocumentsDocIdPatchRequest.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsDemandIdDocumentsDocIdPatchRequest.to_json())

# convert the object into a dict
api_v1_demands_demand_id_documents_doc_id_patch_request_dict = api_v1_demands_demand_id_documents_doc_id_patch_request_instance.to_dict()
# create an instance of ApiV1DemandsDemandIdDocumentsDocIdPatchRequest from a dict
api_v1_demands_demand_id_documents_doc_id_patch_request_from_dict = ApiV1DemandsDemandIdDocumentsDocIdPatchRequest.from_dict(api_v1_demands_demand_id_documents_doc_id_patch_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


