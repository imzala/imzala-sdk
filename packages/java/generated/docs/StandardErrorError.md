

# StandardErrorError


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**code** | [**CodeEnum**](#CodeEnum) |  |  |
|**message** | **String** |  |  |
|**retryAfterSeconds** | **Integer** | Sadece RATE_LIMITED&#39;de doludur |  [optional] |



## Enum: CodeEnum

| Name | Value |
|---- | -----|
| INVALID_CHANNELS | &quot;INVALID_CHANNELS&quot; |
| DEMAND_NOT_FOUND | &quot;DEMAND_NOT_FOUND&quot; |
| ALREADY_COMPLETED | &quot;ALREADY_COMPLETED&quot; |
| RATE_LIMITED | &quot;RATE_LIMITED&quot; |
| MAX_SMS_REMINDERS_REACHED | &quot;MAX_SMS_REMINDERS_REACHED&quot; |
| DEMAND_NOT_DISPATCHED | &quot;DEMAND_NOT_DISPATCHED&quot; |
| DEMAND_NOT_DISPATCHABLE | &quot;DEMAND_NOT_DISPATCHABLE&quot; |
| DEMAND_EXPIRED | &quot;DEMAND_EXPIRED&quot; |
| ENVELOPE_NOT_DISPATCHED | &quot;ENVELOPE_NOT_DISPATCHED&quot; |
| UNAUTHORIZED | &quot;UNAUTHORIZED&quot; |
| INTERNAL_ERROR | &quot;INTERNAL_ERROR&quot; |



