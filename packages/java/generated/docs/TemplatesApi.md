# TemplatesApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**apiV1TemplatesGet**](TemplatesApi.md#apiV1TemplatesGet) | **GET** /api/v1/templates | Şablon listesi |
| [**apiV1TemplatesGetWithHttpInfo**](TemplatesApi.md#apiV1TemplatesGetWithHttpInfo) | **GET** /api/v1/templates | Şablon listesi |
| [**apiV1TemplatesIdGet**](TemplatesApi.md#apiV1TemplatesIdGet) | **GET** /api/v1/templates/{id} | Şablon detay |
| [**apiV1TemplatesIdGetWithHttpInfo**](TemplatesApi.md#apiV1TemplatesIdGetWithHttpInfo) | **GET** /api/v1/templates/{id} | Şablon detay |
| [**apiV1TemplatesIdUsageGet**](TemplatesApi.md#apiV1TemplatesIdUsageGet) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek) |
| [**apiV1TemplatesIdUsageGetWithHttpInfo**](TemplatesApi.md#apiV1TemplatesIdUsageGetWithHttpInfo) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek) |



## apiV1TemplatesGet

> ApiV1TemplatesGet200Response apiV1TemplatesGet(page, limit)

Şablon listesi

Aktif şablonlarınızı listeler.

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.TemplatesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        TemplatesApi apiInstance = new TemplatesApi(defaultClient);
        Integer page = 1; // Integer | 
        Integer limit = 20; // Integer | 
        try {
            ApiV1TemplatesGet200Response result = apiInstance.apiV1TemplatesGet(page, limit);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1TemplatesGet");
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
| **page** | **Integer**|  | [optional] [default to 1] |
| **limit** | **Integer**|  | [optional] [default to 20] |

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

## apiV1TemplatesGetWithHttpInfo

> ApiResponse<ApiV1TemplatesGet200Response> apiV1TemplatesGetWithHttpInfo(page, limit)

Şablon listesi

Aktif şablonlarınızı listeler.

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.TemplatesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        TemplatesApi apiInstance = new TemplatesApi(defaultClient);
        Integer page = 1; // Integer | 
        Integer limit = 20; // Integer | 
        try {
            ApiResponse<ApiV1TemplatesGet200Response> response = apiInstance.apiV1TemplatesGetWithHttpInfo(page, limit);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1TemplatesGet");
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
| **page** | **Integer**|  | [optional] [default to 1] |
| **limit** | **Integer**|  | [optional] [default to 20] |

### Return type

ApiResponse<[**ApiV1TemplatesGet200Response**](ApiV1TemplatesGet200Response.md)>


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


## apiV1TemplatesIdGet

> ApiV1TemplatesIdGet200Response apiV1TemplatesIdGet(id)

Şablon detay

Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.TemplatesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        TemplatesApi apiInstance = new TemplatesApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiV1TemplatesIdGet200Response result = apiInstance.apiV1TemplatesIdGet(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1TemplatesIdGet");
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

## apiV1TemplatesIdGetWithHttpInfo

> ApiResponse<ApiV1TemplatesIdGet200Response> apiV1TemplatesIdGetWithHttpInfo(id)

Şablon detay

Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.TemplatesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        TemplatesApi apiInstance = new TemplatesApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiResponse<ApiV1TemplatesIdGet200Response> response = apiInstance.apiV1TemplatesIdGetWithHttpInfo(id);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1TemplatesIdGet");
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

ApiResponse<[**ApiV1TemplatesIdGet200Response**](ApiV1TemplatesIdGet200Response.md)>


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


## apiV1TemplatesIdUsageGet

> ApiV1TemplatesIdUsageGet200Response apiV1TemplatesIdUsageGet(id)

Şablon kullanım kılavuzu (curl + JSON örnek)

Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.TemplatesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        TemplatesApi apiInstance = new TemplatesApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiV1TemplatesIdUsageGet200Response result = apiInstance.apiV1TemplatesIdUsageGet(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1TemplatesIdUsageGet");
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

## apiV1TemplatesIdUsageGetWithHttpInfo

> ApiResponse<ApiV1TemplatesIdUsageGet200Response> apiV1TemplatesIdUsageGetWithHttpInfo(id)

Şablon kullanım kılavuzu (curl + JSON örnek)

Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.TemplatesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        TemplatesApi apiInstance = new TemplatesApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiResponse<ApiV1TemplatesIdUsageGet200Response> response = apiInstance.apiV1TemplatesIdUsageGetWithHttpInfo(id);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1TemplatesIdUsageGet");
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

ApiResponse<[**ApiV1TemplatesIdUsageGet200Response**](ApiV1TemplatesIdUsageGet200Response.md)>


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

