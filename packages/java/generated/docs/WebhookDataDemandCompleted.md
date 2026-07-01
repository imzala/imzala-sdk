

# WebhookDataDemandCompleted


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**demandId** | **UUID** |  |  |
|**title** | **String** |  |  |
|**status** | [**StatusEnum**](#StatusEnum) |  |  |
|**completedAt** | **OffsetDateTime** |  |  |
|**parties** | [**List&lt;WebhookDataDemandCompletedPartiesInner&gt;**](WebhookDataDemandCompletedPartiesInner.md) |  |  |
|**backfill** | **Boolean** | Replay bayrağı (yan etkileri atla) |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| COMPLETED | &quot;COMPLETED&quot; |



