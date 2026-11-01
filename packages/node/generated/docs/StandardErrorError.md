# StandardErrorError


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**code** | **string** | Bilinen değerler: INVALID_CHANNELS, DEMAND_NOT_FOUND, ALREADY_COMPLETED, RATE_LIMITED, MAX_SMS_REMINDERS_REACHED, DEMAND_NOT_DISPATCHED, DEMAND_NOT_DISPATCHABLE, DEMAND_EXPIRED, ENVELOPE_NOT_DISPATCHED, UNAUTHORIZED, INTERNAL_ERROR. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. | [default to undefined]
**message** | **string** |  | [default to undefined]
**retry_after_seconds** | **number** | Sadece RATE_LIMITED\&#39;de doludur | [optional] [default to undefined]

## Example

```typescript
import { StandardErrorError } from '@imzala/server-sdk-node';

const instance: StandardErrorError = {
    code,
    message,
    retry_after_seconds,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
