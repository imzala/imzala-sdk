# ImzalaApiClient.Model.DocumentSelectionError
`400 INVALID_DOCUMENT_SELECTION` gövdesi. `details.reason` hatanın sınıfını, `details.document_ids` (varsa) yalnız isteğinizde gönderdiğiniz ve soruna yol açan kimlikleri taşır.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Success** | **bool** |  | [optional]
**Error** | **string** |  | [optional]
**Code** | **string** | Bilinen değerler: INVALID_DOCUMENT_SELECTION. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. | [optional]
**Details** | [**DocumentSelectionErrorDetails**](DocumentSelectionErrorDetails.md) |  | [optional]

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
