# TemplatePartySummary


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**order** | **number** |  | [optional] [default to undefined]
**label** | **string** |  | [optional] [default to undefined]
**is_required** | **boolean** |  | [optional] [default to undefined]
**default_locale** | **string** | İmzacı dili. &#x60;tr&#x60; (Türkçe), &#x60;en&#x60; (İngilizce) veya &#x60;null&#x60; (atanmadı).  Bilinen değerler: tr, en. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. | [optional] [default to undefined]

## Example

```typescript
import { TemplatePartySummary } from '@imzala/server-sdk-node';

const instance: TemplatePartySummary = {
    id,
    order,
    label,
    is_required,
    default_locale,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
