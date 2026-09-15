# ApiError

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional]
**error** | **string** |  | [optional]
**message** | **string** |  | [optional]
**code** | **string** | İstek limiti yanıtlarında &#x60;error&#x60; ile aynı kodu taşır; diğer yanıtlarda bulunmayabilir. | [optional]
**retry_after_seconds** | **int** | Yalnız 429 yanıtlarında; &#x60;Retry-After&#x60; başlığıyla aynı değer. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
