# Imzala\Client\TemplatesApi

Sözleşme şablonları

All URIs are relative to https://api-prd.imzala.org, except if the operation defines another base path.

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**apiV1TemplatesGet()**](TemplatesApi.md#apiV1TemplatesGet) | **GET** /api/v1/templates | Şablon listesi |
| [**apiV1TemplatesIdGet()**](TemplatesApi.md#apiV1TemplatesIdGet) | **GET** /api/v1/templates/{id} | Şablon detay |
| [**apiV1TemplatesIdUsageGet()**](TemplatesApi.md#apiV1TemplatesIdUsageGet) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek) |


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

## `apiV1TemplatesIdGet()`

```php
apiV1TemplatesIdGet($id): \Imzala\Client\Model\ApiV1TemplatesIdGet200Response
```

Şablon detay

Şablonun parties + variables bilgisini döner. variables array'ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır.

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
