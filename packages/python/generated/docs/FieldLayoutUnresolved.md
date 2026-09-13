# FieldLayoutUnresolved

`field_template_id` ile gönderilen bir istekte alan yerleşimi çözülemediğinde dönen gövde. **Sözleşme oluşturulmaz, kredi düşülmez.** 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**error** | **str** |  | [optional] 
**message** | **str** |  | [optional] 
**diagnostics** | [**List[FieldLayoutDiagnostic]**](FieldLayoutDiagnostic.md) |  | [optional] 
**warnings** | [**List[FieldLayoutWarning]**](FieldLayoutWarning.md) |  | [optional] 

## Example

```python
from imzala_client.models.field_layout_unresolved import FieldLayoutUnresolved

# TODO update the JSON string below
json = "{}"
# create an instance of FieldLayoutUnresolved from a JSON string
field_layout_unresolved_instance = FieldLayoutUnresolved.from_json(json)
# print the JSON string representation of the object
print(FieldLayoutUnresolved.to_json())

# convert the object into a dict
field_layout_unresolved_dict = field_layout_unresolved_instance.to_dict()
# create an instance of FieldLayoutUnresolved from a dict
field_layout_unresolved_from_dict = FieldLayoutUnresolved.from_dict(field_layout_unresolved_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


