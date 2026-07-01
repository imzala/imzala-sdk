# TemplateDetail


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**name** | **str** |  | [optional] 
**description** | **str** |  | [optional] 
**category** | **str** |  | [optional] 
**usage_count** | **int** |  | [optional] 
**parties** | [**List[TemplatePartySummary]**](TemplatePartySummary.md) |  | [optional] 
**pages_count** | **int** |  | [optional] 
**variables** | [**List[TemplateVariable]**](TemplateVariable.md) |  | [optional] 

## Example

```python
from imzala_client.models.template_detail import TemplateDetail

# TODO update the JSON string below
json = "{}"
# create an instance of TemplateDetail from a JSON string
template_detail_instance = TemplateDetail.from_json(json)
# print the JSON string representation of the object
print(TemplateDetail.to_json())

# convert the object into a dict
template_detail_dict = template_detail_instance.to_dict()
# create an instance of TemplateDetail from a dict
template_detail_from_dict = TemplateDetail.from_dict(template_detail_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


