# DemandCreationError

Sözleşme oluşturma hatalarının açık taşıma zarfı. Alanlar hata türüne göre bulunur; yeni makine kodları istemci tarafından reddedilmez. Somut Belge Toplama zarfları DocumentCollectionPolicyError ve DocumentVerificationDisabledError şemaları ile yanıt örneklerinde açıklanır.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **boolean** |  | [optional] [default to undefined]
**error** | **string** |  | [optional] [default to undefined]
**code** | **string** |  | [optional] [default to undefined]
**message** | **string** |  | [optional] [default to undefined]
**field** | **string** |  | [optional] [default to undefined]
**details** | **{ [key: string]: any; }** |  | [optional] [default to undefined]

## Example

```typescript
import { DemandCreationError } from '@imzala/server-sdk-node';

const instance: DemandCreationError = {
    success,
    error,
    code,
    message,
    field,
    details,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
