# AccountApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**apiV1MeGet**](AccountApi.md#apiV1MeGet) | **GET** /api/v1/me | API key sahibi bilgisi |
| [**apiV1MeGetWithHttpInfo**](AccountApi.md#apiV1MeGetWithHttpInfo) | **GET** /api/v1/me | API key sahibi bilgisi |



## apiV1MeGet

> ApiV1MeGet200Response apiV1MeGet()

API key sahibi bilgisi

Çağrıyı yapan API key&#39;in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. &#x60;timestamps&#x60; scope&#39;u gerektirir; scope yoksa 403 &#x60;INSUFFICIENT_SCOPE&#x60; döner. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.AccountApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        AccountApi apiInstance = new AccountApi(defaultClient);
        try {
            ApiV1MeGet200Response result = apiInstance.apiV1MeGet();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AccountApi#apiV1MeGet");
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

## apiV1MeGetWithHttpInfo

> ApiResponse<ApiV1MeGet200Response> apiV1MeGetWithHttpInfo()

API key sahibi bilgisi

Çağrıyı yapan API key&#39;in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. &#x60;timestamps&#x60; scope&#39;u gerektirir; scope yoksa 403 &#x60;INSUFFICIENT_SCOPE&#x60; döner. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.AccountApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        AccountApi apiInstance = new AccountApi(defaultClient);
        try {
            ApiResponse<ApiV1MeGet200Response> response = apiInstance.apiV1MeGetWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling AccountApi#apiV1MeGet");
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

ApiResponse<[**ApiV1MeGet200Response**](ApiV1MeGet200Response.md)>


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

