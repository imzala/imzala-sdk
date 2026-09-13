# ImzalaApiClient.Model.ApiV1DemandsDemandIdDocumentsPostRequest

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Title** | **string** | Belge başlığı. Boş olamaz. | 
**DocKind** | **string** | &#x60;KVKK_CONSENT&#x60; ile &#x60;is_required: true&#x60; **birlikte gönderilemez** (400 &#x60;CONSENT_CANNOT_BE_REQUIRED&#x60; — açık rıza reddedilebilir olmak zorundadır). &#x60;PREINFO&#x60; ile &#x60;is_required: false&#x60; **birlikte gönderilemez** (400 &#x60;PREINFO_MUST_BE_REQUIRED&#x60;).  | [optional] [default to DocKindEnum.OTHER]
**IsRequired** | **bool** |  | [optional] [default to true]
**SignatureRequired** | **bool** |  | [optional] [default to true]

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

