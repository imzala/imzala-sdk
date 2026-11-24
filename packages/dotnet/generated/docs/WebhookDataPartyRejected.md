# ImzalaApiClient.Model.WebhookDataPartyRejected

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**DemandId** | **Guid** |  |
**Party** | [**WebhookDataPartyRejectedParty**](WebhookDataPartyRejectedParty.md) |  |
**RejectedAt** | **DateTime** |  |
**ExternalRef** | **string** | Sözleşmenin bildirim anındaki takip numarası; tanımlı değilse &#x60;null&#x60;. Takip numarası tekrar edebilir: alıcı tarafta tekrar işlemeyi zarftaki olay &#x60;id&#x60;&#39;si ile önleyin, &#x60;external_ref&#x60; ile değil. Sözleşme bilgisi okunamadığında bildirim bu alan olmadan gönderilir.  | [optional]

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
