# ApiV1DemandsPost500Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **boolean** |  | [optional] [default to undefined]
**error** | **string** |  | [optional] [default to undefined]
**code** | **string** | Bilinen değerler: RECONCILE_FAILED, DISPATCH_PREPARATION_FAILED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. | [optional] [default to undefined]
**data** | [**ApiV1DemandsPost402ResponseData**](ApiV1DemandsPost402ResponseData.md) |  | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsPost500Response } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsPost500Response = {
    success,
    error,
    code,
    data,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
