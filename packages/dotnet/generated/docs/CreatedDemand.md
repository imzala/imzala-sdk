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

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

