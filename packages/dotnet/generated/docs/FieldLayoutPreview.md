# ImzalaApiClient.Model.FieldLayoutPreview
Kuru koşum sonucu — hiçbir yan etki üretilmemiştir.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**TemplateId** | **Guid** |  | [optional] 
**PageCount** | **int** | Yüklenen belgenin sayfa sayısı. | [optional] 
**Resolvable** | **bool** | &#x60;true&#x60; ise aynı belge + aynı şablon ile &#x60;POST /api/v1/demands/upload&#x60; çağrısı alan yerleşimi nedeniyle reddedilmez.  | [optional] 
**OnAnchorMiss** | **string** | Uygulanan davranış (istenen değil). | [optional] 
**PlacementsSummary** | [**FieldLayoutPreviewPlacementsSummary**](FieldLayoutPreviewPlacementsSummary.md) |  | [optional] 
**Diagnostics** | [**List&lt;FieldLayoutDiagnostic&gt;**](FieldLayoutDiagnostic.md) |  | [optional] 
**Warnings** | [**List&lt;FieldLayoutWarning&gt;**](FieldLayoutWarning.md) |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

