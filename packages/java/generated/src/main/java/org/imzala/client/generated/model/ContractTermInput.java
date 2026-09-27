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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * Sözleşme süre/yenileme takibi girdisi. &#x60;POST /demands&#x60; üzerinde oluştururken ve &#x60;PATCH /demands/{id}/term&#x60; üzerinde güncellerken AYNI alan adları kullanılır. &#x60;PATCH&#x60;&#39;te kısmi güncelleme uygulanır: yalnız gövdede GÖNDERİLEN anahtarlar değiştirilir; bir anahtarı &#x60;null&#x60; göndermek o alanı temizler, hiç göndermemek dokunmaz.  Doğrulama kuralları: - &#x60;term_fixed_end_date&#x60; ile &#x60;term_duration_months&#x60; birlikte gönderilemez. - &#x60;term_start_mode: FIXED_DATE&#x60; ise oluştururken &#x60;term_start_date&#x60; ya   da &#x60;term_fixed_end_date&#x60; gerekir; &#x60;ON_FIRST_SIGNATURE&#x60; /   &#x60;ON_COMPLETION&#x60; modlarında &#x60;term_start_date&#x60; gönderilse de yok   sayılır (başlangıç imza anında otomatik yazılır). - &#x60;renewal_type: AUTO_RENEW&#x60; ise &#x60;renewal_period_months&#x60; gerekir;   gönderilmezse &#x60;term_duration_months&#x60;&#39;tan devralınır. - Geçersiz kombinasyon 400 &#x60;TERM_INVALID&#x60; + hangi alanı işaret eden   &#x60;field&#x60; ile döner. 
 */
