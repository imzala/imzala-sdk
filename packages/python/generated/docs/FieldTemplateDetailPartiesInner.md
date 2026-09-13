# FieldTemplateDetailPartiesInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**order** | **int** |  | [optional] 
**label** | **str** |  | [optional] 
**is_required** | **bool** |  | [optional] 
**field_count** | **int** | Bu rolün dolduracağı alan sayısı. | [optional] 

## Example

```python
from imzala_client.models.field_template_detail_parties_inner import FieldTemplateDetailPartiesInner

# TODO update the JSON string below
json = "{}"
# create an instance of FieldTemplateDetailPartiesInner from a JSON string
field_template_detail_parties_inner_instance = FieldTemplateDetailPartiesInner.from_json(json)
# print the JSON string representation of the object
print(FieldTemplateDetailPartiesInner.to_json())

# convert the object into a dict
field_template_detail_parties_inner_dict = field_template_detail_parties_inner_instance.to_dict()
# create an instance of FieldTemplateDetailPartiesInner from a dict
field_template_detail_parties_inner_from_dict = FieldTemplateDetailPartiesInner.from_dict(field_template_detail_parties_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


