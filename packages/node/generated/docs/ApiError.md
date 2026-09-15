# ApiError

Standart hata zarfı. `success: false`, `error` makinece okunabilir hata kodu, `message` kullanıcıya dönük açıklama. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **boolean** |  | [optional] [default to undefined]
**error** | **string** |  | [optional] [default to undefined]
**message** | **string** |  | [optional] [default to undefined]
**code** | **string** | İstek limiti yanıtlarında &#x60;error&#x60; ile aynı kodu taşır; diğer yanıtlarda bulunmayabilir. | [optional] [default to undefined]
**retry_after_seconds** | **number** | Yalnız 429 yanıtlarında; &#x60;Retry-After&#x60; başlığıyla aynı değer. | [optional] [default to undefined]

## Example

```typescript
import { ApiError } from '@imzala/server-sdk-node';

const instance: ApiError = {
    success,
    error,
    message,
    code,
    retry_after_seconds,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
