# ApiV1DemandsPost402Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**error** | **str** |  | [optional] 
**code** | **str** |  | [optional] 
**data** | [**ApiV1DemandsPost402ResponseData**](ApiV1DemandsPost402ResponseData.md) |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_post402_response import ApiV1DemandsPost402Response

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsPost402Response from a JSON string
api_v1_demands_post402_response_instance = ApiV1DemandsPost402Response.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsPost402Response.to_json())

# convert the object into a dict
api_v1_demands_post402_response_dict = api_v1_demands_post402_response_instance.to_dict()
# create an instance of ApiV1DemandsPost402Response from a dict
api_v1_demands_post402_response_from_dict = ApiV1DemandsPost402Response.from_dict(api_v1_demands_post402_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


