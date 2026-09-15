

# TimestampListItem

Zaman damgası liste/detay öğesi. `timestamp_file_url` doğrudan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı olabilir). 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**originalFileName** | **String** |  |  [optional] |
|**originalFileSize** | **Integer** |  |  [optional] |
|**timestampDate** | **OffsetDateTime** |  |  [optional] |
|**status** | [**StatusEnum**](#StatusEnum) |  |  [optional] |
|**description** | **String** |  |  [optional] |
|**createdAt** | **OffsetDateTime** |  |  [optional] |
|**timestampFileUrl** | **String** |  |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| ACTIVE | &quot;ACTIVE&quot; |
| VERIFIED | &quot;VERIFIED&quot; |
| EXPIRED | &quot;EXPIRED&quot; |
| INVALID | &quot;INVALID&quot; |



