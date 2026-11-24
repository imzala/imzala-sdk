

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
|**externalRef** | **String** | Sözleşmenin bildirim anındaki takip numarası; tanımlı değilse &#x60;null&#x60;. Takip numarası tekrar edebilir: alıcı tarafta tekrar işlemeyi zarftaki olay &#x60;id&#x60;&#39;si ile önleyin, &#x60;external_ref&#x60; ile değil. Sözleşme bilgisi okunamadığında bildirim bu alan olmadan gönderilir.  |  [optional] |
|**backfill** | **Boolean** | Replay bayrağı (yan etkileri atla) |  [optional] |
