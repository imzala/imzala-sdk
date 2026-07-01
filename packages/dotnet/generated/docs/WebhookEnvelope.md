# ImzalaApiClient.Model.WebhookEnvelope
Tüm webhook payload'larının ortak zarfı. `data` alanı olay tipine göre değişir; her olay için ayrı veri şeması yukarıda dokümante. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Id** | **string** | Olay benzersiz id&#39;si. Receiver tarafında idempotency için kullanın (DB&#39;de unique key).  | 
**Type** | **string** |  | 
**CreatedAt** | **DateTime** | Olay zamanı (ISO 8601 UTC). | 
**Data** | **Object** | Olay tipine özel veri (aşağıdaki şemalar) | 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

