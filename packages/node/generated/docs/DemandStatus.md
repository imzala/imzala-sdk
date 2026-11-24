# DemandStatus


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**title** | **string** |  | [optional] [default to undefined]
**external_ref** | **string** | Kendi sisteminizdeki kayıt numarası (ör. sipariş no). Benzersiz olması gerekmez; aynı değerle birden çok sözleşme listelenebilir. Kimlik numarası, e-posta veya telefon yazmayın.  En fazla 255 karakter. Baştaki ve sondaki boşluklar kırpılır; boş dize &#x60;null&#x60; sayılır. Metin olmayan değer ya da satır sonu, sekme, kontrol veya görünmez yön karakteri içeren değer &#x60;400 EXTERNAL_REF_INVALID&#x60;, 255 karakteri aşan değer &#x60;400 EXTERNAL_REF_TOO_LONG&#x60; döner; hata mesajı gönderdiğiniz değeri içermez. Eşleşme tam ve büyük/küçük harf duyarlıdır.  &#x60;idempotency_key&#x60; ile farkı: idempotency anahtarı her oluşturma isteği için benzersizdir ve aynı isteğin iki kez işlenmesini engeller; takip numarası ise tekrar edebilir ve tekrar korumasının yerine geçmez. Aynı &#x60;Idempotency-Key&#x60; ile farklı bir takip numarası gönderilirse istek farklı içerik sayılır (&#x60;409 IDEMPOTENCY_KEY_REUSED&#x60;). Kimlik doğrulama (KYC) uçlarındaki &#x60;external_ref&#x60; alanından farklı olarak benzersizlik kuralı yoktur.  Tanımlı değilse &#x60;null&#x60; döner. Sonradan &#x60;PATCH /api/v1/demands/{id}/external-ref&#x60; ile değiştirilebilir. İmzacılara gösterilmez.  | [optional] [default to undefined]
**status** | **string** | Bilinen değerler: DRAFT, PENDING, COMPLETED, EXPIRED, CANCELLED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. | [optional] [default to undefined]
**created_at** | **string** |  | [optional] [default to undefined]
**completed_at** | **string** |  | [optional] [default to undefined]
**parties** | [**Array&lt;DemandStatusPartiesInner&gt;**](DemandStatusPartiesInner.md) |  | [optional] [default to undefined]
**stamp_items** | [**Array&lt;DemandStatusStampItemsInner&gt;**](DemandStatusStampItemsInner.md) | Sözleşmedeki kaşe alanları. &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; için alan kimliği buradan alınır. Kaşe içeriği bu listede dönmez.  | [optional] [default to undefined]
**result_url** | **string** | Sonuç sayfası açık rıza belgesini, sayfalarını ve indirme bağlantısını göstermez. | [optional] [default to undefined]
**pdf_url** | **string** | Sadece status&#x3D;COMPLETED iken dolu. Açık rıza belgesi bu herkese açık bağlantıdan verilmez (zarfın ilk belgesi açık rıza belgesiyse 404 döner); API anahtarıyla &#x60;GET /api/v1/demands/{id}/pdf&#x60; kullanın. | [optional] [default to undefined]
**documents** | [**Array&lt;DemandDocumentStatus&gt;**](DemandDocumentStatus.md) | Zarftaki belgelerin her biri için ayrı durum, &#x60;order&#x60; sırasıyla. Zarf genelindeki &#x60;status&#x60; alanı değişmez; bu liste hangi belgenin ne zaman tamamlandığını, hangisinin beklediğini, mühür durumunu ve her tarafın belge başına kararını ayrıca gösterir. Belge kaydı bulunmayan eski sözleşmelerde boş dizi döner.  | [optional] [default to undefined]
**term** | [**ContractTerm**](ContractTerm.md) | Sözleşmede süre/yenileme takibi tanımlı değilse &#x60;null&#x60;. | [optional] [default to undefined]
**archived_at** | **string** |  | [optional] [default to undefined]
**contract_numbers** | [**Array&lt;ContractNumber&gt;**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları. Numara yoksa boş dizi.  | [optional] [default to undefined]

## Example

```typescript
import { DemandStatus } from '@imzala/server-sdk-node';

const instance: DemandStatus = {
    id,
    title,
    external_ref,
    status,
    created_at,
    completed_at,
    parties,
    stamp_items,
    result_url,
    pdf_url,
    documents,
    term,
    archived_at,
    contract_numbers,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
