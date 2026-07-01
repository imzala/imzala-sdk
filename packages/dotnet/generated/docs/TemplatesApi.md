# ImzalaApiClient.Api.TemplatesApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|--------|--------------|-------------|
| [**ApiV1TemplatesGet**](TemplatesApi.md#apiv1templatesget) | **GET** /api/v1/templates | Şablon listesi |
| [**ApiV1TemplatesIdGet**](TemplatesApi.md#apiv1templatesidget) | **GET** /api/v1/templates/{id} | Şablon detay |
| [**ApiV1TemplatesIdUsageGet**](TemplatesApi.md#apiv1templatesidusageget) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek) |

<a id="apiv1templatesget"></a>
# **ApiV1TemplatesGet**
> ApiV1TemplatesGet200Response ApiV1TemplatesGet (int? page = null, int? limit = null)

Şablon listesi

Aktif şablonlarınızı listeler.

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
    public class ApiV1TemplatesGetExample
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
            var apiInstance = new TemplatesApi(httpClient, config, httpClientHandler);
            var page = 1;  // int? |  (optional)  (default to 1)
            var limit = 20;  // int? |  (optional)  (default to 20)

            try
            {
                // Şablon listesi
                ApiV1TemplatesGet200Response result = apiInstance.ApiV1TemplatesGet(page, limit);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling TemplatesApi.ApiV1TemplatesGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1TemplatesGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Şablon listesi
    ApiResponse<ApiV1TemplatesGet200Response> response = apiInstance.ApiV1TemplatesGetWithHttpInfo(page, limit);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling TemplatesApi.ApiV1TemplatesGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **page** | **int?** |  | [optional] [default to 1] |
| **limit** | **int?** |  | [optional] [default to 20] |

### Return type

[**ApiV1TemplatesGet200Response**](ApiV1TemplatesGet200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Başarılı |  -  |
| **401** | API key geçersiz veya eksik |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1templatesidget"></a>
# **ApiV1TemplatesIdGet**
> ApiV1TemplatesIdGet200Response ApiV1TemplatesIdGet (Guid id)

Şablon detay

Şablonun parties + variables bilgisini döner. variables array'ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 

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
    public class ApiV1TemplatesIdGetExample
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
            var apiInstance = new TemplatesApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | 

            try
            {
                // Şablon detay
                ApiV1TemplatesIdGet200Response result = apiInstance.ApiV1TemplatesIdGet(id);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling TemplatesApi.ApiV1TemplatesIdGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1TemplatesIdGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Şablon detay
    ApiResponse<ApiV1TemplatesIdGet200Response> response = apiInstance.ApiV1TemplatesIdGetWithHttpInfo(id);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling TemplatesApi.ApiV1TemplatesIdGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** |  |  |

### Return type

[**ApiV1TemplatesIdGet200Response**](ApiV1TemplatesIdGet200Response.md)

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

<a id="apiv1templatesidusageget"></a>
# **ApiV1TemplatesIdUsageGet**
> ApiV1TemplatesIdUsageGet200Response ApiV1TemplatesIdUsageGet (Guid id)

Şablon kullanım kılavuzu (curl + JSON örnek)

Bu şablonu API üzerinden çağırmak için tam rehber döner: - `endpoint` (POST URL'i) - `required_headers` (X-API-Key, X-Workspace-Id, Content-Type) - `parties` (her partinin desteklediği field listesi) - `variables` (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - `example_request` (tam curl + JSON örneği, gerçek slug'larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug'larla doludur, root `variables` partisiz field'lar için. 

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
    public class ApiV1TemplatesIdUsageGetExample
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
            var apiInstance = new TemplatesApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | 

            try
            {
                // Şablon kullanım kılavuzu (curl + JSON örnek)
                ApiV1TemplatesIdUsageGet200Response result = apiInstance.ApiV1TemplatesIdUsageGet(id);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling TemplatesApi.ApiV1TemplatesIdUsageGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1TemplatesIdUsageGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Şablon kullanım kılavuzu (curl + JSON örnek)
    ApiResponse<ApiV1TemplatesIdUsageGet200Response> response = apiInstance.ApiV1TemplatesIdUsageGetWithHttpInfo(id);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling TemplatesApi.ApiV1TemplatesIdUsageGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** |  |  |

### Return type

[**ApiV1TemplatesIdUsageGet200Response**](ApiV1TemplatesIdUsageGet200Response.md)

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

