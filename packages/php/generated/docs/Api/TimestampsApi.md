# Imzala\Client\TimestampsApi

RFC 3161 zaman damgası (eser tescil). Dosyanın SHA-256 hash&#39;i TÜBİTAK KAMU SM TSA tarafından imzalanır; bu belgenin belirtilen tarihte var olduğunu ve içeriğinin değişmediğini kanıtlar. Yazarlık veya sahiplik kanıtı DEĞİLDİR.

All URIs are relative to https://api-prd.imzala.org, except if the operation defines another base path.

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**apiV1TimestampsPost()**](TimestampsApi.md#apiV1TimestampsPost) | **POST** /api/v1/timestamps | Zaman damgası oluştur (eser tescil) |


## `apiV1TimestampsPost()`

```php
apiV1TimestampsPost($file, $idempotency_key, $description, $owner_first_name, $owner_last_name): \Imzala\Client\Model\ApiV1TimestampsPost201Response
```

Zaman damgası oluştur (eser tescil)

Dosyanın SHA-256 hash'ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - `owner_first_name` / `owner_last_name` alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Nitelikli elektronik imza (QES) için ayrı imzalama akışını kullanın.  **İdempotency:** `Idempotency-Key` header'ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı `id` döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - `multipart/form-data`: `file` alanıyla ikili dosya yükleme - `application/json`: `file_base64` alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez)

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\TimestampsApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$file = '/path/to/file.txt'; // \SplFileObject | Damgalanacak dosya (maks. 50 MB)
$idempotency_key = 'idempotency_key_example'; // string | Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.
$description = 'description_example'; // string | Kayıt açıklaması (opsiyonel, max 500 karakter)
$owner_first_name = 'owner_first_name_example'; // string | Dosya sahibinin adı (opsiyonel, kullanıcı beyanı)
$owner_last_name = 'owner_last_name_example'; // string | Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı)

try {
    $result = $apiInstance->apiV1TimestampsPost($file, $idempotency_key, $description, $owner_first_name, $owner_last_name);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling TimestampsApi->apiV1TimestampsPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **file** | **\SplFileObject****\SplFileObject**| Damgalanacak dosya (maks. 50 MB) | |
| **idempotency_key** | **string**| Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez. | [optional] |
| **description** | **string**| Kayıt açıklaması (opsiyonel, max 500 karakter) | [optional] |
| **owner_first_name** | **string**| Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) | [optional] |
| **owner_last_name** | **string**| Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) | [optional] |

### Return type

[**\Imzala\Client\Model\ApiV1TimestampsPost201Response**](../Model/ApiV1TimestampsPost201Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `multipart/form-data`, `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)
