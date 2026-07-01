# WebhookDataDemandExpiredPartiesInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**name** | **str** |  | [optional] 
**email** | **str** |  | [optional] 
**signed** | **bool** | Süre dolduğunda imzalamış mıydı? | [optional] 

## Example

```python
from imzala_client.models.webhook_data_demand_expired_parties_inner import WebhookDataDemandExpiredPartiesInner

# TODO update the JSON string below
json = "{}"
# create an instance of WebhookDataDemandExpiredPartiesInner from a JSON string
webhook_data_demand_expired_parties_inner_instance = WebhookDataDemandExpiredPartiesInner.from_json(json)
# print the JSON string representation of the object
print(WebhookDataDemandExpiredPartiesInner.to_json())

# convert the object into a dict
webhook_data_demand_expired_parties_inner_dict = webhook_data_demand_expired_parties_inner_instance.to_dict()
# create an instance of WebhookDataDemandExpiredPartiesInner from a dict
webhook_data_demand_expired_parties_inner_from_dict = WebhookDataDemandExpiredPartiesInner.from_dict(webhook_data_demand_expired_parties_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


