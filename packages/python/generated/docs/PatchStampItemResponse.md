# PatchStampItemResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**data** | [**PatchStampItemResponseData**](PatchStampItemResponseData.md) |  | [optional] 

## Example

```python
from imzala_client.models.patch_stamp_item_response import PatchStampItemResponse

# TODO update the JSON string below
json = "{}"
# create an instance of PatchStampItemResponse from a JSON string
patch_stamp_item_response_instance = PatchStampItemResponse.from_json(json)
# print the JSON string representation of the object
print(PatchStampItemResponse.to_json())

# convert the object into a dict
patch_stamp_item_response_dict = patch_stamp_item_response_instance.to_dict()
# create an instance of PatchStampItemResponse from a dict
patch_stamp_item_response_from_dict = PatchStampItemResponse.from_dict(patch_stamp_item_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


