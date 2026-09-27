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
import java.util.Arrays;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * Yapılandırılmış kaşe verisi. Tüm alanlar isteğe bağlıdır. PATCH isteğinde &#x60;null&#x60; veya boş string alanı kaldırır.  Belgeye yalnız gerekli kişisel veriyi yazın; bu verilerin hukuka uygunluğu ve aydınlatması sizin sorumluluğunuzdadır. 
 */
@JsonPropertyOrder({
  StampData.JSON_PROPERTY_COMPANY_NAME,
  StampData.JSON_PROPERTY_PERSONAL_NAME,
  StampData.JSON_PROPERTY_ADDRESS,
  StampData.JSON_PROPERTY_COMPANY_ADDRESS,
  StampData.JSON_PROPERTY_PERSONAL_ADDRESS,
  StampData.JSON_PROPERTY_TAX_NUMBER,
  StampData.JSON_PROPERTY_TAX_OFFICE,
  StampData.JSON_PROPERTY_ID_NUMBER,
  StampData.JSON_PROPERTY_PHONE,
  StampData.JSON_PROPERTY_COMPANY_PHONE,
  StampData.JSON_PROPERTY_PERSONAL_PHONE,
  StampData.JSON_PROPERTY_EMAIL
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-09-28T01:16:51.997481+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class StampData {
  public static final String JSON_PROPERTY_COMPANY_NAME = "companyName";
  private JsonNullable<String> companyName = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_PERSONAL_NAME = "personalName";
  private JsonNullable<String> personalName = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_ADDRESS = "address";
  private JsonNullable<String> address = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_COMPANY_ADDRESS = "companyAddress";
  private JsonNullable<String> companyAddress = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_PERSONAL_ADDRESS = "personalAddress";
  private JsonNullable<String> personalAddress = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_TAX_NUMBER = "taxNumber";
  private JsonNullable<String> taxNumber = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_TAX_OFFICE = "taxOffice";
  private JsonNullable<String> taxOffice = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_ID_NUMBER = "idNumber";
  private JsonNullable<String> idNumber = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_PHONE = "phone";
  private JsonNullable<String> phone = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_COMPANY_PHONE = "companyPhone";
  private JsonNullable<String> companyPhone = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_PERSONAL_PHONE = "personalPhone";
  private JsonNullable<String> personalPhone = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_EMAIL = "email";
  private JsonNullable<String> email = JsonNullable.<String>undefined();

  public StampData() { 
  }

  public StampData companyName(@javax.annotation.Nullable String companyName) {
    this.companyName = JsonNullable.<String>of(companyName);
    return this;
  }

  /**
   * Get companyName
   * @return companyName
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getCompanyName() {
        return companyName.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_COMPANY_NAME, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getCompanyName_JsonNullable() {
    return companyName;
  }
  
  @JsonProperty(JSON_PROPERTY_COMPANY_NAME)
  public void setCompanyName_JsonNullable(JsonNullable<String> companyName) {
    this.companyName = companyName;
  }

  public void setCompanyName(@javax.annotation.Nullable String companyName) {
    this.companyName = JsonNullable.<String>of(companyName);
  }


  public StampData personalName(@javax.annotation.Nullable String personalName) {
    this.personalName = JsonNullable.<String>of(personalName);
    return this;
  }

  /**
   * Get personalName
   * @return personalName
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getPersonalName() {
        return personalName.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_PERSONAL_NAME, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getPersonalName_JsonNullable() {
    return personalName;
  }
  
  @JsonProperty(JSON_PROPERTY_PERSONAL_NAME)
  public void setPersonalName_JsonNullable(JsonNullable<String> personalName) {
    this.personalName = personalName;
  }

  public void setPersonalName(@javax.annotation.Nullable String personalName) {
    this.personalName = JsonNullable.<String>of(personalName);
  }


  public StampData address(@javax.annotation.Nullable String address) {
    this.address = JsonNullable.<String>of(address);
    return this;
  }

  /**
   * Get address
   * @return address
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getAddress() {
        return address.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_ADDRESS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getAddress_JsonNullable() {
    return address;
  }
  
  @JsonProperty(JSON_PROPERTY_ADDRESS)
  public void setAddress_JsonNullable(JsonNullable<String> address) {
    this.address = address;
  }

  public void setAddress(@javax.annotation.Nullable String address) {
    this.address = JsonNullable.<String>of(address);
  }


  public StampData companyAddress(@javax.annotation.Nullable String companyAddress) {
    this.companyAddress = JsonNullable.<String>of(companyAddress);
    return this;
  }

  /**
   * Get companyAddress
   * @return companyAddress
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getCompanyAddress() {
        return companyAddress.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_COMPANY_ADDRESS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getCompanyAddress_JsonNullable() {
    return companyAddress;
  }
  
  @JsonProperty(JSON_PROPERTY_COMPANY_ADDRESS)
  public void setCompanyAddress_JsonNullable(JsonNullable<String> companyAddress) {
    this.companyAddress = companyAddress;
  }

  public void setCompanyAddress(@javax.annotation.Nullable String companyAddress) {
    this.companyAddress = JsonNullable.<String>of(companyAddress);
  }


  public StampData personalAddress(@javax.annotation.Nullable String personalAddress) {
    this.personalAddress = JsonNullable.<String>of(personalAddress);
    return this;
  }

  /**
   * Get personalAddress
   * @return personalAddress
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getPersonalAddress() {
        return personalAddress.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_PERSONAL_ADDRESS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getPersonalAddress_JsonNullable() {
    return personalAddress;
  }
  
  @JsonProperty(JSON_PROPERTY_PERSONAL_ADDRESS)
  public void setPersonalAddress_JsonNullable(JsonNullable<String> personalAddress) {
    this.personalAddress = personalAddress;
  }

  public void setPersonalAddress(@javax.annotation.Nullable String personalAddress) {
    this.personalAddress = JsonNullable.<String>of(personalAddress);
  }


  public StampData taxNumber(@javax.annotation.Nullable String taxNumber) {
    this.taxNumber = JsonNullable.<String>of(taxNumber);
    return this;
  }

  /**
   * Get taxNumber
   * @return taxNumber
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getTaxNumber() {
        return taxNumber.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_TAX_NUMBER, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getTaxNumber_JsonNullable() {
    return taxNumber;
  }
  
  @JsonProperty(JSON_PROPERTY_TAX_NUMBER)
  public void setTaxNumber_JsonNullable(JsonNullable<String> taxNumber) {
    this.taxNumber = taxNumber;
  }

  public void setTaxNumber(@javax.annotation.Nullable String taxNumber) {
    this.taxNumber = JsonNullable.<String>of(taxNumber);
  }


  public StampData taxOffice(@javax.annotation.Nullable String taxOffice) {
    this.taxOffice = JsonNullable.<String>of(taxOffice);
    return this;
  }

  /**
   * Get taxOffice
   * @return taxOffice
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getTaxOffice() {
        return taxOffice.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_TAX_OFFICE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getTaxOffice_JsonNullable() {
    return taxOffice;
  }
  
  @JsonProperty(JSON_PROPERTY_TAX_OFFICE)
  public void setTaxOffice_JsonNullable(JsonNullable<String> taxOffice) {
    this.taxOffice = taxOffice;
  }

  public void setTaxOffice(@javax.annotation.Nullable String taxOffice) {
    this.taxOffice = JsonNullable.<String>of(taxOffice);
  }


  public StampData idNumber(@javax.annotation.Nullable String idNumber) {
    this.idNumber = JsonNullable.<String>of(idNumber);
    return this;
  }

  /**
   * Get idNumber
   * @return idNumber
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getIdNumber() {
        return idNumber.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_ID_NUMBER, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getIdNumber_JsonNullable() {
    return idNumber;
  }
  
  @JsonProperty(JSON_PROPERTY_ID_NUMBER)
  public void setIdNumber_JsonNullable(JsonNullable<String> idNumber) {
    this.idNumber = idNumber;
  }

  public void setIdNumber(@javax.annotation.Nullable String idNumber) {
    this.idNumber = JsonNullable.<String>of(idNumber);
  }


  public StampData phone(@javax.annotation.Nullable String phone) {
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


  public StampData companyPhone(@javax.annotation.Nullable String companyPhone) {
    this.companyPhone = JsonNullable.<String>of(companyPhone);
    return this;
  }

  /**
   * Get companyPhone
   * @return companyPhone
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getCompanyPhone() {
        return companyPhone.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_COMPANY_PHONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getCompanyPhone_JsonNullable() {
    return companyPhone;
  }
  
  @JsonProperty(JSON_PROPERTY_COMPANY_PHONE)
  public void setCompanyPhone_JsonNullable(JsonNullable<String> companyPhone) {
    this.companyPhone = companyPhone;
  }

  public void setCompanyPhone(@javax.annotation.Nullable String companyPhone) {
    this.companyPhone = JsonNullable.<String>of(companyPhone);
  }


  public StampData personalPhone(@javax.annotation.Nullable String personalPhone) {
    this.personalPhone = JsonNullable.<String>of(personalPhone);
    return this;
  }

  /**
   * Get personalPhone
   * @return personalPhone
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getPersonalPhone() {
        return personalPhone.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_PERSONAL_PHONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getPersonalPhone_JsonNullable() {
    return personalPhone;
  }
  
  @JsonProperty(JSON_PROPERTY_PERSONAL_PHONE)
  public void setPersonalPhone_JsonNullable(JsonNullable<String> personalPhone) {
    this.personalPhone = personalPhone;
  }

  public void setPersonalPhone(@javax.annotation.Nullable String personalPhone) {
    this.personalPhone = JsonNullable.<String>of(personalPhone);
  }


  public StampData email(@javax.annotation.Nullable String email) {
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


  /**
   * Return true if this StampData object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StampData stampData = (StampData) o;
    return equalsNullable(this.companyName, stampData.companyName) &&
        equalsNullable(this.personalName, stampData.personalName) &&
        equalsNullable(this.address, stampData.address) &&
        equalsNullable(this.companyAddress, stampData.companyAddress) &&
        equalsNullable(this.personalAddress, stampData.personalAddress) &&
        equalsNullable(this.taxNumber, stampData.taxNumber) &&
        equalsNullable(this.taxOffice, stampData.taxOffice) &&
        equalsNullable(this.idNumber, stampData.idNumber) &&
        equalsNullable(this.phone, stampData.phone) &&
        equalsNullable(this.companyPhone, stampData.companyPhone) &&
        equalsNullable(this.personalPhone, stampData.personalPhone) &&
        equalsNullable(this.email, stampData.email);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(hashCodeNullable(companyName), hashCodeNullable(personalName), hashCodeNullable(address), hashCodeNullable(companyAddress), hashCodeNullable(personalAddress), hashCodeNullable(taxNumber), hashCodeNullable(taxOffice), hashCodeNullable(idNumber), hashCodeNullable(phone), hashCodeNullable(companyPhone), hashCodeNullable(personalPhone), hashCodeNullable(email));
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
    sb.append("class StampData {\n");
    sb.append("    companyName: ").append(toIndentedString(companyName)).append("\n");
    sb.append("    personalName: ").append(toIndentedString(personalName)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    companyAddress: ").append(toIndentedString(companyAddress)).append("\n");
    sb.append("    personalAddress: ").append(toIndentedString(personalAddress)).append("\n");
    sb.append("    taxNumber: ").append(toIndentedString(taxNumber)).append("\n");
    sb.append("    taxOffice: ").append(toIndentedString(taxOffice)).append("\n");
    sb.append("    idNumber: ").append(toIndentedString(idNumber)).append("\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    companyPhone: ").append(toIndentedString(companyPhone)).append("\n");
    sb.append("    personalPhone: ").append(toIndentedString(personalPhone)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
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

    // add `companyName` to the URL query string
    if (getCompanyName() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%scompanyName%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCompanyName()))));
    }

    // add `personalName` to the URL query string
    if (getPersonalName() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%spersonalName%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPersonalName()))));
    }

    // add `address` to the URL query string
    if (getAddress() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%saddress%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAddress()))));
    }

    // add `companyAddress` to the URL query string
    if (getCompanyAddress() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%scompanyAddress%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCompanyAddress()))));
    }

    // add `personalAddress` to the URL query string
    if (getPersonalAddress() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%spersonalAddress%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPersonalAddress()))));
    }

    // add `taxNumber` to the URL query string
    if (getTaxNumber() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%staxNumber%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTaxNumber()))));
    }

    // add `taxOffice` to the URL query string
    if (getTaxOffice() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%staxOffice%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTaxOffice()))));
    }

    // add `idNumber` to the URL query string
    if (getIdNumber() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sidNumber%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIdNumber()))));
    }

    // add `phone` to the URL query string
    if (getPhone() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sphone%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPhone()))));
    }

    // add `companyPhone` to the URL query string
    if (getCompanyPhone() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%scompanyPhone%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCompanyPhone()))));
    }

    // add `personalPhone` to the URL query string
    if (getPersonalPhone() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%spersonalPhone%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPersonalPhone()))));
    }

    // add `email` to the URL query string
    if (getEmail() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%semail%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEmail()))));
    }

    return joiner.toString();
  }
}

