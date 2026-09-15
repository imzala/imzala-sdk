# ContactsApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**apiV1ContactsGet**](ContactsApi.md#apiV1ContactsGet) | **GET** /api/v1/contacts | Kişi listesi |
| [**apiV1ContactsGetWithHttpInfo**](ContactsApi.md#apiV1ContactsGetWithHttpInfo) | **GET** /api/v1/contacts | Kişi listesi |
| [**apiV1ContactsPost**](ContactsApi.md#apiV1ContactsPost) | **POST** /api/v1/contacts | Kişi oluştur |
| [**apiV1ContactsPostWithHttpInfo**](ContactsApi.md#apiV1ContactsPostWithHttpInfo) | **POST** /api/v1/contacts | Kişi oluştur |



## apiV1ContactsGet

> ApiV1ContactsGet200Response apiV1ContactsGet(page, limit, q, sort, companyId, archived)

Kişi listesi

API key sahibinin workspace&#39;indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org&#39;un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.ContactsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        ContactsApi apiInstance = new ContactsApi(defaultClient);
        Integer page = 1; // Integer | 
        Integer limit = 25; // Integer | 
        String q = "q_example"; // String | Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter)
        String sort = "-createdAt"; // String | Sıralama: createdAt | updatedAt | first_name | last_name | email ('-' öneki azalan)
        UUID companyId = UUID.randomUUID(); // UUID | 
        Boolean archived = false; // Boolean | true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif)
        try {
            ApiV1ContactsGet200Response result = apiInstance.apiV1ContactsGet(page, limit, q, sort, companyId, archived);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ContactsApi#apiV1ContactsGet");
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
| **limit** | **Integer**|  | [optional] [default to 25] |
| **q** | **String**| Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) | [optional] |
| **sort** | **String**| Sıralama: createdAt | updatedAt | first_name | last_name | email (&#39;-&#39; öneki azalan) | [optional] [default to -createdAt] |
| **companyId** | **UUID**|  | [optional] |
| **archived** | **Boolean**| true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) | [optional] [default to false] |

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

## apiV1ContactsGetWithHttpInfo

> ApiResponse<ApiV1ContactsGet200Response> apiV1ContactsGetWithHttpInfo(page, limit, q, sort, companyId, archived)

Kişi listesi

API key sahibinin workspace&#39;indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org&#39;un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.ContactsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        ContactsApi apiInstance = new ContactsApi(defaultClient);
        Integer page = 1; // Integer | 
        Integer limit = 25; // Integer | 
        String q = "q_example"; // String | Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter)
        String sort = "-createdAt"; // String | Sıralama: createdAt | updatedAt | first_name | last_name | email ('-' öneki azalan)
        UUID companyId = UUID.randomUUID(); // UUID | 
        Boolean archived = false; // Boolean | true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif)
        try {
            ApiResponse<ApiV1ContactsGet200Response> response = apiInstance.apiV1ContactsGetWithHttpInfo(page, limit, q, sort, companyId, archived);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ContactsApi#apiV1ContactsGet");
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
| **limit** | **Integer**|  | [optional] [default to 25] |
| **q** | **String**| Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) | [optional] |
| **sort** | **String**| Sıralama: createdAt | updatedAt | first_name | last_name | email (&#39;-&#39; öneki azalan) | [optional] [default to -createdAt] |
| **companyId** | **UUID**|  | [optional] |
| **archived** | **Boolean**| true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) | [optional] [default to false] |

### Return type

ApiResponse<[**ApiV1ContactsGet200Response**](ApiV1ContactsGet200Response.md)>


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


## apiV1ContactsPost

> ApiV1ContactsPost201Response apiV1ContactsPost(apiV1ContactsPostRequest)

Kişi oluştur

Workspace&#39;e yeni bir kişi ekler. Aynı workspace&#39;te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.ContactsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        ContactsApi apiInstance = new ContactsApi(defaultClient);
        ApiV1ContactsPostRequest apiV1ContactsPostRequest = new ApiV1ContactsPostRequest(); // ApiV1ContactsPostRequest | 
        try {
            ApiV1ContactsPost201Response result = apiInstance.apiV1ContactsPost(apiV1ContactsPostRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ContactsApi#apiV1ContactsPost");
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
| **apiV1ContactsPostRequest** | [**ApiV1ContactsPostRequest**](ApiV1ContactsPostRequest.md)|  | |

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

## apiV1ContactsPostWithHttpInfo

> ApiResponse<ApiV1ContactsPost201Response> apiV1ContactsPostWithHttpInfo(apiV1ContactsPostRequest)

Kişi oluştur

Workspace&#39;e yeni bir kişi ekler. Aynı workspace&#39;te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.ContactsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        ContactsApi apiInstance = new ContactsApi(defaultClient);
        ApiV1ContactsPostRequest apiV1ContactsPostRequest = new ApiV1ContactsPostRequest(); // ApiV1ContactsPostRequest | 
        try {
            ApiResponse<ApiV1ContactsPost201Response> response = apiInstance.apiV1ContactsPostWithHttpInfo(apiV1ContactsPostRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ContactsApi#apiV1ContactsPost");
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
| **apiV1ContactsPostRequest** | [**ApiV1ContactsPostRequest**](ApiV1ContactsPostRequest.md)|  | |

### Return type

ApiResponse<[**ApiV1ContactsPost201Response**](ApiV1ContactsPost201Response.md)>


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

