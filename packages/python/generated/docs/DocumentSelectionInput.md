# DocumentSelectionInput

Bu istekte gönderilecek belgeleri şablon varsayılanına göre değiştirir. Alan hiç gönderilmezse `default_included: true` olan belgeler gider. `include` varsayılanı kapalı belgeyi ekler, `exclude` varsayılanı açık belgeyi (zorunlu olsa bile) çıkarır. Çıkarılan belge bu imza sürecine (zarfa) eklenmez: imzacıya gösterilmez, imzalı PDF'te ve tamamlanma sertifikasında yer almaz, kredi hesaplamasına dahil edilmez. Sonuçta en az bir belge kalmalıdır ve eşlediğiniz her role en az bir seçili belge düşmelidir. Mevzuat gereği verilmesi gereken belgeleri çıkarmadan önce `is_required` açıklamasına bakın; bu belgelerin verilmesi yükümlülüğü sözleşmeyi gönderene aittir. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**include** | **List[UUID]** | Şablon belge kimlikleri (&#x60;GET /api/v1/templates/{id}&#x60; → &#x60;documents[].id&#x60;). | [optional] 
**exclude** | **List[UUID]** |  | [optional] 

## Example

```python
from imzala_client.models.document_selection_input import DocumentSelectionInput

# TODO update the JSON string below
json = "{}"
# create an instance of DocumentSelectionInput from a JSON string
document_selection_input_instance = DocumentSelectionInput.from_json(json)
# print the JSON string representation of the object
print(DocumentSelectionInput.to_json())

# convert the object into a dict
document_selection_input_dict = document_selection_input_instance.to_dict()
# create an instance of DocumentSelectionInput from a dict
document_selection_input_from_dict = DocumentSelectionInput.from_dict(document_selection_input_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


