# UpsertItemsResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**data** | [**UpsertItemsResponseData**](UpsertItemsResponseData.md) |  | [optional] 

## Example

```python
from imzala_client.models.upsert_items_response import UpsertItemsResponse

# TODO update the JSON string below
json = "{}"
# create an instance of UpsertItemsResponse from a JSON string
upsert_items_response_instance = UpsertItemsResponse.from_json(json)
# print the JSON string representation of the object
print(UpsertItemsResponse.to_json())

# convert the object into a dict
upsert_items_response_dict = upsert_items_response_instance.to_dict()
# create an instance of UpsertItemsResponse from a dict
upsert_items_response_from_dict = UpsertItemsResponse.from_dict(upsert_items_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


