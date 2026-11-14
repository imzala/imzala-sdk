# ImzalaApiClient.Model.WebhookDataDemandExpired

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**DemandId** | **Guid** |  |
**Title** | **string** |  |
**ExpiryDate** | **DateTime** |  |
**Parties** | [**List&lt;WebhookDataDemandExpiredPartiesInner&gt;**](WebhookDataDemandExpiredPartiesInner.md) |  |
**ContractNumbers** | [**List&lt;ContractNumber&gt;**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları; numara yoksa boş dizi. Numaralar okunamadığında bildirim bu alan olmadan gönderilir.  | [optional]

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
