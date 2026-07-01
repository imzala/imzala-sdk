# DemandsApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**apiV1DemandsIdEmbedSessionPost**](DemandsApi.md#apiV1DemandsIdEmbedSessionPost) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint) |
| [**apiV1DemandsIdEmbedSessionPostWithHttpInfo**](DemandsApi.md#apiV1DemandsIdEmbedSessionPostWithHttpInfo) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint) |
| [**apiV1DemandsIdGet**](DemandsApi.md#apiV1DemandsIdGet) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi |
| [**apiV1DemandsIdGetWithHttpInfo**](DemandsApi.md#apiV1DemandsIdGetWithHttpInfo) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi |
| [**apiV1DemandsIdItemsPost**](DemandsApi.md#apiV1DemandsIdItemsPost) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace) |
| [**apiV1DemandsIdItemsPostWithHttpInfo**](DemandsApi.md#apiV1DemandsIdItemsPostWithHttpInfo) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace) |
| [**apiV1DemandsPost**](DemandsApi.md#apiV1DemandsPost) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan) |
| [**apiV1DemandsPostWithHttpInfo**](DemandsApi.md#apiV1DemandsPostWithHttpInfo) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan) |
| [**apiV1DemandsUploadPost**](DemandsApi.md#apiV1DemandsUploadPost) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz) |
| [**apiV1DemandsUploadPostWithHttpInfo**](DemandsApi.md#apiV1DemandsUploadPostWithHttpInfo) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz) |



## apiV1DemandsIdEmbedSessionPost

> ApiV1DemandsIdEmbedSessionPost200Response apiV1DemandsIdEmbedSessionPost(id, apiV1DemandsIdEmbedSessionPostRequest)

Gömülü imza oturumu başlat (embed token mint)

Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | Sözleşme (demand) ID
        ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest = new ApiV1DemandsIdEmbedSessionPostRequest(); // ApiV1DemandsIdEmbedSessionPostRequest | 
        try {
            ApiV1DemandsIdEmbedSessionPost200Response result = apiInstance.apiV1DemandsIdEmbedSessionPost(id, apiV1DemandsIdEmbedSessionPostRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdEmbedSessionPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**| Sözleşme (demand) ID | |
| **apiV1DemandsIdEmbedSessionPostRequest** | [**ApiV1DemandsIdEmbedSessionPostRequest**](ApiV1DemandsIdEmbedSessionPostRequest.md)|  | |

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

## apiV1DemandsIdEmbedSessionPostWithHttpInfo

> ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response> apiV1DemandsIdEmbedSessionPostWithHttpInfo(id, apiV1DemandsIdEmbedSessionPostRequest)

Gömülü imza oturumu başlat (embed token mint)

Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | Sözleşme (demand) ID
        ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest = new ApiV1DemandsIdEmbedSessionPostRequest(); // ApiV1DemandsIdEmbedSessionPostRequest | 
        try {
            ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response> response = apiInstance.apiV1DemandsIdEmbedSessionPostWithHttpInfo(id, apiV1DemandsIdEmbedSessionPostRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdEmbedSessionPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**| Sözleşme (demand) ID | |
| **apiV1DemandsIdEmbedSessionPostRequest** | [**ApiV1DemandsIdEmbedSessionPostRequest**](ApiV1DemandsIdEmbedSessionPostRequest.md)|  | |

### Return type

ApiResponse<[**ApiV1DemandsIdEmbedSessionPost200Response**](ApiV1DemandsIdEmbedSessionPost200Response.md)>


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


## apiV1DemandsIdGet

> ApiV1DemandsIdGet200Response apiV1DemandsIdGet(id)

Sözleşme durumu + imza ilerlemesi

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiV1DemandsIdGet200Response result = apiInstance.apiV1DemandsIdGet(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

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

## apiV1DemandsIdGetWithHttpInfo

> ApiResponse<ApiV1DemandsIdGet200Response> apiV1DemandsIdGetWithHttpInfo(id)

Sözleşme durumu + imza ilerlemesi

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiResponse<ApiV1DemandsIdGet200Response> response = apiInstance.apiV1DemandsIdGetWithHttpInfo(id);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

### Return type

ApiResponse<[**ApiV1DemandsIdGet200Response**](ApiV1DemandsIdGet200Response.md)>


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


## apiV1DemandsIdItemsPost

> UpsertItemsResponse apiV1DemandsIdItemsPost(id, upsertItemsRequest)

Sözleşmeye alan yerleştir (replace)

Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |-------------|---------------------|-----------------| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        UpsertItemsRequest upsertItemsRequest = new UpsertItemsRequest(); // UpsertItemsRequest | 
        try {
            UpsertItemsResponse result = apiInstance.apiV1DemandsIdItemsPost(id, upsertItemsRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdItemsPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **upsertItemsRequest** | [**UpsertItemsRequest**](UpsertItemsRequest.md)|  | |

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

## apiV1DemandsIdItemsPostWithHttpInfo

> ApiResponse<UpsertItemsResponse> apiV1DemandsIdItemsPostWithHttpInfo(id, upsertItemsRequest)

Sözleşmeye alan yerleştir (replace)

Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |-------------|---------------------|-----------------| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        UpsertItemsRequest upsertItemsRequest = new UpsertItemsRequest(); // UpsertItemsRequest | 
        try {
            ApiResponse<UpsertItemsResponse> response = apiInstance.apiV1DemandsIdItemsPostWithHttpInfo(id, upsertItemsRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdItemsPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **upsertItemsRequest** | [**UpsertItemsRequest**](UpsertItemsRequest.md)|  | |

### Return type

ApiResponse<[**UpsertItemsResponse**](UpsertItemsResponse.md)>


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


## apiV1DemandsPost

> ApiV1DemandsPost201Response apiV1DemandsPost(createDemandRequest)

Sözleşme oluştur (şablondan)

Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        CreateDemandRequest createDemandRequest = new CreateDemandRequest(); // CreateDemandRequest | 
        try {
            ApiV1DemandsPost201Response result = apiInstance.apiV1DemandsPost(createDemandRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **createDemandRequest** | [**CreateDemandRequest**](CreateDemandRequest.md)|  | |

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

## apiV1DemandsPostWithHttpInfo

> ApiResponse<ApiV1DemandsPost201Response> apiV1DemandsPostWithHttpInfo(createDemandRequest)

Sözleşme oluştur (şablondan)

Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        CreateDemandRequest createDemandRequest = new CreateDemandRequest(); // CreateDemandRequest | 
        try {
            ApiResponse<ApiV1DemandsPost201Response> response = apiInstance.apiV1DemandsPostWithHttpInfo(createDemandRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **createDemandRequest** | [**CreateDemandRequest**](CreateDemandRequest.md)|  | |

### Return type

ApiResponse<[**ApiV1DemandsPost201Response**](ApiV1DemandsPost201Response.md)>


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


## apiV1DemandsUploadPost

> ApiV1DemandsUploadPost201Response apiV1DemandsUploadPost(files, parties, order, title, description)

Dosya upload ile sözleşme oluştur (şablonsuz)

Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        List<File> files = Arrays.asList(); // List<File> | 1 belge VEYA 1-20 görsel
        String parties = "parties_example"; // String | JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu). 
        String order = "order_example"; // String | Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\")
        String title = "title_example"; // String | 
        String description = "description_example"; // String | 
        try {
            ApiV1DemandsUploadPost201Response result = apiInstance.apiV1DemandsUploadPost(files, parties, order, title, description);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsUploadPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **files** | **List&lt;File&gt;**| 1 belge VEYA 1-20 görsel | |
| **parties** | **String**| JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  | |
| **order** | **String**| Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) | [optional] |
| **title** | **String**|  | [optional] |
| **description** | **String**|  | [optional] |

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

## apiV1DemandsUploadPostWithHttpInfo

> ApiResponse<ApiV1DemandsUploadPost201Response> apiV1DemandsUploadPostWithHttpInfo(files, parties, order, title, description)

Dosya upload ile sözleşme oluştur (şablonsuz)

Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        List<File> files = Arrays.asList(); // List<File> | 1 belge VEYA 1-20 görsel
        String parties = "parties_example"; // String | JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu). 
        String order = "order_example"; // String | Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\")
        String title = "title_example"; // String | 
        String description = "description_example"; // String | 
        try {
            ApiResponse<ApiV1DemandsUploadPost201Response> response = apiInstance.apiV1DemandsUploadPostWithHttpInfo(files, parties, order, title, description);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsUploadPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **files** | **List&lt;File&gt;**| 1 belge VEYA 1-20 görsel | |
| **parties** | **String**| JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  | |
| **order** | **String**| Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) | [optional] |
| **title** | **String**|  | [optional] |
| **description** | **String**|  | [optional] |

### Return type

ApiResponse<[**ApiV1DemandsUploadPost201Response**](ApiV1DemandsUploadPost201Response.md)>


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

