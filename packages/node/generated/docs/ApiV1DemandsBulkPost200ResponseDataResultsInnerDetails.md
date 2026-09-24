# ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails

Yalnız error=INVALID_DOCUMENT_SELECTION ise: reason (shape | unknown_document | conflict | empty) ve varsa document_ids. error=INVALID_DOCUMENT_VARIABLES ise: reason (unsupported_endpoint) ve path.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**reason** | **string** |  | [optional] [default to undefined]
**document_ids** | **Array&lt;string&gt;** |  | [optional] [default to undefined]
**path** | **string** |  | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails = {
    reason,
    document_ids,
    path,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
