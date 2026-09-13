# KycWebhookEnvelope

KYC olaylarının zarfı. Sözleşme olaylarıyla aynı alanları taşır; `type` enum\'u yalnız KYC olaylarını içerir. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** | Olay id\&#39;si: receiver tarafında idempotency anahtarı. | [default to undefined]
**type** | **string** |  | [default to undefined]
**created_at** | **string** | Olay zamanı (ISO 8601 UTC). | [default to undefined]
**data** | **object** | Olay verisi (WebhookDataKycTerminal) | [default to undefined]

## Example

```typescript
import { KycWebhookEnvelope } from '@imzala/server-sdk-node';

const instance: KycWebhookEnvelope = {
    id,
    type,
    created_at,
    data,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
