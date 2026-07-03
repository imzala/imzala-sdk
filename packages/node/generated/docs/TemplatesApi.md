# TemplatesApi

All URIs are relative to *https://api-prd.imzala.org*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiV1TemplatesGet**](#apiv1templatesget) | **GET** /api/v1/templates | Şablon listesi|
|[**apiV1TemplatesIdDelete**](#apiv1templatesiddelete) | **DELETE** /api/v1/templates/{id} | Şablon sil|
|[**apiV1TemplatesIdGet**](#apiv1templatesidget) | **GET** /api/v1/templates/{id} | Şablon detay|
|[**apiV1TemplatesIdPatch**](#apiv1templatesidpatch) | **PATCH** /api/v1/templates/{id} | Şablon metadata güncelle|
|[**apiV1TemplatesIdUsageGet**](#apiv1templatesidusageget) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek)|

# **apiV1TemplatesGet**
> ApiV1TemplatesGet200Response apiV1TemplatesGet()

Aktif şablonlarınızı listeler.

### Example

```typescript
import {
    TemplatesApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new TemplatesApi(configuration);

let page: number; // (optional) (default to 1)
let limit: number; // (optional) (default to 20)

const { status, data } = await apiInstance.apiV1TemplatesGet(
    page,
    limit
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **page** | [**number**] |  | (optional) defaults to 1|
| **limit** | [**number**] |  | (optional) defaults to 20|


### Return type

**ApiV1TemplatesGet200Response**

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

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesIdDelete**
> ApiV1TemplatesIdDelete200Response apiV1TemplatesIdDelete()

Şablonu siler (soft delete). Mevcut sözleşmeler etkilenmez.

### Example

```typescript
import {
    TemplatesApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new TemplatesApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1TemplatesIdDelete(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**ApiV1TemplatesIdDelete200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Silindi |  -  |
|**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesIdGet**
> ApiV1TemplatesIdGet200Response apiV1TemplatesIdGet()

Şablonun parties + variables bilgisini döner. variables array\'ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 

### Example

```typescript
import {
    TemplatesApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new TemplatesApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1TemplatesIdGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**ApiV1TemplatesIdGet200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Başarılı |  -  |
|**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesIdPatch**
> ApiV1TemplatesIdPatch200Response apiV1TemplatesIdPatch(apiV1TemplatesIdPatchRequest)

Şablonun yalnızca metadata alanlarını (name / description / category) günceller. Sayfa/alan/taraf yapısı bu endpoint\'ten DEĞİŞTİRİLEMEZ (şablon içeriği panelden düzenlenir). 

### Example

```typescript
import {
    TemplatesApi,
    Configuration,
    ApiV1TemplatesIdPatchRequest
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new TemplatesApi(configuration);

let id: string; // (default to undefined)
let apiV1TemplatesIdPatchRequest: ApiV1TemplatesIdPatchRequest; //

const { status, data } = await apiInstance.apiV1TemplatesIdPatch(
    id,
    apiV1TemplatesIdPatchRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1TemplatesIdPatchRequest** | **ApiV1TemplatesIdPatchRequest**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**ApiV1TemplatesIdPatch200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Güncellendi |  -  |
|**400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value\&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate\&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
|**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesIdUsageGet**
> ApiV1TemplatesIdUsageGet200Response apiV1TemplatesIdUsageGet()

Bu şablonu API üzerinden çağırmak için tam rehber döner: - `endpoint` (POST URL\'i) - `required_headers` (X-API-Key, X-Workspace-Id, Content-Type) - `parties` (her partinin desteklediği field listesi) - `variables` (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - `example_request` (tam curl + JSON örneği, gerçek slug\'larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug\'larla doludur, root `variables` partisiz field\'lar için. 

### Example

```typescript
import {
    TemplatesApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new TemplatesApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1TemplatesIdUsageGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**ApiV1TemplatesIdUsageGet200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Başarılı |  -  |
|**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

