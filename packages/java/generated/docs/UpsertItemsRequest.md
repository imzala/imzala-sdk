

# UpsertItemsRequest


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**items** | [**List&lt;PageItem&gt;**](PageItem.md) |  |  |
|**pageIds** | **List&lt;Integer&gt;** | **Opsiyonel.** Verilirse sadece bu sayfaların item&#39;ları replace edilir; diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60; değerleri bu listede olmalıdır. Omitted ise tüm sayfaların item&#39;ları replace edilir.  |  [optional] |



