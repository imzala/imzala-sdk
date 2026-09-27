# ApiV1DemandsGet200ResponseDataDemandsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**title** | **string** |  | [optional] [default to undefined]
**status** | **string** |  | [optional] [default to undefined]
**created_at** | **string** |  | [optional] [default to undefined]
**completed_at** | **string** |  | [optional] [default to undefined]
**term_end_date** | **string** | Takip edilen bitiş tarihi; takip yoksa &#x60;null&#x60; (bkz. &#x60;GET /demands/{id}&#x60; → &#x60;term&#x60;). | [optional] [default to undefined]
**archived_at** | **string** |  | [optional] [default to undefined]
**parties_total** | **number** |  | [optional] [default to undefined]
**parties_signed** | **number** |  | [optional] [default to undefined]
**pdf_url** | **string** | COMPLETED ise imzalı PDF public URL\&#39;i | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsGet200ResponseDataDemandsInner } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsGet200ResponseDataDemandsInner = {
    id,
    title,
    status,
    created_at,
    completed_at,
    term_end_date,
    archived_at,
    parties_total,
    parties_signed,
    pdf_url,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
