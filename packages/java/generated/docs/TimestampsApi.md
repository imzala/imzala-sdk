# TimestampsApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**apiV1TimestampsPost**](TimestampsApi.md#apiV1TimestampsPost) | **POST** /api/v1/timestamps | Zaman damgası oluştur (eser tescil) |
| [**apiV1TimestampsPostWithHttpInfo**](TimestampsApi.md#apiV1TimestampsPostWithHttpInfo) | **POST** /api/v1/timestamps | Zaman damgası oluştur (eser tescil) |



## apiV1TimestampsPost

> ApiV1TimestampsPost201Response apiV1TimestampsPost(_file, idempotencyKey, description, ownerFirstName, ownerLastName)

Zaman damgası oluştur (eser tescil)

Dosyanın SHA-256 hash&#39;ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - &#x60;owner_first_name&#x60; / &#x60;owner_last_name&#x60; alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Nitelikli elektronik imza (QES) için ayrı imzalama akışını kullanın.  **İdempotency:** &#x60;Idempotency-Key&#x60; header&#39;ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı &#x60;id&#x60; döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - &#x60;multipart/form-data&#x60;: &#x60;file&#x60; alanıyla ikili dosya yükleme - &#x60;application/json&#x60;: &#x60;file_base64&#x60; alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.TimestampsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        TimestampsApi apiInstance = new TimestampsApi(defaultClient);
        File _file = new File("/path/to/file"); // File | Damgalanacak dosya (maks. 50 MB)
        String idempotencyKey = "idempotencyKey_example"; // String | Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez. 
        String description = "description_example"; // String | Kayıt açıklaması (opsiyonel, max 500 karakter)
        String ownerFirstName = "ownerFirstName_example"; // String | Dosya sahibinin adı (opsiyonel, kullanıcı beyanı)
        String ownerLastName = "ownerLastName_example"; // String | Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı)
        try {
            ApiV1TimestampsPost201Response result = apiInstance.apiV1TimestampsPost(_file, idempotencyKey, description, ownerFirstName, ownerLastName);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TimestampsApi#apiV1TimestampsPost");
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
| **_file** | **File**| Damgalanacak dosya (maks. 50 MB) | |
| **idempotencyKey** | **String**| Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  | [optional] |
| **description** | **String**| Kayıt açıklaması (opsiyonel, max 500 karakter) | [optional] |
| **ownerFirstName** | **String**| Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) | [optional] |
| **ownerLastName** | **String**| Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) | [optional] |

### Return type

[**ApiV1TimestampsPost201Response**](ApiV1TimestampsPost201Response.md)


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: multipart/form-data, application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Zaman damgası oluşturuldu |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **402** | Yetersiz kredi (INSUFFICIENT_CREDITS) |  -  |
| **403** | INSUFFICIENT_SCOPE — API key&#39;de timestamps scope yok |  -  |
| **422** | İstek içeriği işlenemedi. Olası kodlar: - &#x60;BAD_BASE64&#x60; — &#x60;file_base64&#x60; geçerli standart Base64 değil - &#x60;STAMP_INVALID&#x60; — TSA yanıtı geçersiz zaman damgası döndü  |  -  |
| **429** | Rate limit aşıldı (60 istek/dakika per API key) |  -  |
| **500** | INDETERMINATE — Damga alındı ancak doğrulama sonucu belirsiz. Destek ekibiyle iletişime geçin.  |  -  |
| **503** | TSA_UNAVAILABLE — TÜBİTAK KAMU SM zaman damgası servisi geçici olarak erişilemiyor. Kısa süre sonra tekrar deneyin.  |  -  |

## apiV1TimestampsPostWithHttpInfo

> ApiResponse<ApiV1TimestampsPost201Response> apiV1TimestampsPostWithHttpInfo(_file, idempotencyKey, description, ownerFirstName, ownerLastName)

Zaman damgası oluştur (eser tescil)

Dosyanın SHA-256 hash&#39;ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - &#x60;owner_first_name&#x60; / &#x60;owner_last_name&#x60; alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Nitelikli elektronik imza (QES) için ayrı imzalama akışını kullanın.  **İdempotency:** &#x60;Idempotency-Key&#x60; header&#39;ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı &#x60;id&#x60; döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - &#x60;multipart/form-data&#x60;: &#x60;file&#x60; alanıyla ikili dosya yükleme - &#x60;application/json&#x60;: &#x60;file_base64&#x60; alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.TimestampsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        TimestampsApi apiInstance = new TimestampsApi(defaultClient);
        File _file = new File("/path/to/file"); // File | Damgalanacak dosya (maks. 50 MB)
        String idempotencyKey = "idempotencyKey_example"; // String | Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez. 
        String description = "description_example"; // String | Kayıt açıklaması (opsiyonel, max 500 karakter)
        String ownerFirstName = "ownerFirstName_example"; // String | Dosya sahibinin adı (opsiyonel, kullanıcı beyanı)
        String ownerLastName = "ownerLastName_example"; // String | Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı)
        try {
            ApiResponse<ApiV1TimestampsPost201Response> response = apiInstance.apiV1TimestampsPostWithHttpInfo(_file, idempotencyKey, description, ownerFirstName, ownerLastName);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TimestampsApi#apiV1TimestampsPost");
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
| **_file** | **File**| Damgalanacak dosya (maks. 50 MB) | |
| **idempotencyKey** | **String**| Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  | [optional] |
| **description** | **String**| Kayıt açıklaması (opsiyonel, max 500 karakter) | [optional] |
| **ownerFirstName** | **String**| Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) | [optional] |
| **ownerLastName** | **String**| Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) | [optional] |

### Return type

ApiResponse<[**ApiV1TimestampsPost201Response**](ApiV1TimestampsPost201Response.md)>


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: multipart/form-data, application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Zaman damgası oluşturuldu |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **402** | Yetersiz kredi (INSUFFICIENT_CREDITS) |  -  |
| **403** | INSUFFICIENT_SCOPE — API key&#39;de timestamps scope yok |  -  |
| **422** | İstek içeriği işlenemedi. Olası kodlar: - &#x60;BAD_BASE64&#x60; — &#x60;file_base64&#x60; geçerli standart Base64 değil - &#x60;STAMP_INVALID&#x60; — TSA yanıtı geçersiz zaman damgası döndü  |  -  |
| **429** | Rate limit aşıldı (60 istek/dakika per API key) |  -  |
| **500** | INDETERMINATE — Damga alındı ancak doğrulama sonucu belirsiz. Destek ekibiyle iletişime geçin.  |  -  |
| **503** | TSA_UNAVAILABLE — TÜBİTAK KAMU SM zaman damgası servisi geçici olarak erişilemiyor. Kısa süre sonra tekrar deneyin.  |  -  |

