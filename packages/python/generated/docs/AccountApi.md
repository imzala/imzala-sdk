# imzala_client.AccountApi

All URIs are relative to *https://api-prd.imzala.org*

Method | HTTP request | Description
------------- | ------------- | -------------
[**api_v1_me_get**](AccountApi.md#api_v1_me_get) | **GET** /api/v1/me | API key sahibi bilgisi


# **api_v1_me_get**
> ApiV1MeGet200Response api_v1_me_get()

API key sahibi bilgisi

Çağrıyı yapan API key'in sahibi hakkında temel bilgileri döner:
kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi.
`timestamps` scope'u gerektirir; scope yoksa 403 `INSUFFICIENT_SCOPE` döner.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_me_get200_response import ApiV1MeGet200Response
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
    api_instance = imzala_client.AccountApi(api_client)

    try:
        # API key sahibi bilgisi
        api_response = api_instance.api_v1_me_get()
        print("The response of AccountApi->api_v1_me_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling AccountApi->api_v1_me_get: %s\n" % e)
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
**200** | Başarılı |  -  |
**401** | API key geçersiz veya eksik |  -  |
**403** | **INSUFFICIENT_SCOPE** — API key&#39;in &#x60;timestamps&#x60; scope&#39;u yok. Dashboard → Geliştirici → API Anahtarları sayfasından scope&#39;u güncelleyin.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

