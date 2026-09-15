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
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * Tüm satırlara uygulanan batch-seviye seçenekler (opsiyonel)
 */
@JsonPropertyOrder({
  ApiV1DemandsBulkPostRequestOptions.JSON_PROPERTY_DISPATCH_NOTIFICATIONS,
  ApiV1DemandsBulkPostRequestOptions.JSON_PROPERTY_ORDERED,
  ApiV1DemandsBulkPostRequestOptions.JSON_PROPERTY_SMS_CONTENT,
  ApiV1DemandsBulkPostRequestOptions.JSON_PROPERTY_LANGUAGE,
  ApiV1DemandsBulkPostRequestOptions.JSON_PROPERTY_ENABLE_QES,
  ApiV1DemandsBulkPostRequestOptions.JSON_PROPERTY_QES_PADES_LEVEL,
  ApiV1DemandsBulkPostRequestOptions.JSON_PROPERTY_EIDAS_TIMESTAMP,
  ApiV1DemandsBulkPostRequestOptions.JSON_PROPERTY_REQUIRE_ID_PHOTO,
  ApiV1DemandsBulkPostRequestOptions.JSON_PROPERTY_REQUIRE_LIVENESS_CHECK
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-09-15T10:01:25.885782+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class ApiV1DemandsBulkPostRequestOptions {
  public static final String JSON_PROPERTY_DISPATCH_NOTIFICATIONS = "dispatch_notifications";
  @javax.annotation.Nullable
  private Boolean dispatchNotifications = true;

  public static final String JSON_PROPERTY_ORDERED = "ordered";
  @javax.annotation.Nullable
  private Boolean ordered;

  public static final String JSON_PROPERTY_SMS_CONTENT = "sms_content";
  @javax.annotation.Nullable
  private String smsContent;

  public static final String JSON_PROPERTY_LANGUAGE = "language";
  @javax.annotation.Nullable
  private String language;

  public static final String JSON_PROPERTY_ENABLE_QES = "enable_qes";
  @javax.annotation.Nullable
  private Boolean enableQes;

  /**
   * 🔴 Faturayı etkiler. Yalnız enable_qes ile anlamlıdır: B-T +0, B-LT +0, B-LTA +1 kredi (imzacı başına).
   */
  public enum QesPadesLevelEnum {
    PAD_ES_BASELINE_T(String.valueOf("PAdES_BASELINE_T")),
    
    PAD_ES_BASELINE_LT(String.valueOf("PAdES_BASELINE_LT")),
    
    PAD_ES_BASELINE_LTA(String.valueOf("PAdES_BASELINE_LTA"));

    private String value;

    QesPadesLevelEnum(String value) {
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
    public static QesPadesLevelEnum fromValue(String value) {
      for (QesPadesLevelEnum b : QesPadesLevelEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_QES_PADES_LEVEL = "qes_pades_level";
  @javax.annotation.Nullable
  private QesPadesLevelEnum qesPadesLevel;

  public static final String JSON_PROPERTY_EIDAS_TIMESTAMP = "eidas_timestamp";
  @javax.annotation.Nullable
  private Boolean eidasTimestamp;

  public static final String JSON_PROPERTY_REQUIRE_ID_PHOTO = "require_id_photo";
  @javax.annotation.Nullable
  private Boolean requireIdPhoto;

  public static final String JSON_PROPERTY_REQUIRE_LIVENESS_CHECK = "require_liveness_check";
  @javax.annotation.Nullable
  private Boolean requireLivenessCheck;

  public ApiV1DemandsBulkPostRequestOptions() { 
  }

  public ApiV1DemandsBulkPostRequestOptions dispatchNotifications(@javax.annotation.Nullable Boolean dispatchNotifications) {
    this.dispatchNotifications = dispatchNotifications;
    return this;
  }

  /**
   * false ise davet gönderilmez (yalnızca oluşturulur)
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


  public ApiV1DemandsBulkPostRequestOptions ordered(@javax.annotation.Nullable Boolean ordered) {
    this.ordered = ordered;
    return this;
  }

  /**
   * true ise taraflar sırayla imzalar
   * @return ordered
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_ORDERED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getOrdered() {
    return ordered;
  }


  @JsonProperty(value = JSON_PROPERTY_ORDERED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setOrdered(@javax.annotation.Nullable Boolean ordered) {
    this.ordered = ordered;
  }


  public ApiV1DemandsBulkPostRequestOptions smsContent(@javax.annotation.Nullable String smsContent) {
    this.smsContent = smsContent;
    return this;
  }

  /**
   * SMS davet metni özelleştirmesi (yalnızca kendi SMS sağlayıcısı olan org&#39;lar)
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


  public ApiV1DemandsBulkPostRequestOptions language(@javax.annotation.Nullable String language) {
    this.language = language;
    return this;
  }

  /**
   * Get language
   * @return language
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_LANGUAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getLanguage() {
    return language;
  }


  @JsonProperty(value = JSON_PROPERTY_LANGUAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setLanguage(@javax.annotation.Nullable String language) {
    this.language = language;
  }


  public ApiV1DemandsBulkPostRequestOptions enableQes(@javax.annotation.Nullable Boolean enableQes) {
    this.enableQes = enableQes;
    return this;
  }

  /**
   * 🔴 Faturayı etkiler. Nitelikli imza (QES) sınıfı; imzacı başına taban ücret 1 yerine 2 kredi.
   * @return enableQes
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_ENABLE_QES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getEnableQes() {
    return enableQes;
  }


  @JsonProperty(value = JSON_PROPERTY_ENABLE_QES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEnableQes(@javax.annotation.Nullable Boolean enableQes) {
    this.enableQes = enableQes;
  }


  public ApiV1DemandsBulkPostRequestOptions qesPadesLevel(@javax.annotation.Nullable QesPadesLevelEnum qesPadesLevel) {
    this.qesPadesLevel = qesPadesLevel;
    return this;
  }

  /**
   * 🔴 Faturayı etkiler. Yalnız enable_qes ile anlamlıdır: B-T +0, B-LT +0, B-LTA +1 kredi (imzacı başına).
   * @return qesPadesLevel
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_QES_PADES_LEVEL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public QesPadesLevelEnum getQesPadesLevel() {
    return qesPadesLevel;
  }


  @JsonProperty(value = JSON_PROPERTY_QES_PADES_LEVEL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setQesPadesLevel(@javax.annotation.Nullable QesPadesLevelEnum qesPadesLevel) {
    this.qesPadesLevel = qesPadesLevel;
  }


  public ApiV1DemandsBulkPostRequestOptions eidasTimestamp(@javax.annotation.Nullable Boolean eidasTimestamp) {
    this.eidasTimestamp = eidasTimestamp;
    return this;
  }

  /**
   * 🔴 Faturayı etkiler. AB nitelikli zaman damgası; satır (sözleşme) başına +1 kredi.
   * @return eidasTimestamp
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_EIDAS_TIMESTAMP, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getEidasTimestamp() {
    return eidasTimestamp;
  }


  @JsonProperty(value = JSON_PROPERTY_EIDAS_TIMESTAMP, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEidasTimestamp(@javax.annotation.Nullable Boolean eidasTimestamp) {
    this.eidasTimestamp = eidasTimestamp;
  }


  public ApiV1DemandsBulkPostRequestOptions requireIdPhoto(@javax.annotation.Nullable Boolean requireIdPhoto) {
    this.requireIdPhoto = requireIdPhoto;
    return this;
  }

  /**
   * 🔴 Faturayı etkiler. Kimlik fotoğrafı doğrulaması; imzacı başına +1 kredi.
   * @return requireIdPhoto
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_REQUIRE_ID_PHOTO, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getRequireIdPhoto() {
    return requireIdPhoto;
  }


  @JsonProperty(value = JSON_PROPERTY_REQUIRE_ID_PHOTO, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRequireIdPhoto(@javax.annotation.Nullable Boolean requireIdPhoto) {
    this.requireIdPhoto = requireIdPhoto;
  }


  public ApiV1DemandsBulkPostRequestOptions requireLivenessCheck(@javax.annotation.Nullable Boolean requireLivenessCheck) {
    this.requireLivenessCheck = requireLivenessCheck;
    return this;
  }

  /**
   * 🔴 Faturayı etkiler. Canlılık doğrulaması; imzacı başına +1 kredi.
   * @return requireLivenessCheck
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_REQUIRE_LIVENESS_CHECK, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getRequireLivenessCheck() {
    return requireLivenessCheck;
  }


  @JsonProperty(value = JSON_PROPERTY_REQUIRE_LIVENESS_CHECK, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRequireLivenessCheck(@javax.annotation.Nullable Boolean requireLivenessCheck) {
    this.requireLivenessCheck = requireLivenessCheck;
  }


  /**
   * Return true if this _api_v1_demands_bulk_post_request_options object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiV1DemandsBulkPostRequestOptions apiV1DemandsBulkPostRequestOptions = (ApiV1DemandsBulkPostRequestOptions) o;
    return Objects.equals(this.dispatchNotifications, apiV1DemandsBulkPostRequestOptions.dispatchNotifications) &&
        Objects.equals(this.ordered, apiV1DemandsBulkPostRequestOptions.ordered) &&
        Objects.equals(this.smsContent, apiV1DemandsBulkPostRequestOptions.smsContent) &&
        Objects.equals(this.language, apiV1DemandsBulkPostRequestOptions.language) &&
        Objects.equals(this.enableQes, apiV1DemandsBulkPostRequestOptions.enableQes) &&
        Objects.equals(this.qesPadesLevel, apiV1DemandsBulkPostRequestOptions.qesPadesLevel) &&
        Objects.equals(this.eidasTimestamp, apiV1DemandsBulkPostRequestOptions.eidasTimestamp) &&
        Objects.equals(this.requireIdPhoto, apiV1DemandsBulkPostRequestOptions.requireIdPhoto) &&
        Objects.equals(this.requireLivenessCheck, apiV1DemandsBulkPostRequestOptions.requireLivenessCheck);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dispatchNotifications, ordered, smsContent, language, enableQes, qesPadesLevel, eidasTimestamp, requireIdPhoto, requireLivenessCheck);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiV1DemandsBulkPostRequestOptions {\n");
    sb.append("    dispatchNotifications: ").append(toIndentedString(dispatchNotifications)).append("\n");
    sb.append("    ordered: ").append(toIndentedString(ordered)).append("\n");
    sb.append("    smsContent: ").append(toIndentedString(smsContent)).append("\n");
    sb.append("    language: ").append(toIndentedString(language)).append("\n");
    sb.append("    enableQes: ").append(toIndentedString(enableQes)).append("\n");
    sb.append("    qesPadesLevel: ").append(toIndentedString(qesPadesLevel)).append("\n");
    sb.append("    eidasTimestamp: ").append(toIndentedString(eidasTimestamp)).append("\n");
    sb.append("    requireIdPhoto: ").append(toIndentedString(requireIdPhoto)).append("\n");
    sb.append("    requireLivenessCheck: ").append(toIndentedString(requireLivenessCheck)).append("\n");
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

    // add `dispatch_notifications` to the URL query string
    if (getDispatchNotifications() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sdispatch_notifications%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDispatchNotifications()))));
    }

    // add `ordered` to the URL query string
    if (getOrdered() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sordered%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getOrdered()))));
    }

    // add `sms_content` to the URL query string
    if (getSmsContent() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%ssms_content%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSmsContent()))));
    }

    // add `language` to the URL query string
    if (getLanguage() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%slanguage%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getLanguage()))));
    }

    // add `enable_qes` to the URL query string
    if (getEnableQes() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%senable_qes%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEnableQes()))));
    }

    // add `qes_pades_level` to the URL query string
    if (getQesPadesLevel() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sqes_pades_level%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getQesPadesLevel()))));
    }

    // add `eidas_timestamp` to the URL query string
    if (getEidasTimestamp() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%seidas_timestamp%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEidasTimestamp()))));
    }

    // add `require_id_photo` to the URL query string
    if (getRequireIdPhoto() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%srequire_id_photo%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRequireIdPhoto()))));
    }

    // add `require_liveness_check` to the URL query string
    if (getRequireLivenessCheck() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%srequire_liveness_check%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRequireLivenessCheck()))));
    }

    return joiner.toString();
  }
}

