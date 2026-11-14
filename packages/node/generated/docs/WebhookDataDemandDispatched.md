# WebhookDataDemandDispatched


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **string** |  | [default to undefined]
**title** | **string** |  | [default to undefined]
**dispatched_by** | **string** | Gönderimi yapan kullanıcının kimliği; belirlenemiyorsa &#x60;null&#x60;. | [default to undefined]
**contract_numbers** | [**Array&lt;ContractNumber&gt;**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları; numara yoksa boş dizi. Numaralar okunamadığında bildirim bu alan olmadan gönderilir.  | [optional] [default to undefined]

## Example

```typescript
import { WebhookDataDemandDispatched } from '@imzala/server-sdk-node';

const instance: WebhookDataDemandDispatched = {
    demand_id,
    title,
    dispatched_by,
    contract_numbers,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
