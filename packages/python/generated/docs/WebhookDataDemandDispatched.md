# WebhookDataDemandDispatched


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **UUID** |  |
**title** | **str** |  |
**dispatched_by** | **UUID** | Gönderimi yapan kullanıcının kimliği; belirlenemiyorsa &#x60;null&#x60;. |

## Example

```python
from imzala_client.models.webhook_data_demand_dispatched import WebhookDataDemandDispatched

# TODO update the JSON string below
json = "{}"
# create an instance of WebhookDataDemandDispatched from a JSON string
webhook_data_demand_dispatched_instance = WebhookDataDemandDispatched.from_json(json)
# print the JSON string representation of the object
print(WebhookDataDemandDispatched.to_json())

# convert the object into a dict
webhook_data_demand_dispatched_dict = webhook_data_demand_dispatched_instance.to_dict()
# create an instance of WebhookDataDemandDispatched from a dict
webhook_data_demand_dispatched_from_dict = WebhookDataDemandDispatched.from_dict(webhook_data_demand_dispatched_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
