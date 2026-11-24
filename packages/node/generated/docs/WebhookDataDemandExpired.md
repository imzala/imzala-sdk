# WebhookDataDemandExpired


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **string** |  | [default to undefined]
**title** | **string** |  | [default to undefined]
**expiry_date** | **string** |  | [default to undefined]
**parties** | [**Array&lt;WebhookDataDemandExpiredPartiesInner&gt;**](WebhookDataDemandExpiredPartiesInner.md) |  | [default to undefined]
**contract_numbers** | [**Array&lt;ContractNumber&gt;**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları; numara yoksa boş dizi. Numaralar okunamadığında bildirim bu alan olmadan gönderilir.  | [optional] [default to undefined]
**external_ref** | **string** | Sözleşmenin bildirim anındaki takip numarası; tanımlı değilse &#x60;null&#x60;. Takip numarası tekrar edebilir: alıcı tarafta tekrar işlemeyi zarftaki olay &#x60;id&#x60;\&#39;si ile önleyin, &#x60;external_ref&#x60; ile değil. Sözleşme bilgisi okunamadığında bildirim bu alan olmadan gönderilir.  | [optional] [default to undefined]

## Example

```typescript
import { WebhookDataDemandExpired } from '@imzala/server-sdk-node';

const instance: WebhookDataDemandExpired = {
    demand_id,
    title,
    expiry_date,
    parties,
    contract_numbers,
    external_ref,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
