# ApiV1DemandsBulkPostRequestRowsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**party_mapping** | [**Array&lt;ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner&gt;**](ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.md) | Bu satırın taraf eşlemesi (POST /demands ile aynı şekil) | [default to undefined]
**variables** | **object** | Satır-kök değişkenler | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsBulkPostRequestRowsInner } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsBulkPostRequestRowsInner = {
    party_mapping,
    variables,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
