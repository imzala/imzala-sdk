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
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * Sözleşme sayfasına yerleştirilen alan tanımı. Tüm koordinatlar sayfa boyutuna göre normalize edilmiş [0,1] aralığında. Origin top-left (PDF/canvas standardı). 
 */
@JsonPropertyOrder({
  PageItem.JSON_PROPERTY_PAGE_ID,
  PageItem.JSON_PROPERTY_PARTY_ID,
  PageItem.JSON_PROPERTY_ITEM_TYPE,
  PageItem.JSON_PROPERTY_POSITION_X,
  PageItem.JSON_PROPERTY_POSITION_Y,
  PageItem.JSON_PROPERTY_WIDTH,
  PageItem.JSON_PROPERTY_HEIGHT,
  PageItem.JSON_PROPERTY_IS_REQUIRED,
  PageItem.JSON_PROPERTY_SLUG,
  PageItem.JSON_PROPERTY_LABEL,
  PageItem.JSON_PROPERTY_CONFIG
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-07-01T14:50:26.407096+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class PageItem {
  public static final String JSON_PROPERTY_PAGE_ID = "page_id";
  @javax.annotation.Nonnull
  private Integer pageId;

  public static final String JSON_PROPERTY_PARTY_ID = "party_id";
  private JsonNullable<UUID> partyId = JsonNullable.<UUID>undefined();

  /**
   * Gets or Sets itemType
   */
  public enum ItemTypeEnum {
    SIGNATURE(String.valueOf("signature")),
    
    TEXT(String.valueOf("text")),
    
    DYNAMIC_TEXT(String.valueOf("dynamic_text")),
    
    CELLS(String.valueOf("cells")),
    
    DATE(String.valueOf("date")),
    
    DROPDOWN(String.valueOf("dropdown")),
    
    CHECKBOX(String.valueOf("checkbox")),
    
    RADIO(String.valueOf("radio")),
    
    STAMP(String.valueOf("stamp"));

    private String value;

    ItemTypeEnum(String value) {
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
    public static ItemTypeEnum fromValue(String value) {
      for (ItemTypeEnum b : ItemTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_ITEM_TYPE = "item_type";
  @javax.annotation.Nonnull
  private ItemTypeEnum itemType;

  public static final String JSON_PROPERTY_POSITION_X = "position_x";
  @javax.annotation.Nonnull
  private BigDecimal positionX;

  public static final String JSON_PROPERTY_POSITION_Y = "position_y";
  @javax.annotation.Nonnull
  private BigDecimal positionY;

  public static final String JSON_PROPERTY_WIDTH = "width";
  @javax.annotation.Nonnull
  private BigDecimal width;

  public static final String JSON_PROPERTY_HEIGHT = "height";
  @javax.annotation.Nonnull
  private BigDecimal height;

  public static final String JSON_PROPERTY_IS_REQUIRED = "is_required";
  @javax.annotation.Nullable
  private Boolean isRequired = false;

  public static final String JSON_PROPERTY_SLUG = "slug";
  private JsonNullable<String> slug = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_LABEL = "label";
  private JsonNullable<String> label = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_CONFIG = "config";
  private JsonNullable<Object> config = JsonNullable.<Object>undefined();

  public PageItem() { 
  }

  public PageItem pageId(@javax.annotation.Nonnull Integer pageId) {
    this.pageId = pageId;
    return this;
  }

  /**
   * AgreementPage.id (&#x60;/upload&#x60; response&#39;undaki &#x60;pages[].id&#x60;)
   * @return pageId
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_PAGE_ID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getPageId() {
    return pageId;
  }


  @JsonProperty(value = JSON_PROPERTY_PAGE_ID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPageId(@javax.annotation.Nonnull Integer pageId) {
    this.pageId = pageId;
  }


  public PageItem partyId(@javax.annotation.Nullable UUID partyId) {
    this.partyId = JsonNullable.<UUID>of(partyId);
    return this;
  }

  /**
   * &#x60;signature&#x60; ve doldurulabilir alanlar (&#x60;dynamic_text&#x60;, &#x60;cells&#x60;, &#x60;date&#x60;, &#x60;dropdown&#x60;, &#x60;checkbox&#x60;, &#x60;radio&#x60;) için **zorunlu** — alanı dolduracak/imzalayacak partinin id&#39;si (&#x60;signing_urls[].party_id&#x60;). &#x60;text&#x60; ve &#x60;stamp&#x60; için null. 
   * @return partyId
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public UUID getPartyId() {
        return partyId.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_PARTY_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<UUID> getPartyId_JsonNullable() {
    return partyId;
  }
  
  @JsonProperty(JSON_PROPERTY_PARTY_ID)
  public void setPartyId_JsonNullable(JsonNullable<UUID> partyId) {
    this.partyId = partyId;
  }

  public void setPartyId(@javax.annotation.Nullable UUID partyId) {
    this.partyId = JsonNullable.<UUID>of(partyId);
  }


  public PageItem itemType(@javax.annotation.Nonnull ItemTypeEnum itemType) {
    this.itemType = itemType;
    return this;
  }

  /**
   * Get itemType
   * @return itemType
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_ITEM_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ItemTypeEnum getItemType() {
    return itemType;
  }


  @JsonProperty(value = JSON_PROPERTY_ITEM_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setItemType(@javax.annotation.Nonnull ItemTypeEnum itemType) {
    this.itemType = itemType;
  }


  public PageItem positionX(@javax.annotation.Nonnull BigDecimal positionX) {
    this.positionX = positionX;
    return this;
  }

  /**
   * Sayfa genişliğine göre x koordinatı (sol&#x3D;0)
   * minimum: 0
   * maximum: 1
   * @return positionX
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_POSITION_X, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public BigDecimal getPositionX() {
    return positionX;
  }


  @JsonProperty(value = JSON_PROPERTY_POSITION_X, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPositionX(@javax.annotation.Nonnull BigDecimal positionX) {
    this.positionX = positionX;
  }


  public PageItem positionY(@javax.annotation.Nonnull BigDecimal positionY) {
    this.positionY = positionY;
    return this;
  }

  /**
   * Sayfa yüksekliğine göre y koordinatı (üst&#x3D;0)
   * minimum: 0
   * maximum: 1
   * @return positionY
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_POSITION_Y, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public BigDecimal getPositionY() {
    return positionY;
  }


  @JsonProperty(value = JSON_PROPERTY_POSITION_Y, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPositionY(@javax.annotation.Nonnull BigDecimal positionY) {
    this.positionY = positionY;
  }


  public PageItem width(@javax.annotation.Nonnull BigDecimal width) {
    this.width = width;
    return this;
  }

  /**
   * Get width
   * maximum: 1
   * @return width
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_WIDTH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public BigDecimal getWidth() {
    return width;
  }


  @JsonProperty(value = JSON_PROPERTY_WIDTH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setWidth(@javax.annotation.Nonnull BigDecimal width) {
    this.width = width;
  }


  public PageItem height(@javax.annotation.Nonnull BigDecimal height) {
    this.height = height;
    return this;
  }

  /**
   * Get height
   * maximum: 1
   * @return height
   */
  @javax.annotation.Nonnull
  @JsonProperty(value = JSON_PROPERTY_HEIGHT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public BigDecimal getHeight() {
    return height;
  }


  @JsonProperty(value = JSON_PROPERTY_HEIGHT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setHeight(@javax.annotation.Nonnull BigDecimal height) {
    this.height = height;
  }


  public PageItem isRequired(@javax.annotation.Nullable Boolean isRequired) {
    this.isRequired = isRequired;
    return this;
  }

  /**
   * İmza/alan zorunlu mu — tarafın bu alanı doldurmadan imzalayamadığı
   * @return isRequired
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_IS_REQUIRED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getIsRequired() {
    return isRequired;
  }


  @JsonProperty(value = JSON_PROPERTY_IS_REQUIRED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsRequired(@javax.annotation.Nullable Boolean isRequired) {
    this.isRequired = isRequired;
  }


  public PageItem slug(@javax.annotation.Nullable String slug) {
    this.slug = JsonNullable.<String>of(slug);
    return this;
  }

  /**
   * Alan tanımlayıcı (snake_case, 2-50 karakter). Doldurulabilir alanlar için **önerilir**. &#x60;dynamic_text&#x60;/&#x60;cells&#x60; gibi değişken alanlarda &#x60;config.defaultSource&#x60; ile system değişkenleri (&#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.government_id}}&#x60; vb.) bağlanır. 
   * @return slug
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getSlug() {
        return slug.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_SLUG, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getSlug_JsonNullable() {
    return slug;
  }
  
  @JsonProperty(JSON_PROPERTY_SLUG)
  public void setSlug_JsonNullable(JsonNullable<String> slug) {
    this.slug = slug;
  }

  public void setSlug(@javax.annotation.Nullable String slug) {
    this.slug = JsonNullable.<String>of(slug);
  }


  public PageItem label(@javax.annotation.Nullable String label) {
    this.label = JsonNullable.<String>of(label);
    return this;
  }

  /**
   * Kullanıcıya gösterilecek etiket
   * @return label
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getLabel() {
        return label.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_LABEL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getLabel_JsonNullable() {
    return label;
  }
  
  @JsonProperty(JSON_PROPERTY_LABEL)
  public void setLabel_JsonNullable(JsonNullable<String> label) {
    this.label = label;
  }

  public void setLabel(@javax.annotation.Nullable String label) {
    this.label = JsonNullable.<String>of(label);
  }


  public PageItem config(@javax.annotation.Nullable Object config) {
    this.config = JsonNullable.<Object>of(config);
    return this;
  }

  /**
   * Item type&#39;a özgü konfigürasyon: - &#x60;dynamic_text&#x60;: &#x60;{ defaultSource, defaultValue }&#x60; - &#x60;cells&#x60;: &#x60;{ cellCount, defaultSource }&#x60; - &#x60;date&#x60;: &#x60;{ defaultSource, defaultValue }&#x60; - &#x60;dropdown&#x60;/&#x60;radio&#x60;: &#x60;{ options: [{label, value}], defaultValue }&#x60; - &#x60;checkbox&#x60;: &#x60;{ checkedByDefault }&#x60; - &#x60;stamp&#x60;: &#x60;{ stampData }&#x60; (base64 data URL) 
   * @return config
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public Object getConfig() {
        return config.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_CONFIG, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<Object> getConfig_JsonNullable() {
    return config;
  }
  
  @JsonProperty(JSON_PROPERTY_CONFIG)
  public void setConfig_JsonNullable(JsonNullable<Object> config) {
    this.config = config;
  }

  public void setConfig(@javax.annotation.Nullable Object config) {
    this.config = JsonNullable.<Object>of(config);
  }


  /**
   * Return true if this PageItem object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PageItem pageItem = (PageItem) o;
    return Objects.equals(this.pageId, pageItem.pageId) &&
        equalsNullable(this.partyId, pageItem.partyId) &&
        Objects.equals(this.itemType, pageItem.itemType) &&
        Objects.equals(this.positionX, pageItem.positionX) &&
        Objects.equals(this.positionY, pageItem.positionY) &&
        Objects.equals(this.width, pageItem.width) &&
        Objects.equals(this.height, pageItem.height) &&
        Objects.equals(this.isRequired, pageItem.isRequired) &&
        equalsNullable(this.slug, pageItem.slug) &&
        equalsNullable(this.label, pageItem.label) &&
        equalsNullable(this.config, pageItem.config);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(pageId, hashCodeNullable(partyId), itemType, positionX, positionY, width, height, isRequired, hashCodeNullable(slug), hashCodeNullable(label), hashCodeNullable(config));
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
    sb.append("class PageItem {\n");
    sb.append("    pageId: ").append(toIndentedString(pageId)).append("\n");
    sb.append("    partyId: ").append(toIndentedString(partyId)).append("\n");
    sb.append("    itemType: ").append(toIndentedString(itemType)).append("\n");
    sb.append("    positionX: ").append(toIndentedString(positionX)).append("\n");
    sb.append("    positionY: ").append(toIndentedString(positionY)).append("\n");
    sb.append("    width: ").append(toIndentedString(width)).append("\n");
    sb.append("    height: ").append(toIndentedString(height)).append("\n");
    sb.append("    isRequired: ").append(toIndentedString(isRequired)).append("\n");
    sb.append("    slug: ").append(toIndentedString(slug)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    config: ").append(toIndentedString(config)).append("\n");
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

    // add `page_id` to the URL query string
    if (getPageId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%spage_id%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPageId()))));
    }

    // add `party_id` to the URL query string
    if (getPartyId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sparty_id%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPartyId()))));
    }

    // add `item_type` to the URL query string
    if (getItemType() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sitem_type%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getItemType()))));
    }

    // add `position_x` to the URL query string
    if (getPositionX() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sposition_x%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPositionX()))));
    }

    // add `position_y` to the URL query string
    if (getPositionY() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sposition_y%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPositionY()))));
    }

    // add `width` to the URL query string
    if (getWidth() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%swidth%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getWidth()))));
    }

    // add `height` to the URL query string
    if (getHeight() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sheight%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getHeight()))));
    }

    // add `is_required` to the URL query string
    if (getIsRequired() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sis_required%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIsRequired()))));
    }

    // add `slug` to the URL query string
    if (getSlug() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sslug%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSlug()))));
    }

    // add `label` to the URL query string
    if (getLabel() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%slabel%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getLabel()))));
    }

    // add `config` to the URL query string
    if (getConfig() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sconfig%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getConfig()))));
    }

    return joiner.toString();
  }
}

