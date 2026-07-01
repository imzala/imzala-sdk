# StandardErrorError


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**code** | **string** |  | [default to undefined]
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
