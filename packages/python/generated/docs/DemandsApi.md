# imzala_client.DemandsApi

All URIs are relative to *https://api-prd.imzala.org*

Method | HTTP request | Description
------------- | ------------- | -------------
[**api_v1_demands_id_embed_session_post**](DemandsApi.md#api_v1_demands_id_embed_session_post) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint)
[**api_v1_demands_id_get**](DemandsApi.md#api_v1_demands_id_get) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi
[**api_v1_demands_id_items_post**](DemandsApi.md#api_v1_demands_id_items_post) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace)
[**api_v1_demands_post**](DemandsApi.md#api_v1_demands_post) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan)
[**api_v1_demands_upload_post**](DemandsApi.md#api_v1_demands_upload_post) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz)


# **api_v1_demands_id_embed_session_post**
> ApiV1DemandsIdEmbedSessionPost200Response api_v1_demands_id_embed_session_post(id, api_v1_demands_id_embed_session_post_request)

Gömülü imza oturumu başlat (embed token mint)

Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık
gömülü imza token'ı üretir. Dönen `embed_url` bir `<iframe>` içine
yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.

**İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik
İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri)
yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış
nitelikli elektronik imza (QES) üretmez — "güvenli" veya "nitelikli"
sınıf için ayrı QES akışını kullanın.

**Token özellikleri:**
- Tek kullanımlık: imza sayfası açıldığında token tüketilir.
- Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur.
- `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış
  izin verilen origin'ler dışından `<iframe>` açılamaz (409 döner).

**Güvenlik katmanları:**
- B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması)
- B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması)
- K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)

**Workspace izolasyonu:** `X-Workspace-Id` header'ıyla yalnızca çağıran
organizasyonun sözleşmelerine erişilebilir; başka workspace'in sözleşmesi
için 404 döner (IDOR koruması).


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_id_embed_session_post200_response import ApiV1DemandsIdEmbedSessionPost200Response
from imzala_client.models.api_v1_demands_id_embed_session_post_request import ApiV1DemandsIdEmbedSessionPostRequest
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | Sözleşme (demand) ID
    api_v1_demands_id_embed_session_post_request = {"party_id":"f47ac10b-58cc-4372-a567-0e02b2c3d479"} # ApiV1DemandsIdEmbedSessionPostRequest | 

    try:
        # Gömülü imza oturumu başlat (embed token mint)
        api_response = api_instance.api_v1_demands_id_embed_session_post(id, api_v1_demands_id_embed_session_post_request)
        print("The response of DemandsApi->api_v1_demands_id_embed_session_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_id_embed_session_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**| Sözleşme (demand) ID | 
 **api_v1_demands_id_embed_session_post_request** | [**ApiV1DemandsIdEmbedSessionPostRequest**](ApiV1DemandsIdEmbedSessionPostRequest.md)|  | 

### Return type

[**ApiV1DemandsIdEmbedSessionPost200Response**](ApiV1DemandsIdEmbedSessionPost200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Token üretildi |  -  |
**400** | &#x60;party_id&#x60; eksik veya geçersiz format.  |  -  |
**401** | API key geçersiz veya eksik |  -  |
**403** | **INSUFFICIENT_SCOPE** — API key&#39;in &#x60;demands&#x60; scope&#39;u yok veya gömülü imza özelliği bu API anahtarı için devre dışı.  |  -  |
**404** | Sözleşme veya taraf bulunamadı. İki durum ayrıştırılmaz (IDOR koruması): - &#x60;Sözleşme bulunamadı&#x60; — demand bu workspace&#39;te yok - &#x60;Taraf bulunamadı&#x60; — party_id bu demand&#39;e ait değil  |  -  |
**409** | Token üretilemez. Olası nedenler: - &#x60;Bu taraf zaten imzaladı&#x60; — taraf imzalamış - &#x60;Bu taraf imzayı reddetti&#x60; — taraf reddetmiş - &#x60;embed_allowed_origins tanımlı değil&#x60; — API anahtarında izin verilen   origin listesi boş; dashboard&#39;dan API anahtarı düzenleyerek ekleyin.  |  -  |
**429** | Rate limit aşıldı (per API key + demand + party kombinasyonu) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_id_get**
> ApiV1DemandsIdGet200Response api_v1_demands_id_get(id)

Sözleşme durumu + imza ilerlemesi

### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_id_get200_response import ApiV1DemandsIdGet200Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 

    try:
        # Sözleşme durumu + imza ilerlemesi
        api_response = api_instance.api_v1_demands_id_get(id)
        print("The response of DemandsApi->api_v1_demands_id_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_id_get: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 

### Return type

[**ApiV1DemandsIdGet200Response**](ApiV1DemandsIdGet200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Başarılı |  -  |
**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_id_items_post**
> UpsertItemsResponse api_v1_demands_id_items_post(id, upsert_items_request)

Sözleşmeye alan yerleştir (replace)

Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla
yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile
demand yarat (`dispatch_notifications=false` ile auto-dispatch'i
ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden
ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.

### Replace mode

Endpoint **replace** semantiği taşır:
- `page_ids` **omitted** → demand'in TÜM mevcut item'ları silinir,
  body'dekiler yaratılır (full replace).
- `page_ids: [N, M, ...]` → sadece bu sayfaların item'ları silinir,
  diğer sayfalardaki item'lar korunur. Body'deki `items[].page_id`
  değerleri `page_ids` listesinde olmalıdır.

### Item type'ları

| `item_type` | `party_id` zorunlu? | `config` örneği |
|-------------|---------------------|-----------------|
| `signature` | ✅ | (yok) |
| `text` | ❌ | `{ default_content }` |
| `dynamic_text` | ✅ | `{ defaultSource: "{{signer.full_name}}" }` |
| `cells` | ✅ | `{ cellCount: 11, defaultSource: "{{signer.government_id}}" }` |
| `date` | ✅ | `{ defaultSource, defaultValue }` |
| `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` |
| `checkbox` | ✅ | `{ checkedByDefault: false }` |
| `radio` | ✅ | `{ options: [{label,value}], defaultValue }` |
| `stamp` | ❌ | `{ stampData: "data:image/png;base64,..." }` |

### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)

`{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`,
`{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`,
`{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`,
`{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.

### Workspace izolasyonu

X-API-Key middleware demand'i workspace'e göre filtreler;
başka workspace'in demand'ine item ekleyemezsiniz (404 döner).

### Status kontrolü

Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`,
`REJECTED` için 403.

### Örnek

```bash
curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \
  -H "X-API-Key: imz_..." \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "page_id": 12345,
        "party_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
        "item_type": "signature",
        "position_x": 0.5, "position_y": 0.85,
        "width": 0.2, "height": 0.05,
        "is_required": true
      },
      {
        "page_id": 12345,
        "party_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
        "item_type": "cells",
        "position_x": 0.1, "position_y": 0.5,
        "width": 0.4, "height": 0.04,
        "slug": "tc",
        "config": { "cellCount": 11, "defaultSource": "{{signer.government_id}}" }
      }
    ]
  }'
```


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.upsert_items_request import UpsertItemsRequest
from imzala_client.models.upsert_items_response import UpsertItemsResponse
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    upsert_items_request = imzala_client.UpsertItemsRequest() # UpsertItemsRequest | 

    try:
        # Sözleşmeye alan yerleştir (replace)
        api_response = api_instance.api_v1_demands_id_items_post(id, upsert_items_request)
        print("The response of DemandsApi->api_v1_demands_id_items_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_id_items_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 
 **upsert_items_request** | [**UpsertItemsRequest**](UpsertItemsRequest.md)|  | 

### Return type

[**UpsertItemsResponse**](UpsertItemsResponse.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Alanlar yerleştirildi |  -  |
**400** | Validation hatası. Olası &#x60;error&#x60; değerleri: - &#x60;INVALID_ITEMS_BODY&#x60; — items array değil - &#x60;VALIDATION_ERROR&#x60; — position bounds, slug regex, party-required - &#x60;INVALID_PAGE_ID&#x60; — page_id demand&#39;e ait değil veya page_ids&#39;te yok - &#x60;INVALID_PARTY_ID&#x60; — party_id demand&#39;e ait değil  |  -  |
**401** | API key geçersiz veya eksik |  -  |
**403** | &#x60;DEMAND_NOT_EDITABLE&#x60; — demand status ≠ &#x60;PENDING&#x60; (COMPLETED, EXPIRED, REJECTED edit edilemez).  |  -  |
**404** | &#x60;DEMAND_NOT_FOUND&#x60; — demand bu workspace&#39;te yok (cross-workspace IDOR koruması).  |  -  |
**409** | &#x60;DUPLICATE_SIGNATURE_FIELD&#x60; — aynı &#x60;(page_id, party_id, position_x, position_y)&#x60; tuple&#39;ında ikinci &#x60;signature&#x60; alanı yaratıldı. DB-level partial unique constraint engelledi. Pozisyonu değiştirip tekrar deneyin.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_post**
> ApiV1DemandsPost201Response api_v1_demands_post(create_demand_request)

Sözleşme oluştur (şablondan)

Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini
kaydeder, dynamic field'ları `variables` payload'undan doldurur ve
imzalama URL'lerini döner.

**Variable resolution:**
- Item'ın `template_party_id` non-null → `party_mapping[i].variables`'ta
  o slug var ise oradan uygulanır
- Yoksa root `variables`'tan fallback
- Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,
  `editable: true` ise)

**Validation:**
- `party_mapping[i].variables` ve root `variables` object olmalı
- Variable value'ları `string | number | boolean | null` olmalı
  (object/array reject)
- `template_party_id` party_mapping içinde unique olmalı


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_post201_response import ApiV1DemandsPost201Response
from imzala_client.models.create_demand_request import CreateDemandRequest
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    create_demand_request = {"template_id":"7ec4b653-e84a-47f1-9e0b-7671e1aae2a1","title":"Vize Danışmanlığı - Ada Kalkan","party_mapping":[{"template_party_id":"e5b4e0cb-c2d5-473f-9f62-44d51c76f56e","first_name":"Ada","last_name":"Kalkan","email":"ada@example.com","phone":"+905304636743","government_id":"36747474747","variables":{"adres":"Atatürk Cad. No: 12, Çankaya/Ankara","danismanlik_ucreti":"5.000 TL","danismanlik_notlar":"Schengen vize başvuru danışmanlığı","randevu_takibi_ucreti":"1.500 TL","randevu_takibi_notlar":"Konsolosluk randevu takibi 60 gün","genel_toplam":"6.500 TL"}}]} # CreateDemandRequest | 

    try:
        # Sözleşme oluştur (şablondan)
        api_response = api_instance.api_v1_demands_post(create_demand_request)
        print("The response of DemandsApi->api_v1_demands_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **create_demand_request** | [**CreateDemandRequest**](CreateDemandRequest.md)|  | 

### Return type

[**ApiV1DemandsPost201Response**](ApiV1DemandsPost201Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**201** | Sözleşme oluşturuldu |  -  |
**400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
**401** | API key geçersiz veya eksik |  -  |
**402** | Yetersiz kredi (INSUFFICIENT_CREDITS) |  -  |
**403** | **SMS_CUSTOMIZATION_NOT_ALLOWED** — Body&#39;de &#x60;sms_content&#x60; alanı dolu gönderildi ama çağıran organizasyon PRO/ENTERPRISE planda değil veya kendi SMS sağlayıcı config&#39;i (sender_name dolu) yok. &#x60;sms_content&#x60; alanını çıkarın veya planınızı yükseltip kendi SMS sağlayıcınızı tanımlayın.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_upload_post**
> ApiV1DemandsUploadPost201Response api_v1_demands_upload_post(files, parties, order=order, title=title, description=description)

Dosya upload ile sözleşme oluştur (şablonsuz)

Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur
(şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel
(JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF'e
birleştirilir, office formatları LibreOffice ile PDF'e çevrilir.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_upload_post201_response import ApiV1DemandsUploadPost201Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    files = None # List[bytes] | 1 belge VEYA 1-20 görsel
    parties = 'parties_example' # str | JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu). 
    order = 'order_example' # str | Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\") (optional)
    title = 'title_example' # str |  (optional)
    description = 'description_example' # str |  (optional)

    try:
        # Dosya upload ile sözleşme oluştur (şablonsuz)
        api_response = api_instance.api_v1_demands_upload_post(files, parties, order=order, title=title, description=description)
        print("The response of DemandsApi->api_v1_demands_upload_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_upload_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **files** | **List[bytes]**| 1 belge VEYA 1-20 görsel | 
 **parties** | **str**| JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  | 
 **order** | **str**| Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) | [optional] 
 **title** | **str**|  | [optional] 
 **description** | **str**|  | [optional] 

### Return type

[**ApiV1DemandsUploadPost201Response**](ApiV1DemandsUploadPost201Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**201** | Sözleşme oluşturuldu |  -  |
**400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
**401** | API key geçersiz veya eksik |  -  |
**402** | Yetersiz kredi |  -  |
**413** | Dosya boyut limiti aşıldı (FILE_TOO_LARGE) |  -  |
**422** | Görsel okunamadı (IMAGE_DECODE_FAILED) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

