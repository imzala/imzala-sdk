# Imzala\Client\DemandsApi

Sözleşme talepleri

All URIs are relative to https://api-prd.imzala.org, except if the operation defines another base path.

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**apiV1DemandsBulkPost()**](DemandsApi.md#apiV1DemandsBulkPost) | **POST** /api/v1/demands/bulk | Toplu sözleşme oluştur (tek şablondan N alıcı) |
| [**apiV1DemandsDemandIdDispatchPost()**](DemandsApi.md#apiV1DemandsDemandIdDispatchPost) | **POST** /api/v1/demands/{demandId}/dispatch | Zarfı imzaya gönder (yayınla + davet) |
| [**apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut()**](DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut) | **PUT** /api/v1/demands/{demandId}/documents/{docId}/assignments | Belgeye imzacı ata (tam-küme replace) |
| [**apiV1DemandsDemandIdDocumentsDocIdDelete()**](DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdDelete) | **DELETE** /api/v1/demands/{demandId}/documents/{docId} | Belgeyi zarftan sil |
| [**apiV1DemandsDemandIdDocumentsDocIdPatch()**](DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdPatch) | **PATCH** /api/v1/demands/{demandId}/documents/{docId} | Belge metadata güncelle |
| [**apiV1DemandsDemandIdDocumentsGet()**](DemandsApi.md#apiV1DemandsDemandIdDocumentsGet) | **GET** /api/v1/demands/{demandId}/documents | Zarf belge listesi |
| [**apiV1DemandsDemandIdDocumentsOrderPut()**](DemandsApi.md#apiV1DemandsDemandIdDocumentsOrderPut) | **PUT** /api/v1/demands/{demandId}/documents/order | Zarftaki belgelerin sırasını değiştir |
| [**apiV1DemandsDemandIdDocumentsPost()**](DemandsApi.md#apiV1DemandsDemandIdDocumentsPost) | **POST** /api/v1/demands/{demandId}/documents | Zarfa metadata-only belge ekle |
| [**apiV1DemandsDemandIdDocumentsUploadPost()**](DemandsApi.md#apiV1DemandsDemandIdDocumentsUploadPost) | **POST** /api/v1/demands/{demandId}/documents/upload | Zarfa dosya yükle (belge başına tek dosya) |
| [**apiV1DemandsGet()**](DemandsApi.md#apiV1DemandsGet) | **GET** /api/v1/demands | Sözleşme listesi (counts-only, PII&#39;siz) |
| [**apiV1DemandsIdArchivePost()**](DemandsApi.md#apiV1DemandsIdArchivePost) | **POST** /api/v1/demands/{id}/archive | Sözleşmeyi arşivle |
| [**apiV1DemandsIdBelgeDocumentIdPdfGet()**](DemandsApi.md#apiV1DemandsIdBelgeDocumentIdPdfGet) | **GET** /api/v1/demands/{id}/belge/{document_id}/pdf | Belge-özgü imzalı PDF (çok-belgeli zarf) |
| [**apiV1DemandsIdCancelPost()**](DemandsApi.md#apiV1DemandsIdCancelPost) | **POST** /api/v1/demands/{id}/cancel | Sözleşme iptal (void) |
| [**apiV1DemandsIdCertificateGet()**](DemandsApi.md#apiV1DemandsIdCertificateGet) | **GET** /api/v1/demands/{id}/certificate | Tamamlanma sertifikası (PAdES B-T) |
| [**apiV1DemandsIdDelete()**](DemandsApi.md#apiV1DemandsIdDelete) | **DELETE** /api/v1/demands/{id} | Sözleşme sil (yalnızca tamamlanmamış) |
| [**apiV1DemandsIdEmbedSessionPost()**](DemandsApi.md#apiV1DemandsIdEmbedSessionPost) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint) |
| [**apiV1DemandsIdGet()**](DemandsApi.md#apiV1DemandsIdGet) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi |
| [**apiV1DemandsIdItemsItemIdStampPatch()**](DemandsApi.md#apiV1DemandsIdItemsItemIdStampPatch) | **PATCH** /api/v1/demands/{id}/items/{itemId}/stamp | Kaşe alanını doldur (kısmi güncelleme) |
| [**apiV1DemandsIdItemsPost()**](DemandsApi.md#apiV1DemandsIdItemsPost) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace) |
| [**apiV1DemandsIdPartiesPartyIdResendPost()**](DemandsApi.md#apiV1DemandsIdPartiesPartyIdResendPost) | **POST** /api/v1/demands/{id}/parties/{partyId}/resend | Tekil tarafa imza davetini tekrar gönder |
| [**apiV1DemandsIdPdfGet()**](DemandsApi.md#apiV1DemandsIdPdfGet) | **GET** /api/v1/demands/{id}/pdf | İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme) |
| [**apiV1DemandsIdTermPatch()**](DemandsApi.md#apiV1DemandsIdTermPatch) | **PATCH** /api/v1/demands/{id}/term | Sözleşme süre/yenileme takibini güncelle |
| [**apiV1DemandsIdTimelineGet()**](DemandsApi.md#apiV1DemandsIdTimelineGet) | **GET** /api/v1/demands/{id}/timeline | İmza denetim izi (maskeli) |
| [**apiV1DemandsIdUnarchivePost()**](DemandsApi.md#apiV1DemandsIdUnarchivePost) | **POST** /api/v1/demands/{id}/unarchive | Sözleşmeyi arşivden çıkar |
| [**apiV1DemandsPost()**](DemandsApi.md#apiV1DemandsPost) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan) |
| [**apiV1DemandsUploadPost()**](DemandsApi.md#apiV1DemandsUploadPost) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz) |
| [**apiV1FieldTemplatesIdPreviewLayoutPost()**](DemandsApi.md#apiV1FieldTemplatesIdPreviewLayoutPost) | **POST** /api/v1/field-templates/{id}/preview-layout | Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener |


## `apiV1DemandsBulkPost()`

```php
apiV1DemandsBulkPost($api_v1_demands_bulk_post_request, $x_workspace_id): \Imzala\Client\Model\ApiV1DemandsBulkPost200Response
```

Toplu sözleşme oluştur (tek şablondan N alıcı)

Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \"bulk send\" modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa her satırda önce yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez.  **Davranış:** - `rows` en fazla 10 (aşarsa 400 `BULK_MAX_10`). Daha büyük listeler   istemci tarafında 10'arlı parçalara bölünür. - Kredi: sabit \"1 satır = 1 kredi\" değildir, her satırın maliyeti   `POST /demands` ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; `eidas_timestamp` seçilirse   satır başına +1 kredi eklenir). Şablon çok-belgeli bir zarf   tanımlıyorsa imzacı başına maliyet o imzacıya ATANMIŞ belge   sayısına göre hesaplanır ve belge sayısına göre azalan birim   fiyatlı bir indirim uygulanır (1 belge %0, 2 belge %30, 3 ve üzeri   %50, imzacı başına ayrı yukarı yuvarlanır; bkz. `POST /demands`   \"Kredi (çok belgeli zarf)\" bölümü). PAdES seviye eki yalnız QES'te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   `ocr_id` ve `liveness` +1 kredi, diğerleri 0'dır. `options`   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır `failed` (`error: \"INSUFFICIENT_CREDITS\"`)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine `failed` döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (`demand_id` response'ta bulunur, davet   gönderilmemiştir); mutabakat kredi-dışı bir hatayla düşerse satır   `failed` (`error: \"RECONCILE_FAILED\"`) döner, sözleşme yine taslağa   düşer. - Atamasız imzacı: çok belgeli şablonda eşlenen bir taraf hiçbir   belgeye atanmamışsa (ve `options.dispatch_notifications` `false`   DEĞİLSE) o satır `failed` (`error: \"PARTY_WITHOUT_DOCUMENTS\"`)   döner; kontrol satır oluşturulmadan ÖNCE (`demand_id` YOK) veya   oluşturulduktan hemen SONRA (sözleşme taslağa düşer, `demand_id` +   `party_ids` döner) çalışabilir. Diğer satırlar etkilenmez. - Belge seçimi satır başınadır: `rows[i].documents` (`POST /demands`   ile aynı şema). `options.documents` gönderilirse 400   `INVALID_DOCUMENT_SELECTION`. Satırın seçim hatası o satırı `failed`   yapar (`error`: `INVALID_DOCUMENT_SELECTION` + `details`, ya da   `PARTY_WITHOUT_DOCUMENTS` + `template_party_ids`); diğer satırlar   etkilenmez. - Belge başına değişkenler (`document_variables`) bu uçta   desteklenmez; yalnız `POST /demands` ile gönderilir. Satırda (kökte   ya da `party_mapping[i]` içinde) gönderilirse o satır `failed`   (`error: \"INVALID_DOCUMENT_VARIABLES\"`, `details.reason:   \"unsupported_endpoint\"`, `details.path`) döner ve oluşturulmaz;   `options.document_variables` gönderilirse 400   `INVALID_DOCUMENT_VARIABLES` döner, hiçbir satır oluşturulmaz. - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace'in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar `failed` olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - `X-Workspace-Id` header'ı ile organizasyon workspace'i seçilebilir.  Rate limit: 5 istek/dakika (API key başına).

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
$api_v1_demands_bulk_post_request = new \Imzala\Client\Model\ApiV1DemandsBulkPostRequest(); // \Imzala\Client\Model\ApiV1DemandsBulkPostRequest
$x_workspace_id = 'x_workspace_id_example'; // string | Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   `WORKSPACE_MISMATCH` döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (`Not a member of this organization`).

try {
    $result = $apiInstance->apiV1DemandsBulkPost($api_v1_demands_bulk_post_request, $x_workspace_id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsBulkPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **api_v1_demands_bulk_post_request** | [**\Imzala\Client\Model\ApiV1DemandsBulkPostRequest**](../Model/ApiV1DemandsBulkPostRequest.md)|  | |
| **x_workspace_id** | **string**| Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;). | [optional] |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsBulkPost200Response**](../Model/ApiV1DemandsBulkPost200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsDemandIdDispatchPost()`

```php
apiV1DemandsDemandIdDispatchPost($demand_id, $api_v1_demands_demand_id_dispatch_post_request): \Imzala\Client\Model\ApiV1DemandsDemandIdDispatchPost200Response
```

Zarfı imzaya gönder (yayınla + davet)

Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, `DRAFT` ise sözleşmeyi `PENDING`'e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  **Onaylayan:** Sözleşmede Onaylayan varsa bu uç önce yalnızca onay bekleyen onaylayanları davet eder; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir. Bir onaylayan reddettiyse bu uç kimseye davet göndermez; onay vermiş onaylayan yeniden davet edilmez.  🔴 **Bu uçta yukarıdaki `/documents*` ailesinin bayrak kapısı (`ENVELOPE_DECISION_ENFORCE`) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — `dispatch_notifications: false` ile oluşturulur, ardından `POST .../documents` / `POST .../documents/upload` ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; `send_invitations` gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir. **Bu uç çağrılana kadar** birden çok belgeli bir zarfın oluşturma yanıtındaki `signing_url`'leri ÇALIŞMAZ (imzacı için 410; `POST .../parties/{partyId}/resend` ve `POST .../reminders` için 409 `ENVELOPE_NOT_DISPATCHED` döner); belge uçları (`/documents*`) ise gönderim beklemeden kullanılabilir.  **Kredi:** tek tahsilat noktası burasıdır (`reconcileDemandSigningCost`). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (`dispatched: false` döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. `v1ResendRateLimiter`, `/parties/{partyId}/resend` ile AYNI dakika-bazlı freni paylaşır).

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
$demand_id = 'demand_id_example'; // string
$api_v1_demands_demand_id_dispatch_post_request = new \Imzala\Client\Model\ApiV1DemandsDemandIdDispatchPostRequest(); // \Imzala\Client\Model\ApiV1DemandsDemandIdDispatchPostRequest

try {
    $result = $apiInstance->apiV1DemandsDemandIdDispatchPost($demand_id, $api_v1_demands_demand_id_dispatch_post_request);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsDemandIdDispatchPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **demand_id** | **string**|  | |
| **api_v1_demands_demand_id_dispatch_post_request** | [**\Imzala\Client\Model\ApiV1DemandsDemandIdDispatchPostRequest**](../Model/ApiV1DemandsDemandIdDispatchPostRequest.md)|  | [optional] |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsDemandIdDispatchPost200Response**](../Model/ApiV1DemandsDemandIdDispatchPost200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut()`

```php
apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut($demand_id, $doc_id, $api_v1_demands_demand_id_documents_doc_id_assignments_put_request): \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPost201Response
```

Belgeye imzacı ata (tam-küme replace)

Belgeye atanmış imzacı kümesini **tam olarak** `party_ids` ile değiştirir (eski atamalar silinir, yenileri yazılır). `party_ids` boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 `INVALID_PARTY_ID` — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 `ASSIGNMENT_HAS_DECISION`).

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
$demand_id = 'demand_id_example'; // string
$doc_id = 'doc_id_example'; // string
$api_v1_demands_demand_id_documents_doc_id_assignments_put_request = new \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest(); // \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest

try {
    $result = $apiInstance->apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut($demand_id, $doc_id, $api_v1_demands_demand_id_documents_doc_id_assignments_put_request);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **demand_id** | **string**|  | |
| **doc_id** | **string**|  | |
| **api_v1_demands_demand_id_documents_doc_id_assignments_put_request** | [**\Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest**](../Model/ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest.md)|  | |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPost201Response**](../Model/ApiV1DemandsDemandIdDocumentsPost201Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsDemandIdDocumentsDocIdDelete()`

```php
apiV1DemandsDemandIdDocumentsDocIdDelete($demand_id, $doc_id): \Imzala\Client\Model\ApiV1TemplatesIdDelete200Response
```

Belgeyi zarftan sil

Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N'e yeniden sıralanır. **Zarftaki son belge silinemez** (400 `CANNOT_DELETE_LAST_DOCUMENT`) — zarf hiç boş kalamaz.

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
$demand_id = 'demand_id_example'; // string
$doc_id = 'doc_id_example'; // string

try {
    $result = $apiInstance->apiV1DemandsDemandIdDocumentsDocIdDelete($demand_id, $doc_id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsDemandIdDocumentsDocIdDelete: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **demand_id** | **string**|  | |
| **doc_id** | **string**|  | |

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

## `apiV1DemandsDemandIdDocumentsDocIdPatch()`

```php
apiV1DemandsDemandIdDocumentsDocIdPatch($demand_id, $doc_id, $api_v1_demands_demand_id_documents_doc_id_patch_request): \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPost201Response
```

Belge metadata güncelle

Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (`PUT .../assignments`).

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
$demand_id = 'demand_id_example'; // string
$doc_id = 'doc_id_example'; // string
$api_v1_demands_demand_id_documents_doc_id_patch_request = new \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsDocIdPatchRequest(); // \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsDocIdPatchRequest

try {
    $result = $apiInstance->apiV1DemandsDemandIdDocumentsDocIdPatch($demand_id, $doc_id, $api_v1_demands_demand_id_documents_doc_id_patch_request);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsDemandIdDocumentsDocIdPatch: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **demand_id** | **string**|  | |
| **doc_id** | **string**|  | |
| **api_v1_demands_demand_id_documents_doc_id_patch_request** | [**\Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsDocIdPatchRequest**](../Model/ApiV1DemandsDemandIdDocumentsDocIdPatchRequest.md)|  | |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPost201Response**](../Model/ApiV1DemandsDemandIdDocumentsPost201Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsDemandIdDocumentsGet()`

```php
apiV1DemandsDemandIdDocumentsGet($demand_id, $view): \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsGet200Response
```

Zarf belge listesi

Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm `/documents*` + `/documents/{docId}*` + `/documents/{docId}/assignments` uçları `ENVELOPE_DECISION_ENFORCE` bayrağı **arkasındadır**; bayrak kapalıyken **409 `ENVELOPE_MULTI_DOC_DISABLED`** döner. Bu, dashboard'un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca `POST /demands/{demandId}/dispatch` çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. `?view=wizard` verilirse tam şekil döner (`EnvelopeDocument` şemasındaki alan notlarına bakın).

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
$demand_id = 'demand_id_example'; // string
$view = 'view_example'; // string | `wizard` → tam DTO (`assigned_party_ids` + `decision_count` dahil).

try {
    $result = $apiInstance->apiV1DemandsDemandIdDocumentsGet($demand_id, $view);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsDemandIdDocumentsGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **demand_id** | **string**|  | |
| **view** | **string**| &#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). | [optional] |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsGet200Response**](../Model/ApiV1DemandsDemandIdDocumentsGet200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsDemandIdDocumentsOrderPut()`

```php
apiV1DemandsDemandIdDocumentsOrderPut($demand_id, $api_v1_demands_demand_id_documents_order_put_request): \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsGet200Response
```

Zarftaki belgelerin sırasını değiştir

Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. `document_ids` zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 `ORDER_SET_MISMATCH`** döner.

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
$demand_id = 'demand_id_example'; // string
$api_v1_demands_demand_id_documents_order_put_request = new \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsOrderPutRequest(); // \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsOrderPutRequest

try {
    $result = $apiInstance->apiV1DemandsDemandIdDocumentsOrderPut($demand_id, $api_v1_demands_demand_id_documents_order_put_request);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsDemandIdDocumentsOrderPut: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **demand_id** | **string**|  | |
| **api_v1_demands_demand_id_documents_order_put_request** | [**\Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsOrderPutRequest**](../Model/ApiV1DemandsDemandIdDocumentsOrderPutRequest.md)|  | |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsGet200Response**](../Model/ApiV1DemandsDemandIdDocumentsGet200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsDemandIdDocumentsPost()`

```php
apiV1DemandsDemandIdDocumentsPost($demand_id, $api_v1_demands_demand_id_documents_post_request): \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPost201Response
```

Zarfa metadata-only belge ekle

Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: `POST /demands/{demandId}/documents/upload`.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`'te yapılır.

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
$demand_id = 'demand_id_example'; // string
$api_v1_demands_demand_id_documents_post_request = new \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPostRequest(); // \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPostRequest

try {
    $result = $apiInstance->apiV1DemandsDemandIdDocumentsPost($demand_id, $api_v1_demands_demand_id_documents_post_request);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsDemandIdDocumentsPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **demand_id** | **string**|  | |
| **api_v1_demands_demand_id_documents_post_request** | [**\Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPostRequest**](../Model/ApiV1DemandsDemandIdDocumentsPostRequest.md)|  | |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPost201Response**](../Model/ApiV1DemandsDemandIdDocumentsPost201Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsDemandIdDocumentsUploadPost()`

```php
apiV1DemandsDemandIdDocumentsUploadPost($demand_id, $file, $idempotency_key, $title, $doc_kind, $is_required): \Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPost201Response
```

Zarfa dosya yükle (belge başına tek dosya)

Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  `idempotency_key` **zorunludur** (gövde alanı; `Idempotency-Key` HEADER'ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı `(demandId, idempotency_key)` çifti ile tekrar çağrı **409 `IDEMPOTENT_REPLAY`** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO'su döner.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`'te yapılır.

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
$demand_id = 'demand_id_example'; // string
$file = '/path/to/file.txt'; // \SplFileObject | PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.
$idempotency_key = 'idempotency_key_example'; // string | Zorunlu tekrar-koruma anahtarı.
$title = 'title_example'; // string
$doc_kind = 'OTHER'; // string
$is_required = 'true'; // string | Multipart alanı — string olarak gönderilir.

try {
    $result = $apiInstance->apiV1DemandsDemandIdDocumentsUploadPost($demand_id, $file, $idempotency_key, $title, $doc_kind, $is_required);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsDemandIdDocumentsUploadPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **demand_id** | **string**|  | |
| **file** | **\SplFileObject****\SplFileObject**| PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya. | |
| **idempotency_key** | **string**| Zorunlu tekrar-koruma anahtarı. | |
| **title** | **string**|  | |
| **doc_kind** | **string**|  | [optional] [default to &#39;OTHER&#39;] |
| **is_required** | **string**| Multipart alanı — string olarak gönderilir. | [optional] [default to &#39;true&#39;] |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPost201Response**](../Model/ApiV1DemandsDemandIdDocumentsPost201Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `multipart/form-data`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsGet()`

```php
apiV1DemandsGet($status, $q, $from, $to, $template_id, $page, $limit, $sort, $archived): \Imzala\Client\Model\ApiV1DemandsGet200Response
```

Sözleşme listesi (counts-only, PII'siz)

Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (`parties_total`, `parties_signed`). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için `GET /demands/{id}`.

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
$status = 'status_example'; // string
$q = 'q_example'; // string | Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez.
$from = new \DateTime('2013-10-20T19:20:30+01:00'); // \DateTime
$to = new \DateTime('2013-10-20T19:20:30+01:00'); // \DateTime
$template_id = 'template_id_example'; // string
$page = 1; // int
$limit = 20; // int | Sayfa boyutu (page_size ile aynı)
$sort = 'sort_example'; // string | alan:yön (ör. createdAt:desc)
$archived = 'archived_example'; // string | Arşiv durumu filtresi. `exclude`: yalnız arşivsiz sözleşmeler. `only`: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan `include`: tüm sözleşmeler (arşivli ve arşivsiz).

try {
    $result = $apiInstance->apiV1DemandsGet($status, $q, $from, $to, $template_id, $page, $limit, $sort, $archived);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **status** | **string**|  | [optional] |
| **q** | **string**| Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez. | [optional] |
| **from** | **\DateTime**|  | [optional] |
| **to** | **\DateTime**|  | [optional] |
| **template_id** | **string**|  | [optional] |
| **page** | **int**|  | [optional] [default to 1] |
| **limit** | **int**| Sayfa boyutu (page_size ile aynı) | [optional] [default to 20] |
| **sort** | **string**| alan:yön (ör. createdAt:desc) | [optional] |
| **archived** | **string**| Arşiv durumu filtresi. &#x60;exclude&#x60;: yalnız arşivsiz sözleşmeler. &#x60;only&#x60;: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan &#x60;include&#x60;: tüm sözleşmeler (arşivli ve arşivsiz). | [optional] |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsGet200Response**](../Model/ApiV1DemandsGet200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsIdArchivePost()`

```php
apiV1DemandsIdArchivePost($id): \Imzala\Client\Model\ApiV1DemandsIdArchivePost200Response
```

Sözleşmeyi arşivle

Arşiv sözleşmenin `status`'unu DEĞİŞTİRMEZ; yalnız `archived_at` damgası ekler ve arşivlenen sözleşmeyi salt-okunur yapar (`PATCH .../term` gibi mutasyonlar 409 `DEMAND_ARCHIVED` ile reddedilir; `GET` uçları etkilenmez). Yalnız tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeler arşivlenebilir. Zaten arşivliyse idempotent (200, mevcut `archived_at` ile).

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
    $result = $apiInstance->apiV1DemandsIdArchivePost($id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdArchivePost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsIdArchivePost200Response**](../Model/ApiV1DemandsIdArchivePost200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsIdBelgeDocumentIdPdfGet()`

```php
apiV1DemandsIdBelgeDocumentIdPdfGet($id, $document_id): \SplFileObject
```

Belge-özgü imzalı PDF (çok-belgeli zarf)

Çok-belgeli zarfta TEK bir belgenin imzalı PDF'ini indirir. Zarf-geneli `/demands/{id}/pdf` ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND'lenir (başka zarfın belgesi istenirse 404).

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
$document_id = 'document_id_example'; // string | Zarftaki belgenin kimliği.

try {
    $result = $apiInstance->apiV1DemandsIdBelgeDocumentIdPdfGet($id, $document_id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdBelgeDocumentIdPdfGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |
| **document_id** | **string**| Zarftaki belgenin kimliği. | |

### Return type

**\SplFileObject**

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/pdf`, `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsIdCancelPost()`

```php
apiV1DemandsIdCancelPost($id, $api_v1_demands_id_cancel_post_request): \Imzala\Client\Model\ApiV1DemandsIdCancelPost200Response
```

Sözleşme iptal (void)

Bekleyen bir sözleşmeyi iptal eder (status=CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir.

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
$api_v1_demands_id_cancel_post_request = new \Imzala\Client\Model\ApiV1DemandsIdCancelPostRequest(); // \Imzala\Client\Model\ApiV1DemandsIdCancelPostRequest

try {
    $result = $apiInstance->apiV1DemandsIdCancelPost($id, $api_v1_demands_id_cancel_post_request);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdCancelPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |
| **api_v1_demands_id_cancel_post_request** | [**\Imzala\Client\Model\ApiV1DemandsIdCancelPostRequest**](../Model/ApiV1DemandsIdCancelPostRequest.md)|  | [optional] |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsIdCancelPost200Response**](../Model/ApiV1DemandsIdCancelPost200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsIdCertificateGet()`

```php
apiV1DemandsIdCertificateGet($id, $lang): \SplFileObject
```

Tamamlanma sertifikası (PAdES B-T)

Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409).

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
$lang = 'lang_example'; // string | tr | en

try {
    $result = $apiInstance->apiV1DemandsIdCertificateGet($id, $lang);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdCertificateGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |
| **lang** | **string**| tr | en | [optional] |

### Return type

**\SplFileObject**

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/pdf`, `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsIdDelete()`

```php
apiV1DemandsIdDelete($id): \Imzala\Client\Model\ApiV1TemplatesIdDelete200Response
```

Sözleşme sil (yalnızca tamamlanmamış)

Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API'den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 `DEMAND_COMPLETED`. Arşivlenmiş sözleşme de silinemez → 409 `DEMAND_ARCHIVED`; önce `POST /api/v1/demands/{id}/unarchive` ile arşivden çıkarın.

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
    $result = $apiInstance->apiV1DemandsIdDelete($id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdDelete: ', $e->getMessage(), PHP_EOL;
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

## `apiV1DemandsIdEmbedSessionPost()`

```php
apiV1DemandsIdEmbedSessionPost($id, $api_v1_demands_id_embed_session_post_request): \Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPost200Response
```

Gömülü imza oturumu başlat (embed token mint)

Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token'ı üretir. Dönen `embed_url` bir `<iframe>` içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla atılan dijital imza, 5070 sayılı Elektronik İmza Kanunu m.3 anlamında elektronik imzadır; güvenli elektronik imza değildir ve AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Telefon doğrulaması, T.C. kimlik numarası kontrolü veya biyometrik adımlar gibi ek doğrulamalar imzanın delil değerini güçlendirir; imzayı gelişmiş ya da güvenli elektronik imzaya dönüştürmez. Güvenli elektronik imza gerekiyorsa bu akış yerine nitelikli elektronik sertifika ile imzalama akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur. - `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış   izin verilen origin'ler dışından `<iframe>` açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** `X-Workspace-Id` header'ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace'in sözleşmesi için 404 döner (IDOR koruması). Kurum çalışma alanında sözleşme düzenleme ile aynı rol kuralı geçerlidir: OWNER ve ADMIN kurumun tüm sözleşmeleri için, MEMBER yalnız kendi oluşturduğu sözleşmeler için oturum alır. Erişim yoksa 404 döner.

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

Kurum çalışma alanında erişim, sözleşme listesi ile aynı rol kuralını izler: OWNER ve ADMIN kurumun tüm sözleşmelerini, MEMBER yalnız kendi oluşturduğu sözleşmeleri görür. Erişim yoksa 404 döner.

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

## `apiV1DemandsIdItemsItemIdStampPatch()`

```php
apiV1DemandsIdItemsItemIdStampPatch($id, $item_id, $patch_stamp_item_request): \Imzala\Client\Model\PatchStampItemResponse
```

Kaşe alanını doldur (kısmi güncelleme)

Sözleşmedeki var olan TEK bir kaşe alanını yapılandırılmış veriyle doldurur. `POST /api/v1/demands/{id}/items` tüm alanları yeniden yazar; bu uç ise yalnız belirtilen kaşeye dokunur, diğer alanlar değişmez.  Kaşe alanının kimliği (`itemId`) `GET /api/v1/demands/{id}` yanıtındaki `stamp_items[].item_id` alanından alınır.  ### Kısmi güncelleme kuralları  - `stamp_data` içinde gönderilen alan yazılır (baştaki/sondaki boşluk kırpılır). - `null` veya boş string gönderilen alan kaldırılır. - Gönderilmeyen alan olduğu gibi korunur. - Bilinmeyen alan veya string olmayan değer `400 INVALID_STAMP_DATA` döner.  ### Kaşe kaynağına göre davranış  - `FILLER_PROVIDES`: veri imzalayanın kaşesine önceden doldurulur;   zorunlu kaşe bu veriyle karşılanmış sayılır. İmzalayan imza   sayfasında bu değeri görür ve imzalamadan önce düzenleyebilir;   düzenlerse onun verisi kaydedilir. Kayıtta verinin gönderen   tarafından mı imzalayan tarafından mı girildiği ayrıca tutulur. - `INLINE`: kaşe verisi güncellenir. - `FROM_SAVED`: veri artık kayıtlı kaşeyle aynı olmadığı için kaşe   `INLINE` olur. Görsel kaşe (logo) varsa korunur ve belgede logo gösterilir.  ### Durum kontrolü  Yalnız `DRAFT` ve `PENDING` sözleşmeler güncellenebilir. Taraflardan biri imzaladıysa veya bir belge için karar verdiyse kaşe değiştirilemez.  ### Erişim  Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerinde, MEMBER yalnız kendi oluşturduğu sözleşmelerde kaşe doldurabilir. Erişim yoksa `404 DEMAND_NOT_FOUND` döner.  ### Örnek  ```bash curl -X PATCH https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items/$STAMP_ITEM_ID/stamp \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{     \"stamp_data\": {       \"companyName\": \"Örnek Ltd.\",       \"taxNumber\": \"1234567890\",       \"taxOffice\": \"Kadıköy\",       \"companyPhone\": \"+905551112233\"     }   }' ```

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
$item_id = 56; // int | Kaşe alanının kimliği (`stamp_items[].item_id`)
$patch_stamp_item_request = new \Imzala\Client\Model\PatchStampItemRequest(); // \Imzala\Client\Model\PatchStampItemRequest

try {
    $result = $apiInstance->apiV1DemandsIdItemsItemIdStampPatch($id, $item_id, $patch_stamp_item_request);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdItemsItemIdStampPatch: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |
| **item_id** | **int**| Kaşe alanının kimliği (&#x60;stamp_items[].item_id&#x60;) | |
| **patch_stamp_item_request** | [**\Imzala\Client\Model\PatchStampItemRequest**](../Model/PatchStampItemRequest.md)|  | |

### Return type

[**\Imzala\Client\Model\PatchStampItemResponse**](../Model/PatchStampItemResponse.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsIdItemsPost()`

```php
apiV1DemandsIdItemsPost($id, $upsert_items_request): \Imzala\Client\Model\UpsertItemsResponse
```

Sözleşmeye alan yerleştir (replace)

Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile demand yarat (`dispatch_notifications=false` ile auto-dispatch'i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - `page_ids` **omitted** → demand'in TÜM mevcut item'ları silinir,   body'dekiler yaratılır (full replace). - `page_ids: [N, M, ...]` → sadece bu sayfaların item'ları silinir,   diğer sayfalardaki item'lar korunur. Body'deki `items[].page_id`   değerleri `page_ids` listesinde olmalıdır.  ### Item type'ları  | `item_type` | `party_id` zorunlu? | `config` örneği | |-------------|---------------------|-----------------| | `signature` | ✅ | (yok) veya `{ requireFirstSignatureOtp: true }` | | `text` | ❌ | `{ default_content }` | | `dynamic_text` | ✅ | `{ defaultSource: \"{{signer.full_name}}\" }` | | `cells` | ✅ | `{ cellCount: 11, defaultSource: \"{{signer.government_id}}\" }` | | `date` | ✅ | `{ defaultSource, defaultValue }` | | `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` | | `checkbox` | ✅ | `{ checkedByDefault: false }` | | `radio` | ✅ | `{ options: [{label,value}], defaultValue }` | | `stamp` | Yalnız `source: FILLER_PROVIDES` ise (zorunlu kaşede şart) | `{ source: \"INLINE\", stampData: { companyName: \"Örnek Ltd.\", taxNumber: \"1234567890\" } }` |  ### Kaşe (`stamp`) alanı  - `config.source`: `INLINE` (varsayılan, kaşe verisi gönderenden gelir),   `FROM_SAVED` (kayıtlı kaşeden kopya) veya `FILLER_PROVIDES` (kaşeyi   `party_id` ile belirtilen imzalayan doldurur). - `config.stampData`: yapılandırılmış kaşe verisi. İzinli alanlar:   `companyName`, `personalName`, `address`, `companyAddress`,   `personalAddress`, `taxNumber`, `taxOffice`, `idNumber`, `phone`,   `companyPhone`, `personalPhone`, `email` (hepsi string). Bilinmeyen alan   veya string olmayan değer `400 INVALID_STAMP_DATA` döner. Eski   dokümandaki data URL string biçimi geriye dönük uyum için hâlâ kabul   edilir, ancak belgede kaşe olarak çizilmez; yapılandırılmış nesneyi kullanın. - `config.isRequired: true` yalnız `FILLER_PROVIDES` kaşede anlamlıdır:   imzalayan kaşeyi doldurmadan imzalayamaz. Zorunlu kaşenin `party_id`'si   olmak zorundadır; çok belgeli zarfta taraf, kaşenin bulunduğu belgeye   atanmış olmalıdır. - Var olan tek bir kaşeyi diğer alanlara dokunmadan doldurmak için bu   uç yerine `PATCH /api/v1/demands/{id}/items/{itemId}/stamp` kullanın.  ### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)  `{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`, `{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`, `{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`, `{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.  ### Workspace izolasyonu  X-API-Key middleware demand'i workspace'e göre filtreler; başka workspace'in demand'ine item ekleyemezsiniz (404 döner). Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerine, MEMBER yalnız kendi oluşturduğu sözleşmelere alan yerleştirebilir. Erişim yoksa 404 döner.  ### Status kontrolü  Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`, `REJECTED` için 403.  ### Örnek  ```bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{     \"items\": [       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"signature\",         \"position_x\": 0.5, \"position_y\": 0.85,         \"width\": 0.2, \"height\": 0.05,         \"is_required\": true       },       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"cells\",         \"position_x\": 0.1, \"position_y\": 0.5,         \"width\": 0.4, \"height\": 0.04,         \"slug\": \"tc\",         \"config\": { \"cellCount\": 11, \"defaultSource\": \"{{signer.government_id}}\" }       }     ]   }' ```

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

## `apiV1DemandsIdPartiesPartyIdResendPost()`

```php
apiV1DemandsIdPartiesPartyIdResendPost($id, $party_id): \Imzala\Client\Model\ApiV1DemandsIdPartiesPartyIdResendPost200Response
```

Tekil tarafa imza davetini tekrar gönder

Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa, onayını vermiş onaylayana, açık düzeltme talebi olan tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). Sıralı imzada onaylayanın onayı o adımı tamamlar; sıra bir sonraki tarafa geçer.

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
$party_id = 'party_id_example'; // string

try {
    $result = $apiInstance->apiV1DemandsIdPartiesPartyIdResendPost($id, $party_id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdPartiesPartyIdResendPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |
| **party_id** | **string**|  | |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsIdPartiesPartyIdResendPost200Response**](../Model/ApiV1DemandsIdPartiesPartyIdResendPost200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsIdPdfGet()`

```php
apiV1DemandsIdPdfGet($id): \SplFileObject
```

İmzalı sözleşme PDF'i (auth'lu indirme)

Tamamlanmış sözleşmenin imzalı PDF'ini indirir. Public `/sonuc/{id}/pdf`'in aksine API key ownership'i zorunludur.

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
    $result = $apiInstance->apiV1DemandsIdPdfGet($id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdPdfGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |

### Return type

**\SplFileObject**

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/pdf`, `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsIdTermPatch()`

```php
apiV1DemandsIdTermPatch($id, $contract_term_input): \Imzala\Client\Model\ApiV1DemandsIdTermPatch200Response
```

Sözleşme süre/yenileme takibini güncelle

Kısmi güncelleme: yalnız gövdede gönderilen `ContractTermInput` anahtarları değiştirilir; bir anahtarı `null` göndermek o alanı temizler. Dashboard'daki sözleşme detay sayfasıyla AYNI çekirdek kuralı uygular.  Bitişi etkileyen bir alan (`term_start_mode`, `term_start_date`, `term_duration_months`, `term_fixed_end_date`) kayıttakinden farklı bir DEĞERLE gönderilirse `term_end_date` bilinen başlangıçtan yeniden hesaplanır; aynı değerle gelen alan no-op'tur (otomatik olarak ileri alınmış bitiş korunur).

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
$contract_term_input = {"term_start_mode":"ON_COMPLETION","term_duration_months":12,"renewal_type":"AUTO_RENEW","notice_days":30}; // \Imzala\Client\Model\ContractTermInput

try {
    $result = $apiInstance->apiV1DemandsIdTermPatch($id, $contract_term_input);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdTermPatch: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |
| **contract_term_input** | [**\Imzala\Client\Model\ContractTermInput**](../Model/ContractTermInput.md)|  | |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsIdTermPatch200Response**](../Model/ApiV1DemandsIdTermPatch200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsIdTimelineGet()`

```php
apiV1DemandsIdTimelineGet($id): \Imzala\Client\Model\ApiV1DemandsIdTimelineGet200Response
```

İmza denetim izi (maskeli)

Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP `ip_masked` (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez.  `event_type` değerleri: `CREATED`, `SENT`, `VIEWED`, `FIELDS_FILLED`, `COMMENT_ADDED`, `SIGNED`, `APPROVED`, `REJECTED`, `TIMESTAMPED`, `COMPLETED`, `OTP_SENT` (SMS doğrulama kodu gönderildi), `OTP_VERIFIED` (SMS doğrulama kodu doğrulandı), `OTP_LOCKED` (deneme sınırı doldu), `MOBILE_SIGNATURE_CAPTURED` (imza QR kod ile telefonda çizildi; bu olayın `ip_masked` ve `device_label` alanları telefona aittir), `REAPPROVAL_REQUIRED` (gönderen açık bir değişiklik talebi sırasında sözleşme içeriğini güncelledi, önceki onaylar sıfırlandı ve onaylayanların yeniden onay vermesi gerekiyor). Liste ileride genişleyebilir; tanımadığınız değeri yok saymanız önerilir.

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
    $result = $apiInstance->apiV1DemandsIdTimelineGet($id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdTimelineGet: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsIdTimelineGet200Response**](../Model/ApiV1DemandsIdTimelineGet200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsIdUnarchivePost()`

```php
apiV1DemandsIdUnarchivePost($id): \Imzala\Client\Model\ApiV1DemandsIdUnarchivePost200Response
```

Sözleşmeyi arşivden çıkar

`archived_at`'i temizler; sözleşme yeniden mutasyona açılır. Zaten arşivsizse idempotent (200, `archived_at: null`).

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
    $result = $apiInstance->apiV1DemandsIdUnarchivePost($id);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsIdUnarchivePost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**|  | |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsIdUnarchivePost200Response**](../Model/ApiV1DemandsIdUnarchivePost200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)

## `apiV1DemandsPost()`

```php
apiV1DemandsPost($create_demand_request, $idempotency_key): \Imzala\Client\Model\ApiV1DemandsPost201Response
```

Sözleşme oluştur (şablondan)

Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field'ları `variables` payload'undan doldurur ve imzalama URL'lerini döner.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa oluşturma anında yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez. Ayrıntı: açıklamadaki \"Onaylayan (onay adımı)\" bölümü.  **Variable resolution:** - Item'ın `template_party_id` non-null → `party_mapping[i].variables`'ta   o slug var ise oradan uygulanır - Yoksa root `variables`'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı  **Kredi (çok belgeli zarf):** Şablon çok-belgeli bir zarf tanımlıyorsa (her belge farklı imzacı(lar)a atanabilir), imzacı başına kredi o imzacıya ATANMIŞ belge sayısına göre hesaplanır ve belge sayısına göre azalan birim fiyatlı bir indirim uygulanır: 1 belge %0, 2 belge %30, 3 ve üzeri %50 (indirim imzacı başına ayrı hesaplanır ve yukarı yuvarlanır). AB nitelikli zaman damgası (`eidas_timestamp`) seçiliyse aynı indirim zarftaki farklı belge sayısına göre toplam üzerinden uygulanır. Tek belgeli şablonlarda tutar eski formülle bayt-aynıdır.  **Atamasız imzacı:** `dispatch_notifications` `false` gönderilmediği sürece, eşlenen bir taraf şablonun hiçbir belgesine atanmamışsa sözleşme HİÇ oluşturulmaz (409 `PARTY_WITHOUT_DOCUMENTS`). Bu kontrolü atlamak için `dispatch_notifications: false` gönderip belge atamalarını `PUT .../documents/{docId}/assignments` ile düzelttikten sonra `POST .../dispatch` ile gönderin (o uç aynı kapıyı yeniden uygular).

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
$create_demand_request = {"template_id":"7ec4b653-e84a-47f1-9e0b-7671e1aae2a1","title":"Vize Danışmanlığı - Ayşe Yılmaz","party_mapping":[{"template_party_id":"e5b4e0cb-c2d5-473f-9f62-44d51c76f56e","first_name":"Ayşe","last_name":"Yılmaz","email":"ayse@example.com","phone":"+905551112233","government_id":"36747474747","variables":{"adres":"Atatürk Cad. No: 12, Çankaya/Ankara","danismanlik_ucreti":"5.000 TL","danismanlik_notlar":"Schengen vize başvuru danışmanlığı","randevu_takibi_ucreti":"1.500 TL","randevu_takibi_notlar":"Konsolosluk randevu takibi 60 gün","genel_toplam":"6.500 TL"}}]}; // \Imzala\Client\Model\CreateDemandRequest
$idempotency_key = 'idempotency_key_example'; // string | Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.

try {
    $result = $apiInstance->apiV1DemandsPost($create_demand_request, $idempotency_key);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **create_demand_request** | [**\Imzala\Client\Model\CreateDemandRequest**](../Model/CreateDemandRequest.md)|  | |
| **idempotency_key** | **string**| Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. | [optional] |

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
apiV1DemandsUploadPost($files, $parties, $idempotency_key, $order, $title, $description, $field_template_id, $force, $send_invitations, $on_anchor_miss): \Imzala\Client\Model\ApiV1DemandsUploadPost201Response
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
$parties = 'parties_example'; // string | JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  `field_template_id` gönderildiğinde her party AYRICA `template_party_id` (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel `locale` (`tr`, `en` veya `null`): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. `PartyMappingInput.locale`). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer `400 INVALID_LOCALE` döner, sözleşme oluşturulmaz.
$idempotency_key = 'idempotency_key_example'; // string | Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.
$order = 'order_example'; // string | Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\")
$title = 'title_example'; // string | Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS'lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın.
$description = 'description_example'; // string
$field_template_id = 'field_template_id_example'; // string | Alan Şablonu (`kind: FIELD_LAYOUT`) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - `template_id` ile **birlikte gönderilemez**   (400 `FIELD_TEMPLATE_CONFLICT`). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için `POST /api/v1/field-templates/{id}/preview-layout`.
$force = 'force_example'; // string | Kopya kapısını bilerek geç. Yalnız `Idempotency-Key` GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.
$send_invitations = 'send_invitations_example'; // string | `\\\"true\\\"` (veya `\\\"1\\\"`) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca `POST /api/v1/demands/{id}/parties/{partyId}/resend` çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 `INVALID_SEND_INVITATIONS`   döner (fail-closed). - **Açık değerler:** `\\\"true\\\"`, `\\\"1\\\"`, `\\\"all\\\"`, `\\\"email\\\"`, `\\\"sms\\\"`. - **Kapalı değerler:** `\\\"false\\\"`, `\\\"0\\\"`, `\\\"off\\\"`, `\\\"no\\\"`,   `\\\"hayir\\\"`, `\\\"hayır\\\"`. - Ad bilerek `dispatch_notifications` **değildir**: o   parametre `POST /api/v1/demands` ve `/demands/bulk`   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: `\\\"true\\\"`, `\\\"1\\\"` ve `\\\"all\\\"` tüm kanalları kullanır;   `\\\"email\\\"` yalnız e-posta, `\\\"sms\\\"` telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   `send_sms`/`send_email` bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (`dispatch.error = DISPATCH_TOO_MANY`), tarafları   `resend` ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği `Idempotency-Key`   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı `DUPLICATE_SUSPECTED` yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte `Idempotency-Key` gönderin;   anahtar yoksa yeniden göndermek yerine   `GET /api/v1/demands?...` ile sonucu doğrulayın.
$on_anchor_miss = 'on_anchor_miss_example'; // string | Çapa bulunamadığında ne yapılacağı. Yalnız `field_template_id` ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, `block` uygulanır. - `drop`, yalnız şablonun İLGİLİ TÜM alanları zaten `DROP`   ise uygulanır; aksi halde yok sayılır, `block` uygulanır   ve yanıtta `ON_ANCHOR_MISS_NOT_RELAXED` uyarısı döner. - İmza alanlarında `drop` hiçbir koşulda uygulanmaz. - `fallback` bu API'de **yoktur** (400 `INVALID_ON_ANCHOR_MISS`).  Yanıttaki `data.field_layout.on_anchor_miss` istenen değil, **uygulanan** değeri taşır.

try {
    $result = $apiInstance->apiV1DemandsUploadPost($files, $parties, $idempotency_key, $order, $title, $description, $field_template_id, $force, $send_invitations, $on_anchor_miss);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1DemandsUploadPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **files** | **\SplFileObject[]**| 1 belge VEYA 1-20 görsel | |
| **parties** | **string**| JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel &#x60;locale&#x60; (&#x60;tr&#x60;, &#x60;en&#x60; veya &#x60;null&#x60;): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. &#x60;PartyMappingInput.locale&#x60;). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer &#x60;400 INVALID_LOCALE&#x60; döner, sözleşme oluşturulmaz. | |
| **idempotency_key** | **string**| Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. | [optional] |
| **order** | **string**| Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) | [optional] |
| **title** | **string**| Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS&#39;lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın. | [optional] |
| **description** | **string**|  | [optional] |
| **field_template_id** | **string**| Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;. | [optional] |
| **force** | **string**| Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için. | [optional] |
| **send_invitations** | **string**| &#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60; ve &#x60;\\\&quot;all\\\&quot;&#x60; tüm kanalları kullanır;   &#x60;\\\&quot;email\\\&quot;&#x60; yalnız e-posta, &#x60;\\\&quot;sms\\\&quot;&#x60; telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği &#x60;Idempotency-Key&#x60;   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı &#x60;DUPLICATE_SUSPECTED&#x60; yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte &#x60;Idempotency-Key&#x60; gönderin;   anahtar yoksa yeniden göndermek yerine   &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın. | [optional] |
| **on_anchor_miss** | **string**| Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır. | [optional] |

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

## `apiV1FieldTemplatesIdPreviewLayoutPost()`

```php
apiV1FieldTemplatesIdPreviewLayoutPost($id, $files, $on_anchor_miss): \Imzala\Client\Model\ApiV1FieldTemplatesIdPreviewLayoutPost200Response
```

Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener

Bir Alan Şablonunun (`kind: FIELD_LAYOUT`) yüklediğiniz PDF'e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  `POST /api/v1/demands/upload` + `field_template_id` çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API'de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (`data.resolvable: false`) — kuru koşumun cevabı \"uygulanamaz\"dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 `RATE_LIMITED`).

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
$id = 'id_example'; // string | Alan Şablonu (FIELD_LAYOUT) kimliği
$files = array('/path/to/file.txt'); // \SplFileObject[] | Tek PDF belge
$on_anchor_miss = 'on_anchor_miss_example'; // string | `POST /api/v1/demands/upload` ile aynı semantik (yalnız sıkılaştırır, gönderilmezse `block`).

try {
    $result = $apiInstance->apiV1FieldTemplatesIdPreviewLayoutPost($id, $files, $on_anchor_miss);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling DemandsApi->apiV1FieldTemplatesIdPreviewLayoutPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**| Alan Şablonu (FIELD_LAYOUT) kimliği | |
| **files** | **\SplFileObject[]**| Tek PDF belge | |
| **on_anchor_miss** | **string**| &#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;). | [optional] |

### Return type

[**\Imzala\Client\Model\ApiV1FieldTemplatesIdPreviewLayoutPost200Response**](../Model/ApiV1FieldTemplatesIdPreviewLayoutPost200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `multipart/form-data`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)
