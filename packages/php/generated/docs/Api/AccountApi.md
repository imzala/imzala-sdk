# Imzala\Client\AccountApi

API key sahibinin kullanıcı ve workspace bilgisi, kredi bakiyesi.

All URIs are relative to https://api-prd.imzala.org, except if the operation defines another base path.

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**apiV1MeGet()**](AccountApi.md#apiV1MeGet) | **GET** /api/v1/me | API key sahibi bilgisi |


## `apiV1MeGet()`

```php
apiV1MeGet(): \Imzala\Client\Model\ApiV1MeGet200Response
```

API key sahibi bilgisi

Çağrıyı yapan API key'in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. `timestamps` scope'u gerektirir; scope yoksa 403 `INSUFFICIENT_SCOPE` döner.

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\AccountApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);

try {
    $result = $apiInstance->apiV1MeGet();
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling AccountApi->apiV1MeGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**\Imzala\Client\Model\ApiV1MeGet200Response**](../Model/ApiV1MeGet200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)
