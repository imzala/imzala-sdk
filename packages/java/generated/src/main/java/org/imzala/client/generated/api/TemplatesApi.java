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

import org.imzala.client.generated.model.ApiV1TemplatesGet200Response;
import org.imzala.client.generated.model.ApiV1TemplatesGet401Response;
import org.imzala.client.generated.model.ApiV1TemplatesIdDelete200Response;
import org.imzala.client.generated.model.ApiV1TemplatesIdGet200Response;
import org.imzala.client.generated.model.ApiV1TemplatesIdGet404Response;
import org.imzala.client.generated.model.ApiV1TemplatesIdPatch200Response;
import org.imzala.client.generated.model.ApiV1TemplatesIdPatchRequest;
import org.imzala.client.generated.model.ApiV1TemplatesIdUsageGet200Response;
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
public class TemplatesApi {
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

  public TemplatesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public TemplatesApi(ApiClient apiClient) {
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
   * Şablon listesi
   * Aktif şablonlarınızı listeler.
   * @param page  (optional, default to 1)
   * @param limit  (optional, default to 20)
   * @return ApiV1TemplatesGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesGet200Response apiV1TemplatesGet(@javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit) throws ApiException {
    return apiV1TemplatesGet(page, limit, null);
  }

  /**
   * Şablon listesi
   * Aktif şablonlarınızı listeler.
   * @param page  (optional, default to 1)
   * @param limit  (optional, default to 20)
   * @param headers Optional headers to include in the request
   * @return ApiV1TemplatesGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesGet200Response apiV1TemplatesGet(@javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1TemplatesGet200Response> localVarResponse = apiV1TemplatesGetWithHttpInfo(page, limit, headers);
    return localVarResponse.getData();
  }

  /**
   * Şablon listesi
   * Aktif şablonlarınızı listeler.
   * @param page  (optional, default to 1)
   * @param limit  (optional, default to 20)
   * @return ApiResponse&lt;ApiV1TemplatesGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesGet200Response> apiV1TemplatesGetWithHttpInfo(@javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit) throws ApiException {
    return apiV1TemplatesGetWithHttpInfo(page, limit, null);
  }

  /**
   * Şablon listesi
   * Aktif şablonlarınızı listeler.
   * @param page  (optional, default to 1)
   * @param limit  (optional, default to 20)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1TemplatesGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesGet200Response> apiV1TemplatesGetWithHttpInfo(@javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1TemplatesGetRequestBuilder(page, limit, headers);
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
          throw getApiException("apiV1TemplatesGet", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1TemplatesGet200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1TemplatesGet200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1TemplatesGet200Response>() {});
        

        return new ApiResponse<ApiV1TemplatesGet200Response>(
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

  private HttpRequest.Builder apiV1TemplatesGetRequestBuilder(@javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit, Map<String, String> headers) throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/templates";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "page";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("page", page));
    localVarQueryParameterBaseName = "limit";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));

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
   * Şablon sil
   * Şablonu siler (soft delete). Mevcut sözleşmeler etkilenmez.
   * @param id  (required)
   * @return ApiV1TemplatesIdDelete200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesIdDelete200Response apiV1TemplatesIdDelete(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1TemplatesIdDelete(id, null);
  }

  /**
   * Şablon sil
   * Şablonu siler (soft delete). Mevcut sözleşmeler etkilenmez.
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1TemplatesIdDelete200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesIdDelete200Response apiV1TemplatesIdDelete(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1TemplatesIdDelete200Response> localVarResponse = apiV1TemplatesIdDeleteWithHttpInfo(id, headers);
    return localVarResponse.getData();
  }

  /**
   * Şablon sil
   * Şablonu siler (soft delete). Mevcut sözleşmeler etkilenmez.
   * @param id  (required)
   * @return ApiResponse&lt;ApiV1TemplatesIdDelete200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesIdDelete200Response> apiV1TemplatesIdDeleteWithHttpInfo(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1TemplatesIdDeleteWithHttpInfo(id, null);
  }

  /**
   * Şablon sil
   * Şablonu siler (soft delete). Mevcut sözleşmeler etkilenmez.
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1TemplatesIdDelete200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesIdDelete200Response> apiV1TemplatesIdDeleteWithHttpInfo(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1TemplatesIdDeleteRequestBuilder(id, headers);
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
          throw getApiException("apiV1TemplatesIdDelete", localVarResponse);
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

  private HttpRequest.Builder apiV1TemplatesIdDeleteRequestBuilder(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1TemplatesIdDelete");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/templates/{id}"
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
   * Şablon detay
   * Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
   * @param id  (required)
   * @return ApiV1TemplatesIdGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesIdGet200Response apiV1TemplatesIdGet(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1TemplatesIdGet(id, null);
  }

  /**
   * Şablon detay
   * Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1TemplatesIdGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesIdGet200Response apiV1TemplatesIdGet(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1TemplatesIdGet200Response> localVarResponse = apiV1TemplatesIdGetWithHttpInfo(id, headers);
    return localVarResponse.getData();
  }

  /**
   * Şablon detay
   * Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
   * @param id  (required)
   * @return ApiResponse&lt;ApiV1TemplatesIdGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesIdGet200Response> apiV1TemplatesIdGetWithHttpInfo(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1TemplatesIdGetWithHttpInfo(id, null);
  }

  /**
   * Şablon detay
   * Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1TemplatesIdGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesIdGet200Response> apiV1TemplatesIdGetWithHttpInfo(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1TemplatesIdGetRequestBuilder(id, headers);
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
          throw getApiException("apiV1TemplatesIdGet", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1TemplatesIdGet200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1TemplatesIdGet200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1TemplatesIdGet200Response>() {});
        

        return new ApiResponse<ApiV1TemplatesIdGet200Response>(
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

  private HttpRequest.Builder apiV1TemplatesIdGetRequestBuilder(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1TemplatesIdGet");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/templates/{id}"
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
   * Şablon metadata güncelle
   * Şablonun yalnızca metadata alanlarını (name / description / category) günceller. Sayfa/alan/taraf yapısı bu endpoint&#39;ten DEĞİŞTİRİLEMEZ (şablon içeriği panelden düzenlenir). 
   * @param id  (required)
   * @param apiV1TemplatesIdPatchRequest  (required)
   * @return ApiV1TemplatesIdPatch200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesIdPatch200Response apiV1TemplatesIdPatch(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ApiV1TemplatesIdPatchRequest apiV1TemplatesIdPatchRequest) throws ApiException {
    return apiV1TemplatesIdPatch(id, apiV1TemplatesIdPatchRequest, null);
  }

  /**
   * Şablon metadata güncelle
   * Şablonun yalnızca metadata alanlarını (name / description / category) günceller. Sayfa/alan/taraf yapısı bu endpoint&#39;ten DEĞİŞTİRİLEMEZ (şablon içeriği panelden düzenlenir). 
   * @param id  (required)
   * @param apiV1TemplatesIdPatchRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1TemplatesIdPatch200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesIdPatch200Response apiV1TemplatesIdPatch(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ApiV1TemplatesIdPatchRequest apiV1TemplatesIdPatchRequest, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1TemplatesIdPatch200Response> localVarResponse = apiV1TemplatesIdPatchWithHttpInfo(id, apiV1TemplatesIdPatchRequest, headers);
    return localVarResponse.getData();
  }

  /**
   * Şablon metadata güncelle
   * Şablonun yalnızca metadata alanlarını (name / description / category) günceller. Sayfa/alan/taraf yapısı bu endpoint&#39;ten DEĞİŞTİRİLEMEZ (şablon içeriği panelden düzenlenir). 
   * @param id  (required)
   * @param apiV1TemplatesIdPatchRequest  (required)
   * @return ApiResponse&lt;ApiV1TemplatesIdPatch200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesIdPatch200Response> apiV1TemplatesIdPatchWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ApiV1TemplatesIdPatchRequest apiV1TemplatesIdPatchRequest) throws ApiException {
    return apiV1TemplatesIdPatchWithHttpInfo(id, apiV1TemplatesIdPatchRequest, null);
  }

  /**
   * Şablon metadata güncelle
   * Şablonun yalnızca metadata alanlarını (name / description / category) günceller. Sayfa/alan/taraf yapısı bu endpoint&#39;ten DEĞİŞTİRİLEMEZ (şablon içeriği panelden düzenlenir). 
   * @param id  (required)
   * @param apiV1TemplatesIdPatchRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1TemplatesIdPatch200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesIdPatch200Response> apiV1TemplatesIdPatchWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ApiV1TemplatesIdPatchRequest apiV1TemplatesIdPatchRequest, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1TemplatesIdPatchRequestBuilder(id, apiV1TemplatesIdPatchRequest, headers);
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
          throw getApiException("apiV1TemplatesIdPatch", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1TemplatesIdPatch200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1TemplatesIdPatch200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1TemplatesIdPatch200Response>() {});
        

        return new ApiResponse<ApiV1TemplatesIdPatch200Response>(
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

  private HttpRequest.Builder apiV1TemplatesIdPatchRequestBuilder(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ApiV1TemplatesIdPatchRequest apiV1TemplatesIdPatchRequest, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1TemplatesIdPatch");
    }
    // verify the required parameter 'apiV1TemplatesIdPatchRequest' is set
    if (apiV1TemplatesIdPatchRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'apiV1TemplatesIdPatchRequest' when calling apiV1TemplatesIdPatch");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/templates/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(apiV1TemplatesIdPatchRequest);
      localVarRequestBuilder.method("PATCH", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
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
   * Şablon kullanım kılavuzu (curl + JSON örnek)
   * Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 
   * @param id  (required)
   * @return ApiV1TemplatesIdUsageGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesIdUsageGet200Response apiV1TemplatesIdUsageGet(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1TemplatesIdUsageGet(id, null);
  }

  /**
   * Şablon kullanım kılavuzu (curl + JSON örnek)
   * Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1TemplatesIdUsageGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesIdUsageGet200Response apiV1TemplatesIdUsageGet(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1TemplatesIdUsageGet200Response> localVarResponse = apiV1TemplatesIdUsageGetWithHttpInfo(id, headers);
    return localVarResponse.getData();
  }

  /**
   * Şablon kullanım kılavuzu (curl + JSON örnek)
   * Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 
   * @param id  (required)
   * @return ApiResponse&lt;ApiV1TemplatesIdUsageGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesIdUsageGet200Response> apiV1TemplatesIdUsageGetWithHttpInfo(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1TemplatesIdUsageGetWithHttpInfo(id, null);
  }

  /**
   * Şablon kullanım kılavuzu (curl + JSON örnek)
   * Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1TemplatesIdUsageGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesIdUsageGet200Response> apiV1TemplatesIdUsageGetWithHttpInfo(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1TemplatesIdUsageGetRequestBuilder(id, headers);
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
          throw getApiException("apiV1TemplatesIdUsageGet", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1TemplatesIdUsageGet200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1TemplatesIdUsageGet200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1TemplatesIdUsageGet200Response>() {});
        

        return new ApiResponse<ApiV1TemplatesIdUsageGet200Response>(
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

  private HttpRequest.Builder apiV1TemplatesIdUsageGetRequestBuilder(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1TemplatesIdUsageGet");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/templates/{id}/usage"
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

}
