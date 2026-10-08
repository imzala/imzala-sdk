# ImzalaApiClient.Model.ApiV1DemandsPost404Response

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Success** | **bool** |  | [optional]
**Error** | **string** |  | [optional]
**Message** | **string** |  | [optional]
**Code** | **string** |  | [optional]
**RetryAfterSeconds** | **int** | Yalnız 429 yanıtlarında; &#x60;Retry-After&#x60; başlığıyla aynı değer. | [optional]
**Field** | **string** | Bazı kodlarda (ör. &#x60;TERM_INVALID&#x60;) hangi alanın reddedildiğini gösterir. Yalnız bu tür kodlarda bulunur; her hata gövdesinde YOKTUR.  | [optional]

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
