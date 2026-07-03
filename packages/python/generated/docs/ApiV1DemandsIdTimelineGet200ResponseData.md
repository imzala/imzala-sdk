# ApiV1DemandsIdTimelineGet200ResponseData


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**events** | [**List[ApiV1DemandsIdTimelineGet200ResponseDataEventsInner]**](ApiV1DemandsIdTimelineGet200ResponseDataEventsInner.md) |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_id_timeline_get200_response_data import ApiV1DemandsIdTimelineGet200ResponseData

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsIdTimelineGet200ResponseData from a JSON string
api_v1_demands_id_timeline_get200_response_data_instance = ApiV1DemandsIdTimelineGet200ResponseData.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsIdTimelineGet200ResponseData.to_json())

# convert the object into a dict
api_v1_demands_id_timeline_get200_response_data_dict = api_v1_demands_id_timeline_get200_response_data_instance.to_dict()
# create an instance of ApiV1DemandsIdTimelineGet200ResponseData from a dict
api_v1_demands_id_timeline_get200_response_data_from_dict = ApiV1DemandsIdTimelineGet200ResponseData.from_dict(api_v1_demands_id_timeline_get200_response_data_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


