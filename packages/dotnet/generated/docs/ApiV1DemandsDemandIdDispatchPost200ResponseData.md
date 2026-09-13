# ImzalaApiClient.Model.ApiV1DemandsDemandIdDispatchPost200ResponseData

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**DemandId** | **Guid** |  | [optional] 
**Status** | **string** |  | [optional] 
**Dispatched** | **bool** | &#x60;false&#x60; ise sözleşme zaten yayınlanmıştı (&#x60;ALREADY&#x60;); yalnız davet fazı tekrar çalıştı, &#x60;demand.dispatched&#x60; webhook olayı TEKRAR yayılmadı.  | [optional] 
**Credits** | [**ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits**](ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits.md) |  | [optional] 
**Invitations** | [**ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations**](ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations.md) |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

