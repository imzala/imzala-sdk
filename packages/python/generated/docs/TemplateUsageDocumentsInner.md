# TemplateUsageDocumentsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_document_id** | **str** |  | [optional] 
**title** | **str** |  | [optional] 
**order** | **int** |  | [optional] 
**default_included** | **bool** | &#x60;documents&#x60; seçimi gönderilmezse bu belge sözleşmeye girer mi. | [optional] 

## Example

```python
from imzala_client.models.template_usage_documents_inner import TemplateUsageDocumentsInner

# TODO update the JSON string below
json = "{}"
# create an instance of TemplateUsageDocumentsInner from a JSON string
template_usage_documents_inner_instance = TemplateUsageDocumentsInner.from_json(json)
# print the JSON string representation of the object
print(TemplateUsageDocumentsInner.to_json())

# convert the object into a dict
template_usage_documents_inner_dict = template_usage_documents_inner_instance.to_dict()
# create an instance of TemplateUsageDocumentsInner from a dict
template_usage_documents_inner_from_dict = TemplateUsageDocumentsInner.from_dict(template_usage_documents_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


