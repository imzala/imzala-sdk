# TemplatesApi

All URIs are relative to *https://api-prd.imzala.org*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiV1FieldTemplatesGet**](#apiv1fieldtemplatesget) | **GET** /api/v1/field-templates | Alan Şablonlarını listeler|
|[**apiV1FieldTemplatesIdGet**](#apiv1fieldtemplatesidget) | **GET** /api/v1/field-templates/{id} | Alan Şablonu ayrıntısı (roller + alan sayıları)|
|[**apiV1TemplatesGet**](#apiv1templatesget) | **GET** /api/v1/templates | Şablon listesi|
|[**apiV1TemplatesIdDelete**](#apiv1templatesiddelete) | **DELETE** /api/v1/templates/{id} | Şablon sil|
|[**apiV1TemplatesIdGet**](#apiv1templatesidget) | **GET** /api/v1/templates/{id} | Şablon detay|
|[**apiV1TemplatesIdPatch**](#apiv1templatesidpatch) | **PATCH** /api/v1/templates/{id} | Şablon metadata güncelle|
|[**apiV1TemplatesIdUsageGet**](#apiv1templatesidusageget) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek)|

# **apiV1FieldTemplatesGet**
> ApiV1FieldTemplatesGet200Response apiV1FieldTemplatesGet()

Hesabınızdaki (ya da seçili çalışma alanındaki) **Alan Şablonlarını** (`kind: FIELD_LAYOUT`) döner.  ### Neden ayrı bir uç  `GET /api/v1/templates` Alan Şablonlarını bilerek listelemez ve `GET /api/v1/templates/{id}` onlara `404` döner. Alan Şablonunun referans belgesi hiçbir imzacıya gönderilmez; belgeli şablon sanılması, o referans belgenin karşı tarafa gitmesi demek olurdu. Bu yüzden varsayılan davranış hariç tutmaktır ve Alan Şablonlarını görmek açık bir talep gerektirir.  Döndürülen `id`, `POST /api/v1/demands/upload` çağrısındaki `field_template_id` ve `POST /api/v1/field-templates/{id}/preview-layout` için kullanılır.  `parties` dizisi taraf sırasına göre döner; upload gövdesindeki `parties` dizisini bu sırayla kurun. 

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

const { status, data } = await apiInstance.apiV1FieldTemplatesGet(
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

**ApiV1FieldTemplatesGet200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Alan Şablonu listesi |  -  |
|**400** | &#x60;INVALID_PAGE&#x60;: &#x60;page&#x60; 1 veya daha büyük bir tam sayı değil. &#x60;limit&#x60; hata üretmez: 1 ile 100 arasına kırpılır, sayı değilse varsayılan 20 kullanılır.  |  -  |
|**401** | API key geçersiz veya eksik |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1FieldTemplatesIdGet**
> ApiV1FieldTemplatesIdGet200Response apiV1FieldTemplatesIdGet()

Tek bir Alan Şablonunun rollerini ve rol başına alan sayısını döner.  🔴 **Alanların koordinatları dönmez.** Yerleşimin belgeye nasıl uygulanacağını görmek için kuru koşum ucunu kullanın: `POST /api/v1/field-templates/{id}/preview-layout`.  Belgeli bir şablonun kimliği verilirse `404` döner; şablonun türü ifşa edilmez. Var olmayan ve size ait olmayan kimlik de aynı yanıtı alır. 

### Example

```typescript
import {
    TemplatesApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new TemplatesApi(configuration);

let id: string; //Alan Şablonu kimliği (default to undefined)

const { status, data } = await apiInstance.apiV1FieldTemplatesIdGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] | Alan Şablonu kimliği | defaults to undefined|


### Return type

**ApiV1FieldTemplatesIdGet200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Alan Şablonu ayrıntısı |  -  |
|**401** | API key geçersiz veya eksik |  -  |
|**404** | &#x60;TEMPLATE_NOT_FOUND&#x60; — şablon bulunamadı, size ait değil ya da Alan Şablonu değil.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

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
|**400** | &#x60;INVALID_PAGE&#x60;: &#x60;page&#x60; 1 veya daha büyük bir tam sayı değil. &#x60;limit&#x60; hata üretmez: 1 ile 100 arasına kırpılır, sayı değilse varsayılan 20 kullanılır.  |  -  |
|**401** | API key geçersiz veya eksik |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesIdDelete**
> ApiV1TemplatesIdDelete200Response apiV1TemplatesIdDelete()

Şablonu siler. Silinen şablon 30 gün saklanır, sürenin sonunda kalıcı olarak silinir. Mevcut sözleşmeler etkilenmez.  Şablonun aktif (DRAFT veya PENDING) sözleşmesi varsa silinemez; 409 `TEMPLATE_IN_USE` döner. 

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
|**409** | &#x60;TEMPLATE_IN_USE&#x60;: şablonun aktif (DRAFT veya PENDING) sözleşmesi olduğu için silinemez. Önce o sözleşmeleri sonlandırın.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesIdGet**
> ApiV1TemplatesIdGet200Response apiV1TemplatesIdGet()

Şablonun parties + variables bilgisini döner. variables array\'ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır.  `documents` şablonun zarf belgelerini `order` sırasıyla döndürür; `documents[].id` sözleşme oluştururken `documents.include` / `documents.exclude` listelerinde kullanılır. 

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

