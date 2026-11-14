

# WebhookDataDemandExpired


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**demandId** | **UUID** |  |  |
|**title** | **String** |  |  |
|**expiryDate** | **OffsetDateTime** |  |  |
|**parties** | [**List&lt;WebhookDataDemandExpiredPartiesInner&gt;**](WebhookDataDemandExpiredPartiesInner.md) |  |  |
|**contractNumbers** | [**List&lt;ContractNumber&gt;**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları; numara yoksa boş dizi. Numaralar okunamadığında bildirim bu alan olmadan gönderilir.  |  [optional] |
