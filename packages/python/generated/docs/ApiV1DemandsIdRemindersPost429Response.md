# ApiV1DemandsIdRemindersPost429Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**error** | [**ApiV1DemandsIdRemindersPost429ResponseError**](ApiV1DemandsIdRemindersPost429ResponseError.md) |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_id_reminders_post429_response import ApiV1DemandsIdRemindersPost429Response

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsIdRemindersPost429Response from a JSON string
api_v1_demands_id_reminders_post429_response_instance = ApiV1DemandsIdRemindersPost429Response.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsIdRemindersPost429Response.to_json())

# convert the object into a dict
api_v1_demands_id_reminders_post429_response_dict = api_v1_demands_id_reminders_post429_response_instance.to_dict()
# create an instance of ApiV1DemandsIdRemindersPost429Response from a dict
api_v1_demands_id_reminders_post429_response_from_dict = ApiV1DemandsIdRemindersPost429Response.from_dict(api_v1_demands_id_reminders_post429_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


