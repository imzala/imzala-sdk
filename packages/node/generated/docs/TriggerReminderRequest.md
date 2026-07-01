# TriggerReminderRequest

Anlık hatırlatma tetikleme isteği gövdesi (tüm alanlar opsiyonel).

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**channels** | **Array&lt;string&gt;** | Bu çağrıda kullanılacak kanal(lar). Yollanmazsa default &#x60;[\&quot;sms\&quot;,\&quot;email\&quot;]&#x60; ile her iki kanalda da gönderilir (parti &#x60;send_sms&#x60;/&#x60;send_email&#x60; toggle\&#39;ı + iletişim alanı + demand global toggle uygunluğuna göre).  | [optional] [default to undefined]
**force** | **boolean** | &#x60;true&#x60; ise 5 dk anti-spam penceresini override eder (son hatırlatma 5 dk içinde gönderilmiş olsa bile gönderir). Production akışlarında yanlışlıkla spam atmamak için sadece kasıtlı admin operasyonlarında kullanın.  | [optional] [default to false]

## Example

```typescript
import { TriggerReminderRequest } from '@imzala/server-sdk-node';

const instance: TriggerReminderRequest = {
    channels,
    force,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
