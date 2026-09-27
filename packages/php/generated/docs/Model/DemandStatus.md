# DemandStatus

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional]
**title** | **string** |  | [optional]
**status** | **string** |  | [optional]
**created_at** | **\DateTime** |  | [optional]
**completed_at** | **\DateTime** |  | [optional]
**parties** | [**\Imzala\Client\Model\DemandStatusPartiesInner[]**](DemandStatusPartiesInner.md) |  | [optional]
**stamp_items** | [**\Imzala\Client\Model\DemandStatusStampItemsInner[]**](DemandStatusStampItemsInner.md) | Sözleşmedeki kaşe alanları. &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; için alan kimliği buradan alınır. Kaşe içeriği bu listede dönmez. | [optional]
**result_url** | **string** |  | [optional]
**pdf_url** | **string** | Sadece status&#x3D;COMPLETED iken dolu | [optional]
**documents** | [**\Imzala\Client\Model\DemandDocumentStatus[]**](DemandDocumentStatus.md) | Zarftaki belgelerin her biri için ayrı durum, &#x60;order&#x60; sırasıyla. Zarf genelindeki &#x60;status&#x60; alanı değişmez; bu liste hangi belgenin ne zaman tamamlandığını, hangisinin beklediğini, mühür durumunu ve her tarafın belge başına kararını ayrıca gösterir. Belge kaydı bulunmayan eski sözleşmelerde boş dizi döner. | [optional]
**term** | [**\Imzala\Client\Model\ContractTerm**](ContractTerm.md) | Sözleşmede süre/yenileme takibi tanımlı değilse &#x60;null&#x60;. | [optional]
**archived_at** | **\DateTime** |  | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
