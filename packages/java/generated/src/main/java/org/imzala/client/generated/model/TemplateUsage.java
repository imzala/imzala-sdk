/*
 * imzala External API
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.10.2 · **Son güncelleme:** 2026-10-06  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır (aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Onaylayan (onay adımı) Şablonda bir taraf **Onaylayan** olarak işaretlenmişse, o şablondan oluşturulan sözleşmede bu taraf belgeyi imzalamaz, onaylar. Onaylayan içeren sözleşmelerde davetler iki aşamada gönderilir: - Sözleşme oluşturulurken (`POST /api/v1/demands`,   `POST /api/v1/demands/bulk`) ve `POST /api/v1/demands/{demandId}/dispatch`   çağrıldığında önce yalnızca onay bekleyen onaylayanlar davet edilir.   Sıralı sözleşmede (`ordered: true`) onaylayanlar da kendi sıralarıyla   davet edilir. - İmzacılar, tüm onaylayanlar onayladıktan sonra otomatik olarak davet   edilir. - Bir onaylayan sözleşmeyi reddederse imzacılar davet edilmez;   `dispatch` çağrısı da kimseye davet göndermez. - Onay vermiş bir onaylayan `dispatch` ile yeniden davet edilmez.  Onaylayan içermeyen sözleşmelerde davet davranışı değişmez.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  `403 AI_TOOL_ACCESS_DISABLED`: organizasyon yöneticisi, organizasyonun yapay zekâ araçlarıyla erişimini kapatmıştır. Bu kontrol resmî İmzala MCP istemcisini (`User-Agent` başlığı `imzala-mcp/` ile başlayan istekler) tanır ve yalnız organizasyon çalışma alanını hedefleyen isteklerde uygulanır. Yanıt gövdesi:  ```json {   \"error\": \"AI_TOOL_ACCESS_DISABLED\",   \"code\": \"AI_TOOL_ACCESS_DISABLED\",   \"message\": \"Kurumunuz yapay zekâ araçlarının erişimini kapattı.\" } ```  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez. - Hatırlatma yalnızca davet sırası gelmiş taraflara gönderilir. Onaylayan   onayını beklerken imzacılar, sıralı sözleşmede sırası henüz gelmemiş   taraflar ve bir onaylayan reddettiyse tüm taraflar `details[]` içinde   `skipped` olarak `reason: \"party_not_eligible\"` ile döner.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (11) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı | | `contract.expiring` | Takip edilen sözleşme bitiş tarihine yaklaşıyor | | `contract.ended` | Takip edilen sözleşmenin platformdaki bitiş tarihi geçti | | `contract.advanced` | Otomatik yenilenen sözleşmenin takip edilen bitişi ileri alındı |  > **Not (contract.\\* olayları):** Bunlar platformun **takip amaçlı** > kayıtlarıdır; sözleşmenin hukuken yenilendiğini veya sona erdiğini > BELİRTMEZ. Detay için `ContractTerm` şemasına ve `state` alanına bakın.  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type`: yukarıdaki 11 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.10.2
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.imzala.client.generated.model.ApiV1TemplatesIdPatch200ResponseData;
import org.imzala.client.generated.model.TemplateUsageDocumentsInner;
import org.imzala.client.generated.model.TemplateUsageEndpoint;
import org.imzala.client.generated.model.TemplateUsageExampleRequest;
import org.imzala.client.generated.model.TemplateUsageFormFieldsInner;
import org.imzala.client.generated.model.TemplateUsagePartiesInner;
import org.imzala.client.generated.model.TemplateUsageVariablesInner;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * TemplateUsage
 */
