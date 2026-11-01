# ImzalaApiClient.Model.KycWebhookEnvelope
KYC olaylarının zarfı. Sözleşme olaylarıyla aynı alanları taşır; `type` enum'u yalnız KYC olaylarını içerir.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Id** | **string** | Olay id&#39;si: receiver tarafında idempotency anahtarı. |
**Type** | **string** | Bilinen değerler: kyc.completed, kyc.failed. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |
**CreatedAt** | **DateTime** | Olay zamanı (ISO 8601 UTC). |
**Data** | **Object** | Olay verisi (WebhookDataKycTerminal) |

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
