# WebhookEnvelope

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** | Olay benzersiz id&#39;si. Receiver tarafında idempotency için kullanın (DB&#39;de unique key). |
**type** | **string** | Bilinen değerler: demand.created, demand.completed, demand.expired, party.signed, party.viewed, party.rejected, contract.expiring, contract.ended, contract.advanced. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |
**created_at** | **\DateTime** | Olay zamanı (ISO 8601 UTC). |
**data** | **object** | Olay tipine özel veri (aşağıdaki şemalar) |

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
