# UpsertItemsRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**items** | [**List[PageItem]**](PageItem.md) |  | 
**page_ids** | **List[int]** | **Opsiyonel.** Verilirse sadece bu sayfaların item&#39;ları replace edilir; diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60; değerleri bu listede olmalıdır. Omitted ise tüm sayfaların item&#39;ları replace edilir.  | [optional] 

## Example

```python
from imzala_client.models.upsert_items_request import UpsertItemsRequest

# TODO update the JSON string below
json = "{}"
# create an instance of UpsertItemsRequest from a JSON string
upsert_items_request_instance = UpsertItemsRequest.from_json(json)
# print the JSON string representation of the object
print(UpsertItemsRequest.to_json())

# convert the object into a dict
upsert_items_request_dict = upsert_items_request_instance.to_dict()
# create an instance of UpsertItemsRequest from a dict
upsert_items_request_from_dict = UpsertItemsRequest.from_dict(upsert_items_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


