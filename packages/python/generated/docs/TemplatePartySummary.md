# TemplatePartySummary


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**order** | **int** |  | [optional] 
**label** | **str** |  | [optional] 
**is_required** | **bool** |  | [optional] 

## Example

```python
from imzala_client.models.template_party_summary import TemplatePartySummary

# TODO update the JSON string below
json = "{}"
# create an instance of TemplatePartySummary from a JSON string
template_party_summary_instance = TemplatePartySummary.from_json(json)
# print the JSON string representation of the object
print(TemplatePartySummary.to_json())

# convert the object into a dict
template_party_summary_dict = template_party_summary_instance.to_dict()
# create an instance of TemplatePartySummary from a dict
template_party_summary_from_dict = TemplatePartySummary.from_dict(template_party_summary_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


