

# FieldLayoutPreview

Kuru koşum sonucu — hiçbir yan etki üretilmemiştir.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**templateId** | **UUID** |  |  [optional] |
|**pageCount** | **Integer** | Yüklenen belgenin sayfa sayısı. |  [optional] |
|**resolvable** | **Boolean** | &#x60;true&#x60; ise aynı belge + aynı şablon ile &#x60;POST /api/v1/demands/upload&#x60; çağrısı alan yerleşimi nedeniyle reddedilmez.  |  [optional] |
|**onAnchorMiss** | **String** | Uygulanan davranış (istenen değil).  Bilinen değerler: block, drop. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  [optional] |
|**placementsSummary** | [**FieldLayoutPreviewPlacementsSummary**](FieldLayoutPreviewPlacementsSummary.md) |  |  [optional] |
|**diagnostics** | [**List&lt;FieldLayoutDiagnostic&gt;**](FieldLayoutDiagnostic.md) |  |  [optional] |
|**warnings** | [**List&lt;FieldLayoutWarning&gt;**](FieldLayoutWarning.md) |  |  [optional] |
