

# DemandStatusStampItemsInner


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**itemId** | **Integer** |  |  [optional] |
|**pageId** | **Integer** |  |  [optional] |
|**documentId** | **String** |  |  [optional] |
|**slug** | **String** |  |  [optional] |
|**label** | **String** |  |  [optional] |
|**source** | [**SourceEnum**](#SourceEnum) |  |  [optional] |
|**partyId** | **UUID** | Yalnız FILLER_PROVIDES kaşede kaşeyi dolduracak taraf |  [optional] |
|**isRequired** | **Boolean** |  |  [optional] |



## Enum: SourceEnum

| Name | Value |
|---- | -----|
| INLINE | &quot;INLINE&quot; |
| FROM_SAVED | &quot;FROM_SAVED&quot; |
| FILLER_PROVIDES | &quot;FILLER_PROVIDES&quot; |



