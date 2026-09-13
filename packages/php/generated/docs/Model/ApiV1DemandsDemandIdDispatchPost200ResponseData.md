# ApiV1DemandsDemandIdDispatchPost200ResponseData

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **string** |  | [optional]
**status** | **string** |  | [optional]
**dispatched** | **bool** | &#x60;false&#x60; ise sözleşme zaten yayınlanmıştı (&#x60;ALREADY&#x60;); yalnız davet fazı tekrar çalıştı, &#x60;demand.dispatched&#x60; webhook olayı TEKRAR yayılmadı. | [optional]
**credits** | [**\Imzala\Client\Model\ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits**](ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits.md) |  | [optional]
**invitations** | [**\Imzala\Client\Model\ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations**](ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations.md) |  | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
