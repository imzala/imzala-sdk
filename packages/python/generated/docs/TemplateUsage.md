# TemplateUsage


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template** | [**TemplateUsageTemplate**](TemplateUsageTemplate.md) |  | [optional] 
**endpoint** | [**TemplateUsageEndpoint**](TemplateUsageEndpoint.md) |  | [optional] 
**required_headers** | **Dict[str, str]** |  | [optional] 
**parties** | [**List[TemplateUsagePartiesInner]**](TemplateUsagePartiesInner.md) |  | [optional] 
**variables** | [**List[TemplateUsageVariablesInner]**](TemplateUsageVariablesInner.md) |  | [optional] 
**example_request** | [**TemplateUsageExampleRequest**](TemplateUsageExampleRequest.md) |  | [optional] 

## Example

```python
from imzala_client.models.template_usage import TemplateUsage

# TODO update the JSON string below
json = "{}"
# create an instance of TemplateUsage from a JSON string
template_usage_instance = TemplateUsage.from_json(json)
# print the JSON string representation of the object
print(TemplateUsage.to_json())

# convert the object into a dict
template_usage_dict = template_usage_instance.to_dict()
# create an instance of TemplateUsage from a dict
template_usage_from_dict = TemplateUsage.from_dict(template_usage_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


