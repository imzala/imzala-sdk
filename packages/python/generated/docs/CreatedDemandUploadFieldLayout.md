# CreatedDemandUploadFieldLayout

YALNIZ `field_template_id` gönderildiğinde döner. Alan yerleşiminin uygulanma özeti. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_id** | **UUID** |  | [optional] 
**applied_fields** | **int** | Sözleşmeye yazılan alan sayısı. | [optional] 
**on_anchor_miss** | **str** | İstenen değil, **uygulanan** davranış. &#x60;drop&#x60; istenip uygulanamadıysa burada &#x60;block&#x60; görürsünüz ve &#x60;warnings&#x60; içinde &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; bulunur.  | [optional] 
**warnings** | [**List[FieldLayoutWarning]**](FieldLayoutWarning.md) |  | [optional] 
**diagnostics** | [**List[FieldLayoutDiagnostic]**](FieldLayoutDiagnostic.md) | Yalnız &#x60;WARNING&#x60; seviyesindeki tanılar. &#x60;ERROR&#x60; seviyesinde bir tanı olsaydı istek 422 ile reddedilir ve sözleşme yaratılmazdı.  | [optional] 

## Example

```python
from imzala_client.models.created_demand_upload_field_layout import CreatedDemandUploadFieldLayout

# TODO update the JSON string below
json = "{}"
# create an instance of CreatedDemandUploadFieldLayout from a JSON string
created_demand_upload_field_layout_instance = CreatedDemandUploadFieldLayout.from_json(json)
# print the JSON string representation of the object
print(CreatedDemandUploadFieldLayout.to_json())

# convert the object into a dict
created_demand_upload_field_layout_dict = created_demand_upload_field_layout_instance.to_dict()
# create an instance of CreatedDemandUploadFieldLayout from a dict
created_demand_upload_field_layout_from_dict = CreatedDemandUploadFieldLayout.from_dict(created_demand_upload_field_layout_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


