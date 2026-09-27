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
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * Bir tarafın bu belge için kararı. Kişisel veri (ad, e-posta, telefon) içermez; tarafı &#x60;party_id&#x60; ile &#x60;parties&#x60; listesiyle eşleyin. 
 */
@JsonPropertyOrder({
  DocumentPartyDecision.JSON_PROPERTY_PARTY_ID,
  DocumentPartyDecision.JSON_PROPERTY_DECISION,
  DocumentPartyDecision.JSON_PROPERTY_DECIDED_AT,
  DocumentPartyDecision.JSON_PROPERTY_WITHDRAWN_AT,
  DocumentPartyDecision.JSON_PROPERTY_SIGNATURE_COLLECTED,
  DocumentPartyDecision.JSON_PROPERTY_RECORDED,
  DocumentPartyDecision.JSON_PROPERTY_CONSENT_GRANTED
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-09-28T01:16:51.997481+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class DocumentPartyDecision {
  public static final String JSON_PROPERTY_PARTY_ID = "party_id";
  @javax.annotation.Nullable
  private UUID partyId;

  /**
   * Yürürlükteki karar. &#x60;PENDING&#x60; karar verilmediğini gösterir. &#x60;WITHDRAWN&#x60;, önce verilmiş bir onayın sonradan geri çekildiğini gösterir; geri çekme tamamlanmış belgeyi yeniden açmaz. 
   */
  public enum DecisionEnum {
    PENDING(String.valueOf("PENDING")),
    
    ACCEPTED(String.valueOf("ACCEPTED")),
    
    DECLINED(String.valueOf("DECLINED")),
    
    WITHDRAWN(String.valueOf("WITHDRAWN"));

    private String value;

    DecisionEnum(String value) {
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
    public static DecisionEnum fromValue(String value) {
      for (DecisionEnum b : DecisionEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_DECISION = "decision";
  @javax.annotation.Nullable
  private DecisionEnum decision;

  public static final String JSON_PROPERTY_DECIDED_AT = "decided_at";
  private JsonNullable<OffsetDateTime> decidedAt = JsonNullable.<OffsetDateTime>undefined();

  public static final String JSON_PROPERTY_WITHDRAWN_AT = "withdrawn_at";
  private JsonNullable<OffsetDateTime> withdrawnAt = JsonNullable.<OffsetDateTime>undefined();

  public static final String JSON_PROPERTY_SIGNATURE_COLLECTED = "signature_collected";
  @javax.annotation.Nullable
  private Boolean signatureCollected;

  public static final String JSON_PROPERTY_RECORDED = "recorded";
  @javax.annotation.Nullable
  private Boolean recorded;

  public static final String JSON_PROPERTY_CONSENT_GRANTED = "consent_granted";
  private JsonNullable<Boolean> consentGranted = JsonNullable.<Boolean>undefined();

  public DocumentPartyDecision() { 
  }

  public DocumentPartyDecision partyId(@javax.annotation.Nullable UUID partyId) {
    this.partyId = partyId;
    return this;
  }

  /**
   * Get partyId
   * @return partyId
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_PARTY_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public UUID getPartyId() {
    return partyId;
  }


  @JsonProperty(value = JSON_PROPERTY_PARTY_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPartyId(@javax.annotation.Nullable UUID partyId) {
    this.partyId = partyId;
  }


  public DocumentPartyDecision decision(@javax.annotation.Nullable DecisionEnum decision) {
    this.decision = decision;
    return this;
  }

  /**
   * Yürürlükteki karar. &#x60;PENDING&#x60; karar verilmediğini gösterir. &#x60;WITHDRAWN&#x60;, önce verilmiş bir onayın sonradan geri çekildiğini gösterir; geri çekme tamamlanmış belgeyi yeniden açmaz. 
   * @return decision
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DECISION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public DecisionEnum getDecision() {
    return decision;
  }


  @JsonProperty(value = JSON_PROPERTY_DECISION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDecision(@javax.annotation.Nullable DecisionEnum decision) {
    this.decision = decision;
  }


  public DocumentPartyDecision decidedAt(@javax.annotation.Nullable OffsetDateTime decidedAt) {
    this.decidedAt = JsonNullable.<OffsetDateTime>of(decidedAt);
    return this;
  }

  /**
   * Kararın verildiği an. &#x60;WITHDRAWN&#x60; için geri çekilen onayın anıdır. &#x60;recorded&#x60; &#x60;false&#x60; iken &#x60;null&#x60; döner. 
   * @return decidedAt
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public OffsetDateTime getDecidedAt() {
        return decidedAt.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_DECIDED_AT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<OffsetDateTime> getDecidedAt_JsonNullable() {
    return decidedAt;
  }
  
  @JsonProperty(JSON_PROPERTY_DECIDED_AT)
  public void setDecidedAt_JsonNullable(JsonNullable<OffsetDateTime> decidedAt) {
    this.decidedAt = decidedAt;
  }

  public void setDecidedAt(@javax.annotation.Nullable OffsetDateTime decidedAt) {
    this.decidedAt = JsonNullable.<OffsetDateTime>of(decidedAt);
  }


  public DocumentPartyDecision withdrawnAt(@javax.annotation.Nullable OffsetDateTime withdrawnAt) {
    this.withdrawnAt = JsonNullable.<OffsetDateTime>of(withdrawnAt);
    return this;
  }

  /**
   * Yalnız &#x60;WITHDRAWN&#x60; için dolu; geri çekme anı.
   * @return withdrawnAt
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public OffsetDateTime getWithdrawnAt() {
        return withdrawnAt.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_WITHDRAWN_AT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<OffsetDateTime> getWithdrawnAt_JsonNullable() {
    return withdrawnAt;
  }
  
  @JsonProperty(JSON_PROPERTY_WITHDRAWN_AT)
  public void setWithdrawnAt_JsonNullable(JsonNullable<OffsetDateTime> withdrawnAt) {
    this.withdrawnAt = withdrawnAt;
  }

  public void setWithdrawnAt(@javax.annotation.Nullable OffsetDateTime withdrawnAt) {
    this.withdrawnAt = JsonNullable.<OffsetDateTime>of(withdrawnAt);
  }


  public DocumentPartyDecision signatureCollected(@javax.annotation.Nullable Boolean signatureCollected) {
    this.signatureCollected = signatureCollected;
    return this;
  }

  /**
   * Bu kararla birlikte bu belgede dijital imza toplandı mı.
   * @return signatureCollected
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_SIGNATURE_COLLECTED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getSignatureCollected() {
    return signatureCollected;
  }


  @JsonProperty(value = JSON_PROPERTY_SIGNATURE_COLLECTED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSignatureCollected(@javax.annotation.Nullable Boolean signatureCollected) {
    this.signatureCollected = signatureCollected;
  }


  public DocumentPartyDecision recorded(@javax.annotation.Nullable Boolean recorded) {
    this.recorded = recorded;
    return this;
  }

  /**
   * &#x60;false&#x60; ise karar belge bazında kayıtlı değildir; tek belgeli eski sözleşmelerde tarafın imzasından türetilmiştir ve karar delili olarak kullanılamaz. 
   * @return recorded
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_RECORDED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getRecorded() {
    return recorded;
  }


  @JsonProperty(value = JSON_PROPERTY_RECORDED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRecorded(@javax.annotation.Nullable Boolean recorded) {
    this.recorded = recorded;
  }


  public DocumentPartyDecision consentGranted(@javax.annotation.Nullable Boolean consentGranted) {
    this.consentGranted = JsonNullable.<Boolean>of(consentGranted);
    return this;
  }

  /**
   * Yalnız &#x60;KVKK_CONSENT&#x60; belgesinde dolu. Taraf bu belgeyi onaylamış ve onayını geri çekmemişse &#x60;true&#x60;; reddetmiş, geri çekmiş veya karar vermemişse &#x60;false&#x60;. Bu alan yalnızca platformdaki onay kaydını yansıtır; açık rızanın KVKK anlamında hukuken geçerli olup olmadığını (belirli konuya ilişkin olması, bilgilendirmeye dayanması, özgür iradeyle verilmesi) imzala.org değerlendirmez. Reddedilen isteğe bağlı bir rıza hizmetin koşulu yapılmamalıdır. Diğer belge türlerinde &#x60;null&#x60;. 
   * @return consentGranted
   */
  @javax.annotation.Nullable
  @JsonIgnore
  public Boolean getConsentGranted() {
        return consentGranted.orElse(null);
  }

  @JsonProperty(value = JSON_PROPERTY_CONSENT_GRANTED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<Boolean> getConsentGranted_JsonNullable() {
    return consentGranted;
  }
  
  @JsonProperty(JSON_PROPERTY_CONSENT_GRANTED)
  public void setConsentGranted_JsonNullable(JsonNullable<Boolean> consentGranted) {
    this.consentGranted = consentGranted;
  }

  public void setConsentGranted(@javax.annotation.Nullable Boolean consentGranted) {
    this.consentGranted = JsonNullable.<Boolean>of(consentGranted);
  }


  /**
   * Return true if this DocumentPartyDecision object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DocumentPartyDecision documentPartyDecision = (DocumentPartyDecision) o;
    return Objects.equals(this.partyId, documentPartyDecision.partyId) &&
        Objects.equals(this.decision, documentPartyDecision.decision) &&
        equalsNullable(this.decidedAt, documentPartyDecision.decidedAt) &&
        equalsNullable(this.withdrawnAt, documentPartyDecision.withdrawnAt) &&
        Objects.equals(this.signatureCollected, documentPartyDecision.signatureCollected) &&
        Objects.equals(this.recorded, documentPartyDecision.recorded) &&
        equalsNullable(this.consentGranted, documentPartyDecision.consentGranted);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(partyId, decision, hashCodeNullable(decidedAt), hashCodeNullable(withdrawnAt), signatureCollected, recorded, hashCodeNullable(consentGranted));
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
    sb.append("class DocumentPartyDecision {\n");
    sb.append("    partyId: ").append(toIndentedString(partyId)).append("\n");
    sb.append("    decision: ").append(toIndentedString(decision)).append("\n");
    sb.append("    decidedAt: ").append(toIndentedString(decidedAt)).append("\n");
    sb.append("    withdrawnAt: ").append(toIndentedString(withdrawnAt)).append("\n");
    sb.append("    signatureCollected: ").append(toIndentedString(signatureCollected)).append("\n");
    sb.append("    recorded: ").append(toIndentedString(recorded)).append("\n");
    sb.append("    consentGranted: ").append(toIndentedString(consentGranted)).append("\n");
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

    // add `party_id` to the URL query string
    if (getPartyId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sparty_id%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPartyId()))));
    }

    // add `decision` to the URL query string
    if (getDecision() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sdecision%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDecision()))));
    }

    // add `decided_at` to the URL query string
    if (getDecidedAt() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sdecided_at%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDecidedAt()))));
    }

    // add `withdrawn_at` to the URL query string
    if (getWithdrawnAt() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%swithdrawn_at%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getWithdrawnAt()))));
    }

    // add `signature_collected` to the URL query string
    if (getSignatureCollected() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%ssignature_collected%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSignatureCollected()))));
    }

    // add `recorded` to the URL query string
    if (getRecorded() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%srecorded%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRecorded()))));
    }

    // add `consent_granted` to the URL query string
    if (getConsentGranted() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sconsent_granted%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getConsentGranted()))));
    }

    return joiner.toString();
  }
}

