# ApiV1DemandsPost400Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **boolean** |  | [optional] [default to undefined]
**error** | **string** |  | [optional] [default to undefined]
**code** | **string** |  | [optional] [default to undefined]
**field** | **string** | Bazı kodlarda (ör. &#x60;TERM_INVALID&#x60;) hangi alanın reddedildiğini gösterir. Yalnız bu tür kodlarda bulunur; her hata gövdesinde YOKTUR.  | [optional] [default to undefined]
**details** | [**DocumentSelectionErrorDetails**](DocumentSelectionErrorDetails.md) |  | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsPost400Response } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsPost400Response = {
    success,
    error,
    code,
    field,
    details,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
