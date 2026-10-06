/*
 * imzala External API
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.10.2 · **Son güncelleme:** 2026-10-06  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır (aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Onaylayan (onay adımı) Şablonda bir taraf **Onaylayan** olarak işaretlenmişse, o şablondan oluşturulan sözleşmede bu taraf belgeyi imzalamaz, onaylar. Onaylayan içeren sözleşmelerde davetler iki aşamada gönderilir: - Sözleşme oluşturulurken (`POST /api/v1/demands`,   `POST /api/v1/demands/bulk`) ve `POST /api/v1/demands/{demandId}/dispatch`   çağrıldığında önce yalnızca onay bekleyen onaylayanlar davet edilir.   Sıralı sözleşmede (`ordered: true`) onaylayanlar da kendi sıralarıyla   davet edilir. - İmzacılar, tüm onaylayanlar onayladıktan sonra otomatik olarak davet   edilir. - Bir onaylayan sözleşmeyi reddederse imzacılar davet edilmez;   `dispatch` çağrısı da kimseye davet göndermez. - Onay vermiş bir onaylayan `dispatch` ile yeniden davet edilmez.  Onaylayan içermeyen sözleşmelerde davet davranışı değişmez.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  `403 AI_TOOL_ACCESS_DISABLED`: organizasyon yöneticisi, organizasyonun yapay zekâ araçlarıyla erişimini kapatmıştır. Bu kontrol resmî İmzala MCP istemcisini (`User-Agent` başlığı `imzala-mcp/` ile başlayan istekler) tanır ve yalnız organizasyon çalışma alanını hedefleyen isteklerde uygulanır. Yanıt gövdesi:  ```json {   \"error\": \"AI_TOOL_ACCESS_DISABLED\",   \"code\": \"AI_TOOL_ACCESS_DISABLED\",   \"message\": \"Kurumunuz yapay zekâ araçlarının erişimini kapattı.\" } ```  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez. - Hatırlatma yalnızca davet sırası gelmiş taraflara gönderilir. Onaylayan   onayını beklerken imzacılar, sıralı sözleşmede sırası henüz gelmemiş   taraflar ve bir onaylayan reddettiyse tüm taraflar `details[]` içinde   `skipped` olarak `reason: \"party_not_eligible\"` ile döner.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (11) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı | | `contract.expiring` | Takip edilen sözleşme bitiş tarihine yaklaşıyor | | `contract.ended` | Takip edilen sözleşmenin platformdaki bitiş tarihi geçti | | `contract.advanced` | Otomatik yenilenen sözleşmenin takip edilen bitişi ileri alındı |  > **Not (contract.\\* olayları):** Bunlar platformun **takip amaçlı** > kayıtlarıdır; sözleşmenin hukuken yenilendiğini veya sona erdiğini > BELİRTMEZ. Detay için `ContractTerm` şemasına ve `state` alanına bakın.  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type`: yukarıdaki 11 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.10.2
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
import org.imzala.client.generated.model.ApiV1DemandsBulkPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsBulkPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDispatchPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDispatchPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsDocIdPatchRequest;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsGet200Response;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsOrderPutRequest;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsPost201Response;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsGet200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdArchivePost200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdCancelPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdCancelPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsIdDelete409Response;
import org.imzala.client.generated.model.ApiV1DemandsIdEmbedSessionPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdEmbedSessionPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsIdGet200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdPartiesPartyIdResendPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdTermPatch200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdTimelineGet200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdUnarchivePost200Response;
import org.imzala.client.generated.model.ApiV1DemandsPost201Response;
import org.imzala.client.generated.model.ApiV1DemandsPost400Response;
import org.imzala.client.generated.model.ApiV1DemandsPost402Response;
import org.imzala.client.generated.model.ApiV1DemandsPost500Response;
import org.imzala.client.generated.model.ApiV1DemandsUploadPost201Response;
import org.imzala.client.generated.model.ApiV1DemandsUploadPost402Response;
import org.imzala.client.generated.model.ApiV1DemandsUploadPost500Response;
import org.imzala.client.generated.model.ApiV1FieldTemplatesIdPreviewLayoutPost200Response;
import org.imzala.client.generated.model.ApiV1TemplatesGet401Response;
import org.imzala.client.generated.model.ApiV1TemplatesIdDelete200Response;
import org.imzala.client.generated.model.ApiV1TemplatesIdGet404Response;
import org.imzala.client.generated.model.CodedError;
import org.imzala.client.generated.model.ContractTermInput;
import org.imzala.client.generated.model.CreateDemandRequest;
import org.imzala.client.generated.model.FieldLayoutUnresolved;
import java.io.File;
import java.time.LocalDate;
import org.imzala.client.generated.model.PatchStampItemRequest;
import org.imzala.client.generated.model.PatchStampItemResponse;
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

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-10-06T23:27:17.689888+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
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
   * Toplu sözleşme oluştur (tek şablondan N alıcı)
   * Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa her satırda önce yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). Şablon çok-belgeli bir zarf   tanımlıyorsa imzacı başına maliyet o imzacıya ATANMIŞ belge   sayısına göre hesaplanır ve belge sayısına göre azalan birim   fiyatlı bir indirim uygulanır (1 belge %0, 2 belge %30, 3 ve üzeri   %50, imzacı başına ayrı yukarı yuvarlanır; bkz. &#x60;POST /demands&#x60;   \&quot;Kredi (çok belgeli zarf)\&quot; bölümü). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir); mutabakat kredi-dışı bir hatayla düşerse satır   &#x60;failed&#x60; (&#x60;error: \&quot;RECONCILE_FAILED\&quot;&#x60;) döner, sözleşme yine taslağa   düşer. - Atamasız imzacı: çok belgeli şablonda eşlenen bir taraf hiçbir   belgeye atanmamışsa (ve &#x60;options.dispatch_notifications&#x60; &#x60;false&#x60;   DEĞİLSE) o satır &#x60;failed&#x60; (&#x60;error: \&quot;PARTY_WITHOUT_DOCUMENTS\&quot;&#x60;)   döner; kontrol satır oluşturulmadan ÖNCE (&#x60;demand_id&#x60; YOK) veya   oluşturulduktan hemen SONRA (sözleşme taslağa düşer, &#x60;demand_id&#x60; +   &#x60;party_ids&#x60; döner) çalışabilir. Diğer satırlar etkilenmez. - Belge seçimi satır başınadır: &#x60;rows[i].documents&#x60; (&#x60;POST /demands&#x60;   ile aynı şema). &#x60;options.documents&#x60; gönderilirse 400   &#x60;INVALID_DOCUMENT_SELECTION&#x60;. Satırın seçim hatası o satırı &#x60;failed&#x60;   yapar (&#x60;error&#x60;: &#x60;INVALID_DOCUMENT_SELECTION&#x60; + &#x60;details&#x60;, ya da   &#x60;PARTY_WITHOUT_DOCUMENTS&#x60; + &#x60;template_party_ids&#x60;); diğer satırlar   etkilenmez. - Belge başına değişkenler (&#x60;document_variables&#x60;) bu uçta   desteklenmez; yalnız &#x60;POST /demands&#x60; ile gönderilir. Satırda (kökte   ya da &#x60;party_mapping[i]&#x60; içinde) gönderilirse o satır &#x60;failed&#x60;   (&#x60;error: \&quot;INVALID_DOCUMENT_VARIABLES\&quot;&#x60;, &#x60;details.reason:   \&quot;unsupported_endpoint\&quot;&#x60;, &#x60;details.path&#x60;) döner ve oluşturulmaz;   &#x60;options.document_variables&#x60; gönderilirse 400   &#x60;INVALID_DOCUMENT_VARIABLES&#x60; döner, hiçbir satır oluşturulmaz. - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
   * @param apiV1DemandsBulkPostRequest  (required)
   * @param xWorkspaceId Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  (optional)
   * @return ApiV1DemandsBulkPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsBulkPost200Response apiV1DemandsBulkPost(@javax.annotation.Nonnull ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, @javax.annotation.Nullable UUID xWorkspaceId) throws ApiException {
    return apiV1DemandsBulkPost(apiV1DemandsBulkPostRequest, xWorkspaceId, null);
  }

  /**
   * Toplu sözleşme oluştur (tek şablondan N alıcı)
   * Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa her satırda önce yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). Şablon çok-belgeli bir zarf   tanımlıyorsa imzacı başına maliyet o imzacıya ATANMIŞ belge   sayısına göre hesaplanır ve belge sayısına göre azalan birim   fiyatlı bir indirim uygulanır (1 belge %0, 2 belge %30, 3 ve üzeri   %50, imzacı başına ayrı yukarı yuvarlanır; bkz. &#x60;POST /demands&#x60;   \&quot;Kredi (çok belgeli zarf)\&quot; bölümü). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir); mutabakat kredi-dışı bir hatayla düşerse satır   &#x60;failed&#x60; (&#x60;error: \&quot;RECONCILE_FAILED\&quot;&#x60;) döner, sözleşme yine taslağa   düşer. - Atamasız imzacı: çok belgeli şablonda eşlenen bir taraf hiçbir   belgeye atanmamışsa (ve &#x60;options.dispatch_notifications&#x60; &#x60;false&#x60;   DEĞİLSE) o satır &#x60;failed&#x60; (&#x60;error: \&quot;PARTY_WITHOUT_DOCUMENTS\&quot;&#x60;)   döner; kontrol satır oluşturulmadan ÖNCE (&#x60;demand_id&#x60; YOK) veya   oluşturulduktan hemen SONRA (sözleşme taslağa düşer, &#x60;demand_id&#x60; +   &#x60;party_ids&#x60; döner) çalışabilir. Diğer satırlar etkilenmez. - Belge seçimi satır başınadır: &#x60;rows[i].documents&#x60; (&#x60;POST /demands&#x60;   ile aynı şema). &#x60;options.documents&#x60; gönderilirse 400   &#x60;INVALID_DOCUMENT_SELECTION&#x60;. Satırın seçim hatası o satırı &#x60;failed&#x60;   yapar (&#x60;error&#x60;: &#x60;INVALID_DOCUMENT_SELECTION&#x60; + &#x60;details&#x60;, ya da   &#x60;PARTY_WITHOUT_DOCUMENTS&#x60; + &#x60;template_party_ids&#x60;); diğer satırlar   etkilenmez. - Belge başına değişkenler (&#x60;document_variables&#x60;) bu uçta   desteklenmez; yalnız &#x60;POST /demands&#x60; ile gönderilir. Satırda (kökte   ya da &#x60;party_mapping[i]&#x60; içinde) gönderilirse o satır &#x60;failed&#x60;   (&#x60;error: \&quot;INVALID_DOCUMENT_VARIABLES\&quot;&#x60;, &#x60;details.reason:   \&quot;unsupported_endpoint\&quot;&#x60;, &#x60;details.path&#x60;) döner ve oluşturulmaz;   &#x60;options.document_variables&#x60; gönderilirse 400   &#x60;INVALID_DOCUMENT_VARIABLES&#x60; döner, hiçbir satır oluşturulmaz. - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
   * @param apiV1DemandsBulkPostRequest  (required)
   * @param xWorkspaceId Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsBulkPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsBulkPost200Response apiV1DemandsBulkPost(@javax.annotation.Nonnull ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, @javax.annotation.Nullable UUID xWorkspaceId, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsBulkPost200Response> localVarResponse = apiV1DemandsBulkPostWithHttpInfo(apiV1DemandsBulkPostRequest, xWorkspaceId, headers);
    return localVarResponse.getData();
  }

  /**
   * Toplu sözleşme oluştur (tek şablondan N alıcı)
   * Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa her satırda önce yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). Şablon çok-belgeli bir zarf   tanımlıyorsa imzacı başına maliyet o imzacıya ATANMIŞ belge   sayısına göre hesaplanır ve belge sayısına göre azalan birim   fiyatlı bir indirim uygulanır (1 belge %0, 2 belge %30, 3 ve üzeri   %50, imzacı başına ayrı yukarı yuvarlanır; bkz. &#x60;POST /demands&#x60;   \&quot;Kredi (çok belgeli zarf)\&quot; bölümü). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir); mutabakat kredi-dışı bir hatayla düşerse satır   &#x60;failed&#x60; (&#x60;error: \&quot;RECONCILE_FAILED\&quot;&#x60;) döner, sözleşme yine taslağa   düşer. - Atamasız imzacı: çok belgeli şablonda eşlenen bir taraf hiçbir   belgeye atanmamışsa (ve &#x60;options.dispatch_notifications&#x60; &#x60;false&#x60;   DEĞİLSE) o satır &#x60;failed&#x60; (&#x60;error: \&quot;PARTY_WITHOUT_DOCUMENTS\&quot;&#x60;)   döner; kontrol satır oluşturulmadan ÖNCE (&#x60;demand_id&#x60; YOK) veya   oluşturulduktan hemen SONRA (sözleşme taslağa düşer, &#x60;demand_id&#x60; +   &#x60;party_ids&#x60; döner) çalışabilir. Diğer satırlar etkilenmez. - Belge seçimi satır başınadır: &#x60;rows[i].documents&#x60; (&#x60;POST /demands&#x60;   ile aynı şema). &#x60;options.documents&#x60; gönderilirse 400   &#x60;INVALID_DOCUMENT_SELECTION&#x60;. Satırın seçim hatası o satırı &#x60;failed&#x60;   yapar (&#x60;error&#x60;: &#x60;INVALID_DOCUMENT_SELECTION&#x60; + &#x60;details&#x60;, ya da   &#x60;PARTY_WITHOUT_DOCUMENTS&#x60; + &#x60;template_party_ids&#x60;); diğer satırlar   etkilenmez. - Belge başına değişkenler (&#x60;document_variables&#x60;) bu uçta   desteklenmez; yalnız &#x60;POST /demands&#x60; ile gönderilir. Satırda (kökte   ya da &#x60;party_mapping[i]&#x60; içinde) gönderilirse o satır &#x60;failed&#x60;   (&#x60;error: \&quot;INVALID_DOCUMENT_VARIABLES\&quot;&#x60;, &#x60;details.reason:   \&quot;unsupported_endpoint\&quot;&#x60;, &#x60;details.path&#x60;) döner ve oluşturulmaz;   &#x60;options.document_variables&#x60; gönderilirse 400   &#x60;INVALID_DOCUMENT_VARIABLES&#x60; döner, hiçbir satır oluşturulmaz. - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
   * @param apiV1DemandsBulkPostRequest  (required)
   * @param xWorkspaceId Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  (optional)
   * @return ApiResponse&lt;ApiV1DemandsBulkPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsBulkPost200Response> apiV1DemandsBulkPostWithHttpInfo(@javax.annotation.Nonnull ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, @javax.annotation.Nullable UUID xWorkspaceId) throws ApiException {
    return apiV1DemandsBulkPostWithHttpInfo(apiV1DemandsBulkPostRequest, xWorkspaceId, null);
  }

  /**
   * Toplu sözleşme oluştur (tek şablondan N alıcı)
   * Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa her satırda önce yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). Şablon çok-belgeli bir zarf   tanımlıyorsa imzacı başına maliyet o imzacıya ATANMIŞ belge   sayısına göre hesaplanır ve belge sayısına göre azalan birim   fiyatlı bir indirim uygulanır (1 belge %0, 2 belge %30, 3 ve üzeri   %50, imzacı başına ayrı yukarı yuvarlanır; bkz. &#x60;POST /demands&#x60;   \&quot;Kredi (çok belgeli zarf)\&quot; bölümü). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir); mutabakat kredi-dışı bir hatayla düşerse satır   &#x60;failed&#x60; (&#x60;error: \&quot;RECONCILE_FAILED\&quot;&#x60;) döner, sözleşme yine taslağa   düşer. - Atamasız imzacı: çok belgeli şablonda eşlenen bir taraf hiçbir   belgeye atanmamışsa (ve &#x60;options.dispatch_notifications&#x60; &#x60;false&#x60;   DEĞİLSE) o satır &#x60;failed&#x60; (&#x60;error: \&quot;PARTY_WITHOUT_DOCUMENTS\&quot;&#x60;)   döner; kontrol satır oluşturulmadan ÖNCE (&#x60;demand_id&#x60; YOK) veya   oluşturulduktan hemen SONRA (sözleşme taslağa düşer, &#x60;demand_id&#x60; +   &#x60;party_ids&#x60; döner) çalışabilir. Diğer satırlar etkilenmez. - Belge seçimi satır başınadır: &#x60;rows[i].documents&#x60; (&#x60;POST /demands&#x60;   ile aynı şema). &#x60;options.documents&#x60; gönderilirse 400   &#x60;INVALID_DOCUMENT_SELECTION&#x60;. Satırın seçim hatası o satırı &#x60;failed&#x60;   yapar (&#x60;error&#x60;: &#x60;INVALID_DOCUMENT_SELECTION&#x60; + &#x60;details&#x60;, ya da   &#x60;PARTY_WITHOUT_DOCUMENTS&#x60; + &#x60;template_party_ids&#x60;); diğer satırlar   etkilenmez. - Belge başına değişkenler (&#x60;document_variables&#x60;) bu uçta   desteklenmez; yalnız &#x60;POST /demands&#x60; ile gönderilir. Satırda (kökte   ya da &#x60;party_mapping[i]&#x60; içinde) gönderilirse o satır &#x60;failed&#x60;   (&#x60;error: \&quot;INVALID_DOCUMENT_VARIABLES\&quot;&#x60;, &#x60;details.reason:   \&quot;unsupported_endpoint\&quot;&#x60;, &#x60;details.path&#x60;) döner ve oluşturulmaz;   &#x60;options.document_variables&#x60; gönderilirse 400   &#x60;INVALID_DOCUMENT_VARIABLES&#x60; döner, hiçbir satır oluşturulmaz. - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
   * @param apiV1DemandsBulkPostRequest  (required)
   * @param xWorkspaceId Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsBulkPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsBulkPost200Response> apiV1DemandsBulkPostWithHttpInfo(@javax.annotation.Nonnull ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, @javax.annotation.Nullable UUID xWorkspaceId, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsBulkPostRequestBuilder(apiV1DemandsBulkPostRequest, xWorkspaceId, headers);
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
          throw getApiException("apiV1DemandsBulkPost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsBulkPost200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsBulkPost200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsBulkPost200Response>() {});
        

        return new ApiResponse<ApiV1DemandsBulkPost200Response>(
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

  private HttpRequest.Builder apiV1DemandsBulkPostRequestBuilder(@javax.annotation.Nonnull ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, @javax.annotation.Nullable UUID xWorkspaceId, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'apiV1DemandsBulkPostRequest' is set
    if (apiV1DemandsBulkPostRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'apiV1DemandsBulkPostRequest' when calling apiV1DemandsBulkPost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/bulk";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    if (xWorkspaceId != null) {
      localVarRequestBuilder.header("X-Workspace-Id", xWorkspaceId.toString());
    }
    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(apiV1DemandsBulkPostRequest);
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
   * Zarfı imzaya gönder (yayınla + davet)
   * Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  **Onaylayan:** Sözleşmede Onaylayan varsa bu uç önce yalnızca onay bekleyen onaylayanları davet eder; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir. Bir onaylayan reddettiyse bu uç kimseye davet göndermez; onay vermiş onaylayan yeniden davet edilmez.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir. **Bu uç çağrılana kadar** birden çok belgeli bir zarfın oluşturma yanıtındaki &#x60;signing_url&#x60;&#39;leri ÇALIŞMAZ (imzacı için 410; &#x60;POST .../parties/{partyId}/resend&#x60; ve &#x60;POST .../reminders&#x60; için 409 &#x60;ENVELOPE_NOT_DISPATCHED&#x60; döner); belge uçları (&#x60;/documents*&#x60;) ise gönderim beklemeden kullanılabilir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 
   * @param demandId  (required)
   * @param apiV1DemandsDemandIdDispatchPostRequest  (optional)
   * @return ApiV1DemandsDemandIdDispatchPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDispatchPost200Response apiV1DemandsDemandIdDispatchPost(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nullable ApiV1DemandsDemandIdDispatchPostRequest apiV1DemandsDemandIdDispatchPostRequest) throws ApiException {
    return apiV1DemandsDemandIdDispatchPost(demandId, apiV1DemandsDemandIdDispatchPostRequest, null);
  }

  /**
   * Zarfı imzaya gönder (yayınla + davet)
   * Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  **Onaylayan:** Sözleşmede Onaylayan varsa bu uç önce yalnızca onay bekleyen onaylayanları davet eder; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir. Bir onaylayan reddettiyse bu uç kimseye davet göndermez; onay vermiş onaylayan yeniden davet edilmez.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir. **Bu uç çağrılana kadar** birden çok belgeli bir zarfın oluşturma yanıtındaki &#x60;signing_url&#x60;&#39;leri ÇALIŞMAZ (imzacı için 410; &#x60;POST .../parties/{partyId}/resend&#x60; ve &#x60;POST .../reminders&#x60; için 409 &#x60;ENVELOPE_NOT_DISPATCHED&#x60; döner); belge uçları (&#x60;/documents*&#x60;) ise gönderim beklemeden kullanılabilir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 
   * @param demandId  (required)
   * @param apiV1DemandsDemandIdDispatchPostRequest  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsDemandIdDispatchPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDispatchPost200Response apiV1DemandsDemandIdDispatchPost(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nullable ApiV1DemandsDemandIdDispatchPostRequest apiV1DemandsDemandIdDispatchPostRequest, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response> localVarResponse = apiV1DemandsDemandIdDispatchPostWithHttpInfo(demandId, apiV1DemandsDemandIdDispatchPostRequest, headers);
    return localVarResponse.getData();
  }

  /**
   * Zarfı imzaya gönder (yayınla + davet)
   * Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  **Onaylayan:** Sözleşmede Onaylayan varsa bu uç önce yalnızca onay bekleyen onaylayanları davet eder; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir. Bir onaylayan reddettiyse bu uç kimseye davet göndermez; onay vermiş onaylayan yeniden davet edilmez.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir. **Bu uç çağrılana kadar** birden çok belgeli bir zarfın oluşturma yanıtındaki &#x60;signing_url&#x60;&#39;leri ÇALIŞMAZ (imzacı için 410; &#x60;POST .../parties/{partyId}/resend&#x60; ve &#x60;POST .../reminders&#x60; için 409 &#x60;ENVELOPE_NOT_DISPATCHED&#x60; döner); belge uçları (&#x60;/documents*&#x60;) ise gönderim beklemeden kullanılabilir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 
   * @param demandId  (required)
   * @param apiV1DemandsDemandIdDispatchPostRequest  (optional)
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDispatchPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response> apiV1DemandsDemandIdDispatchPostWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nullable ApiV1DemandsDemandIdDispatchPostRequest apiV1DemandsDemandIdDispatchPostRequest) throws ApiException {
    return apiV1DemandsDemandIdDispatchPostWithHttpInfo(demandId, apiV1DemandsDemandIdDispatchPostRequest, null);
  }

  /**
   * Zarfı imzaya gönder (yayınla + davet)
   * Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  **Onaylayan:** Sözleşmede Onaylayan varsa bu uç önce yalnızca onay bekleyen onaylayanları davet eder; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir. Bir onaylayan reddettiyse bu uç kimseye davet göndermez; onay vermiş onaylayan yeniden davet edilmez.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir. **Bu uç çağrılana kadar** birden çok belgeli bir zarfın oluşturma yanıtındaki &#x60;signing_url&#x60;&#39;leri ÇALIŞMAZ (imzacı için 410; &#x60;POST .../parties/{partyId}/resend&#x60; ve &#x60;POST .../reminders&#x60; için 409 &#x60;ENVELOPE_NOT_DISPATCHED&#x60; döner); belge uçları (&#x60;/documents*&#x60;) ise gönderim beklemeden kullanılabilir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 
   * @param demandId  (required)
   * @param apiV1DemandsDemandIdDispatchPostRequest  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDispatchPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response> apiV1DemandsDemandIdDispatchPostWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nullable ApiV1DemandsDemandIdDispatchPostRequest apiV1DemandsDemandIdDispatchPostRequest, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsDemandIdDispatchPostRequestBuilder(demandId, apiV1DemandsDemandIdDispatchPostRequest, headers);
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
          throw getApiException("apiV1DemandsDemandIdDispatchPost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsDemandIdDispatchPost200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsDemandIdDispatchPost200Response>() {});
        

        return new ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response>(
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

  private HttpRequest.Builder apiV1DemandsDemandIdDispatchPostRequestBuilder(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nullable ApiV1DemandsDemandIdDispatchPostRequest apiV1DemandsDemandIdDispatchPostRequest, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'demandId' is set
    if (demandId == null) {
      throw new ApiException(400, "Missing the required parameter 'demandId' when calling apiV1DemandsDemandIdDispatchPost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{demandId}/dispatch"
        .replace("{demandId}", ApiClient.urlEncode(demandId.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(apiV1DemandsDemandIdDispatchPostRequest);
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
   * Belgeye imzacı ata (tam-küme replace)
   * Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 
   * @param demandId  (required)
   * @param docId  (required)
   * @param apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest  (required)
   * @return ApiV1DemandsDemandIdDocumentsPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDocumentsPost201Response apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest) throws ApiException {
    return apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, null);
  }

  /**
   * Belgeye imzacı ata (tam-küme replace)
   * Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 
   * @param demandId  (required)
   * @param docId  (required)
   * @param apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsDemandIdDocumentsPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDocumentsPost201Response apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> localVarResponse = apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, headers);
    return localVarResponse.getData();
  }

  /**
   * Belgeye imzacı ata (tam-küme replace)
   * Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 
   * @param demandId  (required)
   * @param docId  (required)
   * @param apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest  (required)
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDocumentsPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest) throws ApiException {
    return apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, null);
  }

  /**
   * Belgeye imzacı ata (tam-küme replace)
   * Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 
   * @param demandId  (required)
   * @param docId  (required)
   * @param apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDocumentsPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequestBuilder(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, headers);
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
          throw getApiException("apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsDemandIdDocumentsPost201Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsDemandIdDocumentsPost201Response>() {});
        

        return new ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>(
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

  private HttpRequest.Builder apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequestBuilder(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'demandId' is set
    if (demandId == null) {
      throw new ApiException(400, "Missing the required parameter 'demandId' when calling apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut");
    }
    // verify the required parameter 'docId' is set
    if (docId == null) {
      throw new ApiException(400, "Missing the required parameter 'docId' when calling apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut");
    }
    // verify the required parameter 'apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest' is set
    if (apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest' when calling apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{demandId}/documents/{docId}/assignments"
        .replace("{demandId}", ApiClient.urlEncode(demandId.toString()))
        .replace("{docId}", ApiClient.urlEncode(docId.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest);
      localVarRequestBuilder.method("PUT", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
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
   * Belgeyi zarftan sil
   * Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 
   * @param demandId  (required)
   * @param docId  (required)
   * @return ApiV1TemplatesIdDelete200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesIdDelete200Response apiV1DemandsDemandIdDocumentsDocIdDelete(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId) throws ApiException {
    return apiV1DemandsDemandIdDocumentsDocIdDelete(demandId, docId, null);
  }

  /**
   * Belgeyi zarftan sil
   * Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 
   * @param demandId  (required)
   * @param docId  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1TemplatesIdDelete200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesIdDelete200Response apiV1DemandsDemandIdDocumentsDocIdDelete(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1TemplatesIdDelete200Response> localVarResponse = apiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo(demandId, docId, headers);
    return localVarResponse.getData();
  }

  /**
   * Belgeyi zarftan sil
   * Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 
   * @param demandId  (required)
   * @param docId  (required)
   * @return ApiResponse&lt;ApiV1TemplatesIdDelete200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesIdDelete200Response> apiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId) throws ApiException {
    return apiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo(demandId, docId, null);
  }

  /**
   * Belgeyi zarftan sil
   * Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 
   * @param demandId  (required)
   * @param docId  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1TemplatesIdDelete200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesIdDelete200Response> apiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsDemandIdDocumentsDocIdDeleteRequestBuilder(demandId, docId, headers);
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
          throw getApiException("apiV1DemandsDemandIdDocumentsDocIdDelete", localVarResponse);
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

  private HttpRequest.Builder apiV1DemandsDemandIdDocumentsDocIdDeleteRequestBuilder(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'demandId' is set
    if (demandId == null) {
      throw new ApiException(400, "Missing the required parameter 'demandId' when calling apiV1DemandsDemandIdDocumentsDocIdDelete");
    }
    // verify the required parameter 'docId' is set
    if (docId == null) {
      throw new ApiException(400, "Missing the required parameter 'docId' when calling apiV1DemandsDemandIdDocumentsDocIdDelete");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{demandId}/documents/{docId}"
        .replace("{demandId}", ApiClient.urlEncode(demandId.toString()))
        .replace("{docId}", ApiClient.urlEncode(docId.toString()));

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
   * Belge metadata güncelle
   * Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 
   * @param demandId  (required)
   * @param docId  (required)
   * @param apiV1DemandsDemandIdDocumentsDocIdPatchRequest  (required)
   * @return ApiV1DemandsDemandIdDocumentsPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDocumentsPost201Response apiV1DemandsDemandIdDocumentsDocIdPatch(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest) throws ApiException {
    return apiV1DemandsDemandIdDocumentsDocIdPatch(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest, null);
  }

  /**
   * Belge metadata güncelle
   * Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 
   * @param demandId  (required)
   * @param docId  (required)
   * @param apiV1DemandsDemandIdDocumentsDocIdPatchRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsDemandIdDocumentsPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDocumentsPost201Response apiV1DemandsDemandIdDocumentsDocIdPatch(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> localVarResponse = apiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest, headers);
    return localVarResponse.getData();
  }

  /**
   * Belge metadata güncelle
   * Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 
   * @param demandId  (required)
   * @param docId  (required)
   * @param apiV1DemandsDemandIdDocumentsDocIdPatchRequest  (required)
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDocumentsPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> apiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest) throws ApiException {
    return apiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest, null);
  }

  /**
   * Belge metadata güncelle
   * Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 
   * @param demandId  (required)
   * @param docId  (required)
   * @param apiV1DemandsDemandIdDocumentsDocIdPatchRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDocumentsPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> apiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsDemandIdDocumentsDocIdPatchRequestBuilder(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest, headers);
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
          throw getApiException("apiV1DemandsDemandIdDocumentsDocIdPatch", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsDemandIdDocumentsPost201Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsDemandIdDocumentsPost201Response>() {});
        

        return new ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>(
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

  private HttpRequest.Builder apiV1DemandsDemandIdDocumentsDocIdPatchRequestBuilder(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull UUID docId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'demandId' is set
    if (demandId == null) {
      throw new ApiException(400, "Missing the required parameter 'demandId' when calling apiV1DemandsDemandIdDocumentsDocIdPatch");
    }
    // verify the required parameter 'docId' is set
    if (docId == null) {
      throw new ApiException(400, "Missing the required parameter 'docId' when calling apiV1DemandsDemandIdDocumentsDocIdPatch");
    }
    // verify the required parameter 'apiV1DemandsDemandIdDocumentsDocIdPatchRequest' is set
    if (apiV1DemandsDemandIdDocumentsDocIdPatchRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'apiV1DemandsDemandIdDocumentsDocIdPatchRequest' when calling apiV1DemandsDemandIdDocumentsDocIdPatch");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{demandId}/documents/{docId}"
        .replace("{demandId}", ApiClient.urlEncode(demandId.toString()))
        .replace("{docId}", ApiClient.urlEncode(docId.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(apiV1DemandsDemandIdDocumentsDocIdPatchRequest);
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
   * Zarf belge listesi
   * Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 
   * @param demandId  (required)
   * @param view &#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). (optional)
   * @return ApiV1DemandsDemandIdDocumentsGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDocumentsGet200Response apiV1DemandsDemandIdDocumentsGet(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nullable String view) throws ApiException {
    return apiV1DemandsDemandIdDocumentsGet(demandId, view, null);
  }

  /**
   * Zarf belge listesi
   * Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 
   * @param demandId  (required)
   * @param view &#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). (optional)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsDemandIdDocumentsGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDocumentsGet200Response apiV1DemandsDemandIdDocumentsGet(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nullable String view, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> localVarResponse = apiV1DemandsDemandIdDocumentsGetWithHttpInfo(demandId, view, headers);
    return localVarResponse.getData();
  }

  /**
   * Zarf belge listesi
   * Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 
   * @param demandId  (required)
   * @param view &#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). (optional)
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDocumentsGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> apiV1DemandsDemandIdDocumentsGetWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nullable String view) throws ApiException {
    return apiV1DemandsDemandIdDocumentsGetWithHttpInfo(demandId, view, null);
  }

  /**
   * Zarf belge listesi
   * Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 
   * @param demandId  (required)
   * @param view &#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). (optional)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDocumentsGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> apiV1DemandsDemandIdDocumentsGetWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nullable String view, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsDemandIdDocumentsGetRequestBuilder(demandId, view, headers);
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
          throw getApiException("apiV1DemandsDemandIdDocumentsGet", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsDemandIdDocumentsGet200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsDemandIdDocumentsGet200Response>() {});
        

        return new ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response>(
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

  private HttpRequest.Builder apiV1DemandsDemandIdDocumentsGetRequestBuilder(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nullable String view, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'demandId' is set
    if (demandId == null) {
      throw new ApiException(400, "Missing the required parameter 'demandId' when calling apiV1DemandsDemandIdDocumentsGet");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{demandId}/documents"
        .replace("{demandId}", ApiClient.urlEncode(demandId.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "view";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("view", view));

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
   * Zarftaki belgelerin sırasını değiştir
   * Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 
   * @param demandId  (required)
   * @param apiV1DemandsDemandIdDocumentsOrderPutRequest  (required)
   * @return ApiV1DemandsDemandIdDocumentsGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDocumentsGet200Response apiV1DemandsDemandIdDocumentsOrderPut(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest) throws ApiException {
    return apiV1DemandsDemandIdDocumentsOrderPut(demandId, apiV1DemandsDemandIdDocumentsOrderPutRequest, null);
  }

  /**
   * Zarftaki belgelerin sırasını değiştir
   * Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 
   * @param demandId  (required)
   * @param apiV1DemandsDemandIdDocumentsOrderPutRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsDemandIdDocumentsGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDocumentsGet200Response apiV1DemandsDemandIdDocumentsOrderPut(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> localVarResponse = apiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo(demandId, apiV1DemandsDemandIdDocumentsOrderPutRequest, headers);
    return localVarResponse.getData();
  }

  /**
   * Zarftaki belgelerin sırasını değiştir
   * Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 
   * @param demandId  (required)
   * @param apiV1DemandsDemandIdDocumentsOrderPutRequest  (required)
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDocumentsGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> apiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest) throws ApiException {
    return apiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo(demandId, apiV1DemandsDemandIdDocumentsOrderPutRequest, null);
  }

  /**
   * Zarftaki belgelerin sırasını değiştir
   * Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 
   * @param demandId  (required)
   * @param apiV1DemandsDemandIdDocumentsOrderPutRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDocumentsGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> apiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsDemandIdDocumentsOrderPutRequestBuilder(demandId, apiV1DemandsDemandIdDocumentsOrderPutRequest, headers);
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
          throw getApiException("apiV1DemandsDemandIdDocumentsOrderPut", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsDemandIdDocumentsGet200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsDemandIdDocumentsGet200Response>() {});
        

        return new ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response>(
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

  private HttpRequest.Builder apiV1DemandsDemandIdDocumentsOrderPutRequestBuilder(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'demandId' is set
    if (demandId == null) {
      throw new ApiException(400, "Missing the required parameter 'demandId' when calling apiV1DemandsDemandIdDocumentsOrderPut");
    }
    // verify the required parameter 'apiV1DemandsDemandIdDocumentsOrderPutRequest' is set
    if (apiV1DemandsDemandIdDocumentsOrderPutRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'apiV1DemandsDemandIdDocumentsOrderPutRequest' when calling apiV1DemandsDemandIdDocumentsOrderPut");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{demandId}/documents/order"
        .replace("{demandId}", ApiClient.urlEncode(demandId.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(apiV1DemandsDemandIdDocumentsOrderPutRequest);
      localVarRequestBuilder.method("PUT", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
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
   * Zarfa metadata-only belge ekle
   * Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
   * @param demandId  (required)
   * @param apiV1DemandsDemandIdDocumentsPostRequest  (required)
   * @return ApiV1DemandsDemandIdDocumentsPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDocumentsPost201Response apiV1DemandsDemandIdDocumentsPost(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest) throws ApiException {
    return apiV1DemandsDemandIdDocumentsPost(demandId, apiV1DemandsDemandIdDocumentsPostRequest, null);
  }

  /**
   * Zarfa metadata-only belge ekle
   * Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
   * @param demandId  (required)
   * @param apiV1DemandsDemandIdDocumentsPostRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsDemandIdDocumentsPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDocumentsPost201Response apiV1DemandsDemandIdDocumentsPost(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> localVarResponse = apiV1DemandsDemandIdDocumentsPostWithHttpInfo(demandId, apiV1DemandsDemandIdDocumentsPostRequest, headers);
    return localVarResponse.getData();
  }

  /**
   * Zarfa metadata-only belge ekle
   * Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
   * @param demandId  (required)
   * @param apiV1DemandsDemandIdDocumentsPostRequest  (required)
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDocumentsPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> apiV1DemandsDemandIdDocumentsPostWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest) throws ApiException {
    return apiV1DemandsDemandIdDocumentsPostWithHttpInfo(demandId, apiV1DemandsDemandIdDocumentsPostRequest, null);
  }

  /**
   * Zarfa metadata-only belge ekle
   * Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
   * @param demandId  (required)
   * @param apiV1DemandsDemandIdDocumentsPostRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDocumentsPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> apiV1DemandsDemandIdDocumentsPostWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsDemandIdDocumentsPostRequestBuilder(demandId, apiV1DemandsDemandIdDocumentsPostRequest, headers);
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
          throw getApiException("apiV1DemandsDemandIdDocumentsPost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsDemandIdDocumentsPost201Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsDemandIdDocumentsPost201Response>() {});
        

        return new ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>(
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

  private HttpRequest.Builder apiV1DemandsDemandIdDocumentsPostRequestBuilder(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'demandId' is set
    if (demandId == null) {
      throw new ApiException(400, "Missing the required parameter 'demandId' when calling apiV1DemandsDemandIdDocumentsPost");
    }
    // verify the required parameter 'apiV1DemandsDemandIdDocumentsPostRequest' is set
    if (apiV1DemandsDemandIdDocumentsPostRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'apiV1DemandsDemandIdDocumentsPostRequest' when calling apiV1DemandsDemandIdDocumentsPost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{demandId}/documents"
        .replace("{demandId}", ApiClient.urlEncode(demandId.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(apiV1DemandsDemandIdDocumentsPostRequest);
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
   * Zarfa dosya yükle (belge başına tek dosya)
   * Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
   * @param demandId  (required)
   * @param _file PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya. (required)
   * @param idempotencyKey Zorunlu tekrar-koruma anahtarı. (required)
   * @param title  (required)
   * @param docKind  (optional, default to OTHER)
   * @param isRequired Multipart alanı — string olarak gönderilir. (optional, default to true)
   * @return ApiV1DemandsDemandIdDocumentsPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDocumentsPost201Response apiV1DemandsDemandIdDocumentsUploadPost(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull File _file, @javax.annotation.Nonnull String idempotencyKey, @javax.annotation.Nonnull String title, @javax.annotation.Nullable String docKind, @javax.annotation.Nullable String isRequired) throws ApiException {
    return apiV1DemandsDemandIdDocumentsUploadPost(demandId, _file, idempotencyKey, title, docKind, isRequired, null);
  }

  /**
   * Zarfa dosya yükle (belge başına tek dosya)
   * Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
   * @param demandId  (required)
   * @param _file PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya. (required)
   * @param idempotencyKey Zorunlu tekrar-koruma anahtarı. (required)
   * @param title  (required)
   * @param docKind  (optional, default to OTHER)
   * @param isRequired Multipart alanı — string olarak gönderilir. (optional, default to true)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsDemandIdDocumentsPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsDemandIdDocumentsPost201Response apiV1DemandsDemandIdDocumentsUploadPost(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull File _file, @javax.annotation.Nonnull String idempotencyKey, @javax.annotation.Nonnull String title, @javax.annotation.Nullable String docKind, @javax.annotation.Nullable String isRequired, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> localVarResponse = apiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo(demandId, _file, idempotencyKey, title, docKind, isRequired, headers);
    return localVarResponse.getData();
  }

  /**
   * Zarfa dosya yükle (belge başına tek dosya)
   * Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
   * @param demandId  (required)
   * @param _file PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya. (required)
   * @param idempotencyKey Zorunlu tekrar-koruma anahtarı. (required)
   * @param title  (required)
   * @param docKind  (optional, default to OTHER)
   * @param isRequired Multipart alanı — string olarak gönderilir. (optional, default to true)
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDocumentsPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> apiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull File _file, @javax.annotation.Nonnull String idempotencyKey, @javax.annotation.Nonnull String title, @javax.annotation.Nullable String docKind, @javax.annotation.Nullable String isRequired) throws ApiException {
    return apiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo(demandId, _file, idempotencyKey, title, docKind, isRequired, null);
  }

  /**
   * Zarfa dosya yükle (belge başına tek dosya)
   * Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
   * @param demandId  (required)
   * @param _file PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya. (required)
   * @param idempotencyKey Zorunlu tekrar-koruma anahtarı. (required)
   * @param title  (required)
   * @param docKind  (optional, default to OTHER)
   * @param isRequired Multipart alanı — string olarak gönderilir. (optional, default to true)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsDemandIdDocumentsPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> apiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull File _file, @javax.annotation.Nonnull String idempotencyKey, @javax.annotation.Nonnull String title, @javax.annotation.Nullable String docKind, @javax.annotation.Nullable String isRequired, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsDemandIdDocumentsUploadPostRequestBuilder(demandId, _file, idempotencyKey, title, docKind, isRequired, headers);
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
          throw getApiException("apiV1DemandsDemandIdDocumentsUploadPost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsDemandIdDocumentsPost201Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsDemandIdDocumentsPost201Response>() {});
        

        return new ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>(
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

  private HttpRequest.Builder apiV1DemandsDemandIdDocumentsUploadPostRequestBuilder(@javax.annotation.Nonnull UUID demandId, @javax.annotation.Nonnull File _file, @javax.annotation.Nonnull String idempotencyKey, @javax.annotation.Nonnull String title, @javax.annotation.Nullable String docKind, @javax.annotation.Nullable String isRequired, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'demandId' is set
    if (demandId == null) {
      throw new ApiException(400, "Missing the required parameter 'demandId' when calling apiV1DemandsDemandIdDocumentsUploadPost");
    }
    // verify the required parameter '_file' is set
    if (_file == null) {
      throw new ApiException(400, "Missing the required parameter '_file' when calling apiV1DemandsDemandIdDocumentsUploadPost");
    }
    // verify the required parameter 'idempotencyKey' is set
    if (idempotencyKey == null) {
      throw new ApiException(400, "Missing the required parameter 'idempotencyKey' when calling apiV1DemandsDemandIdDocumentsUploadPost");
    }
    // verify the required parameter 'title' is set
    if (title == null) {
      throw new ApiException(400, "Missing the required parameter 'title' when calling apiV1DemandsDemandIdDocumentsUploadPost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{demandId}/documents/upload"
        .replace("{demandId}", ApiClient.urlEncode(demandId.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/json");

    MultipartEntityBuilder multiPartBuilder = MultipartEntityBuilder.create();
    boolean hasFiles = false;
    multiPartBuilder.addBinaryBody("file", _file);
    hasFiles = true;
    if (idempotencyKey != null) {
        multiPartBuilder.addTextBody("idempotency_key", idempotencyKey.toString(), org.apache.http.entity.ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
    }
    if (title != null) {
        multiPartBuilder.addTextBody("title", title.toString(), org.apache.http.entity.ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
    }
    if (docKind != null) {
        multiPartBuilder.addTextBody("doc_kind", docKind.toString(), org.apache.http.entity.ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
    }
    if (isRequired != null) {
        multiPartBuilder.addTextBody("is_required", isRequired.toString(), org.apache.http.entity.ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
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

  /**
   * Sözleşme listesi (counts-only, PII&#39;siz)
   * Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
   * @param status  (optional)
   * @param q Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez. (optional)
   * @param from  (optional)
   * @param to  (optional)
   * @param templateId  (optional)
   * @param page  (optional, default to 1)
   * @param limit Sayfa boyutu (page_size ile aynı) (optional, default to 20)
   * @param sort alan:yön (ör. createdAt:desc) (optional)
   * @param archived Arşiv durumu filtresi. &#x60;exclude&#x60;: yalnız arşivsiz sözleşmeler. &#x60;only&#x60;: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan &#x60;include&#x60;: tüm sözleşmeler (arşivli ve arşivsiz).  (optional)
   * @return ApiV1DemandsGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsGet200Response apiV1DemandsGet(@javax.annotation.Nullable String status, @javax.annotation.Nullable String q, @javax.annotation.Nullable LocalDate from, @javax.annotation.Nullable LocalDate to, @javax.annotation.Nullable UUID templateId, @javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit, @javax.annotation.Nullable String sort, @javax.annotation.Nullable String archived) throws ApiException {
    return apiV1DemandsGet(status, q, from, to, templateId, page, limit, sort, archived, null);
  }

  /**
   * Sözleşme listesi (counts-only, PII&#39;siz)
   * Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
   * @param status  (optional)
   * @param q Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez. (optional)
   * @param from  (optional)
   * @param to  (optional)
   * @param templateId  (optional)
   * @param page  (optional, default to 1)
   * @param limit Sayfa boyutu (page_size ile aynı) (optional, default to 20)
   * @param sort alan:yön (ör. createdAt:desc) (optional)
   * @param archived Arşiv durumu filtresi. &#x60;exclude&#x60;: yalnız arşivsiz sözleşmeler. &#x60;only&#x60;: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan &#x60;include&#x60;: tüm sözleşmeler (arşivli ve arşivsiz).  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsGet200Response apiV1DemandsGet(@javax.annotation.Nullable String status, @javax.annotation.Nullable String q, @javax.annotation.Nullable LocalDate from, @javax.annotation.Nullable LocalDate to, @javax.annotation.Nullable UUID templateId, @javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit, @javax.annotation.Nullable String sort, @javax.annotation.Nullable String archived, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsGet200Response> localVarResponse = apiV1DemandsGetWithHttpInfo(status, q, from, to, templateId, page, limit, sort, archived, headers);
    return localVarResponse.getData();
  }

  /**
   * Sözleşme listesi (counts-only, PII&#39;siz)
   * Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
   * @param status  (optional)
   * @param q Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez. (optional)
   * @param from  (optional)
   * @param to  (optional)
   * @param templateId  (optional)
   * @param page  (optional, default to 1)
   * @param limit Sayfa boyutu (page_size ile aynı) (optional, default to 20)
   * @param sort alan:yön (ör. createdAt:desc) (optional)
   * @param archived Arşiv durumu filtresi. &#x60;exclude&#x60;: yalnız arşivsiz sözleşmeler. &#x60;only&#x60;: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan &#x60;include&#x60;: tüm sözleşmeler (arşivli ve arşivsiz).  (optional)
   * @return ApiResponse&lt;ApiV1DemandsGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsGet200Response> apiV1DemandsGetWithHttpInfo(@javax.annotation.Nullable String status, @javax.annotation.Nullable String q, @javax.annotation.Nullable LocalDate from, @javax.annotation.Nullable LocalDate to, @javax.annotation.Nullable UUID templateId, @javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit, @javax.annotation.Nullable String sort, @javax.annotation.Nullable String archived) throws ApiException {
    return apiV1DemandsGetWithHttpInfo(status, q, from, to, templateId, page, limit, sort, archived, null);
  }

  /**
   * Sözleşme listesi (counts-only, PII&#39;siz)
   * Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
   * @param status  (optional)
   * @param q Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez. (optional)
   * @param from  (optional)
   * @param to  (optional)
   * @param templateId  (optional)
   * @param page  (optional, default to 1)
   * @param limit Sayfa boyutu (page_size ile aynı) (optional, default to 20)
   * @param sort alan:yön (ör. createdAt:desc) (optional)
   * @param archived Arşiv durumu filtresi. &#x60;exclude&#x60;: yalnız arşivsiz sözleşmeler. &#x60;only&#x60;: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan &#x60;include&#x60;: tüm sözleşmeler (arşivli ve arşivsiz).  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsGet200Response> apiV1DemandsGetWithHttpInfo(@javax.annotation.Nullable String status, @javax.annotation.Nullable String q, @javax.annotation.Nullable LocalDate from, @javax.annotation.Nullable LocalDate to, @javax.annotation.Nullable UUID templateId, @javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit, @javax.annotation.Nullable String sort, @javax.annotation.Nullable String archived, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsGetRequestBuilder(status, q, from, to, templateId, page, limit, sort, archived, headers);
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

  private HttpRequest.Builder apiV1DemandsGetRequestBuilder(@javax.annotation.Nullable String status, @javax.annotation.Nullable String q, @javax.annotation.Nullable LocalDate from, @javax.annotation.Nullable LocalDate to, @javax.annotation.Nullable UUID templateId, @javax.annotation.Nullable Integer page, @javax.annotation.Nullable Integer limit, @javax.annotation.Nullable String sort, @javax.annotation.Nullable String archived, Map<String, String> headers) throws ApiException {

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
    localVarQueryParameterBaseName = "archived";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("archived", archived));

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
   * Sözleşmeyi arşivle
   * Arşiv sözleşmenin &#x60;status&#x60;&#39;unu DEĞİŞTİRMEZ; yalnız &#x60;archived_at&#x60; damgası ekler ve arşivlenen sözleşmeyi salt-okunur yapar (&#x60;PATCH .../term&#x60; gibi mutasyonlar 409 &#x60;DEMAND_ARCHIVED&#x60; ile reddedilir; &#x60;GET&#x60; uçları etkilenmez). Yalnız tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeler arşivlenebilir. Zaten arşivliyse idempotent (200, mevcut &#x60;archived_at&#x60; ile). 
   * @param id  (required)
   * @return ApiV1DemandsIdArchivePost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdArchivePost200Response apiV1DemandsIdArchivePost(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdArchivePost(id, null);
  }

  /**
   * Sözleşmeyi arşivle
   * Arşiv sözleşmenin &#x60;status&#x60;&#39;unu DEĞİŞTİRMEZ; yalnız &#x60;archived_at&#x60; damgası ekler ve arşivlenen sözleşmeyi salt-okunur yapar (&#x60;PATCH .../term&#x60; gibi mutasyonlar 409 &#x60;DEMAND_ARCHIVED&#x60; ile reddedilir; &#x60;GET&#x60; uçları etkilenmez). Yalnız tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeler arşivlenebilir. Zaten arşivliyse idempotent (200, mevcut &#x60;archived_at&#x60; ile). 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsIdArchivePost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdArchivePost200Response apiV1DemandsIdArchivePost(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsIdArchivePost200Response> localVarResponse = apiV1DemandsIdArchivePostWithHttpInfo(id, headers);
    return localVarResponse.getData();
  }

  /**
   * Sözleşmeyi arşivle
   * Arşiv sözleşmenin &#x60;status&#x60;&#39;unu DEĞİŞTİRMEZ; yalnız &#x60;archived_at&#x60; damgası ekler ve arşivlenen sözleşmeyi salt-okunur yapar (&#x60;PATCH .../term&#x60; gibi mutasyonlar 409 &#x60;DEMAND_ARCHIVED&#x60; ile reddedilir; &#x60;GET&#x60; uçları etkilenmez). Yalnız tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeler arşivlenebilir. Zaten arşivliyse idempotent (200, mevcut &#x60;archived_at&#x60; ile). 
   * @param id  (required)
   * @return ApiResponse&lt;ApiV1DemandsIdArchivePost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdArchivePost200Response> apiV1DemandsIdArchivePostWithHttpInfo(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdArchivePostWithHttpInfo(id, null);
  }

  /**
   * Sözleşmeyi arşivle
   * Arşiv sözleşmenin &#x60;status&#x60;&#39;unu DEĞİŞTİRMEZ; yalnız &#x60;archived_at&#x60; damgası ekler ve arşivlenen sözleşmeyi salt-okunur yapar (&#x60;PATCH .../term&#x60; gibi mutasyonlar 409 &#x60;DEMAND_ARCHIVED&#x60; ile reddedilir; &#x60;GET&#x60; uçları etkilenmez). Yalnız tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeler arşivlenebilir. Zaten arşivliyse idempotent (200, mevcut &#x60;archived_at&#x60; ile). 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsIdArchivePost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdArchivePost200Response> apiV1DemandsIdArchivePostWithHttpInfo(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdArchivePostRequestBuilder(id, headers);
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
          throw getApiException("apiV1DemandsIdArchivePost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsIdArchivePost200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsIdArchivePost200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsIdArchivePost200Response>() {});
        

        return new ApiResponse<ApiV1DemandsIdArchivePost200Response>(
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

  private HttpRequest.Builder apiV1DemandsIdArchivePostRequestBuilder(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdArchivePost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}/archive"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

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
   * Belge-özgü imzalı PDF (çok-belgeli zarf)
   * Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 
   * @param id  (required)
   * @param documentId Zarftaki belgenin kimliği. (required)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File apiV1DemandsIdBelgeDocumentIdPdfGet(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UUID documentId) throws ApiException {
    return apiV1DemandsIdBelgeDocumentIdPdfGet(id, documentId, null);
  }

  /**
   * Belge-özgü imzalı PDF (çok-belgeli zarf)
   * Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 
   * @param id  (required)
   * @param documentId Zarftaki belgenin kimliği. (required)
   * @param headers Optional headers to include in the request
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File apiV1DemandsIdBelgeDocumentIdPdfGet(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UUID documentId, Map<String, String> headers) throws ApiException {
    ApiResponse<File> localVarResponse = apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo(id, documentId, headers);
    return localVarResponse.getData();
  }

  /**
   * Belge-özgü imzalı PDF (çok-belgeli zarf)
   * Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 
   * @param id  (required)
   * @param documentId Zarftaki belgenin kimliği. (required)
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UUID documentId) throws ApiException {
    return apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo(id, documentId, null);
  }

  /**
   * Belge-özgü imzalı PDF (çok-belgeli zarf)
   * Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 
   * @param id  (required)
   * @param documentId Zarftaki belgenin kimliği. (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UUID documentId, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdBelgeDocumentIdPdfGetRequestBuilder(id, documentId, headers);
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
          throw getApiException("apiV1DemandsIdBelgeDocumentIdPdfGet", localVarResponse);
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

  private HttpRequest.Builder apiV1DemandsIdBelgeDocumentIdPdfGetRequestBuilder(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull UUID documentId, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdBelgeDocumentIdPdfGet");
    }
    // verify the required parameter 'documentId' is set
    if (documentId == null) {
      throw new ApiException(400, "Missing the required parameter 'documentId' when calling apiV1DemandsIdBelgeDocumentIdPdfGet");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}/belge/{document_id}/pdf"
        .replace("{id}", ApiClient.urlEncode(id.toString()))
        .replace("{document_id}", ApiClient.urlEncode(documentId.toString()));

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
   * Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. Arşivlenmiş sözleşme de silinemez → 409 &#x60;DEMAND_ARCHIVED&#x60;; önce &#x60;POST /api/v1/demands/{id}/unarchive&#x60; ile arşivden çıkarın. 
   * @param id  (required)
   * @return ApiV1TemplatesIdDelete200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1TemplatesIdDelete200Response apiV1DemandsIdDelete(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdDelete(id, null);
  }

  /**
   * Sözleşme sil (yalnızca tamamlanmamış)
   * Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. Arşivlenmiş sözleşme de silinemez → 409 &#x60;DEMAND_ARCHIVED&#x60;; önce &#x60;POST /api/v1/demands/{id}/unarchive&#x60; ile arşivden çıkarın. 
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
   * Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. Arşivlenmiş sözleşme de silinemez → 409 &#x60;DEMAND_ARCHIVED&#x60;; önce &#x60;POST /api/v1/demands/{id}/unarchive&#x60; ile arşivden çıkarın. 
   * @param id  (required)
   * @return ApiResponse&lt;ApiV1TemplatesIdDelete200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1TemplatesIdDelete200Response> apiV1DemandsIdDeleteWithHttpInfo(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdDeleteWithHttpInfo(id, null);
  }

  /**
   * Sözleşme sil (yalnızca tamamlanmamış)
   * Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. Arşivlenmiş sözleşme de silinemez → 409 &#x60;DEMAND_ARCHIVED&#x60;; önce &#x60;POST /api/v1/demands/{id}/unarchive&#x60; ile arşivden çıkarın. 
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
   * Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla atılan dijital imza, 5070 sayılı Elektronik İmza Kanunu m.3 anlamında elektronik imzadır; güvenli elektronik imza değildir ve AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Telefon doğrulaması, T.C. kimlik numarası kontrolü veya biyometrik adımlar gibi ek doğrulamalar imzanın delil değerini güçlendirir; imzayı gelişmiş ya da güvenli elektronik imzaya dönüştürmez. Güvenli elektronik imza gerekiyorsa bu akış yerine nitelikli elektronik sertifika ile imzalama akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). Kurum çalışma alanında sözleşme düzenleme ile aynı rol kuralı geçerlidir: OWNER ve ADMIN kurumun tüm sözleşmeleri için, MEMBER yalnız kendi oluşturduğu sözleşmeler için oturum alır. Erişim yoksa 404 döner. 
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
   * Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla atılan dijital imza, 5070 sayılı Elektronik İmza Kanunu m.3 anlamında elektronik imzadır; güvenli elektronik imza değildir ve AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Telefon doğrulaması, T.C. kimlik numarası kontrolü veya biyometrik adımlar gibi ek doğrulamalar imzanın delil değerini güçlendirir; imzayı gelişmiş ya da güvenli elektronik imzaya dönüştürmez. Güvenli elektronik imza gerekiyorsa bu akış yerine nitelikli elektronik sertifika ile imzalama akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). Kurum çalışma alanında sözleşme düzenleme ile aynı rol kuralı geçerlidir: OWNER ve ADMIN kurumun tüm sözleşmeleri için, MEMBER yalnız kendi oluşturduğu sözleşmeler için oturum alır. Erişim yoksa 404 döner. 
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
   * Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla atılan dijital imza, 5070 sayılı Elektronik İmza Kanunu m.3 anlamında elektronik imzadır; güvenli elektronik imza değildir ve AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Telefon doğrulaması, T.C. kimlik numarası kontrolü veya biyometrik adımlar gibi ek doğrulamalar imzanın delil değerini güçlendirir; imzayı gelişmiş ya da güvenli elektronik imzaya dönüştürmez. Güvenli elektronik imza gerekiyorsa bu akış yerine nitelikli elektronik sertifika ile imzalama akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). Kurum çalışma alanında sözleşme düzenleme ile aynı rol kuralı geçerlidir: OWNER ve ADMIN kurumun tüm sözleşmeleri için, MEMBER yalnız kendi oluşturduğu sözleşmeler için oturum alır. Erişim yoksa 404 döner. 
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
   * Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla atılan dijital imza, 5070 sayılı Elektronik İmza Kanunu m.3 anlamında elektronik imzadır; güvenli elektronik imza değildir ve AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Telefon doğrulaması, T.C. kimlik numarası kontrolü veya biyometrik adımlar gibi ek doğrulamalar imzanın delil değerini güçlendirir; imzayı gelişmiş ya da güvenli elektronik imzaya dönüştürmez. Güvenli elektronik imza gerekiyorsa bu akış yerine nitelikli elektronik sertifika ile imzalama akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). Kurum çalışma alanında sözleşme düzenleme ile aynı rol kuralı geçerlidir: OWNER ve ADMIN kurumun tüm sözleşmeleri için, MEMBER yalnız kendi oluşturduğu sözleşmeler için oturum alır. Erişim yoksa 404 döner. 
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
   * Kurum çalışma alanında erişim, sözleşme listesi ile aynı rol kuralını izler: OWNER ve ADMIN kurumun tüm sözleşmelerini, MEMBER yalnız kendi oluşturduğu sözleşmeleri görür. Erişim yoksa 404 döner. 
   * @param id  (required)
   * @return ApiV1DemandsIdGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdGet200Response apiV1DemandsIdGet(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdGet(id, null);
  }

  /**
   * Sözleşme durumu + imza ilerlemesi
   * Kurum çalışma alanında erişim, sözleşme listesi ile aynı rol kuralını izler: OWNER ve ADMIN kurumun tüm sözleşmelerini, MEMBER yalnız kendi oluşturduğu sözleşmeleri görür. Erişim yoksa 404 döner. 
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
   * Kurum çalışma alanında erişim, sözleşme listesi ile aynı rol kuralını izler: OWNER ve ADMIN kurumun tüm sözleşmelerini, MEMBER yalnız kendi oluşturduğu sözleşmeleri görür. Erişim yoksa 404 döner. 
   * @param id  (required)
   * @return ApiResponse&lt;ApiV1DemandsIdGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdGet200Response> apiV1DemandsIdGetWithHttpInfo(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdGetWithHttpInfo(id, null);
  }

  /**
   * Sözleşme durumu + imza ilerlemesi
   * Kurum çalışma alanında erişim, sözleşme listesi ile aynı rol kuralını izler: OWNER ve ADMIN kurumun tüm sözleşmelerini, MEMBER yalnız kendi oluşturduğu sözleşmeleri görür. Erişim yoksa 404 döner. 
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
   * Kaşe alanını doldur (kısmi güncelleme)
   * Sözleşmedeki var olan TEK bir kaşe alanını yapılandırılmış veriyle doldurur. &#x60;POST /api/v1/demands/{id}/items&#x60; tüm alanları yeniden yazar; bu uç ise yalnız belirtilen kaşeye dokunur, diğer alanlar değişmez.  Kaşe alanının kimliği (&#x60;itemId&#x60;) &#x60;GET /api/v1/demands/{id}&#x60; yanıtındaki &#x60;stamp_items[].item_id&#x60; alanından alınır.  ### Kısmi güncelleme kuralları  - &#x60;stamp_data&#x60; içinde gönderilen alan yazılır (baştaki/sondaki boşluk kırpılır). - &#x60;null&#x60; veya boş string gönderilen alan kaldırılır. - Gönderilmeyen alan olduğu gibi korunur. - Bilinmeyen alan veya string olmayan değer &#x60;400 INVALID_STAMP_DATA&#x60; döner.  ### Kaşe kaynağına göre davranış  - &#x60;FILLER_PROVIDES&#x60;: veri imzalayanın kaşesine önceden doldurulur;   zorunlu kaşe bu veriyle karşılanmış sayılır. İmzalayan imza   sayfasında bu değeri görür ve imzalamadan önce düzenleyebilir;   düzenlerse onun verisi kaydedilir. Kayıtta verinin gönderen   tarafından mı imzalayan tarafından mı girildiği ayrıca tutulur. - &#x60;INLINE&#x60;: kaşe verisi güncellenir. - &#x60;FROM_SAVED&#x60;: veri artık kayıtlı kaşeyle aynı olmadığı için kaşe   &#x60;INLINE&#x60; olur. Görsel kaşe (logo) varsa korunur ve belgede logo gösterilir.  ### Durum kontrolü  Yalnız &#x60;DRAFT&#x60; ve &#x60;PENDING&#x60; sözleşmeler güncellenebilir. Taraflardan biri imzaladıysa veya bir belge için karar verdiyse kaşe değiştirilemez.  ### Erişim  Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerinde, MEMBER yalnız kendi oluşturduğu sözleşmelerde kaşe doldurabilir. Erişim yoksa &#x60;404 DEMAND_NOT_FOUND&#x60; döner.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X PATCH https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items/$STAMP_ITEM_ID/stamp \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;stamp_data\&quot;: {       \&quot;companyName\&quot;: \&quot;Örnek Ltd.\&quot;,       \&quot;taxNumber\&quot;: \&quot;1234567890\&quot;,       \&quot;taxOffice\&quot;: \&quot;Kadıköy\&quot;,       \&quot;companyPhone\&quot;: \&quot;+905551112233\&quot;     }   }&#39; &#x60;&#x60;&#x60; 
   * @param id  (required)
   * @param itemId Kaşe alanının kimliği (&#x60;stamp_items[].item_id&#x60;) (required)
   * @param patchStampItemRequest  (required)
   * @return PatchStampItemResponse
   * @throws ApiException if fails to make API call
   */
  public PatchStampItemResponse apiV1DemandsIdItemsItemIdStampPatch(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull Integer itemId, @javax.annotation.Nonnull PatchStampItemRequest patchStampItemRequest) throws ApiException {
    return apiV1DemandsIdItemsItemIdStampPatch(id, itemId, patchStampItemRequest, null);
  }

  /**
   * Kaşe alanını doldur (kısmi güncelleme)
   * Sözleşmedeki var olan TEK bir kaşe alanını yapılandırılmış veriyle doldurur. &#x60;POST /api/v1/demands/{id}/items&#x60; tüm alanları yeniden yazar; bu uç ise yalnız belirtilen kaşeye dokunur, diğer alanlar değişmez.  Kaşe alanının kimliği (&#x60;itemId&#x60;) &#x60;GET /api/v1/demands/{id}&#x60; yanıtındaki &#x60;stamp_items[].item_id&#x60; alanından alınır.  ### Kısmi güncelleme kuralları  - &#x60;stamp_data&#x60; içinde gönderilen alan yazılır (baştaki/sondaki boşluk kırpılır). - &#x60;null&#x60; veya boş string gönderilen alan kaldırılır. - Gönderilmeyen alan olduğu gibi korunur. - Bilinmeyen alan veya string olmayan değer &#x60;400 INVALID_STAMP_DATA&#x60; döner.  ### Kaşe kaynağına göre davranış  - &#x60;FILLER_PROVIDES&#x60;: veri imzalayanın kaşesine önceden doldurulur;   zorunlu kaşe bu veriyle karşılanmış sayılır. İmzalayan imza   sayfasında bu değeri görür ve imzalamadan önce düzenleyebilir;   düzenlerse onun verisi kaydedilir. Kayıtta verinin gönderen   tarafından mı imzalayan tarafından mı girildiği ayrıca tutulur. - &#x60;INLINE&#x60;: kaşe verisi güncellenir. - &#x60;FROM_SAVED&#x60;: veri artık kayıtlı kaşeyle aynı olmadığı için kaşe   &#x60;INLINE&#x60; olur. Görsel kaşe (logo) varsa korunur ve belgede logo gösterilir.  ### Durum kontrolü  Yalnız &#x60;DRAFT&#x60; ve &#x60;PENDING&#x60; sözleşmeler güncellenebilir. Taraflardan biri imzaladıysa veya bir belge için karar verdiyse kaşe değiştirilemez.  ### Erişim  Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerinde, MEMBER yalnız kendi oluşturduğu sözleşmelerde kaşe doldurabilir. Erişim yoksa &#x60;404 DEMAND_NOT_FOUND&#x60; döner.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X PATCH https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items/$STAMP_ITEM_ID/stamp \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;stamp_data\&quot;: {       \&quot;companyName\&quot;: \&quot;Örnek Ltd.\&quot;,       \&quot;taxNumber\&quot;: \&quot;1234567890\&quot;,       \&quot;taxOffice\&quot;: \&quot;Kadıköy\&quot;,       \&quot;companyPhone\&quot;: \&quot;+905551112233\&quot;     }   }&#39; &#x60;&#x60;&#x60; 
   * @param id  (required)
   * @param itemId Kaşe alanının kimliği (&#x60;stamp_items[].item_id&#x60;) (required)
   * @param patchStampItemRequest  (required)
   * @param headers Optional headers to include in the request
   * @return PatchStampItemResponse
   * @throws ApiException if fails to make API call
   */
  public PatchStampItemResponse apiV1DemandsIdItemsItemIdStampPatch(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull Integer itemId, @javax.annotation.Nonnull PatchStampItemRequest patchStampItemRequest, Map<String, String> headers) throws ApiException {
    ApiResponse<PatchStampItemResponse> localVarResponse = apiV1DemandsIdItemsItemIdStampPatchWithHttpInfo(id, itemId, patchStampItemRequest, headers);
    return localVarResponse.getData();
  }

  /**
   * Kaşe alanını doldur (kısmi güncelleme)
   * Sözleşmedeki var olan TEK bir kaşe alanını yapılandırılmış veriyle doldurur. &#x60;POST /api/v1/demands/{id}/items&#x60; tüm alanları yeniden yazar; bu uç ise yalnız belirtilen kaşeye dokunur, diğer alanlar değişmez.  Kaşe alanının kimliği (&#x60;itemId&#x60;) &#x60;GET /api/v1/demands/{id}&#x60; yanıtındaki &#x60;stamp_items[].item_id&#x60; alanından alınır.  ### Kısmi güncelleme kuralları  - &#x60;stamp_data&#x60; içinde gönderilen alan yazılır (baştaki/sondaki boşluk kırpılır). - &#x60;null&#x60; veya boş string gönderilen alan kaldırılır. - Gönderilmeyen alan olduğu gibi korunur. - Bilinmeyen alan veya string olmayan değer &#x60;400 INVALID_STAMP_DATA&#x60; döner.  ### Kaşe kaynağına göre davranış  - &#x60;FILLER_PROVIDES&#x60;: veri imzalayanın kaşesine önceden doldurulur;   zorunlu kaşe bu veriyle karşılanmış sayılır. İmzalayan imza   sayfasında bu değeri görür ve imzalamadan önce düzenleyebilir;   düzenlerse onun verisi kaydedilir. Kayıtta verinin gönderen   tarafından mı imzalayan tarafından mı girildiği ayrıca tutulur. - &#x60;INLINE&#x60;: kaşe verisi güncellenir. - &#x60;FROM_SAVED&#x60;: veri artık kayıtlı kaşeyle aynı olmadığı için kaşe   &#x60;INLINE&#x60; olur. Görsel kaşe (logo) varsa korunur ve belgede logo gösterilir.  ### Durum kontrolü  Yalnız &#x60;DRAFT&#x60; ve &#x60;PENDING&#x60; sözleşmeler güncellenebilir. Taraflardan biri imzaladıysa veya bir belge için karar verdiyse kaşe değiştirilemez.  ### Erişim  Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerinde, MEMBER yalnız kendi oluşturduğu sözleşmelerde kaşe doldurabilir. Erişim yoksa &#x60;404 DEMAND_NOT_FOUND&#x60; döner.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X PATCH https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items/$STAMP_ITEM_ID/stamp \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;stamp_data\&quot;: {       \&quot;companyName\&quot;: \&quot;Örnek Ltd.\&quot;,       \&quot;taxNumber\&quot;: \&quot;1234567890\&quot;,       \&quot;taxOffice\&quot;: \&quot;Kadıköy\&quot;,       \&quot;companyPhone\&quot;: \&quot;+905551112233\&quot;     }   }&#39; &#x60;&#x60;&#x60; 
   * @param id  (required)
   * @param itemId Kaşe alanının kimliği (&#x60;stamp_items[].item_id&#x60;) (required)
   * @param patchStampItemRequest  (required)
   * @return ApiResponse&lt;PatchStampItemResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PatchStampItemResponse> apiV1DemandsIdItemsItemIdStampPatchWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull Integer itemId, @javax.annotation.Nonnull PatchStampItemRequest patchStampItemRequest) throws ApiException {
    return apiV1DemandsIdItemsItemIdStampPatchWithHttpInfo(id, itemId, patchStampItemRequest, null);
  }

  /**
   * Kaşe alanını doldur (kısmi güncelleme)
   * Sözleşmedeki var olan TEK bir kaşe alanını yapılandırılmış veriyle doldurur. &#x60;POST /api/v1/demands/{id}/items&#x60; tüm alanları yeniden yazar; bu uç ise yalnız belirtilen kaşeye dokunur, diğer alanlar değişmez.  Kaşe alanının kimliği (&#x60;itemId&#x60;) &#x60;GET /api/v1/demands/{id}&#x60; yanıtındaki &#x60;stamp_items[].item_id&#x60; alanından alınır.  ### Kısmi güncelleme kuralları  - &#x60;stamp_data&#x60; içinde gönderilen alan yazılır (baştaki/sondaki boşluk kırpılır). - &#x60;null&#x60; veya boş string gönderilen alan kaldırılır. - Gönderilmeyen alan olduğu gibi korunur. - Bilinmeyen alan veya string olmayan değer &#x60;400 INVALID_STAMP_DATA&#x60; döner.  ### Kaşe kaynağına göre davranış  - &#x60;FILLER_PROVIDES&#x60;: veri imzalayanın kaşesine önceden doldurulur;   zorunlu kaşe bu veriyle karşılanmış sayılır. İmzalayan imza   sayfasında bu değeri görür ve imzalamadan önce düzenleyebilir;   düzenlerse onun verisi kaydedilir. Kayıtta verinin gönderen   tarafından mı imzalayan tarafından mı girildiği ayrıca tutulur. - &#x60;INLINE&#x60;: kaşe verisi güncellenir. - &#x60;FROM_SAVED&#x60;: veri artık kayıtlı kaşeyle aynı olmadığı için kaşe   &#x60;INLINE&#x60; olur. Görsel kaşe (logo) varsa korunur ve belgede logo gösterilir.  ### Durum kontrolü  Yalnız &#x60;DRAFT&#x60; ve &#x60;PENDING&#x60; sözleşmeler güncellenebilir. Taraflardan biri imzaladıysa veya bir belge için karar verdiyse kaşe değiştirilemez.  ### Erişim  Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerinde, MEMBER yalnız kendi oluşturduğu sözleşmelerde kaşe doldurabilir. Erişim yoksa &#x60;404 DEMAND_NOT_FOUND&#x60; döner.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X PATCH https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items/$STAMP_ITEM_ID/stamp \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;stamp_data\&quot;: {       \&quot;companyName\&quot;: \&quot;Örnek Ltd.\&quot;,       \&quot;taxNumber\&quot;: \&quot;1234567890\&quot;,       \&quot;taxOffice\&quot;: \&quot;Kadıköy\&quot;,       \&quot;companyPhone\&quot;: \&quot;+905551112233\&quot;     }   }&#39; &#x60;&#x60;&#x60; 
   * @param id  (required)
   * @param itemId Kaşe alanının kimliği (&#x60;stamp_items[].item_id&#x60;) (required)
   * @param patchStampItemRequest  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;PatchStampItemResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PatchStampItemResponse> apiV1DemandsIdItemsItemIdStampPatchWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull Integer itemId, @javax.annotation.Nonnull PatchStampItemRequest patchStampItemRequest, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdItemsItemIdStampPatchRequestBuilder(id, itemId, patchStampItemRequest, headers);
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
          throw getApiException("apiV1DemandsIdItemsItemIdStampPatch", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<PatchStampItemResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        PatchStampItemResponse responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PatchStampItemResponse>() {});
        

        return new ApiResponse<PatchStampItemResponse>(
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

  private HttpRequest.Builder apiV1DemandsIdItemsItemIdStampPatchRequestBuilder(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull Integer itemId, @javax.annotation.Nonnull PatchStampItemRequest patchStampItemRequest, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdItemsItemIdStampPatch");
    }
    // verify the required parameter 'itemId' is set
    if (itemId == null) {
      throw new ApiException(400, "Missing the required parameter 'itemId' when calling apiV1DemandsIdItemsItemIdStampPatch");
    }
    // verify the required parameter 'patchStampItemRequest' is set
    if (patchStampItemRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'patchStampItemRequest' when calling apiV1DemandsIdItemsItemIdStampPatch");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}/items/{itemId}/stamp"
        .replace("{id}", ApiClient.urlEncode(id.toString()))
        .replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(patchStampItemRequest);
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
   * Sözleşmeye alan yerleştir (replace)
   * Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |-------------|---------------------|-----------------| | &#x60;signature&#x60; | ✅ | (yok) veya &#x60;{ requireFirstSignatureOtp: true }&#x60; | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | Yalnız &#x60;source: FILLER_PROVIDES&#x60; ise (zorunlu kaşede şart) | &#x60;{ source: \&quot;INLINE\&quot;, stampData: { companyName: \&quot;Örnek Ltd.\&quot;, taxNumber: \&quot;1234567890\&quot; } }&#x60; |  ### Kaşe (&#x60;stamp&#x60;) alanı  - &#x60;config.source&#x60;: &#x60;INLINE&#x60; (varsayılan, kaşe verisi gönderenden gelir),   &#x60;FROM_SAVED&#x60; (kayıtlı kaşeden kopya) veya &#x60;FILLER_PROVIDES&#x60; (kaşeyi   &#x60;party_id&#x60; ile belirtilen imzalayan doldurur). - &#x60;config.stampData&#x60;: yapılandırılmış kaşe verisi. İzinli alanlar:   &#x60;companyName&#x60;, &#x60;personalName&#x60;, &#x60;address&#x60;, &#x60;companyAddress&#x60;,   &#x60;personalAddress&#x60;, &#x60;taxNumber&#x60;, &#x60;taxOffice&#x60;, &#x60;idNumber&#x60;, &#x60;phone&#x60;,   &#x60;companyPhone&#x60;, &#x60;personalPhone&#x60;, &#x60;email&#x60; (hepsi string). Bilinmeyen alan   veya string olmayan değer &#x60;400 INVALID_STAMP_DATA&#x60; döner. Eski   dokümandaki data URL string biçimi geriye dönük uyum için hâlâ kabul   edilir, ancak belgede kaşe olarak çizilmez; yapılandırılmış nesneyi kullanın. - &#x60;config.isRequired: true&#x60; yalnız &#x60;FILLER_PROVIDES&#x60; kaşede anlamlıdır:   imzalayan kaşeyi doldurmadan imzalayamaz. Zorunlu kaşenin &#x60;party_id&#x60;&#39;si   olmak zorundadır; çok belgeli zarfta taraf, kaşenin bulunduğu belgeye   atanmış olmalıdır. - Var olan tek bir kaşeyi diğer alanlara dokunmadan doldurmak için bu   uç yerine &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; kullanın.  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner). Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerine, MEMBER yalnız kendi oluşturduğu sözleşmelere alan yerleştirebilir. Erişim yoksa 404 döner.  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
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
   * Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |-------------|---------------------|-----------------| | &#x60;signature&#x60; | ✅ | (yok) veya &#x60;{ requireFirstSignatureOtp: true }&#x60; | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | Yalnız &#x60;source: FILLER_PROVIDES&#x60; ise (zorunlu kaşede şart) | &#x60;{ source: \&quot;INLINE\&quot;, stampData: { companyName: \&quot;Örnek Ltd.\&quot;, taxNumber: \&quot;1234567890\&quot; } }&#x60; |  ### Kaşe (&#x60;stamp&#x60;) alanı  - &#x60;config.source&#x60;: &#x60;INLINE&#x60; (varsayılan, kaşe verisi gönderenden gelir),   &#x60;FROM_SAVED&#x60; (kayıtlı kaşeden kopya) veya &#x60;FILLER_PROVIDES&#x60; (kaşeyi   &#x60;party_id&#x60; ile belirtilen imzalayan doldurur). - &#x60;config.stampData&#x60;: yapılandırılmış kaşe verisi. İzinli alanlar:   &#x60;companyName&#x60;, &#x60;personalName&#x60;, &#x60;address&#x60;, &#x60;companyAddress&#x60;,   &#x60;personalAddress&#x60;, &#x60;taxNumber&#x60;, &#x60;taxOffice&#x60;, &#x60;idNumber&#x60;, &#x60;phone&#x60;,   &#x60;companyPhone&#x60;, &#x60;personalPhone&#x60;, &#x60;email&#x60; (hepsi string). Bilinmeyen alan   veya string olmayan değer &#x60;400 INVALID_STAMP_DATA&#x60; döner. Eski   dokümandaki data URL string biçimi geriye dönük uyum için hâlâ kabul   edilir, ancak belgede kaşe olarak çizilmez; yapılandırılmış nesneyi kullanın. - &#x60;config.isRequired: true&#x60; yalnız &#x60;FILLER_PROVIDES&#x60; kaşede anlamlıdır:   imzalayan kaşeyi doldurmadan imzalayamaz. Zorunlu kaşenin &#x60;party_id&#x60;&#39;si   olmak zorundadır; çok belgeli zarfta taraf, kaşenin bulunduğu belgeye   atanmış olmalıdır. - Var olan tek bir kaşeyi diğer alanlara dokunmadan doldurmak için bu   uç yerine &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; kullanın.  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner). Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerine, MEMBER yalnız kendi oluşturduğu sözleşmelere alan yerleştirebilir. Erişim yoksa 404 döner.  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
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
   * Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |-------------|---------------------|-----------------| | &#x60;signature&#x60; | ✅ | (yok) veya &#x60;{ requireFirstSignatureOtp: true }&#x60; | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | Yalnız &#x60;source: FILLER_PROVIDES&#x60; ise (zorunlu kaşede şart) | &#x60;{ source: \&quot;INLINE\&quot;, stampData: { companyName: \&quot;Örnek Ltd.\&quot;, taxNumber: \&quot;1234567890\&quot; } }&#x60; |  ### Kaşe (&#x60;stamp&#x60;) alanı  - &#x60;config.source&#x60;: &#x60;INLINE&#x60; (varsayılan, kaşe verisi gönderenden gelir),   &#x60;FROM_SAVED&#x60; (kayıtlı kaşeden kopya) veya &#x60;FILLER_PROVIDES&#x60; (kaşeyi   &#x60;party_id&#x60; ile belirtilen imzalayan doldurur). - &#x60;config.stampData&#x60;: yapılandırılmış kaşe verisi. İzinli alanlar:   &#x60;companyName&#x60;, &#x60;personalName&#x60;, &#x60;address&#x60;, &#x60;companyAddress&#x60;,   &#x60;personalAddress&#x60;, &#x60;taxNumber&#x60;, &#x60;taxOffice&#x60;, &#x60;idNumber&#x60;, &#x60;phone&#x60;,   &#x60;companyPhone&#x60;, &#x60;personalPhone&#x60;, &#x60;email&#x60; (hepsi string). Bilinmeyen alan   veya string olmayan değer &#x60;400 INVALID_STAMP_DATA&#x60; döner. Eski   dokümandaki data URL string biçimi geriye dönük uyum için hâlâ kabul   edilir, ancak belgede kaşe olarak çizilmez; yapılandırılmış nesneyi kullanın. - &#x60;config.isRequired: true&#x60; yalnız &#x60;FILLER_PROVIDES&#x60; kaşede anlamlıdır:   imzalayan kaşeyi doldurmadan imzalayamaz. Zorunlu kaşenin &#x60;party_id&#x60;&#39;si   olmak zorundadır; çok belgeli zarfta taraf, kaşenin bulunduğu belgeye   atanmış olmalıdır. - Var olan tek bir kaşeyi diğer alanlara dokunmadan doldurmak için bu   uç yerine &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; kullanın.  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner). Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerine, MEMBER yalnız kendi oluşturduğu sözleşmelere alan yerleştirebilir. Erişim yoksa 404 döner.  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
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
   * Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |-------------|---------------------|-----------------| | &#x60;signature&#x60; | ✅ | (yok) veya &#x60;{ requireFirstSignatureOtp: true }&#x60; | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | Yalnız &#x60;source: FILLER_PROVIDES&#x60; ise (zorunlu kaşede şart) | &#x60;{ source: \&quot;INLINE\&quot;, stampData: { companyName: \&quot;Örnek Ltd.\&quot;, taxNumber: \&quot;1234567890\&quot; } }&#x60; |  ### Kaşe (&#x60;stamp&#x60;) alanı  - &#x60;config.source&#x60;: &#x60;INLINE&#x60; (varsayılan, kaşe verisi gönderenden gelir),   &#x60;FROM_SAVED&#x60; (kayıtlı kaşeden kopya) veya &#x60;FILLER_PROVIDES&#x60; (kaşeyi   &#x60;party_id&#x60; ile belirtilen imzalayan doldurur). - &#x60;config.stampData&#x60;: yapılandırılmış kaşe verisi. İzinli alanlar:   &#x60;companyName&#x60;, &#x60;personalName&#x60;, &#x60;address&#x60;, &#x60;companyAddress&#x60;,   &#x60;personalAddress&#x60;, &#x60;taxNumber&#x60;, &#x60;taxOffice&#x60;, &#x60;idNumber&#x60;, &#x60;phone&#x60;,   &#x60;companyPhone&#x60;, &#x60;personalPhone&#x60;, &#x60;email&#x60; (hepsi string). Bilinmeyen alan   veya string olmayan değer &#x60;400 INVALID_STAMP_DATA&#x60; döner. Eski   dokümandaki data URL string biçimi geriye dönük uyum için hâlâ kabul   edilir, ancak belgede kaşe olarak çizilmez; yapılandırılmış nesneyi kullanın. - &#x60;config.isRequired: true&#x60; yalnız &#x60;FILLER_PROVIDES&#x60; kaşede anlamlıdır:   imzalayan kaşeyi doldurmadan imzalayamaz. Zorunlu kaşenin &#x60;party_id&#x60;&#39;si   olmak zorundadır; çok belgeli zarfta taraf, kaşenin bulunduğu belgeye   atanmış olmalıdır. - Var olan tek bir kaşeyi diğer alanlara dokunmadan doldurmak için bu   uç yerine &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; kullanın.  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner). Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerine, MEMBER yalnız kendi oluşturduğu sözleşmelere alan yerleştirebilir. Erişim yoksa 404 döner.  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
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
   * Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa, onayını vermiş onaylayana, açık düzeltme talebi olan tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). Sıralı imzada onaylayanın onayı o adımı tamamlar; sıra bir sonraki tarafa geçer. 
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
   * Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa, onayını vermiş onaylayana, açık düzeltme talebi olan tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). Sıralı imzada onaylayanın onayı o adımı tamamlar; sıra bir sonraki tarafa geçer. 
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
   * Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa, onayını vermiş onaylayana, açık düzeltme talebi olan tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). Sıralı imzada onaylayanın onayı o adımı tamamlar; sıra bir sonraki tarafa geçer. 
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
   * Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa, onayını vermiş onaylayana, açık düzeltme talebi olan tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). Sıralı imzada onaylayanın onayı o adımı tamamlar; sıra bir sonraki tarafa geçer. 
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
   * Sözleşme süre/yenileme takibini güncelle
   * Kısmi güncelleme: yalnız gövdede gönderilen &#x60;ContractTermInput&#x60; anahtarları değiştirilir; bir anahtarı &#x60;null&#x60; göndermek o alanı temizler. Dashboard&#39;daki sözleşme detay sayfasıyla AYNI çekirdek kuralı uygular.  Bitişi etkileyen bir alan (&#x60;term_start_mode&#x60;, &#x60;term_start_date&#x60;, &#x60;term_duration_months&#x60;, &#x60;term_fixed_end_date&#x60;) kayıttakinden farklı bir DEĞERLE gönderilirse &#x60;term_end_date&#x60; bilinen başlangıçtan yeniden hesaplanır; aynı değerle gelen alan no-op&#39;tur (otomatik olarak ileri alınmış bitiş korunur). 
   * @param id  (required)
   * @param contractTermInput  (required)
   * @return ApiV1DemandsIdTermPatch200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdTermPatch200Response apiV1DemandsIdTermPatch(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ContractTermInput contractTermInput) throws ApiException {
    return apiV1DemandsIdTermPatch(id, contractTermInput, null);
  }

  /**
   * Sözleşme süre/yenileme takibini güncelle
   * Kısmi güncelleme: yalnız gövdede gönderilen &#x60;ContractTermInput&#x60; anahtarları değiştirilir; bir anahtarı &#x60;null&#x60; göndermek o alanı temizler. Dashboard&#39;daki sözleşme detay sayfasıyla AYNI çekirdek kuralı uygular.  Bitişi etkileyen bir alan (&#x60;term_start_mode&#x60;, &#x60;term_start_date&#x60;, &#x60;term_duration_months&#x60;, &#x60;term_fixed_end_date&#x60;) kayıttakinden farklı bir DEĞERLE gönderilirse &#x60;term_end_date&#x60; bilinen başlangıçtan yeniden hesaplanır; aynı değerle gelen alan no-op&#39;tur (otomatik olarak ileri alınmış bitiş korunur). 
   * @param id  (required)
   * @param contractTermInput  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsIdTermPatch200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdTermPatch200Response apiV1DemandsIdTermPatch(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ContractTermInput contractTermInput, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsIdTermPatch200Response> localVarResponse = apiV1DemandsIdTermPatchWithHttpInfo(id, contractTermInput, headers);
    return localVarResponse.getData();
  }

  /**
   * Sözleşme süre/yenileme takibini güncelle
   * Kısmi güncelleme: yalnız gövdede gönderilen &#x60;ContractTermInput&#x60; anahtarları değiştirilir; bir anahtarı &#x60;null&#x60; göndermek o alanı temizler. Dashboard&#39;daki sözleşme detay sayfasıyla AYNI çekirdek kuralı uygular.  Bitişi etkileyen bir alan (&#x60;term_start_mode&#x60;, &#x60;term_start_date&#x60;, &#x60;term_duration_months&#x60;, &#x60;term_fixed_end_date&#x60;) kayıttakinden farklı bir DEĞERLE gönderilirse &#x60;term_end_date&#x60; bilinen başlangıçtan yeniden hesaplanır; aynı değerle gelen alan no-op&#39;tur (otomatik olarak ileri alınmış bitiş korunur). 
   * @param id  (required)
   * @param contractTermInput  (required)
   * @return ApiResponse&lt;ApiV1DemandsIdTermPatch200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdTermPatch200Response> apiV1DemandsIdTermPatchWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ContractTermInput contractTermInput) throws ApiException {
    return apiV1DemandsIdTermPatchWithHttpInfo(id, contractTermInput, null);
  }

  /**
   * Sözleşme süre/yenileme takibini güncelle
   * Kısmi güncelleme: yalnız gövdede gönderilen &#x60;ContractTermInput&#x60; anahtarları değiştirilir; bir anahtarı &#x60;null&#x60; göndermek o alanı temizler. Dashboard&#39;daki sözleşme detay sayfasıyla AYNI çekirdek kuralı uygular.  Bitişi etkileyen bir alan (&#x60;term_start_mode&#x60;, &#x60;term_start_date&#x60;, &#x60;term_duration_months&#x60;, &#x60;term_fixed_end_date&#x60;) kayıttakinden farklı bir DEĞERLE gönderilirse &#x60;term_end_date&#x60; bilinen başlangıçtan yeniden hesaplanır; aynı değerle gelen alan no-op&#39;tur (otomatik olarak ileri alınmış bitiş korunur). 
   * @param id  (required)
   * @param contractTermInput  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsIdTermPatch200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdTermPatch200Response> apiV1DemandsIdTermPatchWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ContractTermInput contractTermInput, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdTermPatchRequestBuilder(id, contractTermInput, headers);
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
          throw getApiException("apiV1DemandsIdTermPatch", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsIdTermPatch200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsIdTermPatch200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsIdTermPatch200Response>() {});
        

        return new ApiResponse<ApiV1DemandsIdTermPatch200Response>(
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

  private HttpRequest.Builder apiV1DemandsIdTermPatchRequestBuilder(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull ContractTermInput contractTermInput, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdTermPatch");
    }
    // verify the required parameter 'contractTermInput' is set
    if (contractTermInput == null) {
      throw new ApiException(400, "Missing the required parameter 'contractTermInput' when calling apiV1DemandsIdTermPatch");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}/term"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(contractTermInput);
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
   * İmza denetim izi (maskeli)
   * Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez.  &#x60;event_type&#x60; değerleri: &#x60;CREATED&#x60;, &#x60;SENT&#x60;, &#x60;VIEWED&#x60;, &#x60;FIELDS_FILLED&#x60;, &#x60;COMMENT_ADDED&#x60;, &#x60;SIGNED&#x60;, &#x60;APPROVED&#x60;, &#x60;REJECTED&#x60;, &#x60;TIMESTAMPED&#x60;, &#x60;COMPLETED&#x60;, &#x60;OTP_SENT&#x60; (SMS doğrulama kodu gönderildi), &#x60;OTP_VERIFIED&#x60; (SMS doğrulama kodu doğrulandı), &#x60;OTP_LOCKED&#x60; (deneme sınırı doldu), &#x60;MOBILE_SIGNATURE_CAPTURED&#x60; (imza QR kod ile telefonda çizildi; bu olayın &#x60;ip_masked&#x60; ve &#x60;device_label&#x60; alanları telefona aittir), &#x60;REAPPROVAL_REQUIRED&#x60; (gönderen açık bir değişiklik talebi sırasında sözleşme içeriğini güncelledi, önceki onaylar sıfırlandı ve onaylayanların yeniden onay vermesi gerekiyor). Liste ileride genişleyebilir; tanımadığınız değeri yok saymanız önerilir. 
   * @param id  (required)
   * @return ApiV1DemandsIdTimelineGet200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdTimelineGet200Response apiV1DemandsIdTimelineGet(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdTimelineGet(id, null);
  }

  /**
   * İmza denetim izi (maskeli)
   * Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez.  &#x60;event_type&#x60; değerleri: &#x60;CREATED&#x60;, &#x60;SENT&#x60;, &#x60;VIEWED&#x60;, &#x60;FIELDS_FILLED&#x60;, &#x60;COMMENT_ADDED&#x60;, &#x60;SIGNED&#x60;, &#x60;APPROVED&#x60;, &#x60;REJECTED&#x60;, &#x60;TIMESTAMPED&#x60;, &#x60;COMPLETED&#x60;, &#x60;OTP_SENT&#x60; (SMS doğrulama kodu gönderildi), &#x60;OTP_VERIFIED&#x60; (SMS doğrulama kodu doğrulandı), &#x60;OTP_LOCKED&#x60; (deneme sınırı doldu), &#x60;MOBILE_SIGNATURE_CAPTURED&#x60; (imza QR kod ile telefonda çizildi; bu olayın &#x60;ip_masked&#x60; ve &#x60;device_label&#x60; alanları telefona aittir), &#x60;REAPPROVAL_REQUIRED&#x60; (gönderen açık bir değişiklik talebi sırasında sözleşme içeriğini güncelledi, önceki onaylar sıfırlandı ve onaylayanların yeniden onay vermesi gerekiyor). Liste ileride genişleyebilir; tanımadığınız değeri yok saymanız önerilir. 
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
   * Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez.  &#x60;event_type&#x60; değerleri: &#x60;CREATED&#x60;, &#x60;SENT&#x60;, &#x60;VIEWED&#x60;, &#x60;FIELDS_FILLED&#x60;, &#x60;COMMENT_ADDED&#x60;, &#x60;SIGNED&#x60;, &#x60;APPROVED&#x60;, &#x60;REJECTED&#x60;, &#x60;TIMESTAMPED&#x60;, &#x60;COMPLETED&#x60;, &#x60;OTP_SENT&#x60; (SMS doğrulama kodu gönderildi), &#x60;OTP_VERIFIED&#x60; (SMS doğrulama kodu doğrulandı), &#x60;OTP_LOCKED&#x60; (deneme sınırı doldu), &#x60;MOBILE_SIGNATURE_CAPTURED&#x60; (imza QR kod ile telefonda çizildi; bu olayın &#x60;ip_masked&#x60; ve &#x60;device_label&#x60; alanları telefona aittir), &#x60;REAPPROVAL_REQUIRED&#x60; (gönderen açık bir değişiklik talebi sırasında sözleşme içeriğini güncelledi, önceki onaylar sıfırlandı ve onaylayanların yeniden onay vermesi gerekiyor). Liste ileride genişleyebilir; tanımadığınız değeri yok saymanız önerilir. 
   * @param id  (required)
   * @return ApiResponse&lt;ApiV1DemandsIdTimelineGet200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdTimelineGet200Response> apiV1DemandsIdTimelineGetWithHttpInfo(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdTimelineGetWithHttpInfo(id, null);
  }

  /**
   * İmza denetim izi (maskeli)
   * Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez.  &#x60;event_type&#x60; değerleri: &#x60;CREATED&#x60;, &#x60;SENT&#x60;, &#x60;VIEWED&#x60;, &#x60;FIELDS_FILLED&#x60;, &#x60;COMMENT_ADDED&#x60;, &#x60;SIGNED&#x60;, &#x60;APPROVED&#x60;, &#x60;REJECTED&#x60;, &#x60;TIMESTAMPED&#x60;, &#x60;COMPLETED&#x60;, &#x60;OTP_SENT&#x60; (SMS doğrulama kodu gönderildi), &#x60;OTP_VERIFIED&#x60; (SMS doğrulama kodu doğrulandı), &#x60;OTP_LOCKED&#x60; (deneme sınırı doldu), &#x60;MOBILE_SIGNATURE_CAPTURED&#x60; (imza QR kod ile telefonda çizildi; bu olayın &#x60;ip_masked&#x60; ve &#x60;device_label&#x60; alanları telefona aittir), &#x60;REAPPROVAL_REQUIRED&#x60; (gönderen açık bir değişiklik talebi sırasında sözleşme içeriğini güncelledi, önceki onaylar sıfırlandı ve onaylayanların yeniden onay vermesi gerekiyor). Liste ileride genişleyebilir; tanımadığınız değeri yok saymanız önerilir. 
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
   * Sözleşmeyi arşivden çıkar
   * &#x60;archived_at&#x60;&#39;i temizler; sözleşme yeniden mutasyona açılır. Zaten arşivsizse idempotent (200, &#x60;archived_at: null&#x60;). 
   * @param id  (required)
   * @return ApiV1DemandsIdUnarchivePost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdUnarchivePost200Response apiV1DemandsIdUnarchivePost(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdUnarchivePost(id, null);
  }

  /**
   * Sözleşmeyi arşivden çıkar
   * &#x60;archived_at&#x60;&#39;i temizler; sözleşme yeniden mutasyona açılır. Zaten arşivsizse idempotent (200, &#x60;archived_at: null&#x60;). 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsIdUnarchivePost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsIdUnarchivePost200Response apiV1DemandsIdUnarchivePost(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsIdUnarchivePost200Response> localVarResponse = apiV1DemandsIdUnarchivePostWithHttpInfo(id, headers);
    return localVarResponse.getData();
  }

  /**
   * Sözleşmeyi arşivden çıkar
   * &#x60;archived_at&#x60;&#39;i temizler; sözleşme yeniden mutasyona açılır. Zaten arşivsizse idempotent (200, &#x60;archived_at: null&#x60;). 
   * @param id  (required)
   * @return ApiResponse&lt;ApiV1DemandsIdUnarchivePost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdUnarchivePost200Response> apiV1DemandsIdUnarchivePostWithHttpInfo(@javax.annotation.Nonnull UUID id) throws ApiException {
    return apiV1DemandsIdUnarchivePostWithHttpInfo(id, null);
  }

  /**
   * Sözleşmeyi arşivden çıkar
   * &#x60;archived_at&#x60;&#39;i temizler; sözleşme yeniden mutasyona açılır. Zaten arşivsizse idempotent (200, &#x60;archived_at: null&#x60;). 
   * @param id  (required)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsIdUnarchivePost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsIdUnarchivePost200Response> apiV1DemandsIdUnarchivePostWithHttpInfo(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsIdUnarchivePostRequestBuilder(id, headers);
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
          throw getApiException("apiV1DemandsIdUnarchivePost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1DemandsIdUnarchivePost200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1DemandsIdUnarchivePost200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1DemandsIdUnarchivePost200Response>() {});
        

        return new ApiResponse<ApiV1DemandsIdUnarchivePost200Response>(
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

  private HttpRequest.Builder apiV1DemandsIdUnarchivePostRequestBuilder(@javax.annotation.Nonnull UUID id, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1DemandsIdUnarchivePost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands/{id}/unarchive"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

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
   * Sözleşme oluştur (şablondan)
   * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa oluşturma anında yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez. Ayrıntı: açıklamadaki \&quot;Onaylayan (onay adımı)\&quot; bölümü.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı  **Kredi (çok belgeli zarf):** Şablon çok-belgeli bir zarf tanımlıyorsa (her belge farklı imzacı(lar)a atanabilir), imzacı başına kredi o imzacıya ATANMIŞ belge sayısına göre hesaplanır ve belge sayısına göre azalan birim fiyatlı bir indirim uygulanır: 1 belge %0, 2 belge %30, 3 ve üzeri %50 (indirim imzacı başına ayrı hesaplanır ve yukarı yuvarlanır). AB nitelikli zaman damgası (&#x60;eidas_timestamp&#x60;) seçiliyse aynı indirim zarftaki farklı belge sayısına göre toplam üzerinden uygulanır. Tek belgeli şablonlarda tutar eski formülle bayt-aynıdır.  **Atamasız imzacı:** &#x60;dispatch_notifications&#x60; &#x60;false&#x60; gönderilmediği sürece, eşlenen bir taraf şablonun hiçbir belgesine atanmamışsa sözleşme HİÇ oluşturulmaz (409 &#x60;PARTY_WITHOUT_DOCUMENTS&#x60;). Bu kontrolü atlamak için &#x60;dispatch_notifications: false&#x60; gönderip belge atamalarını &#x60;PUT .../documents/{docId}/assignments&#x60; ile düzelttikten sonra &#x60;POST .../dispatch&#x60; ile gönderin (o uç aynı kapıyı yeniden uygular). 
   * @param createDemandRequest  (required)
   * @param idempotencyKey Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)
   * @return ApiV1DemandsPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsPost201Response apiV1DemandsPost(@javax.annotation.Nonnull CreateDemandRequest createDemandRequest, @javax.annotation.Nullable String idempotencyKey) throws ApiException {
    return apiV1DemandsPost(createDemandRequest, idempotencyKey, null);
  }

  /**
   * Sözleşme oluştur (şablondan)
   * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa oluşturma anında yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez. Ayrıntı: açıklamadaki \&quot;Onaylayan (onay adımı)\&quot; bölümü.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı  **Kredi (çok belgeli zarf):** Şablon çok-belgeli bir zarf tanımlıyorsa (her belge farklı imzacı(lar)a atanabilir), imzacı başına kredi o imzacıya ATANMIŞ belge sayısına göre hesaplanır ve belge sayısına göre azalan birim fiyatlı bir indirim uygulanır: 1 belge %0, 2 belge %30, 3 ve üzeri %50 (indirim imzacı başına ayrı hesaplanır ve yukarı yuvarlanır). AB nitelikli zaman damgası (&#x60;eidas_timestamp&#x60;) seçiliyse aynı indirim zarftaki farklı belge sayısına göre toplam üzerinden uygulanır. Tek belgeli şablonlarda tutar eski formülle bayt-aynıdır.  **Atamasız imzacı:** &#x60;dispatch_notifications&#x60; &#x60;false&#x60; gönderilmediği sürece, eşlenen bir taraf şablonun hiçbir belgesine atanmamışsa sözleşme HİÇ oluşturulmaz (409 &#x60;PARTY_WITHOUT_DOCUMENTS&#x60;). Bu kontrolü atlamak için &#x60;dispatch_notifications: false&#x60; gönderip belge atamalarını &#x60;PUT .../documents/{docId}/assignments&#x60; ile düzelttikten sonra &#x60;POST .../dispatch&#x60; ile gönderin (o uç aynı kapıyı yeniden uygular). 
   * @param createDemandRequest  (required)
   * @param idempotencyKey Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsPost201Response apiV1DemandsPost(@javax.annotation.Nonnull CreateDemandRequest createDemandRequest, @javax.annotation.Nullable String idempotencyKey, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsPost201Response> localVarResponse = apiV1DemandsPostWithHttpInfo(createDemandRequest, idempotencyKey, headers);
    return localVarResponse.getData();
  }

  /**
   * Sözleşme oluştur (şablondan)
   * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa oluşturma anında yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez. Ayrıntı: açıklamadaki \&quot;Onaylayan (onay adımı)\&quot; bölümü.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı  **Kredi (çok belgeli zarf):** Şablon çok-belgeli bir zarf tanımlıyorsa (her belge farklı imzacı(lar)a atanabilir), imzacı başına kredi o imzacıya ATANMIŞ belge sayısına göre hesaplanır ve belge sayısına göre azalan birim fiyatlı bir indirim uygulanır: 1 belge %0, 2 belge %30, 3 ve üzeri %50 (indirim imzacı başına ayrı hesaplanır ve yukarı yuvarlanır). AB nitelikli zaman damgası (&#x60;eidas_timestamp&#x60;) seçiliyse aynı indirim zarftaki farklı belge sayısına göre toplam üzerinden uygulanır. Tek belgeli şablonlarda tutar eski formülle bayt-aynıdır.  **Atamasız imzacı:** &#x60;dispatch_notifications&#x60; &#x60;false&#x60; gönderilmediği sürece, eşlenen bir taraf şablonun hiçbir belgesine atanmamışsa sözleşme HİÇ oluşturulmaz (409 &#x60;PARTY_WITHOUT_DOCUMENTS&#x60;). Bu kontrolü atlamak için &#x60;dispatch_notifications: false&#x60; gönderip belge atamalarını &#x60;PUT .../documents/{docId}/assignments&#x60; ile düzelttikten sonra &#x60;POST .../dispatch&#x60; ile gönderin (o uç aynı kapıyı yeniden uygular). 
   * @param createDemandRequest  (required)
   * @param idempotencyKey Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)
   * @return ApiResponse&lt;ApiV1DemandsPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsPost201Response> apiV1DemandsPostWithHttpInfo(@javax.annotation.Nonnull CreateDemandRequest createDemandRequest, @javax.annotation.Nullable String idempotencyKey) throws ApiException {
    return apiV1DemandsPostWithHttpInfo(createDemandRequest, idempotencyKey, null);
  }

  /**
   * Sözleşme oluştur (şablondan)
   * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa oluşturma anında yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez. Ayrıntı: açıklamadaki \&quot;Onaylayan (onay adımı)\&quot; bölümü.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı  **Kredi (çok belgeli zarf):** Şablon çok-belgeli bir zarf tanımlıyorsa (her belge farklı imzacı(lar)a atanabilir), imzacı başına kredi o imzacıya ATANMIŞ belge sayısına göre hesaplanır ve belge sayısına göre azalan birim fiyatlı bir indirim uygulanır: 1 belge %0, 2 belge %30, 3 ve üzeri %50 (indirim imzacı başına ayrı hesaplanır ve yukarı yuvarlanır). AB nitelikli zaman damgası (&#x60;eidas_timestamp&#x60;) seçiliyse aynı indirim zarftaki farklı belge sayısına göre toplam üzerinden uygulanır. Tek belgeli şablonlarda tutar eski formülle bayt-aynıdır.  **Atamasız imzacı:** &#x60;dispatch_notifications&#x60; &#x60;false&#x60; gönderilmediği sürece, eşlenen bir taraf şablonun hiçbir belgesine atanmamışsa sözleşme HİÇ oluşturulmaz (409 &#x60;PARTY_WITHOUT_DOCUMENTS&#x60;). Bu kontrolü atlamak için &#x60;dispatch_notifications: false&#x60; gönderip belge atamalarını &#x60;PUT .../documents/{docId}/assignments&#x60; ile düzelttikten sonra &#x60;POST .../dispatch&#x60; ile gönderin (o uç aynı kapıyı yeniden uygular). 
   * @param createDemandRequest  (required)
   * @param idempotencyKey Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsPost201Response> apiV1DemandsPostWithHttpInfo(@javax.annotation.Nonnull CreateDemandRequest createDemandRequest, @javax.annotation.Nullable String idempotencyKey, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsPostRequestBuilder(createDemandRequest, idempotencyKey, headers);
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

  private HttpRequest.Builder apiV1DemandsPostRequestBuilder(@javax.annotation.Nonnull CreateDemandRequest createDemandRequest, @javax.annotation.Nullable String idempotencyKey, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'createDemandRequest' is set
    if (createDemandRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createDemandRequest' when calling apiV1DemandsPost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/demands";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    if (idempotencyKey != null) {
      localVarRequestBuilder.header("Idempotency-Key", idempotencyKey.toString());
    }
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
   * @param parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel &#x60;locale&#x60; (&#x60;tr&#x60;, &#x60;en&#x60; veya &#x60;null&#x60;): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. &#x60;PartyMappingInput.locale&#x60;). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer &#x60;400 INVALID_LOCALE&#x60; döner, sözleşme oluşturulmaz.  (required)
   * @param idempotencyKey Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)
   * @param order Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)
   * @param title Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS&#39;lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın.  (optional)
   * @param description  (optional)
   * @param fieldTemplateId Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  (optional)
   * @param force Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional)
   * @param sendInvitations &#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60; ve &#x60;\\\&quot;all\\\&quot;&#x60; tüm kanalları kullanır;   &#x60;\\\&quot;email\\\&quot;&#x60; yalnız e-posta, &#x60;\\\&quot;sms\\\&quot;&#x60; telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği &#x60;Idempotency-Key&#x60;   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı &#x60;DUPLICATE_SUSPECTED&#x60; yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte &#x60;Idempotency-Key&#x60; gönderin;   anahtar yoksa yeniden göndermek yerine   &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  (optional)
   * @param onAnchorMiss Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  (optional)
   * @return ApiV1DemandsUploadPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsUploadPost201Response apiV1DemandsUploadPost(@javax.annotation.Nonnull List<File> files, @javax.annotation.Nonnull String parties, @javax.annotation.Nullable String idempotencyKey, @javax.annotation.Nullable String order, @javax.annotation.Nullable String title, @javax.annotation.Nullable String description, @javax.annotation.Nullable UUID fieldTemplateId, @javax.annotation.Nullable String force, @javax.annotation.Nullable String sendInvitations, @javax.annotation.Nullable String onAnchorMiss) throws ApiException {
    return apiV1DemandsUploadPost(files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss, null);
  }

  /**
   * Dosya upload ile sözleşme oluştur (şablonsuz)
   * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
   * @param files 1 belge VEYA 1-20 görsel (required)
   * @param parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel &#x60;locale&#x60; (&#x60;tr&#x60;, &#x60;en&#x60; veya &#x60;null&#x60;): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. &#x60;PartyMappingInput.locale&#x60;). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer &#x60;400 INVALID_LOCALE&#x60; döner, sözleşme oluşturulmaz.  (required)
   * @param idempotencyKey Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)
   * @param order Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)
   * @param title Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS&#39;lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın.  (optional)
   * @param description  (optional)
   * @param fieldTemplateId Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  (optional)
   * @param force Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional)
   * @param sendInvitations &#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60; ve &#x60;\\\&quot;all\\\&quot;&#x60; tüm kanalları kullanır;   &#x60;\\\&quot;email\\\&quot;&#x60; yalnız e-posta, &#x60;\\\&quot;sms\\\&quot;&#x60; telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği &#x60;Idempotency-Key&#x60;   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı &#x60;DUPLICATE_SUSPECTED&#x60; yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte &#x60;Idempotency-Key&#x60; gönderin;   anahtar yoksa yeniden göndermek yerine   &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  (optional)
   * @param onAnchorMiss Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiV1DemandsUploadPost201Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1DemandsUploadPost201Response apiV1DemandsUploadPost(@javax.annotation.Nonnull List<File> files, @javax.annotation.Nonnull String parties, @javax.annotation.Nullable String idempotencyKey, @javax.annotation.Nullable String order, @javax.annotation.Nullable String title, @javax.annotation.Nullable String description, @javax.annotation.Nullable UUID fieldTemplateId, @javax.annotation.Nullable String force, @javax.annotation.Nullable String sendInvitations, @javax.annotation.Nullable String onAnchorMiss, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1DemandsUploadPost201Response> localVarResponse = apiV1DemandsUploadPostWithHttpInfo(files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss, headers);
    return localVarResponse.getData();
  }

  /**
   * Dosya upload ile sözleşme oluştur (şablonsuz)
   * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
   * @param files 1 belge VEYA 1-20 görsel (required)
   * @param parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel &#x60;locale&#x60; (&#x60;tr&#x60;, &#x60;en&#x60; veya &#x60;null&#x60;): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. &#x60;PartyMappingInput.locale&#x60;). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer &#x60;400 INVALID_LOCALE&#x60; döner, sözleşme oluşturulmaz.  (required)
   * @param idempotencyKey Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)
   * @param order Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)
   * @param title Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS&#39;lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın.  (optional)
   * @param description  (optional)
   * @param fieldTemplateId Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  (optional)
   * @param force Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional)
   * @param sendInvitations &#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60; ve &#x60;\\\&quot;all\\\&quot;&#x60; tüm kanalları kullanır;   &#x60;\\\&quot;email\\\&quot;&#x60; yalnız e-posta, &#x60;\\\&quot;sms\\\&quot;&#x60; telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği &#x60;Idempotency-Key&#x60;   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı &#x60;DUPLICATE_SUSPECTED&#x60; yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte &#x60;Idempotency-Key&#x60; gönderin;   anahtar yoksa yeniden göndermek yerine   &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  (optional)
   * @param onAnchorMiss Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  (optional)
   * @return ApiResponse&lt;ApiV1DemandsUploadPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsUploadPost201Response> apiV1DemandsUploadPostWithHttpInfo(@javax.annotation.Nonnull List<File> files, @javax.annotation.Nonnull String parties, @javax.annotation.Nullable String idempotencyKey, @javax.annotation.Nullable String order, @javax.annotation.Nullable String title, @javax.annotation.Nullable String description, @javax.annotation.Nullable UUID fieldTemplateId, @javax.annotation.Nullable String force, @javax.annotation.Nullable String sendInvitations, @javax.annotation.Nullable String onAnchorMiss) throws ApiException {
    return apiV1DemandsUploadPostWithHttpInfo(files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss, null);
  }

  /**
   * Dosya upload ile sözleşme oluştur (şablonsuz)
   * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
   * @param files 1 belge VEYA 1-20 görsel (required)
   * @param parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel &#x60;locale&#x60; (&#x60;tr&#x60;, &#x60;en&#x60; veya &#x60;null&#x60;): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. &#x60;PartyMappingInput.locale&#x60;). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer &#x60;400 INVALID_LOCALE&#x60; döner, sözleşme oluşturulmaz.  (required)
   * @param idempotencyKey Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)
   * @param order Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)
   * @param title Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS&#39;lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın.  (optional)
   * @param description  (optional)
   * @param fieldTemplateId Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  (optional)
   * @param force Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional)
   * @param sendInvitations &#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60; ve &#x60;\\\&quot;all\\\&quot;&#x60; tüm kanalları kullanır;   &#x60;\\\&quot;email\\\&quot;&#x60; yalnız e-posta, &#x60;\\\&quot;sms\\\&quot;&#x60; telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği &#x60;Idempotency-Key&#x60;   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı &#x60;DUPLICATE_SUSPECTED&#x60; yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte &#x60;Idempotency-Key&#x60; gönderin;   anahtar yoksa yeniden göndermek yerine   &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  (optional)
   * @param onAnchorMiss Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1DemandsUploadPost201Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1DemandsUploadPost201Response> apiV1DemandsUploadPostWithHttpInfo(@javax.annotation.Nonnull List<File> files, @javax.annotation.Nonnull String parties, @javax.annotation.Nullable String idempotencyKey, @javax.annotation.Nullable String order, @javax.annotation.Nullable String title, @javax.annotation.Nullable String description, @javax.annotation.Nullable UUID fieldTemplateId, @javax.annotation.Nullable String force, @javax.annotation.Nullable String sendInvitations, @javax.annotation.Nullable String onAnchorMiss, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1DemandsUploadPostRequestBuilder(files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss, headers);
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

  private HttpRequest.Builder apiV1DemandsUploadPostRequestBuilder(@javax.annotation.Nonnull List<File> files, @javax.annotation.Nonnull String parties, @javax.annotation.Nullable String idempotencyKey, @javax.annotation.Nullable String order, @javax.annotation.Nullable String title, @javax.annotation.Nullable String description, @javax.annotation.Nullable UUID fieldTemplateId, @javax.annotation.Nullable String force, @javax.annotation.Nullable String sendInvitations, @javax.annotation.Nullable String onAnchorMiss, Map<String, String> headers) throws ApiException {
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

    if (idempotencyKey != null) {
      localVarRequestBuilder.header("Idempotency-Key", idempotencyKey.toString());
    }
    localVarRequestBuilder.header("Accept", "application/json");

    MultipartEntityBuilder multiPartBuilder = MultipartEntityBuilder.create();
    boolean hasFiles = false;
    for (int i=0; i < files.size(); i++) {
        multiPartBuilder.addBinaryBody("files", files.get(i));
        hasFiles = true;
    }
    if (order != null) {
        multiPartBuilder.addTextBody("order", order.toString(), org.apache.http.entity.ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
    }
    if (title != null) {
        multiPartBuilder.addTextBody("title", title.toString(), org.apache.http.entity.ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
    }
    if (description != null) {
        multiPartBuilder.addTextBody("description", description.toString(), org.apache.http.entity.ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
    }
    if (parties != null) {
        multiPartBuilder.addTextBody("parties", parties.toString(), org.apache.http.entity.ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
    }
    if (fieldTemplateId != null) {
        multiPartBuilder.addTextBody("field_template_id", fieldTemplateId.toString(), org.apache.http.entity.ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
    }
    if (force != null) {
        multiPartBuilder.addTextBody("force", force.toString(), org.apache.http.entity.ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
    }
    if (sendInvitations != null) {
        multiPartBuilder.addTextBody("send_invitations", sendInvitations.toString(), org.apache.http.entity.ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
    }
    if (onAnchorMiss != null) {
        multiPartBuilder.addTextBody("on_anchor_miss", onAnchorMiss.toString(), org.apache.http.entity.ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
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

  /**
   * Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
   * Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 
   * @param id Alan Şablonu (FIELD_LAYOUT) kimliği (required)
   * @param files Tek PDF belge (required)
   * @param onAnchorMiss &#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  (optional)
   * @return ApiV1FieldTemplatesIdPreviewLayoutPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1FieldTemplatesIdPreviewLayoutPost200Response apiV1FieldTemplatesIdPreviewLayoutPost(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull List<File> files, @javax.annotation.Nullable String onAnchorMiss) throws ApiException {
    return apiV1FieldTemplatesIdPreviewLayoutPost(id, files, onAnchorMiss, null);
  }

  /**
   * Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
   * Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 
   * @param id Alan Şablonu (FIELD_LAYOUT) kimliği (required)
   * @param files Tek PDF belge (required)
   * @param onAnchorMiss &#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiV1FieldTemplatesIdPreviewLayoutPost200Response
   * @throws ApiException if fails to make API call
   */
  public ApiV1FieldTemplatesIdPreviewLayoutPost200Response apiV1FieldTemplatesIdPreviewLayoutPost(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull List<File> files, @javax.annotation.Nullable String onAnchorMiss, Map<String, String> headers) throws ApiException {
    ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response> localVarResponse = apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo(id, files, onAnchorMiss, headers);
    return localVarResponse.getData();
  }

  /**
   * Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
   * Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 
   * @param id Alan Şablonu (FIELD_LAYOUT) kimliği (required)
   * @param files Tek PDF belge (required)
   * @param onAnchorMiss &#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  (optional)
   * @return ApiResponse&lt;ApiV1FieldTemplatesIdPreviewLayoutPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response> apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull List<File> files, @javax.annotation.Nullable String onAnchorMiss) throws ApiException {
    return apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo(id, files, onAnchorMiss, null);
  }

  /**
   * Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
   * Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 
   * @param id Alan Şablonu (FIELD_LAYOUT) kimliği (required)
   * @param files Tek PDF belge (required)
   * @param onAnchorMiss &#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  (optional)
   * @param headers Optional headers to include in the request
   * @return ApiResponse&lt;ApiV1FieldTemplatesIdPreviewLayoutPost200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response> apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull List<File> files, @javax.annotation.Nullable String onAnchorMiss, Map<String, String> headers) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = apiV1FieldTemplatesIdPreviewLayoutPostRequestBuilder(id, files, onAnchorMiss, headers);
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
          throw getApiException("apiV1FieldTemplatesIdPreviewLayoutPost", localVarResponse);
        }
        localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
        if (localVarResponseBody == null) {
          return new ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        
        
        String responseBody = new String(localVarResponseBody.readAllBytes());
        ApiV1FieldTemplatesIdPreviewLayoutPost200Response responseValue = responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ApiV1FieldTemplatesIdPreviewLayoutPost200Response>() {});
        

        return new ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response>(
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

  private HttpRequest.Builder apiV1FieldTemplatesIdPreviewLayoutPostRequestBuilder(@javax.annotation.Nonnull UUID id, @javax.annotation.Nonnull List<File> files, @javax.annotation.Nullable String onAnchorMiss, Map<String, String> headers) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling apiV1FieldTemplatesIdPreviewLayoutPost");
    }
    // verify the required parameter 'files' is set
    if (files == null) {
      throw new ApiException(400, "Missing the required parameter 'files' when calling apiV1FieldTemplatesIdPreviewLayoutPost");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/field-templates/{id}/preview-layout"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/json");

    MultipartEntityBuilder multiPartBuilder = MultipartEntityBuilder.create();
    boolean hasFiles = false;
    for (int i=0; i < files.size(); i++) {
        multiPartBuilder.addBinaryBody("files", files.get(i));
        hasFiles = true;
    }
    if (onAnchorMiss != null) {
        multiPartBuilder.addTextBody("on_anchor_miss", onAnchorMiss.toString(), org.apache.http.entity.ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
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
