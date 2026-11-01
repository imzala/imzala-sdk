

# KycWebhookEnvelope

KYC olaylarının zarfı. Sözleşme olaylarıyla aynı alanları taşır; `type` enum'u yalnız KYC olaylarını içerir.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **String** | Olay id&#39;si: receiver tarafında idempotency anahtarı. |  |
|**type** | **String** | Bilinen değerler: kyc.completed, kyc.failed. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  |
|**createdAt** | **OffsetDateTime** | Olay zamanı (ISO 8601 UTC). |  |
|**data** | **Object** | Olay verisi (WebhookDataKycTerminal) |  |
