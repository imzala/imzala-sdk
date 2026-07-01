/*
 * imzala External API
 *
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.6.0 · **Son güncelleme:** 2026-06-30  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API key kullanıyorsanız `X-Workspace-Id` header'ı göndermeniz gerekir (organizasyon UUID'si). Kişisel anahtarlar için bu header gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - 60 istek/dakika per API key - Aşılırsa 429 döner  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (6) | Olay | Tetikleyici | |- -- -- -|- -- -- -- -- -- --| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 6 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); - - INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.6.0
 * Contact: destek@imzala.org
 * Generated by: https://github.com/openapitools/openapi-generator.git
 */


using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Mime;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace ImzalaApiClient.Api
{

    /// <summary>
    /// Represents a collection of functions to interact with the API endpoints
    /// </summary>
    public interface ITemplatesApiSync : IApiAccessor
    {
        #region Synchronous Operations
        /// <summary>
        /// Şablon listesi
        /// </summary>
        /// <remarks>
        /// Aktif şablonlarınızı listeler.
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 20)</param>
        /// <returns>ApiV1TemplatesGet200Response</returns>
        ApiV1TemplatesGet200Response ApiV1TemplatesGet(int? page = default, int? limit = default);

        /// <summary>
        /// Şablon listesi
        /// </summary>
        /// <remarks>
        /// Aktif şablonlarınızı listeler.
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 20)</param>
        /// <returns>ApiResponse of ApiV1TemplatesGet200Response</returns>
        ApiResponse<ApiV1TemplatesGet200Response> ApiV1TemplatesGetWithHttpInfo(int? page = default, int? limit = default);
        /// <summary>
        /// Şablon detay
        /// </summary>
        /// <remarks>
        /// Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiV1TemplatesIdGet200Response</returns>
        ApiV1TemplatesIdGet200Response ApiV1TemplatesIdGet(Guid id);

        /// <summary>
        /// Şablon detay
        /// </summary>
        /// <remarks>
        /// Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of ApiV1TemplatesIdGet200Response</returns>
        ApiResponse<ApiV1TemplatesIdGet200Response> ApiV1TemplatesIdGetWithHttpInfo(Guid id);
        /// <summary>
        /// Şablon kullanım kılavuzu (curl + JSON örnek)
        /// </summary>
        /// <remarks>
        /// Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiV1TemplatesIdUsageGet200Response</returns>
        ApiV1TemplatesIdUsageGet200Response ApiV1TemplatesIdUsageGet(Guid id);

        /// <summary>
        /// Şablon kullanım kılavuzu (curl + JSON örnek)
        /// </summary>
        /// <remarks>
        /// Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of ApiV1TemplatesIdUsageGet200Response</returns>
        ApiResponse<ApiV1TemplatesIdUsageGet200Response> ApiV1TemplatesIdUsageGetWithHttpInfo(Guid id);
        #endregion Synchronous Operations
    }

    /// <summary>
    /// Represents a collection of functions to interact with the API endpoints
    /// </summary>
    public interface ITemplatesApiAsync : IApiAccessor
    {
        #region Asynchronous Operations
        /// <summary>
        /// Şablon listesi
        /// </summary>
        /// <remarks>
        /// Aktif şablonlarınızı listeler.
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 20)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TemplatesGet200Response</returns>
        System.Threading.Tasks.Task<ApiV1TemplatesGet200Response> ApiV1TemplatesGetAsync(int? page = default, int? limit = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Şablon listesi
        /// </summary>
        /// <remarks>
        /// Aktif şablonlarınızı listeler.
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 20)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TemplatesGet200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1TemplatesGet200Response>> ApiV1TemplatesGetWithHttpInfoAsync(int? page = default, int? limit = default, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Şablon detay
        /// </summary>
        /// <remarks>
        /// Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TemplatesIdGet200Response</returns>
        System.Threading.Tasks.Task<ApiV1TemplatesIdGet200Response> ApiV1TemplatesIdGetAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Şablon detay
        /// </summary>
        /// <remarks>
        /// Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TemplatesIdGet200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1TemplatesIdGet200Response>> ApiV1TemplatesIdGetWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Şablon kullanım kılavuzu (curl + JSON örnek)
        /// </summary>
        /// <remarks>
        /// Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TemplatesIdUsageGet200Response</returns>
        System.Threading.Tasks.Task<ApiV1TemplatesIdUsageGet200Response> ApiV1TemplatesIdUsageGetAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Şablon kullanım kılavuzu (curl + JSON örnek)
        /// </summary>
        /// <remarks>
        /// Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TemplatesIdUsageGet200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1TemplatesIdUsageGet200Response>> ApiV1TemplatesIdUsageGetWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);
        #endregion Asynchronous Operations
    }

    /// <summary>
    /// Represents a collection of functions to interact with the API endpoints
    /// </summary>
    public interface ITemplatesApi : ITemplatesApiSync, ITemplatesApiAsync
    {

    }

    /// <summary>
    /// Represents a collection of functions to interact with the API endpoints
    /// </summary>
    public partial class TemplatesApi : IDisposable, ITemplatesApi
    {
        private ImzalaApiClient.Client.ExceptionFactory _exceptionFactory = (name, response) => null;

        /// <summary>
        /// Initializes a new instance of the <see cref="TemplatesApi"/> class.
        /// **IMPORTANT** This will also create an instance of HttpClient, which is less than ideal.
        /// It's better to reuse the <see href="https://docs.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests#issues-with-the-original-httpclient-class-available-in-net">HttpClient and HttpClientHandler</see>.
        /// </summary>
        /// <returns></returns>
        public TemplatesApi() : this((string)null)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="TemplatesApi"/> class.
        /// **IMPORTANT** This will also create an instance of HttpClient, which is less than ideal.
        /// It's better to reuse the <see href="https://docs.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests#issues-with-the-original-httpclient-class-available-in-net">HttpClient and HttpClientHandler</see>.
        /// </summary>
        /// <param name="basePath">The target service's base path in URL format.</param>
        /// <exception cref="ArgumentException"></exception>
        /// <returns></returns>
        public TemplatesApi(string basePath)
        {
            this.Configuration = ImzalaApiClient.Client.Configuration.MergeConfigurations(
                ImzalaApiClient.Client.GlobalConfiguration.Instance,
                new ImzalaApiClient.Client.Configuration { BasePath = basePath }
            );
            this.ApiClient = new ImzalaApiClient.Client.ApiClient(this.Configuration.BasePath);
            this.Client =  this.ApiClient;
            this.AsynchronousClient = this.ApiClient;
            this.ExceptionFactory = ImzalaApiClient.Client.Configuration.DefaultExceptionFactory;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="TemplatesApi"/> class using Configuration object.
        /// **IMPORTANT** This will also create an instance of HttpClient, which is less than ideal.
        /// It's better to reuse the <see href="https://docs.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests#issues-with-the-original-httpclient-class-available-in-net">HttpClient and HttpClientHandler</see>.
        /// </summary>
        /// <param name="configuration">An instance of Configuration.</param>
        /// <exception cref="ArgumentNullException"></exception>
        /// <returns></returns>
        public TemplatesApi(ImzalaApiClient.Client.Configuration configuration)
        {
            if (configuration == null) throw new ArgumentNullException("configuration");

            this.Configuration = ImzalaApiClient.Client.Configuration.MergeConfigurations(
                ImzalaApiClient.Client.GlobalConfiguration.Instance,
                configuration
            );
            this.ApiClient = new ImzalaApiClient.Client.ApiClient(this.Configuration.BasePath);
            this.Client = this.ApiClient;
            this.AsynchronousClient = this.ApiClient;
            ExceptionFactory = ImzalaApiClient.Client.Configuration.DefaultExceptionFactory;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="TemplatesApi"/> class.
        /// </summary>
        /// <param name="client">An instance of HttpClient.</param>
        /// <param name="handler">An optional instance of HttpClientHandler that is used by HttpClient.</param>
        /// <exception cref="ArgumentNullException"></exception>
        /// <returns></returns>
        /// <remarks>
        /// Some configuration settings will not be applied without passing an HttpClientHandler.
        /// The features affected are: Setting and Retrieving Cookies, Client Certificates, Proxy settings.
        /// </remarks>
        public TemplatesApi(HttpClient client, HttpClientHandler handler = null) : this(client, (string)null, handler)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="TemplatesApi"/> class.
        /// </summary>
        /// <param name="client">An instance of HttpClient.</param>
        /// <param name="basePath">The target service's base path in URL format.</param>
        /// <param name="handler">An optional instance of HttpClientHandler that is used by HttpClient.</param>
        /// <exception cref="ArgumentNullException"></exception>
        /// <exception cref="ArgumentException"></exception>
        /// <returns></returns>
        /// <remarks>
        /// Some configuration settings will not be applied without passing an HttpClientHandler.
        /// The features affected are: Setting and Retrieving Cookies, Client Certificates, Proxy settings.
        /// </remarks>
        public TemplatesApi(HttpClient client, string basePath, HttpClientHandler handler = null)
        {
            if (client == null) throw new ArgumentNullException("client");

            this.Configuration = ImzalaApiClient.Client.Configuration.MergeConfigurations(
                ImzalaApiClient.Client.GlobalConfiguration.Instance,
                new ImzalaApiClient.Client.Configuration { BasePath = basePath }
            );
            this.ApiClient = new ImzalaApiClient.Client.ApiClient(client, this.Configuration.BasePath, handler);
            this.Client =  this.ApiClient;
            this.AsynchronousClient = this.ApiClient;
            this.ExceptionFactory = ImzalaApiClient.Client.Configuration.DefaultExceptionFactory;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="TemplatesApi"/> class using Configuration object.
        /// </summary>
        /// <param name="client">An instance of HttpClient.</param>
        /// <param name="configuration">An instance of Configuration.</param>
        /// <param name="handler">An optional instance of HttpClientHandler that is used by HttpClient.</param>
        /// <exception cref="ArgumentNullException"></exception>
        /// <returns></returns>
        /// <remarks>
        /// Some configuration settings will not be applied without passing an HttpClientHandler.
        /// The features affected are: Setting and Retrieving Cookies, Client Certificates, Proxy settings.
        /// </remarks>
        public TemplatesApi(HttpClient client, ImzalaApiClient.Client.Configuration configuration, HttpClientHandler handler = null)
        {
            if (configuration == null) throw new ArgumentNullException("configuration");
            if (client == null) throw new ArgumentNullException("client");

            this.Configuration = ImzalaApiClient.Client.Configuration.MergeConfigurations(
                ImzalaApiClient.Client.GlobalConfiguration.Instance,
                configuration
            );
            this.ApiClient = new ImzalaApiClient.Client.ApiClient(client, this.Configuration.BasePath, handler);
            this.Client = this.ApiClient;
            this.AsynchronousClient = this.ApiClient;
            ExceptionFactory = ImzalaApiClient.Client.Configuration.DefaultExceptionFactory;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="TemplatesApi"/> class
        /// using a Configuration object and client instance.
        /// </summary>
        /// <param name="client">The client interface for synchronous API access.</param>
        /// <param name="asyncClient">The client interface for asynchronous API access.</param>
        /// <param name="configuration">The configuration object.</param>
        /// <exception cref="ArgumentNullException"></exception>
        public TemplatesApi(ImzalaApiClient.Client.ISynchronousClient client, ImzalaApiClient.Client.IAsynchronousClient asyncClient, ImzalaApiClient.Client.IReadableConfiguration configuration)
        {
            if (client == null) throw new ArgumentNullException("client");
            if (asyncClient == null) throw new ArgumentNullException("asyncClient");
            if (configuration == null) throw new ArgumentNullException("configuration");

            this.Client = client;
            this.AsynchronousClient = asyncClient;
            this.Configuration = configuration;
            this.ExceptionFactory = ImzalaApiClient.Client.Configuration.DefaultExceptionFactory;
        }

        /// <summary>
        /// Disposes resources if they were created by us
        /// </summary>
        public void Dispose()
        {
            this.ApiClient?.Dispose();
        }

        /// <summary>
        /// Holds the ApiClient if created
        /// </summary>
        public ImzalaApiClient.Client.ApiClient ApiClient { get; set; } = null;

        /// <summary>
        /// The client for accessing this underlying API asynchronously.
        /// </summary>
        public ImzalaApiClient.Client.IAsynchronousClient AsynchronousClient { get; set; }

        /// <summary>
        /// The client for accessing this underlying API synchronously.
        /// </summary>
        public ImzalaApiClient.Client.ISynchronousClient Client { get; set; }

        /// <summary>
        /// Gets the base path of the API client.
        /// </summary>
        /// <value>The base path</value>
        public string GetBasePath()
        {
            return this.Configuration.BasePath;
        }

        /// <summary>
        /// Gets or sets the configuration object
        /// </summary>
        /// <value>An instance of the Configuration</value>
        public ImzalaApiClient.Client.IReadableConfiguration Configuration { get; set; }

        /// <summary>
        /// Provides a factory method hook for the creation of exceptions.
        /// </summary>
        public ImzalaApiClient.Client.ExceptionFactory ExceptionFactory
        {
            get
            {
                if (_exceptionFactory != null && _exceptionFactory.GetInvocationList().Length > 1)
                {
                    throw new InvalidOperationException("Multicast delegate for ExceptionFactory is unsupported.");
                }
                return _exceptionFactory;
            }
            set { _exceptionFactory = value; }
        }

        /// <summary>
        /// Şablon listesi Aktif şablonlarınızı listeler.
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 20)</param>
        /// <returns>ApiV1TemplatesGet200Response</returns>
        public ApiV1TemplatesGet200Response ApiV1TemplatesGet(int? page = default, int? limit = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesGet200Response> localVarResponse = ApiV1TemplatesGetWithHttpInfo(page, limit);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Şablon listesi Aktif şablonlarınızı listeler.
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 20)</param>
        /// <returns>ApiResponse of ApiV1TemplatesGet200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesGet200Response> ApiV1TemplatesGetWithHttpInfo(int? page = default, int? limit = default)
        {
            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/json"
            };

            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            if (page != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "page", page));
            }
            if (limit != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "limit", limit));
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Get<ApiV1TemplatesGet200Response>("/api/v1/templates", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1TemplatesGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Şablon listesi Aktif şablonlarınızı listeler.
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 20)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TemplatesGet200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1TemplatesGet200Response> ApiV1TemplatesGetAsync(int? page = default, int? limit = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesGet200Response> localVarResponse = await ApiV1TemplatesGetWithHttpInfoAsync(page, limit, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Şablon listesi Aktif şablonlarınızı listeler.
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 20)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TemplatesGet200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesGet200Response>> ApiV1TemplatesGetWithHttpInfoAsync(int? page = default, int? limit = default, System.Threading.CancellationToken cancellationToken = default)
        {

            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/json"
            };


            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            if (page != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "page", page));
            }
            if (limit != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "limit", limit));
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.GetAsync<ApiV1TemplatesGet200Response>("/api/v1/templates", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1TemplatesGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Şablon detay Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiV1TemplatesIdGet200Response</returns>
        public ApiV1TemplatesIdGet200Response ApiV1TemplatesIdGet(Guid id)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdGet200Response> localVarResponse = ApiV1TemplatesIdGetWithHttpInfo(id);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Şablon detay Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of ApiV1TemplatesIdGet200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdGet200Response> ApiV1TemplatesIdGetWithHttpInfo(Guid id)
        {
            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/json"
            };

            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Get<ApiV1TemplatesIdGet200Response>("/api/v1/templates/{id}", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1TemplatesIdGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Şablon detay Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TemplatesIdGet200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1TemplatesIdGet200Response> ApiV1TemplatesIdGetAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdGet200Response> localVarResponse = await ApiV1TemplatesIdGetWithHttpInfoAsync(id, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Şablon detay Şablonun parties + variables bilgisini döner. variables array&#39;ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TemplatesIdGet200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdGet200Response>> ApiV1TemplatesIdGetWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
        {

            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/json"
            };


            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.GetAsync<ApiV1TemplatesIdGet200Response>("/api/v1/templates/{id}", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1TemplatesIdGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Şablon kullanım kılavuzu (curl + JSON örnek) Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiV1TemplatesIdUsageGet200Response</returns>
        public ApiV1TemplatesIdUsageGet200Response ApiV1TemplatesIdUsageGet(Guid id)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdUsageGet200Response> localVarResponse = ApiV1TemplatesIdUsageGetWithHttpInfo(id);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Şablon kullanım kılavuzu (curl + JSON örnek) Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of ApiV1TemplatesIdUsageGet200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdUsageGet200Response> ApiV1TemplatesIdUsageGetWithHttpInfo(Guid id)
        {
            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/json"
            };

            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Get<ApiV1TemplatesIdUsageGet200Response>("/api/v1/templates/{id}/usage", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1TemplatesIdUsageGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Şablon kullanım kılavuzu (curl + JSON örnek) Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TemplatesIdUsageGet200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1TemplatesIdUsageGet200Response> ApiV1TemplatesIdUsageGetAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdUsageGet200Response> localVarResponse = await ApiV1TemplatesIdUsageGetWithHttpInfoAsync(id, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Şablon kullanım kılavuzu (curl + JSON örnek) Bu şablonu API üzerinden çağırmak için tam rehber döner: - &#x60;endpoint&#x60; (POST URL&#39;i) - &#x60;required_headers&#x60; (X-API-Key, X-Workspace-Id, Content-Type) - &#x60;parties&#x60; (her partinin desteklediği field listesi) - &#x60;variables&#x60; (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - &#x60;example_request&#x60; (tam curl + JSON örneği, gerçek slug&#39;larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug&#39;larla doludur, root &#x60;variables&#x60; partisiz field&#39;lar için. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TemplatesIdUsageGet200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdUsageGet200Response>> ApiV1TemplatesIdUsageGetWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
        {

            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/json"
            };


            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.GetAsync<ApiV1TemplatesIdUsageGet200Response>("/api/v1/templates/{id}/usage", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1TemplatesIdUsageGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

    }
}
