# CreatedDemandUploadFieldLayout

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_id** | **string** |  | [optional]
**applied_fields** | **int** | Sözleşmeye yazılan alan sayısı. | [optional]
**on_anchor_miss** | **string** | İstenen değil, **uygulanan** davranış. &#x60;drop&#x60; istenip uygulanamadıysa burada &#x60;block&#x60; görürsünüz ve &#x60;warnings&#x60; içinde &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; bulunur. | [optional]
**warnings** | [**\Imzala\Client\Model\FieldLayoutWarning[]**](FieldLayoutWarning.md) |  | [optional]
**diagnostics** | [**\Imzala\Client\Model\FieldLayoutDiagnostic[]**](FieldLayoutDiagnostic.md) | Yalnız &#x60;WARNING&#x60; seviyesindeki tanılar. &#x60;ERROR&#x60; seviyesinde bir tanı olsaydı istek 422 ile reddedilir ve sözleşme yaratılmazdı. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
