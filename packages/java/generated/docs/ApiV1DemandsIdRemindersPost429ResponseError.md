

# ApiV1DemandsIdRemindersPost429ResponseError


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**code** | [**CodeEnum**](#CodeEnum) |  |  [optional] |
|**message** | **String** |  |  [optional] |
|**retryAfterSeconds** | **Integer** | Sadece RATE_LIMITED&#39;de |  [optional] |



## Enum: CodeEnum

| Name | Value |
|---- | -----|
| RATE_LIMITED | &quot;RATE_LIMITED&quot; |
| MAX_SMS_REMINDERS_REACHED | &quot;MAX_SMS_REMINDERS_REACHED&quot; |



