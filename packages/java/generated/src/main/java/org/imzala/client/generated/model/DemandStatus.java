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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.imzala.client.generated.model.DemandStatusPartiesInner;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * DemandStatus
 */
@JsonPropertyOrder({
  DemandStatus.JSON_PROPERTY_ID,
  DemandStatus.JSON_PROPERTY_TITLE,
  DemandStatus.JSON_PROPERTY_STATUS,
  DemandStatus.JSON_PROPERTY_CREATED_AT,
  DemandStatus.JSON_PROPERTY_COMPLETED_AT,
  DemandStatus.JSON_PROPERTY_PARTIES,
  DemandStatus.JSON_PROPERTY_RESULT_URL,
  DemandStatus.JSON_PROPERTY_PDF_URL
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-07-03T05:18:13.896742+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class DemandStatus {
  public static final String JSON_PROPERTY_ID = "id";
  @javax.annotation.Nullable
  private UUID id;

  public static final String JSON_PROPERTY_TITLE = "title";
  @javax.annotation.Nullable
  private String title;

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    DRAFT(String.valueOf("DRAFT")),
    
    PENDING(String.valueOf("PENDING")),
    
    COMPLETED(String.valueOf("COMPLETED")),
    
    EXPIRED(String.valueOf("EXPIRED")),
    
    CANCELLED(String.valueOf("CANCELLED"));

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

  public static final String JSON_PROPERTY_CREATED_AT = "created_at";
  @javax.annotation.Nullable
  private OffsetDateTime createdAt;

  public static final String JSON_PROPERTY_COMPLETED_AT = "completed_at";
  private JsonNullable<OffsetDateTime> completedAt = JsonNullable.<OffsetDateTime>undefined();

  public static final String JSON_PROPERTY_PARTIES = "parties";
  @javax.annotation.Nullable
  private List<DemandStatusPartiesInner> parties = new ArrayList<>();

  public static final String JSON_PROPERTY_RESULT_URL = "result_url";
  @javax.annotation.Nullable
  private URI resultUrl;

  public static final String JSON_PROPERTY_PDF_URL = "pdf_url";
  private JsonNullable<URI> pdfUrl = JsonNullable.<URI>undefined();

  public DemandStatus() { 
  }

  public DemandStatus id(@javax.annotation.Nullable UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public UUID getId() {
    return id;
  }


  @JsonProperty(value = JSON_PROPERTY_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setId(@javax.annotation.Nullable UUID id) {
    this.id = id;
  }


  public DemandStatus title(@javax.annotation.Nullable String title) {
    this.title = title;
    return this;
  }

  /**
   * Get title
   * @return title
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_TITLE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getTitle() {
    return title;
  }


  @JsonProperty(value = JSON_PROPERTY_TITLE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTitle(@javax.annotation.Nullable String title) {
    this.title = title;
  }


  public DemandStatus status(@javax.annotation.Nullable StatusEnum status) {
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


  public DemandStatus createdAt(@javax.annotation.Nullable OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_CREATED_AT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }


  @JsonProperty(value = JSON_PROPERTY_CREATED_AT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCreatedAt(@javax.annotation.Nullable OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }


  public DemandStatus completedAt(@javax.annotation.Nullable OffsetDateTime completedAt) {
    this.completedAt = JsonNullable.<OffsetDateTime>of(completedAt);
    return this;
  }

  /**
   * Get completedAt
   * @return completedAt
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public OffsetDateTime getCompletedAt() {
        return completedAt.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_COMPLETED_AT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<OffsetDateTime> getCompletedAt_JsonNullable() {
    return completedAt;
  }
  
  @JsonProperty(JSON_PROPERTY_COMPLETED_AT)
  public void setCompletedAt_JsonNullable(JsonNullable<OffsetDateTime> completedAt) {
    this.completedAt = completedAt;
  }

  public void setCompletedAt(@javax.annotation.Nullable OffsetDateTime completedAt) {
    this.completedAt = JsonNullable.<OffsetDateTime>of(completedAt);
  }


  public DemandStatus parties(@javax.annotation.Nullable List<DemandStatusPartiesInner> parties) {
    this.parties = parties;
    return this;
  }

  public DemandStatus addPartiesItem(DemandStatusPartiesInner partiesItem) {
    if (this.parties == null) {
      this.parties = new ArrayList<>();
    }
    this.parties.add(partiesItem);
    return this;
  }

  /**
   * Get parties
   * @return parties
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_PARTIES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<DemandStatusPartiesInner> getParties() {
    return parties;
  }


  @JsonProperty(value = JSON_PROPERTY_PARTIES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setParties(@javax.annotation.Nullable List<DemandStatusPartiesInner> parties) {
    this.parties = parties;
  }


  public DemandStatus resultUrl(@javax.annotation.Nullable URI resultUrl) {
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


  public DemandStatus pdfUrl(@javax.annotation.Nullable URI pdfUrl) {
    this.pdfUrl = JsonNullable.<URI>of(pdfUrl);
    return this;
  }

  /**
   * Sadece status&#x3D;COMPLETED iken dolu
   * @return pdfUrl
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public URI getPdfUrl() {
        return pdfUrl.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_PDF_URL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<URI> getPdfUrl_JsonNullable() {
    return pdfUrl;
  }
  
  @JsonProperty(JSON_PROPERTY_PDF_URL)
  public void setPdfUrl_JsonNullable(JsonNullable<URI> pdfUrl) {
    this.pdfUrl = pdfUrl;
  }

  public void setPdfUrl(@javax.annotation.Nullable URI pdfUrl) {
    this.pdfUrl = JsonNullable.<URI>of(pdfUrl);
  }


  /**
   * Return true if this DemandStatus object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DemandStatus demandStatus = (DemandStatus) o;
    return Objects.equals(this.id, demandStatus.id) &&
        Objects.equals(this.title, demandStatus.title) &&
        Objects.equals(this.status, demandStatus.status) &&
        Objects.equals(this.createdAt, demandStatus.createdAt) &&
        equalsNullable(this.completedAt, demandStatus.completedAt) &&
        Objects.equals(this.parties, demandStatus.parties) &&
        Objects.equals(this.resultUrl, demandStatus.resultUrl) &&
        equalsNullable(this.pdfUrl, demandStatus.pdfUrl);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, status, createdAt, hashCodeNullable(completedAt), parties, resultUrl, hashCodeNullable(pdfUrl));
  }

  private static <T> int hashCodeNullable(JsonNullable<T> a) {
    if (a == null) {
      return 1;
    }
    return a.isPresent() ? Arrays.deepHashCode(new Object[]{a.get()}) : 31;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DemandStatus {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    completedAt: ").append(toIndentedString(completedAt)).append("\n");
    sb.append("    parties: ").append(toIndentedString(parties)).append("\n");
    sb.append("    resultUrl: ").append(toIndentedString(resultUrl)).append("\n");
    sb.append("    pdfUrl: ").append(toIndentedString(pdfUrl)).append("\n");
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

    // add `id` to the URL query string
    if (getId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sid%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
    }

    // add `title` to the URL query string
    if (getTitle() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%stitle%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTitle()))));
    }

    // add `status` to the URL query string
    if (getStatus() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sstatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
    }

    // add `created_at` to the URL query string
    if (getCreatedAt() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%screated_at%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCreatedAt()))));
    }

    // add `completed_at` to the URL query string
    if (getCompletedAt() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%scompleted_at%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCompletedAt()))));
    }

    // add `parties` to the URL query string
    if (getParties() != null) {
      for (int i = 0; i < getParties().size(); i++) {
        if (getParties().get(i) != null) {
          joiner.add(getParties().get(i).toUrlQueryString(String.format(java.util.Locale.ROOT, "%sparties%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `result_url` to the URL query string
    if (getResultUrl() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sresult_url%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getResultUrl()))));
    }

    // add `pdf_url` to the URL query string
    if (getPdfUrl() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%spdf_url%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPdfUrl()))));
    }

    return joiner.toString();
  }
}

