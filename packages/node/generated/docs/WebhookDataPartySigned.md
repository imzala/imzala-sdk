# WebhookDataPartySigned


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **string** |  | [default to undefined]
**party_id** | **string** |  | [default to undefined]
**external_ref** | **string** | Sözleşmenin bildirim anındaki takip numarası; tanımlı değilse &#x60;null&#x60;. Takip numarası tekrar edebilir: alıcı tarafta tekrar işlemeyi zarftaki olay &#x60;id&#x60;\&#39;si ile önleyin, &#x60;external_ref&#x60; ile değil. Sözleşme bilgisi okunamadığında bildirim bu alan olmadan gönderilir.  | [optional] [default to undefined]
**_backfill** | **boolean** | Replay bayrağı | [optional] [default to undefined]

## Example

```typescript
import { WebhookDataPartySigned } from '@imzala/server-sdk-node';

const instance: WebhookDataPartySigned = {
    demand_id,
    party_id,
    external_ref,
    _backfill,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
