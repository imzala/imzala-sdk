# TemplateUsageFormFieldsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**key** | **str** |  | [optional] 
**type** | **str** |  | [optional] 
**required** | **bool** |  | [optional] 
**note** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.template_usage_form_fields_inner import TemplateUsageFormFieldsInner

# TODO update the JSON string below
json = "{}"
# create an instance of TemplateUsageFormFieldsInner from a JSON string
template_usage_form_fields_inner_instance = TemplateUsageFormFieldsInner.from_json(json)
# print the JSON string representation of the object
print(TemplateUsageFormFieldsInner.to_json())

# convert the object into a dict
template_usage_form_fields_inner_dict = template_usage_form_fields_inner_instance.to_dict()
# create an instance of TemplateUsageFormFieldsInner from a dict
template_usage_form_fields_inner_from_dict = TemplateUsageFormFieldsInner.from_dict(template_usage_form_fields_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


