# ImzalaApiClient.Model.ApiV1DemandsBulkPost200ResponseDataResultsInner

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**RowIndex** | **int** |  | [optional] 
**Status** | **string** |  | [optional] 
**DemandId** | **Guid** | status&#x3D;created ise; veya status&#x3D;failed olup sözleşme oluşturulduktan SONRA kredi mutabakatı reddettiyse (taslak, davet gönderilmedi) | [optional] 
**SigningUrls** | [**List&lt;ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner&gt;**](ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner.md) |  | [optional] 
**ResultUrl** | **string** |  | [optional] 
**Dispatched** | **int** | Gönderilen davet (SMS+e-posta) sayısı | [optional] 
**Error** | **string** | status&#x3D;failed ise makinece okunabilir kod (VALIDATION, INSUFFICIENT_CREDITS, MEMBER_LIMIT_EXCEEDED, CREATE_FAILED vb.) | [optional] 
**Message** | **string** | status&#x3D;failed ise açıklama | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

