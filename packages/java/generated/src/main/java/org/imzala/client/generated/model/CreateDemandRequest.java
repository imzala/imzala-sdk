/*
 * imzala External API
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.10.1 · **Son güncelleme:** 2026-10-01  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır (aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  `403 AI_TOOL_ACCESS_DISABLED`: organizasyon yöneticisi, organizasyonun yapay zekâ araçlarıyla erişimini kapatmıştır. Bu kontrol resmî İmzala MCP istemcisini (`User-Agent` başlığı `imzala-mcp/` ile başlayan istekler) tanır ve yalnız organizasyon çalışma alanını hedefleyen isteklerde uygulanır. Yanıt gövdesi:  ```json {   \"error\": \"AI_TOOL_ACCESS_DISABLED\",   \"code\": \"AI_TOOL_ACCESS_DISABLED\",   \"message\": \"Kurumunuz yapay zekâ araçlarının erişimini kapattı.\" } ```  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (11) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı | | `contract.expiring` | Takip edilen sözleşme bitiş tarihine yaklaşıyor | | `contract.ended` | Takip edilen sözleşmenin platformdaki bitiş tarihi geçti | | `contract.advanced` | Otomatik yenilenen sözleşmenin takip edilen bitişi ileri alındı |  > **Not (contract.\\* olayları):** Bunlar platformun **takip amaçlı** > kayıtlarıdır; sözleşmenin hukuken yenilendiğini veya sona erdiğini > BELİRTMEZ. Detay için `ContractTerm` şemasına ve `state` alanına bakın.  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type`: yukarıdaki 11 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.10.1
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.imzala.client.generated.model.DocumentSelectionInput;
import org.imzala.client.generated.model.PartyMappingInput;
import org.imzala.client.generated.model.PartyMappingInputVariablesValue;
import org.imzala.client.generated.model.ReminderSettings;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * CreateDemandRequest
 */
