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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.imzala.client.generated.model.DocumentPartyDecision;
import org.imzala.client.generated.model.DocumentProgress;
import org.imzala.client.generated.model.DocumentSealing;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * Zarftaki tek bir belgenin durumu.
 */
@JsonPropertyOrder({
  DemandDocumentStatus.JSON_PROPERTY_ID,
  DemandDocumentStatus.JSON_PROPERTY_ORDER,
  DemandDocumentStatus.JSON_PROPERTY_TITLE,
  DemandDocumentStatus.JSON_PROPERTY_DOC_KIND,
  DemandDocumentStatus.JSON_PROPERTY_IS_REQUIRED,
  DemandDocumentStatus.JSON_PROPERTY_SIGNATURE_REQUIRED,
  DemandDocumentStatus.JSON_PROPERTY_ASSIGNED_PARTY_IDS,
  DemandDocumentStatus.JSON_PROPERTY_PROGRESS,
  DemandDocumentStatus.JSON_PROPERTY_SEALING,
  DemandDocumentStatus.JSON_PROPERTY_DECISIONS
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-09-28T01:16:51.997481+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class DemandDocumentStatus {
  public static final String JSON_PROPERTY_ID = "id";
  @javax.annotation.Nullable
  private UUID id;

  public static final String JSON_PROPERTY_ORDER = "order";
  @javax.annotation.Nullable
  private Integer order;

  public static final String JSON_PROPERTY_TITLE = "title";
  @javax.annotation.Nullable
  private String title;

  /**
   * Gets or Sets docKind
   */
  public enum DocKindEnum {
    CONTRACT(String.valueOf("CONTRACT")),
    
    KVKK_NOTICE(String.valueOf("KVKK_NOTICE")),
    
    KVKK_CONSENT(String.valueOf("KVKK_CONSENT")),
    
    PREINFO(String.valueOf("PREINFO")),
    
    PRICE_LIST(String.valueOf("PRICE_LIST")),
    
    OTHER(String.valueOf("OTHER"));

    private String value;

    DocKindEnum(String value) {
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
    public static DocKindEnum fromValue(String value) {
      for (DocKindEnum b : DocKindEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_DOC_KIND = "doc_kind";
  @javax.annotation.Nullable
  private DocKindEnum docKind;

  public static final String JSON_PROPERTY_IS_REQUIRED = "is_required";
  @javax.annotation.Nullable
  private Boolean isRequired;

  public static final String JSON_PROPERTY_SIGNATURE_REQUIRED = "signature_required";
  @javax.annotation.Nullable
  private Boolean signatureRequired;

  public static final String JSON_PROPERTY_ASSIGNED_PARTY_IDS = "assigned_party_ids";
  @javax.annotation.Nullable
  private List<UUID> assignedPartyIds = new ArrayList<>();

  public static final String JSON_PROPERTY_PROGRESS = "progress";
  @javax.annotation.Nullable
  private DocumentProgress progress;

  public static final String JSON_PROPERTY_SEALING = "sealing";
  @javax.annotation.Nullable
  private DocumentSealing sealing;

  public static final String JSON_PROPERTY_DECISIONS = "decisions";
  @javax.annotation.Nullable
  private List<DocumentPartyDecision> decisions = new ArrayList<>();

  public DemandDocumentStatus() { 
  }

  public DemandDocumentStatus id(@javax.annotation.Nullable UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public UUID getId() {
    return id;
  }


  @JsonProperty(value = JSON_PROPERTY_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setId(@javax.annotation.Nullable UUID id) {
    this.id = id;
  }


  public DemandDocumentStatus order(@javax.annotation.Nullable Integer order) {
    this.order = order;
    return this;
  }

  /**
   * 1&#39;den başlar.
   * @return order
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_ORDER, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Integer getOrder() {
    return order;
  }


  @JsonProperty(value = JSON_PROPERTY_ORDER, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setOrder(@javax.annotation.Nullable Integer order) {
    this.order = order;
  }


  public DemandDocumentStatus title(@javax.annotation.Nullable String title) {
    this.title = title;
    return this;
  }

  /**
   * Get title
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


  public DemandDocumentStatus docKind(@javax.annotation.Nullable DocKindEnum docKind) {
    this.docKind = docKind;
    return this;
  }

  /**
   * Get docKind
   * @return docKind
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DOC_KIND, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public DocKindEnum getDocKind() {
    return docKind;
  }


  @JsonProperty(value = JSON_PROPERTY_DOC_KIND, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDocKind(@javax.annotation.Nullable DocKindEnum docKind) {
    this.docKind = docKind;
  }


  public DemandDocumentStatus isRequired(@javax.annotation.Nullable Boolean isRequired) {
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


  public DemandDocumentStatus signatureRequired(@javax.annotation.Nullable Boolean signatureRequired) {
    this.signatureRequired = signatureRequired;
    return this;
  }

  /**
   * Get signatureRequired
   * @return signatureRequired
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_SIGNATURE_REQUIRED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getSignatureRequired() {
    return signatureRequired;
  }


  @JsonProperty(value = JSON_PROPERTY_SIGNATURE_REQUIRED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSignatureRequired(@javax.annotation.Nullable Boolean signatureRequired) {
    this.signatureRequired = signatureRequired;
  }


  public DemandDocumentStatus assignedPartyIds(@javax.annotation.Nullable List<UUID> assignedPartyIds) {
    this.assignedPartyIds = assignedPartyIds;
    return this;
  }

  public DemandDocumentStatus addAssignedPartyIdsItem(UUID assignedPartyIdsItem) {
    if (this.assignedPartyIds == null) {
      this.assignedPartyIds = new ArrayList<>();
    }
    this.assignedPartyIds.add(assignedPartyIdsItem);
    return this;
  }

  /**
   * Get assignedPartyIds
   * @return assignedPartyIds
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_ASSIGNED_PARTY_IDS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<UUID> getAssignedPartyIds() {
    return assignedPartyIds;
  }


  @JsonProperty(value = JSON_PROPERTY_ASSIGNED_PARTY_IDS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAssignedPartyIds(@javax.annotation.Nullable List<UUID> assignedPartyIds) {
    this.assignedPartyIds = assignedPartyIds;
  }


  public DemandDocumentStatus progress(@javax.annotation.Nullable DocumentProgress progress) {
    this.progress = progress;
    return this;
  }

  /**
   * Get progress
   * @return progress
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_PROGRESS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public DocumentProgress getProgress() {
    return progress;
  }


  @JsonProperty(value = JSON_PROPERTY_PROGRESS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setProgress(@javax.annotation.Nullable DocumentProgress progress) {
    this.progress = progress;
  }


  public DemandDocumentStatus sealing(@javax.annotation.Nullable DocumentSealing sealing) {
    this.sealing = sealing;
    return this;
  }

  /**
   * Get sealing
   * @return sealing
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_SEALING, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public DocumentSealing getSealing() {
    return sealing;
  }


  @JsonProperty(value = JSON_PROPERTY_SEALING, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSealing(@javax.annotation.Nullable DocumentSealing sealing) {
    this.sealing = sealing;
  }


  public DemandDocumentStatus decisions(@javax.annotation.Nullable List<DocumentPartyDecision> decisions) {
    this.decisions = decisions;
    return this;
  }

  public DemandDocumentStatus addDecisionsItem(DocumentPartyDecision decisionsItem) {
    if (this.decisions == null) {
      this.decisions = new ArrayList<>();
    }
    this.decisions.add(decisionsItem);
    return this;
  }

  /**
   * Get decisions
   * @return decisions
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DECISIONS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<DocumentPartyDecision> getDecisions() {
    return decisions;
  }


  @JsonProperty(value = JSON_PROPERTY_DECISIONS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDecisions(@javax.annotation.Nullable List<DocumentPartyDecision> decisions) {
    this.decisions = decisions;
  }


  /**
   * Return true if this DemandDocumentStatus object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DemandDocumentStatus demandDocumentStatus = (DemandDocumentStatus) o;
    return Objects.equals(this.id, demandDocumentStatus.id) &&
        Objects.equals(this.order, demandDocumentStatus.order) &&
        Objects.equals(this.title, demandDocumentStatus.title) &&
        Objects.equals(this.docKind, demandDocumentStatus.docKind) &&
        Objects.equals(this.isRequired, demandDocumentStatus.isRequired) &&
        Objects.equals(this.signatureRequired, demandDocumentStatus.signatureRequired) &&
        Objects.equals(this.assignedPartyIds, demandDocumentStatus.assignedPartyIds) &&
        Objects.equals(this.progress, demandDocumentStatus.progress) &&
        Objects.equals(this.sealing, demandDocumentStatus.sealing) &&
        Objects.equals(this.decisions, demandDocumentStatus.decisions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, order, title, docKind, isRequired, signatureRequired, assignedPartyIds, progress, sealing, decisions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DemandDocumentStatus {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    order: ").append(toIndentedString(order)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    docKind: ").append(toIndentedString(docKind)).append("\n");
    sb.append("    isRequired: ").append(toIndentedString(isRequired)).append("\n");
    sb.append("    signatureRequired: ").append(toIndentedString(signatureRequired)).append("\n");
    sb.append("    assignedPartyIds: ").append(toIndentedString(assignedPartyIds)).append("\n");
    sb.append("    progress: ").append(toIndentedString(progress)).append("\n");
    sb.append("    sealing: ").append(toIndentedString(sealing)).append("\n");
    sb.append("    decisions: ").append(toIndentedString(decisions)).append("\n");
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

    // add `id` to the URL query string
    if (getId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sid%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
    }

    // add `order` to the URL query string
    if (getOrder() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sorder%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getOrder()))));
    }

    // add `title` to the URL query string
    if (getTitle() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%stitle%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTitle()))));
    }

    // add `doc_kind` to the URL query string
    if (getDocKind() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sdoc_kind%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDocKind()))));
    }

    // add `is_required` to the URL query string
    if (getIsRequired() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sis_required%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIsRequired()))));
    }

    // add `signature_required` to the URL query string
    if (getSignatureRequired() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%ssignature_required%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSignatureRequired()))));
    }

    // add `assigned_party_ids` to the URL query string
    if (getAssignedPartyIds() != null) {
      for (int i = 0; i < getAssignedPartyIds().size(); i++) {
        if (getAssignedPartyIds().get(i) != null) {
          joiner.add(String.format(java.util.Locale.ROOT, "%sassigned_party_ids%s%s=%s", prefix, suffix,
              "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
              ApiClient.urlEncode(ApiClient.valueToString(getAssignedPartyIds().get(i)))));
        }
      }
    }

    // add `progress` to the URL query string
    if (getProgress() != null) {
      joiner.add(getProgress().toUrlQueryString(prefix + "progress" + suffix));
    }

    // add `sealing` to the URL query string
    if (getSealing() != null) {
      joiner.add(getSealing().toUrlQueryString(prefix + "sealing" + suffix));
    }

    // add `decisions` to the URL query string
    if (getDecisions() != null) {
      for (int i = 0; i < getDecisions().size(); i++) {
        if (getDecisions().get(i) != null) {
          joiner.add(getDecisions().get(i).toUrlQueryString(String.format(java.util.Locale.ROOT, "%sdecisions%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    return joiner.toString();
  }
}

