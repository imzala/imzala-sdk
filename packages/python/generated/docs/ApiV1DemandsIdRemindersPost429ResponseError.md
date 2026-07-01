# ApiV1DemandsIdRemindersPost429ResponseError


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**code** | **str** |  | [optional] 
**message** | **str** |  | [optional] 
**retry_after_seconds** | **int** | Sadece RATE_LIMITED&#39;de | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_id_reminders_post429_response_error import ApiV1DemandsIdRemindersPost429ResponseError

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsIdRemindersPost429ResponseError from a JSON string
api_v1_demands_id_reminders_post429_response_error_instance = ApiV1DemandsIdRemindersPost429ResponseError.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsIdRemindersPost429ResponseError.to_json())

# convert the object into a dict
api_v1_demands_id_reminders_post429_response_error_dict = api_v1_demands_id_reminders_post429_response_error_instance.to_dict()
# create an instance of ApiV1DemandsIdRemindersPost429ResponseError from a dict
api_v1_demands_id_reminders_post429_response_error_from_dict = ApiV1DemandsIdRemindersPost429ResponseError.from_dict(api_v1_demands_id_reminders_post429_response_error_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


