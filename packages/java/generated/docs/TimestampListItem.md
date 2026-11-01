

# TimestampListItem

Zaman damgası liste/detay öğesi. `timestamp_file_url` doğrudan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı olabilir).

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**originalFileName** | **String** |  |  [optional] |
|**originalFileSize** | **Integer** |  |  [optional] |
|**timestampDate** | **OffsetDateTime** |  |  [optional] |
|**status** | **String** | Bilinen değerler: ACTIVE, VERIFIED, EXPIRED, INVALID. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  [optional] |
|**description** | **String** |  |  [optional] |
|**createdAt** | **OffsetDateTime** |  |  [optional] |
|**timestampFileUrl** | **String** |  |  [optional] |
