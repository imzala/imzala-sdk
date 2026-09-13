# ImzalaApiClient.Model.ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Sent** | **int** | En az bir kanaldan başarıyla gönderilen taraf sayısı. | [optional] 
**Results** | [**List&lt;ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner&gt;**](ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner.md) |  | [optional] 
**Error** | **string** | &#x60;DISPATCH_SKIPPED&#x60; — sözleşme satırı okunamadı, körlemesine gönderim yapılmadı. (Taraf sayısı tavanı bu uçta davet fazından ÖNCE denetlenir ve **409 &#x60;DISPATCH_TOO_MANY&#x60;** olarak döner; 200 gövdesinde görünmez.)  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