@JsonPropertyOrder({
  CreateDemandRequest.JSON_PROPERTY_TEMPLATE_ID,
  CreateDemandRequest.JSON_PROPERTY_TITLE,
  CreateDemandRequest.JSON_PROPERTY_DESCRIPTION,
  CreateDemandRequest.JSON_PROPERTY_IDEMPOTENCY_KEY,
  CreateDemandRequest.JSON_PROPERTY_FORCE,
  CreateDemandRequest.JSON_PROPERTY_PARTY_MAPPING,
  CreateDemandRequest.JSON_PROPERTY_DOCUMENTS,
  CreateDemandRequest.JSON_PROPERTY_VARIABLES,
  CreateDemandRequest.JSON_PROPERTY_DOCUMENT_VARIABLES,
  CreateDemandRequest.JSON_PROPERTY_HAS_TIMESTAMP,
  CreateDemandRequest.JSON_PROPERTY_ALLOWED_SIGNATURE_VARIANTS,
  CreateDemandRequest.JSON_PROPERTY_DISPATCH_NOTIFICATIONS,
  CreateDemandRequest.JSON_PROPERTY_SEND_SMS_NOTIFICATIONS,
  CreateDemandRequest.JSON_PROPERTY_SEND_EMAIL_NOTIFICATIONS,
  CreateDemandRequest.JSON_PROPERTY_SMS_TITLE,
  CreateDemandRequest.JSON_PROPERTY_SMS_CONTENT,
  CreateDemandRequest.JSON_PROPERTY_EMAIL_CONTENT,
  CreateDemandRequest.JSON_PROPERTY_EXPIRY_DATE,
  CreateDemandRequest.JSON_PROPERTY_REQUIRE_TC_VERIFICATION,
  CreateDemandRequest.JSON_PROPERTY_REQUIRE_BIOMETRIC_VERIFICATION,
  CreateDemandRequest.JSON_PROPERTY_REMINDER_SETTINGS,
  CreateDemandRequest.JSON_PROPERTY_TERM_START_MODE,
  CreateDemandRequest.JSON_PROPERTY_TERM_START_DATE,
  CreateDemandRequest.JSON_PROPERTY_TERM_DURATION_MONTHS,
  CreateDemandRequest.JSON_PROPERTY_TERM_FIXED_END_DATE,
  CreateDemandRequest.JSON_PROPERTY_RENEWAL_TYPE,
  CreateDemandRequest.JSON_PROPERTY_RENEWAL_PERIOD_MONTHS,
  CreateDemandRequest.JSON_PROPERTY_NOTICE_DAYS,
  CreateDemandRequest.JSON_PROPERTY_REMINDER_OFFSETS,
  CreateDemandRequest.JSON_PROPERTY_NOTIFY_COUNTERPARTY
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-10-05T01:06:12.119322+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class CreateDemandRequest {
  public static final String JSON_PROPERTY_TEMPLATE_ID = "template_id";
  @javax.annotation.Nonnull
  private UUID templateId;

  public static final String JSON_PROPERTY_TITLE = "title";
  @javax.annotation.Nullable
  private String title;

  public static final String JSON_PROPERTY_DESCRIPTION = "description";
  @javax.annotation.Nullable
  private String description;

  public static final String JSON_PROPERTY_IDEMPOTENCY_KEY = "idempotency_key";
  @javax.annotation.Nullable
  private String idempotencyKey;

  public static final String JSON_PROPERTY_FORCE = "force";
  @javax.annotation.Nullable
  private Boolean force;

  public static final String JSON_PROPERTY_PARTY_MAPPING = "party_mapping";
  @javax.annotation.Nonnull
  private List<PartyMappingInput> partyMapping = new ArrayList<>();

  public static final String JSON_PROPERTY_DOCUMENTS = "documents";
  @javax.annotation.Nullable
  private DocumentSelectionInput documents;

  public static final String JSON_PROPERTY_VARIABLES = "variables";
  @javax.annotation.Nullable
  private Map<String, PartyMappingInputVariablesValue> variables = new HashMap<>();

  public static final String JSON_PROPERTY_DOCUMENT_VARIABLES = "document_variables";
  @javax.annotation.Nullable
  private Map<String, Map<String, PartyMappingInputVariablesValue>> documentVariables;

  public static final String JSON_PROPERTY_HAS_TIMESTAMP = "has_timestamp";
  @javax.annotation.Nullable
  private Boolean hasTimestamp = false;

  /**
   * Gets or Sets allowedSignatureVariants
   */
  public enum AllowedSignatureVariantsEnum {
    DRAW(String.valueOf("draw")),
    
    TYPE(String.valueOf("type")),
    
    UPLOAD(String.valueOf("upload")),
    
    PHONE(String.valueOf("phone")),
    
    PHONE_DRAW(String.valueOf("phone_draw"));

    private String value;

    AllowedSignatureVariantsEnum(String value) {
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
    public static AllowedSignatureVariantsEnum fromValue(String value) {
      for (AllowedSignatureVariantsEnum b : AllowedSignatureVariantsEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_ALLOWED_SIGNATURE_VARIANTS = "allowed_signature_variants";
  private JsonNullable<List<AllowedSignatureVariantsEnum>> allowedSignatureVariants = JsonNullable.<List<AllowedSignatureVariantsEnum>>undefined();

  public static final String JSON_PROPERTY_DISPATCH_NOTIFICATIONS = "dispatch_notifications";
  @javax.annotation.Nullable
  private Boolean dispatchNotifications = true;

  public static final String JSON_PROPERTY_SEND_SMS_NOTIFICATIONS = "send_sms_notifications";
  @javax.annotation.Nullable
  private Boolean sendSmsNotifications = true;

  public static final String JSON_PROPERTY_SEND_EMAIL_NOTIFICATIONS = "send_email_notifications";
  @javax.annotation.Nullable
  private Boolean sendEmailNotifications = true;

  public static final String JSON_PROPERTY_SMS_TITLE = "sms_title";
  @javax.annotation.Nullable
  private String smsTitle = "CODECK";

  public static final String JSON_PROPERTY_SMS_CONTENT = "sms_content";
  @javax.annotation.Nullable
  private String smsContent;

  public static final String JSON_PROPERTY_EMAIL_CONTENT = "email_content";
  @javax.annotation.Nullable
  private String emailContent;

  public static final String JSON_PROPERTY_EXPIRY_DATE = "expiry_date";
  @javax.annotation.Nullable
  private OffsetDateTime expiryDate;

  public static final String JSON_PROPERTY_REQUIRE_TC_VERIFICATION = "require_tc_verification";
  @javax.annotation.Nullable
  private Boolean requireTcVerification = false;

  public static final String JSON_PROPERTY_REQUIRE_BIOMETRIC_VERIFICATION = "require_biometric_verification";
  @javax.annotation.Nullable
  private Boolean requireBiometricVerification = false;

  public static final String JSON_PROPERTY_REMINDER_SETTINGS = "reminder_settings";
  @javax.annotation.Nullable
  private ReminderSettings reminderSettings;

  /**
   * Sözleşme süre/yenileme takibi. Gönderilmezse şablonun takip politikası kullanılır. Tam alan kümesi ve doğrulama kuralları için &#x60;ContractTermInput&#x60; şemasına bakın (&#x60;PATCH /demands/{id}/term&#x60; ile AYNI alan adları). 
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

  public CreateDemandRequest() { 
  }

  public CreateDemandRequest templateId(@javax.annotation.Nonnull UUID templateId) {
    this.templateId = templateId;
    return this;
  }

  /**
   * GET /api/v1/templates listesinden veya dashboard&#39;dan kopyalayın
   * @return templateId
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_TEMPLATE_ID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public UUID getTemplateId() {
    return templateId;
  }


  @JsonProperty(value = JSON_PROPERTY_TEMPLATE_ID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTemplateId(@javax.annotation.Nonnull UUID templateId) {
    this.templateId = templateId;
  }


  public CreateDemandRequest title(@javax.annotation.Nullable String title) {
    this.title = title;
    return this;
  }

  /**
   * Sözleşme başlığı (yoksa template adı kullanılır). Başlık, imza davet ve hatırlatma SMS&#39;lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın. 
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


  public CreateDemandRequest description(@javax.annotation.Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   * @return description
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DESCRIPTION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getDescription() {
    return description;
  }


  @JsonProperty(value = JSON_PROPERTY_DESCRIPTION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDescription(@javax.annotation.Nullable String description) {
    this.description = description;
  }


  public CreateDemandRequest idempotencyKey(@javax.annotation.Nullable String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
    return this;
  }

  /**
   * &#x60;Idempotency-Key&#x60; başlığının gövde karşılığı; başlık ekleyemeyen istemciler için. İkisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;. 
   * @return idempotencyKey
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_IDEMPOTENCY_KEY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getIdempotencyKey() {
    return idempotencyKey;
  }


  @JsonProperty(value = JSON_PROPERTY_IDEMPOTENCY_KEY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIdempotencyKey(@javax.annotation.Nullable String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
  }


  public CreateDemandRequest force(@javax.annotation.Nullable Boolean force) {
    this.force = force;
    return this;
  }

  /**
   * Kopya kapısını bilerek geç. Yalnız idempotency anahtarı GÖNDERİLMEYEN çağrılarda anlamlıdır: aynı şablondan aynı taraflara aynı başlık ve değişkenlerle 10 dakika içinde ikinci bir sözleşmeyi kasten göndermek için. 
   * @return force
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_FORCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getForce() {
    return force;
  }


  @JsonProperty(value = JSON_PROPERTY_FORCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setForce(@javax.annotation.Nullable Boolean force) {
    this.force = force;
  }


  public CreateDemandRequest partyMapping(@javax.annotation.Nonnull List<PartyMappingInput> partyMapping) {
    this.partyMapping = partyMapping;
    return this;
  }

  public CreateDemandRequest addPartyMappingItem(PartyMappingInput partyMappingItem) {
    if (this.partyMapping == null) {
      this.partyMapping = new ArrayList<>();
    }
    this.partyMapping.add(partyMappingItem);
    return this;
  }

  /**
   * Get partyMapping
   * @return partyMapping
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_PARTY_MAPPING, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<PartyMappingInput> getPartyMapping() {
    return partyMapping;
  }


  @JsonProperty(value = JSON_PROPERTY_PARTY_MAPPING, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPartyMapping(@javax.annotation.Nonnull List<PartyMappingInput> partyMapping) {
    this.partyMapping = partyMapping;
  }


  public CreateDemandRequest documents(@javax.annotation.Nullable DocumentSelectionInput documents) {
    this.documents = documents;
    return this;
  }

  /**
   * Get documents
   * @return documents
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DOCUMENTS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public DocumentSelectionInput getDocuments() {
    return documents;
  }


  @JsonProperty(value = JSON_PROPERTY_DOCUMENTS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDocuments(@javax.annotation.Nullable DocumentSelectionInput documents) {
    this.documents = documents;
  }


  public CreateDemandRequest variables(@javax.annotation.Nullable Map<String, PartyMappingInputVariablesValue> variables) {
    this.variables = variables;
    return this;
  }

  public CreateDemandRequest putVariablesItem(String key, PartyMappingInputVariablesValue variablesItem) {
    if (this.variables == null) {
      this.variables = new HashMap<>();
    }
    this.variables.put(key, variablesItem);
    return this;
  }

  /**
   * **Root scope** — partilerden bağımsız field&#39;lara gönderilen değerler. Item&#39;ın template_party_id&#39;si NULL ise (partisiz) buradan dolar. Multi-party şablonda kira_baslangic_tarihi gibi paylaşılan field&#39;lar.  **Kaşe alanları:** Değer bir &#x60;StampData&#x60; NESNESİ ise ve slug bir kaşe alanına aitse, sözleşme oluşturulurken o kaşe &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; ile aynı kurallarla doldurulur (gönderenin kaşesi yapılandırılmış veriyle; imzalayanın dolduracağı kaşeye düzenlenebilir ön değer). Aynı slug birden fazla belgede geçiyorsa hepsine yazılır; öncelik kuralları &#x60;variables&#x60; ile aynıdır (bkz. &#x60;document_variables&#x60;). Nesne değer yalnız kaşe slug&#39;ına verilebilir: kaşe olmayan ya da gönderilecek belgelerde bulunmayan slug 400 &#x60;INVALID_VARIABLES&#x60; (&#x60;details.reason: object_value_for_non_stamp&#x60;), geçersiz kaşe verisi 400 &#x60;INVALID_STAMP_DATA&#x60; döner; ikisinde de sözleşme oluşturulmaz, kredi düşülmez. Kaşe slug&#39;ına düz metin gönderilirse kaşeye yazılmaz ve &#x60;variables_ignored&#x60; içinde görünür. Toplu uçlar nesne değeri desteklemez (400 &#x60;INVALID_VARIABLES&#x60;, &#x60;details.reason: unsupported_endpoint&#x60;). 
   * @return variables
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_VARIABLES, required = false)
  @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
  public Map<String, PartyMappingInputVariablesValue> getVariables() {
    return variables;
  }


  @JsonProperty(value = JSON_PROPERTY_VARIABLES, required = false)
  @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
  public void setVariables(@javax.annotation.Nullable Map<String, PartyMappingInputVariablesValue> variables) {
    this.variables = variables;
  }


  public CreateDemandRequest documentVariables(@javax.annotation.Nullable Map<String, Map<String, PartyMappingInputVariablesValue>> documentVariables) {
    this.documentVariables = documentVariables;
    return this;
  }

  public CreateDemandRequest putDocumentVariablesItem(String key, Map<String, PartyMappingInputVariablesValue> documentVariablesItem) {
    if (this.documentVariables == null) {
      this.documentVariables = new HashMap<>();
    }
    this.documentVariables.put(key, documentVariablesItem);
    return this;
  }

  /**
   * Çok belgeli şablonda BELGE BAŞINA ortak değerler (o belgedeki tüm taraflar ve partisiz alanlar için). Opsiyoneldir; göndermezseniz &#x60;variables&#x60; ve &#x60;party_mapping[].variables&#x60; eskisi gibi çalışır.  Öncelik (alan bazında; bir seviyede anahtar yoksa bir alttakine düşer, açıkça gönderilen boş metin ya da null o seviyede kalır): 1. &#x60;party_mapping[i].document_variables[belge][slug]&#x60; 2. &#x60;document_variables[belge][slug]&#x60; 3. &#x60;party_mapping[i].variables[slug]&#x60; 4. &#x60;variables[slug]&#x60;  Şablona ait olmayan ya da bu istekte gönderilmeyen belge kimliği 400 &#x60;INVALID_DOCUMENT_VARIABLES&#x60; ile reddedilir; sözleşme oluşturulmaz, kredi düşülmez, davet gönderilmez. 
   * @return documentVariables
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DOCUMENT_VARIABLES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Map<String, Map<String, PartyMappingInputVariablesValue>> getDocumentVariables() {
    return documentVariables;
  }


  @JsonProperty(value = JSON_PROPERTY_DOCUMENT_VARIABLES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDocumentVariables(@javax.annotation.Nullable Map<String, Map<String, PartyMappingInputVariablesValue>> documentVariables) {
    this.documentVariables = documentVariables;
  }


  public CreateDemandRequest hasTimestamp(@javax.annotation.Nullable Boolean hasTimestamp) {
    this.hasTimestamp = hasTimestamp;
    return this;
  }

  /**
   * TÜBİTAK zaman damgası
   * @return hasTimestamp
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_HAS_TIMESTAMP, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getHasTimestamp() {
    return hasTimestamp;
  }


  @JsonProperty(value = JSON_PROPERTY_HAS_TIMESTAMP, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setHasTimestamp(@javax.annotation.Nullable Boolean hasTimestamp) {
    this.hasTimestamp = hasTimestamp;
  }


  public CreateDemandRequest allowedSignatureVariants(@javax.annotation.Nullable List<AllowedSignatureVariantsEnum> allowedSignatureVariants) {
    this.allowedSignatureVariants = JsonNullable.<List<AllowedSignatureVariantsEnum>>of(allowedSignatureVariants);
    return this;
  }

  public CreateDemandRequest addAllowedSignatureVariantsItem(AllowedSignatureVariantsEnum allowedSignatureVariantsItem) {
    if (this.allowedSignatureVariants == null || !this.allowedSignatureVariants.isPresent()) {
      this.allowedSignatureVariants = JsonNullable.<List<AllowedSignatureVariantsEnum>>of(new ArrayList<>());
    }
    try {
      this.allowedSignatureVariants.get().add(allowedSignatureVariantsItem);
    } catch (java.util.NoSuchElementException e) {
      // this can never happen, as we make sure above that the value is present
    }
    return this;
  }

  /**
   * İmzacının kullanabileceği dijital imza yöntemleri ve görünme sırası. Dizinin SIRASI imza ekranındaki sekme sırasıdır; listede olmayan yöntem o sözleşmede kapalıdır. Değerler: &#x60;draw&#x60; (parmakla veya fareyle çizerek), &#x60;type&#x60; (adını yazarak), &#x60;upload&#x60; (imza görselini dosya olarak yükleyerek), &#x60;phone&#x60; (telefonda çizerek).  Gönderilmezse (veya &#x60;null&#x60; gönderilirse) şablonun ayarı, şablonda da tanımlı değilse organizasyonun ayarı geçerli olur; hiçbiri tanımlı değilse dört yöntem varsayılan sırayla açıktır.  Bu alan toleranslı okunur: boş dizi, dizi olmayan bir değer veya tanınmayan bir değer gönderilirse alan YOK SAYILIR ve üst katmandan devralınır. Yani boş dizi göndermek \&quot;tüm yöntemleri kapat\&quot; anlamına GELMEZ; en az bir yöntem her zaman açık kalır. Büyük harfli değerler (&#x60;DRAW&#x60;) kabul edilir, tekrar eden değerler teke indirilir.  Yalnız &#x60;phone&#x60; içeren bir liste reddedilir (400 &#x60;SIGNATURE_VARIANTS_PHONE_ONLY&#x60;): telefonda çizim mobil cihazda kullanılamadığı için tek başına bırakılırsa imzacı yöntemsiz kalır. En az bir &#x60;phone&#x60; olmayan yöntem bırakın.  &#x60;phone_draw&#x60; (\&quot;Yalnız telefonda çizerek\&quot;) ayrı bir moddur ve YALNIZ tek başına gönderilir: &#x60;[\&quot;phone_draw\&quot;]&#x60;. Bilgisayardan açan imzacı yalnız QR kod ile telefona yönlendirilir ve imzasını telefonda çizer; telefondan açan imzacı yalnız çizerek imzalar. Başka bir yöntemle birlikte gönderilirse 400 &#x60;SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE&#x60; döner.  Ayar sözleşme düzeyindedir; imzacı bazında farklılaştırılamaz. &#x60;party_mapping&#x60; girdilerine yazılan böyle bir alan yok sayılır. 
   * @return allowedSignatureVariants
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public List<AllowedSignatureVariantsEnum> getAllowedSignatureVariants() {
        return allowedSignatureVariants.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_ALLOWED_SIGNATURE_VARIANTS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<List<AllowedSignatureVariantsEnum>> getAllowedSignatureVariants_JsonNullable() {
    return allowedSignatureVariants;
  }
  
  @JsonProperty(JSON_PROPERTY_ALLOWED_SIGNATURE_VARIANTS)
  public void setAllowedSignatureVariants_JsonNullable(JsonNullable<List<AllowedSignatureVariantsEnum>> allowedSignatureVariants) {
    this.allowedSignatureVariants = allowedSignatureVariants;
  }

  public void setAllowedSignatureVariants(@javax.annotation.Nullable List<AllowedSignatureVariantsEnum> allowedSignatureVariants) {
    this.allowedSignatureVariants = JsonNullable.<List<AllowedSignatureVariantsEnum>>of(allowedSignatureVariants);
  }


  public CreateDemandRequest dispatchNotifications(@javax.annotation.Nullable Boolean dispatchNotifications) {
    this.dispatchNotifications = dispatchNotifications;
    return this;
  }

  /**
   * &#x60;false&#x60; verilirse sözleşme yalnız oluşturulur, davet gönderilmez; daha sonra &#x60;POST /api/v1/demands/{demandId}/dispatch&#x60; ile gönderilir (çok-belgeli zarf akışında belgeler bu arada eklenir). Varsayılan açık: davetler oluşturma anında gider. 
   * @return dispatchNotifications
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DISPATCH_NOTIFICATIONS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getDispatchNotifications() {
    return dispatchNotifications;
  }


  @JsonProperty(value = JSON_PROPERTY_DISPATCH_NOTIFICATIONS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDispatchNotifications(@javax.annotation.Nullable Boolean dispatchNotifications) {
    this.dispatchNotifications = dispatchNotifications;
  }


  public CreateDemandRequest sendSmsNotifications(@javax.annotation.Nullable Boolean sendSmsNotifications) {
    this.sendSmsNotifications = sendSmsNotifications;
    return this;
  }

  /**
   * Get sendSmsNotifications
   * @return sendSmsNotifications
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_SEND_SMS_NOTIFICATIONS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getSendSmsNotifications() {
    return sendSmsNotifications;
  }


  @JsonProperty(value = JSON_PROPERTY_SEND_SMS_NOTIFICATIONS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSendSmsNotifications(@javax.annotation.Nullable Boolean sendSmsNotifications) {
    this.sendSmsNotifications = sendSmsNotifications;
  }


  public CreateDemandRequest sendEmailNotifications(@javax.annotation.Nullable Boolean sendEmailNotifications) {
    this.sendEmailNotifications = sendEmailNotifications;
    return this;
  }

  /**
   * Get sendEmailNotifications
   * @return sendEmailNotifications
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_SEND_EMAIL_NOTIFICATIONS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getSendEmailNotifications() {
    return sendEmailNotifications;
  }


  @JsonProperty(value = JSON_PROPERTY_SEND_EMAIL_NOTIFICATIONS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSendEmailNotifications(@javax.annotation.Nullable Boolean sendEmailNotifications) {
    this.sendEmailNotifications = sendEmailNotifications;
  }


  public CreateDemandRequest smsTitle(@javax.annotation.Nullable String smsTitle) {
    this.smsTitle = smsTitle;
    return this;
  }

  /**
   * SMS gönderici adı
   * @return smsTitle
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_SMS_TITLE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getSmsTitle() {
    return smsTitle;
  }


  @JsonProperty(value = JSON_PROPERTY_SMS_TITLE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSmsTitle(@javax.annotation.Nullable String smsTitle) {
    this.smsTitle = smsTitle;
  }


  public CreateDemandRequest smsContent(@javax.annotation.Nullable String smsContent) {
    this.smsContent = smsContent;
    return this;
  }

  /**
   * Custom SMS gövdesi. **Sadece** çağıran organizasyon **PRO veya ENTERPRISE planda** ise ve aktif &#x60;OrganizationSmsConfig&#x60; (sender_name dolu) varsa kabul edilir; aksi halde 403 &#x60;SMS_CUSTOMIZATION_NOT_ALLOWED&#x60; döner.  FREE/BASIC planda olan veya kendi SMS sağlayıcısı tanımlı olmayan müşterilerin marka itibarını korumak için sistem default sağlayıcısı (Codeck NetGSM) ile gönderim yapılır ve özel metin reddedilir. Kendi sağlayıcınızı tanımlamak için Dashboard → Organizasyon → SMS Ayarları sayfasını kullanın.  Boş string / null gönderirseniz \&quot;clear\&quot; olarak yorumlanır (gating&#39;den geçer). 
   * @return smsContent
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_SMS_CONTENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getSmsContent() {
    return smsContent;
  }


  @JsonProperty(value = JSON_PROPERTY_SMS_CONTENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSmsContent(@javax.annotation.Nullable String smsContent) {
    this.smsContent = smsContent;
  }


  public CreateDemandRequest emailContent(@javax.annotation.Nullable String emailContent) {
    this.emailContent = emailContent;
    return this;
  }

  /**
   * Custom e-posta gövdesi
   * @return emailContent
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_EMAIL_CONTENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getEmailContent() {
    return emailContent;
  }


  @JsonProperty(value = JSON_PROPERTY_EMAIL_CONTENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEmailContent(@javax.annotation.Nullable String emailContent) {
    this.emailContent = emailContent;
  }


  public CreateDemandRequest expiryDate(@javax.annotation.Nullable OffsetDateTime expiryDate) {
    this.expiryDate = expiryDate;
    return this;
  }

  /**
   * Get expiryDate
   * @return expiryDate
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_EXPIRY_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public OffsetDateTime getExpiryDate() {
    return expiryDate;
  }


  @JsonProperty(value = JSON_PROPERTY_EXPIRY_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setExpiryDate(@javax.annotation.Nullable OffsetDateTime expiryDate) {
    this.expiryDate = expiryDate;
  }


  public CreateDemandRequest requireTcVerification(@javax.annotation.Nullable Boolean requireTcVerification) {
    this.requireTcVerification = requireTcVerification;
    return this;
  }

  /**
   * Get requireTcVerification
   * @return requireTcVerification
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_REQUIRE_TC_VERIFICATION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getRequireTcVerification() {
    return requireTcVerification;
  }


  @JsonProperty(value = JSON_PROPERTY_REQUIRE_TC_VERIFICATION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRequireTcVerification(@javax.annotation.Nullable Boolean requireTcVerification) {
    this.requireTcVerification = requireTcVerification;
  }


  public CreateDemandRequest requireBiometricVerification(@javax.annotation.Nullable Boolean requireBiometricVerification) {
    this.requireBiometricVerification = requireBiometricVerification;
    return this;
  }

  /**
   * Get requireBiometricVerification
   * @return requireBiometricVerification
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_REQUIRE_BIOMETRIC_VERIFICATION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getRequireBiometricVerification() {
    return requireBiometricVerification;
  }


  @JsonProperty(value = JSON_PROPERTY_REQUIRE_BIOMETRIC_VERIFICATION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRequireBiometricVerification(@javax.annotation.Nullable Boolean requireBiometricVerification) {
    this.requireBiometricVerification = requireBiometricVerification;
  }


  public CreateDemandRequest reminderSettings(@javax.annotation.Nullable ReminderSettings reminderSettings) {
    this.reminderSettings = reminderSettings;
    return this;
  }

  /**
   * Bu sözleşme için hatırlatma ayarlarını **şablon default&#39;unu override** ederek belirtir. Yollanmazsa şablonun &#x60;reminder_*&#x60; alanları kullanılır (PUT /api/templates/:id ile dashboard&#39;dan kaydedilen değerler); şablonda da yoksa &#x60;{enabled:true, intervals_hours:[48], max_reminders:1, channels:[\&quot;email\&quot;]}&#x60; default&#39;u uygulanır. Demand oluşumunda &#x60;ReminderConfig&#x60; satırı yaratılır ve BullMQ kuyruğuna scheduled hatırlatmalar yazılır. 
   * @return reminderSettings
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_REMINDER_SETTINGS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public ReminderSettings getReminderSettings() {
    return reminderSettings;
  }


  @JsonProperty(value = JSON_PROPERTY_REMINDER_SETTINGS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setReminderSettings(@javax.annotation.Nullable ReminderSettings reminderSettings) {
    this.reminderSettings = reminderSettings;
  }


  public CreateDemandRequest termStartMode(@javax.annotation.Nullable TermStartModeEnum termStartMode) {
    this.termStartMode = JsonNullable.<TermStartModeEnum>of(termStartMode);
    return this;
  }

  /**
   * Sözleşme süre/yenileme takibi. Gönderilmezse şablonun takip politikası kullanılır. Tam alan kümesi ve doğrulama kuralları için &#x60;ContractTermInput&#x60; şemasına bakın (&#x60;PATCH /demands/{id}/term&#x60; ile AYNI alan adları). 
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


  public CreateDemandRequest termStartDate(@javax.annotation.Nullable LocalDate termStartDate) {
    this.termStartDate = JsonNullable.<LocalDate>of(termStartDate);
    return this;
  }

  /**
   * Yalnız term_start_mode: FIXED_DATE iken kullanılır.
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


  public CreateDemandRequest termDurationMonths(@javax.annotation.Nullable Integer termDurationMonths) {
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


  public CreateDemandRequest termFixedEndDate(@javax.annotation.Nullable LocalDate termFixedEndDate) {
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


  public CreateDemandRequest renewalType(@javax.annotation.Nullable RenewalTypeEnum renewalType) {
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


  public CreateDemandRequest renewalPeriodMonths(@javax.annotation.Nullable Integer renewalPeriodMonths) {
    this.renewalPeriodMonths = JsonNullable.<Integer>of(renewalPeriodMonths);
    return this;
  }

  /**
   * AUTO_RENEW gerektirir; verilmezse term_duration_months&#39;tan devralınır.
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


  public CreateDemandRequest noticeDays(@javax.annotation.Nullable Integer noticeDays) {
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


  public CreateDemandRequest reminderOffsets(@javax.annotation.Nullable List<Integer> reminderOffsets) {
    this.reminderOffsets = JsonNullable.<List<Integer>>of(reminderOffsets);
    return this;
  }

  public CreateDemandRequest addReminderOffsetsItem(Integer reminderOffsetsItem) {
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
   * Get reminderOffsets
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


  public CreateDemandRequest notifyCounterparty(@javax.annotation.Nullable Boolean notifyCounterparty) {
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
   * Return true if this CreateDemandRequest object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateDemandRequest createDemandRequest = (CreateDemandRequest) o;
    return Objects.equals(this.templateId, createDemandRequest.templateId) &&
        Objects.equals(this.title, createDemandRequest.title) &&
        Objects.equals(this.description, createDemandRequest.description) &&
        Objects.equals(this.idempotencyKey, createDemandRequest.idempotencyKey) &&
        Objects.equals(this.force, createDemandRequest.force) &&
        Objects.equals(this.partyMapping, createDemandRequest.partyMapping) &&
        Objects.equals(this.documents, createDemandRequest.documents) &&
        Objects.equals(this.variables, createDemandRequest.variables) &&
        Objects.equals(this.documentVariables, createDemandRequest.documentVariables) &&
        Objects.equals(this.hasTimestamp, createDemandRequest.hasTimestamp) &&
        equalsNullable(this.allowedSignatureVariants, createDemandRequest.allowedSignatureVariants) &&
        Objects.equals(this.dispatchNotifications, createDemandRequest.dispatchNotifications) &&
        Objects.equals(this.sendSmsNotifications, createDemandRequest.sendSmsNotifications) &&
        Objects.equals(this.sendEmailNotifications, createDemandRequest.sendEmailNotifications) &&
        Objects.equals(this.smsTitle, createDemandRequest.smsTitle) &&
        Objects.equals(this.smsContent, createDemandRequest.smsContent) &&
        Objects.equals(this.emailContent, createDemandRequest.emailContent) &&
        Objects.equals(this.expiryDate, createDemandRequest.expiryDate) &&
        Objects.equals(this.requireTcVerification, createDemandRequest.requireTcVerification) &&
        Objects.equals(this.requireBiometricVerification, createDemandRequest.requireBiometricVerification) &&
        Objects.equals(this.reminderSettings, createDemandRequest.reminderSettings) &&
        equalsNullable(this.termStartMode, createDemandRequest.termStartMode) &&
        equalsNullable(this.termStartDate, createDemandRequest.termStartDate) &&
        equalsNullable(this.termDurationMonths, createDemandRequest.termDurationMonths) &&
        equalsNullable(this.termFixedEndDate, createDemandRequest.termFixedEndDate) &&
        equalsNullable(this.renewalType, createDemandRequest.renewalType) &&
        equalsNullable(this.renewalPeriodMonths, createDemandRequest.renewalPeriodMonths) &&
        equalsNullable(this.noticeDays, createDemandRequest.noticeDays) &&
        equalsNullable(this.reminderOffsets, createDemandRequest.reminderOffsets) &&
        Objects.equals(this.notifyCounterparty, createDemandRequest.notifyCounterparty);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(templateId, title, description, idempotencyKey, force, partyMapping, documents, variables, documentVariables, hasTimestamp, hashCodeNullable(allowedSignatureVariants), dispatchNotifications, sendSmsNotifications, sendEmailNotifications, smsTitle, smsContent, emailContent, expiryDate, requireTcVerification, requireBiometricVerification, reminderSettings, hashCodeNullable(termStartMode), hashCodeNullable(termStartDate), hashCodeNullable(termDurationMonths), hashCodeNullable(termFixedEndDate), hashCodeNullable(renewalType), hashCodeNullable(renewalPeriodMonths), hashCodeNullable(noticeDays), hashCodeNullable(reminderOffsets), notifyCounterparty);
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
    sb.append("class CreateDemandRequest {\n");
    sb.append("    templateId: ").append(toIndentedString(templateId)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    idempotencyKey: ").append(toIndentedString(idempotencyKey)).append("\n");
    sb.append("    force: ").append(toIndentedString(force)).append("\n");
    sb.append("    partyMapping: ").append(toIndentedString(partyMapping)).append("\n");
    sb.append("    documents: ").append(toIndentedString(documents)).append("\n");
    sb.append("    variables: ").append(toIndentedString(variables)).append("\n");
    sb.append("    documentVariables: ").append(toIndentedString(documentVariables)).append("\n");
    sb.append("    hasTimestamp: ").append(toIndentedString(hasTimestamp)).append("\n");
    sb.append("    allowedSignatureVariants: ").append(toIndentedString(allowedSignatureVariants)).append("\n");
    sb.append("    dispatchNotifications: ").append(toIndentedString(dispatchNotifications)).append("\n");
    sb.append("    sendSmsNotifications: ").append(toIndentedString(sendSmsNotifications)).append("\n");
    sb.append("    sendEmailNotifications: ").append(toIndentedString(sendEmailNotifications)).append("\n");
    sb.append("    smsTitle: ").append(toIndentedString(smsTitle)).append("\n");
    sb.append("    smsContent: ").append(toIndentedString(smsContent)).append("\n");
    sb.append("    emailContent: ").append(toIndentedString(emailContent)).append("\n");
    sb.append("    expiryDate: ").append(toIndentedString(expiryDate)).append("\n");
    sb.append("    requireTcVerification: ").append(toIndentedString(requireTcVerification)).append("\n");
    sb.append("    requireBiometricVerification: ").append(toIndentedString(requireBiometricVerification)).append("\n");
    sb.append("    reminderSettings: ").append(toIndentedString(reminderSettings)).append("\n");
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

    // add `template_id` to the URL query string
    if (getTemplateId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%stemplate_id%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTemplateId()))));
    }

    // add `title` to the URL query string
    if (getTitle() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%stitle%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTitle()))));
    }

    // add `description` to the URL query string
    if (getDescription() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sdescription%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDescription()))));
    }

    // add `idempotency_key` to the URL query string
    if (getIdempotencyKey() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sidempotency_key%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIdempotencyKey()))));
    }

    // add `force` to the URL query string
    if (getForce() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sforce%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getForce()))));
    }

    // add `party_mapping` to the URL query string
    if (getPartyMapping() != null) {
      for (int i = 0; i < getPartyMapping().size(); i++) {
        if (getPartyMapping().get(i) != null) {
          joiner.add(getPartyMapping().get(i).toUrlQueryString(String.format(java.util.Locale.ROOT, "%sparty_mapping%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `documents` to the URL query string
    if (getDocuments() != null) {
      joiner.add(getDocuments().toUrlQueryString(prefix + "documents" + suffix));
    }

    // add `variables` to the URL query string
    if (getVariables() != null) {
      for (String _key : getVariables().keySet()) {
        if (getVariables().get(_key) != null) {
          joiner.add(getVariables().get(_key).toUrlQueryString(String.format(java.util.Locale.ROOT, "%svariables%s%s", prefix, suffix,
              "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix))));
        }
      }
    }

    // add `document_variables` to the URL query string
    if (getDocumentVariables() != null) {
      for (String _key : getDocumentVariables().keySet()) {
        joiner.add(String.format(java.util.Locale.ROOT, "%sdocument_variables%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
            getDocumentVariables().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getDocumentVariables().get(_key)))));
      }
    }

    // add `has_timestamp` to the URL query string
    if (getHasTimestamp() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%shas_timestamp%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getHasTimestamp()))));
    }

    // add `allowed_signature_variants` to the URL query string
    if (getAllowedSignatureVariants() != null) {
      for (int i = 0; i < getAllowedSignatureVariants().size(); i++) {
        joiner.add(String.format(java.util.Locale.ROOT, "%sallowed_signature_variants%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getAllowedSignatureVariants().get(i)))));
      }
    }

    // add `dispatch_notifications` to the URL query string
    if (getDispatchNotifications() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sdispatch_notifications%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDispatchNotifications()))));
    }

    // add `send_sms_notifications` to the URL query string
    if (getSendSmsNotifications() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%ssend_sms_notifications%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSendSmsNotifications()))));
    }

    // add `send_email_notifications` to the URL query string
    if (getSendEmailNotifications() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%ssend_email_notifications%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSendEmailNotifications()))));
    }

    // add `sms_title` to the URL query string
    if (getSmsTitle() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%ssms_title%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSmsTitle()))));
    }

    // add `sms_content` to the URL query string
    if (getSmsContent() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%ssms_content%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSmsContent()))));
    }

    // add `email_content` to the URL query string
    if (getEmailContent() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%semail_content%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEmailContent()))));
    }

    // add `expiry_date` to the URL query string
    if (getExpiryDate() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sexpiry_date%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getExpiryDate()))));
    }

    // add `require_tc_verification` to the URL query string
    if (getRequireTcVerification() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%srequire_tc_verification%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRequireTcVerification()))));
    }

    // add `require_biometric_verification` to the URL query string
    if (getRequireBiometricVerification() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%srequire_biometric_verification%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRequireBiometricVerification()))));
    }

    // add `reminder_settings` to the URL query string
    if (getReminderSettings() != null) {
      joiner.add(getReminderSettings().toUrlQueryString(prefix + "reminder_settings" + suffix));
    }

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

