# WebhookDataContractTerm

`contract.expiring` / `contract.ended` / `contract.advanced` olaylarının ortak verisi. Yalnız takip alanlarını taşır; taraf adı, e-postası veya sözleşme başlığı BU GÖVDEDE YER ALMAZ. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **string** |  | [default to undefined]
**term_end_date** | **string** | Olay anında platformda kayıtlı takip bitiş tarihi. | [default to undefined]
**renewal_type** | **string** |  | [default to undefined]
**notice_deadline** | **string** | İhbar için son gün (&#x60;term_end_date - notice_days&#x60;); tanımsızsa &#x60;null&#x60;. | [default to undefined]
**term_state** | **string** | Olay anındaki takip durumu (bkz. &#x60;ContractTerm.state&#x60;). | [default to undefined]
**due_key** | **string** | İşçinin tekillik anahtarı (aynı olayı tekrar göndermemek için kullandığı dahili kayıt). Formatı sabit değildir, opak string olarak ele alın; idempotency için &#x60;id&#x60; alanını kullanın.  | [default to undefined]

## Example

```typescript
import { WebhookDataContractTerm } from '@imzala/server-sdk-node';

const instance: WebhookDataContractTerm = {
    demand_id,
    term_end_date,
    renewal_type,
    notice_deadline,
    term_state,
    due_key,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
