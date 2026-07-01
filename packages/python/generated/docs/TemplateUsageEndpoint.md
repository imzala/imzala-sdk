# TemplateUsageEndpoint


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**method** | **str** |  | [optional] 
**url** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.template_usage_endpoint import TemplateUsageEndpoint

# TODO update the JSON string below
json = "{}"
# create an instance of TemplateUsageEndpoint from a JSON string
template_usage_endpoint_instance = TemplateUsageEndpoint.from_json(json)
# print the JSON string representation of the object
print(TemplateUsageEndpoint.to_json())

# convert the object into a dict
template_usage_endpoint_dict = template_usage_endpoint_instance.to_dict()
# create an instance of TemplateUsageEndpoint from a dict
template_usage_endpoint_from_dict = TemplateUsageEndpoint.from_dict(template_usage_endpoint_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


