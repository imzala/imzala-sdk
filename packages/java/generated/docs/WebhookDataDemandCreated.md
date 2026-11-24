

# WebhookDataDemandCreated


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**demandId** | **UUID** |  |  |
|**title** | **String** |  |  |
|**contractNumbers** | [**List&lt;ContractNumber&gt;**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları; numara yoksa boş dizi. Numaralar okunamadığında bildirim bu alan olmadan gönderilir.  |  [optional] |
|**externalRef** | **String** | Sözleşmenin bildirim anındaki takip numarası; tanımlı değilse &#x60;null&#x60;. Takip numarası tekrar edebilir: alıcı tarafta tekrar işlemeyi zarftaki olay &#x60;id&#x60;&#39;si ile önleyin, &#x60;external_ref&#x60; ile değil. Sözleşme bilgisi okunamadığında bildirim bu alan olmadan gönderilir.  |  [optional] |
|**backfill** | **Boolean** | Geçmiş event&#39;i replay etmek için bayrak. Receiver yan etkileri (e-posta gönderme, ödeme tetikleme vs.) atlamalı.  |  [optional] |
