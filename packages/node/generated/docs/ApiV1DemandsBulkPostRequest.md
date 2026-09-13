# ApiV1DemandsBulkPostRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_id** | **string** | Sözleşmelerin oluşturulacağı şablon | [default to undefined]
**_options** | [**ApiV1DemandsBulkPostRequestOptions**](ApiV1DemandsBulkPostRequestOptions.md) |  | [optional] [default to undefined]
**rows** | [**Array&lt;ApiV1DemandsBulkPostRequestRowsInner&gt;**](ApiV1DemandsBulkPostRequestRowsInner.md) | Her biri ayrı bir sözleşme olacak satırlar (en fazla 10) | [default to undefined]

## Example

```typescript
import { ApiV1DemandsBulkPostRequest } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsBulkPostRequest = {
    template_id,
    _options,
    rows,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
