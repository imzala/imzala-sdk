# TemplateSummaryPartiesInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**order** | **int** |  | [optional] 
**label** | **str** |  | [optional] 
**is_required** | **bool** |  | [optional] 

## Example

```python
from imzala_client.models.template_summary_parties_inner import TemplateSummaryPartiesInner

# TODO update the JSON string below
json = "{}"
# create an instance of TemplateSummaryPartiesInner from a JSON string
template_summary_parties_inner_instance = TemplateSummaryPartiesInner.from_json(json)
# print the JSON string representation of the object
print(TemplateSummaryPartiesInner.to_json())

# convert the object into a dict
template_summary_parties_inner_dict = template_summary_parties_inner_instance.to_dict()
# create an instance of TemplateSummaryPartiesInner from a dict
template_summary_parties_inner_from_dict = TemplateSummaryPartiesInner.from_dict(template_summary_parties_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


