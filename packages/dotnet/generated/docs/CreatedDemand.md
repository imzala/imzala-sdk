# ImzalaApiClient.Model.CreatedDemand

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Id** | **Guid** |  | [optional] 
**Title** | **string** |  | [optional] 
**Status** | **string** |  | [optional] 
**TemplateId** | **Guid** |  | [optional] 
**SigningUrls** | [**List&lt;CreatedDemandSigningUrlsInner&gt;**](CreatedDemandSigningUrlsInner.md) |  | [optional] 
**ResultUrl** | **string** |  | [optional] 
**VariablesApplied** | **List&lt;string&gt;** | Uygulanan TÜM slug&#39;ların unique union&#39;ı (sorted). Geriye dönük uyumluluk için korunur — yeni entegrasyonlar variables_applied_root + variables_applied_by_party kullanmalı.  | [optional] 
**VariablesAppliedRoot** | **List&lt;string&gt;** | Root variables&#39;tan uygulanan slug listesi (sorted). | [optional] 
**VariablesAppliedByParty** | **Dictionary&lt;string, List&lt;string&gt;&gt;** | template_party_id → o partiye uygulanan slug listesi (sorted per party).  | [optional] 
**VariablesIgnored** | **List&lt;string&gt;** | Gönderdiğiniz AMA hiçbir item&#39;a uygulanmayan slug&#39;lar (unique, sorted). Boş olmayınca yazım hatası yapmışsınız demektir — kontrol edin.  | [optional] 
**StampsApplied** | [**List&lt;CreatedDemandStampsAppliedInner&gt;**](CreatedDemandStampsAppliedInner.md) | Yalnız kaşe slug&#39;ına &#x60;StampData&#x60; nesnesi gönderildiyse döner. Nesne değerle doldurulan kaşe alanları (alan kimliği sözleşmeye aittir, şablona değil).  | [optional] 
**VariablesAppliedByDocument** | [**Dictionary&lt;string, DocumentScopedSlugs&gt;**](DocumentScopedSlugs.md) | Yalnız &#x60;document_variables&#x60; gönderildiyse döner. Belge kimliği → belge kapsamından uygulanan slug&#39;lar: &#x60;_common&#x60; (kök &#x60;document_variables&#x60;) ve &#x60;_by_party&#x60; (template_party_id → slug&#39;lar).  | [optional] 
**VariablesIgnoredByDocument** | [**Dictionary&lt;string, DocumentScopedSlugs&gt;**](DocumentScopedSlugs.md) | Yalnız &#x60;document_variables&#x60; gönderildiyse döner. Belge kapsamında gönderilip O BELGEDE hiçbir alana yazılmayan slug&#39;lar (aynı biçim).  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

