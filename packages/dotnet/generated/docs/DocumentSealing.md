# ImzalaApiClient.Model.DocumentSealing
Belgenin mühür durumu. Karar ilerlemesinden ayrı bir olgudur.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Status** | **string** | Bilinen değerler: NONE, PENDING, SEALED, FAILED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. | [optional]
**HasTimestamp** | **bool** | Yalnız &#x60;status&#x60; &#x60;SEALED&#x60; iken &#x60;true&#x60; olabilir. | [optional]

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
