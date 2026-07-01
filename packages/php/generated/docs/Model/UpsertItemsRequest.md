# UpsertItemsRequest

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**items** | [**\Imzala\Client\Model\PageItem[]**](PageItem.md) |  |
**page_ids** | **int[]** | **Opsiyonel.** Verilirse sadece bu sayfaların item&#39;ları replace edilir; diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60; değerleri bu listede olmalıdır. Omitted ise tüm sayfaların item&#39;ları replace edilir. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
