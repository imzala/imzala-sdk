# ApiV1DemandsIdCancelPostRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**reason** | **str** | İptal nedeni (opsiyonel) | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_id_cancel_post_request import ApiV1DemandsIdCancelPostRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsIdCancelPostRequest from a JSON string
api_v1_demands_id_cancel_post_request_instance = ApiV1DemandsIdCancelPostRequest.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsIdCancelPostRequest.to_json())

# convert the object into a dict
api_v1_demands_id_cancel_post_request_dict = api_v1_demands_id_cancel_post_request_instance.to_dict()
# create an instance of ApiV1DemandsIdCancelPostRequest from a dict
api_v1_demands_id_cancel_post_request_from_dict = ApiV1DemandsIdCancelPostRequest.from_dict(api_v1_demands_id_cancel_post_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


