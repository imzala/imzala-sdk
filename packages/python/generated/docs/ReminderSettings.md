# ReminderSettings

Hatırlatma yapılandırması. Şablon (`Template.reminder_*`) ve sözleşme (`ReminderConfig`) arasında aynı şemaya sahiptir. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**enabled** | **bool** | false → hiç hatırlatma gönderilmez (cron schedule edilmez) | [optional] [default to True]
**intervals_hours** | **List[int]** | Sözleşmenin oluşturulduğu andan itibaren saat cinsinden hatırlatma aralıkları. Örn. &#x60;[24, 72, 168]&#x60; → 24 saat sonra, 3 gün sonra ve 7 gün sonra. &#x60;max_reminders&#x60; ile limit edilir.  | [optional] [default to [48]]
**max_reminders** | **int** | Bir parti için maksimum gönderilecek hatırlatma sayısı | [optional] [default to 1]
**channels** | **List[str]** | Hatırlatma gönderim kanalı. Birden fazla seçilebilir (&#x60;[\&quot;email\&quot;,\&quot;sms\&quot;]&#x60;). SMS kanalı için partinin &#x60;phone&#x60; alanı dolu ve &#x60;send_sms: true&#x60; olmalı; email kanalı için &#x60;email&#x60; dolu ve &#x60;send_email: true&#x60; olmalı, ayrıca demand&#39;in &#x60;send_sms_notifications&#x60; / &#x60;send_email_notifications&#x60; global toggle&#39;ı açık olmalı.  | [optional] [default to ["email"]]

## Example

```python
from imzala_client.models.reminder_settings import ReminderSettings

# TODO update the JSON string below
json = "{}"
# create an instance of ReminderSettings from a JSON string
reminder_settings_instance = ReminderSettings.from_json(json)
# print the JSON string representation of the object
print(ReminderSettings.to_json())

# convert the object into a dict
reminder_settings_dict = reminder_settings_instance.to_dict()
# create an instance of ReminderSettings from a dict
reminder_settings_from_dict = ReminderSettings.from_dict(reminder_settings_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


