# TriggerReminderRequest

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**channels** | **string[]** | Bu çağrıda kullanılacak kanal(lar). Yollanmazsa default &#x60;[\&quot;sms\&quot;,\&quot;email\&quot;]&#x60; ile her iki kanalda da gönderilir (parti &#x60;send_sms&#x60;/&#x60;send_email&#x60; toggle&#39;ı + iletişim alanı + demand global toggle uygunluğuna göre). | [optional]
**force** | **bool** | &#x60;true&#x60; ise 5 dk anti-spam penceresini override eder (son hatırlatma 5 dk içinde gönderilmiş olsa bile gönderir). Production akışlarında yanlışlıkla spam atmamak için sadece kasıtlı admin operasyonlarında kullanın. | [optional] [default to false]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
