# TemplatesApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**apiV1FieldTemplatesGet**](TemplatesApi.md#apiV1FieldTemplatesGet) | **GET** /api/v1/field-templates | Alan Şablonlarını listeler |
| [**apiV1FieldTemplatesGetWithHttpInfo**](TemplatesApi.md#apiV1FieldTemplatesGetWithHttpInfo) | **GET** /api/v1/field-templates | Alan Şablonlarını listeler |
| [**apiV1FieldTemplatesIdGet**](TemplatesApi.md#apiV1FieldTemplatesIdGet) | **GET** /api/v1/field-templates/{id} | Alan Şablonu ayrıntısı (roller + alan sayıları) |
| [**apiV1FieldTemplatesIdGetWithHttpInfo**](TemplatesApi.md#apiV1FieldTemplatesIdGetWithHttpInfo) | **GET** /api/v1/field-templates/{id} | Alan Şablonu ayrıntısı (roller + alan sayıları) |
| [**apiV1TemplatesGet**](TemplatesApi.md#apiV1TemplatesGet) | **GET** /api/v1/templates | Şablon listesi |
| [**apiV1TemplatesGetWithHttpInfo**](TemplatesApi.md#apiV1TemplatesGetWithHttpInfo) | **GET** /api/v1/templates | Şablon listesi |
| [**apiV1TemplatesIdDelete**](TemplatesApi.md#apiV1TemplatesIdDelete) | **DELETE** /api/v1/templates/{id} | Şablon sil |
| [**apiV1TemplatesIdDeleteWithHttpInfo**](TemplatesApi.md#apiV1TemplatesIdDeleteWithHttpInfo) | **DELETE** /api/v1/templates/{id} | Şablon sil |
| [**apiV1TemplatesIdGet**](TemplatesApi.md#apiV1TemplatesIdGet) | **GET** /api/v1/templates/{id} | Şablon detay |
| [**apiV1TemplatesIdGetWithHttpInfo**](TemplatesApi.md#apiV1TemplatesIdGetWithHttpInfo) | **GET** /api/v1/templates/{id} | Şablon detay |
| [**apiV1TemplatesIdPatch**](TemplatesApi.md#apiV1TemplatesIdPatch) | **PATCH** /api/v1/templates/{id} | Şablon metadata güncelle |
| [**apiV1TemplatesIdPatchWithHttpInfo**](TemplatesApi.md#apiV1TemplatesIdPatchWithHttpInfo) | **PATCH** /api/v1/templates/{id} | Şablon metadata güncelle |
| [**apiV1TemplatesIdUsageGet**](TemplatesApi.md#apiV1TemplatesIdUsageGet) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek) |
| [**apiV1TemplatesIdUsageGetWithHttpInfo**](TemplatesApi.md#apiV1TemplatesIdUsageGetWithHttpInfo) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek) |



## apiV1FieldTemplatesGet

> ApiV1FieldTemplatesGet200Response apiV1FieldTemplatesGet(page, limit)

Alan Şablonlarını listeler

Hesabınızdaki (ya da seçili çalışma alanındaki) **Alan Şablonlarını** (&#x60;kind: FIELD_LAYOUT&#x60;) döner.  ### Neden ayrı bir uç  &#x60;GET /api/v1/templates&#x60; Alan Şablonlarını bilerek listelemez ve &#x60;GET /api/v1/templates/{id}&#x60; onlara &#x60;404&#x60; döner. Alan Şablonunun referans belgesi hiçbir imzacıya gönderilmez; belgeli şablon sanılması, o referans belgenin karşı tarafa gitmesi demek olurdu. Bu yüzden varsayılan davranış hariç tutmaktır ve Alan Şablonlarını görmek açık bir talep gerektirir.  Döndürülen &#x60;id&#x60;, &#x60;POST /api/v1/demands/upload&#x60; çağrısındaki &#x60;field_template_id&#x60; ve &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60; için kullanılır.  &#x60;parties&#x60; dizisi taraf sırasına göre döner; upload gövdesindeki &#x60;parties&#x60; dizisini bu sırayla kurun. 

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
            ApiV1FieldTemplatesGet200Response result = apiInstance.apiV1FieldTemplatesGet(page, limit);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1FieldTemplatesGet");
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

[**ApiV1FieldTemplatesGet200Response**](ApiV1FieldTemplatesGet200Response.md)


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Alan Şablonu listesi |  -  |
| **400** | &#x60;INVALID_PAGE&#x60;: &#x60;page&#x60; 1 veya daha büyük bir tam sayı değil. &#x60;limit&#x60; hata üretmez: 1 ile 100 arasına kırpılır, sayı değilse varsayılan 20 kullanılır.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |

## apiV1FieldTemplatesGetWithHttpInfo

> ApiResponse<ApiV1FieldTemplatesGet200Response> apiV1FieldTemplatesGetWithHttpInfo(page, limit)

Alan Şablonlarını listeler

Hesabınızdaki (ya da seçili çalışma alanındaki) **Alan Şablonlarını** (&#x60;kind: FIELD_LAYOUT&#x60;) döner.  ### Neden ayrı bir uç  &#x60;GET /api/v1/templates&#x60; Alan Şablonlarını bilerek listelemez ve &#x60;GET /api/v1/templates/{id}&#x60; onlara &#x60;404&#x60; döner. Alan Şablonunun referans belgesi hiçbir imzacıya gönderilmez; belgeli şablon sanılması, o referans belgenin karşı tarafa gitmesi demek olurdu. Bu yüzden varsayılan davranış hariç tutmaktır ve Alan Şablonlarını görmek açık bir talep gerektirir.  Döndürülen &#x60;id&#x60;, &#x60;POST /api/v1/demands/upload&#x60; çağrısındaki &#x60;field_template_id&#x60; ve &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60; için kullanılır.  &#x60;parties&#x60; dizisi taraf sırasına göre döner; upload gövdesindeki &#x60;parties&#x60; dizisini bu sırayla kurun. 

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
            ApiResponse<ApiV1FieldTemplatesGet200Response> response = apiInstance.apiV1FieldTemplatesGetWithHttpInfo(page, limit);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1FieldTemplatesGet");
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

ApiResponse<[**ApiV1FieldTemplatesGet200Response**](ApiV1FieldTemplatesGet200Response.md)>


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Alan Şablonu listesi |  -  |
| **400** | &#x60;INVALID_PAGE&#x60;: &#x60;page&#x60; 1 veya daha büyük bir tam sayı değil. &#x60;limit&#x60; hata üretmez: 1 ile 100 arasına kırpılır, sayı değilse varsayılan 20 kullanılır.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |


## apiV1FieldTemplatesIdGet

> ApiV1FieldTemplatesIdGet200Response apiV1FieldTemplatesIdGet(id)

Alan Şablonu ayrıntısı (roller + alan sayıları)

Tek bir Alan Şablonunun rollerini ve rol başına alan sayısını döner.  🔴 **Alanların koordinatları dönmez.** Yerleşimin belgeye nasıl uygulanacağını görmek için kuru koşum ucunu kullanın: &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  Belgeli bir şablonun kimliği verilirse &#x60;404&#x60; döner; şablonun türü ifşa edilmez. Var olmayan ve size ait olmayan kimlik de aynı yanıtı alır. 

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
        UUID id = UUID.randomUUID(); // UUID | Alan Şablonu kimliği
        try {
            ApiV1FieldTemplatesIdGet200Response result = apiInstance.apiV1FieldTemplatesIdGet(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1FieldTemplatesIdGet");
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
| **id** | **UUID**| Alan Şablonu kimliği | |

### Return type

[**ApiV1FieldTemplatesIdGet200Response**](ApiV1FieldTemplatesIdGet200Response.md)


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Alan Şablonu ayrıntısı |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **404** | &#x60;TEMPLATE_NOT_FOUND&#x60; — şablon bulunamadı, size ait değil ya da Alan Şablonu değil.  |  -  |

## apiV1FieldTemplatesIdGetWithHttpInfo

> ApiResponse<ApiV1FieldTemplatesIdGet200Response> apiV1FieldTemplatesIdGetWithHttpInfo(id)

Alan Şablonu ayrıntısı (roller + alan sayıları)

Tek bir Alan Şablonunun rollerini ve rol başına alan sayısını döner.  🔴 **Alanların koordinatları dönmez.** Yerleşimin belgeye nasıl uygulanacağını görmek için kuru koşum ucunu kullanın: &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  Belgeli bir şablonun kimliği verilirse &#x60;404&#x60; döner; şablonun türü ifşa edilmez. Var olmayan ve size ait olmayan kimlik de aynı yanıtı alır. 

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
        UUID id = UUID.randomUUID(); // UUID | Alan Şablonu kimliği
        try {
            ApiResponse<ApiV1FieldTemplatesIdGet200Response> response = apiInstance.apiV1FieldTemplatesIdGetWithHttpInfo(id);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1FieldTemplatesIdGet");
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
| **id** | **UUID**| Alan Şablonu kimliği | |

### Return type

ApiResponse<[**ApiV1FieldTemplatesIdGet200Response**](ApiV1FieldTemplatesIdGet200Response.md)>


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Alan Şablonu ayrıntısı |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **404** | &#x60;TEMPLATE_NOT_FOUND&#x60; — şablon bulunamadı, size ait değil ya da Alan Şablonu değil.  |  -  |


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
| **400** | &#x60;INVALID_PAGE&#x60;: &#x60;page&#x60; 1 veya daha büyük bir tam sayı değil. &#x60;limit&#x60; hata üretmez: 1 ile 100 arasına kırpılır, sayı değilse varsayılan 20 kullanılır.  |  -  |
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
| **400** | &#x60;INVALID_PAGE&#x60;: &#x60;page&#x60; 1 veya daha büyük bir tam sayı değil. &#x60;limit&#x60; hata üretmez: 1 ile 100 arasına kırpılır, sayı değilse varsayılan 20 kullanılır.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |


## apiV1TemplatesIdDelete

> ApiV1TemplatesIdDelete200Response apiV1TemplatesIdDelete(id)

Şablon sil

Şablonu siler. Silinen şablon 30 gün saklanır, sürenin sonunda kalıcı olarak silinir. Mevcut sözleşmeler etkilenmez.  Şablonun aktif (DRAFT veya PENDING) sözleşmesi varsa silinemez; 409 &#x60;TEMPLATE_IN_USE&#x60; döner. 

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
            ApiV1TemplatesIdDelete200Response result = apiInstance.apiV1TemplatesIdDelete(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1TemplatesIdDelete");
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

[**ApiV1TemplatesIdDelete200Response**](ApiV1TemplatesIdDelete200Response.md)


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Silindi |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;TEMPLATE_IN_USE&#x60;: şablonun aktif (DRAFT veya PENDING) sözleşmesi olduğu için silinemez. Önce o sözleşmeleri sonlandırın.  |  -  |

## apiV1TemplatesIdDeleteWithHttpInfo

> ApiResponse<ApiV1TemplatesIdDelete200Response> apiV1TemplatesIdDeleteWithHttpInfo(id)

Şablon sil

Şablonu siler. Silinen şablon 30 gün saklanır, sürenin sonunda kalıcı olarak silinir. Mevcut sözleşmeler etkilenmez.  Şablonun aktif (DRAFT veya PENDING) sözleşmesi varsa silinemez; 409 &#x60;TEMPLATE_IN_USE&#x60; döner. 

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
            ApiResponse<ApiV1TemplatesIdDelete200Response> response = apiInstance.apiV1TemplatesIdDeleteWithHttpInfo(id);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1TemplatesIdDelete");
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

ApiResponse<[**ApiV1TemplatesIdDelete200Response**](ApiV1TemplatesIdDelete200Response.md)>


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Silindi |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;TEMPLATE_IN_USE&#x60;: şablonun aktif (DRAFT veya PENDING) sözleşmesi olduğu için silinemez. Önce o sözleşmeleri sonlandırın.  |  -  |


## apiV1TemplatesIdGet

> ApiV1TemplatesIdGet200Response apiV1TemplatesIdGet(id)

Şablon detay

Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır.  &#x60;documents&#x60; şablonun zarf belgelerini &#x60;order&#x60; sırasıyla döndürür; &#x60;documents[].id&#x60; sözleşme oluştururken &#x60;documents.include&#x60; / &#x60;documents.exclude&#x60; listelerinde kullanılır. 

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

Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır.  &#x60;documents&#x60; şablonun zarf belgelerini &#x60;order&#x60; sırasıyla döndürür; &#x60;documents[].id&#x60; sözleşme oluştururken &#x60;documents.include&#x60; / &#x60;documents.exclude&#x60; listelerinde kullanılır. 

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


## apiV1TemplatesIdPatch

> ApiV1TemplatesIdPatch200Response apiV1TemplatesIdPatch(id, apiV1TemplatesIdPatchRequest)

Şablon metadata güncelle

Şablonun yalnızca metadata alanlarını (name / description / category) günceller. Sayfa/alan/taraf yapısı bu endpoint&#39;ten DEĞİŞTİRİLEMEZ (şablon içeriği panelden düzenlenir). 

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
        ApiV1TemplatesIdPatchRequest apiV1TemplatesIdPatchRequest = new ApiV1TemplatesIdPatchRequest(); // ApiV1TemplatesIdPatchRequest | 
        try {
            ApiV1TemplatesIdPatch200Response result = apiInstance.apiV1TemplatesIdPatch(id, apiV1TemplatesIdPatchRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1TemplatesIdPatch");
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
| **apiV1TemplatesIdPatchRequest** | [**ApiV1TemplatesIdPatchRequest**](ApiV1TemplatesIdPatchRequest.md)|  | |

### Return type

[**ApiV1TemplatesIdPatch200Response**](ApiV1TemplatesIdPatch200Response.md)


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Güncellendi |  -  |
| **400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
| **404** | Kayıt bulunamadı |  -  |

## apiV1TemplatesIdPatchWithHttpInfo

> ApiResponse<ApiV1TemplatesIdPatch200Response> apiV1TemplatesIdPatchWithHttpInfo(id, apiV1TemplatesIdPatchRequest)

Şablon metadata güncelle

Şablonun yalnızca metadata alanlarını (name / description / category) günceller. Sayfa/alan/taraf yapısı bu endpoint&#39;ten DEĞİŞTİRİLEMEZ (şablon içeriği panelden düzenlenir). 

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
        ApiV1TemplatesIdPatchRequest apiV1TemplatesIdPatchRequest = new ApiV1TemplatesIdPatchRequest(); // ApiV1TemplatesIdPatchRequest | 
        try {
            ApiResponse<ApiV1TemplatesIdPatch200Response> response = apiInstance.apiV1TemplatesIdPatchWithHttpInfo(id, apiV1TemplatesIdPatchRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TemplatesApi#apiV1TemplatesIdPatch");
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
| **apiV1TemplatesIdPatchRequest** | [**ApiV1TemplatesIdPatchRequest**](ApiV1TemplatesIdPatchRequest.md)|  | |

### Return type

ApiResponse<[**ApiV1TemplatesIdPatch200Response**](ApiV1TemplatesIdPatch200Response.md)>


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Güncellendi |  -  |
| **400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
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

