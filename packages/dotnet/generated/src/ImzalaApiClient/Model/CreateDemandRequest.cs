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
    /// CreateDemandRequest
    /// </summary>
    [DataContract(Name = "CreateDemandRequest")]
    public partial class CreateDemandRequest : IValidatableObject
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="CreateDemandRequest" /> class.
        /// </summary>
        [JsonConstructorAttribute]
        protected CreateDemandRequest() { }
        /// <summary>
        /// Initializes a new instance of the <see cref="CreateDemandRequest" /> class.
        /// </summary>
        /// <param name="templateId">GET /api/v1/templates listesinden veya dashboard&#39;dan kopyalayın (required).</param>
        /// <param name="title">Sözleşme başlığı (yoksa template adı kullanılır).</param>
        /// <param name="description">description.</param>
        /// <param name="partyMapping">partyMapping (required).</param>
        /// <param name="variables">**Root scope** — partilerden bağımsız field&#39;lara gönderilen değerler. Item&#39;ın template_party_id&#39;si NULL ise (partisiz) buradan dolar. Multi-party şablonda kira_baslangic_tarihi gibi paylaşılan field&#39;lar. .</param>
        /// <param name="hasTimestamp">TÜBİTAK zaman damgası (default to false).</param>
        /// <param name="sendSmsNotifications">sendSmsNotifications (default to true).</param>
        /// <param name="sendEmailNotifications">sendEmailNotifications (default to true).</param>
        /// <param name="smsTitle">SMS gönderici adı (default to &quot;CODECK&quot;).</param>
        /// <param name="smsContent">Custom SMS gövdesi. **Sadece** çağıran organizasyon **PRO veya ENTERPRISE planda** ise ve aktif &#x60;OrganizationSmsConfig&#x60; (sender_name dolu) varsa kabul edilir; aksi halde 403 &#x60;SMS_CUSTOMIZATION_NOT_ALLOWED&#x60; döner.  FREE/BASIC planda olan veya kendi SMS sağlayıcısı tanımlı olmayan müşterilerin marka itibarını korumak için sistem default sağlayıcısı (Codeck NetGSM) ile gönderim yapılır ve özel metin reddedilir. Kendi sağlayıcınızı tanımlamak için Dashboard → Organizasyon → SMS Ayarları sayfasını kullanın.  Boş string / null gönderirseniz \&quot;clear\&quot; olarak yorumlanır (gating&#39;den geçer). .</param>
        /// <param name="emailContent">Custom e-posta gövdesi.</param>
        /// <param name="expiryDate">expiryDate.</param>
        /// <param name="requireTcVerification">requireTcVerification (default to false).</param>
        /// <param name="requireBiometricVerification">requireBiometricVerification (default to false).</param>
        /// <param name="reminderSettings">Bu sözleşme için hatırlatma ayarlarını **şablon default&#39;unu override** ederek belirtir. Yollanmazsa şablonun &#x60;reminder_*&#x60; alanları kullanılır (PUT /api/templates/:id ile dashboard&#39;dan kaydedilen değerler); şablonda da yoksa &#x60;{enabled:true, intervals_hours:[48], max_reminders:1, channels:[\&quot;email\&quot;]}&#x60; default&#39;u uygulanır. Demand oluşumunda &#x60;ReminderConfig&#x60; satırı yaratılır ve BullMQ kuyruğuna scheduled hatırlatmalar yazılır. .</param>
        public CreateDemandRequest(Guid templateId = default, string title = default, string description = default, List<PartyMappingInput> partyMapping = default, Dictionary<string, PartyMappingInputVariablesValue> variables = default, bool hasTimestamp = false, bool sendSmsNotifications = true, bool sendEmailNotifications = true, string smsTitle = @"CODECK", string smsContent = default, string emailContent = default, DateTime expiryDate = default, bool requireTcVerification = false, bool requireBiometricVerification = false, ReminderSettings reminderSettings = default)
        {
            this.TemplateId = templateId;
            // to ensure "partyMapping" is required (not null)
            if (partyMapping == null)
            {
                throw new ArgumentNullException("partyMapping is a required property for CreateDemandRequest and cannot be null");
            }
            this.PartyMapping = partyMapping;
            this.Title = title;
            this.Description = description;
            this.Variables = variables;
            this.HasTimestamp = hasTimestamp;
            this.SendSmsNotifications = sendSmsNotifications;
            this.SendEmailNotifications = sendEmailNotifications;
            // use default value if no "smsTitle" provided
            this.SmsTitle = smsTitle ?? @"CODECK";
            this.SmsContent = smsContent;
            this.EmailContent = emailContent;
            this.ExpiryDate = expiryDate;
            this.RequireTcVerification = requireTcVerification;
            this.RequireBiometricVerification = requireBiometricVerification;
            this.ReminderSettings = reminderSettings;
        }

        /// <summary>
        /// GET /api/v1/templates listesinden veya dashboard&#39;dan kopyalayın
        /// </summary>
        /// <value>GET /api/v1/templates listesinden veya dashboard&#39;dan kopyalayın</value>
        [DataMember(Name = "template_id", IsRequired = true, EmitDefaultValue = true)]
        public Guid TemplateId { get; set; }

        /// <summary>
        /// Sözleşme başlığı (yoksa template adı kullanılır)
        /// </summary>
        /// <value>Sözleşme başlığı (yoksa template adı kullanılır)</value>
        [DataMember(Name = "title", EmitDefaultValue = false)]
        public string Title { get; set; }

        /// <summary>
        /// Gets or Sets Description
        /// </summary>
        [DataMember(Name = "description", EmitDefaultValue = false)]
        public string Description { get; set; }

        /// <summary>
        /// Gets or Sets PartyMapping
        /// </summary>
        [DataMember(Name = "party_mapping", IsRequired = true, EmitDefaultValue = true)]
        public List<PartyMappingInput> PartyMapping { get; set; }

        /// <summary>
        /// **Root scope** — partilerden bağımsız field&#39;lara gönderilen değerler. Item&#39;ın template_party_id&#39;si NULL ise (partisiz) buradan dolar. Multi-party şablonda kira_baslangic_tarihi gibi paylaşılan field&#39;lar. 
        /// </summary>
        /// <value>**Root scope** — partilerden bağımsız field&#39;lara gönderilen değerler. Item&#39;ın template_party_id&#39;si NULL ise (partisiz) buradan dolar. Multi-party şablonda kira_baslangic_tarihi gibi paylaşılan field&#39;lar. </value>
        [DataMember(Name = "variables", EmitDefaultValue = false)]
        public Dictionary<string, PartyMappingInputVariablesValue> Variables { get; set; }

        /// <summary>
        /// TÜBİTAK zaman damgası
        /// </summary>
        /// <value>TÜBİTAK zaman damgası</value>
        [DataMember(Name = "has_timestamp", EmitDefaultValue = true)]
        public bool HasTimestamp { get; set; }

        /// <summary>
        /// Gets or Sets SendSmsNotifications
        /// </summary>
        [DataMember(Name = "send_sms_notifications", EmitDefaultValue = true)]
        public bool SendSmsNotifications { get; set; }

        /// <summary>
        /// Gets or Sets SendEmailNotifications
        /// </summary>
        [DataMember(Name = "send_email_notifications", EmitDefaultValue = true)]
        public bool SendEmailNotifications { get; set; }

        /// <summary>
        /// SMS gönderici adı
        /// </summary>
        /// <value>SMS gönderici adı</value>
        [DataMember(Name = "sms_title", EmitDefaultValue = false)]
        public string SmsTitle { get; set; }

        /// <summary>
        /// Custom SMS gövdesi. **Sadece** çağıran organizasyon **PRO veya ENTERPRISE planda** ise ve aktif &#x60;OrganizationSmsConfig&#x60; (sender_name dolu) varsa kabul edilir; aksi halde 403 &#x60;SMS_CUSTOMIZATION_NOT_ALLOWED&#x60; döner.  FREE/BASIC planda olan veya kendi SMS sağlayıcısı tanımlı olmayan müşterilerin marka itibarını korumak için sistem default sağlayıcısı (Codeck NetGSM) ile gönderim yapılır ve özel metin reddedilir. Kendi sağlayıcınızı tanımlamak için Dashboard → Organizasyon → SMS Ayarları sayfasını kullanın.  Boş string / null gönderirseniz \&quot;clear\&quot; olarak yorumlanır (gating&#39;den geçer). 
        /// </summary>
        /// <value>Custom SMS gövdesi. **Sadece** çağıran organizasyon **PRO veya ENTERPRISE planda** ise ve aktif &#x60;OrganizationSmsConfig&#x60; (sender_name dolu) varsa kabul edilir; aksi halde 403 &#x60;SMS_CUSTOMIZATION_NOT_ALLOWED&#x60; döner.  FREE/BASIC planda olan veya kendi SMS sağlayıcısı tanımlı olmayan müşterilerin marka itibarını korumak için sistem default sağlayıcısı (Codeck NetGSM) ile gönderim yapılır ve özel metin reddedilir. Kendi sağlayıcınızı tanımlamak için Dashboard → Organizasyon → SMS Ayarları sayfasını kullanın.  Boş string / null gönderirseniz \&quot;clear\&quot; olarak yorumlanır (gating&#39;den geçer). </value>
        [DataMember(Name = "sms_content", EmitDefaultValue = false)]
        public string SmsContent { get; set; }

        /// <summary>
        /// Custom e-posta gövdesi
        /// </summary>
        /// <value>Custom e-posta gövdesi</value>
        [DataMember(Name = "email_content", EmitDefaultValue = false)]
        public string EmailContent { get; set; }

        /// <summary>
        /// Gets or Sets ExpiryDate
        /// </summary>
        [DataMember(Name = "expiry_date", EmitDefaultValue = false)]
        public DateTime ExpiryDate { get; set; }

        /// <summary>
        /// Gets or Sets RequireTcVerification
        /// </summary>
        [DataMember(Name = "require_tc_verification", EmitDefaultValue = true)]
        public bool RequireTcVerification { get; set; }

        /// <summary>
        /// Gets or Sets RequireBiometricVerification
        /// </summary>
        [DataMember(Name = "require_biometric_verification", EmitDefaultValue = true)]
        public bool RequireBiometricVerification { get; set; }

        /// <summary>
        /// Bu sözleşme için hatırlatma ayarlarını **şablon default&#39;unu override** ederek belirtir. Yollanmazsa şablonun &#x60;reminder_*&#x60; alanları kullanılır (PUT /api/templates/:id ile dashboard&#39;dan kaydedilen değerler); şablonda da yoksa &#x60;{enabled:true, intervals_hours:[48], max_reminders:1, channels:[\&quot;email\&quot;]}&#x60; default&#39;u uygulanır. Demand oluşumunda &#x60;ReminderConfig&#x60; satırı yaratılır ve BullMQ kuyruğuna scheduled hatırlatmalar yazılır. 
        /// </summary>
        /// <value>Bu sözleşme için hatırlatma ayarlarını **şablon default&#39;unu override** ederek belirtir. Yollanmazsa şablonun &#x60;reminder_*&#x60; alanları kullanılır (PUT /api/templates/:id ile dashboard&#39;dan kaydedilen değerler); şablonda da yoksa &#x60;{enabled:true, intervals_hours:[48], max_reminders:1, channels:[\&quot;email\&quot;]}&#x60; default&#39;u uygulanır. Demand oluşumunda &#x60;ReminderConfig&#x60; satırı yaratılır ve BullMQ kuyruğuna scheduled hatırlatmalar yazılır. </value>
        [DataMember(Name = "reminder_settings", EmitDefaultValue = false)]
        public ReminderSettings ReminderSettings { get; set; }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class CreateDemandRequest {\n");
            sb.Append("  TemplateId: ").Append(TemplateId).Append("\n");
            sb.Append("  Title: ").Append(Title).Append("\n");
            sb.Append("  Description: ").Append(Description).Append("\n");
            sb.Append("  PartyMapping: ").Append(PartyMapping).Append("\n");
            sb.Append("  Variables: ").Append(Variables).Append("\n");
            sb.Append("  HasTimestamp: ").Append(HasTimestamp).Append("\n");
            sb.Append("  SendSmsNotifications: ").Append(SendSmsNotifications).Append("\n");
            sb.Append("  SendEmailNotifications: ").Append(SendEmailNotifications).Append("\n");
            sb.Append("  SmsTitle: ").Append(SmsTitle).Append("\n");
            sb.Append("  SmsContent: ").Append(SmsContent).Append("\n");
            sb.Append("  EmailContent: ").Append(EmailContent).Append("\n");
            sb.Append("  ExpiryDate: ").Append(ExpiryDate).Append("\n");
            sb.Append("  RequireTcVerification: ").Append(RequireTcVerification).Append("\n");
            sb.Append("  RequireBiometricVerification: ").Append(RequireBiometricVerification).Append("\n");
            sb.Append("  ReminderSettings: ").Append(ReminderSettings).Append("\n");
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
            yield break;
        }
    }

}
