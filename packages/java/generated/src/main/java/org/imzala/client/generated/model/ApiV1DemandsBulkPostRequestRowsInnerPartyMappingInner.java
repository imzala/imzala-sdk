/*
 * imzala External API
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.8.6 · **Son güncelleme:** 2026-09-10  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API key kullanıyorsanız `X-Workspace-Id` header'ı göndermeniz gerekir (organizasyon UUID'si). Kişisel anahtarlar için bu header gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (8) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 8 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.8.6
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
 * ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner
 */
@JsonPropertyOrder({
  ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.JSON_PROPERTY_TEMPLATE_PARTY_ID,
  ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.JSON_PROPERTY_FIRST_NAME,
  ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.JSON_PROPERTY_LAST_NAME,
  ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.JSON_PROPERTY_EMAIL,
  ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.JSON_PROPERTY_PHONE,
  ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.JSON_PROPERTY_GOVERNMENT_ID,
  ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.JSON_PROPERTY_BIRTH_DATE,
  ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.JSON_PROPERTY_SEND_SMS,
  ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.JSON_PROPERTY_SEND_EMAIL,
  ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.JSON_PROPERTY_CUSTOM_MESSAGE,
  ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.JSON_PROPERTY_VARIABLES
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-09-15T10:01:25.885782+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner {
  public static final String JSON_PROPERTY_TEMPLATE_PARTY_ID = "template_party_id";
  @javax.annotation.Nullable
  private UUID templatePartyId;

  public static final String JSON_PROPERTY_FIRST_NAME = "first_name";
  @javax.annotation.Nullable
  private String firstName;

  public static final String JSON_PROPERTY_LAST_NAME = "last_name";
  @javax.annotation.Nullable
  private String lastName;

  public static final String JSON_PROPERTY_EMAIL = "email";
  @javax.annotation.Nullable
  private String email;

  public static final String JSON_PROPERTY_PHONE = "phone";
  @javax.annotation.Nullable
  private String phone;

  public static final String JSON_PROPERTY_GOVERNMENT_ID = "government_id";
  @javax.annotation.Nullable
  private String governmentId;

  public static final String JSON_PROPERTY_BIRTH_DATE = "birth_date";
  @javax.annotation.Nullable
  private LocalDate birthDate;

  public static final String JSON_PROPERTY_SEND_SMS = "send_sms";
  @javax.annotation.Nullable
  private Boolean sendSms = true;

  public static final String JSON_PROPERTY_SEND_EMAIL = "send_email";
  @javax.annotation.Nullable
  private Boolean sendEmail = true;

  public static final String JSON_PROPERTY_CUSTOM_MESSAGE = "custom_message";
  @javax.annotation.Nullable
  private String customMessage;

  public static final String JSON_PROPERTY_VARIABLES = "variables";
  @javax.annotation.Nullable
  private Object variables;

  public ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner() { 
  }

  public ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner templatePartyId(@javax.annotation.Nullable UUID templatePartyId) {
    this.templatePartyId = templatePartyId;
    return this;
  }

  /**
   * Get templatePartyId
   * @return templatePartyId
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_TEMPLATE_PARTY_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public UUID getTemplatePartyId() {
    return templatePartyId;
  }


  @JsonProperty(value = JSON_PROPERTY_TEMPLATE_PARTY_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTemplatePartyId(@javax.annotation.Nullable UUID templatePartyId) {
    this.templatePartyId = templatePartyId;
  }


  public ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner firstName(@javax.annotation.Nullable String firstName) {
    this.firstName = firstName;
    return this;
  }

  /**
   * Get firstName
   * @return firstName
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_FIRST_NAME, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getFirstName() {
    return firstName;
  }


  @JsonProperty(value = JSON_PROPERTY_FIRST_NAME, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setFirstName(@javax.annotation.Nullable String firstName) {
    this.firstName = firstName;
  }


  public ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner lastName(@javax.annotation.Nullable String lastName) {
    this.lastName = lastName;
    return this;
  }

  /**
   * Get lastName
   * @return lastName
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_LAST_NAME, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getLastName() {
    return lastName;
  }


  @JsonProperty(value = JSON_PROPERTY_LAST_NAME, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setLastName(@javax.annotation.Nullable String lastName) {
    this.lastName = lastName;
  }


  public ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner email(@javax.annotation.Nullable String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_EMAIL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getEmail() {
    return email;
  }


  @JsonProperty(value = JSON_PROPERTY_EMAIL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEmail(@javax.annotation.Nullable String email) {
    this.email = email;
  }


  public ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner phone(@javax.annotation.Nullable String phone) {
    this.phone = phone;
    return this;
  }

  /**
   * E.164 biçiminde telefon (ör. &#x60;+905551234567&#x60;). Ülke kodu ile birlikte gönderin; fazlalık şehirlerarası öneki (TRde baş sıfır, &#x60;+90 0542…&#x60;) sunucuda kaldırılır ve numara kanonik E.164 biçiminde saklanır. Tanınmayan girdi olduğu gibi korunur ama SMS teslim edilemeyebilir.
   * @return phone
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_PHONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getPhone() {
    return phone;
  }


  @JsonProperty(value = JSON_PROPERTY_PHONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPhone(@javax.annotation.Nullable String phone) {
    this.phone = phone;
  }


  public ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner governmentId(@javax.annotation.Nullable String governmentId) {
    this.governmentId = governmentId;
    return this;
  }

  /**
   * T.C. kimlik numarası (11 hane)
   * @return governmentId
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_GOVERNMENT_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getGovernmentId() {
    return governmentId;
  }


  @JsonProperty(value = JSON_PROPERTY_GOVERNMENT_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setGovernmentId(@javax.annotation.Nullable String governmentId) {
    this.governmentId = governmentId;
  }


  public ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner birthDate(@javax.annotation.Nullable LocalDate birthDate) {
    this.birthDate = birthDate;
    return this;
  }

  /**
   * ISO 8601 (ör. 1990-05-15)
   * @return birthDate
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_BIRTH_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public LocalDate getBirthDate() {
    return birthDate;
  }


  @JsonProperty(value = JSON_PROPERTY_BIRTH_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setBirthDate(@javax.annotation.Nullable LocalDate birthDate) {
    this.birthDate = birthDate;
  }


  public ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner sendSms(@javax.annotation.Nullable Boolean sendSms) {
    this.sendSms = sendSms;
    return this;
  }

  /**
   * Get sendSms
   * @return sendSms
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_SEND_SMS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getSendSms() {
    return sendSms;
  }


  @JsonProperty(value = JSON_PROPERTY_SEND_SMS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSendSms(@javax.annotation.Nullable Boolean sendSms) {
    this.sendSms = sendSms;
  }


  public ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner sendEmail(@javax.annotation.Nullable Boolean sendEmail) {
    this.sendEmail = sendEmail;
    return this;
  }

  /**
   * Get sendEmail
   * @return sendEmail
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_SEND_EMAIL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getSendEmail() {
    return sendEmail;
  }


  @JsonProperty(value = JSON_PROPERTY_SEND_EMAIL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSendEmail(@javax.annotation.Nullable Boolean sendEmail) {
    this.sendEmail = sendEmail;
  }


  public ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner customMessage(@javax.annotation.Nullable String customMessage) {
    this.customMessage = customMessage;
    return this;
  }

  /**
   * Bu tarafa özel davet mesajı
   * @return customMessage
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_CUSTOM_MESSAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getCustomMessage() {
    return customMessage;
  }


  @JsonProperty(value = JSON_PROPERTY_CUSTOM_MESSAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCustomMessage(@javax.annotation.Nullable String customMessage) {
    this.customMessage = customMessage;
  }


  public ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner variables(@javax.annotation.Nullable Object variables) {
    this.variables = variables;
    return this;
  }

  /**
   * Bu tarafa özel değişkenler
   * @return variables
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_VARIABLES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Object getVariables() {
    return variables;
  }


  @JsonProperty(value = JSON_PROPERTY_VARIABLES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setVariables(@javax.annotation.Nullable Object variables) {
    this.variables = variables;
  }


  /**
   * Return true if this _api_v1_demands_bulk_post_request_rows_inner_party_mapping_inner object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner apiV1DemandsBulkPostRequestRowsInnerPartyMappingInner = (ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner) o;
    return Objects.equals(this.templatePartyId, apiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.templatePartyId) &&
        Objects.equals(this.firstName, apiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.firstName) &&
        Objects.equals(this.lastName, apiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.lastName) &&
        Objects.equals(this.email, apiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.email) &&
        Objects.equals(this.phone, apiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.phone) &&
        Objects.equals(this.governmentId, apiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.governmentId) &&
        Objects.equals(this.birthDate, apiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.birthDate) &&
        Objects.equals(this.sendSms, apiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.sendSms) &&
        Objects.equals(this.sendEmail, apiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.sendEmail) &&
        Objects.equals(this.customMessage, apiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.customMessage) &&
        Objects.equals(this.variables, apiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.variables);
  }

  @Override
  public int hashCode() {
    return Objects.hash(templatePartyId, firstName, lastName, email, phone, governmentId, birthDate, sendSms, sendEmail, customMessage, variables);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner {\n");
    sb.append("    templatePartyId: ").append(toIndentedString(templatePartyId)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    governmentId: ").append(toIndentedString(governmentId)).append("\n");
    sb.append("    birthDate: ").append(toIndentedString(birthDate)).append("\n");
    sb.append("    sendSms: ").append(toIndentedString(sendSms)).append("\n");
    sb.append("    sendEmail: ").append(toIndentedString(sendEmail)).append("\n");
    sb.append("    customMessage: ").append(toIndentedString(customMessage)).append("\n");
    sb.append("    variables: ").append(toIndentedString(variables)).append("\n");
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

    // add `template_party_id` to the URL query string
    if (getTemplatePartyId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%stemplate_party_id%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTemplatePartyId()))));
    }

    // add `first_name` to the URL query string
    if (getFirstName() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sfirst_name%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getFirstName()))));
    }

    // add `last_name` to the URL query string
    if (getLastName() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%slast_name%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getLastName()))));
    }

    // add `email` to the URL query string
    if (getEmail() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%semail%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEmail()))));
    }

    // add `phone` to the URL query string
    if (getPhone() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sphone%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPhone()))));
    }

    // add `government_id` to the URL query string
    if (getGovernmentId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sgovernment_id%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getGovernmentId()))));
    }

    // add `birth_date` to the URL query string
    if (getBirthDate() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sbirth_date%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getBirthDate()))));
    }

    // add `send_sms` to the URL query string
    if (getSendSms() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%ssend_sms%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSendSms()))));
    }

    // add `send_email` to the URL query string
    if (getSendEmail() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%ssend_email%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSendEmail()))));
    }

    // add `custom_message` to the URL query string
    if (getCustomMessage() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%scustom_message%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCustomMessage()))));
    }

    // add `variables` to the URL query string
    if (getVariables() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%svariables%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getVariables()))));
    }

    return joiner.toString();
  }
}

