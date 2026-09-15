

# ApiError

Standart hata zarfı. `success: false`, `error` makinece okunabilir hata kodu, `message` kullanıcıya dönük açıklama. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**success** | **Boolean** |  |  [optional] |
|**error** | **String** |  |  [optional] |
|**message** | **String** |  |  [optional] |
|**code** | **String** | İstek limiti yanıtlarında &#x60;error&#x60; ile aynı kodu taşır; diğer yanıtlarda bulunmayabilir. |  [optional] |
|**retryAfterSeconds** | **Integer** | Yalnız 429 yanıtlarında; &#x60;Retry-After&#x60; başlığıyla aynı değer. |  [optional] |



