# TemplateUsageVariablesInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**slug** | **str** |  | [optional] 
**label** | **str** |  | [optional] 
**item_type** | **str** |  | [optional] 
**is_required** | **bool** |  | [optional] 
**default_source** | **str** |  | [optional] 
**auto_filled** | **bool** |  | [optional] 
**template_party_id** | **str** |  | [optional] 
**note** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.template_usage_variables_inner import TemplateUsageVariablesInner

# TODO update the JSON string below
json = "{}"
# create an instance of TemplateUsageVariablesInner from a JSON string
template_usage_variables_inner_instance = TemplateUsageVariablesInner.from_json(json)
# print the JSON string representation of the object
print(TemplateUsageVariablesInner.to_json())

# convert the object into a dict
template_usage_variables_inner_dict = template_usage_variables_inner_instance.to_dict()
# create an instance of TemplateUsageVariablesInner from a dict
template_usage_variables_inner_from_dict = TemplateUsageVariablesInner.from_dict(template_usage_variables_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


