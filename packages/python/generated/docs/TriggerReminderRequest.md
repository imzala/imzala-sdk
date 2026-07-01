# TriggerReminderRequest

Anlık hatırlatma tetikleme isteği gövdesi (tüm alanlar opsiyonel).

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**channels** | **List[str]** | Bu çağrıda kullanılacak kanal(lar). Yollanmazsa default &#x60;[\&quot;sms\&quot;,\&quot;email\&quot;]&#x60; ile her iki kanalda da gönderilir (parti &#x60;send_sms&#x60;/&#x60;send_email&#x60; toggle&#39;ı + iletişim alanı + demand global toggle uygunluğuna göre).  | [optional] [default to ["sms","email"]]
**force** | **bool** | &#x60;true&#x60; ise 5 dk anti-spam penceresini override eder (son hatırlatma 5 dk içinde gönderilmiş olsa bile gönderir). Production akışlarında yanlışlıkla spam atmamak için sadece kasıtlı admin operasyonlarında kullanın.  | [optional] [default to False]

## Example

```python
from imzala_client.models.trigger_reminder_request import TriggerReminderRequest

# TODO update the JSON string below
json = "{}"
# create an instance of TriggerReminderRequest from a JSON string
trigger_reminder_request_instance = TriggerReminderRequest.from_json(json)
# print the JSON string representation of the object
print(TriggerReminderRequest.to_json())

# convert the object into a dict
trigger_reminder_request_dict = trigger_reminder_request_instance.to_dict()
# create an instance of TriggerReminderRequest from a dict
trigger_reminder_request_from_dict = TriggerReminderRequest.from_dict(trigger_reminder_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


