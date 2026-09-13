# Imzala\Client\ContactsApi

Kişi (adres defteri) yönetimi. Workspace-scoped: kişisel workspace yalnızca sahibine, org workspace org üyelerine görünür. Yanıtlar iç eşleşme ve şifreleme alanlarını içermez.

All URIs are relative to https://api-prd.imzala.org, except if the operation defines another base path.

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**apiV1ContactsGet()**](ContactsApi.md#apiV1ContactsGet) | **GET** /api/v1/contacts | Kişi listesi |
| [**apiV1ContactsPost()**](ContactsApi.md#apiV1ContactsPost) | **POST** /api/v1/contacts | Kişi oluştur |


## `apiV1ContactsGet()`

```php
apiV1ContactsGet($page, $limit, $q, $sort, $company_id, $archived): \Imzala\Client\Model\ApiV1ContactsGet200Response
```

Kişi listesi

API key sahibinin workspace'indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org'un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz.

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\ContactsApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$page = 1; // int
$limit = 25; // int
$q = 'q_example'; // string | Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter)
$sort = '-createdAt'; // string | Sıralama: createdAt | updatedAt | first_name | last_name | email ('-' öneki azalan)
$company_id = 'company_id_example'; // string
$archived = false; // bool | true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif)

try {
    $result = $apiInstance->apiV1ContactsGet($page, $limit, $q, $sort, $company_id, $archived);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling ContactsApi->apiV1ContactsGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **page** | **int**|  | [optional] [default to 1] |
| **limit** | **int**|  | [optional] [default to 25] |
| **q** | **string**| Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) | [optional] |
| **sort** | **string**| Sıralama: createdAt | updatedAt | first_name | last_name | email (&#39;-&#39; öneki azalan) | [optional] [default to &#39;-createdAt&#39;] |
| **company_id** | **string**|  | [optional] |
| **archived** | **bool**| true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) | [optional] [default to false] |

### Return type

[**\Imzala\Client\Model\ApiV1ContactsGet200Response**](../Model/ApiV1ContactsGet200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1ContactsPost()`

```php
apiV1ContactsPost($api_v1_contacts_post_request): \Imzala\Client\Model\ApiV1ContactsPost201Response
```

Kişi oluştur

Workspace'e yeni bir kişi ekler. Aynı workspace'te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir.

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\ContactsApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$api_v1_contacts_post_request = new \Imzala\Client\Model\ApiV1ContactsPostRequest(); // \Imzala\Client\Model\ApiV1ContactsPostRequest

try {
    $result = $apiInstance->apiV1ContactsPost($api_v1_contacts_post_request);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling ContactsApi->apiV1ContactsPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **api_v1_contacts_post_request** | [**\Imzala\Client\Model\ApiV1ContactsPostRequest**](../Model/ApiV1ContactsPostRequest.md)|  | |

### Return type

[**\Imzala\Client\Model\ApiV1ContactsPost201Response**](../Model/ApiV1ContactsPost201Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)
