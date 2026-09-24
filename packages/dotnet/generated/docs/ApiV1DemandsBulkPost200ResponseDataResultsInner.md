# ImzalaApiClient.Model.ApiV1DemandsBulkPost200ResponseDataResultsInner

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**RowIndex** | **int** |  | [optional] 
**Status** | **string** |  | [optional] 
**DemandId** | **Guid** | status&#x3D;created ise; veya status&#x3D;failed olup sözleşme oluşturulduktan SONRA kredi mutabakatı reddettiyse ya da atamasız imzacı bulunduysa (taslak, davet gönderilmedi) | [optional] 
**PartyIds** | **List&lt;Guid&gt;** | Yalnız error&#x3D;PARTY_WITHOUT_DOCUMENTS ve sözleşme oluşturulduktan SONRA tespit edildiyse: hiçbir belgeye atanmamış taraf id&#39;leri. | [optional] 
**TemplatePartyIds** | **List&lt;Guid&gt;** | Yalnız error&#x3D;PARTY_WITHOUT_DOCUMENTS ve satır oluşturulmadan ÖNCE tespit edildiyse: seçim sonucunda hiçbir belgeye atanmamış kalan şablon rol kimlikleri. | [optional] 
**Details** | [**ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails**](ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails.md) |  | [optional] 
**SigningUrls** | [**List&lt;ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner&gt;**](ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner.md) |  | [optional] 
**ResultUrl** | **string** |  | [optional] 
**Dispatched** | **int** | Gönderilen davet (SMS+e-posta) sayısı | [optional] 
**Error** | **string** | status&#x3D;failed ise makinece okunabilir kod (VALIDATION, INSUFFICIENT_CREDITS, MEMBER_LIMIT_EXCEEDED, PARTY_WITHOUT_DOCUMENTS, INVALID_DOCUMENT_SELECTION, INVALID_DOCUMENT_VARIABLES, TEMPLATE_DOCUMENTS_NOT_READY, DOCUMENT_SOURCE_UNAVAILABLE, RECONCILE_FAILED, CREATE_FAILED vb.) | [optional] 
**Message** | **string** | status&#x3D;failed ise açıklama | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

