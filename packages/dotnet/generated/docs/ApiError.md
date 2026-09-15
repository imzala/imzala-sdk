# ImzalaApiClient.Model.ApiError
Standart hata zarfı. `success: false`, `error` makinece okunabilir hata kodu, `message` kullanıcıya dönük açıklama. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Success** | **bool** |  | [optional] 
**Error** | **string** |  | [optional] 
**Message** | **string** |  | [optional] 
**Code** | **string** | İstek limiti yanıtlarında &#x60;error&#x60; ile aynı kodu taşır; diğer yanıtlarda bulunmayabilir. | [optional] 
**RetryAfterSeconds** | **int** | Yalnız 429 yanıtlarında; &#x60;Retry-After&#x60; başlığıyla aynı değer. | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

