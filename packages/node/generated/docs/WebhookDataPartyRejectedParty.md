# WebhookDataPartyRejectedParty


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [default to undefined]
**first_name** | **string** |  | [default to undefined]
**last_name** | **string** |  | [default to undefined]
**email** | **string** |  | [optional] [default to undefined]
**rejection_reason** | **string** | Tarafın belirttiği sebep (opsiyonel, modal textarea) | [optional] [default to undefined]

## Example

```typescript
import { WebhookDataPartyRejectedParty } from '@imzala/server-sdk-node';

const instance: WebhookDataPartyRejectedParty = {
    id,
    first_name,
    last_name,
    email,
    rejection_reason,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
