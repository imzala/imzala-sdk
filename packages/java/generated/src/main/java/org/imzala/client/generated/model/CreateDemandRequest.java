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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.imzala.client.generated.model.PartyMappingInput;
import org.imzala.client.generated.model.PartyMappingInputVariablesValue;
import org.imzala.client.generated.model.ReminderSettings;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * CreateDemandRequest
 */
@JsonPropertyOrder({
  CreateDemandRequest.JSON_PROPERTY_TEMPLATE_ID,
  CreateDemandRequest.JSON_PROPERTY_TITLE,
  CreateDemandRequest.JSON_PROPERTY_DESCRIPTION,
  CreateDemandRequest.JSON_PROPERTY_PARTY_MAPPING,
  CreateDemandRequest.JSON_PROPERTY_VARIABLES,
  CreateDemandRequest.JSON_PROPERTY_HAS_TIMESTAMP,
  CreateDemandRequest.JSON_PROPERTY_SEND_SMS_NOTIFICATIONS,
  CreateDemandRequest.JSON_PROPERTY_SEND_EMAIL_NOTIFICATIONS,
  CreateDemandRequest.JSON_PROPERTY_SMS_TITLE,
  CreateDemandRequest.JSON_PROPERTY_SMS_CONTENT,
  CreateDemandRequest.JSON_PROPERTY_EMAIL_CONTENT,
  CreateDemandRequest.JSON_PROPERTY_EXPIRY_DATE,
  CreateDemandRequest.JSON_PROPERTY_REQUIRE_TC_VERIFICATION,
  CreateDemandRequest.JSON_PROPERTY_REQUIRE_BIOMETRIC_VERIFICATION,
  CreateDemandRequest.JSON_PROPERTY_REMINDER_SETTINGS
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-07-01T14:50:26.407096+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
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

  public static final String JSON_PROPERTY_PARTY_MAPPING = "party_mapping";
  @javax.annotation.Nonnull
  private List<PartyMappingInput> partyMapping = new ArrayList<>();

  public static final String JSON_PROPERTY_VARIABLES = "variables";
  @javax.annotation.Nullable
  private Map<String, PartyMappingInputVariablesValue> variables = new HashMap<>();

  public static final String JSON_PROPERTY_HAS_TIMESTAMP = "has_timestamp";
  @javax.annotation.Nullable
  private Boolean hasTimestamp = false;

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
   * Sözleşme başlığı (yoksa template adı kullanılır)
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
   * **Root scope** — partilerden bağımsız field&#39;lara gönderilen değerler. Item&#39;ın template_party_id&#39;si NULL ise (partisiz) buradan dolar. Multi-party şablonda kira_baslangic_tarihi gibi paylaşılan field&#39;lar. 
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
        Objects.equals(this.partyMapping, createDemandRequest.partyMapping) &&
        Objects.equals(this.variables, createDemandRequest.variables) &&
        Objects.equals(this.hasTimestamp, createDemandRequest.hasTimestamp) &&
        Objects.equals(this.sendSmsNotifications, createDemandRequest.sendSmsNotifications) &&
        Objects.equals(this.sendEmailNotifications, createDemandRequest.sendEmailNotifications) &&
        Objects.equals(this.smsTitle, createDemandRequest.smsTitle) &&
        Objects.equals(this.smsContent, createDemandRequest.smsContent) &&
        Objects.equals(this.emailContent, createDemandRequest.emailContent) &&
        Objects.equals(this.expiryDate, createDemandRequest.expiryDate) &&
        Objects.equals(this.requireTcVerification, createDemandRequest.requireTcVerification) &&
        Objects.equals(this.requireBiometricVerification, createDemandRequest.requireBiometricVerification) &&
        Objects.equals(this.reminderSettings, createDemandRequest.reminderSettings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(templateId, title, description, partyMapping, variables, hasTimestamp, sendSmsNotifications, sendEmailNotifications, smsTitle, smsContent, emailContent, expiryDate, requireTcVerification, requireBiometricVerification, reminderSettings);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateDemandRequest {\n");
    sb.append("    templateId: ").append(toIndentedString(templateId)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    partyMapping: ").append(toIndentedString(partyMapping)).append("\n");
    sb.append("    variables: ").append(toIndentedString(variables)).append("\n");
    sb.append("    hasTimestamp: ").append(toIndentedString(hasTimestamp)).append("\n");
    sb.append("    sendSmsNotifications: ").append(toIndentedString(sendSmsNotifications)).append("\n");
    sb.append("    sendEmailNotifications: ").append(toIndentedString(sendEmailNotifications)).append("\n");
    sb.append("    smsTitle: ").append(toIndentedString(smsTitle)).append("\n");
    sb.append("    smsContent: ").append(toIndentedString(smsContent)).append("\n");
    sb.append("    emailContent: ").append(toIndentedString(emailContent)).append("\n");
    sb.append("    expiryDate: ").append(toIndentedString(expiryDate)).append("\n");
    sb.append("    requireTcVerification: ").append(toIndentedString(requireTcVerification)).append("\n");
    sb.append("    requireBiometricVerification: ").append(toIndentedString(requireBiometricVerification)).append("\n");
    sb.append("    reminderSettings: ").append(toIndentedString(reminderSettings)).append("\n");
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

    // add `party_mapping` to the URL query string
    if (getPartyMapping() != null) {
      for (int i = 0; i < getPartyMapping().size(); i++) {
        if (getPartyMapping().get(i) != null) {
          joiner.add(getPartyMapping().get(i).toUrlQueryString(String.format(java.util.Locale.ROOT, "%sparty_mapping%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
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

    // add `has_timestamp` to the URL query string
    if (getHasTimestamp() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%shas_timestamp%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getHasTimestamp()))));
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

    return joiner.toString();
  }
}

