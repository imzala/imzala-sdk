

# KycWebhookEnvelope

KYC olaylarının zarfı. Sözleşme olaylarıyla aynı alanları taşır; `type` enum'u yalnız KYC olaylarını içerir. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **String** | Olay id&#39;si: receiver tarafında idempotency anahtarı. |  |
|**type** | [**TypeEnum**](#TypeEnum) |  |  |
|**createdAt** | **OffsetDateTime** | Olay zamanı (ISO 8601 UTC). |  |
|**data** | **Object** | Olay verisi (WebhookDataKycTerminal) |  |



## Enum: TypeEnum

| Name | Value |
|---- | -----|
| KYC_COMPLETED | &quot;kyc.completed&quot; |
| KYC_FAILED | &quot;kyc.failed&quot; |



