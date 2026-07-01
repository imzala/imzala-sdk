# ImzalaApiClient.Api.AccountApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|--------|--------------|-------------|
| [**ApiV1MeGet**](AccountApi.md#apiv1meget) | **GET** /api/v1/me | API key sahibi bilgisi |

<a id="apiv1meget"></a>
# **ApiV1MeGet**
> ApiV1MeGet200Response ApiV1MeGet ()

API key sahibi bilgisi

Çağrıyı yapan API key'in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. `timestamps` scope'u gerektirir; scope yoksa 403 `INSUFFICIENT_SCOPE` döner. 

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
    public class ApiV1MeGetExample
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
            var apiInstance = new AccountApi(httpClient, config, httpClientHandler);

            try
            {
                // API key sahibi bilgisi
                ApiV1MeGet200Response result = apiInstance.ApiV1MeGet();
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling AccountApi.ApiV1MeGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1MeGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // API key sahibi bilgisi
    ApiResponse<ApiV1MeGet200Response> response = apiInstance.ApiV1MeGetWithHttpInfo();
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling AccountApi.ApiV1MeGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters
This endpoint does not need any parameter.
### Return type

[**ApiV1MeGet200Response**](ApiV1MeGet200Response.md)

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
| **403** | **INSUFFICIENT_SCOPE** — API key&#39;in &#x60;timestamps&#x60; scope&#39;u yok. Dashboard → Geliştirici → API Anahtarları sayfasından scope&#39;u güncelleyin.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

