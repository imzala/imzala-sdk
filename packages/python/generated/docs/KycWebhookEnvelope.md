# KycWebhookEnvelope

KYC olaylarının zarfı. Sözleşme olaylarıyla aynı alanları taşır; `type` enum'u yalnız KYC olaylarını içerir. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | Olay id&#39;si: receiver tarafında idempotency anahtarı. | 
**type** | **str** |  | 
**created_at** | **datetime** | Olay zamanı (ISO 8601 UTC). | 
**data** | **object** | Olay verisi (WebhookDataKycTerminal) | 

## Example

```python
from imzala_client.models.kyc_webhook_envelope import KycWebhookEnvelope

# TODO update the JSON string below
json = "{}"
# create an instance of KycWebhookEnvelope from a JSON string
kyc_webhook_envelope_instance = KycWebhookEnvelope.from_json(json)
# print the JSON string representation of the object
print(KycWebhookEnvelope.to_json())

# convert the object into a dict
kyc_webhook_envelope_dict = kyc_webhook_envelope_instance.to_dict()
# create an instance of KycWebhookEnvelope from a dict
kyc_webhook_envelope_from_dict = KycWebhookEnvelope.from_dict(kyc_webhook_envelope_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


