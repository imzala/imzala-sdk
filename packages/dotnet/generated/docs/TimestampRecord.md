# ImzalaApiClient.Model.TimestampRecord

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Id** | **Guid** |  | [optional] 
**TimestampTime** | **DateTime** | TÜBİTAK KAMU SM&#39;nin onayladığı damga zamanı (UTC) | [optional] 
**TsaAuthority** | **string** | Zaman damgası sağlayıcısı | [optional] 
**FileSha256** | **string** | Damgalanan dosyanın SHA-256 hash değeri (hex, 64 karakter) | [optional] 
**VerifyUrl** | **string** | Damgayı doğrulamak için URL | [optional] 
**CertificateUrl** | **string** | Damga sertifikası URL&#39;i | [optional] 
**CreditsUsed** | **int** | Bu damga için harcanan kredi miktarı | [optional] 
**CreditsRemaining** | **int** | İşlem sonrası kalan kredi bakiyesi | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

