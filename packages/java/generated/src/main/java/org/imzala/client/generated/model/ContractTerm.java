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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * Sözleşme süre/yenileme takibi. Sözleşmede takip hiç tanımlanmadıysa (&#x60;start_mode&#x60; VE &#x60;end_date&#x60; boşsa) bu alan &#x60;null&#x60; döner.  Bu, hukuki bir yenileme veya sona erme beyanı değildir; platformun takip amaçlı kaydıdır. Sözleşmenin fiilen ne zaman sona erdiği veya yenilendiği, tarafların kendi sözleşme hükümlerine ve yürürlükteki hukuka tabidir. Bu alan yalnız hatırlatma ve görünürlük sağlar. 
 */
@JsonPropertyOrder({
  ContractTerm.JSON_PROPERTY_START_MODE,
  ContractTerm.JSON_PROPERTY_START_DATE,
  ContractTerm.JSON_PROPERTY_DURATION_MONTHS,
  ContractTerm.JSON_PROPERTY_FIXED_END_DATE,
  ContractTerm.JSON_PROPERTY_END_DATE,
  ContractTerm.JSON_PROPERTY_END_DATE_SIGNED,
  ContractTerm.JSON_PROPERTY_RENEWAL_TYPE,
  ContractTerm.JSON_PROPERTY_RENEWAL_PERIOD_MONTHS,
  ContractTerm.JSON_PROPERTY_NOTICE_DAYS,
  ContractTerm.JSON_PROPERTY_NOTICE_DEADLINE,
  ContractTerm.JSON_PROPERTY_REMINDER_OFFSETS,
  ContractTerm.JSON_PROPERTY_NOTIFY_COUNTERPARTY,
  ContractTerm.JSON_PROPERTY_STATE,
  ContractTerm.JSON_PROPERTY_DAYS_LEFT,
  ContractTerm.JSON_PROPERTY_RENEWAL_STOPPED_AT
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-09-28T01:11:50.840889+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class ContractTerm {
  /**
   * Takibin ne zaman başladığı. &#x60;FIXED_DATE&#x60;: &#x60;start_date&#x60;&#39;ten. &#x60;ON_FIRST_SIGNATURE&#x60; / &#x60;ON_COMPLETION&#x60;: ilk imza / tüm imzalar tamamlandığı anda otomatik (o ana kadar &#x60;end_date&#x60; bilinmez). 
   */
  public enum StartModeEnum {
    FIXED_DATE(String.valueOf("FIXED_DATE")),
    
    ON_FIRST_SIGNATURE(String.valueOf("ON_FIRST_SIGNATURE")),
    
    ON_COMPLETION(String.valueOf("ON_COMPLETION"));

    private String value;

    StartModeEnum(String value) {
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
    public static StartModeEnum fromValue(String value) {
      for (StartModeEnum b : StartModeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return null;
    }
  }

  public static final String JSON_PROPERTY_START_MODE = "start_mode";
  @javax.annotation.Nullable
  private StartModeEnum startMode;

  public static final String JSON_PROPERTY_START_DATE = "start_date";
  @javax.annotation.Nullable
  private LocalDate startDate;

  public static final String JSON_PROPERTY_DURATION_MONTHS = "duration_months";
  @javax.annotation.Nullable
  private Integer durationMonths;

  public static final String JSON_PROPERTY_FIXED_END_DATE = "fixed_end_date";
  @javax.annotation.Nullable
  private LocalDate fixedEndDate;

  public static final String JSON_PROPERTY_END_DATE = "end_date";
  @javax.annotation.Nullable
  private LocalDate endDate;

  public static final String JSON_PROPERTY_END_DATE_SIGNED = "end_date_signed";
  @javax.annotation.Nullable
  private LocalDate endDateSigned;

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

  public static final String JSON_PROPERTY_RENEWAL_PERIOD_MONTHS = "renewal_period_months";
  @javax.annotation.Nullable
  private Integer renewalPeriodMonths;

  public static final String JSON_PROPERTY_NOTICE_DAYS = "notice_days";
  @javax.annotation.Nullable
  private Integer noticeDays;

  public static final String JSON_PROPERTY_NOTICE_DEADLINE = "notice_deadline";
  @javax.annotation.Nullable
  private LocalDate noticeDeadline;

  public static final String JSON_PROPERTY_REMINDER_OFFSETS = "reminder_offsets";
  @javax.annotation.Nullable
  private List<Integer> reminderOffsets;

  public static final String JSON_PROPERTY_NOTIFY_COUNTERPARTY = "notify_counterparty";
  @javax.annotation.Nonnull
  private Boolean notifyCounterparty;

  /**
   * &#x60;UNTRACKED&#x60;: takip yok (&#x60;end_date&#x60; bilinmiyor). &#x60;ACTIVE&#x60;: takip sürüyor. &#x60;ENDED&#x60;: &#x60;FIXED_TERM&#x60; sözleşmenin bitiş tarihi geçti. &#x60;STOPPED&#x60;: yenileme takibi kullanıcı tarafından durduruldu. 
   */
  public enum StateEnum {
    UNTRACKED(String.valueOf("UNTRACKED")),
    
    ACTIVE(String.valueOf("ACTIVE")),
    
    ENDED(String.valueOf("ENDED")),
    
    STOPPED(String.valueOf("STOPPED"));

    private String value;

    StateEnum(String value) {
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
    public static StateEnum fromValue(String value) {
      for (StateEnum b : StateEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_STATE = "state";
  @javax.annotation.Nonnull
  private StateEnum state;

  public static final String JSON_PROPERTY_DAYS_LEFT = "days_left";
  @javax.annotation.Nullable
  private Integer daysLeft;

  public static final String JSON_PROPERTY_RENEWAL_STOPPED_AT = "renewal_stopped_at";
  @javax.annotation.Nullable
  private OffsetDateTime renewalStoppedAt;

  public ContractTerm() { 
  }

  public ContractTerm startMode(@javax.annotation.Nullable StartModeEnum startMode) {
    this.startMode = startMode;
    return this;
  }

  /**
   * Takibin ne zaman başladığı. &#x60;FIXED_DATE&#x60;: &#x60;start_date&#x60;&#39;ten. &#x60;ON_FIRST_SIGNATURE&#x60; / &#x60;ON_COMPLETION&#x60;: ilk imza / tüm imzalar tamamlandığı anda otomatik (o ana kadar &#x60;end_date&#x60; bilinmez). 
   * @return startMode
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_START_MODE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public StartModeEnum getStartMode() {
    return startMode;
  }


  @JsonProperty(value = JSON_PROPERTY_START_MODE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setStartMode(@javax.annotation.Nullable StartModeEnum startMode) {
    this.startMode = startMode;
  }


  public ContractTerm startDate(@javax.annotation.Nullable LocalDate startDate) {
    this.startDate = startDate;
    return this;
  }

  /**
   * Get startDate
   * @return startDate
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_START_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public LocalDate getStartDate() {
    return startDate;
  }


  @JsonProperty(value = JSON_PROPERTY_START_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setStartDate(@javax.annotation.Nullable LocalDate startDate) {
    this.startDate = startDate;
  }


  public ContractTerm durationMonths(@javax.annotation.Nullable Integer durationMonths) {
    this.durationMonths = durationMonths;
    return this;
  }

  /**
   * Get durationMonths
   * minimum: 1
   * maximum: 600
   * @return durationMonths
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DURATION_MONTHS, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getDurationMonths() {
    return durationMonths;
  }


  @JsonProperty(value = JSON_PROPERTY_DURATION_MONTHS, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDurationMonths(@javax.annotation.Nullable Integer durationMonths) {
    this.durationMonths = durationMonths;
  }


  public ContractTerm fixedEndDate(@javax.annotation.Nullable LocalDate fixedEndDate) {
    this.fixedEndDate = fixedEndDate;
    return this;
  }

  /**
   * Get fixedEndDate
   * @return fixedEndDate
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_FIXED_END_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public LocalDate getFixedEndDate() {
    return fixedEndDate;
  }


  @JsonProperty(value = JSON_PROPERTY_FIXED_END_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setFixedEndDate(@javax.annotation.Nullable LocalDate fixedEndDate) {
    this.fixedEndDate = fixedEndDate;
  }


  public ContractTerm endDate(@javax.annotation.Nullable LocalDate endDate) {
    this.endDate = endDate;
    return this;
  }

  /**
   * Takip edilen bitiş tarihi. &#x60;renewal_type: AUTO_RENEW&#x60; sözleşmelerde her dönemde (günlük olarak) bir sonraki döneme ileri alınır; bkz. &#x60;contract.advanced&#x60; webhook olayı. 
   * @return endDate
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_END_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public LocalDate getEndDate() {
    return endDate;
  }


  @JsonProperty(value = JSON_PROPERTY_END_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEndDate(@javax.annotation.Nullable LocalDate endDate) {
    this.endDate = endDate;
  }


  public ContractTerm endDateSigned(@javax.annotation.Nullable LocalDate endDateSigned) {
    this.endDateSigned = endDateSigned;
    return this;
  }

  /**
   * İmzalanan belge metninde bitiş tarihi bir değişken olarak yazılıysa, sözleşme tamamlandığı anda &#x60;end_date&#x60;&#39;in dondurulmuş kopyası. &#x60;end_date&#x60; sonradan ileri alınsa bile bu alan DEĞİŞMEZ. İmzalanan metinle takip değeri arasındaki tarihsel referans budur. 
   * @return endDateSigned
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_END_DATE_SIGNED, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public LocalDate getEndDateSigned() {
    return endDateSigned;
  }


  @JsonProperty(value = JSON_PROPERTY_END_DATE_SIGNED, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEndDateSigned(@javax.annotation.Nullable LocalDate endDateSigned) {
    this.endDateSigned = endDateSigned;
  }


  public ContractTerm renewalType(@javax.annotation.Nullable RenewalTypeEnum renewalType) {
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


  public ContractTerm renewalPeriodMonths(@javax.annotation.Nullable Integer renewalPeriodMonths) {
    this.renewalPeriodMonths = renewalPeriodMonths;
    return this;
  }

  /**
   * Get renewalPeriodMonths
   * minimum: 1
   * maximum: 600
   * @return renewalPeriodMonths
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_RENEWAL_PERIOD_MONTHS, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getRenewalPeriodMonths() {
    return renewalPeriodMonths;
  }


  @JsonProperty(value = JSON_PROPERTY_RENEWAL_PERIOD_MONTHS, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRenewalPeriodMonths(@javax.annotation.Nullable Integer renewalPeriodMonths) {
    this.renewalPeriodMonths = renewalPeriodMonths;
  }


  public ContractTerm noticeDays(@javax.annotation.Nullable Integer noticeDays) {
    this.noticeDays = noticeDays;
    return this;
  }

  /**
   * Fesih ihbarı için bitişten önce kaç gün gerektiği.
   * minimum: 0
   * maximum: 3650
   * @return noticeDays
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_NOTICE_DAYS, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getNoticeDays() {
    return noticeDays;
  }


  @JsonProperty(value = JSON_PROPERTY_NOTICE_DAYS, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNoticeDays(@javax.annotation.Nullable Integer noticeDays) {
    this.noticeDays = noticeDays;
  }


  public ContractTerm noticeDeadline(@javax.annotation.Nullable LocalDate noticeDeadline) {
    this.noticeDeadline = noticeDeadline;
    return this;
  }

  /**
   * Hesaplanır (&#x60;end_date - notice_days&#x60;); &#x60;end_date&#x60; veya &#x60;notice_days&#x60; boşsa &#x60;null&#x60;.
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


  public ContractTerm reminderOffsets(@javax.annotation.Nullable List<Integer> reminderOffsets) {
    this.reminderOffsets = reminderOffsets;
    return this;
  }

  public ContractTerm addReminderOffsetsItem(Integer reminderOffsetsItem) {
    if (this.reminderOffsets == null) {
      this.reminderOffsets = new ArrayList<>();
    }
    this.reminderOffsets.add(reminderOffsetsItem);
    return this;
  }

  /**
   * Bitişten kaç gün önce hatırlatma gönderileceği (gün sayıları).
   * @return reminderOffsets
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_REMINDER_OFFSETS, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<Integer> getReminderOffsets() {
    return reminderOffsets;
  }


  @JsonProperty(value = JSON_PROPERTY_REMINDER_OFFSETS, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setReminderOffsets(@javax.annotation.Nullable List<Integer> reminderOffsets) {
    this.reminderOffsets = reminderOffsets;
  }


  public ContractTerm notifyCounterparty(@javax.annotation.Nonnull Boolean notifyCounterparty) {
    this.notifyCounterparty = notifyCounterparty;
    return this;
  }

  /**
   * Karşı tarafa da hatırlatma gönderilsin mi.
   * @return notifyCounterparty
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_NOTIFY_COUNTERPARTY, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getNotifyCounterparty() {
    return notifyCounterparty;
  }


  @JsonProperty(value = JSON_PROPERTY_NOTIFY_COUNTERPARTY, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNotifyCounterparty(@javax.annotation.Nonnull Boolean notifyCounterparty) {
    this.notifyCounterparty = notifyCounterparty;
  }


  public ContractTerm state(@javax.annotation.Nonnull StateEnum state) {
    this.state = state;
    return this;
  }

  /**
   * &#x60;UNTRACKED&#x60;: takip yok (&#x60;end_date&#x60; bilinmiyor). &#x60;ACTIVE&#x60;: takip sürüyor. &#x60;ENDED&#x60;: &#x60;FIXED_TERM&#x60; sözleşmenin bitiş tarihi geçti. &#x60;STOPPED&#x60;: yenileme takibi kullanıcı tarafından durduruldu. 
   * @return state
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_STATE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public StateEnum getState() {
    return state;
  }


  @JsonProperty(value = JSON_PROPERTY_STATE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setState(@javax.annotation.Nonnull StateEnum state) {
    this.state = state;
  }


  public ContractTerm daysLeft(@javax.annotation.Nullable Integer daysLeft) {
    this.daysLeft = daysLeft;
    return this;
  }

  /**
   * Bugünden &#x60;end_date&#x60;&#39;e gün sayısı (negatifse geçmişte).
   * @return daysLeft
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DAYS_LEFT, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getDaysLeft() {
    return daysLeft;
  }


  @JsonProperty(value = JSON_PROPERTY_DAYS_LEFT, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDaysLeft(@javax.annotation.Nullable Integer daysLeft) {
    this.daysLeft = daysLeft;
  }


  public ContractTerm renewalStoppedAt(@javax.annotation.Nullable OffsetDateTime renewalStoppedAt) {
    this.renewalStoppedAt = renewalStoppedAt;
    return this;
  }

  /**
   * Get renewalStoppedAt
   * @return renewalStoppedAt
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_RENEWAL_STOPPED_AT, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getRenewalStoppedAt() {
    return renewalStoppedAt;
  }


  @JsonProperty(value = JSON_PROPERTY_RENEWAL_STOPPED_AT, required = false)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRenewalStoppedAt(@javax.annotation.Nullable OffsetDateTime renewalStoppedAt) {
    this.renewalStoppedAt = renewalStoppedAt;
  }


  /**
   * Return true if this ContractTerm object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContractTerm contractTerm = (ContractTerm) o;
    return Objects.equals(this.startMode, contractTerm.startMode) &&
        Objects.equals(this.startDate, contractTerm.startDate) &&
        Objects.equals(this.durationMonths, contractTerm.durationMonths) &&
        Objects.equals(this.fixedEndDate, contractTerm.fixedEndDate) &&
        Objects.equals(this.endDate, contractTerm.endDate) &&
        Objects.equals(this.endDateSigned, contractTerm.endDateSigned) &&
        Objects.equals(this.renewalType, contractTerm.renewalType) &&
        Objects.equals(this.renewalPeriodMonths, contractTerm.renewalPeriodMonths) &&
        Objects.equals(this.noticeDays, contractTerm.noticeDays) &&
        Objects.equals(this.noticeDeadline, contractTerm.noticeDeadline) &&
        Objects.equals(this.reminderOffsets, contractTerm.reminderOffsets) &&
        Objects.equals(this.notifyCounterparty, contractTerm.notifyCounterparty) &&
        Objects.equals(this.state, contractTerm.state) &&
        Objects.equals(this.daysLeft, contractTerm.daysLeft) &&
        Objects.equals(this.renewalStoppedAt, contractTerm.renewalStoppedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startMode, startDate, durationMonths, fixedEndDate, endDate, endDateSigned, renewalType, renewalPeriodMonths, noticeDays, noticeDeadline, reminderOffsets, notifyCounterparty, state, daysLeft, renewalStoppedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ContractTerm {\n");
    sb.append("    startMode: ").append(toIndentedString(startMode)).append("\n");
    sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
    sb.append("    durationMonths: ").append(toIndentedString(durationMonths)).append("\n");
    sb.append("    fixedEndDate: ").append(toIndentedString(fixedEndDate)).append("\n");
    sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
    sb.append("    endDateSigned: ").append(toIndentedString(endDateSigned)).append("\n");
    sb.append("    renewalType: ").append(toIndentedString(renewalType)).append("\n");
    sb.append("    renewalPeriodMonths: ").append(toIndentedString(renewalPeriodMonths)).append("\n");
    sb.append("    noticeDays: ").append(toIndentedString(noticeDays)).append("\n");
    sb.append("    noticeDeadline: ").append(toIndentedString(noticeDeadline)).append("\n");
    sb.append("    reminderOffsets: ").append(toIndentedString(reminderOffsets)).append("\n");
    sb.append("    notifyCounterparty: ").append(toIndentedString(notifyCounterparty)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    daysLeft: ").append(toIndentedString(daysLeft)).append("\n");
    sb.append("    renewalStoppedAt: ").append(toIndentedString(renewalStoppedAt)).append("\n");
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

    // add `start_mode` to the URL query string
    if (getStartMode() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sstart_mode%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStartMode()))));
    }

    // add `start_date` to the URL query string
    if (getStartDate() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sstart_date%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStartDate()))));
    }

    // add `duration_months` to the URL query string
    if (getDurationMonths() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sduration_months%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDurationMonths()))));
    }

    // add `fixed_end_date` to the URL query string
    if (getFixedEndDate() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sfixed_end_date%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getFixedEndDate()))));
    }

    // add `end_date` to the URL query string
    if (getEndDate() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%send_date%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEndDate()))));
    }

    // add `end_date_signed` to the URL query string
    if (getEndDateSigned() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%send_date_signed%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEndDateSigned()))));
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

    // add `notice_deadline` to the URL query string
    if (getNoticeDeadline() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%snotice_deadline%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getNoticeDeadline()))));
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

    // add `state` to the URL query string
    if (getState() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sstate%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getState()))));
    }

    // add `days_left` to the URL query string
    if (getDaysLeft() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sdays_left%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDaysLeft()))));
    }

    // add `renewal_stopped_at` to the URL query string
    if (getRenewalStoppedAt() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%srenewal_stopped_at%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRenewalStoppedAt()))));
    }

    return joiner.toString();
  }
}

