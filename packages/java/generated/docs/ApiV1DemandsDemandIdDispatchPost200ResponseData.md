

# ApiV1DemandsDemandIdDispatchPost200ResponseData


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**demandId** | **UUID** |  |  [optional] |
|**status** | **String** |  |  [optional] |
|**dispatched** | **Boolean** | &#x60;false&#x60; ise sözleşme zaten yayınlanmıştı (&#x60;ALREADY&#x60;); yalnız davet fazı tekrar çalıştı, &#x60;demand.dispatched&#x60; webhook olayı TEKRAR yayılmadı.  |  [optional] |
|**credits** | [**ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits**](ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits.md) |  |  [optional] |
|**invitations** | [**ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations**](ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations.md) |  |  [optional] |



