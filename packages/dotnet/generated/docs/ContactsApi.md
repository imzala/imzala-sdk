# ImzalaApiClient.Api.ContactsApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|--------|--------------|-------------|
| [**ApiV1ContactsGet**](ContactsApi.md#apiv1contactsget) | **GET** /api/v1/contacts | Kişi listesi |
| [**ApiV1ContactsPost**](ContactsApi.md#apiv1contactspost) | **POST** /api/v1/contacts | Kişi oluştur |

<a id="apiv1contactsget"></a>
# **ApiV1ContactsGet**
> ApiV1ContactsGet200Response ApiV1ContactsGet (int? page = null, int? limit = null, string? q = null, string? sort = null, Guid? companyId = null, bool? archived = null)

Kişi listesi

API key sahibinin workspace'indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org'un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 

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
    public class ApiV1ContactsGetExample
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
            var apiInstance = new ContactsApi(httpClient, config, httpClientHandler);
            var page = 1;  // int? |  (optional)  (default to 1)
            var limit = 25;  // int? |  (optional)  (default to 25)
            var q = "q_example";  // string? | Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) (optional) 
            var sort = "\"-createdAt\"";  // string? | Sıralama: createdAt | updatedAt | first_name | last_name | email ('-' öneki azalan) (optional)  (default to "-createdAt")
            var companyId = "companyId_example";  // Guid? |  (optional) 
            var archived = false;  // bool? | true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) (optional)  (default to false)

            try
            {
                // Kişi listesi
                ApiV1ContactsGet200Response result = apiInstance.ApiV1ContactsGet(page, limit, q, sort, companyId, archived);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling ContactsApi.ApiV1ContactsGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1ContactsGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Kişi listesi
    ApiResponse<ApiV1ContactsGet200Response> response = apiInstance.ApiV1ContactsGetWithHttpInfo(page, limit, q, sort, companyId, archived);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling ContactsApi.ApiV1ContactsGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **page** | **int?** |  | [optional] [default to 1] |
| **limit** | **int?** |  | [optional] [default to 25] |
| **q** | **string?** | Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) | [optional]  |
| **sort** | **string?** | Sıralama: createdAt | updatedAt | first_name | last_name | email (&#39;-&#39; öneki azalan) | [optional] [default to &quot;-createdAt&quot;] |
| **companyId** | **Guid?** |  | [optional]  |
| **archived** | **bool?** | true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) | [optional] [default to false] |

### Return type

[**ApiV1ContactsGet200Response**](ApiV1ContactsGet200Response.md)

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
| **403** | INSUFFICIENT_SCOPE — API key&#39;de contacts:read scope yok |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1contactspost"></a>
# **ApiV1ContactsPost**
> ApiV1ContactsPost201Response ApiV1ContactsPost (ApiV1ContactsPostRequest apiV1ContactsPostRequest)

Kişi oluştur

Workspace'e yeni bir kişi ekler. Aynı workspace'te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 

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
    public class ApiV1ContactsPostExample
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
            var apiInstance = new ContactsApi(httpClient, config, httpClientHandler);
            var apiV1ContactsPostRequest = new ApiV1ContactsPostRequest(); // ApiV1ContactsPostRequest | 

            try
            {
                // Kişi oluştur
                ApiV1ContactsPost201Response result = apiInstance.ApiV1ContactsPost(apiV1ContactsPostRequest);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling ContactsApi.ApiV1ContactsPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1ContactsPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Kişi oluştur
    ApiResponse<ApiV1ContactsPost201Response> response = apiInstance.ApiV1ContactsPostWithHttpInfo(apiV1ContactsPostRequest);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling ContactsApi.ApiV1ContactsPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **apiV1ContactsPostRequest** | [**ApiV1ContactsPostRequest**](ApiV1ContactsPostRequest.md) |  |  |

### Return type

[**ApiV1ContactsPost201Response**](ApiV1ContactsPost201Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Kişi oluşturuldu |  -  |
| **400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **403** | INSUFFICIENT_SCOPE — API key&#39;de contacts:write scope yok |  -  |
| **409** | CONTACT_DUPLICATE — aynı e-posta/telefona sahip kişi zaten var |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

