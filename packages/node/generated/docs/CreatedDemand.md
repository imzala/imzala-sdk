# CreatedDemand


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**title** | **string** |  | [optional] [default to undefined]
**status** | **string** |  | [optional] [default to undefined]
**template_id** | **string** |  | [optional] [default to undefined]
**signing_urls** | [**Array&lt;CreatedDemandSigningUrlsInner&gt;**](CreatedDemandSigningUrlsInner.md) |  | [optional] [default to undefined]
**result_url** | **string** |  | [optional] [default to undefined]
**variables_applied** | **Array&lt;string&gt;** | Uygulanan TÜM slug\&#39;ların unique union\&#39;ı (sorted). Geriye dönük uyumluluk için korunur — yeni entegrasyonlar variables_applied_root + variables_applied_by_party kullanmalı.  | [optional] [default to undefined]
**variables_applied_root** | **Array&lt;string&gt;** | Root variables\&#39;tan uygulanan slug listesi (sorted). | [optional] [default to undefined]
**variables_applied_by_party** | **{ [key: string]: Array&lt;string&gt;; }** | template_party_id → o partiye uygulanan slug listesi (sorted per party).  | [optional] [default to undefined]
**variables_ignored** | **Array&lt;string&gt;** | Gönderdiğiniz AMA hiçbir item\&#39;a uygulanmayan slug\&#39;lar (unique, sorted). Boş olmayınca yazım hatası yapmışsınız demektir — kontrol edin.  | [optional] [default to undefined]
**stamps_applied** | [**Array&lt;CreatedDemandStampsAppliedInner&gt;**](CreatedDemandStampsAppliedInner.md) | Yalnız kaşe slug\&#39;ına &#x60;StampData&#x60; nesnesi gönderildiyse döner. Nesne değerle doldurulan kaşe alanları (alan kimliği sözleşmeye aittir, şablona değil).  | [optional] [default to undefined]
**variables_applied_by_document** | [**{ [key: string]: DocumentScopedSlugs; }**](DocumentScopedSlugs.md) | Yalnız &#x60;document_variables&#x60; gönderildiyse döner. Belge kimliği → belge kapsamından uygulanan slug\&#39;lar: &#x60;_common&#x60; (kök &#x60;document_variables&#x60;) ve &#x60;_by_party&#x60; (template_party_id → slug\&#39;lar).  | [optional] [default to undefined]
**variables_ignored_by_document** | [**{ [key: string]: DocumentScopedSlugs; }**](DocumentScopedSlugs.md) | Yalnız &#x60;document_variables&#x60; gönderildiyse döner. Belge kapsamında gönderilip O BELGEDE hiçbir alana yazılmayan slug\&#39;lar (aynı biçim).  | [optional] [default to undefined]

## Example

```typescript
import { CreatedDemand } from '@imzala/server-sdk-node';

const instance: CreatedDemand = {
    id,
    title,
    status,
    template_id,
    signing_urls,
    result_url,
    variables_applied,
    variables_applied_root,
    variables_applied_by_party,
    variables_ignored,
    stamps_applied,
    variables_applied_by_document,
    variables_ignored_by_document,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
