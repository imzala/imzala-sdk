

# ApiV1DemandsDemandIdDocumentsPostRequest


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**title** | **String** | Belge başlığı. Boş olamaz. |  |
|**docKind** | [**DocKindEnum**](#DocKindEnum) | &#x60;KVKK_CONSENT&#x60; ile &#x60;is_required: true&#x60; **birlikte gönderilemez** (400 &#x60;CONSENT_CANNOT_BE_REQUIRED&#x60; — açık rıza reddedilebilir olmak zorundadır). &#x60;PREINFO&#x60; ile &#x60;is_required: false&#x60; **birlikte gönderilemez** (400 &#x60;PREINFO_MUST_BE_REQUIRED&#x60;).  |  [optional] |
|**isRequired** | **Boolean** |  |  [optional] |
|**signatureRequired** | **Boolean** |  |  [optional] |



## Enum: DocKindEnum

| Name | Value |
|---- | -----|
| CONTRACT | &quot;CONTRACT&quot; |
| KVKK_NOTICE | &quot;KVKK_NOTICE&quot; |
| KVKK_CONSENT | &quot;KVKK_CONSENT&quot; |
| PREINFO | &quot;PREINFO&quot; |
| PRICE_LIST | &quot;PRICE_LIST&quot; |
| OTHER | &quot;OTHER&quot; |



