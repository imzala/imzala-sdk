# FieldLayoutDiagnostic

Alan yerleşimi tanısı. `code` makine sözleşmesidir, `message` insan metnidir (TR). **Bilinmeyen `code` toleranslı okunmalıdır**: yeni kod eklemek kırıcı değişiklik sayılmaz, istemci tanımadığı kodda `message` alanına düşmelidir.  Belgenizden çıkarılan hiçbir metin (çapa metni, eşleşen satır, sayfa metni, dosya adı) bu yapıya girmez. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**code** | **str** |  | 
**severity** | **str** |  | 
**item_id** | **int** | Şablon alanının kimliği. &#x60;-1&#x60; belge/taraf düzeyindeki tanılar için kullanılan sentinel değerdir.  | 
**slug** | **str** |  | [optional] 
**label** | **str** |  | [optional] 
**item_type** | **str** |  | 
**page** | **int** | 1 tabanlı sayfa numarası; bilinmiyorsa null. | [optional] 
**message** | **str** |  | 
**detail** | **Dict[str, object]** | Koda göre değişen, kapalı bir anahtar kümesi (yalnız sayısal değerler ve alan kimlikleri).  | [optional] 

## Example

```python
from imzala_client.models.field_layout_diagnostic import FieldLayoutDiagnostic

# TODO update the JSON string below
json = "{}"
# create an instance of FieldLayoutDiagnostic from a JSON string
field_layout_diagnostic_instance = FieldLayoutDiagnostic.from_json(json)
# print the JSON string representation of the object
print(FieldLayoutDiagnostic.to_json())

# convert the object into a dict
field_layout_diagnostic_dict = field_layout_diagnostic_instance.to_dict()
# create an instance of FieldLayoutDiagnostic from a dict
field_layout_diagnostic_from_dict = FieldLayoutDiagnostic.from_dict(field_layout_diagnostic_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


