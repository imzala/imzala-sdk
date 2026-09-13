# TemplateUsage


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**kind** | **str** | Rehberin anlattığı akış. Bu uç yalnız belgeli şablonu çözer, dolayısıyla burada daima &#x60;DOCUMENT&#x60; döner (Alan Şablonu bu uçta 404&#39;tür; kimliği &#x60;GET /api/v1/field-templates&#x60; ile öğrenilir ve &#x60;POST /api/v1/demands/upload&#x60; ile kullanılır).  | [optional] 
**form_fields** | [**List[TemplateUsageFormFieldsInner]**](TemplateUsageFormFieldsInner.md) | &#x60;multipart/form-data&#x60; alanları. Belgeli şablon JSON gövde kullandığı için bu uçta boş dizidir.  | [optional] 
**template** | [**ApiV1TemplatesIdPatch200ResponseData**](ApiV1TemplatesIdPatch200ResponseData.md) |  | [optional] 
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


