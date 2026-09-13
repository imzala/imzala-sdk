# FieldTemplateDetail

Alan Şablonu ayrıntısı. Alan koordinatları TAŞIMAZ; yerleşimin bir belgeye nasıl uygulanacağı için kuru koşum ucunu kullanın. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**name** | **str** |  | [optional] 
**description** | **str** |  | [optional] 
**category** | **str** |  | [optional] 
**total_field_count** | **int** | Şablondaki toplam alan sayısı. | [optional] 
**parties** | [**List[FieldTemplateDetailPartiesInner]**](FieldTemplateDetailPartiesInner.md) |  | [optional] 

## Example

```python
from imzala_client.models.field_template_detail import FieldTemplateDetail

# TODO update the JSON string below
json = "{}"
# create an instance of FieldTemplateDetail from a JSON string
field_template_detail_instance = FieldTemplateDetail.from_json(json)
# print the JSON string representation of the object
print(FieldTemplateDetail.to_json())

# convert the object into a dict
field_template_detail_dict = field_template_detail_instance.to_dict()
# create an instance of FieldTemplateDetail from a dict
field_template_detail_from_dict = FieldTemplateDetail.from_dict(field_template_detail_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


