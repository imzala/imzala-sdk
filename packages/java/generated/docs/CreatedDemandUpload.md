

# CreatedDemandUpload


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**title** | **String** |  |  [optional] |
|**status** | **String** |  |  [optional] |
|**pages** | [**List&lt;DemandPage&gt;**](DemandPage.md) | Oluşturulan sözleşmedeki her sayfanın &#x60;id&#x60; ve &#x60;order&#x60; bilgisi. &#x60;POST /api/v1/demands/{id}/items&#x60; endpoint&#39;ine alan yerleştirmek için &#x60;page_id&#x60; parametresi olarak kullanın.  |  [optional] |
|**signingUrls** | [**List&lt;CreatedDemandSigningUrlsInner&gt;**](CreatedDemandSigningUrlsInner.md) |  |  [optional] |
|**resultUrl** | **URI** |  |  [optional] |



