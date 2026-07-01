# ImzalaApiClient.Api.TimestampsApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|--------|--------------|-------------|
| [**ApiV1TimestampsPost**](TimestampsApi.md#apiv1timestampspost) | **POST** /api/v1/timestamps | Zaman damgası oluştur (eser tescil) |

<a id="apiv1timestampspost"></a>
# **ApiV1TimestampsPost**
> ApiV1TimestampsPost201Response ApiV1TimestampsPost (FileParameter file, string? idempotencyKey = null, string? description = null, string? ownerFirstName = null, string? ownerLastName = null)

Zaman damgası oluştur (eser tescil)

Dosyanın SHA-256 hash'ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - `owner_first_name` / `owner_last_name` alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Nitelikli elektronik imza (QES) için ayrı imzalama akışını kullanın.  **İdempotency:** `Idempotency-Key` header'ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı `id` döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - `multipart/form-data`: `file` alanıyla ikili dosya yükleme - `application/json`: `file_base64` alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 

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
    public class ApiV1TimestampsPostExample
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
            var apiInstance = new TimestampsApi(httpClient, config, httpClientHandler);
            var file = new System.IO.MemoryStream(System.IO.File.ReadAllBytes("/path/to/file.txt"));  // FileParameter | Damgalanacak dosya (maks. 50 MB)
            var idempotencyKey = "idempotencyKey_example";  // string? | Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  (optional) 
            var description = "description_example";  // string? | Kayıt açıklaması (opsiyonel, max 500 karakter) (optional) 
            var ownerFirstName = "ownerFirstName_example";  // string? | Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional) 
            var ownerLastName = "ownerLastName_example";  // string? | Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional) 

            try
            {
                // Zaman damgası oluştur (eser tescil)
                ApiV1TimestampsPost201Response result = apiInstance.ApiV1TimestampsPost(file, idempotencyKey, description, ownerFirstName, ownerLastName);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling TimestampsApi.ApiV1TimestampsPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1TimestampsPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Zaman damgası oluştur (eser tescil)
    ApiResponse<ApiV1TimestampsPost201Response> response = apiInstance.ApiV1TimestampsPostWithHttpInfo(file, idempotencyKey, description, ownerFirstName, ownerLastName);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling TimestampsApi.ApiV1TimestampsPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **file** | **FileParameter****FileParameter** | Damgalanacak dosya (maks. 50 MB) |  |
| **idempotencyKey** | **string?** | Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  | [optional]  |
| **description** | **string?** | Kayıt açıklaması (opsiyonel, max 500 karakter) | [optional]  |
| **ownerFirstName** | **string?** | Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) | [optional]  |
| **ownerLastName** | **string?** | Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) | [optional]  |

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

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

