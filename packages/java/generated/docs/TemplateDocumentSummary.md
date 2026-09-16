

# TemplateDocumentSummary

Şablonun zarf belgesi. `id` alanı `POST /api/v1/demands` ve `POST /api/v1/demands/bulk` isteklerindeki `documents.include` / `documents.exclude` listelerinde kullanılır. Şablon kopyalandığında (panelden \"Kopyala\" ya da sözleşmeden şablon oluşturma) kopyanın belge kimlikleri YENİDİR; entegrasyonunuz kopyanın kimliklerini bu uçtan yeniden okumalıdır. Başlık, tür ve ayar değişiklikleri ile yeniden sıralama kimliği değiştirmez. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**order** | **Integer** | Şablondaki sıra (1&#39;den başlar) |  [optional] |
|**title** | **String** |  |  [optional] |
|**docKind** | [**DocKindEnum**](#DocKindEnum) |  |  [optional] |
|**isRequired** | **Boolean** | İmzacının bu belgeyi atlayıp atlayamayacağı. Gönderilecek belgeleri SEÇMEKLE ilgisi yoktur: zorunlu belge de &#x60;documents.exclude&#x60; ile bu imza sürecinden çıkarılabilir. Mevzuat gereği karşı tarafa verilmesi gereken bir belgeyi (örneğin &#x60;doc_kind: PREINFO&#x60; ön bilgilendirme formu veya &#x60;KVKK_NOTICE&#x60; aydınlatma metni) başka bir kanaldan vermiyorsanız çıkarmayın; bu belgelerin verilmesi yükümlülüğü sözleşmeyi gönderene aittir.  |  [optional] |
|**signatureRequired** | **Boolean** |  |  [optional] |
|**defaultIncluded** | **Boolean** | İstek &#x60;documents&#x60; belirtmezse bu belge imza sürecine (zarfa) dahil edilir mi. |  [optional] |
|**assignedTemplatePartyIds** | **List&lt;UUID&gt;** | Bu belgeyi imzalayacak şablon rollerinin kimlikleri (&#x60;parties[].id&#x60; ve &#x60;party_mapping[].template_party_id&#x60; ile aynı küme). Sözleşme uçlarındaki &#x60;assigned_party_ids&#x60; (imzacı kimliği) ile karıştırmayın.  |  [optional] |



## Enum: DocKindEnum

| Name | Value |
|---- | -----|
| CONTRACT | &quot;CONTRACT&quot; |
| KVKK_NOTICE | &quot;KVKK_NOTICE&quot; |
| KVKK_CONSENT | &quot;KVKK_CONSENT&quot; |
| PREINFO | &quot;PREINFO&quot; |
| PRICE_LIST | &quot;PRICE_LIST&quot; |
| OTHER | &quot;OTHER&quot; |



