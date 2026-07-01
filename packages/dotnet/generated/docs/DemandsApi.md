# ImzalaApiClient.Api.DemandsApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|--------|--------------|-------------|
| [**ApiV1DemandsIdEmbedSessionPost**](DemandsApi.md#apiv1demandsidembedsessionpost) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint) |
| [**ApiV1DemandsIdGet**](DemandsApi.md#apiv1demandsidget) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi |
| [**ApiV1DemandsIdItemsPost**](DemandsApi.md#apiv1demandsiditemspost) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace) |
| [**ApiV1DemandsPost**](DemandsApi.md#apiv1demandspost) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan) |
| [**ApiV1DemandsUploadPost**](DemandsApi.md#apiv1demandsuploadpost) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz) |

<a id="apiv1demandsidembedsessionpost"></a>
# **ApiV1DemandsIdEmbedSessionPost**
> ApiV1DemandsIdEmbedSessionPost200Response ApiV1DemandsIdEmbedSessionPost (Guid id, ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest)

Gömülü imza oturumu başlat (embed token mint)

Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token'ı üretir. Dönen `embed_url` bir `<iframe>` içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \"güvenli\" veya \"nitelikli\" sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur. - `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış   izin verilen origin'ler dışından `<iframe>` açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** `X-Workspace-Id` header'ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace'in sözleşmesi için 404 döner (IDOR koruması). 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdEmbedSessionPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | Sözleşme (demand) ID
            var apiV1DemandsIdEmbedSessionPostRequest = new ApiV1DemandsIdEmbedSessionPostRequest(); // ApiV1DemandsIdEmbedSessionPostRequest | 

            try
            {
                // Gömülü imza oturumu başlat (embed token mint)
                ApiV1DemandsIdEmbedSessionPost200Response result = apiInstance.ApiV1DemandsIdEmbedSessionPost(id, apiV1DemandsIdEmbedSessionPostRequest);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdEmbedSessionPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdEmbedSessionPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Gömülü imza oturumu başlat (embed token mint)
    ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response> response = apiInstance.ApiV1DemandsIdEmbedSessionPostWithHttpInfo(id, apiV1DemandsIdEmbedSessionPostRequest);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdEmbedSessionPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** | Sözleşme (demand) ID |  |
| **apiV1DemandsIdEmbedSessionPostRequest** | [**ApiV1DemandsIdEmbedSessionPostRequest**](ApiV1DemandsIdEmbedSessionPostRequest.md) |  |  |

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
| **200** | Token üretildi |  -  |
| **400** | &#x60;party_id&#x60; eksik veya geçersiz format.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **403** | **INSUFFICIENT_SCOPE** — API key&#39;in &#x60;demands&#x60; scope&#39;u yok veya gömülü imza özelliği bu API anahtarı için devre dışı.  |  -  |
| **404** | Sözleşme veya taraf bulunamadı. İki durum ayrıştırılmaz (IDOR koruması): - &#x60;Sözleşme bulunamadı&#x60; — demand bu workspace&#39;te yok - &#x60;Taraf bulunamadı&#x60; — party_id bu demand&#39;e ait değil  |  -  |
| **409** | Token üretilemez. Olası nedenler: - &#x60;Bu taraf zaten imzaladı&#x60; — taraf imzalamış - &#x60;Bu taraf imzayı reddetti&#x60; — taraf reddetmiş - &#x60;embed_allowed_origins tanımlı değil&#x60; — API anahtarında izin verilen   origin listesi boş; dashboard&#39;dan API anahtarı düzenleyerek ekleyin.  |  -  |
| **429** | Rate limit aşıldı (per API key + demand + party kombinasyonu) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsidget"></a>
# **ApiV1DemandsIdGet**
> ApiV1DemandsIdGet200Response ApiV1DemandsIdGet (Guid id)

Sözleşme durumu + imza ilerlemesi

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdGetExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | 

            try
            {
                // Sözleşme durumu + imza ilerlemesi
                ApiV1DemandsIdGet200Response result = apiInstance.ApiV1DemandsIdGet(id);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Sözleşme durumu + imza ilerlemesi
    ApiResponse<ApiV1DemandsIdGet200Response> response = apiInstance.ApiV1DemandsIdGetWithHttpInfo(id);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** |  |  |

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
| **200** | Başarılı |  -  |
| **404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsiditemspost"></a>
# **ApiV1DemandsIdItemsPost**
> UpsertItemsResponse ApiV1DemandsIdItemsPost (Guid id, UpsertItemsRequest upsertItemsRequest)

Sözleşmeye alan yerleştir (replace)

Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile demand yarat (`dispatch_notifications=false` ile auto-dispatch'i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - `page_ids` **omitted** → demand'in TÜM mevcut item'ları silinir,   body'dekiler yaratılır (full replace). - `page_ids: [N, M, ...]` → sadece bu sayfaların item'ları silinir,   diğer sayfalardaki item'lar korunur. Body'deki `items[].page_id`   değerleri `page_ids` listesinde olmalıdır.  ### Item type'ları  | `item_type` | `party_id` zorunlu? | `config` örneği | |- -- -- -- -- -- --|- -- -- -- -- -- -- -- -- -- --|- -- -- -- -- -- -- -- --| | `signature` | ✅ | (yok) | | `text` | ❌ | `{ default_content }` | | `dynamic_text` | ✅ | `{ defaultSource: \"{{signer.full_name}}\" }` | | `cells` | ✅ | `{ cellCount: 11, defaultSource: \"{{signer.government_id}}\" }` | | `date` | ✅ | `{ defaultSource, defaultValue }` | | `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` | | `checkbox` | ✅ | `{ checkedByDefault: false }` | | `radio` | ✅ | `{ options: [{label,value}], defaultValue }` | | `stamp` | ❌ | `{ stampData: \"data:image/png;base64,...\" }` |  ### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)  `{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`, `{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`, `{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`, `{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.  ### Workspace izolasyonu  X-API-Key middleware demand'i workspace'e göre filtreler; başka workspace'in demand'ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`, `REJECTED` için 403.  ### Örnek  ```bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{     \"items\": [       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"signature\",         \"position_x\": 0.5, \"position_y\": 0.85,         \"width\": 0.2, \"height\": 0.05,         \"is_required\": true       },       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"cells\",         \"position_x\": 0.1, \"position_y\": 0.5,         \"width\": 0.4, \"height\": 0.04,         \"slug\": \"tc\",         \"config\": { \"cellCount\": 11, \"defaultSource\": \"{{signer.government_id}}\" }       }     ]   }' ``` 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdItemsPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | 
            var upsertItemsRequest = new UpsertItemsRequest(); // UpsertItemsRequest | 

            try
            {
                // Sözleşmeye alan yerleştir (replace)
                UpsertItemsResponse result = apiInstance.ApiV1DemandsIdItemsPost(id, upsertItemsRequest);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdItemsPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdItemsPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Sözleşmeye alan yerleştir (replace)
    ApiResponse<UpsertItemsResponse> response = apiInstance.ApiV1DemandsIdItemsPostWithHttpInfo(id, upsertItemsRequest);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdItemsPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** |  |  |
| **upsertItemsRequest** | [**UpsertItemsRequest**](UpsertItemsRequest.md) |  |  |

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
| **200** | Alanlar yerleştirildi |  -  |
| **400** | Validation hatası. Olası &#x60;error&#x60; değerleri: - &#x60;INVALID_ITEMS_BODY&#x60; — items array değil - &#x60;VALIDATION_ERROR&#x60; — position bounds, slug regex, party-required - &#x60;INVALID_PAGE_ID&#x60; — page_id demand&#39;e ait değil veya page_ids&#39;te yok - &#x60;INVALID_PARTY_ID&#x60; — party_id demand&#39;e ait değil  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **403** | &#x60;DEMAND_NOT_EDITABLE&#x60; — demand status ≠ &#x60;PENDING&#x60; (COMPLETED, EXPIRED, REJECTED edit edilemez).  |  -  |
| **404** | &#x60;DEMAND_NOT_FOUND&#x60; — demand bu workspace&#39;te yok (cross-workspace IDOR koruması).  |  -  |
| **409** | &#x60;DUPLICATE_SIGNATURE_FIELD&#x60; — aynı &#x60;(page_id, party_id, position_x, position_y)&#x60; tuple&#39;ında ikinci &#x60;signature&#x60; alanı yaratıldı. DB-level partial unique constraint engelledi. Pozisyonu değiştirip tekrar deneyin.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandspost"></a>
# **ApiV1DemandsPost**
> ApiV1DemandsPost201Response ApiV1DemandsPost (CreateDemandRequest createDemandRequest)

Sözleşme oluştur (şablondan)

Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field'ları `variables` payload'undan doldurur ve imzalama URL'lerini döner.  **Variable resolution:** - Item'ın `template_party_id` non-null → `party_mapping[i].variables`'ta   o slug var ise oradan uygulanır - Yoksa root `variables`'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var createDemandRequest = new CreateDemandRequest(); // CreateDemandRequest | 

            try
            {
                // Sözleşme oluştur (şablondan)
                ApiV1DemandsPost201Response result = apiInstance.ApiV1DemandsPost(createDemandRequest);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Sözleşme oluştur (şablondan)
    ApiResponse<ApiV1DemandsPost201Response> response = apiInstance.ApiV1DemandsPostWithHttpInfo(createDemandRequest);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **createDemandRequest** | [**CreateDemandRequest**](CreateDemandRequest.md) |  |  |

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
| **201** | Sözleşme oluşturuldu |  -  |
| **400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **402** | Yetersiz kredi (INSUFFICIENT_CREDITS) |  -  |
| **403** | **SMS_CUSTOMIZATION_NOT_ALLOWED** — Body&#39;de &#x60;sms_content&#x60; alanı dolu gönderildi ama çağıran organizasyon PRO/ENTERPRISE planda değil veya kendi SMS sağlayıcı config&#39;i (sender_name dolu) yok. &#x60;sms_content&#x60; alanını çıkarın veya planınızı yükseltip kendi SMS sağlayıcınızı tanımlayın.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsuploadpost"></a>
# **ApiV1DemandsUploadPost**
> ApiV1DemandsUploadPost201Response ApiV1DemandsUploadPost (List<FileParameter> files, string parties, string? order = null, string? title = null, string? description = null)

Dosya upload ile sözleşme oluştur (şablonsuz)

Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF'e birleştirilir, office formatları LibreOffice ile PDF'e çevrilir. 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsUploadPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var files = new List<FileParameter>(); // List<FileParameter> | 1 belge VEYA 1-20 görsel
            var parties = "parties_example";  // string | JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu). 
            var order = "order_example";  // string? | Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\") (optional) 
            var title = "title_example";  // string? |  (optional) 
            var description = "description_example";  // string? |  (optional) 

            try
            {
                // Dosya upload ile sözleşme oluştur (şablonsuz)
                ApiV1DemandsUploadPost201Response result = apiInstance.ApiV1DemandsUploadPost(files, parties, order, title, description);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsUploadPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsUploadPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Dosya upload ile sözleşme oluştur (şablonsuz)
    ApiResponse<ApiV1DemandsUploadPost201Response> response = apiInstance.ApiV1DemandsUploadPostWithHttpInfo(files, parties, order, title, description);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsUploadPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **files** | **List&lt;FileParameter&gt;** | 1 belge VEYA 1-20 görsel |  |
| **parties** | **string** | JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  |  |
| **order** | **string?** | Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) | [optional]  |
| **title** | **string?** |  | [optional]  |
| **description** | **string?** |  | [optional]  |

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
| **201** | Sözleşme oluşturuldu |  -  |
| **400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **402** | Yetersiz kredi |  -  |
| **413** | Dosya boyut limiti aşıldı (FILE_TOO_LARGE) |  -  |
| **422** | Görsel okunamadı (IMAGE_DECODE_FAILED) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

