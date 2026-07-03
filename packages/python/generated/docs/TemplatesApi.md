# imzala_client.TemplatesApi

All URIs are relative to *https://api-prd.imzala.org*

Method | HTTP request | Description
------------- | ------------- | -------------
[**api_v1_templates_get**](TemplatesApi.md#api_v1_templates_get) | **GET** /api/v1/templates | Şablon listesi
[**api_v1_templates_id_delete**](TemplatesApi.md#api_v1_templates_id_delete) | **DELETE** /api/v1/templates/{id} | Şablon sil
[**api_v1_templates_id_get**](TemplatesApi.md#api_v1_templates_id_get) | **GET** /api/v1/templates/{id} | Şablon detay
[**api_v1_templates_id_patch**](TemplatesApi.md#api_v1_templates_id_patch) | **PATCH** /api/v1/templates/{id} | Şablon metadata güncelle
[**api_v1_templates_id_usage_get**](TemplatesApi.md#api_v1_templates_id_usage_get) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek)


# **api_v1_templates_get**
> ApiV1TemplatesGet200Response api_v1_templates_get(page=page, limit=limit)

Şablon listesi

Aktif şablonlarınızı listeler.

### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_templates_get200_response import ApiV1TemplatesGet200Response
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
    api_instance = imzala_client.TemplatesApi(api_client)
    page = 1 # int |  (optional) (default to 1)
    limit = 20 # int |  (optional) (default to 20)

    try:
        # Şablon listesi
        api_response = api_instance.api_v1_templates_get(page=page, limit=limit)
        print("The response of TemplatesApi->api_v1_templates_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling TemplatesApi->api_v1_templates_get: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **page** | **int**|  | [optional] [default to 1]
 **limit** | **int**|  | [optional] [default to 20]

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
**200** | Başarılı |  -  |
**401** | API key geçersiz veya eksik |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_templates_id_delete**
> ApiV1TemplatesIdDelete200Response api_v1_templates_id_delete(id)

Şablon sil

Şablonu siler (soft delete). Mevcut sözleşmeler etkilenmez.

### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_templates_id_delete200_response import ApiV1TemplatesIdDelete200Response
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
    api_instance = imzala_client.TemplatesApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 

    try:
        # Şablon sil
        api_response = api_instance.api_v1_templates_id_delete(id)
        print("The response of TemplatesApi->api_v1_templates_id_delete:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling TemplatesApi->api_v1_templates_id_delete: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 

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
**200** | Silindi |  -  |
**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_templates_id_get**
> ApiV1TemplatesIdGet200Response api_v1_templates_id_get(id)

Şablon detay

Şablonun parties + variables bilgisini döner. variables array'ı
tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date,
dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı
slug birden fazla partide olabilir, her parti için ayrı satır.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_templates_id_get200_response import ApiV1TemplatesIdGet200Response
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
    api_instance = imzala_client.TemplatesApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 

    try:
        # Şablon detay
        api_response = api_instance.api_v1_templates_id_get(id)
        print("The response of TemplatesApi->api_v1_templates_id_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling TemplatesApi->api_v1_templates_id_get: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 

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
**200** | Başarılı |  -  |
**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_templates_id_patch**
> ApiV1TemplatesIdPatch200Response api_v1_templates_id_patch(id, api_v1_templates_id_patch_request)

Şablon metadata güncelle

Şablonun yalnızca metadata alanlarını (name / description / category)
günceller. Sayfa/alan/taraf yapısı bu endpoint'ten DEĞİŞTİRİLEMEZ
(şablon içeriği panelden düzenlenir).


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_templates_id_patch200_response import ApiV1TemplatesIdPatch200Response
from imzala_client.models.api_v1_templates_id_patch_request import ApiV1TemplatesIdPatchRequest
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
    api_instance = imzala_client.TemplatesApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    api_v1_templates_id_patch_request = imzala_client.ApiV1TemplatesIdPatchRequest() # ApiV1TemplatesIdPatchRequest | 

    try:
        # Şablon metadata güncelle
        api_response = api_instance.api_v1_templates_id_patch(id, api_v1_templates_id_patch_request)
        print("The response of TemplatesApi->api_v1_templates_id_patch:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling TemplatesApi->api_v1_templates_id_patch: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 
 **api_v1_templates_id_patch_request** | [**ApiV1TemplatesIdPatchRequest**](ApiV1TemplatesIdPatchRequest.md)|  | 

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
**200** | Güncellendi |  -  |
**400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_templates_id_usage_get**
> ApiV1TemplatesIdUsageGet200Response api_v1_templates_id_usage_get(id)

Şablon kullanım kılavuzu (curl + JSON örnek)

Bu şablonu API üzerinden çağırmak için tam rehber döner:
- `endpoint` (POST URL'i)
- `required_headers` (X-API-Key, X-Workspace-Id, Content-Type)
- `parties` (her partinin desteklediği field listesi)
- `variables` (her field için slug, label, item_type, is_required,
  default_source, auto_filled, template_party_id)
- `example_request` (tam curl + JSON örneği, gerçek slug'larla)

Multi-party şablonlarda example.party_mapping[i].variables uygun
slug'larla doludur, root `variables` partisiz field'lar için.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_templates_id_usage_get200_response import ApiV1TemplatesIdUsageGet200Response
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
    api_instance = imzala_client.TemplatesApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 

    try:
        # Şablon kullanım kılavuzu (curl + JSON örnek)
        api_response = api_instance.api_v1_templates_id_usage_get(id)
        print("The response of TemplatesApi->api_v1_templates_id_usage_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling TemplatesApi->api_v1_templates_id_usage_get: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 

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
**200** | Başarılı |  -  |
**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

