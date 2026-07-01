# UpsertItemsResponseDataItemsInner

Yaratılan AgreementPageItem snapshot'ı

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**page_id** | **int** |  | [optional] 
**party_id** | **UUID** |  | [optional] 
**item_type** | **str** |  | [optional] 
**position_x** | **float** |  | [optional] 
**position_y** | **float** |  | [optional] 
**width** | **float** |  | [optional] 
**height** | **float** |  | [optional] 
**is_required** | **bool** |  | [optional] 
**slug** | **str** |  | [optional] 
**label** | **str** |  | [optional] 
**config** | **object** |  | [optional] 

## Example

```python
from imzala_client.models.upsert_items_response_data_items_inner import UpsertItemsResponseDataItemsInner

# TODO update the JSON string below
json = "{}"
# create an instance of UpsertItemsResponseDataItemsInner from a JSON string
upsert_items_response_data_items_inner_instance = UpsertItemsResponseDataItemsInner.from_json(json)
# print the JSON string representation of the object
print(UpsertItemsResponseDataItemsInner.to_json())

# convert the object into a dict
upsert_items_response_data_items_inner_dict = upsert_items_response_data_items_inner_instance.to_dict()
# create an instance of UpsertItemsResponseDataItemsInner from a dict
upsert_items_response_data_items_inner_from_dict = UpsertItemsResponseDataItemsInner.from_dict(upsert_items_response_data_items_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


