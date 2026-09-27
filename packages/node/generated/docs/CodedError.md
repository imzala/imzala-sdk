# CodedError

`error` alanında insan-okur mesaj, `code` alanında makinece okunur hata kodu taşıyan hata gövdesi. Hangi kodların döndüğü ilgili yanıtın açıklamasında listelenir. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **boolean** |  | [optional] [default to undefined]
**error** | **string** |  | [optional] [default to undefined]
**code** | **string** |  | [optional] [default to undefined]
**field** | **string** | Bazı kodlarda (ör. &#x60;TERM_INVALID&#x60;) hangi alanın reddedildiğini gösterir. Yalnız bu tür kodlarda bulunur; her hata gövdesinde YOKTUR.  | [optional] [default to undefined]

## Example

```typescript
import { CodedError } from '@imzala/server-sdk-node';

const instance: CodedError = {
    success,
    error,
    code,
    field,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
