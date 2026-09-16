# ApiV1DemandsBulkPost200ResponseDataResultsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**row_index** | **number** |  | [optional] [default to undefined]
**status** | **string** |  | [optional] [default to undefined]
**demand_id** | **string** | status&#x3D;created ise; veya status&#x3D;failed olup sözleşme oluşturulduktan SONRA kredi mutabakatı reddettiyse ya da atamasız imzacı bulunduysa (taslak, davet gönderilmedi) | [optional] [default to undefined]
**party_ids** | **Array&lt;string&gt;** | Yalnız error&#x3D;PARTY_WITHOUT_DOCUMENTS ve sözleşme oluşturulduktan SONRA tespit edildiyse: hiçbir belgeye atanmamış taraf id\&#39;leri. | [optional] [default to undefined]
**template_party_ids** | **Array&lt;string&gt;** | Yalnız error&#x3D;PARTY_WITHOUT_DOCUMENTS ve satır oluşturulmadan ÖNCE tespit edildiyse: seçim sonucunda hiçbir belgeye atanmamış kalan şablon rol kimlikleri. | [optional] [default to undefined]
**details** | [**ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails**](ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails.md) |  | [optional] [default to undefined]
**signing_urls** | [**Array&lt;ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner&gt;**](ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner.md) |  | [optional] [default to undefined]
**result_url** | **string** |  | [optional] [default to undefined]
**dispatched** | **number** | Gönderilen davet (SMS+e-posta) sayısı | [optional] [default to undefined]
**error** | **string** | status&#x3D;failed ise makinece okunabilir kod (VALIDATION, INSUFFICIENT_CREDITS, MEMBER_LIMIT_EXCEEDED, PARTY_WITHOUT_DOCUMENTS, INVALID_DOCUMENT_SELECTION, TEMPLATE_DOCUMENTS_NOT_READY, DOCUMENT_SOURCE_UNAVAILABLE, RECONCILE_FAILED, CREATE_FAILED vb.) | [optional] [default to undefined]
**message** | **string** | status&#x3D;failed ise açıklama | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsBulkPost200ResponseDataResultsInner } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsBulkPost200ResponseDataResultsInner = {
    row_index,
    status,
    demand_id,
    party_ids,
    template_party_ids,
    details,
    signing_urls,
    result_url,
    dispatched,
    error,
    message,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
