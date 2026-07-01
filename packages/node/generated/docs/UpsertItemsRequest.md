# UpsertItemsRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**items** | [**Array&lt;PageItem&gt;**](PageItem.md) |  | [default to undefined]
**page_ids** | **Array&lt;number&gt;** | **Opsiyonel.** Verilirse sadece bu sayfaların item\&#39;ları replace edilir; diğer sayfalardaki item\&#39;lar korunur. Body\&#39;deki &#x60;items[].page_id&#x60; değerleri bu listede olmalıdır. Omitted ise tüm sayfaların item\&#39;ları replace edilir.  | [optional] [default to undefined]

## Example

```typescript
import { UpsertItemsRequest } from '@imzala/server-sdk-node';

const instance: UpsertItemsRequest = {
    items,
    page_ids,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
