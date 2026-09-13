# ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**sent** | **int** | En az bir kanaldan başarıyla gönderilen taraf sayısı. | [optional]
**results** | [**\Imzala\Client\Model\ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner[]**](ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner.md) |  | [optional]
**error** | **string** | &#x60;DISPATCH_SKIPPED&#x60; — sözleşme satırı okunamadı, körlemesine gönderim yapılmadı. (Taraf sayısı tavanı bu uçta davet fazından ÖNCE denetlenir ve **409 &#x60;DISPATCH_TOO_MANY&#x60;** olarak döner; 200 gövdesinde görünmez.) | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
