

# CreatedDemandUploadDispatch

YALNIZ istekte `send_invitations` gönderildiğinde döner. Davet gönderimi 201'i düşürmez (sözleşme yaratılmış ve kredi düşülmüştür), bu yüzden hatalar burada taraf bazında raporlanır. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**requested** | **Boolean** |  |  [optional] |
|**sent** | **Integer** | En az bir kanaldan (SMS/e-posta/WhatsApp) davet **gönderilen taraf** sayısı — kanal sayısı DEĞİL. (&#x60;POST /api/v1/demands&#x60; yanıtındaki &#x60;dispatched&#x60; alanı kanal sayar; bu alan saymaz.)  |  [optional] |
|**results** | [**List&lt;CreatedDemandUploadDispatchResultsInner&gt;**](CreatedDemandUploadDispatchResultsInner.md) |  |  [optional] |
|**error** | [**ErrorEnum**](#ErrorEnum) | &#x60;DISPATCH_SKIPPED&#x60; — sözleşme satırı okunamadı, bildirim ayarları bilinmediği için hiç davet gönderilmedi. &#x60;DISPATCH_TOO_MANY&#x60; — taraf sayısı 20 sınırını aşıyor.  |  [optional] |



## Enum: ErrorEnum

| Name | Value |
|---- | -----|
| DISPATCH_SKIPPED | &quot;DISPATCH_SKIPPED&quot; |
| DISPATCH_TOO_MANY | &quot;DISPATCH_TOO_MANY&quot; |



