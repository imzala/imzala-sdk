# WebhookEnvelope

Tüm webhook payload'larının ortak zarfı. `data` alanı olay tipine göre değişir; her olay için ayrı veri şeması yukarıda dokümante. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | Olay benzersiz id&#39;si. Receiver tarafında idempotency için kullanın (DB&#39;de unique key).  | 
**type** | **str** |  | 
**created_at** | **datetime** | Olay zamanı (ISO 8601 UTC). | 
**data** | **object** | Olay tipine özel veri (aşağıdaki şemalar) | 

## Example

```python
from imzala_client.models.webhook_envelope import WebhookEnvelope

# TODO update the JSON string below
json = "{}"
# create an instance of WebhookEnvelope from a JSON string
webhook_envelope_instance = WebhookEnvelope.from_json(json)
# print the JSON string representation of the object
print(WebhookEnvelope.to_json())

# convert the object into a dict
webhook_envelope_dict = webhook_envelope_instance.to_dict()
# create an instance of WebhookEnvelope from a dict
webhook_envelope_from_dict = WebhookEnvelope.from_dict(webhook_envelope_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


