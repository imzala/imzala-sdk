# CreatedDemandUpload

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional]
**title** | **string** |  | [optional]
**status** | **string** |  | [optional]
**pages** | [**\Imzala\Client\Model\DemandPage[]**](DemandPage.md) | Oluşturulan sözleşmedeki her sayfanın &#x60;id&#x60; ve &#x60;order&#x60; bilgisi. &#x60;POST /api/v1/demands/{id}/items&#x60; endpoint&#39;ine alan yerleştirmek için &#x60;page_id&#x60; parametresi olarak kullanın. | [optional]
**signing_urls** | [**\Imzala\Client\Model\CreatedDemandSigningUrlsInner[]**](CreatedDemandSigningUrlsInner.md) |  | [optional]
**result_url** | **string** |  | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
