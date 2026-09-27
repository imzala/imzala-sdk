/*
 * imzala External API
 *
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.9.1 · **Son güncelleme:** 2026-09-27  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır (aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |- -- -|- -- -- --|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (11) | Olay | Tetikleyici | |- -- -- -|- -- -- -- -- -- --| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı | | `contract.expiring` | Takip edilen sözleşme bitiş tarihine yaklaşıyor | | `contract.ended` | Takip edilen sözleşmenin platformdaki bitiş tarihi geçti | | `contract.advanced` | Otomatik yenilenen sözleşmenin takip edilen bitişi ileri alındı |  > **Not (contract.\\* olayları):** Bunlar platformun **takip amaçlı** > kayıtlarıdır; sözleşmenin hukuken yenilendiğini veya sona erdiğini > BELİRTMEZ. Detay için `ContractTerm` şemasına ve `state` alanına bakın.  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type`: yukarıdaki 11 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); - - INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.9.1
 * Contact: destek@imzala.org
 * Generated by: https://github.com/openapitools/openapi-generator.git
 */


using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.IO;
using System.Runtime.Serialization;
using System.Text;
using System.Text.RegularExpressions;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Linq;
using System.ComponentModel.DataAnnotations;
using FileParameter = ImzalaApiClient.Client.FileParameter;
using OpenAPIDateConverter = ImzalaApiClient.Client.OpenAPIDateConverter;

namespace ImzalaApiClient.Model
{
    /// <summary>
    /// Sözleşme süre/yenileme takibi girdisi. &#x60;POST /demands&#x60; üzerinde oluştururken ve &#x60;PATCH /demands/{id}/term&#x60; üzerinde güncellerken AYNI alan adları kullanılır. &#x60;PATCH&#x60;&#39;te kısmi güncelleme uygulanır: yalnız gövdede GÖNDERİLEN anahtarlar değiştirilir; bir anahtarı &#x60;null&#x60; göndermek o alanı temizler, hiç göndermemek dokunmaz.  Doğrulama kuralları: - &#x60;term_fixed_end_date&#x60; ile &#x60;term_duration_months&#x60; birlikte gönderilemez. - &#x60;term_start_mode: FIXED_DATE&#x60; ise oluştururken &#x60;term_start_date&#x60; ya   da &#x60;term_fixed_end_date&#x60; gerekir; &#x60;ON_FIRST_SIGNATURE&#x60; /   &#x60;ON_COMPLETION&#x60; modlarında &#x60;term_start_date&#x60; gönderilse de yok   sayılır (başlangıç imza anında otomatik yazılır). - &#x60;renewal_type: AUTO_RENEW&#x60; ise &#x60;renewal_period_months&#x60; gerekir;   gönderilmezse &#x60;term_duration_months&#x60;&#39;tan devralınır. - Geçersiz kombinasyon 400 &#x60;TERM_INVALID&#x60; + hangi alanı işaret eden   &#x60;field&#x60; ile döner. 
    /// </summary>
    [DataContract(Name = "ContractTermInput")]
    public partial class ContractTermInput : IValidatableObject
    {
        /// <summary>
        /// Defines TermStartMode
        /// </summary>
        [JsonConverter(typeof(StringEnumConverter))]
        public enum TermStartModeEnum
        {
            /// <summary>
            /// Enum FIXEDDATE for value: FIXED_DATE
            /// </summary>
            [EnumMember(Value = "FIXED_DATE")]
            FIXEDDATE = 1,

            /// <summary>
            /// Enum ONFIRSTSIGNATURE for value: ON_FIRST_SIGNATURE
            /// </summary>
            [EnumMember(Value = "ON_FIRST_SIGNATURE")]
            ONFIRSTSIGNATURE = 2,

            /// <summary>
            /// Enum ONCOMPLETION for value: ON_COMPLETION
            /// </summary>
            [EnumMember(Value = "ON_COMPLETION")]
            ONCOMPLETION = 3
        }


