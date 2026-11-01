

# DocumentSelectionError

`400 INVALID_DOCUMENT_SELECTION` gövdesi. `details.reason` hatanın sınıfını, `details.document_ids` (varsa) yalnız isteğinizde gönderdiğiniz ve soruna yol açan kimlikleri taşır.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**success** | **Boolean** |  |  [optional] |
|**error** | **String** |  |  [optional] |
|**code** | **String** | Bilinen değerler: INVALID_DOCUMENT_SELECTION. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  [optional] |
|**details** | [**DocumentSelectionErrorDetails**](DocumentSelectionErrorDetails.md) |  |  [optional] |
