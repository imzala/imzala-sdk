# DemandsApi

All URIs are relative to *https://api-prd.imzala.org*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiV1DemandsGet**](#apiv1demandsget) | **GET** /api/v1/demands | Sözleşme listesi (counts-only, PII\&#39;siz)|
|[**apiV1DemandsIdCancelPost**](#apiv1demandsidcancelpost) | **POST** /api/v1/demands/{id}/cancel | Sözleşme iptal (void)|
|[**apiV1DemandsIdCertificateGet**](#apiv1demandsidcertificateget) | **GET** /api/v1/demands/{id}/certificate | Tamamlanma sertifikası (PAdES B-T)|
|[**apiV1DemandsIdDelete**](#apiv1demandsiddelete) | **DELETE** /api/v1/demands/{id} | Sözleşme sil (yalnızca tamamlanmamış)|
|[**apiV1DemandsIdEmbedSessionPost**](#apiv1demandsidembedsessionpost) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint)|
|[**apiV1DemandsIdGet**](#apiv1demandsidget) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi|
|[**apiV1DemandsIdItemsPost**](#apiv1demandsiditemspost) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace)|
|[**apiV1DemandsIdPartiesPartyIdResendPost**](#apiv1demandsidpartiespartyidresendpost) | **POST** /api/v1/demands/{id}/parties/{partyId}/resend | Tekil tarafa imza davetini tekrar gönder|
|[**apiV1DemandsIdPdfGet**](#apiv1demandsidpdfget) | **GET** /api/v1/demands/{id}/pdf | İmzalı sözleşme PDF\&#39;i (auth\&#39;lu indirme)|
|[**apiV1DemandsIdTimelineGet**](#apiv1demandsidtimelineget) | **GET** /api/v1/demands/{id}/timeline | İmza denetim izi (maskeli)|
|[**apiV1DemandsPost**](#apiv1demandspost) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan)|
|[**apiV1DemandsUploadPost**](#apiv1demandsuploadpost) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz)|

# **apiV1DemandsGet**
> ApiV1DemandsGet200Response apiV1DemandsGet()

Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (`parties_total`, `parties_signed`). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için `GET /demands/{id}`. 

### Example

```typescript
import {
    DemandsApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new DemandsApi(configuration);

let status: 'DRAFT' | 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'EXPIRED'; // (optional) (default to undefined)
let q: string; //Başlık araması (optional) (default to undefined)
let from: string; // (optional) (default to undefined)
let to: string; // (optional) (default to undefined)
let templateId: string; // (optional) (default to undefined)
let page: number; // (optional) (default to 1)
let limit: number; //Sayfa boyutu (page_size ile aynı) (optional) (default to 20)
let sort: string; //alan:yön (ör. createdAt:desc) (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1DemandsGet(
    status,
    q,
    from,
    to,
    templateId,
    page,
    limit,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **status** | [**&#39;DRAFT&#39; | &#39;PENDING&#39; | &#39;COMPLETED&#39; | &#39;CANCELLED&#39; | &#39;EXPIRED&#39;**]**Array<&#39;DRAFT&#39; &#124; &#39;PENDING&#39; &#124; &#39;COMPLETED&#39; &#124; &#39;CANCELLED&#39; &#124; &#39;EXPIRED&#39;>** |  | (optional) defaults to undefined|
| **q** | [**string**] | Başlık araması | (optional) defaults to undefined|
| **from** | [**string**] |  | (optional) defaults to undefined|
| **to** | [**string**] |  | (optional) defaults to undefined|
| **templateId** | [**string**] |  | (optional) defaults to undefined|
| **page** | [**number**] |  | (optional) defaults to 1|
| **limit** | [**number**] | Sayfa boyutu (page_size ile aynı) | (optional) defaults to 20|
| **sort** | [**string**] | alan:yön (ör. createdAt:desc) | (optional) defaults to undefined|


### Return type

**ApiV1DemandsGet200Response**

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

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1DemandsIdCancelPost**
> ApiV1DemandsIdCancelPost200Response apiV1DemandsIdCancelPost()

Bekleyen bir sözleşmeyi iptal eder (status=CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 

### Example

```typescript
import {
    DemandsApi,
    Configuration,
    ApiV1DemandsIdCancelPostRequest
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new DemandsApi(configuration);

let id: string; // (default to undefined)
let apiV1DemandsIdCancelPostRequest: ApiV1DemandsIdCancelPostRequest; // (optional)

const { status, data } = await apiInstance.apiV1DemandsIdCancelPost(
    id,
    apiV1DemandsIdCancelPostRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1DemandsIdCancelPostRequest** | **ApiV1DemandsIdCancelPostRequest**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**ApiV1DemandsIdCancelPost200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | İptal edildi |  -  |
|**404** | Kayıt bulunamadı |  -  |
|**409** | Tamamlanmış/iptal edilmiş sözleşme |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1DemandsIdCertificateGet**
> File apiV1DemandsIdCertificateGet()

Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 

### Example

```typescript
import {
    DemandsApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new DemandsApi(configuration);

let id: string; // (default to undefined)
let lang: string; //tr | en (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1DemandsIdCertificateGet(
    id,
    lang
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|
| **lang** | [**string**] | tr | en | (optional) defaults to undefined|


### Return type

**File**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/pdf, application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Sertifika PDF |  -  |
|**404** | Kayıt bulunamadı |  -  |
|**409** | Sözleşme henüz tamamlanmadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1DemandsIdDelete**
> ApiV1TemplatesIdDelete200Response apiV1DemandsIdDelete()

Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API\'den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 `DEMAND_COMPLETED`. 

### Example

```typescript
import {
    DemandsApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new DemandsApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1DemandsIdDelete(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**ApiV1TemplatesIdDelete200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Silindi |  -  |
|**404** | Kayıt bulunamadı |  -  |
|**409** | Tamamlanmış sözleşme silinemez |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1DemandsIdEmbedSessionPost**
> ApiV1DemandsIdEmbedSessionPost200Response apiV1DemandsIdEmbedSessionPost(apiV1DemandsIdEmbedSessionPostRequest)

Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token\'ı üretir. Dönen `embed_url` bir `<iframe>` içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \"güvenli\" veya \"nitelikli\" sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur. - `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış   izin verilen origin\'ler dışından `<iframe>` açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** `X-Workspace-Id` header\'ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace\'in sözleşmesi için 404 döner (IDOR koruması). 

### Example

```typescript
import {
    DemandsApi,
    Configuration,
    ApiV1DemandsIdEmbedSessionPostRequest
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new DemandsApi(configuration);

let id: string; //Sözleşme (demand) ID (default to undefined)
let apiV1DemandsIdEmbedSessionPostRequest: ApiV1DemandsIdEmbedSessionPostRequest; //

const { status, data } = await apiInstance.apiV1DemandsIdEmbedSessionPost(
    id,
    apiV1DemandsIdEmbedSessionPostRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1DemandsIdEmbedSessionPostRequest** | **ApiV1DemandsIdEmbedSessionPostRequest**|  | |
| **id** | [**string**] | Sözleşme (demand) ID | defaults to undefined|


### Return type

**ApiV1DemandsIdEmbedSessionPost200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Token üretildi |  -  |
|**400** | &#x60;party_id&#x60; eksik veya geçersiz format.  |  -  |
|**401** | API key geçersiz veya eksik |  -  |
|**403** | **INSUFFICIENT_SCOPE** — API key\&#39;in &#x60;demands&#x60; scope\&#39;u yok veya gömülü imza özelliği bu API anahtarı için devre dışı.  |  -  |
|**404** | Sözleşme veya taraf bulunamadı. İki durum ayrıştırılmaz (IDOR koruması): - &#x60;Sözleşme bulunamadı&#x60; — demand bu workspace\&#39;te yok - &#x60;Taraf bulunamadı&#x60; — party_id bu demand\&#39;e ait değil  |  -  |
|**409** | Token üretilemez. Olası nedenler: - &#x60;Bu taraf zaten imzaladı&#x60; — taraf imzalamış - &#x60;Bu taraf imzayı reddetti&#x60; — taraf reddetmiş - &#x60;embed_allowed_origins tanımlı değil&#x60; — API anahtarında izin verilen   origin listesi boş; dashboard\&#39;dan API anahtarı düzenleyerek ekleyin.  |  -  |
|**429** | Rate limit aşıldı (per API key + demand + party kombinasyonu) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1DemandsIdGet**
> ApiV1DemandsIdGet200Response apiV1DemandsIdGet()


### Example

```typescript
import {
    DemandsApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new DemandsApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1DemandsIdGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**ApiV1DemandsIdGet200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Başarılı |  -  |
|**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1DemandsIdItemsPost**
> UpsertItemsResponse apiV1DemandsIdItemsPost(upsertItemsRequest)

Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile demand yarat (`dispatch_notifications=false` ile auto-dispatch\'i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - `page_ids` **omitted** → demand\'in TÜM mevcut item\'ları silinir,   body\'dekiler yaratılır (full replace). - `page_ids: [N, M, ...]` → sadece bu sayfaların item\'ları silinir,   diğer sayfalardaki item\'lar korunur. Body\'deki `items[].page_id`   değerleri `page_ids` listesinde olmalıdır.  ### Item type\'ları  | `item_type` | `party_id` zorunlu? | `config` örneği | |-------------|---------------------|-----------------| | `signature` | ✅ | (yok) | | `text` | ❌ | `{ default_content }` | | `dynamic_text` | ✅ | `{ defaultSource: \"{{signer.full_name}}\" }` | | `cells` | ✅ | `{ cellCount: 11, defaultSource: \"{{signer.government_id}}\" }` | | `date` | ✅ | `{ defaultSource, defaultValue }` | | `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` | | `checkbox` | ✅ | `{ checkedByDefault: false }` | | `radio` | ✅ | `{ options: [{label,value}], defaultValue }` | | `stamp` | ❌ | `{ stampData: \"data:image/png;base64,...\" }` |  ### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)  `{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`, `{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`, `{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`, `{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.  ### Workspace izolasyonu  X-API-Key middleware demand\'i workspace\'e göre filtreler; başka workspace\'in demand\'ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`, `REJECTED` için 403.  ### Örnek  ```bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d \'{     \"items\": [       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"signature\",         \"position_x\": 0.5, \"position_y\": 0.85,         \"width\": 0.2, \"height\": 0.05,         \"is_required\": true       },       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"cells\",         \"position_x\": 0.1, \"position_y\": 0.5,         \"width\": 0.4, \"height\": 0.04,         \"slug\": \"tc\",         \"config\": { \"cellCount\": 11, \"defaultSource\": \"{{signer.government_id}}\" }       }     ]   }\' ``` 

### Example

```typescript
import {
    DemandsApi,
    Configuration,
    UpsertItemsRequest
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new DemandsApi(configuration);

let id: string; // (default to undefined)
let upsertItemsRequest: UpsertItemsRequest; //

const { status, data } = await apiInstance.apiV1DemandsIdItemsPost(
    id,
    upsertItemsRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **upsertItemsRequest** | **UpsertItemsRequest**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**UpsertItemsResponse**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Alanlar yerleştirildi |  -  |
|**400** | Validation hatası. Olası &#x60;error&#x60; değerleri: - &#x60;INVALID_ITEMS_BODY&#x60; — items array değil - &#x60;VALIDATION_ERROR&#x60; — position bounds, slug regex, party-required - &#x60;INVALID_PAGE_ID&#x60; — page_id demand\&#39;e ait değil veya page_ids\&#39;te yok - &#x60;INVALID_PARTY_ID&#x60; — party_id demand\&#39;e ait değil  |  -  |
|**401** | API key geçersiz veya eksik |  -  |
|**403** | &#x60;DEMAND_NOT_EDITABLE&#x60; — demand status ≠ &#x60;PENDING&#x60; (COMPLETED, EXPIRED, REJECTED edit edilemez).  |  -  |
|**404** | &#x60;DEMAND_NOT_FOUND&#x60; — demand bu workspace\&#39;te yok (cross-workspace IDOR koruması).  |  -  |
|**409** | &#x60;DUPLICATE_SIGNATURE_FIELD&#x60; — aynı &#x60;(page_id, party_id, position_x, position_y)&#x60; tuple\&#39;ında ikinci &#x60;signature&#x60; alanı yaratıldı. DB-level partial unique constraint engelledi. Pozisyonu değiştirip tekrar deneyin.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1DemandsIdPartiesPartyIdResendPost**
> ApiV1DemandsIdPartiesPartyIdResendPost200Response apiV1DemandsIdPartiesPartyIdResendPost()

Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 

### Example

```typescript
import {
    DemandsApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new DemandsApi(configuration);

let id: string; // (default to undefined)
let partyId: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1DemandsIdPartiesPartyIdResendPost(
    id,
    partyId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|
| **partyId** | [**string**] |  | defaults to undefined|


### Return type

**ApiV1DemandsIdPartiesPartyIdResendPost200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Gönderildi |  -  |
|**404** | Kayıt bulunamadı |  -  |
|**409** | İmzalamış/reddetmiş/sıra-dışı taraf |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1DemandsIdPdfGet**
> File apiV1DemandsIdPdfGet()

Tamamlanmış sözleşmenin imzalı PDF\'ini indirir. Public `/sonuc/{id}/pdf`\'in aksine API key ownership\'i zorunludur. 

### Example

```typescript
import {
    DemandsApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new DemandsApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1DemandsIdPdfGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**File**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/pdf, application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | PDF |  -  |
|**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1DemandsIdTimelineGet**
> ApiV1DemandsIdTimelineGet200Response apiV1DemandsIdTimelineGet()

Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP `ip_masked` (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 

### Example

```typescript
import {
    DemandsApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new DemandsApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1DemandsIdTimelineGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**ApiV1DemandsIdTimelineGet200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Başarılı |  -  |
|**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1DemandsPost**
> ApiV1DemandsPost201Response apiV1DemandsPost(createDemandRequest)

Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field\'ları `variables` payload\'undan doldurur ve imzalama URL\'lerini döner.  **Variable resolution:** - Item\'ın `template_party_id` non-null → `party_mapping[i].variables`\'ta   o slug var ise oradan uygulanır - Yoksa root `variables`\'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value\'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı 

### Example

```typescript
import {
    DemandsApi,
    Configuration,
    CreateDemandRequest
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new DemandsApi(configuration);

let createDemandRequest: CreateDemandRequest; //

const { status, data } = await apiInstance.apiV1DemandsPost(
    createDemandRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **createDemandRequest** | **CreateDemandRequest**|  | |


### Return type

**ApiV1DemandsPost201Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | Sözleşme oluşturuldu |  -  |
|**400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value\&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate\&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
|**401** | API key geçersiz veya eksik |  -  |
|**402** | Yetersiz kredi (INSUFFICIENT_CREDITS) |  -  |
|**403** | **SMS_CUSTOMIZATION_NOT_ALLOWED** — Body\&#39;de &#x60;sms_content&#x60; alanı dolu gönderildi ama çağıran organizasyon PRO/ENTERPRISE planda değil veya kendi SMS sağlayıcı config\&#39;i (sender_name dolu) yok. &#x60;sms_content&#x60; alanını çıkarın veya planınızı yükseltip kendi SMS sağlayıcınızı tanımlayın.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1DemandsUploadPost**
> ApiV1DemandsUploadPost201Response apiV1DemandsUploadPost()

Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF\'e birleştirilir, office formatları LibreOffice ile PDF\'e çevrilir. 

### Example

```typescript
import {
    DemandsApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new DemandsApi(configuration);

let files: Array<File>; //1 belge VEYA 1-20 görsel (default to undefined)
let parties: string; //JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  (default to undefined)
let order: string; //Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\") (optional) (default to undefined)
let title: string; // (optional) (default to undefined)
let description: string; // (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1DemandsUploadPost(
    files,
    parties,
    order,
    title,
    description
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **files** | **Array&lt;File&gt;** | 1 belge VEYA 1-20 görsel | defaults to undefined|
| **parties** | [**string**] | JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  | defaults to undefined|
| **order** | [**string**] | Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) | (optional) defaults to undefined|
| **title** | [**string**] |  | (optional) defaults to undefined|
| **description** | [**string**] |  | (optional) defaults to undefined|


### Return type

**ApiV1DemandsUploadPost201Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | Sözleşme oluşturuldu |  -  |
|**400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value\&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate\&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
|**401** | API key geçersiz veya eksik |  -  |
|**402** | Yetersiz kredi |  -  |
|**413** | Dosya boyut limiti aşıldı (FILE_TOO_LARGE) |  -  |
|**422** | Görsel okunamadı (IMAGE_DECODE_FAILED) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

