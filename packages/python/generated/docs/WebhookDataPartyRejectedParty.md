# WebhookDataPartyRejectedParty


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | 
**first_name** | **str** |  | 
**last_name** | **str** |  | 
**email** | **str** |  | [optional] 
**rejection_reason** | **str** | Tarafın belirttiği sebep (opsiyonel, modal textarea) | [optional] 

## Example

```python
from imzala_client.models.webhook_data_party_rejected_party import WebhookDataPartyRejectedParty

# TODO update the JSON string below
json = "{}"
# create an instance of WebhookDataPartyRejectedParty from a JSON string
webhook_data_party_rejected_party_instance = WebhookDataPartyRejectedParty.from_json(json)
# print the JSON string representation of the object
print(WebhookDataPartyRejectedParty.to_json())

# convert the object into a dict
webhook_data_party_rejected_party_dict = webhook_data_party_rejected_party_instance.to_dict()
# create an instance of WebhookDataPartyRejectedParty from a dict
webhook_data_party_rejected_party_from_dict = WebhookDataPartyRejectedParty.from_dict(webhook_data_party_rejected_party_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


