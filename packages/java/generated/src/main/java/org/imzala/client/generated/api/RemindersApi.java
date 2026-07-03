/*
 * imzala External API
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.6.0 · **Son güncelleme:** 2026-06-30  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API key kullanıyorsanız `X-Workspace-Id` header'ı göndermeniz gerekir (organizasyon UUID'si). Kişisel anahtarlar için bu header gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - 60 istek/dakika per API key - Aşılırsa 429 döner  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (6) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 6 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.7.0
 * Contact: destek@imzala.org
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package org.imzala.client.generated.api;

import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.Pair;

import org.imzala.client.generated.model.ApiV1DemandsIdRemindersPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdRemindersPost429Response;
import org.imzala.client.generated.model.ApiV1TemplatesGet401Response;
import org.imzala.client.generated.model.StandardError;
import org.imzala.client.generated.model.TriggerReminderRequest;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-07-03T05:18:13.896742+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class RemindersApi {
  /**
   * Utility class for extending HttpRequest.Builder functionality.
   */
  private static class HttpRequestBuilderExtensions {
    /**
     * Adds additional headers to the provided HttpRequest.Builder. Useful for adding method/endpoint specific headers.
     *
     * @param builder the HttpRequest.Builder to which headers will be added
     * @param headers a map of header names and values to add; may be null
     * @return the same HttpRequest.Builder instance with the additional headers set
     */
    static HttpRequest.Builder withAdditionalHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }
        return builder;
    }
  }
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<InputStream>> memberVarAsyncResponseInterceptor;

  public RemindersApi() {
    this(Configuration.getDefaultApiClient());
  }

  public RemindersApi(ApiClient apiClient) {
    memberVarHttpClient = apiClient.getHttpClient();
    memberVarObjectMapper = apiClient.getObjectMapper();
    memberVarBaseUri = apiClient.getBaseUri();
    memberVarInterceptor = apiClient.getRequestInterceptor();
    memberVarReadTimeout = apiClient.getReadTimeout();
    memberVarResponseInterceptor = apiClient.getResponseInterceptor();
    memberVarAsyncResponseInterceptor = apiClient.getAsyncResponseInterceptor();
  }


  protected ApiException getApiException(String operationId, HttpResponse<InputStream> response) throws IOException {
    InputStream responseBody = ApiClient.getResponseBody(response);
    String body = null;
    try {
      body = responseBody == null ? null : new String(responseBody.readAllBytes());
    } finally {
      if (responseBody != null) {
        responseBody.close();
      }
    }
    String message = formatExceptionMessage(operationId, response.statusCode(), body);
    return new ApiException(response.statusCode(), message, response.headers(), body);
  }

  private String formatExceptionMessage(String operationId, int statusCode, String body) {
    if (body == null || body.isEmpty()) {
      body = "[no body]";
    }
    return operationId + " call failed with: " + statusCode + " - " + body;
  }

  /**
   * Download file from the given response.
   *
   * @param response Response
   * @return File
   * @throws ApiException If fail to read file content from response and write to disk
   */
  public File downloadFileFromResponse(HttpResponse<InputStream> response, InputStream responseBody) throws ApiException {
    if (responseBody == null) {
      throw new ApiException(new IOException("Response body is empty"));
    }
    try {
      File file = prepareDownloadFile(response);
      java.nio.file.Files.copy(responseBody, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      return file;
    } catch (IOException e) {
      throw new ApiException(e);
    }
  }

  /**
   * <p>Prepare the file for download from the response.</p>
   *
   * @param response a {@link java.net.http.HttpResponse} object.
   * @return a {@link java.io.File} object.
   * @throws java.io.IOException if any.
   */
  private File prepareDownloadFile(HttpResponse<InputStream> response) throws IOException {
    String filename = null;
    java.util.Optional<String> contentDisposition = response.headers().firstValue("Content-Disposition");
    if (contentDisposition.isPresent() && !"".equals(contentDisposition.get())) {
      // Get filename from the Content-Disposition header.
      java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("filename=['\"]?([^'\"\\s]+)['\"]?");
      java.util.regex.Matcher matcher = pattern.matcher(contentDisposition.get());
      if (matcher.find())
        filename = matcher.group(1);
    }
    File file = null;
    if (filename != null) {
      java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("swagger-gen-native");
      java.nio.file.Path filePath = java.nio.file.Files.createFile(tempDir.resolve(filename));
      file = filePath.toFile();
      tempDir.toFile().deleteOnExit();   // best effort cleanup
      file.deleteOnExit(); // best effort cleanup
    } else {
      file = java.nio.file.Files.createTempFile("download-", "").toFile();
      file.deleteOnExit(); // best effort cleanup
    }
    return file;
  }

  /**
   * Anlık hatırlatma tetikle (imzalanmamış taraflara)
   * Sözleşmenin **henüz imzalamamış** taraflarına SMS ve/veya e-posta hatırlatması anlık olarak gönderir. Şablon-/sözleşme-seviyesi &#x60;reminder_settings&#x60; ayarından **bağımsız** olarak çalışır — istediğiniz zaman elle tetiklenebilir.  **Anti-spam koruması:** Aynı sözleşme için son hatırlatmanın üzerinden 5 dakika geçmemişse 429 &#x60;RATE_LIMITED&#x60; döner ve &#x60;Retry-After&#x60; header&#39;ı ile &#x60;retry_after_seconds&#x60; alanı bilgilendirir. Override için body&#39;de &#x60;force: true&#x60; yollayın (yanlışlıkla spam atmamak için kasıtlı kullanın).  **Kişi başına sert sınırlar (override edilemez):** Spam ve gönderici reputasyon koruması için kişi-başına reminder sayısı sınırlıdır:  - **Bir kişiye en fazla 3 SMS reminder** gönderilebilir (otomatik   scheduled + manuel trigger toplam). Sayaç dolu kişi için SMS skip   edilir, &#x60;details[]&#x60; içinde &#x60;reason: \&quot;party_sms_cap_reached (3)\&quot;&#x60; görünür. - **Bir kişiye en fazla 3 e-posta reminder** gönderilebilir. Sayaç   dolu kişi için email skip edilir, &#x60;reason: \&quot;party_email_cap_reached (3)\&quot;&#x60;. - Diğer kişilere gönderim normal şekilde devam eder; tek bir kişinin   sayacı dolu diye tüm çağrı reddedilmez. - &#x60;force: true&#x60; 5dk anti-spam pencereyi override eder ama kişi-başı   cap&#39;i ASLA — sert kural. - Sözleşme başına global cap pratikte yok (&#x60;999&#x60; safety net) — kural   kişi başınadır.  Sayım kaynağı: &#x60;ReminderLog&#x60; tablosu (channel + party_id, &#x60;status&#x3D;&#39;SENT&#39;&#x60;). Hem otomatik scheduled (ReminderWorker) hem manuel trigger reminders tek toplamda sayılır.  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla erişilen organizasyonun sözleşmesi olmalıdır; aksi halde 404 &#x60;DEMAND_NOT_FOUND&#x60; (IDOR shield).  **Kanal eligibility:** Bir parti için - &#x60;email&#x60; kanalı: &#x60;party.email&#x60; dolu **ve** &#x60;party.send_email&#x3D;true&#x60; **ve**   &#x60;demand.send_email_notifications&#x3D;true&#x60; ise gönderilir - &#x60;sms&#x60; kanalı: &#x60;party.phone&#x60; dolu **ve** &#x60;party.send_sms&#x3D;true&#x60; **ve**   &#x60;demand.send_sms_notifications&#x3D;true&#x60; ise gönderilir  **Mesaj içeriği:** Şablonun &#x60;sms_reminder_message&#x60; alanı (varsa) + &#x60;signer.first_name&#x60; / &#x60;{{name}}&#x60; / &#x60;{{link}}&#x60; gibi sistem değişkenleri substitute edilir. Şablonda yoksa sistem default reminder mesajı kullanılır. 
   * @param id Sözleşme (demand) ID (required)
   * @param triggerReminderRequest  (optional)
   * @return ApiV1DemandsIdRemindersPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdRemindersPost200Response apiV1DemandsIdRemindersPost(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable TriggerReminderRequest triggerReminderRequest) throws ApiException {
    return apiV1DemandsIdRemindersPost(id, triggerReminderRequest, null);
  }

  /**
   * Anlık hatırlatma tetikle (imzalanmamış taraflara)
   * Sözleşmenin **henüz imzalamamış** taraflarına SMS ve/veya e-posta hatırlatması anlık olarak gönderir. Şablon-/sözleşme-seviyesi &#x60;reminder_settings&#x60; ayarından **bağımsız** olarak çalışır — istediğiniz zaman elle tetiklenebilir.  **Anti-spam koruması:** Aynı sözleşme için son hatırlatmanın üzerinden 5 dakika geçmemişse 429 &#x60;RATE_LIMITED&#x60; döner ve &#x60;Retry-After&#x60; header&#39;ı ile &#x60;retry_after_seconds&#x60; alanı bilgilendirir. Override için body&#39;de &#x60;force: true&#x60; yollayın (yanlışlıkla spam atmamak için kasıtlı kullanın).  **Kişi başına sert sınırlar (override edilemez):** Spam ve gönderici reputasyon koruması için kişi-başına reminder sayısı sınırlıdır:  - **Bir kişiye en fazla 3 SMS reminder** gönderilebilir (otomatik   scheduled + manuel trigger toplam). Sayaç dolu kişi için SMS skip   edilir, &#x60;details[]&#x60; içinde &#x60;reason: \&quot;party_sms_cap_reached (3)\&quot;&#x60; görünür. - **Bir kişiye en fazla 3 e-posta reminder** gönderilebilir. Sayaç   dolu kişi için email skip edilir, &#x60;reason: \&quot;party_email_cap_reached (3)\&quot;&#x60;. - Diğer kişilere gönderim normal şekilde devam eder; tek bir kişinin   sayacı dolu diye tüm çağrı reddedilmez. - &#x60;force: true&#x60; 5dk anti-spam pencereyi override eder ama kişi-başı   cap&#39;i ASLA — sert kural. - Sözleşme başına global cap pratikte yok (&#x60;999&#x60; safety net) — kural   kişi başınadır.  Sayım kaynağı: &#x60;ReminderLog&#x60; tablosu (channel + party_id, &#x60;status&#x3D;&#39;SENT&#39;&#x60;). Hem otomatik scheduled (ReminderWorker) hem manuel trigger reminders tek toplamda sayılır.  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla erişilen organizasyonun sözleşmesi olmalıdır; aksi halde 404 &#x60;DEMAND_NOT_FOUND&#x60; (IDOR shield).  **Kanal eligibility:** Bir parti için - &#x60;email&#x60; kanalı: &#x60;party.email&#x60; dolu **ve** &#x60;party.send_email&#x3D;true&#x60; **ve**   &#x60;demand.send_email_notifications&#x3D;true&#x60; ise gönderilir - &#x60;sms&#x60; kanalı: &#x60;party.phone&#x60; dolu **ve** &#x60;party.send_sms&#x3D;true&#x60; **ve**   &#x60;demand.send_sms_notifications&#x3D;true&#x60; ise gönderilir  **Mesaj içeriği:** Şablonun &#x60;sms_reminder_message&#x60; alanı (varsa) + &#x60;signer.first_name&#x60; / &#x60;{{name}}&#x60; / &#x60;{{link}}&#x60; gibi sistem değişkenleri substitute edilir. Şablonda yoksa sistem default reminder mesajı kullanılır. 
   * @param id Sözleşme (demand) ID (required)
   * @param triggerReminderRequest  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsIdRemindersPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdRemindersPost200Response apiV1DemandsIdRemindersPost(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable TriggerReminderRequest triggerReminderRequest, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsIdRemindersPost200Response> localVarResponse = apiV1DemandsIdRemindersPostWithHttpInfo(id, triggerReminderRequest, headers);
    return localVarResponse.getData();
  }

  /**
   * Anlık hatırlatma tetikle (imzalanmamış taraflara)
   * Sözleşmenin **henüz imzalamamış** taraflarına SMS ve/veya e-posta hatırlatması anlık olarak gönderir. Şablon-/sözleşme-seviyesi &#x60;reminder_settings&#x60; ayarından **bağımsız** olarak çalışır — istediğiniz zaman elle tetiklenebilir.  **Anti-spam koruması:** Aynı sözleşme için son hatırlatmanın üzerinden 5 dakika geçmemişse 429 &#x60;RATE_LIMITED&#x60; döner ve &#x60;Retry-After&#x60; header&#39;ı ile &#x60;retry_after_seconds&#x60; alanı bilgilendirir. Override için body&#39;de &#x60;force: true&#x60; yollayın (yanlışlıkla spam atmamak için kasıtlı kullanın).  **Kişi başına sert sınırlar (override edilemez):** Spam ve gönderici reputasyon koruması için kişi-başına reminder sayısı sınırlıdır:  - **Bir kişiye en fazla 3 SMS reminder** gönderilebilir (otomatik   scheduled + manuel trigger toplam). Sayaç dolu kişi için SMS skip   edilir, &#x60;details[]&#x60; içinde &#x60;reason: \&quot;party_sms_cap_reached (3)\&quot;&#x60; görünür. - **Bir kişiye en fazla 3 e-posta reminder** gönderilebilir. Sayaç   dolu kişi için email skip edilir, &#x60;reason: \&quot;party_email_cap_reached (3)\&quot;&#x60;. - Diğer kişilere gönderim normal şekilde devam eder; tek bir kişinin   sayacı dolu diye tüm çağrı reddedilmez. - &#x60;force: true&#x60; 5dk anti-spam pencereyi override eder ama kişi-başı   cap&#39;i ASLA — sert kural. - Sözleşme başına global cap pratikte yok (&#x60;999&#x60; safety net) — kural   kişi başınadır.  Sayım kaynağı: &#x60;ReminderLog&#x60; tablosu (channel + party_id, &#x60;status&#x3D;&#39;SENT&#39;&#x60;). Hem otomatik scheduled (ReminderWorker) hem manuel trigger reminders tek toplamda sayılır.  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla erişilen organizasyonun sözleşmesi olmalıdır; aksi halde 404 &#x60;DEMAND_NOT_FOUND&#x60; (IDOR shield).  **Kanal eligibility:** Bir parti için - &#x60;email&#x60; kanalı: &#x60;party.email&#x60; dolu **ve** &#x60;party.send_email&#x3D;true&#x60; **ve**   &#x60;demand.send_email_notifications&#x3D;true&#x60; ise gönderilir - &#x60;sms&#x60; kanalı: &#x60;party.phone&#x60; dolu **ve** &#x60;party.send_sms&#x3D;true&#x60; **ve**   &#x60;demand.send_sms_notifications&#x3D;true&#x60; ise gönderilir  **Mesaj içeriği:** Şablonun &#x60;sms_reminder_message&#x60; alanı (varsa) + &#x60;signer.first_name&#x60; / &#x60;{{name}}&#x60; / &#x60;{{link}}&#x60; gibi sistem değişkenleri substitute edilir. Şablonda yoksa sistem default reminder mesajı kullanılır. 
   * @param id Sözleşme (demand) ID (required)
   * @param triggerReminderRequest  (optional)
   * @return ApiResponse&lt;ApiV1DemandsIdRemindersPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdRemindersPost200Response> apiV1DemandsIdRemindersPostWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable TriggerReminderRequest triggerReminderRequest) throws ApiException {
    return apiV1DemandsIdRemindersPostWithHttpInfo(id, triggerReminderRequest, null);
  }

  /**
   * Anlık hatırlatma tetikle (imzalanmamış taraflara)
   * Sözleşmenin **henüz imzalamamış** taraflarına SMS ve/veya e-posta hatırlatması anlık olarak gönderir. Şablon-/sözleşme-seviyesi &#x60;reminder_settings&#x60; ayarından **bağımsız** olarak çalışır — istediğiniz zaman elle tetiklenebilir.  **Anti-spam koruması:** Aynı sözleşme için son hatırlatmanın üzerinden 5 dakika geçmemişse 429 &#x60;RATE_LIMITED&#x60; döner ve &#x60;Retry-After&#x60; header&#39;ı ile &#x60;retry_after_seconds&#x60; alanı bilgilendirir. Override için body&#39;de &#x60;force: true&#x60; yollayın (yanlışlıkla spam atmamak için kasıtlı kullanın).  **Kişi başına sert sınırlar (override edilemez):** Spam ve gönderici reputasyon koruması için kişi-başına reminder sayısı sınırlıdır:  - **Bir kişiye en fazla 3 SMS reminder** gönderilebilir (otomatik   scheduled + manuel trigger toplam). Sayaç dolu kişi için SMS skip   edilir, &#x60;details[]&#x60; içinde &#x60;reason: \&quot;party_sms_cap_reached (3)\&quot;&#x60; görünür. - **Bir kişiye en fazla 3 e-posta reminder** gönderilebilir. Sayaç   dolu kişi için email skip edilir, &#x60;reason: \&quot;party_email_cap_reached (3)\&quot;&#x60;. - Diğer kişilere gönderim normal şekilde devam eder; tek bir kişinin   sayacı dolu diye tüm çağrı reddedilmez. - &#x60;force: true&#x60; 5dk anti-spam pencereyi override eder ama kişi-başı   cap&#39;i ASLA — sert kural. - Sözleşme başına global cap pratikte yok (&#x60;999&#x60; safety net) — kural   kişi başınadır.  Sayım kaynağı: &#x60;ReminderLog&#x60; tablosu (channel + party_id, &#x60;status&#x3D;&#39;SENT&#39;&#x60;). Hem otomatik scheduled (ReminderWorker) hem manuel trigger reminders tek toplamda sayılır.  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla erişilen organizasyonun sözleşmesi olmalıdır; aksi halde 404 &#x60;DEMAND_NOT_FOUND&#x60; (IDOR shield).  **Kanal eligibility:** Bir parti için - &#x60;email&#x60; kanalı: &#x60;party.email&#x60; dolu **ve** &#x60;party.send_email&#x3D;true&#x60; **ve**   &#x60;demand.send_email_notifications&#x3D;true&#x60; ise gönderilir - &#x60;sms&#x60; kanalı: &#x60;party.phone&#x60; dolu **ve** &#x60;party.send_sms&#x3D;true&#x60; **ve**   &#x60;demand.send_sms_notifications&#x3D;true&#x60; ise gönderilir  **Mesaj içeriği:** Şablonun &#x60;sms_reminder_message&#x60; alanı (varsa) + &#x60;signer.first_name&#x60; / &#x60;{{name}}&#x60; / &#x60;{{link}}&#x60; gibi sistem değişkenleri substitute edilir. Şablonda yoksa sistem default reminder mesajı kullanılır. 
   * @param id Sözleşme (demand) ID (required)
   * @param triggerReminderRequest  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsIdRemindersPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdRemindersPost200Response> apiV1DemandsIdRemindersPostWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable TriggerReminderRequest triggerReminderRequest, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdRemindersPostRequestBuilder(id, triggerReminderRequest, headers);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      InputStream localVarResponseBody = null;
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("apiV1DemandsIdRemindersPost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsIdRemindersPost200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsIdRemindersPost200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsIdRemindersPost200Response>() {});
        

        return new ApiResponse<ApiV1DemandsIdRemindersPost200Response>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseValue
        );
      } finally {
        if (localVarResponseBody != null) {
          localVarResponseBody.close();
        }
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder apiV1DemandsIdRemindersPostRequestBuilder(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable TriggerReminderRequest triggerReminderRequest, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdRemindersPost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}/reminders"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(triggerReminderRequest);
      localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    // Add custom headers if provided
    localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

}
