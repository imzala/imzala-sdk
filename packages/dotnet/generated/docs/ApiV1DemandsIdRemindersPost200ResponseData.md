# ImzalaApiClient.Model.ApiV1DemandsIdRemindersPost200ResponseData

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**DemandId** | **Guid** |  | [optional] 
**Dispatched** | [**List&lt;ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner&gt;**](ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner.md) |  | [optional] 
**Skipped** | [**List&lt;ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner&gt;**](ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner.md) | Hatırlatma gönderilmeyen partilerin nedenleriyle birlikte (telefon/email yok, opt-out vs.) | [optional] 
**LastReminderSentAt** | **DateTime** |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

