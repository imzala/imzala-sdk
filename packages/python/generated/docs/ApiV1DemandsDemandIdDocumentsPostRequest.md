# ApiV1DemandsDemandIdDocumentsPostRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**title** | **str** | Belge başlığı. Boş olamaz. | 
**doc_kind** | **str** | &#x60;KVKK_CONSENT&#x60; ile &#x60;is_required: true&#x60; **birlikte gönderilemez** (400 &#x60;CONSENT_CANNOT_BE_REQUIRED&#x60; — açık rıza reddedilebilir olmak zorundadır). &#x60;PREINFO&#x60; ile &#x60;is_required: false&#x60; **birlikte gönderilemez** (400 &#x60;PREINFO_MUST_BE_REQUIRED&#x60;).  | [optional] [default to 'OTHER']
**is_required** | **bool** |  | [optional] [default to True]
**signature_required** | **bool** |  | [optional] [default to True]

## Example

```python
from imzala_client.models.api_v1_demands_demand_id_documents_post_request import ApiV1DemandsDemandIdDocumentsPostRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsDemandIdDocumentsPostRequest from a JSON string
api_v1_demands_demand_id_documents_post_request_instance = ApiV1DemandsDemandIdDocumentsPostRequest.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsDemandIdDocumentsPostRequest.to_json())

# convert the object into a dict
api_v1_demands_demand_id_documents_post_request_dict = api_v1_demands_demand_id_documents_post_request_instance.to_dict()
# create an instance of ApiV1DemandsDemandIdDocumentsPostRequest from a dict
api_v1_demands_demand_id_documents_post_request_from_dict = ApiV1DemandsDemandIdDocumentsPostRequest.from_dict(api_v1_demands_demand_id_documents_post_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


