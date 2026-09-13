# ApiV1DemandsBulkPost200ResponseDataResultsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**row_index** | **number** |  | [optional] [default to undefined]
**status** | **string** |  | [optional] [default to undefined]
**demand_id** | **string** | status&#x3D;created ise; veya status&#x3D;failed olup sözleşme oluşturulduktan SONRA kredi mutabakatı reddettiyse (taslak, davet gönderilmedi) | [optional] [default to undefined]
**signing_urls** | [**Array&lt;ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner&gt;**](ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner.md) |  | [optional] [default to undefined]
**result_url** | **string** |  | [optional] [default to undefined]
**dispatched** | **number** | Gönderilen davet (SMS+e-posta) sayısı | [optional] [default to undefined]
**error** | **string** | status&#x3D;failed ise makinece okunabilir kod (VALIDATION, INSUFFICIENT_CREDITS, MEMBER_LIMIT_EXCEEDED, CREATE_FAILED vb.) | [optional] [default to undefined]
**message** | **string** | status&#x3D;failed ise açıklama | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsBulkPost200ResponseDataResultsInner } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsBulkPost200ResponseDataResultsInner = {
    row_index,
    status,
    demand_id,
    signing_urls,
    result_url,
    dispatched,
    error,
    message,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
