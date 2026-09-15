

# ApiV1DemandsBulkPost200ResponseDataResultsInner


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**rowIndex** | **Integer** |  |  [optional] |
|**status** | [**StatusEnum**](#StatusEnum) |  |  [optional] |
|**demandId** | **UUID** | status&#x3D;created ise; veya status&#x3D;failed olup sözleşme oluşturulduktan SONRA kredi mutabakatı reddettiyse (taslak, davet gönderilmedi) |  [optional] |
|**signingUrls** | [**List&lt;ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner&gt;**](ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner.md) |  |  [optional] |
|**resultUrl** | **URI** |  |  [optional] |
|**dispatched** | **Integer** | Gönderilen davet (SMS+e-posta) sayısı |  [optional] |
|**error** | **String** | status&#x3D;failed ise makinece okunabilir kod (VALIDATION, INSUFFICIENT_CREDITS, MEMBER_LIMIT_EXCEEDED, CREATE_FAILED vb.) |  [optional] |
|**message** | **String** | status&#x3D;failed ise açıklama |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| CREATED | &quot;created&quot; |
| FAILED | &quot;failed&quot; |



