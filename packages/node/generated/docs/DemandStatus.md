# DemandStatus


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**title** | **string** |  | [optional] [default to undefined]
**status** | **string** |  | [optional] [default to undefined]
**created_at** | **string** |  | [optional] [default to undefined]
**completed_at** | **string** |  | [optional] [default to undefined]
**parties** | [**Array&lt;DemandStatusPartiesInner&gt;**](DemandStatusPartiesInner.md) |  | [optional] [default to undefined]
**result_url** | **string** |  | [optional] [default to undefined]
**pdf_url** | **string** | Sadece status&#x3D;COMPLETED iken dolu | [optional] [default to undefined]

## Example

```typescript
import { DemandStatus } from '@imzala/server-sdk-node';

const instance: DemandStatus = {
    id,
    title,
    status,
    created_at,
    completed_at,
    parties,
    result_url,
    pdf_url,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
