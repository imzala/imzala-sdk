# imzala_client.ContactsApi

All URIs are relative to *https://api-prd.imzala.org*

Method | HTTP request | Description
------------- | ------------- | -------------
[**api_v1_contacts_get**](ContactsApi.md#api_v1_contacts_get) | **GET** /api/v1/contacts | Kişi listesi
[**api_v1_contacts_post**](ContactsApi.md#api_v1_contacts_post) | **POST** /api/v1/contacts | Kişi oluştur


# **api_v1_contacts_get**
> ApiV1ContactsGet200Response api_v1_contacts_get(page=page, limit=limit, q=q, sort=sort, company_id=company_id, archived=archived)

Kişi listesi

API key sahibinin workspace'indeki kişileri (Contact / adres defteri)
listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace
org'un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları
yanıta HİÇ konmaz.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_contacts_get200_response import ApiV1ContactsGet200Response
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
    api_instance = imzala_client.ContactsApi(api_client)
    page = 1 # int |  (optional) (default to 1)
    limit = 25 # int |  (optional) (default to 25)
    q = 'q_example' # str | Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) (optional)
    sort = '-createdAt' # str | Sıralama: createdAt | updatedAt | first_name | last_name | email ('-' öneki azalan) (optional) (default to '-createdAt')
    company_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID |  (optional)
    archived = False # bool | true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) (optional) (default to False)

    try:
        # Kişi listesi
        api_response = api_instance.api_v1_contacts_get(page=page, limit=limit, q=q, sort=sort, company_id=company_id, archived=archived)
        print("The response of ContactsApi->api_v1_contacts_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ContactsApi->api_v1_contacts_get: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **page** | **int**|  | [optional] [default to 1]
 **limit** | **int**|  | [optional] [default to 25]
 **q** | **str**| Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) | [optional] 
 **sort** | **str**| Sıralama: createdAt | updatedAt | first_name | last_name | email (&#39;-&#39; öneki azalan) | [optional] [default to &#39;-createdAt&#39;]
 **company_id** | **UUID**|  | [optional] 
 **archived** | **bool**| true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) | [optional] [default to False]

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
**200** | Başarılı |  -  |
**401** | API key geçersiz veya eksik |  -  |
**403** | INSUFFICIENT_SCOPE — API key&#39;de contacts:read scope yok |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_contacts_post**
> ApiV1ContactsPost201Response api_v1_contacts_post(api_v1_contacts_post_request)

Kişi oluştur

Workspace'e yeni bir kişi ekler. Aynı workspace'te aynı e-posta veya
telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.

🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya
yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve
hukuki dayanak yükümlülüğü kendisine aittir.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_contacts_post201_response import ApiV1ContactsPost201Response
from imzala_client.models.api_v1_contacts_post_request import ApiV1ContactsPostRequest
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
    api_instance = imzala_client.ContactsApi(api_client)
    api_v1_contacts_post_request = imzala_client.ApiV1ContactsPostRequest() # ApiV1ContactsPostRequest | 

    try:
        # Kişi oluştur
        api_response = api_instance.api_v1_contacts_post(api_v1_contacts_post_request)
        print("The response of ContactsApi->api_v1_contacts_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ContactsApi->api_v1_contacts_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **api_v1_contacts_post_request** | [**ApiV1ContactsPostRequest**](ApiV1ContactsPostRequest.md)|  | 

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
**201** | Kişi oluşturuldu |  -  |
**400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
**401** | API key geçersiz veya eksik |  -  |
**403** | INSUFFICIENT_SCOPE — API key&#39;de contacts:write scope yok |  -  |
**409** | CONTACT_DUPLICATE — aynı e-posta/telefona sahip kişi zaten var |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

