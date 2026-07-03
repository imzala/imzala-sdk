# ApiV1DemandsGet200ResponseData


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demands** | [**List[ApiV1DemandsGet200ResponseDataDemandsInner]**](ApiV1DemandsGet200ResponseDataDemandsInner.md) |  | [optional] 
**total** | **int** |  | [optional] 
**page** | **int** |  | [optional] 
**limit** | **int** |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_get200_response_data import ApiV1DemandsGet200ResponseData

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsGet200ResponseData from a JSON string
api_v1_demands_get200_response_data_instance = ApiV1DemandsGet200ResponseData.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsGet200ResponseData.to_json())

# convert the object into a dict
api_v1_demands_get200_response_data_dict = api_v1_demands_get200_response_data_instance.to_dict()
# create an instance of ApiV1DemandsGet200ResponseData from a dict
api_v1_demands_get200_response_data_from_dict = ApiV1DemandsGet200ResponseData.from_dict(api_v1_demands_get200_response_data_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


