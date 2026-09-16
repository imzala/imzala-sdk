# Imzala\Client\TemplatesApi

Sözleşme şablonları

All URIs are relative to https://api-prd.imzala.org, except if the operation defines another base path.

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**apiV1FieldTemplatesGet()**](TemplatesApi.md#apiV1FieldTemplatesGet) | **GET** /api/v1/field-templates | Alan Şablonlarını listeler |
| [**apiV1FieldTemplatesIdGet()**](TemplatesApi.md#apiV1FieldTemplatesIdGet) | **GET** /api/v1/field-templates/{id} | Alan Şablonu ayrıntısı (roller + alan sayıları) |
| [**apiV1TemplatesGet()**](TemplatesApi.md#apiV1TemplatesGet) | **GET** /api/v1/templates | Şablon listesi |
| [**apiV1TemplatesIdDelete()**](TemplatesApi.md#apiV1TemplatesIdDelete) | **DELETE** /api/v1/templates/{id} | Şablon sil |
| [**apiV1TemplatesIdGet()**](TemplatesApi.md#apiV1TemplatesIdGet) | **GET** /api/v1/templates/{id} | Şablon detay |
| [**apiV1TemplatesIdPatch()**](TemplatesApi.md#apiV1TemplatesIdPatch) | **PATCH** /api/v1/templates/{id} | Şablon metadata güncelle |
| [**apiV1TemplatesIdUsageGet()**](TemplatesApi.md#apiV1TemplatesIdUsageGet) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek) |


## `apiV1FieldTemplatesGet()`

```php
apiV1FieldTemplatesGet($page, $limit): \Imzala\Client\Model\ApiV1FieldTemplatesGet200Response
```

Alan Şablonlarını listeler

Hesabınızdaki (ya da seçili çalışma alanındaki) **Alan Şablonlarını** (`kind: FIELD_LAYOUT`) döner.  ### Neden ayrı bir uç  `GET /api/v1/templates` Alan Şablonlarını bilerek listelemez ve `GET /api/v1/templates/{id}` onlara `404` döner. Alan Şablonunun referans belgesi hiçbir imzacıya gönderilmez; belgeli şablon sanılması, o referans belgenin karşı tarafa gitmesi demek olurdu. Bu yüzden varsayılan davranış hariç tutmaktır ve Alan Şablonlarını görmek açık bir talep gerektirir.  Döndürülen `id`, `POST /api/v1/demands/upload` çağrısındaki `field_template_id` ve `POST /api/v1/field-templates/{id}/preview-layout` için kullanılır.  `parties` dizisi taraf sırasına göre döner; upload gövdesindeki `parties` dizisini bu sırayla kurun.

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\TemplatesApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$page = 1; // int
$limit = 20; // int

try {
    $result = $apiInstance->apiV1FieldTemplatesGet($page, $limit);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling TemplatesApi->apiV1FieldTemplatesGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **page** | **int**|  | [optional] [default to 1] |
| **limit** | **int**|  | [optional] [default to 20] |

### Return type

[**\Imzala\Client\Model\ApiV1FieldTemplatesGet200Response**](../Model/ApiV1FieldTemplatesGet200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1FieldTemplatesIdGet()`

```php
apiV1FieldTemplatesIdGet($id): \Imzala\Client\Model\ApiV1FieldTemplatesIdGet200Response
```

Alan Şablonu ayrıntısı (roller + alan sayıları)

Tek bir Alan Şablonunun rollerini ve rol başına alan sayısını döner.  🔴 **Alanların koordinatları dönmez.** Yerleşimin belgeye nasıl uygulanacağını görmek için kuru koşum ucunu kullanın: `POST /api/v1/field-templates/{id}/preview-layout`.  Belgeli bir şablonun kimliği verilirse `404` döner; şablonun türü ifşa edilmez. Var olmayan ve size ait olmayan kimlik de aynı yanıtı alır.

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\TemplatesApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$id = 'id_example'; // string | Alan Şablonu kimliği

try {
    $result = $apiInstance->apiV1FieldTemplatesIdGet($id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling TemplatesApi->apiV1FieldTemplatesIdGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**| Alan Şablonu kimliği | |

### Return type

[**\Imzala\Client\Model\ApiV1FieldTemplatesIdGet200Response**](../Model/ApiV1FieldTemplatesIdGet200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1TemplatesGet()`

```php
apiV1TemplatesGet($page, $limit): \Imzala\Client\Model\ApiV1TemplatesGet200Response
```

Şablon listesi

Aktif şablonlarınızı listeler.

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\TemplatesApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$page = 1; // int
$limit = 20; // int

try {
    $result = $apiInstance->apiV1TemplatesGet($page, $limit);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling TemplatesApi->apiV1TemplatesGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **page** | **int**|  | [optional] [default to 1] |
| **limit** | **int**|  | [optional] [default to 20] |

### Return type

[**\Imzala\Client\Model\ApiV1TemplatesGet200Response**](../Model/ApiV1TemplatesGet200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1TemplatesIdDelete()`

```php
apiV1TemplatesIdDelete($id): \Imzala\Client\Model\ApiV1TemplatesIdDelete200Response
```

Şablon sil

Şablonu siler. Silinen şablon 30 gün saklanır, sürenin sonunda kalıcı olarak silinir. Mevcut sözleşmeler etkilenmez.  Şablonun aktif (DRAFT veya PENDING) sözleşmesi varsa silinemez; 409 `TEMPLATE_IN_USE` döner.

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\TemplatesApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$id = 'id_example'; // string

try {
    $result = $apiInstance->apiV1TemplatesIdDelete($id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling TemplatesApi->apiV1TemplatesIdDelete: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |

### Return type

[**\Imzala\Client\Model\ApiV1TemplatesIdDelete200Response**](../Model/ApiV1TemplatesIdDelete200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1TemplatesIdGet()`

```php
apiV1TemplatesIdGet($id): \Imzala\Client\Model\ApiV1TemplatesIdGet200Response
```

Şablon detay

Şablonun parties + variables bilgisini döner. variables array'ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır.  `documents` şablonun zarf belgelerini `order` sırasıyla döndürür; `documents[].id` sözleşme oluştururken `documents.include` / `documents.exclude` listelerinde kullanılır.

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\TemplatesApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$id = 'id_example'; // string

try {
    $result = $apiInstance->apiV1TemplatesIdGet($id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling TemplatesApi->apiV1TemplatesIdGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |

### Return type

[**\Imzala\Client\Model\ApiV1TemplatesIdGet200Response**](../Model/ApiV1TemplatesIdGet200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1TemplatesIdPatch()`

```php
apiV1TemplatesIdPatch($id, $api_v1_templates_id_patch_request): \Imzala\Client\Model\ApiV1TemplatesIdPatch200Response
```

Şablon metadata güncelle

Şablonun yalnızca metadata alanlarını (name / description / category) günceller. Sayfa/alan/taraf yapısı bu endpoint'ten DEĞİŞTİRİLEMEZ (şablon içeriği panelden düzenlenir).

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\TemplatesApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$id = 'id_example'; // string
$api_v1_templates_id_patch_request = new \Imzala\Client\Model\ApiV1TemplatesIdPatchRequest(); // \Imzala\Client\Model\ApiV1TemplatesIdPatchRequest

try {
    $result = $apiInstance->apiV1TemplatesIdPatch($id, $api_v1_templates_id_patch_request);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling TemplatesApi->apiV1TemplatesIdPatch: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |
| **api_v1_templates_id_patch_request** | [**\Imzala\Client\Model\ApiV1TemplatesIdPatchRequest**](../Model/ApiV1TemplatesIdPatchRequest.md)|  | |

### Return type

[**\Imzala\Client\Model\ApiV1TemplatesIdPatch200Response**](../Model/ApiV1TemplatesIdPatch200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1TemplatesIdUsageGet()`

```php
apiV1TemplatesIdUsageGet($id): \Imzala\Client\Model\ApiV1TemplatesIdUsageGet200Response
```

Şablon kullanım kılavuzu (curl + JSON örnek)

Bu şablonu API üzerinden çağırmak için tam rehber döner: - `endpoint` (POST URL'i) - `required_headers` (X-API-Key, X-Workspace-Id, Content-Type) - `parties` (her partinin desteklediği field listesi) - `variables` (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - `example_request` (tam curl + JSON örneği, gerçek slug'larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug'larla doludur, root `variables` partisiz field'lar için.

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\TemplatesApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$id = 'id_example'; // string

try {
    $result = $apiInstance->apiV1TemplatesIdUsageGet($id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling TemplatesApi->apiV1TemplatesIdUsageGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |

### Return type

[**\Imzala\Client\Model\ApiV1TemplatesIdUsageGet200Response**](../Model/ApiV1TemplatesIdUsageGet200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)
