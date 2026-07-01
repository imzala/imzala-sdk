# CreatedDemandUpload


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**title** | **string** |  | [optional] [default to undefined]
**status** | **string** |  | [optional] [default to undefined]
**pages** | [**Array&lt;DemandPage&gt;**](DemandPage.md) | Oluşturulan sözleşmedeki her sayfanın &#x60;id&#x60; ve &#x60;order&#x60; bilgisi. &#x60;POST /api/v1/demands/{id}/items&#x60; endpoint\&#39;ine alan yerleştirmek için &#x60;page_id&#x60; parametresi olarak kullanın.  | [optional] [default to undefined]
**signing_urls** | [**Array&lt;CreatedDemandSigningUrlsInner&gt;**](CreatedDemandSigningUrlsInner.md) |  | [optional] [default to undefined]
**result_url** | **string** |  | [optional] [default to undefined]

## Example

```typescript
import { CreatedDemandUpload } from '@imzala/server-sdk-node';

const instance: CreatedDemandUpload = {
    id,
    title,
    status,
    pages,
    signing_urls,
    result_url,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
