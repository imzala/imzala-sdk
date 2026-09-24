# DemandStatus


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**title** | **string** |  | [optional] [default to undefined]
**status** | **string** |  | [optional] [default to undefined]
**created_at** | **string** |  | [optional] [default to undefined]
**completed_at** | **string** |  | [optional] [default to undefined]
**parties** | [**Array&lt;DemandStatusPartiesInner&gt;**](DemandStatusPartiesInner.md) |  | [optional] [default to undefined]
**stamp_items** | [**Array&lt;DemandStatusStampItemsInner&gt;**](DemandStatusStampItemsInner.md) | Sözleşmedeki kaşe alanları. &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; için alan kimliği buradan alınır. Kaşe içeriği bu listede dönmez.  | [optional] [default to undefined]
**result_url** | **string** |  | [optional] [default to undefined]
**pdf_url** | **string** | Sadece status&#x3D;COMPLETED iken dolu | [optional] [default to undefined]
**documents** | [**Array&lt;DemandDocumentStatus&gt;**](DemandDocumentStatus.md) | Zarftaki belgelerin her biri için ayrı durum, &#x60;order&#x60; sırasıyla. Zarf genelindeki &#x60;status&#x60; alanı değişmez; bu liste hangi belgenin ne zaman tamamlandığını, hangisinin beklediğini, mühür durumunu ve her tarafın belge başına kararını ayrıca gösterir. Belge kaydı bulunmayan eski sözleşmelerde boş dizi döner.  | [optional] [default to undefined]

## Example

```typescript
import { DemandStatus } from '@imzala/server-sdk-node';

const instance: DemandStatus = {
    id,
    title,
    status,
    created_at,
    completed_at,
    parties,
    stamp_items,
    result_url,
    pdf_url,
    documents,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
