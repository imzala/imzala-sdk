

# WebhookDataPartyRejected


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**demandId** | **UUID** |  |  |
|**party** | [**WebhookDataPartyRejectedParty**](WebhookDataPartyRejectedParty.md) |  |  |
|**rejectedAt** | **OffsetDateTime** |  |  |
|**externalRef** | **String** | Sözleşmenin bildirim anındaki takip numarası; tanımlı değilse &#x60;null&#x60;. Takip numarası tekrar edebilir: alıcı tarafta tekrar işlemeyi zarftaki olay &#x60;id&#x60;&#39;si ile önleyin, &#x60;external_ref&#x60; ile değil. Sözleşme bilgisi okunamadığında bildirim bu alan olmadan gönderilir.  |  [optional] |
