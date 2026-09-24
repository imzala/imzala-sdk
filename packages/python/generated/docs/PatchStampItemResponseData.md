# PatchStampItemResponseData


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**item_id** | **int** |  | [optional] 
**page_id** | **int** |  | [optional] 
**document_id** | **str** |  | [optional] 
**party_id** | **UUID** |  | [optional] 
**source** | **str** | Güncelleme sonrası kaynak (FROM_SAVED kaşe INLINE olur) | [optional] 
**is_required** | **bool** |  | [optional] 
**stamp_data** | **Dict[str, object]** | Güncelleme sonrası kaşe verisinin tamamı. | [optional] 

## Example

```python
from imzala_client.models.patch_stamp_item_response_data import PatchStampItemResponseData

# TODO update the JSON string below
json = "{}"
# create an instance of PatchStampItemResponseData from a JSON string
patch_stamp_item_response_data_instance = PatchStampItemResponseData.from_json(json)
# print the JSON string representation of the object
print(PatchStampItemResponseData.to_json())

# convert the object into a dict
patch_stamp_item_response_data_dict = patch_stamp_item_response_data_instance.to_dict()
# create an instance of PatchStampItemResponseData from a dict
patch_stamp_item_response_data_from_dict = PatchStampItemResponseData.from_dict(patch_stamp_item_response_data_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


