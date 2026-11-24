# ImzalaApiClient.Model.WebhookDataDemandCreated

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**DemandId** | **Guid** |  |
**Title** | **string** |  |
**ContractNumbers** | [**List&lt;ContractNumber&gt;**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları; numara yoksa boş dizi. Numaralar okunamadığında bildirim bu alan olmadan gönderilir.  | [optional]
**ExternalRef** | **string** | Sözleşmenin bildirim anındaki takip numarası; tanımlı değilse &#x60;null&#x60;. Takip numarası tekrar edebilir: alıcı tarafta tekrar işlemeyi zarftaki olay &#x60;id&#x60;&#39;si ile önleyin, &#x60;external_ref&#x60; ile değil. Sözleşme bilgisi okunamadığında bildirim bu alan olmadan gönderilir.  | [optional]
**Backfill** | **bool** | Geçmiş event&#39;i replay etmek için bayrak. Receiver yan etkileri (e-posta gönderme, ödeme tetikleme vs.) atlamalı.  | [optional]

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
