

# WebhookDataDemandCompleted


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**demandId** | **UUID** |  |  |
|**title** | **String** |  |  |
|**status** | **String** | Bilinen değerler: COMPLETED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  |
|**completedAt** | **OffsetDateTime** |  |  |
|**parties** | [**List&lt;WebhookDataDemandCompletedPartiesInner&gt;**](WebhookDataDemandCompletedPartiesInner.md) |  |  |
|**contractNumbers** | [**List&lt;ContractNumber&gt;**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları; numara yoksa boş dizi. Numaralar okunamadığında bildirim bu alan olmadan gönderilir.  |  [optional] |
|**backfill** | **Boolean** | Replay bayrağı (yan etkileri atla) |  [optional] |
