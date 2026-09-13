# FieldLayoutPreview

Kuru koşum sonucu — hiçbir yan etki üretilmemiştir.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_id** | **string** |  | [optional] [default to undefined]
**page_count** | **number** | Yüklenen belgenin sayfa sayısı. | [optional] [default to undefined]
**resolvable** | **boolean** | &#x60;true&#x60; ise aynı belge + aynı şablon ile &#x60;POST /api/v1/demands/upload&#x60; çağrısı alan yerleşimi nedeniyle reddedilmez.  | [optional] [default to undefined]
**on_anchor_miss** | **string** | Uygulanan davranış (istenen değil). | [optional] [default to undefined]
**placements_summary** | [**FieldLayoutPreviewPlacementsSummary**](FieldLayoutPreviewPlacementsSummary.md) |  | [optional] [default to undefined]
**diagnostics** | [**Array&lt;FieldLayoutDiagnostic&gt;**](FieldLayoutDiagnostic.md) |  | [optional] [default to undefined]
**warnings** | [**Array&lt;FieldLayoutWarning&gt;**](FieldLayoutWarning.md) |  | [optional] [default to undefined]

## Example

```typescript
import { FieldLayoutPreview } from '@imzala/server-sdk-node';

const instance: FieldLayoutPreview = {
    template_id,
    page_count,
    resolvable,
    on_anchor_miss,
    placements_summary,
    diagnostics,
    warnings,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
