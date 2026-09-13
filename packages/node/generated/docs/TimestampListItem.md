# TimestampListItem

Zaman damgası liste/detay öğesi. `timestamp_file_url` doğrudan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı olabilir). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**original_file_name** | **string** |  | [optional] [default to undefined]
**original_file_size** | **number** |  | [optional] [default to undefined]
**timestamp_date** | **string** |  | [optional] [default to undefined]
**status** | **string** |  | [optional] [default to undefined]
**description** | **string** |  | [optional] [default to undefined]
**created_at** | **string** |  | [optional] [default to undefined]
**timestamp_file_url** | **string** |  | [optional] [default to undefined]

## Example

```typescript
import { TimestampListItem } from '@imzala/server-sdk-node';

const instance: TimestampListItem = {
    id,
    original_file_name,
    original_file_size,
    timestamp_date,
    status,
    description,
    created_at,
    timestamp_file_url,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