        /// <summary>
        /// Gets or Sets TermStartMode
        /// </summary>
        [DataMember(Name = "term_start_mode", EmitDefaultValue = true)]
        public TermStartModeEnum? TermStartMode { get; set; }
        /// <summary>
        /// Defines RenewalType
        /// </summary>
        [JsonConverter(typeof(StringEnumConverter))]
        public enum RenewalTypeEnum
        {
            /// <summary>
            /// Enum AUTORENEW for value: AUTO_RENEW
            /// </summary>
            [EnumMember(Value = "AUTO_RENEW")]
            AUTORENEW = 1,

            /// <summary>
            /// Enum FIXEDTERM for value: FIXED_TERM
            /// </summary>
            [EnumMember(Value = "FIXED_TERM")]
            FIXEDTERM = 2
        }


        /// <summary>
        /// Gets or Sets RenewalType
        /// </summary>
        [DataMember(Name = "renewal_type", EmitDefaultValue = true)]
        public RenewalTypeEnum? RenewalType { get; set; }
        /// <summary>
        /// Initializes a new instance of the <see cref="ContractTermInput" /> class.
        /// </summary>
        /// <param name="termStartMode">termStartMode.</param>
        /// <param name="termStartDate">Yalnız &#x60;term_start_mode: FIXED_DATE&#x60; iken kullanılır..</param>
        /// <param name="termDurationMonths">termDurationMonths.</param>
        /// <param name="termFixedEndDate">termFixedEndDate.</param>
        /// <param name="renewalType">renewalType.</param>
        /// <param name="renewalPeriodMonths">renewalPeriodMonths.</param>
        /// <param name="noticeDays">noticeDays.</param>
        /// <param name="reminderOffsets">Boş dizi göndermek şablon/önceki değeri SİLMEZ (yok sayılır); temizlemek için &#x60;null&#x60; gönderin. .</param>
        /// <param name="notifyCounterparty">notifyCounterparty.</param>
        public ContractTermInput(TermStartModeEnum? termStartMode = default, DateOnly? termStartDate = default, int? termDurationMonths = default, DateOnly? termFixedEndDate = default, RenewalTypeEnum? renewalType = default, int? renewalPeriodMonths = default, int? noticeDays = default, List<int> reminderOffsets = default, bool notifyCounterparty = default)
        {
            this.TermStartMode = termStartMode;
            this.TermStartDate = termStartDate;
            this.TermDurationMonths = termDurationMonths;
            this.TermFixedEndDate = termFixedEndDate;
            this.RenewalType = renewalType;
            this.RenewalPeriodMonths = renewalPeriodMonths;
            this.NoticeDays = noticeDays;
            this.ReminderOffsets = reminderOffsets;
            this.NotifyCounterparty = notifyCounterparty;
        }

        /// <summary>
        /// Yalnız &#x60;term_start_mode: FIXED_DATE&#x60; iken kullanılır.
        /// </summary>
        /// <value>Yalnız &#x60;term_start_mode: FIXED_DATE&#x60; iken kullanılır.</value>
        [DataMember(Name = "term_start_date", EmitDefaultValue = true)]
        public DateOnly? TermStartDate { get; set; }

        /// <summary>
        /// Gets or Sets TermDurationMonths
        /// </summary>
        [DataMember(Name = "term_duration_months", EmitDefaultValue = true)]
        public int? TermDurationMonths { get; set; }

        /// <summary>
        /// Gets or Sets TermFixedEndDate
        /// </summary>
        [DataMember(Name = "term_fixed_end_date", EmitDefaultValue = true)]
        public DateOnly? TermFixedEndDate { get; set; }

        /// <summary>
        /// Gets or Sets RenewalPeriodMonths
        /// </summary>
        [DataMember(Name = "renewal_period_months", EmitDefaultValue = true)]
        public int? RenewalPeriodMonths { get; set; }

        /// <summary>
        /// Gets or Sets NoticeDays
        /// </summary>
        [DataMember(Name = "notice_days", EmitDefaultValue = true)]
        public int? NoticeDays { get; set; }

