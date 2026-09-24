

# DemandDocumentStatus

Zarftaki tek bir belgenin durumu.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**order** | **Integer** | 1&#39;den başlar. |  [optional] |
|**title** | **String** |  |  [optional] |
|**docKind** | [**DocKindEnum**](#DocKindEnum) |  |  [optional] |
|**isRequired** | **Boolean** |  |  [optional] |
|**signatureRequired** | **Boolean** |  |  [optional] |
|**assignedPartyIds** | **List&lt;UUID&gt;** |  |  [optional] |
|**progress** | [**DocumentProgress**](DocumentProgress.md) |  |  [optional] |
|**sealing** | [**DocumentSealing**](DocumentSealing.md) |  |  [optional] |
|**decisions** | [**List&lt;DocumentPartyDecision&gt;**](DocumentPartyDecision.md) |  |  [optional] |



## Enum: DocKindEnum

| Name | Value |
|---- | -----|
| CONTRACT | &quot;CONTRACT&quot; |
| KVKK_NOTICE | &quot;KVKK_NOTICE&quot; |
| KVKK_CONSENT | &quot;KVKK_CONSENT&quot; |
| PREINFO | &quot;PREINFO&quot; |
| PRICE_LIST | &quot;PRICE_LIST&quot; |
| OTHER | &quot;OTHER&quot; |



