# DemandStatusStampItemsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**item_id** | **int** |  | [optional] 
**page_id** | **int** |  | [optional] 
**document_id** | **str** |  | [optional] 
**slug** | **str** |  | [optional] 
**label** | **str** |  | [optional] 
**source** | **str** |  | [optional] 
**party_id** | **UUID** | Yalnız FILLER_PROVIDES kaşede kaşeyi dolduracak taraf | [optional] 
**is_required** | **bool** |  | [optional] 

## Example

```python
from imzala_client.models.demand_status_stamp_items_inner import DemandStatusStampItemsInner

# TODO update the JSON string below
json = "{}"
# create an instance of DemandStatusStampItemsInner from a JSON string
demand_status_stamp_items_inner_instance = DemandStatusStampItemsInner.from_json(json)
# print the JSON string representation of the object
print(DemandStatusStampItemsInner.to_json())

# convert the object into a dict
demand_status_stamp_items_inner_dict = demand_status_stamp_items_inner_instance.to_dict()
# create an instance of DemandStatusStampItemsInner from a dict
demand_status_stamp_items_inner_from_dict = DemandStatusStampItemsInner.from_dict(demand_status_stamp_items_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


