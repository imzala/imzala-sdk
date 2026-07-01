# ImzalaApiClient.Model.CreatedDemandUpload

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Id** | **Guid** |  | [optional] 
**Title** | **string** |  | [optional] 
**Status** | **string** |  | [optional] 
**Pages** | [**List&lt;DemandPage&gt;**](DemandPage.md) | Oluşturulan sözleşmedeki her sayfanın &#x60;id&#x60; ve &#x60;order&#x60; bilgisi. &#x60;POST /api/v1/demands/{id}/items&#x60; endpoint&#39;ine alan yerleştirmek için &#x60;page_id&#x60; parametresi olarak kullanın.  | [optional] 
**SigningUrls** | [**List&lt;CreatedDemandSigningUrlsInner&gt;**](CreatedDemandSigningUrlsInner.md) |  | [optional] 
**ResultUrl** | **string** |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

