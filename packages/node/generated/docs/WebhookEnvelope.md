# WebhookEnvelope

Tüm webhook payload\'larının ortak zarfı. `data` alanı olay tipine göre değişir; her olay için ayrı veri şeması yukarıda dokümante. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** | Olay benzersiz id\&#39;si. Receiver tarafında idempotency için kullanın (DB\&#39;de unique key).  | [default to undefined]
**type** | **string** |  | [default to undefined]
**created_at** | **string** | Olay zamanı (ISO 8601 UTC). | [default to undefined]
**data** | **object** | Olay tipine özel veri (aşağıdaki şemalar) | [default to undefined]

## Example

```typescript
import { WebhookEnvelope } from '@imzala/server-sdk-node';

const instance: WebhookEnvelope = {
    id,
    type,
    created_at,
    data,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
