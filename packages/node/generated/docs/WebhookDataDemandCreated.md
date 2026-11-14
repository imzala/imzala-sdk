# WebhookDataDemandCreated


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **string** |  | [default to undefined]
**title** | **string** |  | [default to undefined]
**contract_numbers** | [**Array&lt;ContractNumber&gt;**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları; numara yoksa boş dizi. Numaralar okunamadığında bildirim bu alan olmadan gönderilir.  | [optional] [default to undefined]
**_backfill** | **boolean** | Geçmiş event\&#39;i replay etmek için bayrak. Receiver yan etkileri (e-posta gönderme, ödeme tetikleme vs.) atlamalı.  | [optional] [default to undefined]

## Example

```typescript
import { WebhookDataDemandCreated } from '@imzala/server-sdk-node';

const instance: WebhookDataDemandCreated = {
    demand_id,
    title,
    contract_numbers,
    _backfill,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
