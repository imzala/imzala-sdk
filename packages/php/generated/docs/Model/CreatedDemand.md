# CreatedDemand

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional]
**title** | **string** |  | [optional]
**status** | **string** |  | [optional]
**template_id** | **string** |  | [optional]
**signing_urls** | [**\Imzala\Client\Model\CreatedDemandSigningUrlsInner[]**](CreatedDemandSigningUrlsInner.md) |  | [optional]
**result_url** | **string** |  | [optional]
**variables_applied** | **string[]** | Uygulanan TÜM slug&#39;ların unique union&#39;ı (sorted). Geriye dönük uyumluluk için korunur — yeni entegrasyonlar variables_applied_root + variables_applied_by_party kullanmalı. | [optional]
**variables_applied_root** | **string[]** | Root variables&#39;tan uygulanan slug listesi (sorted). | [optional]
**variables_applied_by_party** | **array<string,string[]>** | template_party_id → o partiye uygulanan slug listesi (sorted per party). | [optional]
**variables_ignored** | **string[]** | Gönderdiğiniz AMA hiçbir item&#39;a uygulanmayan slug&#39;lar (unique, sorted). Boş olmayınca yazım hatası yapmışsınız demektir — kontrol edin. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
