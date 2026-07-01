# UpsertItemsResponseData


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**items** | [**List[UpsertItemsResponseDataItemsInner]**](UpsertItemsResponseDataItemsInner.md) |  | [optional] 
**items_count** | **int** |  | [optional] 

## Example

```python
from imzala_client.models.upsert_items_response_data import UpsertItemsResponseData

# TODO update the JSON string below
json = "{}"
# create an instance of UpsertItemsResponseData from a JSON string
upsert_items_response_data_instance = UpsertItemsResponseData.from_json(json)
# print the JSON string representation of the object
print(UpsertItemsResponseData.to_json())

# convert the object into a dict
upsert_items_response_data_dict = upsert_items_response_data_instance.to_dict()
# create an instance of UpsertItemsResponseData from a dict
upsert_items_response_data_from_dict = UpsertItemsResponseData.from_dict(upsert_items_response_data_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


