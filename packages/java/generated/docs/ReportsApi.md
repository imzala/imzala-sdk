# ReportsApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**apiV1ReportsGet**](ReportsApi.md#apiV1ReportsGet) | **GET** /api/v1/reports | Rapor / özet (agrege sözleşme durum sayıları) |
| [**apiV1ReportsGetWithHttpInfo**](ReportsApi.md#apiV1ReportsGetWithHttpInfo) | **GET** /api/v1/reports | Rapor / özet (agrege sözleşme durum sayıları) |



## apiV1ReportsGet

> ApiV1ReportsGet200Response apiV1ReportsGet()

Rapor / özet (agrege sözleşme durum sayıları)

API key sahibinin workspace&#39;indeki sözleşmelerin agrege durum sayılarını döner (bekleyen / tamamlanan / iptal / süresi-dolmuş + bu ay oluşturulan). Yalnızca sayı; PII içermez. Kişisel workspace yalnızca sahibinin, org workspace org&#39;un sözleşmelerini sayar (IDOR-safe, GET /demands ile aynı SSOT). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.ReportsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        ReportsApi apiInstance = new ReportsApi(defaultClient);
        try {
            ApiV1ReportsGet200Response result = apiInstance.apiV1ReportsGet();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ReportsApi#apiV1ReportsGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
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

## apiV1ReportsGetWithHttpInfo

> ApiResponse<ApiV1ReportsGet200Response> apiV1ReportsGetWithHttpInfo()

Rapor / özet (agrege sözleşme durum sayıları)

API key sahibinin workspace&#39;indeki sözleşmelerin agrege durum sayılarını döner (bekleyen / tamamlanan / iptal / süresi-dolmuş + bu ay oluşturulan). Yalnızca sayı; PII içermez. Kişisel workspace yalnızca sahibinin, org workspace org&#39;un sözleşmelerini sayar (IDOR-safe, GET /demands ile aynı SSOT). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.ReportsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        ReportsApi apiInstance = new ReportsApi(defaultClient);
        try {
            ApiResponse<ApiV1ReportsGet200Response> response = apiInstance.apiV1ReportsGetWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ReportsApi#apiV1ReportsGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

ApiResponse<[**ApiV1ReportsGet200Response**](ApiV1ReportsGet200Response.md)>


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

