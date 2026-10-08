# ApiV1DemandsBulkPost400Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **boolean** |  | [default to undefined]
**error** | **string** |  | [default to undefined]
**message** | **string** |  | [optional] [default to undefined]
**code** | **string** |  | [default to undefined]
**retry_after_seconds** | **number** | Yalnız 429 yanıtlarında; &#x60;Retry-After&#x60; başlığıyla aynı değer. | [optional] [default to undefined]
**field** | **string** | Bazı kodlarda (ör. &#x60;TERM_INVALID&#x60;) hangi alanın reddedildiğini gösterir. Yalnız bu tür kodlarda bulunur; her hata gövdesinde YOKTUR.  | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsBulkPost400Response } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsBulkPost400Response = {
    success,
    error,
    message,
    code,
    retry_after_seconds,
    field,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
