

# ApiV1DemandsIdRemindersPost429ResponseError


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**code** | **String** | Bilinen değerler: RATE_LIMITED, MAX_SMS_REMINDERS_REACHED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  [optional] |
|**message** | **String** |  |  [optional] |
|**retryAfterSeconds** | **Integer** | Sadece RATE_LIMITED&#39;de |  [optional] |
