# DocumentSelectionError

`400 INVALID_DOCUMENT_SELECTION` gövdesi. `details.reason` hatanın sınıfını, `details.document_ids` (varsa) yalnız isteğinizde gönderdiğiniz ve soruna yol açan kimlikleri taşır. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **boolean** |  | [optional] [default to undefined]
**error** | **string** |  | [optional] [default to undefined]
**code** | **string** |  | [optional] [default to undefined]
**details** | [**DocumentSelectionErrorDetails**](DocumentSelectionErrorDetails.md) |  | [optional] [default to undefined]

## Example

```typescript
import { DocumentSelectionError } from '@imzala/server-sdk-node';

const instance: DocumentSelectionError = {
    success,
    error,
    code,
    details,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
