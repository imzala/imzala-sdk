# TemplateDocumentSummary

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional]
**order** | **int** | Şablondaki sıra (1&#39;den başlar) | [optional]
**title** | **string** |  | [optional]
**doc_kind** | **string** |  | [optional]
**is_required** | **bool** | İmzacının bu belgeyi atlayıp atlayamayacağı. Gönderilecek belgeleri SEÇMEKLE ilgisi yoktur: zorunlu belge de &#x60;documents.exclude&#x60; ile bu imza sürecinden çıkarılabilir. Mevzuat gereği karşı tarafa verilmesi gereken bir belgeyi (örneğin &#x60;doc_kind: PREINFO&#x60; ön bilgilendirme formu veya &#x60;KVKK_NOTICE&#x60; aydınlatma metni) başka bir kanaldan vermiyorsanız çıkarmayın; bu belgelerin verilmesi yükümlülüğü sözleşmeyi gönderene aittir. | [optional]
**signature_required** | **bool** |  | [optional]
**default_included** | **bool** | İstek &#x60;documents&#x60; belirtmezse bu belge imza sürecine (zarfa) dahil edilir mi. | [optional]
**assigned_template_party_ids** | **string[]** | Bu belgeyi imzalayacak şablon rollerinin kimlikleri (&#x60;parties[].id&#x60; ve &#x60;party_mapping[].template_party_id&#x60; ile aynı küme). Sözleşme uçlarındaki &#x60;assigned_party_ids&#x60; (imzacı kimliği) ile karıştırmayın. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
