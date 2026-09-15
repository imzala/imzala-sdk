

# FieldLayoutPreview

Kuru koşum sonucu — hiçbir yan etki üretilmemiştir.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**templateId** | **UUID** |  |  [optional] |
|**pageCount** | **Integer** | Yüklenen belgenin sayfa sayısı. |  [optional] |
|**resolvable** | **Boolean** | &#x60;true&#x60; ise aynı belge + aynı şablon ile &#x60;POST /api/v1/demands/upload&#x60; çağrısı alan yerleşimi nedeniyle reddedilmez.  |  [optional] |
|**onAnchorMiss** | [**OnAnchorMissEnum**](#OnAnchorMissEnum) | Uygulanan davranış (istenen değil). |  [optional] |
|**placementsSummary** | [**FieldLayoutPreviewPlacementsSummary**](FieldLayoutPreviewPlacementsSummary.md) |  |  [optional] |
|**diagnostics** | [**List&lt;FieldLayoutDiagnostic&gt;**](FieldLayoutDiagnostic.md) |  |  [optional] |
|**warnings** | [**List&lt;FieldLayoutWarning&gt;**](FieldLayoutWarning.md) |  |  [optional] |



## Enum: OnAnchorMissEnum

| Name | Value |
|---- | -----|
| BLOCK | &quot;block&quot; |
| DROP | &quot;drop&quot; |



