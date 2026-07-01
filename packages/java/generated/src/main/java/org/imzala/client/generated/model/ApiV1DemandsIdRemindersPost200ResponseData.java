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
import java.util.List;
import java.util.UUID;
import org.imzala.client.generated.model.ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner;
import org.imzala.client.generated.model.ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.imzala.client.generated.ApiClient;
/**
 * ApiV1DemandsIdRemindersPost200ResponseData
 */
@JsonPropertyOrder({
  ApiV1DemandsIdRemindersPost200ResponseData.JSON_PROPERTY_DEMAND_ID,
  ApiV1DemandsIdRemindersPost200ResponseData.JSON_PROPERTY_DISPATCHED,
  ApiV1DemandsIdRemindersPost200ResponseData.JSON_PROPERTY_SKIPPED,
  ApiV1DemandsIdRemindersPost200ResponseData.JSON_PROPERTY_LAST_REMINDER_SENT_AT
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-07-01T14:50:26.407096+03:00[Europe/Istanbul]", comments = "Generator version: 7.23.0")
public class ApiV1DemandsIdRemindersPost200ResponseData {
  public static final String JSON_PROPERTY_DEMAND_ID = "demand_id";
  @javax.annotation.Nullable
  private UUID demandId;

  public static final String JSON_PROPERTY_DISPATCHED = "dispatched";
  @javax.annotation.Nullable
  private List<ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner> dispatched = new ArrayList<>();

  public static final String JSON_PROPERTY_SKIPPED = "skipped";
  @javax.annotation.Nullable
  private List<ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner> skipped = new ArrayList<>();

  public static final String JSON_PROPERTY_LAST_REMINDER_SENT_AT = "last_reminder_sent_at";
  @javax.annotation.Nullable
  private OffsetDateTime lastReminderSentAt;

  public ApiV1DemandsIdRemindersPost200ResponseData() { 
  }

  public ApiV1DemandsIdRemindersPost200ResponseData demandId(@javax.annotation.Nullable UUID demandId) {
    this.demandId = demandId;
    return this;
  }

  /**
   * Get demandId
   * @return demandId
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DEMAND_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public UUID getDemandId() {
    return demandId;
  }


  @JsonProperty(value = JSON_PROPERTY_DEMAND_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDemandId(@javax.annotation.Nullable UUID demandId) {
    this.demandId = demandId;
  }


  public ApiV1DemandsIdRemindersPost200ResponseData dispatched(@javax.annotation.Nullable List<ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner> dispatched) {
    this.dispatched = dispatched;
    return this;
  }

  public ApiV1DemandsIdRemindersPost200ResponseData addDispatchedItem(ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner dispatchedItem) {
    if (this.dispatched == null) {
      this.dispatched = new ArrayList<>();
    }
    this.dispatched.add(dispatchedItem);
    return this;
  }

  /**
   * Get dispatched
   * @return dispatched
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_DISPATCHED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner> getDispatched() {
    return dispatched;
  }


  @JsonProperty(value = JSON_PROPERTY_DISPATCHED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDispatched(@javax.annotation.Nullable List<ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner> dispatched) {
    this.dispatched = dispatched;
  }


  public ApiV1DemandsIdRemindersPost200ResponseData skipped(@javax.annotation.Nullable List<ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner> skipped) {
    this.skipped = skipped;
    return this;
  }

  public ApiV1DemandsIdRemindersPost200ResponseData addSkippedItem(ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner skippedItem) {
    if (this.skipped == null) {
      this.skipped = new ArrayList<>();
    }
    this.skipped.add(skippedItem);
    return this;
  }

  /**
   * Hatırlatma gönderilmeyen partilerin nedenleriyle birlikte (telefon/email yok, opt-out vs.)
   * @return skipped
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_SKIPPED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner> getSkipped() {
    return skipped;
  }


  @JsonProperty(value = JSON_PROPERTY_SKIPPED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSkipped(@javax.annotation.Nullable List<ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner> skipped) {
    this.skipped = skipped;
  }


  public ApiV1DemandsIdRemindersPost200ResponseData lastReminderSentAt(@javax.annotation.Nullable OffsetDateTime lastReminderSentAt) {
    this.lastReminderSentAt = lastReminderSentAt;
    return this;
  }

  /**
   * Get lastReminderSentAt
   * @return lastReminderSentAt
   */
  @javax.annotation.Nullable
  @JsonProperty(value = JSON_PROPERTY_LAST_REMINDER_SENT_AT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public OffsetDateTime getLastReminderSentAt() {
    return lastReminderSentAt;
  }


  @JsonProperty(value = JSON_PROPERTY_LAST_REMINDER_SENT_AT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setLastReminderSentAt(@javax.annotation.Nullable OffsetDateTime lastReminderSentAt) {
    this.lastReminderSentAt = lastReminderSentAt;
  }


  /**
   * Return true if this _api_v1_demands__id__reminders_post_200_response_data object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiV1DemandsIdRemindersPost200ResponseData apiV1DemandsIdRemindersPost200ResponseData = (ApiV1DemandsIdRemindersPost200ResponseData) o;
    return Objects.equals(this.demandId, apiV1DemandsIdRemindersPost200ResponseData.demandId) &&
        Objects.equals(this.dispatched, apiV1DemandsIdRemindersPost200ResponseData.dispatched) &&
        Objects.equals(this.skipped, apiV1DemandsIdRemindersPost200ResponseData.skipped) &&
        Objects.equals(this.lastReminderSentAt, apiV1DemandsIdRemindersPost200ResponseData.lastReminderSentAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(demandId, dispatched, skipped, lastReminderSentAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiV1DemandsIdRemindersPost200ResponseData {\n");
    sb.append("    demandId: ").append(toIndentedString(demandId)).append("\n");
    sb.append("    dispatched: ").append(toIndentedString(dispatched)).append("\n");
    sb.append("    skipped: ").append(toIndentedString(skipped)).append("\n");
    sb.append("    lastReminderSentAt: ").append(toIndentedString(lastReminderSentAt)).append("\n");
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

    // add `demand_id` to the URL query string
    if (getDemandId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sdemand_id%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDemandId()))));
    }

    // add `dispatched` to the URL query string
    if (getDispatched() != null) {
      for (int i = 0; i < getDispatched().size(); i++) {
        if (getDispatched().get(i) != null) {
          joiner.add(getDispatched().get(i).toUrlQueryString(String.format(java.util.Locale.ROOT, "%sdispatched%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `skipped` to the URL query string
    if (getSkipped() != null) {
      for (int i = 0; i < getSkipped().size(); i++) {
        if (getSkipped().get(i) != null) {
          joiner.add(getSkipped().get(i).toUrlQueryString(String.format(java.util.Locale.ROOT, "%sskipped%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `last_reminder_sent_at` to the URL query string
    if (getLastReminderSentAt() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%slast_reminder_sent_at%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getLastReminderSentAt()))));
    }

    return joiner.toString();
  }
}

