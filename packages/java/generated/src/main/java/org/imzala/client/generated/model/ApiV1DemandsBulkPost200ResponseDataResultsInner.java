/*
 * imzala External API
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.8.13 · **Son güncelleme:** 2026-09-15  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API key kullanıyorsanız `X-Workspace-Id` header'ı göndermeniz gerekir (organizasyon UUID'si). Kişisel anahtarlar için bu header gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (8) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 8 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.8.13
 * Contact: destek@imzala.org
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.imzala.client.generated.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.imzala.client.generated.model.ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * ApiV1DemandsBulkPost200ResponseDataResultsInner
 */
@JsonPropertyOrder({
  ApiV1DemandsBulkPost200ResponseDataResultsInner.JSON_PROPERTY_ROW_INDEX,
  ApiV1DemandsBulkPost200ResponseDataResultsInner.JSON_PROPERTY_STATUS,
  ApiV1DemandsBulkPost200ResponseDataResultsInner.JSON_PROPERTY_DEMAND_ID,
  ApiV1DemandsBulkPost200ResponseDataResultsInner.JSON_PROPERTY_SIGNING_URLS,
  ApiV1DemandsBulkPost200ResponseDataResultsInner.JSON_PROPERTY_RESULT_URL,
  ApiV1DemandsBulkPost200ResponseDataResultsInner.JSON_PROPERTY_DISPATCHED,
  ApiV1DemandsBulkPost200ResponseDataResultsInner.JSON_PROPERTY_ERROR,
  ApiV1DemandsBulkPost200ResponseDataResultsInner.JSON_PROPERTY_MESSAGE
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-09-15T14:40:40.831203+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class ApiV1DemandsBulkPost200ResponseDataResultsInner {
  public static final String JSON_PROPERTY_ROW_INDEX = "row_index";
  @javax.annotation.Nullable
  private Integer rowIndex;

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    CREATED(String.valueOf("created")),
    
    FAILED(String.valueOf("failed"));

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
      for (StatusEnum b : StatusEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_STATUS = "status";
  @javax.annotation.Nullable
  private StatusEnum status;

  public static final String JSON_PROPERTY_DEMAND_ID = "demand_id";
  @javax.annotation.Nullable
  private UUID demandId;

  public static final String JSON_PROPERTY_SIGNING_URLS = "signing_urls";
  @javax.annotation.Nullable
  private List<ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner> signingUrls = new ArrayList<>();

  public static final String JSON_PROPERTY_RESULT_URL = "result_url";
  @javax.annotation.Nullable
  private URI resultUrl;

  public static final String JSON_PROPERTY_DISPATCHED = "dispatched";
  @javax.annotation.Nullable
  private Integer dispatched;

  public static final String JSON_PROPERTY_ERROR = "error";
  @javax.annotation.Nullable
  private String error;

  public static final String JSON_PROPERTY_MESSAGE = "message";
  @javax.annotation.Nullable
  private String message;

  public ApiV1DemandsBulkPost200ResponseDataResultsInner() { 
  }

  public ApiV1DemandsBulkPost200ResponseDataResultsInner rowIndex(@javax.annotation.Nullable Integer rowIndex) {
    this.rowIndex = rowIndex;
    return this;
  }

  /**
   * Get rowIndex
   * @return rowIndex
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_ROW_INDEX, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Integer getRowIndex() {
    return rowIndex;
  }


  @JsonProperty(value = JSON_PROPERTY_ROW_INDEX, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRowIndex(@javax.annotation.Nullable Integer rowIndex) {
    this.rowIndex = rowIndex;
  }


  public ApiV1DemandsBulkPost200ResponseDataResultsInner status(@javax.annotation.Nullable StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_STATUS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public StatusEnum getStatus() {
    return status;
  }


  @JsonProperty(value = JSON_PROPERTY_STATUS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setStatus(@javax.annotation.Nullable StatusEnum status) {
    this.status = status;
  }


  public ApiV1DemandsBulkPost200ResponseDataResultsInner demandId(@javax.annotation.Nullable UUID demandId) {
    this.demandId = demandId;
    return this;
  }

  /**
   * status&#x3D;created ise; veya status&#x3D;failed olup sözleşme oluşturulduktan SONRA kredi mutabakatı reddettiyse (taslak, davet gönderilmedi)
   * @return demandId
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DEMAND_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public UUID getDemandId() {
    return demandId;
  }


  @JsonProperty(value = JSON_PROPERTY_DEMAND_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDemandId(@javax.annotation.Nullable UUID demandId) {
    this.demandId = demandId;
  }


  public ApiV1DemandsBulkPost200ResponseDataResultsInner signingUrls(@javax.annotation.Nullable List<ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner> signingUrls) {
    this.signingUrls = signingUrls;
    return this;
  }

  public ApiV1DemandsBulkPost200ResponseDataResultsInner addSigningUrlsItem(ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner signingUrlsItem) {
    if (this.signingUrls == null) {
      this.signingUrls = new ArrayList<>();
    }
    this.signingUrls.add(signingUrlsItem);
    return this;
  }

  /**
   * Get signingUrls
   * @return signingUrls
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_SIGNING_URLS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner> getSigningUrls() {
    return signingUrls;
  }


  @JsonProperty(value = JSON_PROPERTY_SIGNING_URLS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSigningUrls(@javax.annotation.Nullable List<ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner> signingUrls) {
    this.signingUrls = signingUrls;
  }


  public ApiV1DemandsBulkPost200ResponseDataResultsInner resultUrl(@javax.annotation.Nullable URI resultUrl) {
    this.resultUrl = resultUrl;
    return this;
  }

  /**
   * Get resultUrl
   * @return resultUrl
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_RESULT_URL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public URI getResultUrl() {
    return resultUrl;
  }


  @JsonProperty(value = JSON_PROPERTY_RESULT_URL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setResultUrl(@javax.annotation.Nullable URI resultUrl) {
    this.resultUrl = resultUrl;
  }


  public ApiV1DemandsBulkPost200ResponseDataResultsInner dispatched(@javax.annotation.Nullable Integer dispatched) {
    this.dispatched = dispatched;
    return this;
  }

  /**
   * Gönderilen davet (SMS+e-posta) sayısı
   * @return dispatched
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DISPATCHED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Integer getDispatched() {
    return dispatched;
  }


  @JsonProperty(value = JSON_PROPERTY_DISPATCHED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDispatched(@javax.annotation.Nullable Integer dispatched) {
    this.dispatched = dispatched;
  }


  public ApiV1DemandsBulkPost200ResponseDataResultsInner error(@javax.annotation.Nullable String error) {
    this.error = error;
    return this;
  }

  /**
   * status&#x3D;failed ise makinece okunabilir kod (VALIDATION, INSUFFICIENT_CREDITS, MEMBER_LIMIT_EXCEEDED, CREATE_FAILED vb.)
   * @return error
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_ERROR, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getError() {
    return error;
  }


  @JsonProperty(value = JSON_PROPERTY_ERROR, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setError(@javax.annotation.Nullable String error) {
    this.error = error;
  }


  public ApiV1DemandsBulkPost200ResponseDataResultsInner message(@javax.annotation.Nullable String message) {
    this.message = message;
    return this;
  }

  /**
   * status&#x3D;failed ise açıklama
   * @return message
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_MESSAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getMessage() {
    return message;
  }


  @JsonProperty(value = JSON_PROPERTY_MESSAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setMessage(@javax.annotation.Nullable String message) {
    this.message = message;
  }


  /**
   * Return true if this _api_v1_demands_bulk_post_200_response_data_results_inner object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiV1DemandsBulkPost200ResponseDataResultsInner apiV1DemandsBulkPost200ResponseDataResultsInner = (ApiV1DemandsBulkPost200ResponseDataResultsInner) o;
    return Objects.equals(this.rowIndex, apiV1DemandsBulkPost200ResponseDataResultsInner.rowIndex) &&
        Objects.equals(this.status, apiV1DemandsBulkPost200ResponseDataResultsInner.status) &&
        Objects.equals(this.demandId, apiV1DemandsBulkPost200ResponseDataResultsInner.demandId) &&
        Objects.equals(this.signingUrls, apiV1DemandsBulkPost200ResponseDataResultsInner.signingUrls) &&
        Objects.equals(this.resultUrl, apiV1DemandsBulkPost200ResponseDataResultsInner.resultUrl) &&
        Objects.equals(this.dispatched, apiV1DemandsBulkPost200ResponseDataResultsInner.dispatched) &&
        Objects.equals(this.error, apiV1DemandsBulkPost200ResponseDataResultsInner.error) &&
        Objects.equals(this.message, apiV1DemandsBulkPost200ResponseDataResultsInner.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rowIndex, status, demandId, signingUrls, resultUrl, dispatched, error, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiV1DemandsBulkPost200ResponseDataResultsInner {\n");
    sb.append("    rowIndex: ").append(toIndentedString(rowIndex)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    demandId: ").append(toIndentedString(demandId)).append("\n");
    sb.append("    signingUrls: ").append(toIndentedString(signingUrls)).append("\n");
    sb.append("    resultUrl: ").append(toIndentedString(resultUrl)).append("\n");
    sb.append("    dispatched: ").append(toIndentedString(dispatched)).append("\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }

  /**
   * Convert the instance into URL query string.
   *
   * @return URL query string
   */
  public String toUrlQueryString() {
    return toUrlQueryString(null);
  }

  /**
   * Convert the instance into URL query string.
   *
   * @param prefix prefix of the query string
   * @return URL query string
   */
  public String toUrlQueryString(String prefix) {
    String suffix = "";
    String containerSuffix = "";
    String containerPrefix = "";
    if (prefix == null) {
      // style=form, explode=true, e.g. /pet?name=cat&type=manx
      prefix = "";
    } else {
      // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
      prefix = prefix + "[";
      suffix = "]";
      containerSuffix = "]";
      containerPrefix = "[";
    }

    StringJoiner joiner = new StringJoiner("&");

    // add `row_index` to the URL query string
    if (getRowIndex() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%srow_index%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRowIndex()))));
    }

    // add `status` to the URL query string
    if (getStatus() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sstatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
    }

    // add `demand_id` to the URL query string
    if (getDemandId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sdemand_id%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDemandId()))));
    }

    // add `signing_urls` to the URL query string
    if (getSigningUrls() != null) {
      for (int i = 0; i < getSigningUrls().size(); i++) {
        if (getSigningUrls().get(i) != null) {
          joiner.add(getSigningUrls().get(i).toUrlQueryString(String.format(java.util.Locale.ROOT, "%ssigning_urls%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `result_url` to the URL query string
    if (getResultUrl() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sresult_url%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getResultUrl()))));
    }

    // add `dispatched` to the URL query string
    if (getDispatched() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sdispatched%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDispatched()))));
    }

    // add `error` to the URL query string
    if (getError() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%serror%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getError()))));
    }

    // add `message` to the URL query string
    if (getMessage() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%smessage%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getMessage()))));
    }

    return joiner.toString();
  }
}

