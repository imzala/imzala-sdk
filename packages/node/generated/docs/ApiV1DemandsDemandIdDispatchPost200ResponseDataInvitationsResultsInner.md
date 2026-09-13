# ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**party_id** | **string** |  | [optional] [default to undefined]
**sms** | **boolean** |  | [optional] [default to undefined]
**email** | **boolean** |  | [optional] [default to undefined]
**whatsapp** | **boolean** |  | [optional] [default to undefined]
**error** | **string** | Yalnız o taraf için dolar. &#x60;DISPATCH_FAILED&#x60; — gönderim patladı. &#x60;RECIPIENT_QUOTA_EXCEEDED&#x60; — alıcı ekseni freni (telefon başına 3/sa + 10/gün); yalnız bu taraf ATLANDI, diğerleri gönderilmeye devam etti.  | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner = {
    party_id,
    sms,
    email,
    whatsapp,
    error,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
