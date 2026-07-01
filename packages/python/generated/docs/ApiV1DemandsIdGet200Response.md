# ApiV1DemandsIdGet200Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**data** | [**DemandStatus**](DemandStatus.md) |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_id_get200_response import ApiV1DemandsIdGet200Response

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsIdGet200Response from a JSON string
api_v1_demands_id_get200_response_instance = ApiV1DemandsIdGet200Response.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsIdGet200Response.to_json())

# convert the object into a dict
api_v1_demands_id_get200_response_dict = api_v1_demands_id_get200_response_instance.to_dict()
# create an instance of ApiV1DemandsIdGet200Response from a dict
api_v1_demands_id_get200_response_from_dict = ApiV1DemandsIdGet200Response.from_dict(api_v1_demands_id_get200_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


