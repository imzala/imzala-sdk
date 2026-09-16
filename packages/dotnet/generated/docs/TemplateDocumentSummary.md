# ImzalaApiClient.Model.TemplateDocumentSummary
Şablonun zarf belgesi. `id` alanı `POST /api/v1/demands` ve `POST /api/v1/demands/bulk` isteklerindeki `documents.include` / `documents.exclude` listelerinde kullanılır. Şablon kopyalandığında (panelden \"Kopyala\" ya da sözleşmeden şablon oluşturma) kopyanın belge kimlikleri YENİDİR; entegrasyonunuz kopyanın kimliklerini bu uçtan yeniden okumalıdır. Başlık, tür ve ayar değişiklikleri ile yeniden sıralama kimliği değiştirmez. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Id** | **Guid** |  | [optional] 
**Order** | **int** | Şablondaki sıra (1&#39;den başlar) | [optional] 
**Title** | **string** |  | [optional] 
**DocKind** | **string** |  | [optional] 
**IsRequired** | **bool** | İmzacının bu belgeyi atlayıp atlayamayacağı. Gönderilecek belgeleri SEÇMEKLE ilgisi yoktur: zorunlu belge de &#x60;documents.exclude&#x60; ile bu imza sürecinden çıkarılabilir. Mevzuat gereği karşı tarafa verilmesi gereken bir belgeyi (örneğin &#x60;doc_kind: PREINFO&#x60; ön bilgilendirme formu veya &#x60;KVKK_NOTICE&#x60; aydınlatma metni) başka bir kanaldan vermiyorsanız çıkarmayın; bu belgelerin verilmesi yükümlülüğü sözleşmeyi gönderene aittir.  | [optional] 
**SignatureRequired** | **bool** |  | [optional] 
**DefaultIncluded** | **bool** | İstek &#x60;documents&#x60; belirtmezse bu belge imza sürecine (zarfa) dahil edilir mi. | [optional] 
**AssignedTemplatePartyIds** | **List&lt;Guid&gt;** | Bu belgeyi imzalayacak şablon rollerinin kimlikleri (&#x60;parties[].id&#x60; ve &#x60;party_mapping[].template_party_id&#x60; ile aynı küme). Sözleşme uçlarındaki &#x60;assigned_party_ids&#x60; (imzacı kimliği) ile karıştırmayın.  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

