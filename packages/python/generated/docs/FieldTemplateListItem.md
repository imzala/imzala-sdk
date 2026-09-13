# FieldTemplateListItem


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**name** | **str** |  | [optional] 
**description** | **str** |  | [optional] 
**category** | **str** |  | [optional] 
**usage_count** | **int** |  | [optional] 
**parties** | [**List[FieldTemplateParty]**](FieldTemplateParty.md) |  | [optional] 

## Example

```python
from imzala_client.models.field_template_list_item import FieldTemplateListItem

# TODO update the JSON string below
json = "{}"
# create an instance of FieldTemplateListItem from a JSON string
field_template_list_item_instance = FieldTemplateListItem.from_json(json)
# print the JSON string representation of the object
print(FieldTemplateListItem.to_json())

# convert the object into a dict
field_template_list_item_dict = field_template_list_item_instance.to_dict()
# create an instance of FieldTemplateListItem from a dict
field_template_list_item_from_dict = FieldTemplateListItem.from_dict(field_template_list_item_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


