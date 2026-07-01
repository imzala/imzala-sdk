/*
 * imzala External API
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.6.0 · **Son güncelleme:** 2026-06-30  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API key kullanıyorsanız `X-Workspace-Id` header'ı göndermeniz gerekir (organizasyon UUID'si). Kişisel anahtarlar için bu header gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - 60 istek/dakika per API key - Aşılırsa 429 döner  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (6) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 6 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.6.0
 * Contact: destek@imzala.org
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package org.imzala.client.generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.openapitools.jackson.nullable.JsonNullableModule;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Configuration and utility class for API clients.
 *
 * <p>This class can be constructed and modified, then used to instantiate the
 * various API classes. The API classes use the settings in this class to
 * configure themselves, but otherwise do not store a link to this class.</p>
 *
 * <p>This class is mutable and not synchronized, so it is not thread-safe.
 * The API classes generated from this are immutable and thread-safe.</p>
 *
 * <p>The setter methods of this class return the current object to facilitate
 * a fluent style of configuration.</p>
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-07-01T14:50:26.407096+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class ApiClient {

  protected HttpClient.Builder builder;
  protected ObjectMapper mapper;
  protected String scheme;
  protected String host;
  protected int port;
  protected String basePath;
  protected Consumer<HttpRequest.Builder> interceptor;
  protected Consumer<HttpResponse<InputStream>> responseInterceptor;
  protected Consumer<HttpResponse<InputStream>> asyncResponseInterceptor;
  protected Duration readTimeout;
  protected Duration connectTimeout;

  public static String valueToString(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof OffsetDateTime) {
      return ((OffsetDateTime) value).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
    return value.toString();
  }

  /**
   * URL encode a string in the UTF-8 encoding.
   *
   * @param s String to encode.
   * @return URL-encoded representation of the input string.
   */
  public static String urlEncode(String s) {
    return URLEncoder.encode(s, UTF_8).replaceAll("\\+", "%20");
  }

  /**
   * Convert a URL query name/value parameter to a list of encoded {@link Pair}
   * objects.
   *
   * <p>The value can be null, in which case an empty list is returned.</p>
   *
   * @param name The query name parameter.
   * @param value The query value, which may not be a collection but may be
   *              null.
   * @return A singleton list of the {@link Pair} objects representing the input
   * parameters, which is encoded for use in a URL. If the value is null, an
   * empty list is returned.
   */
  public static List<Pair> parameterToPairs(String name, Object value) {
    if (name == null || name.isEmpty() || value == null) {
      return Collections.emptyList();
    }
    return Collections.singletonList(new Pair(urlEncode(name), urlEncode(valueToString(value))));
  }

  /**
   * Convert a URL query name/collection parameter to a list of encoded
   * {@link Pair} objects.
   *
   * @param collectionFormat The swagger collectionFormat string (csv, tsv, etc).
   * @param name The query name parameter.
   * @param values A collection of values for the given query name, which may be
   *               null.
   * @return A list of {@link Pair} objects representing the input parameters,
   * which is encoded for use in a URL. If the values collection is null, an
   * empty list is returned.
   */
  public static List<Pair> parameterToPairs(
      String collectionFormat, String name, Collection<?> values) {
    if (name == null || name.isEmpty() || values == null || values.isEmpty()) {
      return Collections.emptyList();
    }

    // get the collection format (default: csv)
    String format = collectionFormat == null || collectionFormat.isEmpty() ? "csv" : collectionFormat;

    // create the params based on the collection format
    if ("multi".equals(format)) {
      return values.stream()
          .map(value -> new Pair(urlEncode(name), urlEncode(valueToString(value))))
          .collect(Collectors.toList());
    }

    String delimiter;
    switch(format) {
      case "csv":
        delimiter = urlEncode(",");
        break;
      case "ssv":
        delimiter = urlEncode(" ");
        break;
      case "tsv":
        delimiter = urlEncode("\t");
        break;
      case "pipes":
        delimiter = urlEncode("|");
        break;
      default:
        throw new IllegalArgumentException("Illegal collection format: " + collectionFormat);
    }

    StringJoiner joiner = new StringJoiner(delimiter);
    for (Object value : values) {
      joiner.add(urlEncode(valueToString(value)));
    }

    return Collections.singletonList(new Pair(urlEncode(name), joiner.toString()));
  }

  /**
   * Create an instance of ApiClient.
   */
  public ApiClient() {
    this.builder = createDefaultHttpClientBuilder();
    this.mapper = createDefaultObjectMapper();
    updateBaseUri("https://api-prd.imzala.org");
    interceptor = null;
    readTimeout = null;
    connectTimeout = null;
    responseInterceptor = null;
    asyncResponseInterceptor = null;
  }

  /**
   * Create an instance of ApiClient.
   *
   * @param builder Http client builder.
   * @param mapper Object mapper.
   * @param baseUri Base URI
   */
  public ApiClient(HttpClient.Builder builder, ObjectMapper mapper, String baseUri) {
    this.builder = builder;
    this.mapper = mapper;
    updateBaseUri(baseUri != null ? baseUri : "https://api-prd.imzala.org");
    interceptor = null;
    readTimeout = null;
    connectTimeout = null;
    responseInterceptor = null;
    asyncResponseInterceptor = null;
  }

  public static ObjectMapper createDefaultObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(new JsonNullableModule());
    mapper.registerModule(new RFC3339JavaTimeModule());
    return mapper;
  }

  protected final String getDefaultBaseUri() {
    return basePath;
  }

  public static HttpClient.Builder createDefaultHttpClientBuilder() {
    return HttpClient.newBuilder();
  }

  public final void updateBaseUri(String baseUri) {
    URI uri = URI.create(baseUri);
    scheme = uri.getScheme();
    host = uri.getHost();
    port = uri.getPort();
    basePath = uri.getRawPath();
  }

  /**
   * Set a custom {@link HttpClient.Builder} object to use when creating the
   * {@link HttpClient} that is used by the API client.
   *
   * @param builder Custom client builder.
   * @return This object.
   */
  public ApiClient setHttpClientBuilder(HttpClient.Builder builder) {
    this.builder = builder;
    return this;
  }

  /**
   * Get an {@link HttpClient} based on the current {@link HttpClient.Builder}.
   *
   * <p>The returned object is immutable and thread-safe.</p>
   *
   * @return The HTTP client.
   */
  public HttpClient getHttpClient() {
    return builder.build();
  }

  /**
   * Set a custom {@link ObjectMapper} to serialize and deserialize the request
   * and response bodies.
   *
   * @param mapper Custom object mapper.
   * @return This object.
   */
  public ApiClient setObjectMapper(ObjectMapper mapper) {
    this.mapper = mapper;
    return this;
  }

  /**
   * Get a copy of the current {@link ObjectMapper}.
   *
   * @return A copy of the current object mapper.
   */
  public ObjectMapper getObjectMapper() {
    return mapper.copy();
  }

  /**
   * Set a custom host name for the target service.
   *
   * @param host The host name of the target service.
   * @return This object.
   */
  public ApiClient setHost(String host) {
    this.host = host;
    return this;
  }

  /**
   * Set a custom port number for the target service.
   *
   * @param port The port of the target service. Set this to -1 to reset the
   *             value to the default for the scheme.
   * @return This object.
   */
  public ApiClient setPort(int port) {
    this.port = port;
    return this;
  }

  /**
   * Set a custom base path for the target service, for example '/v2'.
   *
   * @param basePath The base path against which the rest of the path is
   *                 resolved.
   * @return This object.
   */
  public ApiClient setBasePath(String basePath) {
    this.basePath = basePath;
    return this;
  }

  /**
   * Get the base URI to resolve the endpoint paths against.
   *
   * @return The complete base URI that the rest of the API parameters are
   * resolved against.
   */
  public String getBaseUri() {
    return scheme + "://" + host + (port == -1 ? "" : ":" + port) + basePath;
  }

  /**
   * Set a custom scheme for the target service, for example 'https'.
   *
   * @param scheme The scheme of the target service
   * @return This object.
   */
  public ApiClient setScheme(String scheme){
    this.scheme = scheme;
    return this;
  }

  /**
   * Set a custom request interceptor.
   *
   * <p>A request interceptor is a mechanism for altering each request before it
   * is sent. After the request has been fully configured but not yet built, the
   * request builder is passed into this function for further modification,
   * after which it is sent out.</p>
   *
   * <p>This is useful for altering the requests in a custom manner, such as
   * adding headers. It could also be used for logging and monitoring.</p>
   *
   * @param interceptor A function invoked before creating each request. A value
   *                    of null resets the interceptor to a no-op.
   * @return This object.
   */
  public ApiClient setRequestInterceptor(Consumer<HttpRequest.Builder> interceptor) {
    this.interceptor = interceptor;
    return this;
  }

  /**
   * Get the custom interceptor.
   *
   * @return The custom interceptor that was set, or null if there isn't any.
   */
  public Consumer<HttpRequest.Builder> getRequestInterceptor() {
    return interceptor;
  }

  /**
   * Set a custom response interceptor.
   *
   * <p>This is useful for logging, monitoring or extraction of header variables</p>
   *
   * @param interceptor A function invoked before creating each request. A value
   *                    of null resets the interceptor to a no-op.
   * @return This object.
   */
  public ApiClient setResponseInterceptor(Consumer<HttpResponse<InputStream>> interceptor) {
    this.responseInterceptor = interceptor;
    return this;
  }

 /**
   * Get the custom response interceptor.
   *
   * @return The custom interceptor that was set, or null if there isn't any.
   */
  public Consumer<HttpResponse<InputStream>> getResponseInterceptor() {
    return responseInterceptor;
  }

  /**
   * Set a custom async response interceptor. Use this interceptor when asyncNative is set to 'true'.
   *
   * <p>This is useful for logging, monitoring or extraction of header variables</p>
   *
   * @param interceptor A function invoked before creating each request. A value
   *                    of null resets the interceptor to a no-op.
   * @return This object.
   */
  public ApiClient setAsyncResponseInterceptor(Consumer<HttpResponse<InputStream>> interceptor) {
    this.asyncResponseInterceptor = interceptor;
    return this;
  }

 /**
   * Get the custom async response interceptor. Use this interceptor when asyncNative is set to 'true'.
   *
   * @return The custom interceptor that was set, or null if there isn't any.
   */
  public Consumer<HttpResponse<InputStream>> getAsyncResponseInterceptor() {
    return asyncResponseInterceptor;
  }

  /**
   * Set the read timeout for the http client.
   *
   * <p>This is the value used by default for each request, though it can be
   * overridden on a per-request basis with a request interceptor.</p>
   *
   * @param readTimeout The read timeout used by default by the http client.
   *                    Setting this value to null resets the timeout to an
   *                    effectively infinite value.
   * @return This object.
   */
  public ApiClient setReadTimeout(Duration readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }

  /**
   * Get the read timeout that was set.
   *
   * @return The read timeout, or null if no timeout was set. Null represents
   * an infinite wait time.
   */
  public Duration getReadTimeout() {
    return readTimeout;
  }
  /**
   * Sets the connect timeout (in milliseconds) for the http client.
   *
   * <p> In the case where a new connection needs to be established, if
   * the connection cannot be established within the given {@code
   * duration}, then {@link HttpClient#send(HttpRequest,BodyHandler)
   * HttpClient::send} throws an {@link HttpConnectTimeoutException}, or
   * {@link HttpClient#sendAsync(HttpRequest,BodyHandler)
   * HttpClient::sendAsync} completes exceptionally with an
   * {@code HttpConnectTimeoutException}. If a new connection does not
   * need to be established, for example if a connection can be reused
   * from a previous request, then this timeout duration has no effect.
   *
   * @param connectTimeout connection timeout in milliseconds
   *
   * @return This object.
   */
  public ApiClient setConnectTimeout(Duration connectTimeout) {
    this.connectTimeout = connectTimeout;
    this.builder.connectTimeout(connectTimeout);
    return this;
  }

  /**
   * Get connection timeout (in milliseconds).
   *
   * @return Timeout in milliseconds
   */
  public Duration getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Returns the response body InputStream, transparently decoding gzip-compressed
   * payloads when the server sets {@code Content-Encoding: gzip}.
   *
   * @param response HTTP response whose body should be consumed
   * @return Original or decompressed InputStream for the response body
   * @throws IOException if the response body cannot be accessed or wrapping fails
   */
  public static InputStream getResponseBody(HttpResponse<InputStream> response) throws IOException {
    if (response == null) {
      return null;
    }
    InputStream body = response.body();
    if (body == null) {
      return null;
    }
    Optional<String> encoding = response.headers().firstValue("Content-Encoding");
    if (encoding.isPresent()) {
      for (String token : encoding.get().split(",")) {
        if ("gzip".equalsIgnoreCase(token.trim())) {
          return new GZIPInputStream(body, 8192);
        }
      }
    }
    return body;
  }

}
