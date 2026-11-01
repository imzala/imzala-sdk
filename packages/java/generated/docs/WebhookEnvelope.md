

# WebhookEnvelope

Tüm webhook payload'larının ortak zarfı. `data` alanı olay tipine göre değişir; her olay için ayrı veri şeması yukarıda dokümante.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **String** | Olay benzersiz id&#39;si. Receiver tarafında idempotency için kullanın (DB&#39;de unique key).  |  |
|**type** | **String** | Bilinen değerler: demand.created, demand.completed, demand.expired, party.signed, party.viewed, party.rejected, contract.expiring, contract.ended, contract.advanced. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  |
|**createdAt** | **OffsetDateTime** | Olay zamanı (ISO 8601 UTC). |  |
|**data** | **Object** | Olay tipine özel veri (aşağıdaki şemalar) |  |
