# WebhookDataKycTerminalStepsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**method** | **str** |  | [optional] 
**verified** | **bool** | Adım sonucu; adım hiç tamamlanmadıysa &#x60;null&#x60;. | [optional] 

## Example

```python
from imzala_client.models.webhook_data_kyc_terminal_steps_inner import WebhookDataKycTerminalStepsInner

# TODO update the JSON string below
json = "{}"
# create an instance of WebhookDataKycTerminalStepsInner from a JSON string
webhook_data_kyc_terminal_steps_inner_instance = WebhookDataKycTerminalStepsInner.from_json(json)
# print the JSON string representation of the object
print(WebhookDataKycTerminalStepsInner.to_json())

# convert the object into a dict
webhook_data_kyc_terminal_steps_inner_dict = webhook_data_kyc_terminal_steps_inner_instance.to_dict()
# create an instance of WebhookDataKycTerminalStepsInner from a dict
webhook_data_kyc_terminal_steps_inner_from_dict = WebhookDataKycTerminalStepsInner.from_dict(webhook_data_kyc_terminal_steps_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


