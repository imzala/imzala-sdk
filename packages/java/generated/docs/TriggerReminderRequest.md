

# TriggerReminderRequest

Anlık hatırlatma tetikleme isteği gövdesi (tüm alanlar opsiyonel).

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**channels** | [**List&lt;ChannelsEnum&gt;**](#List&lt;ChannelsEnum&gt;) | Bu çağrıda kullanılacak kanal(lar). Yollanmazsa default &#x60;[\&quot;sms\&quot;,\&quot;email\&quot;]&#x60; ile her iki kanalda da gönderilir (parti &#x60;send_sms&#x60;/&#x60;send_email&#x60; toggle&#39;ı + iletişim alanı + demand global toggle uygunluğuna göre).  |  [optional] |
|**force** | **Boolean** | &#x60;true&#x60; ise 5 dk anti-spam penceresini override eder (son hatırlatma 5 dk içinde gönderilmiş olsa bile gönderir). Production akışlarında yanlışlıkla spam atmamak için sadece kasıtlı admin operasyonlarında kullanın.  |  [optional] |



## Enum: List&lt;ChannelsEnum&gt;

| Name | Value |
|---- | -----|
| EMAIL | &quot;email&quot; |
| SMS | &quot;sms&quot; |