@JsonPropertyOrder({
  ContractTermInput.JSON_PROPERTY_TERM_START_MODE,
  ContractTermInput.JSON_PROPERTY_TERM_START_DATE,
  ContractTermInput.JSON_PROPERTY_TERM_DURATION_MONTHS,
  ContractTermInput.JSON_PROPERTY_TERM_FIXED_END_DATE,
  ContractTermInput.JSON_PROPERTY_RENEWAL_TYPE,
  ContractTermInput.JSON_PROPERTY_RENEWAL_PERIOD_MONTHS,
  ContractTermInput.JSON_PROPERTY_NOTICE_DAYS,
  ContractTermInput.JSON_PROPERTY_REMINDER_OFFSETS,
  ContractTermInput.JSON_PROPERTY_NOTIFY_COUNTERPARTY
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-09-28T01:16:51.997481+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class ContractTermInput {
  /**
   * Gets or Sets termStartMode
   */
  public enum TermStartModeEnum {
    FIXED_DATE(String.valueOf("FIXED_DATE")),
    
    ON_FIRST_SIGNATURE(String.valueOf("ON_FIRST_SIGNATURE")),
    
    ON_COMPLETION(String.valueOf("ON_COMPLETION"));

    private String value;

    TermStartModeEnum(String value) {
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
    public static TermStartModeEnum fromValue(String value) {
      for (TermStartModeEnum b : TermStartModeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return null;
    }
  }

  public static final String JSON_PROPERTY_TERM_START_MODE = "term_start_mode";
  private JsonNullable<TermStartModeEnum> termStartMode = JsonNullable.<TermStartModeEnum>undefined();

  public static final String JSON_PROPERTY_TERM_START_DATE = "term_start_date";
  private JsonNullable<LocalDate> termStartDate = JsonNullable.<LocalDate>undefined();

  public static final String JSON_PROPERTY_TERM_DURATION_MONTHS = "term_duration_months";
  private JsonNullable<Integer> termDurationMonths = JsonNullable.<Integer>undefined();

  public static final String JSON_PROPERTY_TERM_FIXED_END_DATE = "term_fixed_end_date";
  private JsonNullable<LocalDate> termFixedEndDate = JsonNullable.<LocalDate>undefined();

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
  private JsonNullable<RenewalTypeEnum> renewalType = JsonNullable.<RenewalTypeEnum>undefined();

  public static final String JSON_PROPERTY_RENEWAL_PERIOD_MONTHS = "renewal_period_months";
  private JsonNullable<Integer> renewalPeriodMonths = JsonNullable.<Integer>undefined();

  public static final String JSON_PROPERTY_NOTICE_DAYS = "notice_days";
  private JsonNullable<Integer> noticeDays = JsonNullable.<Integer>undefined();

  public static final String JSON_PROPERTY_REMINDER_OFFSETS = "reminder_offsets";
  private JsonNullable<List<Integer>> reminderOffsets = JsonNullable.<List<Integer>>undefined();

  public static final String JSON_PROPERTY_NOTIFY_COUNTERPARTY = "notify_counterparty";
  @javax.annotation.Nullable
  private Boolean notifyCounterparty;

  public ContractTermInput() { 
  }

  public ContractTermInput termStartMode(@javax.annotation.Nullable TermStartModeEnum termStartMode) {
    this.termStartMode = JsonNullable.<TermStartModeEnum>of(termStartMode);
    return this;
  }

  /**
   * Get termStartMode
   * @return termStartMode
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public TermStartModeEnum getTermStartMode() {
        return termStartMode.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_TERM_START_MODE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<TermStartModeEnum> getTermStartMode_JsonNullable() {
    return termStartMode;
  }
  
  @JsonProperty(JSON_PROPERTY_TERM_START_MODE)
  public void setTermStartMode_JsonNullable(JsonNullable<TermStartModeEnum> termStartMode) {
    this.termStartMode = termStartMode;
  }

  public void setTermStartMode(@javax.annotation.Nullable TermStartModeEnum termStartMode) {
    this.termStartMode = JsonNullable.<TermStartModeEnum>of(termStartMode);
  }


  public ContractTermInput termStartDate(@javax.annotation.Nullable LocalDate termStartDate) {
    this.termStartDate = JsonNullable.<LocalDate>of(termStartDate);
    return this;
  }

  /**
   * Yalnız &#x60;term_start_mode: FIXED_DATE&#x60; iken kullanılır.
   * @return termStartDate
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public LocalDate getTermStartDate() {
        return termStartDate.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_TERM_START_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<LocalDate> getTermStartDate_JsonNullable() {
    return termStartDate;
  }
  
  @JsonProperty(JSON_PROPERTY_TERM_START_DATE)
  public void setTermStartDate_JsonNullable(JsonNullable<LocalDate> termStartDate) {
    this.termStartDate = termStartDate;
  }

  public void setTermStartDate(@javax.annotation.Nullable LocalDate termStartDate) {
    this.termStartDate = JsonNullable.<LocalDate>of(termStartDate);
  }


  public ContractTermInput termDurationMonths(@javax.annotation.Nullable Integer termDurationMonths) {
    this.termDurationMonths = JsonNullable.<Integer>of(termDurationMonths);
    return this;
  }

  /**
   * Get termDurationMonths
   * minimum: 1
   * maximum: 600
   * @return termDurationMonths
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public Integer getTermDurationMonths() {
        return termDurationMonths.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_TERM_DURATION_MONTHS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<Integer> getTermDurationMonths_JsonNullable() {
    return termDurationMonths;
  }
  
  @JsonProperty(JSON_PROPERTY_TERM_DURATION_MONTHS)
  public void setTermDurationMonths_JsonNullable(JsonNullable<Integer> termDurationMonths) {
    this.termDurationMonths = termDurationMonths;
  }

  public void setTermDurationMonths(@javax.annotation.Nullable Integer termDurationMonths) {
    this.termDurationMonths = JsonNullable.<Integer>of(termDurationMonths);
  }


  public ContractTermInput termFixedEndDate(@javax.annotation.Nullable LocalDate termFixedEndDate) {
    this.termFixedEndDate = JsonNullable.<LocalDate>of(termFixedEndDate);
    return this;
  }

  /**
   * Get termFixedEndDate
   * @return termFixedEndDate
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public LocalDate getTermFixedEndDate() {
        return termFixedEndDate.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_TERM_FIXED_END_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<LocalDate> getTermFixedEndDate_JsonNullable() {
    return termFixedEndDate;
  }
  
  @JsonProperty(JSON_PROPERTY_TERM_FIXED_END_DATE)
  public void setTermFixedEndDate_JsonNullable(JsonNullable<LocalDate> termFixedEndDate) {
    this.termFixedEndDate = termFixedEndDate;
  }

  public void setTermFixedEndDate(@javax.annotation.Nullable LocalDate termFixedEndDate) {
    this.termFixedEndDate = JsonNullable.<LocalDate>of(termFixedEndDate);
  }


  public ContractTermInput renewalType(@javax.annotation.Nullable RenewalTypeEnum renewalType) {
    this.renewalType = JsonNullable.<RenewalTypeEnum>of(renewalType);
    return this;
  }

  /**
   * Get renewalType
   * @return renewalType
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public RenewalTypeEnum getRenewalType() {
        return renewalType.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_RENEWAL_TYPE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<RenewalTypeEnum> getRenewalType_JsonNullable() {
    return renewalType;
  }
  
  @JsonProperty(JSON_PROPERTY_RENEWAL_TYPE)
  public void setRenewalType_JsonNullable(JsonNullable<RenewalTypeEnum> renewalType) {
    this.renewalType = renewalType;
  }

  public void setRenewalType(@javax.annotation.Nullable RenewalTypeEnum renewalType) {
    this.renewalType = JsonNullable.<RenewalTypeEnum>of(renewalType);
  }


  public ContractTermInput renewalPeriodMonths(@javax.annotation.Nullable Integer renewalPeriodMonths) {
    this.renewalPeriodMonths = JsonNullable.<Integer>of(renewalPeriodMonths);
    return this;
  }

  /**
   * Get renewalPeriodMonths
   * minimum: 1
   * maximum: 600
   * @return renewalPeriodMonths
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public Integer getRenewalPeriodMonths() {
        return renewalPeriodMonths.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_RENEWAL_PERIOD_MONTHS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<Integer> getRenewalPeriodMonths_JsonNullable() {
    return renewalPeriodMonths;
  }
  
  @JsonProperty(JSON_PROPERTY_RENEWAL_PERIOD_MONTHS)
  public void setRenewalPeriodMonths_JsonNullable(JsonNullable<Integer> renewalPeriodMonths) {
    this.renewalPeriodMonths = renewalPeriodMonths;
  }

  public void setRenewalPeriodMonths(@javax.annotation.Nullable Integer renewalPeriodMonths) {
    this.renewalPeriodMonths = JsonNullable.<Integer>of(renewalPeriodMonths);
  }


  public ContractTermInput noticeDays(@javax.annotation.Nullable Integer noticeDays) {
    this.noticeDays = JsonNullable.<Integer>of(noticeDays);
    return this;
  }

  /**
   * Get noticeDays
   * minimum: 0
   * maximum: 3650
   * @return noticeDays
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public Integer getNoticeDays() {
        return noticeDays.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_NOTICE_DAYS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<Integer> getNoticeDays_JsonNullable() {
    return noticeDays;
  }
  
  @JsonProperty(JSON_PROPERTY_NOTICE_DAYS)
  public void setNoticeDays_JsonNullable(JsonNullable<Integer> noticeDays) {
    this.noticeDays = noticeDays;
  }

  public void setNoticeDays(@javax.annotation.Nullable Integer noticeDays) {
    this.noticeDays = JsonNullable.<Integer>of(noticeDays);
  }


  public ContractTermInput reminderOffsets(@javax.annotation.Nullable List<Integer> reminderOffsets) {
    this.reminderOffsets = JsonNullable.<List<Integer>>of(reminderOffsets);
    return this;
  }

  public ContractTermInput addReminderOffsetsItem(Integer reminderOffsetsItem) {
    if (this.reminderOffsets == null || !this.reminderOffsets.isPresent()) {
      this.reminderOffsets = JsonNullable.<List<Integer>>of(new ArrayList<>());
    }
    try {
      this.reminderOffsets.get().add(reminderOffsetsItem);
    } catch (java.util.NoSuchElementException e) {
      // this can never happen, as we make sure above that the value is present
    }
    return this;
  }

  /**
   * Boş dizi göndermek şablon/önceki değeri SİLMEZ (yok sayılır); temizlemek için &#x60;null&#x60; gönderin. 
   * @return reminderOffsets
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public List<Integer> getReminderOffsets() {
        return reminderOffsets.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_REMINDER_OFFSETS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<List<Integer>> getReminderOffsets_JsonNullable() {
    return reminderOffsets;
  }
  
  @JsonProperty(JSON_PROPERTY_REMINDER_OFFSETS)
  public void setReminderOffsets_JsonNullable(JsonNullable<List<Integer>> reminderOffsets) {
    this.reminderOffsets = reminderOffsets;
  }

  public void setReminderOffsets(@javax.annotation.Nullable List<Integer> reminderOffsets) {
    this.reminderOffsets = JsonNullable.<List<Integer>>of(reminderOffsets);
  }


  public ContractTermInput notifyCounterparty(@javax.annotation.Nullable Boolean notifyCounterparty) {
    this.notifyCounterparty = notifyCounterparty;
    return this;
  }

  /**
   * Get notifyCounterparty
   * @return notifyCounterparty
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_NOTIFY_COUNTERPARTY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getNotifyCounterparty() {
    return notifyCounterparty;
  }


  @JsonProperty(value = JSON_PROPERTY_NOTIFY_COUNTERPARTY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setNotifyCounterparty(@javax.annotation.Nullable Boolean notifyCounterparty) {
    this.notifyCounterparty = notifyCounterparty;
  }


  /**
   * Return true if this ContractTermInput object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContractTermInput contractTermInput = (ContractTermInput) o;
    return equalsNullable(this.termStartMode, contractTermInput.termStartMode) &&
        equalsNullable(this.termStartDate, contractTermInput.termStartDate) &&
        equalsNullable(this.termDurationMonths, contractTermInput.termDurationMonths) &&
        equalsNullable(this.termFixedEndDate, contractTermInput.termFixedEndDate) &&
        equalsNullable(this.renewalType, contractTermInput.renewalType) &&
        equalsNullable(this.renewalPeriodMonths, contractTermInput.renewalPeriodMonths) &&
        equalsNullable(this.noticeDays, contractTermInput.noticeDays) &&
        equalsNullable(this.reminderOffsets, contractTermInput.reminderOffsets) &&
        Objects.equals(this.notifyCounterparty, contractTermInput.notifyCounterparty);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(hashCodeNullable(termStartMode), hashCodeNullable(termStartDate), hashCodeNullable(termDurationMonths), hashCodeNullable(termFixedEndDate), hashCodeNullable(renewalType), hashCodeNullable(renewalPeriodMonths), hashCodeNullable(noticeDays), hashCodeNullable(reminderOffsets), notifyCounterparty);
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
    sb.append("class ContractTermInput {\n");
    sb.append("    termStartMode: ").append(toIndentedString(termStartMode)).append("\n");
    sb.append("    termStartDate: ").append(toIndentedString(termStartDate)).append("\n");
    sb.append("    termDurationMonths: ").append(toIndentedString(termDurationMonths)).append("\n");
    sb.append("    termFixedEndDate: ").append(toIndentedString(termFixedEndDate)).append("\n");
    sb.append("    renewalType: ").append(toIndentedString(renewalType)).append("\n");
    sb.append("    renewalPeriodMonths: ").append(toIndentedString(renewalPeriodMonths)).append("\n");
    sb.append("    noticeDays: ").append(toIndentedString(noticeDays)).append("\n");
    sb.append("    reminderOffsets: ").append(toIndentedString(reminderOffsets)).append("\n");
    sb.append("    notifyCounterparty: ").append(toIndentedString(notifyCounterparty)).append("\n");
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

    // add `term_start_mode` to the URL query string
    if (getTermStartMode() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sterm_start_mode%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTermStartMode()))));
    }

    // add `term_start_date` to the URL query string
    if (getTermStartDate() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sterm_start_date%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTermStartDate()))));
    }

    // add `term_duration_months` to the URL query string
    if (getTermDurationMonths() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sterm_duration_months%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTermDurationMonths()))));
    }

    // add `term_fixed_end_date` to the URL query string
    if (getTermFixedEndDate() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sterm_fixed_end_date%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTermFixedEndDate()))));
    }

    // add `renewal_type` to the URL query string
    if (getRenewalType() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%srenewal_type%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRenewalType()))));
    }

    // add `renewal_period_months` to the URL query string
    if (getRenewalPeriodMonths() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%srenewal_period_months%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRenewalPeriodMonths()))));
    }

    // add `notice_days` to the URL query string
    if (getNoticeDays() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%snotice_days%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getNoticeDays()))));
    }

    // add `reminder_offsets` to the URL query string
    if (getReminderOffsets() != null) {
      for (int i = 0; i < getReminderOffsets().size(); i++) {
        joiner.add(String.format(java.util.Locale.ROOT, "%sreminder_offsets%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getReminderOffsets().get(i)))));
      }
    }

    // add `notify_counterparty` to the URL query string
    if (getNotifyCounterparty() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%snotify_counterparty%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getNotifyCounterparty()))));
    }

    return joiner.toString();
  }
}

