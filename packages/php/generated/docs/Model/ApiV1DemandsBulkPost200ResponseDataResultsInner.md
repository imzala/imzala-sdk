# ApiV1DemandsBulkPost200ResponseDataResultsInner

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**row_index** | **int** |  | [optional]
**status** | **string** |  | [optional]
**demand_id** | **string** | status&#x3D;created ise; veya status&#x3D;failed olup sözleşme oluşturulduktan SONRA kredi mutabakatı reddettiyse (taslak, davet gönderilmedi) | [optional]
**signing_urls** | [**\Imzala\Client\Model\ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner[]**](ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner.md) |  | [optional]
**result_url** | **string** |  | [optional]
**dispatched** | **int** | Gönderilen davet (SMS+e-posta) sayısı | [optional]
**error** | **string** | status&#x3D;failed ise makinece okunabilir kod (VALIDATION, INSUFFICIENT_CREDITS, MEMBER_LIMIT_EXCEEDED, CREATE_FAILED vb.) | [optional]
**message** | **string** | status&#x3D;failed ise açıklama | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
