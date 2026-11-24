# ImzalaApiClient.Model.WebhookDataDemandCompleted

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**DemandId** | **Guid** |  |
**Title** | **string** |  |
**Status** | **string** | Bilinen değerler: COMPLETED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |
**CompletedAt** | **DateTime** |  |
**Parties** | [**List&lt;WebhookDataDemandCompletedPartiesInner&gt;**](WebhookDataDemandCompletedPartiesInner.md) |  |
**ContractNumbers** | [**List&lt;ContractNumber&gt;**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları; numara yoksa boş dizi. Numaralar okunamadığında bildirim bu alan olmadan gönderilir.  | [optional]
**ExternalRef** | **string** | Sözleşmenin bildirim anındaki takip numarası; tanımlı değilse &#x60;null&#x60;. Takip numarası tekrar edebilir: alıcı tarafta tekrar işlemeyi zarftaki olay &#x60;id&#x60;&#39;si ile önleyin, &#x60;external_ref&#x60; ile değil. Sözleşme bilgisi okunamadığında bildirim bu alan olmadan gönderilir.  | [optional]
**Backfill** | **bool** | Replay bayrağı (yan etkileri atla) | [optional]

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
