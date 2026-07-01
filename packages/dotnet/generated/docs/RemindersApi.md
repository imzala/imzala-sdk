# ImzalaApiClient.Api.RemindersApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|--------|--------------|-------------|
| [**ApiV1DemandsIdRemindersPost**](RemindersApi.md#apiv1demandsidreminderspost) | **POST** /api/v1/demands/{id}/reminders | Anlık hatırlatma tetikle (imzalanmamış taraflara) |

<a id="apiv1demandsidreminderspost"></a>
# **ApiV1DemandsIdRemindersPost**
> ApiV1DemandsIdRemindersPost200Response ApiV1DemandsIdRemindersPost (Guid id, TriggerReminderRequest? triggerReminderRequest = null)

Anlık hatırlatma tetikle (imzalanmamış taraflara)

Sözleşmenin **henüz imzalamamış** taraflarına SMS ve/veya e-posta hatırlatması anlık olarak gönderir. Şablon-/sözleşme-seviyesi `reminder_settings` ayarından **bağımsız** olarak çalışır — istediğiniz zaman elle tetiklenebilir.  **Anti-spam koruması:** Aynı sözleşme için son hatırlatmanın üzerinden 5 dakika geçmemişse 429 `RATE_LIMITED` döner ve `Retry-After` header'ı ile `retry_after_seconds` alanı bilgilendirir. Override için body'de `force: true` yollayın (yanlışlıkla spam atmamak için kasıtlı kullanın).  **Kişi başına sert sınırlar (override edilemez):** Spam ve gönderici reputasyon koruması için kişi-başına reminder sayısı sınırlıdır:  - **Bir kişiye en fazla 3 SMS reminder** gönderilebilir (otomatik   scheduled + manuel trigger toplam). Sayaç dolu kişi için SMS skip   edilir, `details[]` içinde `reason: \"party_sms_cap_reached (3)\"` görünür. - **Bir kişiye en fazla 3 e-posta reminder** gönderilebilir. Sayaç   dolu kişi için email skip edilir, `reason: \"party_email_cap_reached (3)\"`. - Diğer kişilere gönderim normal şekilde devam eder; tek bir kişinin   sayacı dolu diye tüm çağrı reddedilmez. - `force: true` 5dk anti-spam pencereyi override eder ama kişi-başı   cap'i ASLA — sert kural. - Sözleşme başına global cap pratikte yok (`999` safety net) — kural   kişi başınadır.  Sayım kaynağı: `ReminderLog` tablosu (channel + party_id, `status='SENT'`). Hem otomatik scheduled (ReminderWorker) hem manuel trigger reminders tek toplamda sayılır.  **Workspace izolasyonu:** `X-Workspace-Id` header'ıyla erişilen organizasyonun sözleşmesi olmalıdır; aksi halde 404 `DEMAND_NOT_FOUND` (IDOR shield).  **Kanal eligibility:** Bir parti için - `email` kanalı: `party.email` dolu **ve** `party.send_email=true` **ve**   `demand.send_email_notifications=true` ise gönderilir - `sms` kanalı: `party.phone` dolu **ve** `party.send_sms=true` **ve**   `demand.send_sms_notifications=true` ise gönderilir  **Mesaj içeriği:** Şablonun `sms_reminder_message` alanı (varsa) + `signer.first_name` / `{{name}}` / `{{link}}` gibi sistem değişkenleri substitute edilir. Şablonda yoksa sistem default reminder mesajı kullanılır. 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdRemindersPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new RemindersApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | Sözleşme (demand) ID
            var triggerReminderRequest = new TriggerReminderRequest?(); // TriggerReminderRequest? |  (optional) 

            try
            {
                // Anlık hatırlatma tetikle (imzalanmamış taraflara)
                ApiV1DemandsIdRemindersPost200Response result = apiInstance.ApiV1DemandsIdRemindersPost(id, triggerReminderRequest);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling RemindersApi.ApiV1DemandsIdRemindersPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdRemindersPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Anlık hatırlatma tetikle (imzalanmamış taraflara)
    ApiResponse<ApiV1DemandsIdRemindersPost200Response> response = apiInstance.ApiV1DemandsIdRemindersPostWithHttpInfo(id, triggerReminderRequest);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling RemindersApi.ApiV1DemandsIdRemindersPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** | Sözleşme (demand) ID |  |
| **triggerReminderRequest** | [**TriggerReminderRequest?**](TriggerReminderRequest?.md) |  | [optional]  |

### Return type

[**ApiV1DemandsIdRemindersPost200Response**](ApiV1DemandsIdRemindersPost200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Hatırlatma kuyruğa alındı. &#x60;data.dispatched&#x60; her parti için hangi kanallarda gönderildiğini gösterir.  |  -  |
| **400** | **INVALID_CHANNELS** — &#x60;channels&#x60; parametresi yollandı ama boş array veya &#x60;email&#x60;/&#x60;sms&#x60; dışı bir değer içeriyor.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **404** | **DEMAND_NOT_FOUND** — Sözleşme bu workspace&#39;te (ya da kişisel workspace&#39;te) bulunamadı. IDOR koruması: başka org&#39;un sözleşmesi için de bu cevap döner.  |  -  |
| **409** | **ALREADY_COMPLETED** — Tüm taraflar imzalamış. Hatırlatılacak imzalanmamış parti yok.  |  -  |
| **429** | **RATE_LIMITED** — Son hatırlatmadan 5 dk geçmedi. &#x60;Retry-After&#x60; header&#39;ı + &#x60;retry_after_seconds&#x60; alanı bekleme süresini gösterir. Override için &#x60;force: true&#x60; ile tekrar deneyin.  Not: Kişi-başı cap&#39;ler 429 dönmez — o kişiyi &#x60;details[]&#x60; içinde &#x60;skipped&#x60; olarak işaretler ve diğer kişilere gönderime devam eder (200 success). MAX_SMS_REMINDERS_REACHED 429 yalnızca pratikte ulaşılmayan global demand cap (999) güvenlik ağı için tanımlı.  |  * Retry-After - Saniye cinsinden bekleme süresi (sadece RATE_LIMITED) <br>  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

