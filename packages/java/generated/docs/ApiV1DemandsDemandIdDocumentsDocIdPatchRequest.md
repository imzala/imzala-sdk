

# ApiV1DemandsDemandIdDocumentsDocIdPatchRequest


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**title** | **String** | Gönderilirse boş olamaz. |  [optional] |
|**docKind** | [**DocKindEnum**](#DocKindEnum) |  |  [optional] |
|**isRequired** | **Boolean** | Gönderilmezse değişmez. |  [optional] |
|**signatureRequired** | **Boolean** | Gönderilmezse değişmez. |  [optional] |



## Enum: DocKindEnum

| Name | Value |
|---- | -----|
| CONTRACT | &quot;CONTRACT&quot; |
| KVKK_NOTICE | &quot;KVKK_NOTICE&quot; |
| KVKK_CONSENT | &quot;KVKK_CONSENT&quot; |
| PREINFO | &quot;PREINFO&quot; |
| PRICE_LIST | &quot;PRICE_LIST&quot; |
| OTHER | &quot;OTHER&quot; |



