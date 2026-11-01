

# StandardErrorError


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**code** | **String** | Bilinen değerler: INVALID_CHANNELS, DEMAND_NOT_FOUND, ALREADY_COMPLETED, RATE_LIMITED, MAX_SMS_REMINDERS_REACHED, DEMAND_NOT_DISPATCHED, DEMAND_NOT_DISPATCHABLE, DEMAND_EXPIRED, ENVELOPE_NOT_DISPATCHED, UNAUTHORIZED, INTERNAL_ERROR. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  |
|**message** | **String** |  |  |
|**retryAfterSeconds** | **Integer** | Sadece RATE_LIMITED&#39;de doludur |  [optional] |
