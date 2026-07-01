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
import java.util.Arrays;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * TemplateVariable
 */
@JsonPropertyOrder({
  TemplateVariable.JSON_PROPERTY_SLUG,
  TemplateVariable.JSON_PROPERTY_LABEL,
  TemplateVariable.JSON_PROPERTY_ITEM_TYPE,
  TemplateVariable.JSON_PROPERTY_IS_REQUIRED,
  TemplateVariable.JSON_PROPERTY_DEFAULT_SOURCE,
  TemplateVariable.JSON_PROPERTY_TEMPLATE_PARTY_ID
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-07-01T14:50:26.407096+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class TemplateVariable {
  public static final String JSON_PROPERTY_SLUG = "slug";
  @javax.annotation.Nullable
  private String slug;

  public static final String JSON_PROPERTY_LABEL = "label";
  @javax.annotation.Nullable
  private String label;

  /**
   * Gets or Sets itemType
   */
  public enum ItemTypeEnum {
    DYNAMIC_TEXT(String.valueOf("dynamic_text")),
    
    CELLS(String.valueOf("cells")),
    
    DATE(String.valueOf("date")),
    
    DROPDOWN(String.valueOf("dropdown")),
    
    TEXT(String.valueOf("text"));

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
  @javax.annotation.Nullable
  private ItemTypeEnum itemType;

  public static final String JSON_PROPERTY_IS_REQUIRED = "is_required";
  @javax.annotation.Nullable
  private Boolean isRequired;

  public static final String JSON_PROPERTY_DEFAULT_SOURCE = "default_source";
  private JsonNullable<String> defaultSource = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_TEMPLATE_PARTY_ID = "template_party_id";
  private JsonNullable<String> templatePartyId = JsonNullable.<String>undefined();

  public TemplateVariable() { 
  }

  public TemplateVariable slug(@javax.annotation.Nullable String slug) {
    this.slug = slug;
    return this;
  }

  /**
   * Get slug
   * @return slug
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_SLUG, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getSlug() {
    return slug;
  }


  @JsonProperty(value = JSON_PROPERTY_SLUG, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSlug(@javax.annotation.Nullable String slug) {
    this.slug = slug;
  }


  public TemplateVariable label(@javax.annotation.Nullable String label) {
    this.label = label;
    return this;
  }

  /**
   * Get label
   * @return label
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_LABEL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getLabel() {
    return label;
  }


  @JsonProperty(value = JSON_PROPERTY_LABEL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setLabel(@javax.annotation.Nullable String label) {
    this.label = label;
  }


  public TemplateVariable itemType(@javax.annotation.Nullable ItemTypeEnum itemType) {
    this.itemType = itemType;
    return this;
  }

  /**
   * Get itemType
   * @return itemType
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_ITEM_TYPE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public ItemTypeEnum getItemType() {
    return itemType;
  }


  @JsonProperty(value = JSON_PROPERTY_ITEM_TYPE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setItemType(@javax.annotation.Nullable ItemTypeEnum itemType) {
    this.itemType = itemType;
  }


  public TemplateVariable isRequired(@javax.annotation.Nullable Boolean isRequired) {
    this.isRequired = isRequired;
    return this;
  }

  /**
   * Get isRequired
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


  public TemplateVariable defaultSource(@javax.annotation.Nullable String defaultSource) {
    this.defaultSource = JsonNullable.<String>of(defaultSource);
    return this;
  }

  /**
   * Doluysa item değeri otomatik olarak bu kaynaktan dolar (örn. signer.full_name → party_mapping&#39;teki ad+soyad). variables payload&#39;ında override edilebilir.  **Kullanılabilir sistem değişkenleri:**  İmzalayan (party-bağlı, render anında çözülür): - &#x60;signer.first_name&#x60;, &#x60;signer.last_name&#x60;, &#x60;signer.full_name&#x60; - &#x60;signer.email&#x60;, &#x60;signer.phone&#x60;, &#x60;signer.government_id&#x60; - &#x60;signer.birth_date&#x60; — İmzalayanın doğum tarihi (gg.aa.yyyy).   Source: &#x60;party_mapping[i].birth_date&#x60; API alanı. - &#x60;signer.sign_date&#x60; — İmzalayanın imza tarihi (gg.aa.yyyy);   imzalanmadıysa boş. Source: server-computed   (&#x60;DemandContractParty.sign_timestamp&#x60;), API üzerinden   settable DEĞİL.  Sözleşme: &#x60;contract.title&#x60;, &#x60;contract.created_date&#x60;, &#x60;contract.expiry_date&#x60;, &#x60;contract.id&#x60;  Gönderen: &#x60;sender.full_name&#x60;, &#x60;sender.email&#x60;, &#x60;sender.company_name&#x60;  Tarih: &#x60;current.date&#x60;, &#x60;current.datetime&#x60;  **Precedence — TC alanı ve diğer slug çakışmaları:** Eğer &#x60;party_mapping[i].government_id&#x60; ile &#x60;party_mapping[i].variables.tc_kimlik&#x60; (veya başka slug-eşleşmeli variable) aynı anda gönderilirse, **&#x60;variables.&lt;slug&gt;&#x60; öncelikli** olur. Bunun nedeni &#x60;applyPartyAwareVariables&#x60; slug yazımını önce uygular; system variable autofill (&#x60;signer.government_id&#x60; defaultSource&#39;u) sonra çalışır ve dolu alanları atlar. 
   * @return defaultSource
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getDefaultSource() {
        return defaultSource.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_DEFAULT_SOURCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getDefaultSource_JsonNullable() {
    return defaultSource;
  }
  
  @JsonProperty(JSON_PROPERTY_DEFAULT_SOURCE)
  public void setDefaultSource_JsonNullable(JsonNullable<String> defaultSource) {
    this.defaultSource = defaultSource;
  }

  public void setDefaultSource(@javax.annotation.Nullable String defaultSource) {
    this.defaultSource = JsonNullable.<String>of(defaultSource);
  }


  public TemplateVariable templatePartyId(@javax.annotation.Nullable String templatePartyId) {
    this.templatePartyId = JsonNullable.<String>of(templatePartyId);
    return this;
  }

  /**
   * Bu field&#39;in sahibi olan template parti id&#39;si. NULL ise root scope (party_mapping dışında) — kök variables&#39;a göndermeniz gerekir. Doluysa party_mapping[i].variables&#39;ta i bu id ile eşleşen partinin altına göndermeniz gerekir. 
   * @return templatePartyId
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public String getTemplatePartyId() {
        return templatePartyId.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_TEMPLATE_PARTY_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getTemplatePartyId_JsonNullable() {
    return templatePartyId;
  }
  
  @JsonProperty(JSON_PROPERTY_TEMPLATE_PARTY_ID)
  public void setTemplatePartyId_JsonNullable(JsonNullable<String> templatePartyId) {
    this.templatePartyId = templatePartyId;
  }

  public void setTemplatePartyId(@javax.annotation.Nullable String templatePartyId) {
    this.templatePartyId = JsonNullable.<String>of(templatePartyId);
  }


  /**
   * Return true if this TemplateVariable object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TemplateVariable templateVariable = (TemplateVariable) o;
    return Objects.equals(this.slug, templateVariable.slug) &&
        Objects.equals(this.label, templateVariable.label) &&
        Objects.equals(this.itemType, templateVariable.itemType) &&
        Objects.equals(this.isRequired, templateVariable.isRequired) &&
        equalsNullable(this.defaultSource, templateVariable.defaultSource) &&
        equalsNullable(this.templatePartyId, templateVariable.templatePartyId);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(slug, label, itemType, isRequired, hashCodeNullable(defaultSource), hashCodeNullable(templatePartyId));
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
    sb.append("class TemplateVariable {\n");
    sb.append("    slug: ").append(toIndentedString(slug)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    itemType: ").append(toIndentedString(itemType)).append("\n");
    sb.append("    isRequired: ").append(toIndentedString(isRequired)).append("\n");
    sb.append("    defaultSource: ").append(toIndentedString(defaultSource)).append("\n");
    sb.append("    templatePartyId: ").append(toIndentedString(templatePartyId)).append("\n");
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

    // add `slug` to the URL query string
    if (getSlug() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sslug%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSlug()))));
    }

    // add `label` to the URL query string
    if (getLabel() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%slabel%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getLabel()))));
    }

    // add `item_type` to the URL query string
    if (getItemType() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sitem_type%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getItemType()))));
    }

    // add `is_required` to the URL query string
    if (getIsRequired() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sis_required%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIsRequired()))));
    }

    // add `default_source` to the URL query string
    if (getDefaultSource() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sdefault_source%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDefaultSource()))));
    }

    // add `template_party_id` to the URL query string
    if (getTemplatePartyId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%stemplate_party_id%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTemplatePartyId()))));
    }

    return joiner.toString();
  }
}

