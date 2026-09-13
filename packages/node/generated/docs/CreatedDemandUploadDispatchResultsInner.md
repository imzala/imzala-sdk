# CreatedDemandUploadDispatchResultsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**party_id** | **string** |  | [optional] [default to undefined]
**sms** | **boolean** |  | [optional] [default to undefined]
**email** | **boolean** |  | [optional] [default to undefined]
**whatsapp** | **boolean** |  | [optional] [default to undefined]
**error** | **string** | Yalnız o taraf için gönderim patladığında döner. | [optional] [default to undefined]

## Example

```typescript
import { CreatedDemandUploadDispatchResultsInner } from '@imzala/server-sdk-node';

const instance: CreatedDemandUploadDispatchResultsInner = {
    party_id,
    sms,
    email,
    whatsapp,
    error,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
