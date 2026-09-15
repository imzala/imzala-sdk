

# CreatedDemandUploadFieldLayout

YALNIZ `field_template_id` gönderildiğinde döner. Alan yerleşiminin uygulanma özeti. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**templateId** | **UUID** |  |  [optional] |
|**appliedFields** | **Integer** | Sözleşmeye yazılan alan sayısı. |  [optional] |
|**onAnchorMiss** | [**OnAnchorMissEnum**](#OnAnchorMissEnum) | İstenen değil, **uygulanan** davranış. &#x60;drop&#x60; istenip uygulanamadıysa burada &#x60;block&#x60; görürsünüz ve &#x60;warnings&#x60; içinde &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; bulunur.  |  [optional] |
|**warnings** | [**List&lt;FieldLayoutWarning&gt;**](FieldLayoutWarning.md) |  |  [optional] |
|**diagnostics** | [**List&lt;FieldLayoutDiagnostic&gt;**](FieldLayoutDiagnostic.md) | Yalnız &#x60;WARNING&#x60; seviyesindeki tanılar. &#x60;ERROR&#x60; seviyesinde bir tanı olsaydı istek 422 ile reddedilir ve sözleşme yaratılmazdı.  |  [optional] |



## Enum: OnAnchorMissEnum

| Name | Value |
|---- | -----|
| BLOCK | &quot;block&quot; |
| DROP | &quot;drop&quot; |



