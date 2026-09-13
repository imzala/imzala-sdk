# ContactsApi

All URIs are relative to *https://api-prd.imzala.org*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiV1ContactsGet**](#apiv1contactsget) | **GET** /api/v1/contacts | Kişi listesi|
|[**apiV1ContactsPost**](#apiv1contactspost) | **POST** /api/v1/contacts | Kişi oluştur|

# **apiV1ContactsGet**
> ApiV1ContactsGet200Response apiV1ContactsGet()

API key sahibinin workspace\'indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org\'un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 

### Example

```typescript
import {
    ContactsApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new ContactsApi(configuration);

let page: number; // (optional) (default to 1)
let limit: number; // (optional) (default to 25)
let q: string; //Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) (optional) (default to undefined)
let sort: string; //Sıralama: createdAt | updatedAt | first_name | last_name | email (\'-\' öneki azalan) (optional) (default to '-createdAt')
let companyId: string; // (optional) (default to undefined)
let archived: boolean; //true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) (optional) (default to false)

const { status, data } = await apiInstance.apiV1ContactsGet(
    page,
    limit,
    q,
    sort,
    companyId,
    archived
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **page** | [**number**] |  | (optional) defaults to 1|
| **limit** | [**number**] |  | (optional) defaults to 25|
| **q** | [**string**] | Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) | (optional) defaults to undefined|
| **sort** | [**string**] | Sıralama: createdAt | updatedAt | first_name | last_name | email (\&#39;-\&#39; öneki azalan) | (optional) defaults to '-createdAt'|
| **companyId** | [**string**] |  | (optional) defaults to undefined|
| **archived** | [**boolean**] | true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) | (optional) defaults to false|


### Return type

**ApiV1ContactsGet200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Başarılı |  -  |
|**401** | API key geçersiz veya eksik |  -  |
|**403** | INSUFFICIENT_SCOPE — API key\&#39;de contacts:read scope yok |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1ContactsPost**
> ApiV1ContactsPost201Response apiV1ContactsPost(apiV1ContactsPostRequest)

Workspace\'e yeni bir kişi ekler. Aynı workspace\'te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 

### Example

```typescript
import {
    ContactsApi,
    Configuration,
    ApiV1ContactsPostRequest
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new ContactsApi(configuration);

let apiV1ContactsPostRequest: ApiV1ContactsPostRequest; //

const { status, data } = await apiInstance.apiV1ContactsPost(
    apiV1ContactsPostRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1ContactsPostRequest** | **ApiV1ContactsPostRequest**|  | |


### Return type

**ApiV1ContactsPost201Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | Kişi oluşturuldu |  -  |
|**400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value\&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate\&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
|**401** | API key geçersiz veya eksik |  -  |
|**403** | INSUFFICIENT_SCOPE — API key\&#39;de contacts:write scope yok |  -  |
|**409** | CONTACT_DUPLICATE — aynı e-posta/telefona sahip kişi zaten var |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

