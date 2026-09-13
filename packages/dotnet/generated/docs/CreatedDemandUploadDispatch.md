# ImzalaApiClient.Model.CreatedDemandUploadDispatch
YALNIZ istekte `send_invitations` gönderildiğinde döner. Davet gönderimi 201'i düşürmez (sözleşme yaratılmış ve kredi düşülmüştür), bu yüzden hatalar burada taraf bazında raporlanır. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Requested** | **bool** |  | [optional] 
**Sent** | **int** | En az bir kanaldan (SMS/e-posta/WhatsApp) davet **gönderilen taraf** sayısı — kanal sayısı DEĞİL. (&#x60;POST /api/v1/demands&#x60; yanıtındaki &#x60;dispatched&#x60; alanı kanal sayar; bu alan saymaz.)  | [optional] 
**Results** | [**List&lt;CreatedDemandUploadDispatchResultsInner&gt;**](CreatedDemandUploadDispatchResultsInner.md) |  | [optional] 
**Error** | **string** | &#x60;DISPATCH_SKIPPED&#x60; — sözleşme satırı okunamadı, bildirim ayarları bilinmediği için hiç davet gönderilmedi. &#x60;DISPATCH_TOO_MANY&#x60; — taraf sayısı 20 sınırını aşıyor.  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

