# DemandDocumentStatus

Zarftaki tek bir belgenin durumu.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**order** | **number** | 1\&#39;den başlar. | [optional] [default to undefined]
**title** | **string** |  | [optional] [default to undefined]
**doc_kind** | **string** |  | [optional] [default to undefined]
**is_required** | **boolean** |  | [optional] [default to undefined]
**signature_required** | **boolean** |  | [optional] [default to undefined]
**assigned_party_ids** | **Array&lt;string&gt;** |  | [optional] [default to undefined]
**progress** | [**DocumentProgress**](DocumentProgress.md) |  | [optional] [default to undefined]
**sealing** | [**DocumentSealing**](DocumentSealing.md) |  | [optional] [default to undefined]
**decisions** | [**Array&lt;DocumentPartyDecision&gt;**](DocumentPartyDecision.md) |  | [optional] [default to undefined]

## Example

```typescript
import { DemandDocumentStatus } from '@imzala/server-sdk-node';

const instance: DemandDocumentStatus = {
    id,
    order,
    title,
    doc_kind,
    is_required,
    signature_required,
    assigned_party_ids,
    progress,
    sealing,
    decisions,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
