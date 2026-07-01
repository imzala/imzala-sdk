# TemplateUsagePartiesInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_party_id** | **UUID** |  | [optional] 
**order** | **int** |  | [optional] 
**label** | **str** |  | [optional] 
**is_required** | **bool** |  | [optional] 
**supported_fields** | [**List[TemplateUsagePartiesInnerSupportedFieldsInner]**](TemplateUsagePartiesInnerSupportedFieldsInner.md) |  | [optional] 

## Example

```python
from imzala_client.models.template_usage_parties_inner import TemplateUsagePartiesInner

# TODO update the JSON string below
json = "{}"
# create an instance of TemplateUsagePartiesInner from a JSON string
template_usage_parties_inner_instance = TemplateUsagePartiesInner.from_json(json)
# print the JSON string representation of the object
print(TemplateUsagePartiesInner.to_json())

# convert the object into a dict
template_usage_parties_inner_dict = template_usage_parties_inner_instance.to_dict()
# create an instance of TemplateUsagePartiesInner from a dict
template_usage_parties_inner_from_dict = TemplateUsagePartiesInner.from_dict(template_usage_parties_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


