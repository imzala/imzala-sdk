# WebhookDataDemandCompleted


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **string** |  | [default to undefined]
**title** | **string** |  | [default to undefined]
**status** | **string** |  | [default to undefined]
**completed_at** | **string** |  | [default to undefined]
**parties** | [**Array&lt;WebhookDataDemandCompletedPartiesInner&gt;**](WebhookDataDemandCompletedPartiesInner.md) |  | [default to undefined]
**_backfill** | **boolean** | Replay bayrağı (yan etkileri atla) | [optional] [default to undefined]

## Example

```typescript
import { WebhookDataDemandCompleted } from '@imzala/server-sdk-node';

const instance: WebhookDataDemandCompleted = {
    demand_id,
    title,
    status,
    completed_at,
    parties,
    _backfill,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
