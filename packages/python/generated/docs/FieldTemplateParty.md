# FieldTemplateParty

Alan Şablonundaki tek rol. `order`, `POST /api/v1/demands/upload` gövdesindeki `parties` dizisinin sırasıdır. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**order** | **int** |  | [optional] 
**label** | **str** |  | [optional] 
**is_required** | **bool** |  | [optional] 

## Example

```python
from imzala_client.models.field_template_party import FieldTemplateParty

# TODO update the JSON string below
json = "{}"
# create an instance of FieldTemplateParty from a JSON string
field_template_party_instance = FieldTemplateParty.from_json(json)
# print the JSON string representation of the object
print(FieldTemplateParty.to_json())

# convert the object into a dict
field_template_party_dict = field_template_party_instance.to_dict()
# create an instance of FieldTemplateParty from a dict
field_template_party_from_dict = FieldTemplateParty.from_dict(field_template_party_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


