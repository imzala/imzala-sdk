

# ApiV1DemandsBulkPost200ResponseDataResultsInner


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**rowIndex** | **Integer** |  |  [optional] |
|**status** | [**StatusEnum**](#StatusEnum) |  |  [optional] |
|**demandId** | **UUID** | status&#x3D;created ise; veya status&#x3D;failed olup sözleşme oluşturulduktan SONRA kredi mutabakatı reddettiyse ya da atamasız imzacı bulunduysa (taslak, davet gönderilmedi) |  [optional] |
|**partyIds** | **List&lt;UUID&gt;** | Yalnız error&#x3D;PARTY_WITHOUT_DOCUMENTS ve sözleşme oluşturulduktan SONRA tespit edildiyse: hiçbir belgeye atanmamış taraf id&#39;leri. |  [optional] |
|**templatePartyIds** | **List&lt;UUID&gt;** | Yalnız error&#x3D;PARTY_WITHOUT_DOCUMENTS ve satır oluşturulmadan ÖNCE tespit edildiyse: seçim sonucunda hiçbir belgeye atanmamış kalan şablon rol kimlikleri. |  [optional] |
|**details** | [**ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails**](ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails.md) |  |  [optional] |
|**signingUrls** | [**List&lt;ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner&gt;**](ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner.md) |  |  [optional] |
|**resultUrl** | **URI** |  |  [optional] |
|**dispatched** | **Integer** | Gönderilen davet (SMS+e-posta) sayısı |  [optional] |
|**error** | **String** | status&#x3D;failed ise makinece okunabilir kod (VALIDATION, INVALID_LOCALE, INSUFFICIENT_CREDITS, MEMBER_LIMIT_EXCEEDED, PARTY_WITHOUT_DOCUMENTS, INVALID_DOCUMENT_SELECTION, INVALID_DOCUMENT_VARIABLES, TEMPLATE_DOCUMENTS_NOT_READY, DOCUMENT_SOURCE_UNAVAILABLE, RECONCILE_FAILED, CREATE_FAILED vb.) |  [optional] |
|**message** | **String** | status&#x3D;failed ise açıklama |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| CREATED | &quot;created&quot; |
| FAILED | &quot;failed&quot; |