@JsonPropertyOrder({
  TemplateUsage.JSON_PROPERTY_KIND,
  TemplateUsage.JSON_PROPERTY_FORM_FIELDS,
  TemplateUsage.JSON_PROPERTY_TEMPLATE,
  TemplateUsage.JSON_PROPERTY_ENDPOINT,
  TemplateUsage.JSON_PROPERTY_REQUIRED_HEADERS,
  TemplateUsage.JSON_PROPERTY_PARTIES,
  TemplateUsage.JSON_PROPERTY_VARIABLES,
  TemplateUsage.JSON_PROPERTY_DOCUMENTS,
  TemplateUsage.JSON_PROPERTY_EXAMPLE_REQUEST
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-10-06T23:27:17.689888+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class TemplateUsage {
  /**
   * Rehberin anlattığı akış. Bu uç yalnız belgeli şablonu çözer, dolayısıyla burada daima &#x60;DOCUMENT&#x60; döner (Alan Şablonu bu uçta 404&#39;tür; kimliği &#x60;GET /api/v1/field-templates&#x60; ile öğrenilir ve &#x60;POST /api/v1/demands/upload&#x60; ile kullanılır). 
   */
  public enum KindEnum {
    DOCUMENT(String.valueOf("DOCUMENT")),
    
    FIELD_LAYOUT(String.valueOf("FIELD_LAYOUT"));

    private String value;

    KindEnum(String value) {
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
    public static KindEnum fromValue(String value) {
      for (KindEnum b : KindEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_KIND = "kind";
  @javax.annotation.Nullable
  private KindEnum kind;

  public static final String JSON_PROPERTY_FORM_FIELDS = "form_fields";
  @javax.annotation.Nullable
  private List<TemplateUsageFormFieldsInner> formFields = new ArrayList<>();

  public static final String JSON_PROPERTY_TEMPLATE = "template";
  @javax.annotation.Nullable
  private ApiV1TemplatesIdPatch200ResponseData template;

  public static final String JSON_PROPERTY_ENDPOINT = "endpoint";
  @javax.annotation.Nullable
  private TemplateUsageEndpoint endpoint;

  public static final String JSON_PROPERTY_REQUIRED_HEADERS = "required_headers";
  @javax.annotation.Nullable
  private Map<String, String> requiredHeaders = new HashMap<>();

  public static final String JSON_PROPERTY_PARTIES = "parties";
  @javax.annotation.Nullable
  private List<TemplateUsagePartiesInner> parties = new ArrayList<>();

  public static final String JSON_PROPERTY_VARIABLES = "variables";
  @javax.annotation.Nullable
  private List<TemplateUsageVariablesInner> variables = new ArrayList<>();

  public static final String JSON_PROPERTY_DOCUMENTS = "documents";
  @javax.annotation.Nullable
  private List<TemplateUsageDocumentsInner> documents = new ArrayList<>();

  public static final String JSON_PROPERTY_EXAMPLE_REQUEST = "example_request";
  @javax.annotation.Nullable
  private TemplateUsageExampleRequest exampleRequest;

  public TemplateUsage() { 
  }

  public TemplateUsage kind(@javax.annotation.Nullable KindEnum kind) {
    this.kind = kind;
    return this;
  }

  /**
   * Rehberin anlattığı akış. Bu uç yalnız belgeli şablonu çözer, dolayısıyla burada daima &#x60;DOCUMENT&#x60; döner (Alan Şablonu bu uçta 404&#39;tür; kimliği &#x60;GET /api/v1/field-templates&#x60; ile öğrenilir ve &#x60;POST /api/v1/demands/upload&#x60; ile kullanılır). 
   * @return kind
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_KIND, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public KindEnum getKind() {
    return kind;
  }


  @JsonProperty(value = JSON_PROPERTY_KIND, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setKind(@javax.annotation.Nullable KindEnum kind) {
    this.kind = kind;
  }


  public TemplateUsage formFields(@javax.annotation.Nullable List<TemplateUsageFormFieldsInner> formFields) {
    this.formFields = formFields;
    return this;
  }

  public TemplateUsage addFormFieldsItem(TemplateUsageFormFieldsInner formFieldsItem) {
    if (this.formFields == null) {
      this.formFields = new ArrayList<>();
    }
    this.formFields.add(formFieldsItem);
    return this;
  }

  /**
   * &#x60;multipart/form-data&#x60; alanları. Belgeli şablon JSON gövde kullandığı için bu uçta boş dizidir. 
   * @return formFields
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_FORM_FIELDS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<TemplateUsageFormFieldsInner> getFormFields() {
    return formFields;
  }


  @JsonProperty(value = JSON_PROPERTY_FORM_FIELDS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setFormFields(@javax.annotation.Nullable List<TemplateUsageFormFieldsInner> formFields) {
    this.formFields = formFields;
  }


  public TemplateUsage template(@javax.annotation.Nullable ApiV1TemplatesIdPatch200ResponseData template) {
    this.template = template;
    return this;
  }

  /**
   * Get template
   * @return template
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_TEMPLATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public ApiV1TemplatesIdPatch200ResponseData getTemplate() {
    return template;
  }


  @JsonProperty(value = JSON_PROPERTY_TEMPLATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTemplate(@javax.annotation.Nullable ApiV1TemplatesIdPatch200ResponseData template) {
    this.template = template;
  }


  public TemplateUsage endpoint(@javax.annotation.Nullable TemplateUsageEndpoint endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Get endpoint
   * @return endpoint
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_ENDPOINT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public TemplateUsageEndpoint getEndpoint() {
    return endpoint;
  }


  @JsonProperty(value = JSON_PROPERTY_ENDPOINT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEndpoint(@javax.annotation.Nullable TemplateUsageEndpoint endpoint) {
    this.endpoint = endpoint;
  }


  public TemplateUsage requiredHeaders(@javax.annotation.Nullable Map<String, String> requiredHeaders) {
    this.requiredHeaders = requiredHeaders;
    return this;
  }

  public TemplateUsage putRequiredHeadersItem(String key, String requiredHeadersItem) {
    if (this.requiredHeaders == null) {
      this.requiredHeaders = new HashMap<>();
    }
    this.requiredHeaders.put(key, requiredHeadersItem);
    return this;
  }

  /**
   * Get requiredHeaders
   * @return requiredHeaders
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_REQUIRED_HEADERS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Map<String, String> getRequiredHeaders() {
    return requiredHeaders;
  }


  @JsonProperty(value = JSON_PROPERTY_REQUIRED_HEADERS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRequiredHeaders(@javax.annotation.Nullable Map<String, String> requiredHeaders) {
    this.requiredHeaders = requiredHeaders;
  }


  public TemplateUsage parties(@javax.annotation.Nullable List<TemplateUsagePartiesInner> parties) {
    this.parties = parties;
    return this;
  }

  public TemplateUsage addPartiesItem(TemplateUsagePartiesInner partiesItem) {
    if (this.parties == null) {
      this.parties = new ArrayList<>();
    }
    this.parties.add(partiesItem);
    return this;
  }

  /**
   * Get parties
   * @return parties
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_PARTIES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<TemplateUsagePartiesInner> getParties() {
    return parties;
  }


  @JsonProperty(value = JSON_PROPERTY_PARTIES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setParties(@javax.annotation.Nullable List<TemplateUsagePartiesInner> parties) {
    this.parties = parties;
  }


  public TemplateUsage variables(@javax.annotation.Nullable List<TemplateUsageVariablesInner> variables) {
    this.variables = variables;
    return this;
  }

  public TemplateUsage addVariablesItem(TemplateUsageVariablesInner variablesItem) {
    if (this.variables == null) {
      this.variables = new ArrayList<>();
    }
    this.variables.add(variablesItem);
    return this;
  }

  /**
   * Get variables
   * @return variables
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_VARIABLES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<TemplateUsageVariablesInner> getVariables() {
    return variables;
  }


  @JsonProperty(value = JSON_PROPERTY_VARIABLES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setVariables(@javax.annotation.Nullable List<TemplateUsageVariablesInner> variables) {
    this.variables = variables;
  }


  public TemplateUsage documents(@javax.annotation.Nullable List<TemplateUsageDocumentsInner> documents) {
    this.documents = documents;
    return this;
  }

  public TemplateUsage addDocumentsItem(TemplateUsageDocumentsInner documentsItem) {
    if (this.documents == null) {
      this.documents = new ArrayList<>();
    }
    this.documents.add(documentsItem);
    return this;
  }

  /**
   * Şablonun belgeleri (sıra artan). &#x60;document_variables&#x60; anahtarları buradaki &#x60;template_document_id&#x60; değerleridir. 
   * @return documents
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DOCUMENTS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<TemplateUsageDocumentsInner> getDocuments() {
    return documents;
  }


  @JsonProperty(value = JSON_PROPERTY_DOCUMENTS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDocuments(@javax.annotation.Nullable List<TemplateUsageDocumentsInner> documents) {
    this.documents = documents;
  }


  public TemplateUsage exampleRequest(@javax.annotation.Nullable TemplateUsageExampleRequest exampleRequest) {
    this.exampleRequest = exampleRequest;
    return this;
  }

  /**
   * Get exampleRequest
   * @return exampleRequest
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_EXAMPLE_REQUEST, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public TemplateUsageExampleRequest getExampleRequest() {
    return exampleRequest;
  }


  @JsonProperty(value = JSON_PROPERTY_EXAMPLE_REQUEST, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setExampleRequest(@javax.annotation.Nullable TemplateUsageExampleRequest exampleRequest) {
    this.exampleRequest = exampleRequest;
  }


  /**
   * Return true if this TemplateUsage object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TemplateUsage templateUsage = (TemplateUsage) o;
    return Objects.equals(this.kind, templateUsage.kind) &&
        Objects.equals(this.formFields, templateUsage.formFields) &&
        Objects.equals(this.template, templateUsage.template) &&
        Objects.equals(this.endpoint, templateUsage.endpoint) &&
        Objects.equals(this.requiredHeaders, templateUsage.requiredHeaders) &&
        Objects.equals(this.parties, templateUsage.parties) &&
        Objects.equals(this.variables, templateUsage.variables) &&
        Objects.equals(this.documents, templateUsage.documents) &&
        Objects.equals(this.exampleRequest, templateUsage.exampleRequest);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kind, formFields, template, endpoint, requiredHeaders, parties, variables, documents, exampleRequest);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TemplateUsage {\n");
    sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
    sb.append("    formFields: ").append(toIndentedString(formFields)).append("\n");
    sb.append("    template: ").append(toIndentedString(template)).append("\n");
    sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
    sb.append("    requiredHeaders: ").append(toIndentedString(requiredHeaders)).append("\n");
    sb.append("    parties: ").append(toIndentedString(parties)).append("\n");
    sb.append("    variables: ").append(toIndentedString(variables)).append("\n");
    sb.append("    documents: ").append(toIndentedString(documents)).append("\n");
    sb.append("    exampleRequest: ").append(toIndentedString(exampleRequest)).append("\n");
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

    // add `kind` to the URL query string
    if (getKind() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%skind%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getKind()))));
    }

    // add `form_fields` to the URL query string
    if (getFormFields() != null) {
      for (int i = 0; i < getFormFields().size(); i++) {
        if (getFormFields().get(i) != null) {
          joiner.add(getFormFields().get(i).toUrlQueryString(String.format(java.util.Locale.ROOT, "%sform_fields%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `template` to the URL query string
    if (getTemplate() != null) {
      joiner.add(getTemplate().toUrlQueryString(prefix + "template" + suffix));
    }

    // add `endpoint` to the URL query string
    if (getEndpoint() != null) {
      joiner.add(getEndpoint().toUrlQueryString(prefix + "endpoint" + suffix));
    }

    // add `required_headers` to the URL query string
    if (getRequiredHeaders() != null) {
      for (String _key : getRequiredHeaders().keySet()) {
        joiner.add(String.format(java.util.Locale.ROOT, "%srequired_headers%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
            getRequiredHeaders().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getRequiredHeaders().get(_key)))));
      }
    }

    // add `parties` to the URL query string
    if (getParties() != null) {
      for (int i = 0; i < getParties().size(); i++) {
        if (getParties().get(i) != null) {
          joiner.add(getParties().get(i).toUrlQueryString(String.format(java.util.Locale.ROOT, "%sparties%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `variables` to the URL query string
    if (getVariables() != null) {
      for (int i = 0; i < getVariables().size(); i++) {
        if (getVariables().get(i) != null) {
          joiner.add(getVariables().get(i).toUrlQueryString(String.format(java.util.Locale.ROOT, "%svariables%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `documents` to the URL query string
    if (getDocuments() != null) {
      for (int i = 0; i < getDocuments().size(); i++) {
        if (getDocuments().get(i) != null) {
          joiner.add(getDocuments().get(i).toUrlQueryString(String.format(java.util.Locale.ROOT, "%sdocuments%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `example_request` to the URL query string
    if (getExampleRequest() != null) {
      joiner.add(getExampleRequest().toUrlQueryString(prefix + "example_request" + suffix));
    }

    return joiner.toString();
  }
}

