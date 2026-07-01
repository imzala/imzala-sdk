# WebhookDataPartyRejected


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **UUID** |  | 
**party** | [**WebhookDataPartyRejectedParty**](WebhookDataPartyRejectedParty.md) |  | 
**rejected_at** | **datetime** |  | 

## Example

```python
from imzala_client.models.webhook_data_party_rejected import WebhookDataPartyRejected

# TODO update the JSON string below
json = "{}"
# create an instance of WebhookDataPartyRejected from a JSON string
webhook_data_party_rejected_instance = WebhookDataPartyRejected.from_json(json)
# print the JSON string representation of the object
print(WebhookDataPartyRejected.to_json())

# convert the object into a dict
webhook_data_party_rejected_dict = webhook_data_party_rejected_instance.to_dict()
# create an instance of WebhookDataPartyRejected from a dict
webhook_data_party_rejected_from_dict = WebhookDataPartyRejected.from_dict(webhook_data_party_rejected_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


