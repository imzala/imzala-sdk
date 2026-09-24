# ApiV1DemandsBulkPost200ResponseDataResultsInner

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**row_index** | **int** |  | [optional]
**status** | **string** |  | [optional]
**demand_id** | **string** | status&#x3D;created ise; veya status&#x3D;failed olup sözleşme oluşturulduktan SONRA kredi mutabakatı reddettiyse ya da atamasız imzacı bulunduysa (taslak, davet gönderilmedi) | [optional]
**party_ids** | **string[]** | Yalnız error&#x3D;PARTY_WITHOUT_DOCUMENTS ve sözleşme oluşturulduktan SONRA tespit edildiyse: hiçbir belgeye atanmamış taraf id&#39;leri. | [optional]
**template_party_ids** | **string[]** | Yalnız error&#x3D;PARTY_WITHOUT_DOCUMENTS ve satır oluşturulmadan ÖNCE tespit edildiyse: seçim sonucunda hiçbir belgeye atanmamış kalan şablon rol kimlikleri. | [optional]
**details** | [**\Imzala\Client\Model\ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails**](ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails.md) |  | [optional]
**signing_urls** | [**\Imzala\Client\Model\ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner[]**](ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner.md) |  | [optional]
**result_url** | **string** |  | [optional]
**dispatched** | **int** | Gönderilen davet (SMS+e-posta) sayısı | [optional]
**error** | **string** | status&#x3D;failed ise makinece okunabilir kod (VALIDATION, INSUFFICIENT_CREDITS, MEMBER_LIMIT_EXCEEDED, PARTY_WITHOUT_DOCUMENTS, INVALID_DOCUMENT_SELECTION, INVALID_DOCUMENT_VARIABLES, TEMPLATE_DOCUMENTS_NOT_READY, DOCUMENT_SOURCE_UNAVAILABLE, RECONCILE_FAILED, CREATE_FAILED vb.) | [optional]
**message** | **string** | status&#x3D;failed ise açıklama | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
