# ImzalaApiClient.Model.DemandStatus

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Id** | **Guid** |  | [optional] 
**Title** | **string** |  | [optional] 
**Status** | **string** |  | [optional] 
**CreatedAt** | **DateTime** |  | [optional] 
**CompletedAt** | **DateTime?** |  | [optional] 
**Parties** | [**List&lt;DemandStatusPartiesInner&gt;**](DemandStatusPartiesInner.md) |  | [optional] 
**StampItems** | [**List&lt;DemandStatusStampItemsInner&gt;**](DemandStatusStampItemsInner.md) | Sözleşmedeki kaşe alanları. &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; için alan kimliği buradan alınır. Kaşe içeriği bu listede dönmez.  | [optional] 
**ResultUrl** | **string** |  | [optional] 
**PdfUrl** | **string** | Sadece status&#x3D;COMPLETED iken dolu | [optional] 
**Documents** | [**List&lt;DemandDocumentStatus&gt;**](DemandDocumentStatus.md) | Zarftaki belgelerin her biri için ayrı durum, &#x60;order&#x60; sırasıyla. Zarf genelindeki &#x60;status&#x60; alanı değişmez; bu liste hangi belgenin ne zaman tamamlandığını, hangisinin beklediğini, mühür durumunu ve her tarafın belge başına kararını ayrıca gösterir. Belge kaydı bulunmayan eski sözleşmelerde boş dizi döner.  | [optional] 
**Term** | [**ContractTerm**](ContractTerm.md) | Sözleşmede süre/yenileme takibi tanımlı değilse &#x60;null&#x60;. | [optional] 
**ArchivedAt** | **DateTime?** |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

