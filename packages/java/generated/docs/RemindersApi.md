# RemindersApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**apiV1DemandsIdRemindersPost**](RemindersApi.md#apiV1DemandsIdRemindersPost) | **POST** /api/v1/demands/{id}/reminders | Anlık hatırlatma tetikle (imzalanmamış taraflara) |
| [**apiV1DemandsIdRemindersPostWithHttpInfo**](RemindersApi.md#apiV1DemandsIdRemindersPostWithHttpInfo) | **POST** /api/v1/demands/{id}/reminders | Anlık hatırlatma tetikle (imzalanmamış taraflara) |



## apiV1DemandsIdRemindersPost

> ApiV1DemandsIdRemindersPost200Response apiV1DemandsIdRemindersPost(id, triggerReminderRequest)

Anlık hatırlatma tetikle (imzalanmamış taraflara)

Sözleşmenin **henüz imzalamamış** taraflarına SMS ve/veya e-posta hatırlatması anlık olarak gönderir. Şablon-/sözleşme-seviyesi &#x60;reminder_settings&#x60; ayarından **bağımsız** olarak çalışır — istediğiniz zaman elle tetiklenebilir.  **Anti-spam koruması:** Aynı sözleşme için son hatırlatmanın üzerinden 5 dakika geçmemişse 429 &#x60;RATE_LIMITED&#x60; döner ve &#x60;Retry-After&#x60; header&#39;ı ile &#x60;retry_after_seconds&#x60; alanı bilgilendirir. Override için body&#39;de &#x60;force: true&#x60; yollayın (yanlışlıkla spam atmamak için kasıtlı kullanın).  **Kişi başına sert sınırlar (override edilemez):** Spam ve gönderici reputasyon koruması için kişi-başına reminder sayısı sınırlıdır:  - **Bir kişiye en fazla 3 SMS reminder** gönderilebilir (otomatik   scheduled + manuel trigger toplam). Sayaç dolu kişi için SMS skip   edilir, &#x60;details[]&#x60; içinde &#x60;reason: \&quot;party_sms_cap_reached (3)\&quot;&#x60; görünür. - **Bir kişiye en fazla 3 e-posta reminder** gönderilebilir. Sayaç   dolu kişi için email skip edilir, &#x60;reason: \&quot;party_email_cap_reached (3)\&quot;&#x60;. - Diğer kişilere gönderim normal şekilde devam eder; tek bir kişinin   sayacı dolu diye tüm çağrı reddedilmez. - &#x60;force: true&#x60; 5dk anti-spam pencereyi override eder ama kişi-başı   cap&#39;i ASLA — sert kural. - Sözleşme başına global cap pratikte yok (&#x60;999&#x60; safety net) — kural   kişi başınadır.  Sayım kaynağı: &#x60;ReminderLog&#x60; tablosu (channel + party_id, &#x60;status&#x3D;&#39;SENT&#39;&#x60;). Hem otomatik scheduled (ReminderWorker) hem manuel trigger reminders tek toplamda sayılır.  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla erişilen organizasyonun sözleşmesi olmalıdır; aksi halde 404 &#x60;DEMAND_NOT_FOUND&#x60; (IDOR shield).  **Kanal eligibility:** Bir parti için - &#x60;email&#x60; kanalı: &#x60;party.email&#x60; dolu **ve** &#x60;party.send_email&#x3D;true&#x60; **ve**   &#x60;demand.send_email_notifications&#x3D;true&#x60; ise gönderilir - &#x60;sms&#x60; kanalı: &#x60;party.phone&#x60; dolu **ve** &#x60;party.send_sms&#x3D;true&#x60; **ve**   &#x60;demand.send_sms_notifications&#x3D;true&#x60; ise gönderilir  **Mesaj içeriği:** Şablonun &#x60;sms_reminder_message&#x60; alanı (varsa) + &#x60;signer.first_name&#x60; / &#x60;{{name}}&#x60; / &#x60;{{link}}&#x60; gibi sistem değişkenleri substitute edilir. Şablonda yoksa sistem default reminder mesajı kullanılır. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.RemindersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        RemindersApi apiInstance = new RemindersApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | Sözleşme (demand) ID
        TriggerReminderRequest triggerReminderRequest = new TriggerReminderRequest(); // TriggerReminderRequest | 
        try {
            ApiV1DemandsIdRemindersPost200Response result = apiInstance.apiV1DemandsIdRemindersPost(id, triggerReminderRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RemindersApi#apiV1DemandsIdRemindersPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**| Sözleşme (demand) ID | |
| **triggerReminderRequest** | [**TriggerReminderRequest**](TriggerReminderRequest.md)|  | [optional] |

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

## apiV1DemandsIdRemindersPostWithHttpInfo

> ApiResponse<ApiV1DemandsIdRemindersPost200Response> apiV1DemandsIdRemindersPostWithHttpInfo(id, triggerReminderRequest)

Anlık hatırlatma tetikle (imzalanmamış taraflara)

Sözleşmenin **henüz imzalamamış** taraflarına SMS ve/veya e-posta hatırlatması anlık olarak gönderir. Şablon-/sözleşme-seviyesi &#x60;reminder_settings&#x60; ayarından **bağımsız** olarak çalışır — istediğiniz zaman elle tetiklenebilir.  **Anti-spam koruması:** Aynı sözleşme için son hatırlatmanın üzerinden 5 dakika geçmemişse 429 &#x60;RATE_LIMITED&#x60; döner ve &#x60;Retry-After&#x60; header&#39;ı ile &#x60;retry_after_seconds&#x60; alanı bilgilendirir. Override için body&#39;de &#x60;force: true&#x60; yollayın (yanlışlıkla spam atmamak için kasıtlı kullanın).  **Kişi başına sert sınırlar (override edilemez):** Spam ve gönderici reputasyon koruması için kişi-başına reminder sayısı sınırlıdır:  - **Bir kişiye en fazla 3 SMS reminder** gönderilebilir (otomatik   scheduled + manuel trigger toplam). Sayaç dolu kişi için SMS skip   edilir, &#x60;details[]&#x60; içinde &#x60;reason: \&quot;party_sms_cap_reached (3)\&quot;&#x60; görünür. - **Bir kişiye en fazla 3 e-posta reminder** gönderilebilir. Sayaç   dolu kişi için email skip edilir, &#x60;reason: \&quot;party_email_cap_reached (3)\&quot;&#x60;. - Diğer kişilere gönderim normal şekilde devam eder; tek bir kişinin   sayacı dolu diye tüm çağrı reddedilmez. - &#x60;force: true&#x60; 5dk anti-spam pencereyi override eder ama kişi-başı   cap&#39;i ASLA — sert kural. - Sözleşme başına global cap pratikte yok (&#x60;999&#x60; safety net) — kural   kişi başınadır.  Sayım kaynağı: &#x60;ReminderLog&#x60; tablosu (channel + party_id, &#x60;status&#x3D;&#39;SENT&#39;&#x60;). Hem otomatik scheduled (ReminderWorker) hem manuel trigger reminders tek toplamda sayılır.  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla erişilen organizasyonun sözleşmesi olmalıdır; aksi halde 404 &#x60;DEMAND_NOT_FOUND&#x60; (IDOR shield).  **Kanal eligibility:** Bir parti için - &#x60;email&#x60; kanalı: &#x60;party.email&#x60; dolu **ve** &#x60;party.send_email&#x3D;true&#x60; **ve**   &#x60;demand.send_email_notifications&#x3D;true&#x60; ise gönderilir - &#x60;sms&#x60; kanalı: &#x60;party.phone&#x60; dolu **ve** &#x60;party.send_sms&#x3D;true&#x60; **ve**   &#x60;demand.send_sms_notifications&#x3D;true&#x60; ise gönderilir  **Mesaj içeriği:** Şablonun &#x60;sms_reminder_message&#x60; alanı (varsa) + &#x60;signer.first_name&#x60; / &#x60;{{name}}&#x60; / &#x60;{{link}}&#x60; gibi sistem değişkenleri substitute edilir. Şablonda yoksa sistem default reminder mesajı kullanılır. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.RemindersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        RemindersApi apiInstance = new RemindersApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | Sözleşme (demand) ID
        TriggerReminderRequest triggerReminderRequest = new TriggerReminderRequest(); // TriggerReminderRequest | 
        try {
            ApiResponse<ApiV1DemandsIdRemindersPost200Response> response = apiInstance.apiV1DemandsIdRemindersPostWithHttpInfo(id, triggerReminderRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling RemindersApi#apiV1DemandsIdRemindersPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**| Sözleşme (demand) ID | |
| **triggerReminderRequest** | [**TriggerReminderRequest**](TriggerReminderRequest.md)|  | [optional] |

### Return type

ApiResponse<[**ApiV1DemandsIdRemindersPost200Response**](ApiV1DemandsIdRemindersPost200Response.md)>


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

