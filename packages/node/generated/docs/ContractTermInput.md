# ContractTermInput

Sözleşme süre/yenileme takibi girdisi. `POST /demands` üzerinde oluştururken ve `PATCH /demands/{id}/term` üzerinde güncellerken AYNI alan adları kullanılır. `PATCH`\'te kısmi güncelleme uygulanır: yalnız gövdede GÖNDERİLEN anahtarlar değiştirilir; bir anahtarı `null` göndermek o alanı temizler, hiç göndermemek dokunmaz.  Doğrulama kuralları: - `term_fixed_end_date` ile `term_duration_months` birlikte gönderilemez. - `term_start_mode: FIXED_DATE` ise oluştururken `term_start_date` ya   da `term_fixed_end_date` gerekir; `ON_FIRST_SIGNATURE` /   `ON_COMPLETION` modlarında `term_start_date` gönderilse de yok   sayılır (başlangıç imza anında otomatik yazılır). - `renewal_type: AUTO_RENEW` ise `renewal_period_months` gerekir;   gönderilmezse `term_duration_months`\'tan devralınır. - Geçersiz kombinasyon 400 `TERM_INVALID` + hangi alanı işaret eden   `field` ile döner. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**term_start_mode** | **string** |  | [optional] [default to undefined]
**term_start_date** | **string** | Yalnız &#x60;term_start_mode: FIXED_DATE&#x60; iken kullanılır. | [optional] [default to undefined]
**term_duration_months** | **number** |  | [optional] [default to undefined]
**term_fixed_end_date** | **string** |  | [optional] [default to undefined]
**renewal_type** | **string** |  | [optional] [default to undefined]
**renewal_period_months** | **number** |  | [optional] [default to undefined]
**notice_days** | **number** |  | [optional] [default to undefined]
**reminder_offsets** | **Array&lt;number&gt;** | Boş dizi göndermek şablon/önceki değeri SİLMEZ (yok sayılır); temizlemek için &#x60;null&#x60; gönderin.  | [optional] [default to undefined]
**notify_counterparty** | **boolean** |  | [optional] [default to false]

## Example

```typescript
import { ContractTermInput } from '@imzala/server-sdk-node';

const instance: ContractTermInput = {
    term_start_mode,
    term_start_date,
    term_duration_months,
    term_fixed_end_date,
    renewal_type,
    renewal_period_months,
    notice_days,
    reminder_offsets,
    notify_counterparty,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
