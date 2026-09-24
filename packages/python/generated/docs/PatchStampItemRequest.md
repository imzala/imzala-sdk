# PatchStampItemRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**stamp_data** | [**StampData**](StampData.md) |  | 
**document_id** | **str** | Verilirse kaşe bu belgede olmalıdır (çok belgeli zarf). | [optional] 

## Example

```python
from imzala_client.models.patch_stamp_item_request import PatchStampItemRequest

# TODO update the JSON string below
json = "{}"
# create an instance of PatchStampItemRequest from a JSON string
patch_stamp_item_request_instance = PatchStampItemRequest.from_json(json)
# print the JSON string representation of the object
print(PatchStampItemRequest.to_json())

# convert the object into a dict
patch_stamp_item_request_dict = patch_stamp_item_request_instance.to_dict()
# create an instance of PatchStampItemRequest from a dict
patch_stamp_item_request_from_dict = PatchStampItemRequest.from_dict(patch_stamp_item_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


