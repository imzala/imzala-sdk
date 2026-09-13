# CreatedDemandUploadFieldLayout

YALNIZ `field_template_id` gönderildiğinde döner. Alan yerleşiminin uygulanma özeti. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_id** | **string** |  | [optional] [default to undefined]
**applied_fields** | **number** | Sözleşmeye yazılan alan sayısı. | [optional] [default to undefined]
**on_anchor_miss** | **string** | İstenen değil, **uygulanan** davranış. &#x60;drop&#x60; istenip uygulanamadıysa burada &#x60;block&#x60; görürsünüz ve &#x60;warnings&#x60; içinde &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; bulunur.  | [optional] [default to undefined]
**warnings** | [**Array&lt;FieldLayoutWarning&gt;**](FieldLayoutWarning.md) |  | [optional] [default to undefined]
**diagnostics** | [**Array&lt;FieldLayoutDiagnostic&gt;**](FieldLayoutDiagnostic.md) | Yalnız &#x60;WARNING&#x60; seviyesindeki tanılar. &#x60;ERROR&#x60; seviyesinde bir tanı olsaydı istek 422 ile reddedilir ve sözleşme yaratılmazdı.  | [optional] [default to undefined]

## Example

```typescript
import { CreatedDemandUploadFieldLayout } from '@imzala/server-sdk-node';

const instance: CreatedDemandUploadFieldLayout = {
    template_id,
    applied_fields,
    on_anchor_miss,
    warnings,
    diagnostics,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
