/*
 * imzala External API
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.9.1 · **Son güncelleme:** 2026-09-27  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır (aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (11) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı | | `contract.expiring` | Takip edilen sözleşme bitiş tarihine yaklaşıyor | | `contract.ended` | Takip edilen sözleşmenin platformdaki bitiş tarihi geçti | | `contract.advanced` | Otomatik yenilenen sözleşmenin takip edilen bitişi ileri alındı |  > **Not (contract.\\* olayları):** Bunlar platformun **takip amaçlı** > kayıtlarıdır; sözleşmenin hukuken yenilendiğini veya sona erdiğini > BELİRTMEZ. Detay için `ContractTerm` şemasına ve `state` alanına bakın.  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type`: yukarıdaki 11 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.9.1
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * &#x60;contract.expiring&#x60; / &#x60;contract.ended&#x60; / &#x60;contract.advanced&#x60; olaylarının ortak verisi. Yalnız takip alanlarını taşır; taraf adı, e-postası veya sözleşme başlığı BU GÖVDEDE YER ALMAZ. 
 */
@JsonPropertyOrder({
  WebhookDataContractTerm.JSON_PROPERTY_DEMAND_ID,
  WebhookDataContractTerm.JSON_PROPERTY_TERM_END_DATE,
  WebhookDataContractTerm.JSON_PROPERTY_RENEWAL_TYPE,
  WebhookDataContractTerm.JSON_PROPERTY_NOTICE_DEADLINE,
  WebhookDataContractTerm.JSON_PROPERTY_TERM_STATE,
  WebhookDataContractTerm.JSON_PROPERTY_DUE_KEY
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-09-28T01:16:51.997481+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class WebhookDataContractTerm {
  public static final String JSON_PROPERTY_DEMAND_ID = "demand_id";
  @javax.annotation.Nonnull
  private UUID demandId;

  public static final String JSON_PROPERTY_TERM_END_DATE = "term_end_date";
  @javax.annotation.Nullable
  private LocalDate termEndDate;

  /**
   * Gets or Sets renewalType
   */
  public enum RenewalTypeEnum {
    AUTO_RENEW(String.valueOf("AUTO_RENEW")),
    
    FIXED_TERM(String.valueOf("FIXED_TERM"));

    private String value;

    RenewalTypeEnum(String value) {
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
    public static RenewalTypeEnum fromValue(String value) {
      for (RenewalTypeEnum b : RenewalTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return null;
    }
  }

  public static final String JSON_PROPERTY_RENEWAL_TYPE = "renewal_type";
  @javax.annotation.Nullable
  private RenewalTypeEnum renewalType;

  public static final String JSON_PROPERTY_NOTICE_DEADLINE = "notice_deadline";
  @javax.annotation.Nullable
  private LocalDate noticeDeadline;

  /**
   * Olay anındaki takip durumu (bkz. &#x60;ContractTerm.state&#x60;).
   */
  public enum TermStateEnum {
    UNTRACKED(String.valueOf("UNTRACKED")),
    
    ACTIVE(String.valueOf("ACTIVE")),
    
    ENDED(String.valueOf("ENDED")),
    
    STOPPED(String.valueOf("STOPPED"));

    private String value;

    TermStateEnum(String value) {
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
    public static TermStateEnum fromValue(String value) {
      for (TermStateEnum b : TermStateEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_TERM_STATE = "term_state";
  @javax.annotation.Nonnull
  private TermStateEnum termState;

  public static final String JSON_PROPERTY_DUE_KEY = "due_key";
  @javax.annotation.Nullable
  private String dueKey;

  public WebhookDataContractTerm() { 
  }

  public WebhookDataContractTerm demandId(@javax.annotation.Nonnull UUID demandId) {
    this.demandId = demandId;
    return this;
  }

  /**
   * Get demandId
   * @return demandId
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_DEMAND_ID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public UUID getDemandId() {
    return demandId;
  }


  @JsonProperty(value = JSON_PROPERTY_DEMAND_ID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDemandId(@javax.annotation.Nonnull UUID demandId) {
    this.demandId = demandId;
  }


  public WebhookDataContractTerm termEndDate(@javax.annotation.Nullable LocalDate termEndDate) {
    this.termEndDate = termEndDate;
    return this;
  }

  /**
   * Olay anında platformda kayıtlı takip bitiş tarihi.
   * @return termEndDate
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_TERM_END_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public LocalDate getTermEndDate() {
    return termEndDate;
  }


  @JsonProperty(value = JSON_PROPERTY_TERM_END_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTermEndDate(@javax.annotation.Nullable LocalDate termEndDate) {
    this.termEndDate = termEndDate;
  }


  public WebhookDataContractTerm renewalType(@javax.annotation.Nullable RenewalTypeEnum renewalType) {
    this.renewalType = renewalType;
    return this;
  }

  /**
   * Get renewalType
   * @return renewalType
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_RENEWAL_TYPE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public RenewalTypeEnum getRenewalType() {
    return renewalType;
  }


  @JsonProperty(value = JSON_PROPERTY_RENEWAL_TYPE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRenewalType(@javax.annotation.Nullable RenewalTypeEnum renewalType) {
    this.renewalType = renewalType;
  }


  public WebhookDataContractTerm noticeDeadline(@javax.annotation.Nullable LocalDate noticeDeadline) {
    this.noticeDeadline = noticeDeadline;
    return this;
  }

  /**
   * İhbar için son gün (&#x60;term_end_date - notice_days&#x60;); tanımsızsa &#x60;null&#x60;.
   * @return noticeDeadline
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_NOTICE_DEADLINE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public LocalDate getNoticeDeadline() {
    return noticeDeadline;
  }


  @JsonProperty(value = JSON_PROPERTY_NOTICE_DEADLINE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNoticeDeadline(@javax.annotation.Nullable LocalDate noticeDeadline) {
    this.noticeDeadline = noticeDeadline;
  }


  public WebhookDataContractTerm termState(@javax.annotation.Nonnull TermStateEnum termState) {
    this.termState = termState;
    return this;
  }

  /**
   * Olay anındaki takip durumu (bkz. &#x60;ContractTerm.state&#x60;).
   * @return termState
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_TERM_STATE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public TermStateEnum getTermState() {
    return termState;
  }


  @JsonProperty(value = JSON_PROPERTY_TERM_STATE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTermState(@javax.annotation.Nonnull TermStateEnum termState) {
    this.termState = termState;
  }


  public WebhookDataContractTerm dueKey(@javax.annotation.Nullable String dueKey) {
    this.dueKey = dueKey;
    return this;
  }

  /**
   * İşçinin tekillik anahtarı (aynı olayı tekrar göndermemek için kullandığı dahili kayıt). Formatı sabit değildir, opak string olarak ele alın; idempotency için &#x60;id&#x60; alanını kullanın. 
   * @return dueKey
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DUE_KEY, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDueKey() {
    return dueKey;
  }


  @JsonProperty(value = JSON_PROPERTY_DUE_KEY, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDueKey(@javax.annotation.Nullable String dueKey) {
    this.dueKey = dueKey;
  }


  /**
   * Return true if this WebhookDataContractTerm object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WebhookDataContractTerm webhookDataContractTerm = (WebhookDataContractTerm) o;
    return Objects.equals(this.demandId, webhookDataContractTerm.demandId) &&
        Objects.equals(this.termEndDate, webhookDataContractTerm.termEndDate) &&
        Objects.equals(this.renewalType, webhookDataContractTerm.renewalType) &&
        Objects.equals(this.noticeDeadline, webhookDataContractTerm.noticeDeadline) &&
        Objects.equals(this.termState, webhookDataContractTerm.termState) &&
        Objects.equals(this.dueKey, webhookDataContractTerm.dueKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(demandId, termEndDate, renewalType, noticeDeadline, termState, dueKey);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WebhookDataContractTerm {\n");
    sb.append("    demandId: ").append(toIndentedString(demandId)).append("\n");
    sb.append("    termEndDate: ").append(toIndentedString(termEndDate)).append("\n");
    sb.append("    renewalType: ").append(toIndentedString(renewalType)).append("\n");
    sb.append("    noticeDeadline: ").append(toIndentedString(noticeDeadline)).append("\n");
    sb.append("    termState: ").append(toIndentedString(termState)).append("\n");
    sb.append("    dueKey: ").append(toIndentedString(dueKey)).append("\n");
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

    // add `demand_id` to the URL query string
    if (getDemandId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sdemand_id%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDemandId()))));
    }

    // add `term_end_date` to the URL query string
    if (getTermEndDate() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sterm_end_date%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTermEndDate()))));
    }

    // add `renewal_type` to the URL query string
    if (getRenewalType() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%srenewal_type%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRenewalType()))));
    }

    // add `notice_deadline` to the URL query string
    if (getNoticeDeadline() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%snotice_deadline%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getNoticeDeadline()))));
    }

    // add `term_state` to the URL query string
    if (getTermState() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sterm_state%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTermState()))));
    }

    // add `due_key` to the URL query string
    if (getDueKey() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sdue_key%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDueKey()))));
    }

    return joiner.toString();
  }
}

