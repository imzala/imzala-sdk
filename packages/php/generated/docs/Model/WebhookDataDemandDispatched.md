# WebhookDataDemandDispatched

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **string** |  |
**title** | **string** |  |
**dispatched_by** | **string** | Gönderimi yapan kullanıcının kimliği; belirlenemiyorsa &#x60;null&#x60;. |
**contract_numbers** | [**\Imzala\Client\Model\ContractNumber[]**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları; numara yoksa boş dizi. Numaralar okunamadığında bildirim bu alan olmadan gönderilir. | [optional]
**external_ref** | **string** | Sözleşmenin bildirim anındaki takip numarası; tanımlı değilse &#x60;null&#x60;. Takip numarası tekrar edebilir: alıcı tarafta tekrar işlemeyi zarftaki olay &#x60;id&#x60;&#39;si ile önleyin, &#x60;external_ref&#x60; ile değil. Sözleşme bilgisi okunamadığında bildirim bu alan olmadan gönderilir. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
