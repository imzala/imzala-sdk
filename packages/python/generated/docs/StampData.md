# StampData

Yapılandırılmış kaşe verisi. Tüm alanlar isteğe bağlıdır. PATCH isteğinde `null` veya boş string alanı kaldırır.  Belgeye yalnız gerekli kişisel veriyi yazın; bu verilerin hukuka uygunluğu ve aydınlatması sizin sorumluluğunuzdadır. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**company_name** | **str** |  | [optional] 
**personal_name** | **str** |  | [optional] 
**address** | **str** |  | [optional] 
**company_address** | **str** |  | [optional] 
**personal_address** | **str** |  | [optional] 
**tax_number** | **str** |  | [optional] 
**tax_office** | **str** |  | [optional] 
**id_number** | **str** |  | [optional] 
**phone** | **str** |  | [optional] 
**company_phone** | **str** |  | [optional] 
**personal_phone** | **str** |  | [optional] 
**email** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.stamp_data import StampData

# TODO update the JSON string below
json = "{}"
# create an instance of StampData from a JSON string
stamp_data_instance = StampData.from_json(json)
# print the JSON string representation of the object
print(StampData.to_json())

# convert the object into a dict
stamp_data_dict = stamp_data_instance.to_dict()
# create an instance of StampData from a dict
stamp_data_from_dict = StampData.from_dict(stamp_data_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


