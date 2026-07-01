# TimestampsApi

All URIs are relative to *https://api-prd.imzala.org*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiV1TimestampsPost**](#apiv1timestampspost) | **POST** /api/v1/timestamps | Zaman damgası oluştur (eser tescil)|

# **apiV1TimestampsPost**
> ApiV1TimestampsPost201Response apiV1TimestampsPost()

Dosyanın SHA-256 hash\'ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - `owner_first_name` / `owner_last_name` alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Nitelikli elektronik imza (QES) için ayrı imzalama akışını kullanın.  **İdempotency:** `Idempotency-Key` header\'ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı `id` döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - `multipart/form-data`: `file` alanıyla ikili dosya yükleme - `application/json`: `file_base64` alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 

### Example

```typescript
import {
    TimestampsApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new TimestampsApi(configuration);

let file: File; //Damgalanacak dosya (maks. 50 MB) (default to undefined)
let idempotencyKey: string; //Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  (optional) (default to undefined)
let description: string; //Kayıt açıklaması (opsiyonel, max 500 karakter) (optional) (default to undefined)
let ownerFirstName: string; //Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional) (default to undefined)
let ownerLastName: string; //Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1TimestampsPost(
    file,
    idempotencyKey,
    description,
    ownerFirstName,
    ownerLastName
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **file** | [**File**] | Damgalanacak dosya (maks. 50 MB) | defaults to undefined|
| **idempotencyKey** | [**string**] | Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  | (optional) defaults to undefined|
| **description** | [**string**] | Kayıt açıklaması (opsiyonel, max 500 karakter) | (optional) defaults to undefined|
| **ownerFirstName** | [**string**] | Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) | (optional) defaults to undefined|
| **ownerLastName** | [**string**] | Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) | (optional) defaults to undefined|


### Return type

**ApiV1TimestampsPost201Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: multipart/form-data, application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | Zaman damgası oluşturuldu |  -  |
|**401** | API key geçersiz veya eksik |  -  |
|**402** | Yetersiz kredi (INSUFFICIENT_CREDITS) |  -  |
|**403** | INSUFFICIENT_SCOPE — API key\&#39;de timestamps scope yok |  -  |
|**422** | İstek içeriği işlenemedi. Olası kodlar: - &#x60;BAD_BASE64&#x60; — &#x60;file_base64&#x60; geçerli standart Base64 değil - &#x60;STAMP_INVALID&#x60; — TSA yanıtı geçersiz zaman damgası döndü  |  -  |
|**429** | Rate limit aşıldı (60 istek/dakika per API key) |  -  |
|**500** | INDETERMINATE — Damga alındı ancak doğrulama sonucu belirsiz. Destek ekibiyle iletişime geçin.  |  -  |
|**503** | TSA_UNAVAILABLE — TÜBİTAK KAMU SM zaman damgası servisi geçici olarak erişilemiyor. Kısa süre sonra tekrar deneyin.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

