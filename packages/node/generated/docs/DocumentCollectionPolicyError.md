# DocumentCollectionPolicyError

Seçili Belge Toplama şablonunun politikası geçersiz; error kaynak makine kodudur.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **boolean** |  | [default to undefined]
**code** | **string** | Bilinen değerler: VERIFICATION_CONTEXT_REQUIRED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. | [default to undefined]
**error** | **string** | Bilinen değerler: DOCUMENT_TYPE_INVALID, VERIFICATION_POLICY_INVALID, VERIFICATION_INSTRUCTION_INVALID, VERIFICATION_CONTEXT_REQUIRED, VERIFICATION_POLICY_CONFLICT. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. | [default to undefined]

## Example

```typescript
import { DocumentCollectionPolicyError } from '@imzala/server-sdk-node';

const instance: DocumentCollectionPolicyError = {
    success,
    code,
    error,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
