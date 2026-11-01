# KycWebhookEnvelope

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** | Olay id&#39;si: receiver tarafında idempotency anahtarı. |
**type** | **string** | Bilinen değerler: kyc.completed, kyc.failed. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |
**created_at** | **\DateTime** | Olay zamanı (ISO 8601 UTC). |
**data** | **object** | Olay verisi (WebhookDataKycTerminal) |

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
