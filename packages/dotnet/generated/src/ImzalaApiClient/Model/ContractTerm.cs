/*
 * imzala External API
 *
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.10.2 · **Son güncelleme:** 2026-10-06  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır (aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Onaylayan (onay adımı) Şablonda bir taraf **Onaylayan** olarak işaretlenmişse, o şablondan oluşturulan sözleşmede bu taraf belgeyi imzalamaz, onaylar. Onaylayan içeren sözleşmelerde davetler iki aşamada gönderilir: - Sözleşme oluşturulurken (`POST /api/v1/demands`,   `POST /api/v1/demands/bulk`) ve `POST /api/v1/demands/{demandId}/dispatch`   çağrıldığında önce yalnızca onay bekleyen onaylayanlar davet edilir.   Sıralı sözleşmede (`ordered: true`) onaylayanlar da kendi sıralarıyla   davet edilir. - İmzacılar, tüm onaylayanlar onayladıktan sonra otomatik olarak davet   edilir. - Bir onaylayan sözleşmeyi reddederse imzacılar davet edilmez;   `dispatch` çağrısı da kimseye davet göndermez. - Onay vermiş bir onaylayan `dispatch` ile yeniden davet edilmez.  Onaylayan içermeyen sözleşmelerde davet davranışı değişmez.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |- -- -|- -- -- --|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  `403 AI_TOOL_ACCESS_DISABLED`: organizasyon yöneticisi, organizasyonun yapay zekâ araçlarıyla erişimini kapatmıştır. Bu kontrol resmî İmzala MCP istemcisini (`User-Agent` başlığı `imzala-mcp/` ile başlayan istekler) tanır ve yalnız organizasyon çalışma alanını hedefleyen isteklerde uygulanır. Yanıt gövdesi:  ```json {   \"error\": \"AI_TOOL_ACCESS_DISABLED\",   \"code\": \"AI_TOOL_ACCESS_DISABLED\",   \"message\": \"Kurumunuz yapay zekâ araçlarının erişimini kapattı.\" } ```  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez. - Hatırlatma yalnızca davet sırası gelmiş taraflara gönderilir. Onaylayan   onayını beklerken imzacılar, sıralı sözleşmede sırası henüz gelmemiş   taraflar ve bir onaylayan reddettiyse tüm taraflar `details[]` içinde   `skipped` olarak `reason: \"party_not_eligible\"` ile döner.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (11) | Olay | Tetikleyici | |- -- -- -|- -- -- -- -- -- --| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı | | `contract.expiring` | Takip edilen sözleşme bitiş tarihine yaklaşıyor | | `contract.ended` | Takip edilen sözleşmenin platformdaki bitiş tarihi geçti | | `contract.advanced` | Otomatik yenilenen sözleşmenin takip edilen bitişi ileri alındı |  > **Not (contract.\\* olayları):** Bunlar platformun **takip amaçlı** > kayıtlarıdır; sözleşmenin hukuken yenilendiğini veya sona erdiğini > BELİRTMEZ. Detay için `ContractTerm` şemasına ve `state` alanına bakın.  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type`: yukarıdaki 11 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); - - INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.10.2
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
    /// Sözleşme süre/yenileme takibi. Sözleşmede takip hiç tanımlanmadıysa (&#x60;start_mode&#x60; VE &#x60;end_date&#x60; boşsa) bu alan &#x60;null&#x60; döner.  Bu, hukuki bir yenileme veya sona erme beyanı değildir; platformun takip amaçlı kaydıdır. Sözleşmenin fiilen ne zaman sona erdiği veya yenilendiği, tarafların kendi sözleşme hükümlerine ve yürürlükteki hukuka tabidir. Bu alan yalnız hatırlatma ve görünürlük sağlar. 
    /// </summary>
    [DataContract(Name = "ContractTerm")]
    public partial class ContractTerm : IValidatableObject
    {
        /// <summary>
        /// Takibin ne zaman başladığı. &#x60;FIXED_DATE&#x60;: &#x60;start_date&#x60;&#39;ten. &#x60;ON_FIRST_SIGNATURE&#x60; / &#x60;ON_COMPLETION&#x60;: ilk imza / tüm imzalar tamamlandığı anda otomatik (o ana kadar &#x60;end_date&#x60; bilinmez). 
        /// </summary>
        /// <value>Takibin ne zaman başladığı. &#x60;FIXED_DATE&#x60;: &#x60;start_date&#x60;&#39;ten. &#x60;ON_FIRST_SIGNATURE&#x60; / &#x60;ON_COMPLETION&#x60;: ilk imza / tüm imzalar tamamlandığı anda otomatik (o ana kadar &#x60;end_date&#x60; bilinmez). </value>
        [JsonConverter(typeof(StringEnumConverter))]
        public enum StartModeEnum
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
        /// Takibin ne zaman başladığı. &#x60;FIXED_DATE&#x60;: &#x60;start_date&#x60;&#39;ten. &#x60;ON_FIRST_SIGNATURE&#x60; / &#x60;ON_COMPLETION&#x60;: ilk imza / tüm imzalar tamamlandığı anda otomatik (o ana kadar &#x60;end_date&#x60; bilinmez). 
        /// </summary>
        /// <value>Takibin ne zaman başladığı. &#x60;FIXED_DATE&#x60;: &#x60;start_date&#x60;&#39;ten. &#x60;ON_FIRST_SIGNATURE&#x60; / &#x60;ON_COMPLETION&#x60;: ilk imza / tüm imzalar tamamlandığı anda otomatik (o ana kadar &#x60;end_date&#x60; bilinmez). </value>
        [DataMember(Name = "start_mode", IsRequired = true, EmitDefaultValue = true)]
        public StartModeEnum StartMode { get; set; }
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
        [DataMember(Name = "renewal_type", IsRequired = true, EmitDefaultValue = true)]
        public RenewalTypeEnum RenewalType { get; set; }
        /// <summary>
        /// &#x60;UNTRACKED&#x60;: takip yok (&#x60;end_date&#x60; bilinmiyor). &#x60;ACTIVE&#x60;: takip sürüyor. &#x60;ENDED&#x60;: &#x60;FIXED_TERM&#x60; sözleşmenin bitiş tarihi geçti. &#x60;STOPPED&#x60;: yenileme takibi kullanıcı tarafından durduruldu. 
        /// </summary>
        /// <value>&#x60;UNTRACKED&#x60;: takip yok (&#x60;end_date&#x60; bilinmiyor). &#x60;ACTIVE&#x60;: takip sürüyor. &#x60;ENDED&#x60;: &#x60;FIXED_TERM&#x60; sözleşmenin bitiş tarihi geçti. &#x60;STOPPED&#x60;: yenileme takibi kullanıcı tarafından durduruldu. </value>
        [JsonConverter(typeof(StringEnumConverter))]
        public enum StateEnum
        {
            /// <summary>
            /// Enum UNTRACKED for value: UNTRACKED
            /// </summary>
            [EnumMember(Value = "UNTRACKED")]
            UNTRACKED = 1,

            /// <summary>
            /// Enum ACTIVE for value: ACTIVE
            /// </summary>
            [EnumMember(Value = "ACTIVE")]
            ACTIVE = 2,

            /// <summary>
            /// Enum ENDED for value: ENDED
            /// </summary>
            [EnumMember(Value = "ENDED")]
            ENDED = 3,

            /// <summary>
            /// Enum STOPPED for value: STOPPED
            /// </summary>
            [EnumMember(Value = "STOPPED")]
            STOPPED = 4
        }


        /// <summary>
        /// &#x60;UNTRACKED&#x60;: takip yok (&#x60;end_date&#x60; bilinmiyor). &#x60;ACTIVE&#x60;: takip sürüyor. &#x60;ENDED&#x60;: &#x60;FIXED_TERM&#x60; sözleşmenin bitiş tarihi geçti. &#x60;STOPPED&#x60;: yenileme takibi kullanıcı tarafından durduruldu. 
        /// </summary>
        /// <value>&#x60;UNTRACKED&#x60;: takip yok (&#x60;end_date&#x60; bilinmiyor). &#x60;ACTIVE&#x60;: takip sürüyor. &#x60;ENDED&#x60;: &#x60;FIXED_TERM&#x60; sözleşmenin bitiş tarihi geçti. &#x60;STOPPED&#x60;: yenileme takibi kullanıcı tarafından durduruldu. </value>
        [DataMember(Name = "state", IsRequired = true, EmitDefaultValue = true)]
        public StateEnum State { get; set; }
        /// <summary>
        /// Initializes a new instance of the <see cref="ContractTerm" /> class.
        /// </summary>
        [JsonConstructorAttribute]
        protected ContractTerm() { }
        /// <summary>
        /// Initializes a new instance of the <see cref="ContractTerm" /> class.
        /// </summary>
        /// <param name="startMode">Takibin ne zaman başladığı. &#x60;FIXED_DATE&#x60;: &#x60;start_date&#x60;&#39;ten. &#x60;ON_FIRST_SIGNATURE&#x60; / &#x60;ON_COMPLETION&#x60;: ilk imza / tüm imzalar tamamlandığı anda otomatik (o ana kadar &#x60;end_date&#x60; bilinmez).  (required).</param>
        /// <param name="startDate">startDate (required).</param>
        /// <param name="durationMonths">durationMonths (required).</param>
        /// <param name="fixedEndDate">fixedEndDate (required).</param>
        /// <param name="endDate">Takip edilen bitiş tarihi. &#x60;renewal_type: AUTO_RENEW&#x60; sözleşmelerde her dönemde (günlük olarak) bir sonraki döneme ileri alınır; bkz. &#x60;contract.advanced&#x60; webhook olayı.  (required).</param>
        /// <param name="endDateSigned">İmzalanan belge metninde bitiş tarihi bir değişken olarak yazılıysa, sözleşme tamamlandığı anda &#x60;end_date&#x60;&#39;in dondurulmuş kopyası. &#x60;end_date&#x60; sonradan ileri alınsa bile bu alan DEĞİŞMEZ. İmzalanan metinle takip değeri arasındaki tarihsel referans budur.  (required).</param>
        /// <param name="renewalType">renewalType (required).</param>
        /// <param name="renewalPeriodMonths">renewalPeriodMonths (required).</param>
        /// <param name="noticeDays">Fesih ihbarı için bitişten önce kaç gün gerektiği. (required).</param>
        /// <param name="noticeDeadline">Hesaplanır (&#x60;end_date - notice_days&#x60;); &#x60;end_date&#x60; veya &#x60;notice_days&#x60; boşsa &#x60;null&#x60;. (required).</param>
        /// <param name="reminderOffsets">Bitişten kaç gün önce hatırlatma gönderileceği (gün sayıları). (required).</param>
        /// <param name="notifyCounterparty">Karşı tarafa da hatırlatma gönderilsin mi. (required).</param>
        /// <param name="state">&#x60;UNTRACKED&#x60;: takip yok (&#x60;end_date&#x60; bilinmiyor). &#x60;ACTIVE&#x60;: takip sürüyor. &#x60;ENDED&#x60;: &#x60;FIXED_TERM&#x60; sözleşmenin bitiş tarihi geçti. &#x60;STOPPED&#x60;: yenileme takibi kullanıcı tarafından durduruldu.  (required).</param>
        /// <param name="daysLeft">Bugünden &#x60;end_date&#x60;&#39;e gün sayısı (negatifse geçmişte). (required).</param>
        /// <param name="renewalStoppedAt">renewalStoppedAt (required).</param>
        public ContractTerm(StartModeEnum startMode = default, DateOnly? startDate = default, int? durationMonths = default, DateOnly? fixedEndDate = default, DateOnly? endDate = default, DateOnly? endDateSigned = default, RenewalTypeEnum renewalType = default, int? renewalPeriodMonths = default, int? noticeDays = default, DateOnly? noticeDeadline = default, List<int> reminderOffsets = default, bool notifyCounterparty = default, StateEnum state = default, int? daysLeft = default, DateTime? renewalStoppedAt = default)
        {
            this.StartMode = startMode;
            // to ensure "startDate" is required (not null)
            if (startDate == null)
            {
                throw new ArgumentNullException("startDate is a required property for ContractTerm and cannot be null");
            }
            this.StartDate = startDate;
            // to ensure "durationMonths" is required (not null)
            if (durationMonths == null)
            {
                throw new ArgumentNullException("durationMonths is a required property for ContractTerm and cannot be null");
            }
            this.DurationMonths = durationMonths;
            // to ensure "fixedEndDate" is required (not null)
            if (fixedEndDate == null)
            {
                throw new ArgumentNullException("fixedEndDate is a required property for ContractTerm and cannot be null");
            }
            this.FixedEndDate = fixedEndDate;
            // to ensure "endDate" is required (not null)
            if (endDate == null)
            {
                throw new ArgumentNullException("endDate is a required property for ContractTerm and cannot be null");
            }
            this.EndDate = endDate;
            // to ensure "endDateSigned" is required (not null)
            if (endDateSigned == null)
            {
                throw new ArgumentNullException("endDateSigned is a required property for ContractTerm and cannot be null");
            }
            this.EndDateSigned = endDateSigned;
            this.RenewalType = renewalType;
            // to ensure "renewalPeriodMonths" is required (not null)
            if (renewalPeriodMonths == null)
            {
                throw new ArgumentNullException("renewalPeriodMonths is a required property for ContractTerm and cannot be null");
            }
            this.RenewalPeriodMonths = renewalPeriodMonths;
            // to ensure "noticeDays" is required (not null)
            if (noticeDays == null)
            {
                throw new ArgumentNullException("noticeDays is a required property for ContractTerm and cannot be null");
            }
            this.NoticeDays = noticeDays;
            // to ensure "noticeDeadline" is required (not null)
            if (noticeDeadline == null)
            {
                throw new ArgumentNullException("noticeDeadline is a required property for ContractTerm and cannot be null");
            }
            this.NoticeDeadline = noticeDeadline;
            // to ensure "reminderOffsets" is required (not null)
            if (reminderOffsets == null)
            {
                throw new ArgumentNullException("reminderOffsets is a required property for ContractTerm and cannot be null");
            }
            this.ReminderOffsets = reminderOffsets;
            this.NotifyCounterparty = notifyCounterparty;
            this.State = state;
            // to ensure "daysLeft" is required (not null)
            if (daysLeft == null)
            {
                throw new ArgumentNullException("daysLeft is a required property for ContractTerm and cannot be null");
            }
            this.DaysLeft = daysLeft;
            // to ensure "renewalStoppedAt" is required (not null)
            if (renewalStoppedAt == null)
            {
                throw new ArgumentNullException("renewalStoppedAt is a required property for ContractTerm and cannot be null");
            }
            this.RenewalStoppedAt = renewalStoppedAt;
        }

        /// <summary>
        /// Gets or Sets StartDate
        /// </summary>
        [DataMember(Name = "start_date", IsRequired = true, EmitDefaultValue = true)]
        public DateOnly? StartDate { get; set; }

        /// <summary>
        /// Gets or Sets DurationMonths
        /// </summary>
        [DataMember(Name = "duration_months", IsRequired = true, EmitDefaultValue = true)]
        public int? DurationMonths { get; set; }

        /// <summary>
        /// Gets or Sets FixedEndDate
        /// </summary>
        [DataMember(Name = "fixed_end_date", IsRequired = true, EmitDefaultValue = true)]
        public DateOnly? FixedEndDate { get; set; }

        /// <summary>
        /// Takip edilen bitiş tarihi. &#x60;renewal_type: AUTO_RENEW&#x60; sözleşmelerde her dönemde (günlük olarak) bir sonraki döneme ileri alınır; bkz. &#x60;contract.advanced&#x60; webhook olayı. 
        /// </summary>
        /// <value>Takip edilen bitiş tarihi. &#x60;renewal_type: AUTO_RENEW&#x60; sözleşmelerde her dönemde (günlük olarak) bir sonraki döneme ileri alınır; bkz. &#x60;contract.advanced&#x60; webhook olayı. </value>
        [DataMember(Name = "end_date", IsRequired = true, EmitDefaultValue = true)]
        public DateOnly? EndDate { get; set; }

        /// <summary>
        /// İmzalanan belge metninde bitiş tarihi bir değişken olarak yazılıysa, sözleşme tamamlandığı anda &#x60;end_date&#x60;&#39;in dondurulmuş kopyası. &#x60;end_date&#x60; sonradan ileri alınsa bile bu alan DEĞİŞMEZ. İmzalanan metinle takip değeri arasındaki tarihsel referans budur. 
        /// </summary>
        /// <value>İmzalanan belge metninde bitiş tarihi bir değişken olarak yazılıysa, sözleşme tamamlandığı anda &#x60;end_date&#x60;&#39;in dondurulmuş kopyası. &#x60;end_date&#x60; sonradan ileri alınsa bile bu alan DEĞİŞMEZ. İmzalanan metinle takip değeri arasındaki tarihsel referans budur. </value>
        [DataMember(Name = "end_date_signed", IsRequired = true, EmitDefaultValue = true)]
        public DateOnly? EndDateSigned { get; set; }

        /// <summary>
        /// Gets or Sets RenewalPeriodMonths
        /// </summary>
        [DataMember(Name = "renewal_period_months", IsRequired = true, EmitDefaultValue = true)]
        public int? RenewalPeriodMonths { get; set; }

        /// <summary>
        /// Fesih ihbarı için bitişten önce kaç gün gerektiği.
        /// </summary>
        /// <value>Fesih ihbarı için bitişten önce kaç gün gerektiği.</value>
        [DataMember(Name = "notice_days", IsRequired = true, EmitDefaultValue = true)]
        public int? NoticeDays { get; set; }

        /// <summary>
        /// Hesaplanır (&#x60;end_date - notice_days&#x60;); &#x60;end_date&#x60; veya &#x60;notice_days&#x60; boşsa &#x60;null&#x60;.
        /// </summary>
        /// <value>Hesaplanır (&#x60;end_date - notice_days&#x60;); &#x60;end_date&#x60; veya &#x60;notice_days&#x60; boşsa &#x60;null&#x60;.</value>
        [DataMember(Name = "notice_deadline", IsRequired = true, EmitDefaultValue = true)]
        public DateOnly? NoticeDeadline { get; set; }

        /// <summary>
        /// Bitişten kaç gün önce hatırlatma gönderileceği (gün sayıları).
        /// </summary>
        /// <value>Bitişten kaç gün önce hatırlatma gönderileceği (gün sayıları).</value>
        [DataMember(Name = "reminder_offsets", IsRequired = true, EmitDefaultValue = true)]
        public List<int> ReminderOffsets { get; set; }

        /// <summary>
        /// Karşı tarafa da hatırlatma gönderilsin mi.
        /// </summary>
        /// <value>Karşı tarafa da hatırlatma gönderilsin mi.</value>
        [DataMember(Name = "notify_counterparty", IsRequired = true, EmitDefaultValue = true)]
        public bool NotifyCounterparty { get; set; }

        /// <summary>
        /// Bugünden &#x60;end_date&#x60;&#39;e gün sayısı (negatifse geçmişte).
        /// </summary>
        /// <value>Bugünden &#x60;end_date&#x60;&#39;e gün sayısı (negatifse geçmişte).</value>
        [DataMember(Name = "days_left", IsRequired = true, EmitDefaultValue = true)]
        public int? DaysLeft { get; set; }

        /// <summary>
        /// Gets or Sets RenewalStoppedAt
        /// </summary>
        [DataMember(Name = "renewal_stopped_at", IsRequired = true, EmitDefaultValue = true)]
        public DateTime? RenewalStoppedAt { get; set; }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class ContractTerm {\n");
            sb.Append("  StartMode: ").Append(StartMode).Append("\n");
            sb.Append("  StartDate: ").Append(StartDate).Append("\n");
            sb.Append("  DurationMonths: ").Append(DurationMonths).Append("\n");
            sb.Append("  FixedEndDate: ").Append(FixedEndDate).Append("\n");
            sb.Append("  EndDate: ").Append(EndDate).Append("\n");
            sb.Append("  EndDateSigned: ").Append(EndDateSigned).Append("\n");
            sb.Append("  RenewalType: ").Append(RenewalType).Append("\n");
            sb.Append("  RenewalPeriodMonths: ").Append(RenewalPeriodMonths).Append("\n");
            sb.Append("  NoticeDays: ").Append(NoticeDays).Append("\n");
            sb.Append("  NoticeDeadline: ").Append(NoticeDeadline).Append("\n");
            sb.Append("  ReminderOffsets: ").Append(ReminderOffsets).Append("\n");
            sb.Append("  NotifyCounterparty: ").Append(NotifyCounterparty).Append("\n");
            sb.Append("  State: ").Append(State).Append("\n");
            sb.Append("  DaysLeft: ").Append(DaysLeft).Append("\n");
            sb.Append("  RenewalStoppedAt: ").Append(RenewalStoppedAt).Append("\n");
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
            // DurationMonths (int?) maximum
            if (this.DurationMonths > (int?)600)
            {
                yield return new ValidationResult("Invalid value for DurationMonths, must be a value less than or equal to 600.", new [] { "DurationMonths" });
            }

            // DurationMonths (int?) minimum
            if (this.DurationMonths < (int?)1)
            {
                yield return new ValidationResult("Invalid value for DurationMonths, must be a value greater than or equal to 1.", new [] { "DurationMonths" });
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
