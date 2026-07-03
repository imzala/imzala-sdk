# ApiV1DemandsIdTimelineGet200ResponseDataEventsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** |  | [optional] 
**event_type** | **str** |  | [optional] 
**actor_label** | **str** |  | [optional] 
**ip_masked** | **str** |  | [optional] 
**device_label** | **str** |  | [optional] 
**comment_text** | **str** |  | [optional] 
**created_at** | **datetime** |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_id_timeline_get200_response_data_events_inner import ApiV1DemandsIdTimelineGet200ResponseDataEventsInner

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsIdTimelineGet200ResponseDataEventsInner from a JSON string
api_v1_demands_id_timeline_get200_response_data_events_inner_instance = ApiV1DemandsIdTimelineGet200ResponseDataEventsInner.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsIdTimelineGet200ResponseDataEventsInner.to_json())

# convert the object into a dict
api_v1_demands_id_timeline_get200_response_data_events_inner_dict = api_v1_demands_id_timeline_get200_response_data_events_inner_instance.to_dict()
# create an instance of ApiV1DemandsIdTimelineGet200ResponseDataEventsInner from a dict
api_v1_demands_id_timeline_get200_response_data_events_inner_from_dict = ApiV1DemandsIdTimelineGet200ResponseDataEventsInner.from_dict(api_v1_demands_id_timeline_get200_response_data_events_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


