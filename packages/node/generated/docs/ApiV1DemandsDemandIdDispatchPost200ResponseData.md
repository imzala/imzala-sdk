# ApiV1DemandsDemandIdDispatchPost200ResponseData


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **string** |  | [optional] [default to undefined]
**status** | **string** |  | [optional] [default to undefined]
**dispatched** | **boolean** | &#x60;false&#x60; ise sözleşme zaten yayınlanmıştı (&#x60;ALREADY&#x60;); yalnız davet fazı tekrar çalıştı, &#x60;demand.dispatched&#x60; webhook olayı TEKRAR yayılmadı.  | [optional] [default to undefined]
**credits** | [**ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits**](ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits.md) |  | [optional] [default to undefined]
**invitations** | [**ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations**](ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations.md) |  | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsDemandIdDispatchPost200ResponseData } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsDemandIdDispatchPost200ResponseData = {
    demand_id,
    status,
    dispatched,
    credits,
    invitations,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
