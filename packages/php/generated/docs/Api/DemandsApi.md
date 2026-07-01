# Imzala\Client\DemandsApi

Sözleşme talepleri

All URIs are relative to https://api-prd.imzala.org, except if the operation defines another base path.

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**apiV1DemandsIdEmbedSessionPost()**](DemandsApi.md#apiV1DemandsIdEmbedSessionPost) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint) |
| [**apiV1DemandsIdGet()**](DemandsApi.md#apiV1DemandsIdGet) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi |
| [**apiV1DemandsIdItemsPost()**](DemandsApi.md#apiV1DemandsIdItemsPost) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace) |
| [**apiV1DemandsPost()**](DemandsApi.md#apiV1DemandsPost) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan) |
| [**apiV1DemandsUploadPost()**](DemandsApi.md#apiV1DemandsUploadPost) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz) |


## `apiV1DemandsIdEmbedSessionPost()`

```php
apiV1DemandsIdEmbedSessionPost($id, $api_v1_demands_id_embed_session_post_request): \Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPost200Response
```

Gömülü imza oturumu başlat (embed token mint)

Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token'ı üretir. Dönen `embed_url` bir `<iframe>` içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \"güvenli\" veya \"nitelikli\" sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur. - `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış   izin verilen origin'ler dışından `<iframe>` açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** `X-Workspace-Id` header'ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace'in sözleşmesi için 404 döner (IDOR koruması).

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\DemandsApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$id = 'id_example'; // string | Sözleşme (demand) ID
$api_v1_demands_id_embed_session_post_request = {"party_id":"f47ac10b-58cc-4372-a567-0e02b2c3d479"}; // \Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPostRequest

try {
    $result = $apiInstance->apiV1DemandsIdEmbedSessionPost($id, $api_v1_demands_id_embed_session_post_request);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdEmbedSessionPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**| Sözleşme (demand) ID | |
| **api_v1_demands_id_embed_session_post_request** | [**\Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPostRequest**](../Model/ApiV1DemandsIdEmbedSessionPostRequest.md)|  | |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPost200Response**](../Model/ApiV1DemandsIdEmbedSessionPost200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsIdGet()`

```php
apiV1DemandsIdGet($id): \Imzala\Client\Model\ApiV1DemandsIdGet200Response
```

Sözleşme durumu + imza ilerlemesi

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\DemandsApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$id = 'id_example'; // string

try {
    $result = $apiInstance->apiV1DemandsIdGet($id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsIdGet200Response**](../Model/ApiV1DemandsIdGet200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsIdItemsPost()`

```php
apiV1DemandsIdItemsPost($id, $upsert_items_request): \Imzala\Client\Model\UpsertItemsResponse
```

Sözleşmeye alan yerleştir (replace)

Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile demand yarat (`dispatch_notifications=false` ile auto-dispatch'i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - `page_ids` **omitted** → demand'in TÜM mevcut item'ları silinir,   body'dekiler yaratılır (full replace). - `page_ids: [N, M, ...]` → sadece bu sayfaların item'ları silinir,   diğer sayfalardaki item'lar korunur. Body'deki `items[].page_id`   değerleri `page_ids` listesinde olmalıdır.  ### Item type'ları  | `item_type` | `party_id` zorunlu? | `config` örneği | |-------------|---------------------|-----------------| | `signature` | ✅ | (yok) | | `text` | ❌ | `{ default_content }` | | `dynamic_text` | ✅ | `{ defaultSource: \"{{signer.full_name}}\" }` | | `cells` | ✅ | `{ cellCount: 11, defaultSource: \"{{signer.government_id}}\" }` | | `date` | ✅ | `{ defaultSource, defaultValue }` | | `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` | | `checkbox` | ✅ | `{ checkedByDefault: false }` | | `radio` | ✅ | `{ options: [{label,value}], defaultValue }` | | `stamp` | ❌ | `{ stampData: \"data:image/png;base64,...\" }` |  ### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)  `{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`, `{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`, `{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`, `{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.  ### Workspace izolasyonu  X-API-Key middleware demand'i workspace'e göre filtreler; başka workspace'in demand'ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`, `REJECTED` için 403.  ### Örnek  ```bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{     \"items\": [       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"signature\",         \"position_x\": 0.5, \"position_y\": 0.85,         \"width\": 0.2, \"height\": 0.05,         \"is_required\": true       },       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"cells\",         \"position_x\": 0.1, \"position_y\": 0.5,         \"width\": 0.4, \"height\": 0.04,         \"slug\": \"tc\",         \"config\": { \"cellCount\": 11, \"defaultSource\": \"{{signer.government_id}}\" }       }     ]   }' ```

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\DemandsApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$id = 'id_example'; // string
$upsert_items_request = new \Imzala\Client\Model\UpsertItemsRequest(); // \Imzala\Client\Model\UpsertItemsRequest

try {
    $result = $apiInstance->apiV1DemandsIdItemsPost($id, $upsert_items_request);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdItemsPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |
| **upsert_items_request** | [**\Imzala\Client\Model\UpsertItemsRequest**](../Model/UpsertItemsRequest.md)|  | |

### Return type

[**\Imzala\Client\Model\UpsertItemsResponse**](../Model/UpsertItemsResponse.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsPost()`

```php
apiV1DemandsPost($create_demand_request): \Imzala\Client\Model\ApiV1DemandsPost201Response
```

Sözleşme oluştur (şablondan)

Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field'ları `variables` payload'undan doldurur ve imzalama URL'lerini döner.  **Variable resolution:** - Item'ın `template_party_id` non-null → `party_mapping[i].variables`'ta   o slug var ise oradan uygulanır - Yoksa root `variables`'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\DemandsApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$create_demand_request = {"template_id":"7ec4b653-e84a-47f1-9e0b-7671e1aae2a1","title":"Vize Danışmanlığı - Ada Kalkan","party_mapping":[{"template_party_id":"e5b4e0cb-c2d5-473f-9f62-44d51c76f56e","first_name":"Ada","last_name":"Kalkan","email":"ada@example.com","phone":"+905304636743","government_id":"36747474747","variables":{"adres":"Atatürk Cad. No: 12, Çankaya/Ankara","danismanlik_ucreti":"5.000 TL","danismanlik_notlar":"Schengen vize başvuru danışmanlığı","randevu_takibi_ucreti":"1.500 TL","randevu_takibi_notlar":"Konsolosluk randevu takibi 60 gün","genel_toplam":"6.500 TL"}}]}; // \Imzala\Client\Model\CreateDemandRequest

try {
    $result = $apiInstance->apiV1DemandsPost($create_demand_request);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **create_demand_request** | [**\Imzala\Client\Model\CreateDemandRequest**](../Model/CreateDemandRequest.md)|  | |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsPost201Response**](../Model/ApiV1DemandsPost201Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsUploadPost()`

```php
apiV1DemandsUploadPost($files, $parties, $order, $title, $description): \Imzala\Client\Model\ApiV1DemandsUploadPost201Response
```

Dosya upload ile sözleşme oluştur (şablonsuz)

Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF'e birleştirilir, office formatları LibreOffice ile PDF'e çevrilir.

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\DemandsApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$files = array('/path/to/file.txt'); // \SplFileObject[] | 1 belge VEYA 1-20 görsel
$parties = 'parties_example'; // string | JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).
$order = 'order_example'; // string | Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\")
$title = 'title_example'; // string
$description = 'description_example'; // string

try {
    $result = $apiInstance->apiV1DemandsUploadPost($files, $parties, $order, $title, $description);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsUploadPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **files** | **\SplFileObject[]**| 1 belge VEYA 1-20 görsel | |
| **parties** | **string**| JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu). | |
| **order** | **string**| Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) | [optional] |
| **title** | **string**|  | [optional] |
| **description** | **string**|  | [optional] |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsUploadPost201Response**](../Model/ApiV1DemandsUploadPost201Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `multipart/form-data`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)
