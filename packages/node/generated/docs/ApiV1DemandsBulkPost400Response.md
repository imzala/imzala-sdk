# ApiV1DemandsBulkPost400Response

Toplu oluşturma ön kontrolünün açık hata zarfı; alanlar hata türüne göre bulunur.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **boolean** |  | [optional] [default to undefined]
**error** | **string** |  | [optional] [default to undefined]
**code** | **string** |  | [optional] [default to undefined]
**message** | **string** |  | [optional] [default to undefined]
**field** | **string** |  | [optional] [default to undefined]
**details** | **{ [key: string]: any; }** |  | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsBulkPost400Response } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsBulkPost400Response = {
    success,
    error,
    code,
    message,
    field,
    details,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
