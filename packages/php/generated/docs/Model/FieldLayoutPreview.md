# FieldLayoutPreview

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_id** | **string** |  | [optional]
**page_count** | **int** | Yüklenen belgenin sayfa sayısı. | [optional]
**resolvable** | **bool** | &#x60;true&#x60; ise aynı belge + aynı şablon ile &#x60;POST /api/v1/demands/upload&#x60; çağrısı alan yerleşimi nedeniyle reddedilmez. | [optional]
**on_anchor_miss** | **string** | Uygulanan davranış (istenen değil). | [optional]
**placements_summary** | [**\Imzala\Client\Model\FieldLayoutPreviewPlacementsSummary**](FieldLayoutPreviewPlacementsSummary.md) |  | [optional]
**diagnostics** | [**\Imzala\Client\Model\FieldLayoutDiagnostic[]**](FieldLayoutDiagnostic.md) |  | [optional]
**warnings** | [**\Imzala\Client\Model\FieldLayoutWarning[]**](FieldLayoutWarning.md) |  | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
