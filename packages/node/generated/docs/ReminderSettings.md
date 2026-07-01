# ReminderSettings

Hatırlatma yapılandırması. Şablon (`Template.reminder_*`) ve sözleşme (`ReminderConfig`) arasında aynı şemaya sahiptir. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**enabled** | **boolean** | false → hiç hatırlatma gönderilmez (cron schedule edilmez) | [optional] [default to true]
**intervals_hours** | **Array&lt;number&gt;** | Sözleşmenin oluşturulduğu andan itibaren saat cinsinden hatırlatma aralıkları. Örn. &#x60;[24, 72, 168]&#x60; → 24 saat sonra, 3 gün sonra ve 7 gün sonra. &#x60;max_reminders&#x60; ile limit edilir.  | [optional] [default to undefined]
**max_reminders** | **number** | Bir parti için maksimum gönderilecek hatırlatma sayısı | [optional] [default to 1]
**channels** | **Array&lt;string&gt;** | Hatırlatma gönderim kanalı. Birden fazla seçilebilir (&#x60;[\&quot;email\&quot;,\&quot;sms\&quot;]&#x60;). SMS kanalı için partinin &#x60;phone&#x60; alanı dolu ve &#x60;send_sms: true&#x60; olmalı; email kanalı için &#x60;email&#x60; dolu ve &#x60;send_email: true&#x60; olmalı, ayrıca demand\&#39;in &#x60;send_sms_notifications&#x60; / &#x60;send_email_notifications&#x60; global toggle\&#39;ı açık olmalı.  | [optional] [default to undefined]

## Example

```typescript
import { ReminderSettings } from '@imzala/server-sdk-node';

const instance: ReminderSettings = {
    enabled,
    intervals_hours,
    max_reminders,
    channels,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
