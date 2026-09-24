# EnvelopeDocument

Çok-belgeli imza zarfındaki tek bir belge. `assigned_party_ids` ve `decision_count` yalnız `GET .../documents?view=wizard` yanıtında bulunur; `has_timestamp` yalnız varsayılan (özet) görünümde bulunur — ikisi AYNI ANDA gelmez (dashboard\'un iki ayrı iç DTO\'sunun [`EnvelopeDocumentDTO` / `EnvelopeDocumentSummaryDTO`] tek şema altında birleştirilmiş hâli). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**order** | **number** | 1\&#39;den başlar. | [optional] [default to undefined]
**title** | **string** |  | [optional] [default to undefined]
**doc_kind** | **string** |  | [optional] [default to undefined]
**is_required** | **boolean** |  | [optional] [default to undefined]
**signature_required** | **boolean** |  | [optional] [default to undefined]
**assigned_party_ids** | **Array&lt;string&gt;** | Yalnız &#x60;?view&#x3D;wizard&#x60; yanıtında bulunur. | [optional] [default to undefined]
**decision_count** | **number** | Yalnız &#x60;?view&#x3D;wizard&#x60; yanıtında bulunur. Bu belge için verilmiş onay/red kararı sayısı.  | [optional] [default to undefined]
**sealing_status** | **string** |  | [optional] [default to undefined]
**has_timestamp** | **boolean** | Yalnız varsayılan (özet) görünümde bulunur. &#x60;sealing_status&#x60; &#x60;SEALED&#x60; değilken daima &#x60;false&#x60; döner.  | [optional] [default to undefined]
**page_count** | **number** |  | [optional] [default to undefined]
**completed_at** | **string** |  | [optional] [default to undefined]
**progress** | [**DocumentProgress**](DocumentProgress.md) | Yalnız &#x60;GET .../documents&#x60; liste yanıtında bulunur. | [optional] [default to undefined]
**sealing** | [**DocumentSealing**](DocumentSealing.md) | Yalnız &#x60;GET .../documents&#x60; liste yanıtında bulunur. | [optional] [default to undefined]
**decisions** | [**Array&lt;DocumentPartyDecision&gt;**](DocumentPartyDecision.md) | Yalnız &#x60;GET .../documents&#x60; liste yanıtında bulunur. Belge başına taraf kararları; &#x60;GET /api/v1/demands/{id}&#x60; yanıtındaki &#x60;documents[].decisions&#x60; ile aynıdır.  | [optional] [default to undefined]

## Example

```typescript
import { EnvelopeDocument } from '@imzala/server-sdk-node';

const instance: EnvelopeDocument = {
    id,
    order,
    title,
    doc_kind,
    is_required,
    signature_required,
    assigned_party_ids,
    decision_count,
    sealing_status,
    has_timestamp,
    page_count,
    completed_at,
    progress,
    sealing,
    decisions,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
