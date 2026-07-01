# imzala_client.TimestampsApi

All URIs are relative to *https://api-prd.imzala.org*

Method | HTTP request | Description
------------- | ------------- | -------------
[**api_v1_timestamps_post**](TimestampsApi.md#api_v1_timestamps_post) | **POST** /api/v1/timestamps | Zaman damgası oluştur (eser tescil)


# **api_v1_timestamps_post**
> ApiV1TimestampsPost201Response api_v1_timestamps_post(file, idempotency_key=idempotency_key, description=description, owner_first_name=owner_first_name, owner_last_name=owner_last_name)

Zaman damgası oluştur (eser tescil)

Dosyanın SHA-256 hash'ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile
imzalar ve bir zaman damgası kaydı oluşturur.

**Damga neyi kanıtlar:**
- Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı)
- Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)

**Damga neyi kanıtlamaz:**
- Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL)
- `owner_first_name` / `owner_last_name` alanları bilgilendirme amaçlıdır;
  sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.

Damga elektronik imza DEĞİLDİR. Nitelikli elektronik imza (QES) için
ayrı imzalama akışını kullanın.

**İdempotency:** `Idempotency-Key` header'ı (UUID önerilir) ile aynı
istek tekrar gönderilirse 5 dakika içinde aynı `id` döner, yeni damga
alınmaz ve kredi kesilmez.

**İki içerik formatı desteklenir:**
- `multipart/form-data`: `file` alanıyla ikili dosya yükleme
- `application/json`: `file_base64` alanıyla standart Base64 (RFC 4648 §4,
  canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez)


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_timestamps_post201_response import ApiV1TimestampsPost201Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.TimestampsApi(api_client)
    file = None # bytes | Damgalanacak dosya (maks. 50 MB)
    idempotency_key = 'idempotency_key_example' # str | Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  (optional)
    description = 'description_example' # str | Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)
    owner_first_name = 'owner_first_name_example' # str | Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)
    owner_last_name = 'owner_last_name_example' # str | Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)

    try:
        # Zaman damgası oluştur (eser tescil)
        api_response = api_instance.api_v1_timestamps_post(file, idempotency_key=idempotency_key, description=description, owner_first_name=owner_first_name, owner_last_name=owner_last_name)
        print("The response of TimestampsApi->api_v1_timestamps_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling TimestampsApi->api_v1_timestamps_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **file** | **bytes**| Damgalanacak dosya (maks. 50 MB) | 
 **idempotency_key** | **str**| Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  | [optional] 
 **description** | **str**| Kayıt açıklaması (opsiyonel, max 500 karakter) | [optional] 
 **owner_first_name** | **str**| Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) | [optional] 
 **owner_last_name** | **str**| Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) | [optional] 

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
**201** | Zaman damgası oluşturuldu |  -  |
**401** | API key geçersiz veya eksik |  -  |
**402** | Yetersiz kredi (INSUFFICIENT_CREDITS) |  -  |
**403** | INSUFFICIENT_SCOPE — API key&#39;de timestamps scope yok |  -  |
**422** | İstek içeriği işlenemedi. Olası kodlar: - &#x60;BAD_BASE64&#x60; — &#x60;file_base64&#x60; geçerli standart Base64 değil - &#x60;STAMP_INVALID&#x60; — TSA yanıtı geçersiz zaman damgası döndü  |  -  |
**429** | Rate limit aşıldı (60 istek/dakika per API key) |  -  |
**500** | INDETERMINATE — Damga alındı ancak doğrulama sonucu belirsiz. Destek ekibiyle iletişime geçin.  |  -  |
**503** | TSA_UNAVAILABLE — TÜBİTAK KAMU SM zaman damgası servisi geçici olarak erişilemiyor. Kısa süre sonra tekrar deneyin.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

