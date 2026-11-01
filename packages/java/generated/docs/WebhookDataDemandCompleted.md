

# WebhookDataDemandCompleted


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**demandId** | **UUID** |  |  |
|**title** | **String** |  |  |
|**status** | **String** | Bilinen değerler: COMPLETED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  |
|**completedAt** | **OffsetDateTime** |  |  |
|**parties** | [**List&lt;WebhookDataDemandCompletedPartiesInner&gt;**](WebhookDataDemandCompletedPartiesInner.md) |  |  |
|**backfill** | **Boolean** | Replay bayrağı (yan etkileri atla) |  [optional] |
