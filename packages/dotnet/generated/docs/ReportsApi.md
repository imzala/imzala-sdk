# ImzalaApiClient.Api.ReportsApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|--------|--------------|-------------|
| [**ApiV1ReportsGet**](ReportsApi.md#apiv1reportsget) | **GET** /api/v1/reports | Rapor / özet (agrege sözleşme durum sayıları) |

<a id="apiv1reportsget"></a>
# **ApiV1ReportsGet**
> ApiV1ReportsGet200Response ApiV1ReportsGet ()

Rapor / özet (agrege sözleşme durum sayıları)

API key sahibinin workspace'indeki sözleşmelerin agrege durum sayılarını döner (bekleyen / tamamlanan / iptal / süresi-dolmuş + bu ay oluşturulan). Yalnızca sayı; PII içermez. Kişisel workspace yalnızca sahibinin, org workspace org'un sözleşmelerini sayar (IDOR-safe, GET /demands ile aynı SSOT). 

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
    public class ApiV1ReportsGetExample
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
            var apiInstance = new ReportsApi(httpClient, config, httpClientHandler);

            try
            {
                // Rapor / özet (agrege sözleşme durum sayıları)
                ApiV1ReportsGet200Response result = apiInstance.ApiV1ReportsGet();
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling ReportsApi.ApiV1ReportsGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1ReportsGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Rapor / özet (agrege sözleşme durum sayıları)
    ApiResponse<ApiV1ReportsGet200Response> response = apiInstance.ApiV1ReportsGetWithHttpInfo();
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling ReportsApi.ApiV1ReportsGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters
This endpoint does not need any parameter.
### Return type

[**ApiV1ReportsGet200Response**](ApiV1ReportsGet200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Özet sayılar |  -  |
| **403** | Yetersiz kapsam (demands:read gerekli) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

