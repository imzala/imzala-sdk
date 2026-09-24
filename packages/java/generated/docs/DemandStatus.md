

# DemandStatus


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**title** | **String** |  |  [optional] |
|**status** | [**StatusEnum**](#StatusEnum) |  |  [optional] |
|**createdAt** | **OffsetDateTime** |  |  [optional] |
|**completedAt** | **OffsetDateTime** |  |  [optional] |
|**parties** | [**List&lt;DemandStatusPartiesInner&gt;**](DemandStatusPartiesInner.md) |  |  [optional] |
|**stampItems** | [**List&lt;DemandStatusStampItemsInner&gt;**](DemandStatusStampItemsInner.md) | Sözleşmedeki kaşe alanları. &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; için alan kimliği buradan alınır. Kaşe içeriği bu listede dönmez.  |  [optional] |
|**resultUrl** | **URI** |  |  [optional] |
|**pdfUrl** | **URI** | Sadece status&#x3D;COMPLETED iken dolu |  [optional] |
|**documents** | [**List&lt;DemandDocumentStatus&gt;**](DemandDocumentStatus.md) | Zarftaki belgelerin her biri için ayrı durum, &#x60;order&#x60; sırasıyla. Zarf genelindeki &#x60;status&#x60; alanı değişmez; bu liste hangi belgenin ne zaman tamamlandığını, hangisinin beklediğini, mühür durumunu ve her tarafın belge başına kararını ayrıca gösterir. Belge kaydı bulunmayan eski sözleşmelerde boş dizi döner.  |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| DRAFT | &quot;DRAFT&quot; |
| PENDING | &quot;PENDING&quot; |
| COMPLETED | &quot;COMPLETED&quot; |
| EXPIRED | &quot;EXPIRED&quot; |
| CANCELLED | &quot;CANCELLED&quot; |



