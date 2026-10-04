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
import java.util.Arrays;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * ApiV1ContactsPostRequest
 */
@JsonPropertyOrder({
  ApiV1ContactsPostRequest.JSON_PROPERTY_FIRST_NAME,
  ApiV1ContactsPostRequest.JSON_PROPERTY_LAST_NAME,
  ApiV1ContactsPostRequest.JSON_PROPERTY_EMAIL,
  ApiV1ContactsPostRequest.JSON_PROPERTY_PHONE,
  ApiV1ContactsPostRequest.JSON_PROPERTY_JOB_TITLE,
  ApiV1ContactsPostRequest.JSON_PROPERTY_COMPANY_ID,
  ApiV1ContactsPostRequest.JSON_PROPERTY_NOTES,
  ApiV1ContactsPostRequest.JSON_PROPERTY_ADDRESS_COUNTRY,
  ApiV1ContactsPostRequest.JSON_PROPERTY_ADDRESS_CITY,
  ApiV1ContactsPostRequest.JSON_PROPERTY_ADDRESS_DISTRICT,
  ApiV1ContactsPostRequest.JSON_PROPERTY_ADDRESS_LINE
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-10-05T01:06:12.119322+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class ApiV1ContactsPostRequest {
  public static final String JSON_PROPERTY_FIRST_NAME = "first_name";
  @javax.annotation.Nonnull
  private String firstName;

  public static final String JSON_PROPERTY_LAST_NAME = "last_name";
  @javax.annotation.Nonnull
  private String lastName;

  public static final String JSON_PROPERTY_EMAIL = "email";
  private JsonNullable<String> email = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_PHONE = "phone";
  private JsonNullable<String> phone = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_JOB_TITLE = "job_title";
  private JsonNullable<String> jobTitle = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_COMPANY_ID = "company_id";
  private JsonNullable<UUID> companyId = JsonNullable.<UUID>undefined();

  public static final String JSON_PROPERTY_NOTES = "notes";
  private JsonNullable<String> notes = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_ADDRESS_COUNTRY = "address_country";
  private JsonNullable<String> addressCountry = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_ADDRESS_CITY = "address_city";
  private JsonNullable<String> addressCity = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_ADDRESS_DISTRICT = "address_district";
  private JsonNullable<String> addressDistrict = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_ADDRESS_LINE = "address_line";
  private JsonNullable<String> addressLine = JsonNullable.<String>undefined();

  public ApiV1ContactsPostRequest() { 
  }

  public ApiV1ContactsPostRequest firstName(@javax.annotation.Nonnull String firstName) {
    this.firstName = firstName;
    return this;
  }

  /**
   * Get firstName
   * @return firstName
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_FIRST_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getFirstName() {
    return firstName;
  }


  @JsonProperty(value = JSON_PROPERTY_FIRST_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setFirstName(@javax.annotation.Nonnull String firstName) {
    this.firstName = firstName;
  }


  public ApiV1ContactsPostRequest lastName(@javax.annotation.Nonnull String lastName) {
    this.lastName = lastName;
    return this;
  }

  /**
   * Get lastName
   * @return lastName
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_LAST_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getLastName() {
    return lastName;
  }


  @JsonProperty(value = JSON_PROPERTY_LAST_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLastName(@javax.annotation.Nonnull String lastName) {
    this.lastName = lastName;
  }


  public ApiV1ContactsPostRequest email(@javax.annotation.Nullable String email) {
    this.email = JsonNullable.<String>of(email);
    return this;
  }

  /**
   * Get email
   * @return email
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getEmail() {
        return email.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_EMAIL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getEmail_JsonNullable() {
    return email;
  }
  
  @JsonProperty(JSON_PROPERTY_EMAIL)
  public void setEmail_JsonNullable(JsonNullable<String> email) {
    this.email = email;
  }

  public void setEmail(@javax.annotation.Nullable String email) {
    this.email = JsonNullable.<String>of(email);
  }


  public ApiV1ContactsPostRequest phone(@javax.annotation.Nullable String phone) {
    this.phone = JsonNullable.<String>of(phone);
    return this;
  }

  /**
   * Get phone
   * @return phone
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getPhone() {
        return phone.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_PHONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getPhone_JsonNullable() {
    return phone;
  }
  
  @JsonProperty(JSON_PROPERTY_PHONE)
  public void setPhone_JsonNullable(JsonNullable<String> phone) {
    this.phone = phone;
  }

  public void setPhone(@javax.annotation.Nullable String phone) {
    this.phone = JsonNullable.<String>of(phone);
  }


  public ApiV1ContactsPostRequest jobTitle(@javax.annotation.Nullable String jobTitle) {
    this.jobTitle = JsonNullable.<String>of(jobTitle);
    return this;
  }

  /**
   * Get jobTitle
   * @return jobTitle
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getJobTitle() {
        return jobTitle.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_JOB_TITLE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getJobTitle_JsonNullable() {
    return jobTitle;
  }
  
  @JsonProperty(JSON_PROPERTY_JOB_TITLE)
  public void setJobTitle_JsonNullable(JsonNullable<String> jobTitle) {
    this.jobTitle = jobTitle;
  }

  public void setJobTitle(@javax.annotation.Nullable String jobTitle) {
    this.jobTitle = JsonNullable.<String>of(jobTitle);
  }


  public ApiV1ContactsPostRequest companyId(@javax.annotation.Nullable UUID companyId) {
    this.companyId = JsonNullable.<UUID>of(companyId);
    return this;
  }

  /**
   * Get companyId
   * @return companyId
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public UUID getCompanyId() {
        return companyId.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_COMPANY_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<UUID> getCompanyId_JsonNullable() {
    return companyId;
  }
  
  @JsonProperty(JSON_PROPERTY_COMPANY_ID)
  public void setCompanyId_JsonNullable(JsonNullable<UUID> companyId) {
    this.companyId = companyId;
  }

  public void setCompanyId(@javax.annotation.Nullable UUID companyId) {
    this.companyId = JsonNullable.<UUID>of(companyId);
  }


  public ApiV1ContactsPostRequest notes(@javax.annotation.Nullable String notes) {
    this.notes = JsonNullable.<String>of(notes);
    return this;
  }

  /**
   * Get notes
   * @return notes
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getNotes() {
        return notes.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_NOTES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getNotes_JsonNullable() {
    return notes;
  }
  
  @JsonProperty(JSON_PROPERTY_NOTES)
  public void setNotes_JsonNullable(JsonNullable<String> notes) {
    this.notes = notes;
  }

  public void setNotes(@javax.annotation.Nullable String notes) {
    this.notes = JsonNullable.<String>of(notes);
  }


  public ApiV1ContactsPostRequest addressCountry(@javax.annotation.Nullable String addressCountry) {
    this.addressCountry = JsonNullable.<String>of(addressCountry);
    return this;
  }

  /**
   * Get addressCountry
   * @return addressCountry
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getAddressCountry() {
        return addressCountry.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_ADDRESS_COUNTRY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getAddressCountry_JsonNullable() {
    return addressCountry;
  }
  
  @JsonProperty(JSON_PROPERTY_ADDRESS_COUNTRY)
  public void setAddressCountry_JsonNullable(JsonNullable<String> addressCountry) {
    this.addressCountry = addressCountry;
  }

  public void setAddressCountry(@javax.annotation.Nullable String addressCountry) {
    this.addressCountry = JsonNullable.<String>of(addressCountry);
  }


  public ApiV1ContactsPostRequest addressCity(@javax.annotation.Nullable String addressCity) {
    this.addressCity = JsonNullable.<String>of(addressCity);
    return this;
  }

  /**
   * Get addressCity
   * @return addressCity
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getAddressCity() {
        return addressCity.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_ADDRESS_CITY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getAddressCity_JsonNullable() {
    return addressCity;
  }
  
  @JsonProperty(JSON_PROPERTY_ADDRESS_CITY)
  public void setAddressCity_JsonNullable(JsonNullable<String> addressCity) {
    this.addressCity = addressCity;
  }

  public void setAddressCity(@javax.annotation.Nullable String addressCity) {
    this.addressCity = JsonNullable.<String>of(addressCity);
  }


  public ApiV1ContactsPostRequest addressDistrict(@javax.annotation.Nullable String addressDistrict) {
    this.addressDistrict = JsonNullable.<String>of(addressDistrict);
    return this;
  }

  /**
   * Get addressDistrict
   * @return addressDistrict
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getAddressDistrict() {
        return addressDistrict.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_ADDRESS_DISTRICT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getAddressDistrict_JsonNullable() {
    return addressDistrict;
  }
  
  @JsonProperty(JSON_PROPERTY_ADDRESS_DISTRICT)
  public void setAddressDistrict_JsonNullable(JsonNullable<String> addressDistrict) {
    this.addressDistrict = addressDistrict;
  }

  public void setAddressDistrict(@javax.annotation.Nullable String addressDistrict) {
    this.addressDistrict = JsonNullable.<String>of(addressDistrict);
  }


  public ApiV1ContactsPostRequest addressLine(@javax.annotation.Nullable String addressLine) {
    this.addressLine = JsonNullable.<String>of(addressLine);
    return this;
  }

  /**
   * Get addressLine
   * @return addressLine
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getAddressLine() {
        return addressLine.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_ADDRESS_LINE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getAddressLine_JsonNullable() {
    return addressLine;
  }
  
  @JsonProperty(JSON_PROPERTY_ADDRESS_LINE)
  public void setAddressLine_JsonNullable(JsonNullable<String> addressLine) {
    this.addressLine = addressLine;
  }

  public void setAddressLine(@javax.annotation.Nullable String addressLine) {
    this.addressLine = JsonNullable.<String>of(addressLine);
  }


  /**
   * Return true if this _api_v1_contacts_post_request object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiV1ContactsPostRequest apiV1ContactsPostRequest = (ApiV1ContactsPostRequest) o;
    return Objects.equals(this.firstName, apiV1ContactsPostRequest.firstName) &&
        Objects.equals(this.lastName, apiV1ContactsPostRequest.lastName) &&
        equalsNullable(this.email, apiV1ContactsPostRequest.email) &&
        equalsNullable(this.phone, apiV1ContactsPostRequest.phone) &&
        equalsNullable(this.jobTitle, apiV1ContactsPostRequest.jobTitle) &&
        equalsNullable(this.companyId, apiV1ContactsPostRequest.companyId) &&
        equalsNullable(this.notes, apiV1ContactsPostRequest.notes) &&
        equalsNullable(this.addressCountry, apiV1ContactsPostRequest.addressCountry) &&
        equalsNullable(this.addressCity, apiV1ContactsPostRequest.addressCity) &&
        equalsNullable(this.addressDistrict, apiV1ContactsPostRequest.addressDistrict) &&
        equalsNullable(this.addressLine, apiV1ContactsPostRequest.addressLine);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstName, lastName, hashCodeNullable(email), hashCodeNullable(phone), hashCodeNullable(jobTitle), hashCodeNullable(companyId), hashCodeNullable(notes), hashCodeNullable(addressCountry), hashCodeNullable(addressCity), hashCodeNullable(addressDistrict), hashCodeNullable(addressLine));
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
    sb.append("class ApiV1ContactsPostRequest {\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    jobTitle: ").append(toIndentedString(jobTitle)).append("\n");
    sb.append("    companyId: ").append(toIndentedString(companyId)).append("\n");
    sb.append("    notes: ").append(toIndentedString(notes)).append("\n");
    sb.append("    addressCountry: ").append(toIndentedString(addressCountry)).append("\n");
    sb.append("    addressCity: ").append(toIndentedString(addressCity)).append("\n");
    sb.append("    addressDistrict: ").append(toIndentedString(addressDistrict)).append("\n");
    sb.append("    addressLine: ").append(toIndentedString(addressLine)).append("\n");
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

    // add `job_title` to the URL query string
    if (getJobTitle() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sjob_title%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getJobTitle()))));
    }

    // add `company_id` to the URL query string
    if (getCompanyId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%scompany_id%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCompanyId()))));
    }

    // add `notes` to the URL query string
    if (getNotes() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%snotes%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getNotes()))));
    }

    // add `address_country` to the URL query string
    if (getAddressCountry() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%saddress_country%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAddressCountry()))));
    }

    // add `address_city` to the URL query string
    if (getAddressCity() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%saddress_city%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAddressCity()))));
    }

    // add `address_district` to the URL query string
    if (getAddressDistrict() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%saddress_district%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAddressDistrict()))));
    }

    // add `address_line` to the URL query string
    if (getAddressLine() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%saddress_line%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAddressLine()))));
    }

    return joiner.toString();
  }
}

