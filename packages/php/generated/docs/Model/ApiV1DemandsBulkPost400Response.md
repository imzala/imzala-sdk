# ApiV1DemandsBulkPost400Response

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  |
**error** | **string** |  |
**message** | **string** |  | [optional]
**code** | **string** |  |
**retry_after_seconds** | **int** | Yalnız 429 yanıtlarında; &#x60;Retry-After&#x60; başlığıyla aynı değer. | [optional]
**field** | **string** | Bazı kodlarda (ör. &#x60;TERM_INVALID&#x60;) hangi alanın reddedildiğini gösterir. Yalnız bu tür kodlarda bulunur; her hata gövdesinde YOKTUR. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
