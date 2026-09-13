# ApiV1DemandsDemandIdDocumentsPostRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**title** | **string** | Belge başlığı. Boş olamaz. | [default to undefined]
**doc_kind** | **string** | &#x60;KVKK_CONSENT&#x60; ile &#x60;is_required: true&#x60; **birlikte gönderilemez** (400 &#x60;CONSENT_CANNOT_BE_REQUIRED&#x60; — açık rıza reddedilebilir olmak zorundadır). &#x60;PREINFO&#x60; ile &#x60;is_required: false&#x60; **birlikte gönderilemez** (400 &#x60;PREINFO_MUST_BE_REQUIRED&#x60;).  | [optional] [default to DocKindEnum_Other]
**is_required** | **boolean** |  | [optional] [default to true]
**signature_required** | **boolean** |  | [optional] [default to true]

## Example

```typescript
import { ApiV1DemandsDemandIdDocumentsPostRequest } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsDemandIdDocumentsPostRequest = {
    title,
    doc_kind,
    is_required,
    signature_required,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
