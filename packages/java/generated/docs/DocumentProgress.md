

# DocumentProgress

Belgeye atanmış tarafların karar ilerlemesi. Mühürden ve rızadan bağımsızdır: `COMPLETED`, belgeye atanmış tüm tarafların bu belge için karar verdiğini gösterir (isteğe bağlı bir belgede ret de karardır). 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**status** | [**StatusEnum**](#StatusEnum) | &#x60;PENDING&#x60;: henüz kimse karar vermedi. &#x60;PARTIAL&#x60;: tarafların bir kısmı karar verdi. &#x60;COMPLETED&#x60;: atanmış tüm taraflar karar verdi.  |  [optional] |
|**activeAssignedPartyCount** | **Integer** | Belgeye atanmış ve doldurulmuş (boş bırakılmamış) taraf sayısı. |  [optional] |
|**decidedPartyCount** | **Integer** | Bunlardan bu belge için karar vermiş olanların sayısı. |  [optional] |
|**completedAt** | **OffsetDateTime** | Belgenin kendi tamamlanma anı. Sistem bu anı kaydetmediyse &#x60;null&#x60; döner; zarfın tamamlanma zamanı bu alana kopyalanmaz.  |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| PENDING | &quot;PENDING&quot; |
| PARTIAL | &quot;PARTIAL&quot; |
| COMPLETED | &quot;COMPLETED&quot; |



