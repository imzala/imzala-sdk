# WebhookDataDemandCompletedPartiesInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**name** | **str** | first_name + last_name | [optional] 
**email** | **str** |  | [optional] 
**signed_at** | **datetime** |  | [optional] 

## Example

```python
from imzala_client.models.webhook_data_demand_completed_parties_inner import WebhookDataDemandCompletedPartiesInner

# TODO update the JSON string below
json = "{}"
# create an instance of WebhookDataDemandCompletedPartiesInner from a JSON string
webhook_data_demand_completed_parties_inner_instance = WebhookDataDemandCompletedPartiesInner.from_json(json)
# print the JSON string representation of the object
print(WebhookDataDemandCompletedPartiesInner.to_json())

# convert the object into a dict
webhook_data_demand_completed_parties_inner_dict = webhook_data_demand_completed_parties_inner_instance.to_dict()
# create an instance of WebhookDataDemandCompletedPartiesInner from a dict
webhook_data_demand_completed_parties_inner_from_dict = WebhookDataDemandCompletedPartiesInner.from_dict(webhook_data_demand_completed_parties_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


