# TemplateDocumentSummary

Şablonun zarf belgesi. `id` alanı `POST /api/v1/demands` ve `POST /api/v1/demands/bulk` isteklerindeki `documents.include` / `documents.exclude` listelerinde kullanılır. Şablon kopyalandığında (panelden \"Kopyala\" ya da sözleşmeden şablon oluşturma) kopyanın belge kimlikleri YENİDİR; entegrasyonunuz kopyanın kimliklerini bu uçtan yeniden okumalıdır. Başlık, tür ve ayar değişiklikleri ile yeniden sıralama kimliği değiştirmez. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**order** | **int** | Şablondaki sıra (1&#39;den başlar) | [optional] 
**title** | **str** |  | [optional] 
**doc_kind** | **str** |  | [optional] 
**is_required** | **bool** | İmzacının bu belgeyi atlayıp atlayamayacağı. Gönderilecek belgeleri SEÇMEKLE ilgisi yoktur: zorunlu belge de &#x60;documents.exclude&#x60; ile bu imza sürecinden çıkarılabilir. Mevzuat gereği karşı tarafa verilmesi gereken bir belgeyi (örneğin &#x60;doc_kind: PREINFO&#x60; ön bilgilendirme formu veya &#x60;KVKK_NOTICE&#x60; aydınlatma metni) başka bir kanaldan vermiyorsanız çıkarmayın; bu belgelerin verilmesi yükümlülüğü sözleşmeyi gönderene aittir.  | [optional] 
**signature_required** | **bool** |  | [optional] 
**default_included** | **bool** | İstek &#x60;documents&#x60; belirtmezse bu belge imza sürecine (zarfa) dahil edilir mi. | [optional] 
**assigned_template_party_ids** | **List[UUID]** | Bu belgeyi imzalayacak şablon rollerinin kimlikleri (&#x60;parties[].id&#x60; ve &#x60;party_mapping[].template_party_id&#x60; ile aynı küme). Sözleşme uçlarındaki &#x60;assigned_party_ids&#x60; (imzacı kimliği) ile karıştırmayın.  | [optional] 

## Example

```python
from imzala_client.models.template_document_summary import TemplateDocumentSummary

# TODO update the JSON string below
json = "{}"
# create an instance of TemplateDocumentSummary from a JSON string
template_document_summary_instance = TemplateDocumentSummary.from_json(json)
# print the JSON string representation of the object
print(TemplateDocumentSummary.to_json())

# convert the object into a dict
template_document_summary_dict = template_document_summary_instance.to_dict()
# create an instance of TemplateDocumentSummary from a dict
template_document_summary_from_dict = TemplateDocumentSummary.from_dict(template_document_summary_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


