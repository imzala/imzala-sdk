# StandardError

`code`/`message` formatında standart hata gövdesi. Reminder trigger (`POST /api/v1/demands/{id}/reminders`) bu formatı kullanır. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **boolean** |  | [optional] [default to undefined]
**error** | [**StandardErrorError**](StandardErrorError.md) |  | [optional] [default to undefined]

## Example

```typescript
import { StandardError } from '@imzala/server-sdk-node';

const instance: StandardError = {
    success,
    error,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
