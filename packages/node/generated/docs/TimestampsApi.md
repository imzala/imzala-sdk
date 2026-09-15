# TimestampsApi

All URIs are relative to *https://api-prd.imzala.org*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiV1TimestampsGet**](#apiv1timestampsget) | **GET** /api/v1/timestamps | Zaman damgası listesi|
|[**apiV1TimestampsIdGet**](#apiv1timestampsidget) | **GET** /api/v1/timestamps/{id} | Zaman damgası detayı|
|[**apiV1TimestampsPost**](#apiv1timestampspost) | **POST** /api/v1/timestamps | Zaman damgası oluştur (eser tescil)|

# **apiV1TimestampsGet**
> ApiV1TimestampsGet200Response apiV1TimestampsGet()

Hesabın (workspace) manuel oluşturduğu zaman damgalarını listeler. Sözleşme tamamlanınca otomatik üretilen damgalar bu listede GÖSTERİLMEZ (onlar ilgili sözleşme sayfasından indirilir). Yalnızca API key\'in kendi workspace\'indeki kayıtlar döner (IDOR-safe).  `timestamp_file_url` doğrudan tarayıcıdan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı / şifreli olabilir). 

### Example

```typescript
import {
    TimestampsApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new TimestampsApi(configuration);

let page: number; // (optional) (default to 1)
let limit: number; // (optional) (default to 25)
let q: string; //Dosya adı / açıklamada arama (max 100 karakter) (optional) (default to undefined)
let status: string; //Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional) (default to undefined)
let from: string; //Damga günü alt sınırı (YYYY-MM-DD) (optional) (default to undefined)
let to: string; //Damga günü üst sınırı (YYYY-MM-DD) (optional) (default to undefined)
let sort: string; //Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (\'-\' öneki azalan) (optional) (default to '-createdAt')

const { status, data } = await apiInstance.apiV1TimestampsGet(
    page,
    limit,
    q,
    status,
    from,
    to,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **page** | [**number**] |  | (optional) defaults to 1|
| **limit** | [**number**] |  | (optional) defaults to 25|
| **q** | [**string**] | Dosya adı / açıklamada arama (max 100 karakter) | (optional) defaults to undefined|
| **status** | [**string**] | Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID | (optional) defaults to undefined|
| **from** | [**string**] | Damga günü alt sınırı (YYYY-MM-DD) | (optional) defaults to undefined|
| **to** | [**string**] | Damga günü üst sınırı (YYYY-MM-DD) | (optional) defaults to undefined|
| **sort** | [**string**] | Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (\&#39;-\&#39; öneki azalan) | (optional) defaults to '-createdAt'|


### Return type

**ApiV1TimestampsGet200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Başarılı |  -  |
|**401** | API key geçersiz veya eksik |  -  |
|**403** | INSUFFICIENT_SCOPE — API key\&#39;de timestamps scope yok |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TimestampsIdGet**
> ApiV1TimestampsIdGet200Response apiV1TimestampsIdGet()

Tek bir zaman damgası kaydının detayını döner. Yalnızca API key\'in kendi workspace\'indeki kayıt getirilebilir (IDOR-safe); başka bir workspace\'in kaydı 404 döner. 

### Example

```typescript
import {
    TimestampsApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new TimestampsApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1TimestampsIdGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**ApiV1TimestampsIdGet200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Başarılı |  -  |
|**401** | API key geçersiz veya eksik |  -  |
|**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

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
|**415** | UNSUPPORTED_MIME_TYPE — Yüklenen dosyanın MIME türü zaman damgası allowlist\&#39;inde değil (belge/görsel/ses/video dışı ya da içeriksiz &#x60;application/octet-stream&#x60;). İstemci doğru content-type göndermeli.  |  -  |
|**422** | İstek içeriği işlenemedi. Olası kodlar: - &#x60;BAD_BASE64&#x60; — &#x60;file_base64&#x60; geçerli standart Base64 değil - &#x60;STAMP_INVALID&#x60; — TSA yanıtı geçersiz zaman damgası döndü  |  -  |
|**429** | &#x60;RATE_LIMIT_EXCEEDED&#x60;: API anahtarı başına dakikada 10 zaman damgası isteği sınırı aşıldı. Genel dakikada 60 istek sınırı da ayrıca geçerlidir. |  -  |
|**500** | INDETERMINATE — Damga alındı ancak doğrulama sonucu belirsiz. Destek ekibiyle iletişime geçin.  |  -  |
|**503** | TSA_UNAVAILABLE — TÜBİTAK KAMU SM zaman damgası servisi geçici olarak erişilemiyor. Kısa süre sonra tekrar deneyin.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

