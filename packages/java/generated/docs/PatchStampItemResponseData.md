

# PatchStampItemResponseData


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**itemId** | **Integer** |  |  [optional] |
|**pageId** | **Integer** |  |  [optional] |
|**documentId** | **String** |  |  [optional] |
|**partyId** | **UUID** |  |  [optional] |
|**source** | [**SourceEnum**](#SourceEnum) | Güncelleme sonrası kaynak (FROM_SAVED kaşe INLINE olur) |  [optional] |
|**isRequired** | **Boolean** |  |  [optional] |
|**stampData** | **Map&lt;String, Object&gt;** | Güncelleme sonrası kaşe verisinin tamamı. |  [optional] |



## Enum: SourceEnum

| Name | Value |
|---- | -----|
| INLINE | &quot;INLINE&quot; |
| FILLER_PROVIDES | &quot;FILLER_PROVIDES&quot; |



