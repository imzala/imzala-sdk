# Imzala\Client\RemindersApi

Sözleşmedeki imzalanmamış taraflara hatırlatma tetikleme ve hatırlatma ayarlarını yapılandırma. Şablona kaydedilen &#x60;reminder_settings&#x60; yeni oluşturulan sözleşmeye otomatik aktarılır; ayrıca &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile anlık hatırlatma tetiklenebilir.

All URIs are relative to https://api-prd.imzala.org, except if the operation defines another base path.

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**apiV1DemandsIdRemindersPost()**](RemindersApi.md#apiV1DemandsIdRemindersPost) | **POST** /api/v1/demands/{id}/reminders | Anlık hatırlatma tetikle (imzalanmamış taraflara) |


## `apiV1DemandsIdRemindersPost()`

```php
apiV1DemandsIdRemindersPost($id, $trigger_reminder_request): \Imzala\Client\Model\ApiV1DemandsIdRemindersPost200Response
```

Anlık hatırlatma tetikle (imzalanmamış taraflara)

Sözleşmenin **henüz imzalamamış** taraflarına SMS ve/veya e-posta hatırlatması anlık olarak gönderir. Şablon-/sözleşme-seviyesi `reminder_settings` ayarından **bağımsız** olarak çalışır — istediğiniz zaman elle tetiklenebilir.  **Anti-spam koruması:** Aynı sözleşme için son hatırlatmanın üzerinden 5 dakika geçmemişse 429 `RATE_LIMITED` döner ve `Retry-After` header'ı ile `retry_after_seconds` alanı bilgilendirir. Override için body'de `force: true` yollayın (yanlışlıkla spam atmamak için kasıtlı kullanın).  **Kişi başına sert sınırlar (override edilemez):** Spam ve gönderici reputasyon koruması için kişi-başına reminder sayısı sınırlıdır:  - **Bir kişiye en fazla 3 SMS reminder** gönderilebilir (otomatik   scheduled + manuel trigger toplam). Sayaç dolu kişi için SMS skip   edilir, `details[]` içinde `reason: \"party_sms_cap_reached (3)\"` görünür. - **Bir kişiye en fazla 3 e-posta reminder** gönderilebilir. Sayaç   dolu kişi için email skip edilir, `reason: \"party_email_cap_reached (3)\"`. - Diğer kişilere gönderim normal şekilde devam eder; tek bir kişinin   sayacı dolu diye tüm çağrı reddedilmez. - `force: true` 5dk anti-spam pencereyi override eder ama kişi-başı   cap'i ASLA — sert kural. - Sözleşme başına global cap pratikte yok (`999` safety net) — kural   kişi başınadır.  Sayım kaynağı: `ReminderLog` tablosu (channel + party_id, `status='SENT'`). Hem otomatik scheduled (ReminderWorker) hem manuel trigger reminders tek toplamda sayılır.  **Workspace izolasyonu:** `X-Workspace-Id` header'ıyla erişilen organizasyonun sözleşmesi olmalıdır; aksi halde 404 `DEMAND_NOT_FOUND` (IDOR shield).  **Kanal eligibility:** Bir parti için - `email` kanalı: `party.email` dolu **ve** `party.send_email=true` **ve**   `demand.send_email_notifications=true` ise gönderilir - `sms` kanalı: `party.phone` dolu **ve** `party.send_sms=true` **ve**   `demand.send_sms_notifications=true` ise gönderilir  **Mesaj içeriği:** Şablonun `sms_reminder_message` alanı (varsa) + `signer.first_name` / `{{name}}` / `{{link}}` gibi sistem değişkenleri substitute edilir. Şablonda yoksa sistem default reminder mesajı kullanılır.

### Example

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');


// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\RemindersApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);
$id = 'id_example'; // string | Sözleşme (demand) ID
$trigger_reminder_request = {}; // \Imzala\Client\Model\TriggerReminderRequest

try {
    $result = $apiInstance->apiV1DemandsIdRemindersPost($id, $trigger_reminder_request);
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling RemindersApi->apiV1DemandsIdRemindersPost: ', $e->getMessage(), PHP_EOL;
}
```

### Parameters

| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **string**| Sözleşme (demand) ID | |
| **trigger_reminder_request** | [**\Imzala\Client\Model\TriggerReminderRequest**](../Model/TriggerReminderRequest.md)|  | [optional] |

### Return type

[**\Imzala\Client\Model\ApiV1DemandsIdRemindersPost200Response**](../Model/ApiV1DemandsIdRemindersPost200Response.md)

### Authorization

[ApiKeyAuth](../../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`

[[Back to top]](#) [[Back to API list]](../../README.md#endpoints)
[[Back to Model list]](../../README.md#models)
[[Back to README]](../../README.md)
