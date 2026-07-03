/*
 * imzala External API
 *
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.6.0 · **Son güncelleme:** 2026-06-30  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API key kullanıyorsanız `X-Workspace-Id` header'ı göndermeniz gerekir (organizasyon UUID'si). Kişisel anahtarlar için bu header gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - 60 istek/dakika per API key - Aşılırsa 429 döner  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (6) | Olay | Tetikleyici | |- -- -- -|- -- -- -- -- -- --| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 6 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); - - INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.7.0
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
    /// Hatırlatma yapılandırması. Şablon (&#x60;Template.reminder_*&#x60;) ve sözleşme (&#x60;ReminderConfig&#x60;) arasında aynı şemaya sahiptir. 
    /// </summary>
    [DataContract(Name = "ReminderSettings")]
    public partial class ReminderSettings : IValidatableObject
    {
        /// <summary>
        /// Defines Channels
        /// </summary>
        [JsonConverter(typeof(StringEnumConverter))]
        public enum ChannelsEnum
        {
            /// <summary>
            /// Enum Email for value: email
            /// </summary>
            [EnumMember(Value = "email")]
            Email = 1,

            /// <summary>
            /// Enum Sms for value: sms
            /// </summary>
            [EnumMember(Value = "sms")]
            Sms = 2
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ReminderSettings" /> class.
        /// </summary>
        /// <param name="enabled">false → hiç hatırlatma gönderilmez (cron schedule edilmez) (default to true).</param>
        /// <param name="intervalsHours">Sözleşmenin oluşturulduğu andan itibaren saat cinsinden hatırlatma aralıkları. Örn. &#x60;[24, 72, 168]&#x60; → 24 saat sonra, 3 gün sonra ve 7 gün sonra. &#x60;max_reminders&#x60; ile limit edilir. .</param>
        /// <param name="maxReminders">Bir parti için maksimum gönderilecek hatırlatma sayısı (default to 1).</param>
        /// <param name="channels">Hatırlatma gönderim kanalı. Birden fazla seçilebilir (&#x60;[\&quot;email\&quot;,\&quot;sms\&quot;]&#x60;). SMS kanalı için partinin &#x60;phone&#x60; alanı dolu ve &#x60;send_sms: true&#x60; olmalı; email kanalı için &#x60;email&#x60; dolu ve &#x60;send_email: true&#x60; olmalı, ayrıca demand&#39;in &#x60;send_sms_notifications&#x60; / &#x60;send_email_notifications&#x60; global toggle&#39;ı açık olmalı. .</param>
        public ReminderSettings(bool enabled = true, List<int> intervalsHours = default, int maxReminders = 1, List<ChannelsEnum> channels = default)
        {
            this.Enabled = enabled;
            this.IntervalsHours = intervalsHours;
            this.MaxReminders = maxReminders;
            this.Channels = channels;
        }

        /// <summary>
        /// false → hiç hatırlatma gönderilmez (cron schedule edilmez)
        /// </summary>
        /// <value>false → hiç hatırlatma gönderilmez (cron schedule edilmez)</value>
        [DataMember(Name = "enabled", EmitDefaultValue = true)]
        public bool Enabled { get; set; }

        /// <summary>
        /// Sözleşmenin oluşturulduğu andan itibaren saat cinsinden hatırlatma aralıkları. Örn. &#x60;[24, 72, 168]&#x60; → 24 saat sonra, 3 gün sonra ve 7 gün sonra. &#x60;max_reminders&#x60; ile limit edilir. 
        /// </summary>
        /// <value>Sözleşmenin oluşturulduğu andan itibaren saat cinsinden hatırlatma aralıkları. Örn. &#x60;[24, 72, 168]&#x60; → 24 saat sonra, 3 gün sonra ve 7 gün sonra. &#x60;max_reminders&#x60; ile limit edilir. </value>
        /*
        <example>[24,72]</example>
        */
        [DataMember(Name = "intervals_hours", EmitDefaultValue = false)]
        public List<int> IntervalsHours { get; set; }

        /// <summary>
        /// Bir parti için maksimum gönderilecek hatırlatma sayısı
        /// </summary>
        /// <value>Bir parti için maksimum gönderilecek hatırlatma sayısı</value>
        /*
        <example>2</example>
        */
        [DataMember(Name = "max_reminders", EmitDefaultValue = false)]
        public int MaxReminders { get; set; }

        /// <summary>
        /// Hatırlatma gönderim kanalı. Birden fazla seçilebilir (&#x60;[\&quot;email\&quot;,\&quot;sms\&quot;]&#x60;). SMS kanalı için partinin &#x60;phone&#x60; alanı dolu ve &#x60;send_sms: true&#x60; olmalı; email kanalı için &#x60;email&#x60; dolu ve &#x60;send_email: true&#x60; olmalı, ayrıca demand&#39;in &#x60;send_sms_notifications&#x60; / &#x60;send_email_notifications&#x60; global toggle&#39;ı açık olmalı. 
        /// </summary>
        /// <value>Hatırlatma gönderim kanalı. Birden fazla seçilebilir (&#x60;[\&quot;email\&quot;,\&quot;sms\&quot;]&#x60;). SMS kanalı için partinin &#x60;phone&#x60; alanı dolu ve &#x60;send_sms: true&#x60; olmalı; email kanalı için &#x60;email&#x60; dolu ve &#x60;send_email: true&#x60; olmalı, ayrıca demand&#39;in &#x60;send_sms_notifications&#x60; / &#x60;send_email_notifications&#x60; global toggle&#39;ı açık olmalı. </value>
        /*
        <example>[&quot;email&quot;,&quot;sms&quot;]</example>
        */
        [DataMember(Name = "channels", EmitDefaultValue = false)]
        public List<ReminderSettings.ChannelsEnum> Channels { get; set; }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class ReminderSettings {\n");
            sb.Append("  Enabled: ").Append(Enabled).Append("\n");
            sb.Append("  IntervalsHours: ").Append(IntervalsHours).Append("\n");
            sb.Append("  MaxReminders: ").Append(MaxReminders).Append("\n");
            sb.Append("  Channels: ").Append(Channels).Append("\n");
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
            // MaxReminders (int) minimum
            if (this.MaxReminders < (int)0)
            {
                yield return new ValidationResult("Invalid value for MaxReminders, must be a value greater than or equal to 0.", new [] { "MaxReminders" });
            }

            yield break;
        }
    }

}
