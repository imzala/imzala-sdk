# ImzalaApiClient.Model.UpsertItemsRequest

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Items** | [**List&lt;PageItem&gt;**](PageItem.md) |  | 
**PageIds** | **List&lt;int&gt;** | **Opsiyonel.** Verilirse sadece bu sayfaların item&#39;ları replace edilir; diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60; değerleri bu listede olmalıdır. Omitted ise tüm sayfaların item&#39;ları replace edilir.  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

