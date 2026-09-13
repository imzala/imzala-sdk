# FieldLayoutPreviewPlacementsSummary


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**total** | **int** |  | [optional] 
**by_item_type** | **Dict[str, int]** |  | [optional] 
**by_page** | [**List[FieldLayoutPreviewPlacementsSummaryByPageInner]**](FieldLayoutPreviewPlacementsSummaryByPageInner.md) |  | [optional] 

## Example

```python
from imzala_client.models.field_layout_preview_placements_summary import FieldLayoutPreviewPlacementsSummary

# TODO update the JSON string below
json = "{}"
# create an instance of FieldLayoutPreviewPlacementsSummary from a JSON string
field_layout_preview_placements_summary_instance = FieldLayoutPreviewPlacementsSummary.from_json(json)
# print the JSON string representation of the object
print(FieldLayoutPreviewPlacementsSummary.to_json())

# convert the object into a dict
field_layout_preview_placements_summary_dict = field_layout_preview_placements_summary_instance.to_dict()
# create an instance of FieldLayoutPreviewPlacementsSummary from a dict
field_layout_preview_placements_summary_from_dict = FieldLayoutPreviewPlacementsSummary.from_dict(field_layout_preview_placements_summary_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


