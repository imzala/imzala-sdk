# WebhookDataPartySigned


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **UUID** |  |
**party_id** | **UUID** |  |
**external_ref** | **str** | Sözleşmenin bildirim anındaki takip numarası; tanımlı değilse &#x60;null&#x60;. Takip numarası tekrar edebilir: alıcı tarafta tekrar işlemeyi zarftaki olay &#x60;id&#x60;&#39;si ile önleyin, &#x60;external_ref&#x60; ile değil. Sözleşme bilgisi okunamadığında bildirim bu alan olmadan gönderilir.  | [optional]
**backfill** | **bool** | Replay bayrağı | [optional]

## Example

```python
from imzala_client.models.webhook_data_party_signed import WebhookDataPartySigned

# TODO update the JSON string below
json = "{}"
# create an instance of WebhookDataPartySigned from a JSON string
webhook_data_party_signed_instance = WebhookDataPartySigned.from_json(json)
# print the JSON string representation of the object
print(WebhookDataPartySigned.to_json())

# convert the object into a dict
webhook_data_party_signed_dict = webhook_data_party_signed_instance.to_dict()
# create an instance of WebhookDataPartySigned from a dict
webhook_data_party_signed_from_dict = WebhookDataPartySigned.from_dict(webhook_data_party_signed_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
