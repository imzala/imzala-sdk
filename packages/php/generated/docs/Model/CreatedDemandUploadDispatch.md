# CreatedDemandUploadDispatch

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**requested** | **bool** |  | [optional]
**sent** | **int** | En az bir kanaldan (SMS/e-posta/WhatsApp) davet **gönderilen taraf** sayısı — kanal sayısı DEĞİL. (&#x60;POST /api/v1/demands&#x60; yanıtındaki &#x60;dispatched&#x60; alanı kanal sayar; bu alan saymaz.) | [optional]
**results** | [**\Imzala\Client\Model\CreatedDemandUploadDispatchResultsInner[]**](CreatedDemandUploadDispatchResultsInner.md) |  | [optional]
**error** | **string** | &#x60;DISPATCH_SKIPPED&#x60; — sözleşme satırı okunamadı, bildirim ayarları bilinmediği için hiç davet gönderilmedi. &#x60;DISPATCH_TOO_MANY&#x60; — taraf sayısı 20 sınırını aşıyor. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
