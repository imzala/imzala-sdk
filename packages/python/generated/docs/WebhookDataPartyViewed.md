# WebhookDataPartyViewed


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **UUID** |  | 
**party_id** | **UUID** |  | 

## Example

```python
from imzala_client.models.webhook_data_party_viewed import WebhookDataPartyViewed

# TODO update the JSON string below
json = "{}"
# create an instance of WebhookDataPartyViewed from a JSON string
webhook_data_party_viewed_instance = WebhookDataPartyViewed.from_json(json)
# print the JSON string representation of the object
print(WebhookDataPartyViewed.to_json())

# convert the object into a dict
webhook_data_party_viewed_dict = webhook_data_party_viewed_instance.to_dict()
# create an instance of WebhookDataPartyViewed from a dict
webhook_data_party_viewed_from_dict = WebhookDataPartyViewed.from_dict(webhook_data_party_viewed_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


