# TemplateUsageTemplate


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**name** | **str** |  | [optional] 
**description** | **str** |  | [optional] 
**category** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.template_usage_template import TemplateUsageTemplate

# TODO update the JSON string below
json = "{}"
# create an instance of TemplateUsageTemplate from a JSON string
template_usage_template_instance = TemplateUsageTemplate.from_json(json)
# print the JSON string representation of the object
print(TemplateUsageTemplate.to_json())

# convert the object into a dict
template_usage_template_dict = template_usage_template_instance.to_dict()
# create an instance of TemplateUsageTemplate from a dict
template_usage_template_from_dict = TemplateUsageTemplate.from_dict(template_usage_template_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


