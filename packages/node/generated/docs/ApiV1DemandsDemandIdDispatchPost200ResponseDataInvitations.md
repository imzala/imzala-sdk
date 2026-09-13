# ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**sent** | **number** | En az bir kanaldan başarıyla gönderilen taraf sayısı. | [optional] [default to undefined]
**results** | [**Array&lt;ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner&gt;**](ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner.md) |  | [optional] [default to undefined]
**error** | **string** | &#x60;DISPATCH_SKIPPED&#x60; — sözleşme satırı okunamadı, körlemesine gönderim yapılmadı. (Taraf sayısı tavanı bu uçta davet fazından ÖNCE denetlenir ve **409 &#x60;DISPATCH_TOO_MANY&#x60;** olarak döner; 200 gövdesinde görünmez.)  | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations = {
    sent,
    results,
    error,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
