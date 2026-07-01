# ApiV1MeGet200ResponseData


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**email** | **str** |  | [optional] 
**first_name** | **str** |  | [optional] 
**last_name** | **str** |  | [optional] 
**workspace** | [**ApiV1MeGet200ResponseDataWorkspace**](ApiV1MeGet200ResponseDataWorkspace.md) |  | [optional] 
**credits** | [**ApiV1MeGet200ResponseDataCredits**](ApiV1MeGet200ResponseDataCredits.md) |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_me_get200_response_data import ApiV1MeGet200ResponseData

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1MeGet200ResponseData from a JSON string
api_v1_me_get200_response_data_instance = ApiV1MeGet200ResponseData.from_json(json)
# print the JSON string representation of the object
print(ApiV1MeGet200ResponseData.to_json())

# convert the object into a dict
api_v1_me_get200_response_data_dict = api_v1_me_get200_response_data_instance.to_dict()
# create an instance of ApiV1MeGet200ResponseData from a dict
api_v1_me_get200_response_data_from_dict = ApiV1MeGet200ResponseData.from_dict(api_v1_me_get200_response_data_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