        /// <summary>
        /// Boş dizi göndermek şablon/önceki değeri SİLMEZ (yok sayılır); temizlemek için &#x60;null&#x60; gönderin. 
        /// </summary>
        /// <value>Boş dizi göndermek şablon/önceki değeri SİLMEZ (yok sayılır); temizlemek için &#x60;null&#x60; gönderin. </value>
        [DataMember(Name = "reminder_offsets", EmitDefaultValue = true)]
        public List<int> ReminderOffsets { get; set; }

        /// <summary>
        /// Gets or Sets NotifyCounterparty
        /// </summary>
        [DataMember(Name = "notify_counterparty", EmitDefaultValue = true)]
        public bool NotifyCounterparty { get; set; }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class ContractTermInput {\n");
            sb.Append("  TermStartMode: ").Append(TermStartMode).Append("\n");
            sb.Append("  TermStartDate: ").Append(TermStartDate).Append("\n");
            sb.Append("  TermDurationMonths: ").Append(TermDurationMonths).Append("\n");
            sb.Append("  TermFixedEndDate: ").Append(TermFixedEndDate).Append("\n");
            sb.Append("  RenewalType: ").Append(RenewalType).Append("\n");
            sb.Append("  RenewalPeriodMonths: ").Append(RenewalPeriodMonths).Append("\n");
            sb.Append("  NoticeDays: ").Append(NoticeDays).Append("\n");
            sb.Append("  ReminderOffsets: ").Append(ReminderOffsets).Append("\n");
            sb.Append("  NotifyCounterparty: ").Append(NotifyCounterparty).Append("\n");
            sb.Append("}\n");
            return sb.ToString();
        }

        /// <summary>
        /// Returns the JSON string presentation of the object
        /// </summary>
        /// <returns>JSON string presentation of the object</returns>
        public virtual string ToJson()
        {
            return Newtonsoft.Json.JsonConvert.SerializeObject(this, Newtonsoft.Json.Formatting.Indented);
        }

        /// <summary>
        /// To validate all properties of the instance
        /// </summary>
        /// <param name="validationContext">Validation context</param>
        /// <returns>Validation Result</returns>
        IEnumerable<ValidationResult> IValidatableObject.Validate(ValidationContext validationContext)
        {
            // TermDurationMonths (int?) maximum
            if (this.TermDurationMonths > (int?)600)
            {
                yield return new ValidationResult("Invalid value for TermDurationMonths, must be a value less than or equal to 600.", new [] { "TermDurationMonths" });
            }

            // TermDurationMonths (int?) minimum
            if (this.TermDurationMonths < (int?)1)
            {
                yield return new ValidationResult("Invalid value for TermDurationMonths, must be a value greater than or equal to 1.", new [] { "TermDurationMonths" });
            }

            // RenewalPeriodMonths (int?) maximum
            if (this.RenewalPeriodMonths > (int?)600)
            {
                yield return new ValidationResult("Invalid value for RenewalPeriodMonths, must be a value less than or equal to 600.", new [] { "RenewalPeriodMonths" });
            }

            // RenewalPeriodMonths (int?) minimum
            if (this.RenewalPeriodMonths < (int?)1)
            {
                yield return new ValidationResult("Invalid value for RenewalPeriodMonths, must be a value greater than or equal to 1.", new [] { "RenewalPeriodMonths" });
            }

            // NoticeDays (int?) maximum
            if (this.NoticeDays > (int?)3650)
            {
                yield return new ValidationResult("Invalid value for NoticeDays, must be a value less than or equal to 3650.", new [] { "NoticeDays" });
            }

            // NoticeDays (int?) minimum
            if (this.NoticeDays < (int?)0)
            {
                yield return new ValidationResult("Invalid value for NoticeDays, must be a value greater than or equal to 0.", new [] { "NoticeDays" });
            }

            yield break;
        }
    }

}
