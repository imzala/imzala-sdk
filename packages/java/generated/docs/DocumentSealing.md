

# DocumentSealing

Belgenin mühür durumu. Karar ilerlemesinden ayrı bir olgudur.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**status** | [**StatusEnum**](#StatusEnum) |  |  [optional] |
|**hasTimestamp** | **Boolean** | Yalnız &#x60;status&#x60; &#x60;SEALED&#x60; iken &#x60;true&#x60; olabilir. |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| NONE | &quot;NONE&quot; |
| PENDING | &quot;PENDING&quot; |
| SEALED | &quot;SEALED&quot; |
| FAILED | &quot;FAILED&quot; |



