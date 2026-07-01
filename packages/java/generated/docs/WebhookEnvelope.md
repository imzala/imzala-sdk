

# WebhookEnvelope

Tüm webhook payload'larının ortak zarfı. `data` alanı olay tipine göre değişir; her olay için ayrı veri şeması yukarıda dokümante. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **String** | Olay benzersiz id&#39;si. Receiver tarafında idempotency için kullanın (DB&#39;de unique key).  |  |
|**type** | [**TypeEnum**](#TypeEnum) |  |  |
|**createdAt** | **OffsetDateTime** | Olay zamanı (ISO 8601 UTC). |  |
|**data** | **Object** | Olay tipine özel veri (aşağıdaki şemalar) |  |



## Enum: TypeEnum

| Name | Value |
|---- | -----|
| DEMAND_CREATED | &quot;demand.created&quot; |
| DEMAND_COMPLETED | &quot;demand.completed&quot; |
| DEMAND_EXPIRED | &quot;demand.expired&quot; |
| PARTY_SIGNED | &quot;party.signed&quot; |
| PARTY_VIEWED | &quot;party.viewed&quot; |
| PARTY_REJECTED | &quot;party.rejected&quot; |



