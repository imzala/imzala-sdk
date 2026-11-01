# DocumentSealing

Belgenin mühür durumu. Karar ilerlemesinden ayrı bir olgudur.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**status** | **string** | Bilinen değerler: NONE, PENDING, SEALED, FAILED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. | [optional] [default to undefined]
**has_timestamp** | **boolean** | Yalnız &#x60;status&#x60; &#x60;SEALED&#x60; iken &#x60;true&#x60; olabilir. | [optional] [default to undefined]

## Example

```typescript
import { DocumentSealing } from '@imzala/server-sdk-node';

const instance: DocumentSealing = {
    status,
    has_timestamp,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
