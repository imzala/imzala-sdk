# TemplateUsageExampleRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**curl** | **str** | curl komutu, slug&#39;lar dolu | [optional] 
**var_json** | **object** | JSON payload, multi-party-aware (party_mapping[].variables + root variables) | [optional] 

## Example

```python
from imzala_client.models.template_usage_example_request import TemplateUsageExampleRequest

# TODO update the JSON string below
json = "{}"
# create an instance of TemplateUsageExampleRequest from a JSON string
template_usage_example_request_instance = TemplateUsageExampleRequest.from_json(json)
# print the JSON string representation of the object
print(TemplateUsageExampleRequest.to_json())

# convert the object into a dict
template_usage_example_request_dict = template_usage_example_request_instance.to_dict()
# create an instance of TemplateUsageExampleRequest from a dict
template_usage_example_request_from_dict = TemplateUsageExampleRequest.from_dict(template_usage_example_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


