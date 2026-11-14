

# WebhookDataDemandDispatched


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**demandId** | **UUID** |  |  |
|**title** | **String** |  |  |
|**dispatchedBy** | **UUID** | Gönderimi yapan kullanıcının kimliği; belirlenemiyorsa &#x60;null&#x60;. |  |
|**contractNumbers** | [**List&lt;ContractNumber&gt;**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları; numara yoksa boş dizi. Numaralar okunamadığında bildirim bu alan olmadan gönderilir.  |  [optional] |
