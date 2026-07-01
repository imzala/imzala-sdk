# TimestampRecord


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**timestamp_time** | **string** | TÜBİTAK KAMU SM\&#39;nin onayladığı damga zamanı (UTC) | [optional] [default to undefined]
**tsa_authority** | **string** | Zaman damgası sağlayıcısı | [optional] [default to undefined]
**file_sha256** | **string** | Damgalanan dosyanın SHA-256 hash değeri (hex, 64 karakter) | [optional] [default to undefined]
**verify_url** | **string** | Damgayı doğrulamak için URL | [optional] [default to undefined]
**certificate_url** | **string** | Damga sertifikası URL\&#39;i | [optional] [default to undefined]
**credits_used** | **number** | Bu damga için harcanan kredi miktarı | [optional] [default to undefined]
**credits_remaining** | **number** | İşlem sonrası kalan kredi bakiyesi | [optional] [default to undefined]

## Example

```typescript
import { TimestampRecord } from '@imzala/server-sdk-node';

const instance: TimestampRecord = {
    id,
    timestamp_time,
    tsa_authority,
    file_sha256,
    verify_url,
    certificate_url,
    credits_used,
    credits_remaining,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
