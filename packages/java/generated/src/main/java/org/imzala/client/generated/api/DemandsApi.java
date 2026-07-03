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

import org.imzala.client.generated.model.ApiError;
import org.imzala.client.generated.model.ApiV1DemandsGet200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdCancelPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdCancelPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsIdDelete409Response;
import org.imzala.client.generated.model.ApiV1DemandsIdEmbedSessionPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdEmbedSessionPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsIdGet200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdPartiesPartyIdResendPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdTimelineGet200Response;
import org.imzala.client.generated.model.ApiV1DemandsPost201Response;
import org.imzala.client.generated.model.ApiV1DemandsUploadPost201Response;
import org.imzala.client.generated.model.ApiV1TemplatesGet401Response;
import org.imzala.client.generated.model.ApiV1TemplatesIdDelete200Response;
import org.imzala.client.generated.model.ApiV1TemplatesIdGet404Response;
import org.imzala.client.generated.model.CreateDemandRequest;
import java.io.File;
import java.time.LocalDate;
import java.util.UUID;
import org.imzala.client.generated.model.UpsertItemsRequest;
import org.imzala.client.generated.model.UpsertItemsResponse;

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
public class DemandsApi {
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

  public DemandsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DemandsApi(ApiClient apiClient) {
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
   * Sözleşme listesi (counts-only, PII&#39;siz)
   * Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
   * @param status  (optional)
   * @param q Başlık araması (optional)
   * @param from  (optional)
   * @param to  (optional)
   * @param templateId  (optional)
   * @param page  (optional, default to 1)
   * @param limit Sayfa boyutu (page_size ile aynı) (optional, default to 20)
   * @param sort alan:yön (ör. createdAt:desc) (optional)
   * @return ApiV1DemandsGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsGet200Response apiV1DemandsGet(@javax.annotation.Nullable String status, @javax.annotation.Nullable String q, @javax.annotation.Nullable LocalDate from, @javax.annotation.Nullable LocalDate to, @javax.annotation.Nullable UUID templateId, @javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit, @javax.annotation.Nullable String sort) throws ApiException {
    return apiV1DemandsGet(status, q, from, to, templateId, page, limit, sort, null);
  }

  /**
   * Sözleşme listesi (counts-only, PII&#39;siz)
   * Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
   * @param status  (optional)
   * @param q Başlık araması (optional)
   * @param from  (optional)
   * @param to  (optional)
   * @param templateId  (optional)
   * @param page  (optional, default to 1)
   * @param limit Sayfa boyutu (page_size ile aynı) (optional, default to 20)
   * @param sort alan:yön (ör. createdAt:desc) (optional)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsGet200Response apiV1DemandsGet(@javax.annotation.Nullable String status, @javax.annotation.Nullable String q, @javax.annotation.Nullable LocalDate from, @javax.annotation.Nullable LocalDate to, @javax.annotation.Nullable UUID templateId, @javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit, @javax.annotation.Nullable String sort, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsGet200Response> localVarResponse = apiV1DemandsGetWithHttpInfo(status, q, from, to, templateId, page, limit, sort, headers);
    return localVarResponse.getData();
  }

  /**
   * Sözleşme listesi (counts-only, PII&#39;siz)
   * Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
   * @param status  (optional)
   * @param q Başlık araması (optional)
   * @param from  (optional)
   * @param to  (optional)
   * @param templateId  (optional)
   * @param page  (optional, default to 1)
   * @param limit Sayfa boyutu (page_size ile aynı) (optional, default to 20)
   * @param sort alan:yön (ör. createdAt:desc) (optional)
   * @return ApiResponse&lt;ApiV1DemandsGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsGet200Response> apiV1DemandsGetWithHttpInfo(@javax.annotation.Nullable String status, @javax.annotation.Nullable String q, @javax.annotation.Nullable LocalDate from, @javax.annotation.Nullable LocalDate to, @javax.annotation.Nullable UUID templateId, @javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit, @javax.annotation.Nullable String sort) throws ApiException {
    return apiV1DemandsGetWithHttpInfo(status, q, from, to, templateId, page, limit, sort, null);
  }

  /**
   * Sözleşme listesi (counts-only, PII&#39;siz)
   * Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
   * @param status  (optional)
   * @param q Başlık araması (optional)
   * @param from  (optional)
   * @param to  (optional)
   * @param templateId  (optional)
   * @param page  (optional, default to 1)
   * @param limit Sayfa boyutu (page_size ile aynı) (optional, default to 20)
   * @param sort alan:yön (ör. createdAt:desc) (optional)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsGet200Response> apiV1DemandsGetWithHttpInfo(@javax.annotation.Nullable String status, @javax.annotation.Nullable String q, @javax.annotation.Nullable LocalDate from, @javax.annotation.Nullable LocalDate to, @javax.annotation.Nullable UUID templateId, @javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit, @javax.annotation.Nullable String sort, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsGetRequestBuilder(status, q, from, to, templateId, page, limit, sort, headers);
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
          throw getApiException("apiV1DemandsGet", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsGet200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsGet200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsGet200Response>() {});
        

        return new ApiResponse<ApiV1DemandsGet200Response>(
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

  private HttpRequest.Builder apiV1DemandsGetRequestBuilder(@javax.annotation.Nullable String status, @javax.annotation.Nullable String q, @javax.annotation.Nullable LocalDate from, @javax.annotation.Nullable LocalDate to, @javax.annotation.Nullable UUID templateId, @javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit, @javax.annotation.Nullable String sort, Map<String, String> headers) throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "status";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("status", status));
    localVarQueryParameterBaseName = "q";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("q", q));
    localVarQueryParameterBaseName = "from";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("from", from));
    localVarQueryParameterBaseName = "to";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("to", to));
    localVarQueryParameterBaseName = "template_id";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("template_id", templateId));
    localVarQueryParameterBaseName = "page";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("page", page));
    localVarQueryParameterBaseName = "limit";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
    localVarQueryParameterBaseName = "sort";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("sort", sort));

    if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
      StringJoiner queryJoiner = new StringJoiner("&");
      localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
      if (localVarQueryStringJoiner.length() != 0) {
        queryJoiner.add(localVarQueryStringJoiner.toString());
      }
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
    } else {
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
    }

    localVarRequestBuilder.header("Accept", "application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
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

  /**
   * Sözleşme iptal (void)
   * Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
   * @param id  (required)
   * @param apiV1DemandsIdCancelPostRequest  (optional)
   * @return ApiV1DemandsIdCancelPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdCancelPost200Response apiV1DemandsIdCancelPost(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable ApiV1DemandsIdCancelPostRequest apiV1DemandsIdCancelPostRequest) throws ApiException {
    return apiV1DemandsIdCancelPost(id, apiV1DemandsIdCancelPostRequest, null);
  }

  /**
   * Sözleşme iptal (void)
   * Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
   * @param id  (required)
   * @param apiV1DemandsIdCancelPostRequest  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsIdCancelPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdCancelPost200Response apiV1DemandsIdCancelPost(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable ApiV1DemandsIdCancelPostRequest apiV1DemandsIdCancelPostRequest, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsIdCancelPost200Response> localVarResponse = apiV1DemandsIdCancelPostWithHttpInfo(id, apiV1DemandsIdCancelPostRequest, headers);
    return localVarResponse.getData();
  }

  /**
   * Sözleşme iptal (void)
   * Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
   * @param id  (required)
   * @param apiV1DemandsIdCancelPostRequest  (optional)
   * @return ApiResponse&lt;ApiV1DemandsIdCancelPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdCancelPost200Response> apiV1DemandsIdCancelPostWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable ApiV1DemandsIdCancelPostRequest apiV1DemandsIdCancelPostRequest) throws ApiException {
    return apiV1DemandsIdCancelPostWithHttpInfo(id, apiV1DemandsIdCancelPostRequest, null);
  }

  /**
   * Sözleşme iptal (void)
   * Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
   * @param id  (required)
   * @param apiV1DemandsIdCancelPostRequest  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsIdCancelPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdCancelPost200Response> apiV1DemandsIdCancelPostWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable ApiV1DemandsIdCancelPostRequest apiV1DemandsIdCancelPostRequest, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdCancelPostRequestBuilder(id, apiV1DemandsIdCancelPostRequest, headers);
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
          throw getApiException("apiV1DemandsIdCancelPost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsIdCancelPost200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsIdCancelPost200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsIdCancelPost200Response>() {});
        

        return new ApiResponse<ApiV1DemandsIdCancelPost200Response>(
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

  private HttpRequest.Builder apiV1DemandsIdCancelPostRequestBuilder(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable ApiV1DemandsIdCancelPostRequest apiV1DemandsIdCancelPostRequest, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdCancelPost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}/cancel"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(apiV1DemandsIdCancelPostRequest);
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

  /**
   * Tamamlanma sertifikası (PAdES B-T)
   * Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
   * @param id  (required)
   * @param lang tr | en (optional)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File apiV1DemandsIdCertificateGet(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable String lang) throws ApiException {
    return apiV1DemandsIdCertificateGet(id, lang, null);
  }

  /**
   * Tamamlanma sertifikası (PAdES B-T)
   * Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
   * @param id  (required)
   * @param lang tr | en (optional)
   * @param headers Optional headers to include in the request
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File apiV1DemandsIdCertificateGet(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable String lang, Map<String, String> headers) throws ApiException {
    ApiResponse<File> localVarResponse = apiV1DemandsIdCertificateGetWithHttpInfo(id, lang, headers);
    return localVarResponse.getData();
  }

  /**
   * Tamamlanma sertifikası (PAdES B-T)
   * Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
   * @param id  (required)
   * @param lang tr | en (optional)
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> apiV1DemandsIdCertificateGetWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable String lang) throws ApiException {
    return apiV1DemandsIdCertificateGetWithHttpInfo(id, lang, null);
  }

  /**
   * Tamamlanma sertifikası (PAdES B-T)
   * Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
   * @param id  (required)
   * @param lang tr | en (optional)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> apiV1DemandsIdCertificateGetWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable String lang, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdCertificateGetRequestBuilder(id, lang, headers);
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
          throw getApiException("apiV1DemandsIdCertificateGet", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<File>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        // Handle file downloading.
        File responseValue = downloadFileFromResponse(localVarResponse, localVarResponseBody);
        

        return new ApiResponse<File>(
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

  private HttpRequest.Builder apiV1DemandsIdCertificateGetRequestBuilder(@javax.annotation.Nonnull UUID id, @javax.annotation.Nullable String lang, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdCertificateGet");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}/certificate"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "lang";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("lang", lang));

    if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
      StringJoiner queryJoiner = new StringJoiner("&");
      localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
      if (localVarQueryStringJoiner.length() != 0) {
        queryJoiner.add(localVarQueryStringJoiner.toString());
      }
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
    } else {
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
    }

    localVarRequestBuilder.header("Accept", "application/pdf, application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
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

  /**
   * Sözleşme sil (yalnızca tamamlanmamış)
   * Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. 
   * @param id  (required)
   * @return ApiV1TemplatesIdDelete200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesIdDelete200Response apiV1DemandsIdDelete(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdDelete(id, null);
  }

  /**
   * Sözleşme sil (yalnızca tamamlanmamış)
   * Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1TemplatesIdDelete200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesIdDelete200Response apiV1DemandsIdDelete(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1TemplatesIdDelete200Response> localVarResponse = apiV1DemandsIdDeleteWithHttpInfo(id, headers);
    return localVarResponse.getData();
  }

  /**
   * Sözleşme sil (yalnızca tamamlanmamış)
   * Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. 
   * @param id  (required)
   * @return ApiResponse&lt;ApiV1TemplatesIdDelete200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesIdDelete200Response> apiV1DemandsIdDeleteWithHttpInfo(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdDeleteWithHttpInfo(id, null);
  }

  /**
   * Sözleşme sil (yalnızca tamamlanmamış)
   * Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1TemplatesIdDelete200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesIdDelete200Response> apiV1DemandsIdDeleteWithHttpInfo(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdDeleteRequestBuilder(id, headers);
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
          throw getApiException("apiV1DemandsIdDelete", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1TemplatesIdDelete200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1TemplatesIdDelete200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1TemplatesIdDelete200Response>() {});
        

        return new ApiResponse<ApiV1TemplatesIdDelete200Response>(
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

  private HttpRequest.Builder apiV1DemandsIdDeleteRequestBuilder(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdDelete");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/json");

    localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
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

  /**
   * Gömülü imza oturumu başlat (embed token mint)
   * Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 
   * @param id Sözleşme (demand) ID (required)
   * @param apiV1DemandsIdEmbedSessionPostRequest  (required)
   * @return ApiV1DemandsIdEmbedSessionPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdEmbedSessionPost200Response apiV1DemandsIdEmbedSessionPost(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest) throws ApiException {
    return apiV1DemandsIdEmbedSessionPost(id, apiV1DemandsIdEmbedSessionPostRequest, null);
  }

  /**
   * Gömülü imza oturumu başlat (embed token mint)
   * Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 
   * @param id Sözleşme (demand) ID (required)
   * @param apiV1DemandsIdEmbedSessionPostRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsIdEmbedSessionPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdEmbedSessionPost200Response apiV1DemandsIdEmbedSessionPost(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response> localVarResponse = apiV1DemandsIdEmbedSessionPostWithHttpInfo(id, apiV1DemandsIdEmbedSessionPostRequest, headers);
    return localVarResponse.getData();
  }

  /**
   * Gömülü imza oturumu başlat (embed token mint)
   * Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 
   * @param id Sözleşme (demand) ID (required)
   * @param apiV1DemandsIdEmbedSessionPostRequest  (required)
   * @return ApiResponse&lt;ApiV1DemandsIdEmbedSessionPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response> apiV1DemandsIdEmbedSessionPostWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest) throws ApiException {
    return apiV1DemandsIdEmbedSessionPostWithHttpInfo(id, apiV1DemandsIdEmbedSessionPostRequest, null);
  }

  /**
   * Gömülü imza oturumu başlat (embed token mint)
   * Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 
   * @param id Sözleşme (demand) ID (required)
   * @param apiV1DemandsIdEmbedSessionPostRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsIdEmbedSessionPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response> apiV1DemandsIdEmbedSessionPostWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdEmbedSessionPostRequestBuilder(id, apiV1DemandsIdEmbedSessionPostRequest, headers);
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
          throw getApiException("apiV1DemandsIdEmbedSessionPost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsIdEmbedSessionPost200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsIdEmbedSessionPost200Response>() {});
        

        return new ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response>(
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

  private HttpRequest.Builder apiV1DemandsIdEmbedSessionPostRequestBuilder(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdEmbedSessionPost");
    }
    // verify the required parameter 'apiV1DemandsIdEmbedSessionPostRequest' is set
    if (apiV1DemandsIdEmbedSessionPostRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'apiV1DemandsIdEmbedSessionPostRequest' when calling apiV1DemandsIdEmbedSessionPost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}/embed-session"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(apiV1DemandsIdEmbedSessionPostRequest);
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

  /**
   * Sözleşme durumu + imza ilerlemesi
   * 
   * @param id  (required)
   * @return ApiV1DemandsIdGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdGet200Response apiV1DemandsIdGet(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdGet(id, null);
  }

  /**
   * Sözleşme durumu + imza ilerlemesi
   * 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsIdGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdGet200Response apiV1DemandsIdGet(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsIdGet200Response> localVarResponse = apiV1DemandsIdGetWithHttpInfo(id, headers);
    return localVarResponse.getData();
  }

  /**
   * Sözleşme durumu + imza ilerlemesi
   * 
   * @param id  (required)
   * @return ApiResponse&lt;ApiV1DemandsIdGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdGet200Response> apiV1DemandsIdGetWithHttpInfo(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdGetWithHttpInfo(id, null);
  }

  /**
   * Sözleşme durumu + imza ilerlemesi
   * 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsIdGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdGet200Response> apiV1DemandsIdGetWithHttpInfo(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdGetRequestBuilder(id, headers);
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
          throw getApiException("apiV1DemandsIdGet", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsIdGet200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsIdGet200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsIdGet200Response>() {});
        

        return new ApiResponse<ApiV1DemandsIdGet200Response>(
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

  private HttpRequest.Builder apiV1DemandsIdGetRequestBuilder(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdGet");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
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

  /**
   * Sözleşmeye alan yerleştir (replace)
   * Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |-------------|---------------------|-----------------| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
   * @param id  (required)
   * @param upsertItemsRequest  (required)
   * @return UpsertItemsResponse
   * @throws ApiException if fails to make API call
   */
  public UpsertItemsResponse apiV1DemandsIdItemsPost(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UpsertItemsRequest upsertItemsRequest) throws ApiException {
    return apiV1DemandsIdItemsPost(id, upsertItemsRequest, null);
  }

  /**
   * Sözleşmeye alan yerleştir (replace)
   * Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |-------------|---------------------|-----------------| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
   * @param id  (required)
   * @param upsertItemsRequest  (required)
   * @param headers Optional headers to include in the request
   * @return UpsertItemsResponse
   * @throws ApiException if fails to make API call
   */
  public UpsertItemsResponse apiV1DemandsIdItemsPost(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UpsertItemsRequest upsertItemsRequest, Map<String, String> headers) throws ApiException {
    ApiResponse<UpsertItemsResponse> localVarResponse = apiV1DemandsIdItemsPostWithHttpInfo(id, upsertItemsRequest, headers);
    return localVarResponse.getData();
  }

  /**
   * Sözleşmeye alan yerleştir (replace)
   * Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |-------------|---------------------|-----------------| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
   * @param id  (required)
   * @param upsertItemsRequest  (required)
   * @return ApiResponse&lt;UpsertItemsResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UpsertItemsResponse> apiV1DemandsIdItemsPostWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UpsertItemsRequest upsertItemsRequest) throws ApiException {
    return apiV1DemandsIdItemsPostWithHttpInfo(id, upsertItemsRequest, null);
  }

  /**
   * Sözleşmeye alan yerleştir (replace)
   * Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |-------------|---------------------|-----------------| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
   * @param id  (required)
   * @param upsertItemsRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;UpsertItemsResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UpsertItemsResponse> apiV1DemandsIdItemsPostWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UpsertItemsRequest upsertItemsRequest, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdItemsPostRequestBuilder(id, upsertItemsRequest, headers);
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
          throw getApiException("apiV1DemandsIdItemsPost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<UpsertItemsResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        UpsertItemsResponse responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<UpsertItemsResponse>() {});
        

        return new ApiResponse<UpsertItemsResponse>(
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

  private HttpRequest.Builder apiV1DemandsIdItemsPostRequestBuilder(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UpsertItemsRequest upsertItemsRequest, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdItemsPost");
    }
    // verify the required parameter 'upsertItemsRequest' is set
    if (upsertItemsRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'upsertItemsRequest' when calling apiV1DemandsIdItemsPost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}/items"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(upsertItemsRequest);
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

  /**
   * Tekil tarafa imza davetini tekrar gönder
   * Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
   * @param id  (required)
   * @param partyId  (required)
   * @return ApiV1DemandsIdPartiesPartyIdResendPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdPartiesPartyIdResendPost200Response apiV1DemandsIdPartiesPartyIdResendPost(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UUID partyId) throws ApiException {
    return apiV1DemandsIdPartiesPartyIdResendPost(id, partyId, null);
  }

  /**
   * Tekil tarafa imza davetini tekrar gönder
   * Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
   * @param id  (required)
   * @param partyId  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsIdPartiesPartyIdResendPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdPartiesPartyIdResendPost200Response apiV1DemandsIdPartiesPartyIdResendPost(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UUID partyId, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response> localVarResponse = apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo(id, partyId, headers);
    return localVarResponse.getData();
  }

  /**
   * Tekil tarafa imza davetini tekrar gönder
   * Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
   * @param id  (required)
   * @param partyId  (required)
   * @return ApiResponse&lt;ApiV1DemandsIdPartiesPartyIdResendPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response> apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UUID partyId) throws ApiException {
    return apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo(id, partyId, null);
  }

  /**
   * Tekil tarafa imza davetini tekrar gönder
   * Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
   * @param id  (required)
   * @param partyId  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsIdPartiesPartyIdResendPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response> apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UUID partyId, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdPartiesPartyIdResendPostRequestBuilder(id, partyId, headers);
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
          throw getApiException("apiV1DemandsIdPartiesPartyIdResendPost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsIdPartiesPartyIdResendPost200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsIdPartiesPartyIdResendPost200Response>() {});
        

        return new ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response>(
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

  private HttpRequest.Builder apiV1DemandsIdPartiesPartyIdResendPostRequestBuilder(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UUID partyId, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdPartiesPartyIdResendPost");
    }
    // verify the required parameter 'partyId' is set
    if (partyId == null) {
      throw new ApiException(400, "Missing the required parameter 'partyId' when calling apiV1DemandsIdPartiesPartyIdResendPost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}/parties/{partyId}/resend"
        .replace("{id}", ApiClient.urlEncode(id.toString()))
        .replace("{partyId}", ApiClient.urlEncode(partyId.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/json");

    localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
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

  /**
   * İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
   * Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 
   * @param id  (required)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File apiV1DemandsIdPdfGet(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdPdfGet(id, null);
  }

  /**
   * İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
   * Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File apiV1DemandsIdPdfGet(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    ApiResponse<File> localVarResponse = apiV1DemandsIdPdfGetWithHttpInfo(id, headers);
    return localVarResponse.getData();
  }

  /**
   * İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
   * Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 
   * @param id  (required)
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> apiV1DemandsIdPdfGetWithHttpInfo(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdPdfGetWithHttpInfo(id, null);
  }

  /**
   * İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
   * Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> apiV1DemandsIdPdfGetWithHttpInfo(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdPdfGetRequestBuilder(id, headers);
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
          throw getApiException("apiV1DemandsIdPdfGet", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<File>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        // Handle file downloading.
        File responseValue = downloadFileFromResponse(localVarResponse, localVarResponseBody);
        

        return new ApiResponse<File>(
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

  private HttpRequest.Builder apiV1DemandsIdPdfGetRequestBuilder(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdPdfGet");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}/pdf"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/pdf, application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
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

  /**
   * İmza denetim izi (maskeli)
   * Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
   * @param id  (required)
   * @return ApiV1DemandsIdTimelineGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdTimelineGet200Response apiV1DemandsIdTimelineGet(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdTimelineGet(id, null);
  }

  /**
   * İmza denetim izi (maskeli)
   * Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsIdTimelineGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdTimelineGet200Response apiV1DemandsIdTimelineGet(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsIdTimelineGet200Response> localVarResponse = apiV1DemandsIdTimelineGetWithHttpInfo(id, headers);
    return localVarResponse.getData();
  }

  /**
   * İmza denetim izi (maskeli)
   * Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
   * @param id  (required)
   * @return ApiResponse&lt;ApiV1DemandsIdTimelineGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdTimelineGet200Response> apiV1DemandsIdTimelineGetWithHttpInfo(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdTimelineGetWithHttpInfo(id, null);
  }

  /**
   * İmza denetim izi (maskeli)
   * Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsIdTimelineGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdTimelineGet200Response> apiV1DemandsIdTimelineGetWithHttpInfo(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdTimelineGetRequestBuilder(id, headers);
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
          throw getApiException("apiV1DemandsIdTimelineGet", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsIdTimelineGet200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsIdTimelineGet200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsIdTimelineGet200Response>() {});
        

        return new ApiResponse<ApiV1DemandsIdTimelineGet200Response>(
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

  private HttpRequest.Builder apiV1DemandsIdTimelineGetRequestBuilder(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdTimelineGet");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}/timeline"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
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

  /**
   * Sözleşme oluştur (şablondan)
   * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 
   * @param createDemandRequest  (required)
   * @return ApiV1DemandsPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsPost201Response apiV1DemandsPost(@javax.annotation.Nonnull CreateDemandRequest createDemandRequest) throws ApiException {
    return apiV1DemandsPost(createDemandRequest, null);
  }

  /**
   * Sözleşme oluştur (şablondan)
   * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 
   * @param createDemandRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsPost201Response apiV1DemandsPost(@javax.annotation.Nonnull CreateDemandRequest createDemandRequest, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsPost201Response> localVarResponse = apiV1DemandsPostWithHttpInfo(createDemandRequest, headers);
    return localVarResponse.getData();
  }

  /**
   * Sözleşme oluştur (şablondan)
   * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 
   * @param createDemandRequest  (required)
   * @return ApiResponse&lt;ApiV1DemandsPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsPost201Response> apiV1DemandsPostWithHttpInfo(@javax.annotation.Nonnull CreateDemandRequest createDemandRequest) throws ApiException {
    return apiV1DemandsPostWithHttpInfo(createDemandRequest, null);
  }

  /**
   * Sözleşme oluştur (şablondan)
   * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 
   * @param createDemandRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsPost201Response> apiV1DemandsPostWithHttpInfo(@javax.annotation.Nonnull CreateDemandRequest createDemandRequest, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsPostRequestBuilder(createDemandRequest, headers);
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
          throw getApiException("apiV1DemandsPost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsPost201Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsPost201Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsPost201Response>() {});
        

        return new ApiResponse<ApiV1DemandsPost201Response>(
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

  private HttpRequest.Builder apiV1DemandsPostRequestBuilder(@javax.annotation.Nonnull CreateDemandRequest createDemandRequest, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'createDemandRequest' is set
    if (createDemandRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createDemandRequest' when calling apiV1DemandsPost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(createDemandRequest);
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

  /**
   * Dosya upload ile sözleşme oluştur (şablonsuz)
   * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
   * @param files 1 belge VEYA 1-20 görsel (required)
   * @param parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  (required)
   * @param order Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)
   * @param title  (optional)
   * @param description  (optional)
   * @return ApiV1DemandsUploadPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsUploadPost201Response apiV1DemandsUploadPost(@javax.annotation.Nonnull List<File> files, @javax.annotation.Nonnull String parties, @javax.annotation.Nullable String order, @javax.annotation.Nullable String title, @javax.annotation.Nullable String description) throws ApiException {
    return apiV1DemandsUploadPost(files, parties, order, title, description, null);
  }

  /**
   * Dosya upload ile sözleşme oluştur (şablonsuz)
   * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
   * @param files 1 belge VEYA 1-20 görsel (required)
   * @param parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  (required)
   * @param order Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)
   * @param title  (optional)
   * @param description  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsUploadPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsUploadPost201Response apiV1DemandsUploadPost(@javax.annotation.Nonnull List<File> files, @javax.annotation.Nonnull String parties, @javax.annotation.Nullable String order, @javax.annotation.Nullable String title, @javax.annotation.Nullable String description, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsUploadPost201Response> localVarResponse = apiV1DemandsUploadPostWithHttpInfo(files, parties, order, title, description, headers);
    return localVarResponse.getData();
  }

  /**
   * Dosya upload ile sözleşme oluştur (şablonsuz)
   * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
   * @param files 1 belge VEYA 1-20 görsel (required)
   * @param parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  (required)
   * @param order Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)
   * @param title  (optional)
   * @param description  (optional)
   * @return ApiResponse&lt;ApiV1DemandsUploadPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsUploadPost201Response> apiV1DemandsUploadPostWithHttpInfo(@javax.annotation.Nonnull List<File> files, @javax.annotation.Nonnull String parties, @javax.annotation.Nullable String order, @javax.annotation.Nullable String title, @javax.annotation.Nullable String description) throws ApiException {
    return apiV1DemandsUploadPostWithHttpInfo(files, parties, order, title, description, null);
  }

  /**
   * Dosya upload ile sözleşme oluştur (şablonsuz)
   * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
   * @param files 1 belge VEYA 1-20 görsel (required)
   * @param parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  (required)
   * @param order Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)
   * @param title  (optional)
   * @param description  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsUploadPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsUploadPost201Response> apiV1DemandsUploadPostWithHttpInfo(@javax.annotation.Nonnull List<File> files, @javax.annotation.Nonnull String parties, @javax.annotation.Nullable String order, @javax.annotation.Nullable String title, @javax.annotation.Nullable String description, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsUploadPostRequestBuilder(files, parties, order, title, description, headers);
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
          throw getApiException("apiV1DemandsUploadPost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsUploadPost201Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsUploadPost201Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsUploadPost201Response>() {});
        

        return new ApiResponse<ApiV1DemandsUploadPost201Response>(
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

  private HttpRequest.Builder apiV1DemandsUploadPostRequestBuilder(@javax.annotation.Nonnull List<File> files, @javax.annotation.Nonnull String parties, @javax.annotation.Nullable String order, @javax.annotation.Nullable String title, @javax.annotation.Nullable String description, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'files' is set
    if (files == null) {
      throw new ApiException(400, "Missing the required parameter 'files' when calling apiV1DemandsUploadPost");
    }
    // verify the required parameter 'parties' is set
    if (parties == null) {
      throw new ApiException(400, "Missing the required parameter 'parties' when calling apiV1DemandsUploadPost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/upload";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/json");

    MultipartEntityBuilder multiPartBuilder = MultipartEntityBuilder.create();
    boolean hasFiles = false;
    for (int i=0; i < files.size(); i++) {
        multiPartBuilder.addBinaryBody("files", files.get(i));
        hasFiles = true;
    }
    if (order != null) {
        multiPartBuilder.addTextBody("order", order.toString());
    }
    if (title != null) {
        multiPartBuilder.addTextBody("title", title.toString());
    }
    if (description != null) {
        multiPartBuilder.addTextBody("description", description.toString());
    }
    if (parties != null) {
        multiPartBuilder.addTextBody("parties", parties.toString());
    }
    HttpEntity entity = multiPartBuilder.build();
    HttpRequest.BodyPublisher formDataPublisher;
    if (hasFiles) {
        Pipe pipe;
        try {
            pipe = Pipe.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            try (OutputStream outputStream = Channels.newOutputStream(pipe.sink())) {
                entity.writeTo(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        formDataPublisher = HttpRequest.BodyPublishers.ofInputStream(() -> Channels.newInputStream(pipe.source()));
    } else {
        ByteArrayOutputStream formOutputStream = new ByteArrayOutputStream();
        try {
            entity.writeTo(formOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] formBytes = formOutputStream.toByteArray();
        formDataPublisher = HttpRequest.BodyPublishers
            .ofInputStream(() -> new ByteArrayInputStream(formBytes));
    }
    localVarRequestBuilder
        .header("Content-Type", entity.getContentType().getValue())
        .method("POST", formDataPublisher);
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
