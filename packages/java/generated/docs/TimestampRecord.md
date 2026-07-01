

# TimestampRecord


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**timestampTime** | **OffsetDateTime** | TÜBİTAK KAMU SM&#39;nin onayladığı damga zamanı (UTC) |  [optional] |
|**tsaAuthority** | **String** | Zaman damgası sağlayıcısı |  [optional] |
|**fileSha256** | **String** | Damgalanan dosyanın SHA-256 hash değeri (hex, 64 karakter) |  [optional] |
|**verifyUrl** | **URI** | Damgayı doğrulamak için URL |  [optional] |
|**certificateUrl** | **URI** | Damga sertifikası URL&#39;i |  [optional] |
|**creditsUsed** | **Integer** | Bu damga için harcanan kredi miktarı |  [optional] |
|**creditsRemaining** | **Integer** | İşlem sonrası kalan kredi bakiyesi |  [optional] |



