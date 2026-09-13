# FieldLayoutPreview

Kuru koşum sonucu — hiçbir yan etki üretilmemiştir.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_id** | **UUID** |  | [optional] 
**page_count** | **int** | Yüklenen belgenin sayfa sayısı. | [optional] 
**resolvable** | **bool** | &#x60;true&#x60; ise aynı belge + aynı şablon ile &#x60;POST /api/v1/demands/upload&#x60; çağrısı alan yerleşimi nedeniyle reddedilmez.  | [optional] 
**on_anchor_miss** | **str** | Uygulanan davranış (istenen değil). | [optional] 
**placements_summary** | [**FieldLayoutPreviewPlacementsSummary**](FieldLayoutPreviewPlacementsSummary.md) |  | [optional] 
**diagnostics** | [**List[FieldLayoutDiagnostic]**](FieldLayoutDiagnostic.md) |  | [optional] 
**warnings** | [**List[FieldLayoutWarning]**](FieldLayoutWarning.md) |  | [optional] 

## Example

```python
from imzala_client.models.field_layout_preview import FieldLayoutPreview

# TODO update the JSON string below
json = "{}"
# create an instance of FieldLayoutPreview from a JSON string
field_layout_preview_instance = FieldLayoutPreview.from_json(json)
# print the JSON string representation of the object
print(FieldLayoutPreview.to_json())

# convert the object into a dict
field_layout_preview_dict = field_layout_preview_instance.to_dict()
# create an instance of FieldLayoutPreview from a dict
field_layout_preview_from_dict = FieldLayoutPreview.from_dict(field_layout_preview_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


