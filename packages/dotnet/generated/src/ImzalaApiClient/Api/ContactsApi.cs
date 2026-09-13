/*
 * imzala External API
 *
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.8.5 · **Son güncelleme:** 2026-09-10  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API key kullanıyorsanız `X-Workspace-Id` header'ı göndermeniz gerekir (organizasyon UUID'si). Kişisel anahtarlar için bu header gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |- -- -|- -- -- --|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (8) | Olay | Tetikleyici | |- -- -- -|- -- -- -- -- -- --| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 8 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); - - INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.8.5
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
    public interface IContactsApiSync : IApiAccessor
    {
        #region Synchronous Operations
        /// <summary>
        /// Kişi listesi
        /// </summary>
        /// <remarks>
        /// API key sahibinin workspace&#39;indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org&#39;un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | updatedAt | first_name | last_name | email (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <param name="companyId"> (optional)</param>
        /// <param name="archived">true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) (optional, default to false)</param>
        /// <returns>ApiV1ContactsGet200Response</returns>
        ApiV1ContactsGet200Response ApiV1ContactsGet(int? page = default, int? limit = default, string? q = default, string? sort = default, Guid? companyId = default, bool? archived = default);

        /// <summary>
        /// Kişi listesi
        /// </summary>
        /// <remarks>
        /// API key sahibinin workspace&#39;indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org&#39;un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | updatedAt | first_name | last_name | email (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <param name="companyId"> (optional)</param>
        /// <param name="archived">true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) (optional, default to false)</param>
        /// <returns>ApiResponse of ApiV1ContactsGet200Response</returns>
        ApiResponse<ApiV1ContactsGet200Response> ApiV1ContactsGetWithHttpInfo(int? page = default, int? limit = default, string? q = default, string? sort = default, Guid? companyId = default, bool? archived = default);
        /// <summary>
        /// Kişi oluştur
        /// </summary>
        /// <remarks>
        /// Workspace&#39;e yeni bir kişi ekler. Aynı workspace&#39;te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1ContactsPostRequest"></param>
        /// <returns>ApiV1ContactsPost201Response</returns>
        ApiV1ContactsPost201Response ApiV1ContactsPost(ApiV1ContactsPostRequest apiV1ContactsPostRequest);

        /// <summary>
        /// Kişi oluştur
        /// </summary>
        /// <remarks>
        /// Workspace&#39;e yeni bir kişi ekler. Aynı workspace&#39;te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1ContactsPostRequest"></param>
        /// <returns>ApiResponse of ApiV1ContactsPost201Response</returns>
        ApiResponse<ApiV1ContactsPost201Response> ApiV1ContactsPostWithHttpInfo(ApiV1ContactsPostRequest apiV1ContactsPostRequest);
        #endregion Synchronous Operations
    }

    /// <summary>
    /// Represents a collection of functions to interact with the API endpoints
    /// </summary>
    public interface IContactsApiAsync : IApiAccessor
    {
        #region Asynchronous Operations
        /// <summary>
        /// Kişi listesi
        /// </summary>
        /// <remarks>
        /// API key sahibinin workspace&#39;indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org&#39;un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | updatedAt | first_name | last_name | email (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <param name="companyId"> (optional)</param>
        /// <param name="archived">true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) (optional, default to false)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1ContactsGet200Response</returns>
        System.Threading.Tasks.Task<ApiV1ContactsGet200Response> ApiV1ContactsGetAsync(int? page = default, int? limit = default, string? q = default, string? sort = default, Guid? companyId = default, bool? archived = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Kişi listesi
        /// </summary>
        /// <remarks>
        /// API key sahibinin workspace&#39;indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org&#39;un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | updatedAt | first_name | last_name | email (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <param name="companyId"> (optional)</param>
        /// <param name="archived">true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) (optional, default to false)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1ContactsGet200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1ContactsGet200Response>> ApiV1ContactsGetWithHttpInfoAsync(int? page = default, int? limit = default, string? q = default, string? sort = default, Guid? companyId = default, bool? archived = default, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Kişi oluştur
        /// </summary>
        /// <remarks>
        /// Workspace&#39;e yeni bir kişi ekler. Aynı workspace&#39;te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1ContactsPostRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1ContactsPost201Response</returns>
        System.Threading.Tasks.Task<ApiV1ContactsPost201Response> ApiV1ContactsPostAsync(ApiV1ContactsPostRequest apiV1ContactsPostRequest, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Kişi oluştur
        /// </summary>
        /// <remarks>
        /// Workspace&#39;e yeni bir kişi ekler. Aynı workspace&#39;te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1ContactsPostRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1ContactsPost201Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1ContactsPost201Response>> ApiV1ContactsPostWithHttpInfoAsync(ApiV1ContactsPostRequest apiV1ContactsPostRequest, System.Threading.CancellationToken cancellationToken = default);
        #endregion Asynchronous Operations
    }

    /// <summary>
    /// Represents a collection of functions to interact with the API endpoints
    /// </summary>
    public interface IContactsApi : IContactsApiSync, IContactsApiAsync
    {

    }

    /// <summary>
    /// Represents a collection of functions to interact with the API endpoints
    /// </summary>
    public partial class ContactsApi : IDisposable, IContactsApi
    {
        private ImzalaApiClient.Client.ExceptionFactory _exceptionFactory = (name, response) => null;

        /// <summary>
        /// Initializes a new instance of the <see cref="ContactsApi"/> class.
        /// **IMPORTANT** This will also create an instance of HttpClient, which is less than ideal.
        /// It's better to reuse the <see href="https://docs.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests#issues-with-the-original-httpclient-class-available-in-net">HttpClient and HttpClientHandler</see>.
        /// </summary>
        /// <returns></returns>
        public ContactsApi() : this((string)null)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ContactsApi"/> class.
        /// **IMPORTANT** This will also create an instance of HttpClient, which is less than ideal.
        /// It's better to reuse the <see href="https://docs.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests#issues-with-the-original-httpclient-class-available-in-net">HttpClient and HttpClientHandler</see>.
        /// </summary>
        /// <param name="basePath">The target service's base path in URL format.</param>
        /// <exception cref="ArgumentException"></exception>
        /// <returns></returns>
        public ContactsApi(string basePath)
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
        /// Initializes a new instance of the <see cref="ContactsApi"/> class using Configuration object.
        /// **IMPORTANT** This will also create an instance of HttpClient, which is less than ideal.
        /// It's better to reuse the <see href="https://docs.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests#issues-with-the-original-httpclient-class-available-in-net">HttpClient and HttpClientHandler</see>.
        /// </summary>
        /// <param name="configuration">An instance of Configuration.</param>
        /// <exception cref="ArgumentNullException"></exception>
        /// <returns></returns>
        public ContactsApi(ImzalaApiClient.Client.Configuration configuration)
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
        /// Initializes a new instance of the <see cref="ContactsApi"/> class.
        /// </summary>
        /// <param name="client">An instance of HttpClient.</param>
        /// <param name="handler">An optional instance of HttpClientHandler that is used by HttpClient.</param>
        /// <exception cref="ArgumentNullException"></exception>
        /// <returns></returns>
        /// <remarks>
        /// Some configuration settings will not be applied without passing an HttpClientHandler.
        /// The features affected are: Setting and Retrieving Cookies, Client Certificates, Proxy settings.
        /// </remarks>
        public ContactsApi(HttpClient client, HttpClientHandler handler = null) : this(client, (string)null, handler)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ContactsApi"/> class.
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
        public ContactsApi(HttpClient client, string basePath, HttpClientHandler handler = null)
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
        /// Initializes a new instance of the <see cref="ContactsApi"/> class using Configuration object.
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
        public ContactsApi(HttpClient client, ImzalaApiClient.Client.Configuration configuration, HttpClientHandler handler = null)
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
        /// Initializes a new instance of the <see cref="ContactsApi"/> class
        /// using a Configuration object and client instance.
        /// </summary>
        /// <param name="client">The client interface for synchronous API access.</param>
        /// <param name="asyncClient">The client interface for asynchronous API access.</param>
        /// <param name="configuration">The configuration object.</param>
        /// <exception cref="ArgumentNullException"></exception>
        public ContactsApi(ImzalaApiClient.Client.ISynchronousClient client, ImzalaApiClient.Client.IAsynchronousClient asyncClient, ImzalaApiClient.Client.IReadableConfiguration configuration)
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
        /// Kişi listesi API key sahibinin workspace&#39;indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org&#39;un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | updatedAt | first_name | last_name | email (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <param name="companyId"> (optional)</param>
        /// <param name="archived">true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) (optional, default to false)</param>
        /// <returns>ApiV1ContactsGet200Response</returns>
        public ApiV1ContactsGet200Response ApiV1ContactsGet(int? page = default, int? limit = default, string? q = default, string? sort = default, Guid? companyId = default, bool? archived = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1ContactsGet200Response> localVarResponse = ApiV1ContactsGetWithHttpInfo(page, limit, q, sort, companyId, archived);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Kişi listesi API key sahibinin workspace&#39;indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org&#39;un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | updatedAt | first_name | last_name | email (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <param name="companyId"> (optional)</param>
        /// <param name="archived">true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) (optional, default to false)</param>
        /// <returns>ApiResponse of ApiV1ContactsGet200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1ContactsGet200Response> ApiV1ContactsGetWithHttpInfo(int? page = default, int? limit = default, string? q = default, string? sort = default, Guid? companyId = default, bool? archived = default)
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
            if (q != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "q", q));
            }
            if (sort != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "sort", sort));
            }
            if (companyId != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "company_id", companyId));
            }
            if (archived != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "archived", archived));
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Get<ApiV1ContactsGet200Response>("/api/v1/contacts", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1ContactsGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Kişi listesi API key sahibinin workspace&#39;indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org&#39;un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | updatedAt | first_name | last_name | email (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <param name="companyId"> (optional)</param>
        /// <param name="archived">true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) (optional, default to false)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1ContactsGet200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1ContactsGet200Response> ApiV1ContactsGetAsync(int? page = default, int? limit = default, string? q = default, string? sort = default, Guid? companyId = default, bool? archived = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1ContactsGet200Response> localVarResponse = await ApiV1ContactsGetWithHttpInfoAsync(page, limit, q, sort, companyId, archived, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Kişi listesi API key sahibinin workspace&#39;indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org&#39;un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | updatedAt | first_name | last_name | email (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <param name="companyId"> (optional)</param>
        /// <param name="archived">true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif) (optional, default to false)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1ContactsGet200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1ContactsGet200Response>> ApiV1ContactsGetWithHttpInfoAsync(int? page = default, int? limit = default, string? q = default, string? sort = default, Guid? companyId = default, bool? archived = default, System.Threading.CancellationToken cancellationToken = default)
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
            if (q != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "q", q));
            }
            if (sort != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "sort", sort));
            }
            if (companyId != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "company_id", companyId));
            }
            if (archived != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "archived", archived));
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.GetAsync<ApiV1ContactsGet200Response>("/api/v1/contacts", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1ContactsGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Kişi oluştur Workspace&#39;e yeni bir kişi ekler. Aynı workspace&#39;te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1ContactsPostRequest"></param>
        /// <returns>ApiV1ContactsPost201Response</returns>
        public ApiV1ContactsPost201Response ApiV1ContactsPost(ApiV1ContactsPostRequest apiV1ContactsPostRequest)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1ContactsPost201Response> localVarResponse = ApiV1ContactsPostWithHttpInfo(apiV1ContactsPostRequest);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Kişi oluştur Workspace&#39;e yeni bir kişi ekler. Aynı workspace&#39;te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1ContactsPostRequest"></param>
        /// <returns>ApiResponse of ApiV1ContactsPost201Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1ContactsPost201Response> ApiV1ContactsPostWithHttpInfo(ApiV1ContactsPostRequest apiV1ContactsPostRequest)
        {
            // verify the required parameter 'apiV1ContactsPostRequest' is set
            if (apiV1ContactsPostRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1ContactsPostRequest' when calling ContactsApi->ApiV1ContactsPost");

            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
                "application/json"
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/json"
            };

            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            localVarRequestOptions.Data = apiV1ContactsPostRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Post<ApiV1ContactsPost201Response>("/api/v1/contacts", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1ContactsPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Kişi oluştur Workspace&#39;e yeni bir kişi ekler. Aynı workspace&#39;te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1ContactsPostRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1ContactsPost201Response</returns>
        public async System.Threading.Tasks.Task<ApiV1ContactsPost201Response> ApiV1ContactsPostAsync(ApiV1ContactsPostRequest apiV1ContactsPostRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1ContactsPost201Response> localVarResponse = await ApiV1ContactsPostWithHttpInfoAsync(apiV1ContactsPostRequest, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Kişi oluştur Workspace&#39;e yeni bir kişi ekler. Aynı workspace&#39;te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1ContactsPostRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1ContactsPost201Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1ContactsPost201Response>> ApiV1ContactsPostWithHttpInfoAsync(ApiV1ContactsPostRequest apiV1ContactsPostRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            // verify the required parameter 'apiV1ContactsPostRequest' is set
            if (apiV1ContactsPostRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1ContactsPostRequest' when calling ContactsApi->ApiV1ContactsPost");


            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
                "application/json"
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/json"
            };


            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            localVarRequestOptions.Data = apiV1ContactsPostRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PostAsync<ApiV1ContactsPost201Response>("/api/v1/contacts", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1ContactsPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

    }
}
