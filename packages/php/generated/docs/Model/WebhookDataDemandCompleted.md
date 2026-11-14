# WebhookDataDemandCompleted

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **string** |  |
**title** | **string** |  |
**status** | **string** | Bilinen değerler: COMPLETED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |
**completed_at** | **\DateTime** |  |
**parties** | [**\Imzala\Client\Model\WebhookDataDemandCompletedPartiesInner[]**](WebhookDataDemandCompletedPartiesInner.md) |  |
**contract_numbers** | [**\Imzala\Client\Model\ContractNumber[]**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları; numara yoksa boş dizi. Numaralar okunamadığında bildirim bu alan olmadan gönderilir. | [optional]
**_backfill** | **bool** | Replay bayrağı (yan etkileri atla) | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
