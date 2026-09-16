

# DocumentSelectionError

`400 INVALID_DOCUMENT_SELECTION` gövdesi. `details.reason` hatanın sınıfını, `details.document_ids` (varsa) yalnız isteğinizde gönderdiğiniz ve soruna yol açan kimlikleri taşır. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**success** | **Boolean** |  |  [optional] |
|**error** | **String** |  |  [optional] |
|**code** | [**CodeEnum**](#CodeEnum) |  |  [optional] |
|**details** | [**DocumentSelectionErrorDetails**](DocumentSelectionErrorDetails.md) |  |  [optional] |



## Enum: CodeEnum

| Name | Value |
|---- | -----|
| INVALID_DOCUMENT_SELECTION | &quot;INVALID_DOCUMENT_SELECTION&quot; |



