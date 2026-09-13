# Imzala\Client\ReportsApi



All URIs are relative to https://api-prd.imzala.org, except if the operation defines another base path.

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**apiV1ReportsGet()**](ReportsApi.md#apiV1ReportsGet) | **GET** /api/v1/reports | Rapor / özet (agrege sözleşme durum sayıları) |


## `apiV1ReportsGet()`

```php
apiV1ReportsGet(): \Imzala\Client\Model\ApiV1ReportsGet200Response
```

Rapor / özet (agrege sözleşme durum sayıları)

API key sahibinin workspace'indeki sözleşmelerin agrege durum sayılarını döner (bekleyen / tamamlanan / iptal / süresi-dolmuş + bu ay oluşturulan). Yalnızca sayı; PII içermez. Kişisel workspace yalnızca sahibinin, org workspace org'un sözleşmelerini sayar (IDOR-safe, GET /demands ile aynı SSOT).

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\ReportsApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);

try {
    $result = $apiInstance->apiV1ReportsGet();
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling ReportsApi->apiV1ReportsGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**\Imzala\Client\Model\ApiV1ReportsGet200Response**](../Model/ApiV1ReportsGet200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)
