# FieldTemplateDetail

Alan Şablonu ayrıntısı. Alan koordinatları TAŞIMAZ; yerleşimin bir belgeye nasıl uygulanacağı için kuru koşum ucunu kullanın. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**name** | **string** |  | [optional] [default to undefined]
**description** | **string** |  | [optional] [default to undefined]
**category** | **string** |  | [optional] [default to undefined]
**total_field_count** | **number** | Şablondaki toplam alan sayısı. | [optional] [default to undefined]
**parties** | [**Array&lt;FieldTemplateDetailPartiesInner&gt;**](FieldTemplateDetailPartiesInner.md) |  | [optional] [default to undefined]

## Example

```typescript
import { FieldTemplateDetail } from '@imzala/server-sdk-node';

const instance: FieldTemplateDetail = {
    id,
    name,
    description,
    category,
    total_field_count,
    parties,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
