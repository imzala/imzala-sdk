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
    public interface ITimestampsApiSync : IApiAccessor
    {
        #region Synchronous Operations
        /// <summary>
        /// Zaman damgası listesi
        /// </summary>
        /// <remarks>
        /// Hesabın (workspace) manuel oluşturduğu zaman damgalarını listeler. Sözleşme tamamlanınca otomatik üretilen damgalar bu listede GÖSTERİLMEZ (onlar ilgili sözleşme sayfasından indirilir). Yalnızca API key&#39;in kendi workspace&#39;indeki kayıtlar döner (IDOR-safe).  &#x60;timestamp_file_url&#x60; doğrudan tarayıcıdan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı / şifreli olabilir). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Dosya adı / açıklamada arama (max 100 karakter) (optional)</param>
        /// <param name="status">Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional)</param>
        /// <param name="from">Damga günü alt sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="to">Damga günü üst sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <returns>ApiV1TimestampsGet200Response</returns>
        ApiV1TimestampsGet200Response ApiV1TimestampsGet(int? page = default, int? limit = default, string? q = default, string? status = default, DateOnly? from = default, DateOnly? to = default, string? sort = default);

        /// <summary>
        /// Zaman damgası listesi
        /// </summary>
        /// <remarks>
        /// Hesabın (workspace) manuel oluşturduğu zaman damgalarını listeler. Sözleşme tamamlanınca otomatik üretilen damgalar bu listede GÖSTERİLMEZ (onlar ilgili sözleşme sayfasından indirilir). Yalnızca API key&#39;in kendi workspace&#39;indeki kayıtlar döner (IDOR-safe).  &#x60;timestamp_file_url&#x60; doğrudan tarayıcıdan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı / şifreli olabilir). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Dosya adı / açıklamada arama (max 100 karakter) (optional)</param>
        /// <param name="status">Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional)</param>
        /// <param name="from">Damga günü alt sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="to">Damga günü üst sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <returns>ApiResponse of ApiV1TimestampsGet200Response</returns>
        ApiResponse<ApiV1TimestampsGet200Response> ApiV1TimestampsGetWithHttpInfo(int? page = default, int? limit = default, string? q = default, string? status = default, DateOnly? from = default, DateOnly? to = default, string? sort = default);
        /// <summary>
        /// Zaman damgası detayı
        /// </summary>
        /// <remarks>
        /// Tek bir zaman damgası kaydının detayını döner. Yalnızca API key&#39;in kendi workspace&#39;indeki kayıt getirilebilir (IDOR-safe); başka bir workspace&#39;in kaydı 404 döner. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiV1TimestampsIdGet200Response</returns>
        ApiV1TimestampsIdGet200Response ApiV1TimestampsIdGet(Guid id);

        /// <summary>
        /// Zaman damgası detayı
        /// </summary>
        /// <remarks>
        /// Tek bir zaman damgası kaydının detayını döner. Yalnızca API key&#39;in kendi workspace&#39;indeki kayıt getirilebilir (IDOR-safe); başka bir workspace&#39;in kaydı 404 döner. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of ApiV1TimestampsIdGet200Response</returns>
        ApiResponse<ApiV1TimestampsIdGet200Response> ApiV1TimestampsIdGetWithHttpInfo(Guid id);
        /// <summary>
        /// Zaman damgası oluştur (eser tescil)
        /// </summary>
        /// <remarks>
        /// Dosyanın SHA-256 hash&#39;ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - &#x60;owner_first_name&#x60; / &#x60;owner_last_name&#x60; alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Güvenli elektronik imza için ayrı imzalama akışını kullanın.  **İdempotency:** &#x60;Idempotency-Key&#x60; header&#39;ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı &#x60;id&#x60; döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - &#x60;multipart/form-data&#x60;: &#x60;file&#x60; alanıyla ikili dosya yükleme - &#x60;application/json&#x60;: &#x60;file_base64&#x60; alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="file">Damgalanacak dosya (maks. 50 MB)</param>
        /// <param name="idempotencyKey">Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  (optional)</param>
        /// <param name="description">Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)</param>
        /// <param name="ownerFirstName">Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <param name="ownerLastName">Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <returns>ApiV1TimestampsPost201Response</returns>
        ApiV1TimestampsPost201Response ApiV1TimestampsPost(FileParameter file, string? idempotencyKey = default, string? description = default, string? ownerFirstName = default, string? ownerLastName = default);

        /// <summary>
        /// Zaman damgası oluştur (eser tescil)
        /// </summary>
        /// <remarks>
        /// Dosyanın SHA-256 hash&#39;ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - &#x60;owner_first_name&#x60; / &#x60;owner_last_name&#x60; alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Güvenli elektronik imza için ayrı imzalama akışını kullanın.  **İdempotency:** &#x60;Idempotency-Key&#x60; header&#39;ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı &#x60;id&#x60; döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - &#x60;multipart/form-data&#x60;: &#x60;file&#x60; alanıyla ikili dosya yükleme - &#x60;application/json&#x60;: &#x60;file_base64&#x60; alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="file">Damgalanacak dosya (maks. 50 MB)</param>
        /// <param name="idempotencyKey">Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  (optional)</param>
        /// <param name="description">Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)</param>
        /// <param name="ownerFirstName">Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <param name="ownerLastName">Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <returns>ApiResponse of ApiV1TimestampsPost201Response</returns>
        ApiResponse<ApiV1TimestampsPost201Response> ApiV1TimestampsPostWithHttpInfo(FileParameter file, string? idempotencyKey = default, string? description = default, string? ownerFirstName = default, string? ownerLastName = default);
        #endregion Synchronous Operations
    }

    /// <summary>
    /// Represents a collection of functions to interact with the API endpoints
    /// </summary>
    public interface ITimestampsApiAsync : IApiAccessor
    {
        #region Asynchronous Operations
        /// <summary>
        /// Zaman damgası listesi
        /// </summary>
        /// <remarks>
        /// Hesabın (workspace) manuel oluşturduğu zaman damgalarını listeler. Sözleşme tamamlanınca otomatik üretilen damgalar bu listede GÖSTERİLMEZ (onlar ilgili sözleşme sayfasından indirilir). Yalnızca API key&#39;in kendi workspace&#39;indeki kayıtlar döner (IDOR-safe).  &#x60;timestamp_file_url&#x60; doğrudan tarayıcıdan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı / şifreli olabilir). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Dosya adı / açıklamada arama (max 100 karakter) (optional)</param>
        /// <param name="status">Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional)</param>
        /// <param name="from">Damga günü alt sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="to">Damga günü üst sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TimestampsGet200Response</returns>
        System.Threading.Tasks.Task<ApiV1TimestampsGet200Response> ApiV1TimestampsGetAsync(int? page = default, int? limit = default, string? q = default, string? status = default, DateOnly? from = default, DateOnly? to = default, string? sort = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Zaman damgası listesi
        /// </summary>
        /// <remarks>
        /// Hesabın (workspace) manuel oluşturduğu zaman damgalarını listeler. Sözleşme tamamlanınca otomatik üretilen damgalar bu listede GÖSTERİLMEZ (onlar ilgili sözleşme sayfasından indirilir). Yalnızca API key&#39;in kendi workspace&#39;indeki kayıtlar döner (IDOR-safe).  &#x60;timestamp_file_url&#x60; doğrudan tarayıcıdan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı / şifreli olabilir). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Dosya adı / açıklamada arama (max 100 karakter) (optional)</param>
        /// <param name="status">Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional)</param>
        /// <param name="from">Damga günü alt sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="to">Damga günü üst sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TimestampsGet200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1TimestampsGet200Response>> ApiV1TimestampsGetWithHttpInfoAsync(int? page = default, int? limit = default, string? q = default, string? status = default, DateOnly? from = default, DateOnly? to = default, string? sort = default, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Zaman damgası detayı
        /// </summary>
        /// <remarks>
        /// Tek bir zaman damgası kaydının detayını döner. Yalnızca API key&#39;in kendi workspace&#39;indeki kayıt getirilebilir (IDOR-safe); başka bir workspace&#39;in kaydı 404 döner. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TimestampsIdGet200Response</returns>
        System.Threading.Tasks.Task<ApiV1TimestampsIdGet200Response> ApiV1TimestampsIdGetAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Zaman damgası detayı
        /// </summary>
        /// <remarks>
        /// Tek bir zaman damgası kaydının detayını döner. Yalnızca API key&#39;in kendi workspace&#39;indeki kayıt getirilebilir (IDOR-safe); başka bir workspace&#39;in kaydı 404 döner. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TimestampsIdGet200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1TimestampsIdGet200Response>> ApiV1TimestampsIdGetWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Zaman damgası oluştur (eser tescil)
        /// </summary>
        /// <remarks>
        /// Dosyanın SHA-256 hash&#39;ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - &#x60;owner_first_name&#x60; / &#x60;owner_last_name&#x60; alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Güvenli elektronik imza için ayrı imzalama akışını kullanın.  **İdempotency:** &#x60;Idempotency-Key&#x60; header&#39;ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı &#x60;id&#x60; döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - &#x60;multipart/form-data&#x60;: &#x60;file&#x60; alanıyla ikili dosya yükleme - &#x60;application/json&#x60;: &#x60;file_base64&#x60; alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="file">Damgalanacak dosya (maks. 50 MB)</param>
        /// <param name="idempotencyKey">Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  (optional)</param>
        /// <param name="description">Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)</param>
        /// <param name="ownerFirstName">Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <param name="ownerLastName">Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TimestampsPost201Response</returns>
        System.Threading.Tasks.Task<ApiV1TimestampsPost201Response> ApiV1TimestampsPostAsync(FileParameter file, string? idempotencyKey = default, string? description = default, string? ownerFirstName = default, string? ownerLastName = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Zaman damgası oluştur (eser tescil)
        /// </summary>
        /// <remarks>
        /// Dosyanın SHA-256 hash&#39;ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - &#x60;owner_first_name&#x60; / &#x60;owner_last_name&#x60; alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Güvenli elektronik imza için ayrı imzalama akışını kullanın.  **İdempotency:** &#x60;Idempotency-Key&#x60; header&#39;ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı &#x60;id&#x60; döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - &#x60;multipart/form-data&#x60;: &#x60;file&#x60; alanıyla ikili dosya yükleme - &#x60;application/json&#x60;: &#x60;file_base64&#x60; alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="file">Damgalanacak dosya (maks. 50 MB)</param>
        /// <param name="idempotencyKey">Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  (optional)</param>
        /// <param name="description">Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)</param>
        /// <param name="ownerFirstName">Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <param name="ownerLastName">Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TimestampsPost201Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1TimestampsPost201Response>> ApiV1TimestampsPostWithHttpInfoAsync(FileParameter file, string? idempotencyKey = default, string? description = default, string? ownerFirstName = default, string? ownerLastName = default, System.Threading.CancellationToken cancellationToken = default);
        #endregion Asynchronous Operations
    }

    /// <summary>
    /// Represents a collection of functions to interact with the API endpoints
    /// </summary>
    public interface ITimestampsApi : ITimestampsApiSync, ITimestampsApiAsync
    {

    }

    /// <summary>
    /// Represents a collection of functions to interact with the API endpoints
    /// </summary>
    public partial class TimestampsApi : IDisposable, ITimestampsApi
    {
        private ImzalaApiClient.Client.ExceptionFactory _exceptionFactory = (name, response) => null;

        /// <summary>
        /// Initializes a new instance of the <see cref="TimestampsApi"/> class.
        /// **IMPORTANT** This will also create an instance of HttpClient, which is less than ideal.
        /// It's better to reuse the <see href="https://docs.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests#issues-with-the-original-httpclient-class-available-in-net">HttpClient and HttpClientHandler</see>.
        /// </summary>
        /// <returns></returns>
        public TimestampsApi() : this((string)null)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="TimestampsApi"/> class.
        /// **IMPORTANT** This will also create an instance of HttpClient, which is less than ideal.
        /// It's better to reuse the <see href="https://docs.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests#issues-with-the-original-httpclient-class-available-in-net">HttpClient and HttpClientHandler</see>.
        /// </summary>
        /// <param name="basePath">The target service's base path in URL format.</param>
        /// <exception cref="ArgumentException"></exception>
        /// <returns></returns>
        public TimestampsApi(string basePath)
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
        /// Initializes a new instance of the <see cref="TimestampsApi"/> class using Configuration object.
        /// **IMPORTANT** This will also create an instance of HttpClient, which is less than ideal.
        /// It's better to reuse the <see href="https://docs.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests#issues-with-the-original-httpclient-class-available-in-net">HttpClient and HttpClientHandler</see>.
        /// </summary>
        /// <param name="configuration">An instance of Configuration.</param>
        /// <exception cref="ArgumentNullException"></exception>
        /// <returns></returns>
        public TimestampsApi(ImzalaApiClient.Client.Configuration configuration)
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
        /// Initializes a new instance of the <see cref="TimestampsApi"/> class.
        /// </summary>
        /// <param name="client">An instance of HttpClient.</param>
        /// <param name="handler">An optional instance of HttpClientHandler that is used by HttpClient.</param>
        /// <exception cref="ArgumentNullException"></exception>
        /// <returns></returns>
        /// <remarks>
        /// Some configuration settings will not be applied without passing an HttpClientHandler.
        /// The features affected are: Setting and Retrieving Cookies, Client Certificates, Proxy settings.
        /// </remarks>
        public TimestampsApi(HttpClient client, HttpClientHandler handler = null) : this(client, (string)null, handler)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="TimestampsApi"/> class.
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
        public TimestampsApi(HttpClient client, string basePath, HttpClientHandler handler = null)
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
        /// Initializes a new instance of the <see cref="TimestampsApi"/> class using Configuration object.
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
        public TimestampsApi(HttpClient client, ImzalaApiClient.Client.Configuration configuration, HttpClientHandler handler = null)
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
        /// Initializes a new instance of the <see cref="TimestampsApi"/> class
        /// using a Configuration object and client instance.
        /// </summary>
        /// <param name="client">The client interface for synchronous API access.</param>
        /// <param name="asyncClient">The client interface for asynchronous API access.</param>
        /// <param name="configuration">The configuration object.</param>
        /// <exception cref="ArgumentNullException"></exception>
        public TimestampsApi(ImzalaApiClient.Client.ISynchronousClient client, ImzalaApiClient.Client.IAsynchronousClient asyncClient, ImzalaApiClient.Client.IReadableConfiguration configuration)
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
        /// Zaman damgası listesi Hesabın (workspace) manuel oluşturduğu zaman damgalarını listeler. Sözleşme tamamlanınca otomatik üretilen damgalar bu listede GÖSTERİLMEZ (onlar ilgili sözleşme sayfasından indirilir). Yalnızca API key&#39;in kendi workspace&#39;indeki kayıtlar döner (IDOR-safe).  &#x60;timestamp_file_url&#x60; doğrudan tarayıcıdan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı / şifreli olabilir). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Dosya adı / açıklamada arama (max 100 karakter) (optional)</param>
        /// <param name="status">Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional)</param>
        /// <param name="from">Damga günü alt sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="to">Damga günü üst sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <returns>ApiV1TimestampsGet200Response</returns>
        public ApiV1TimestampsGet200Response ApiV1TimestampsGet(int? page = default, int? limit = default, string? q = default, string? status = default, DateOnly? from = default, DateOnly? to = default, string? sort = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TimestampsGet200Response> localVarResponse = ApiV1TimestampsGetWithHttpInfo(page, limit, q, status, from, to, sort);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zaman damgası listesi Hesabın (workspace) manuel oluşturduğu zaman damgalarını listeler. Sözleşme tamamlanınca otomatik üretilen damgalar bu listede GÖSTERİLMEZ (onlar ilgili sözleşme sayfasından indirilir). Yalnızca API key&#39;in kendi workspace&#39;indeki kayıtlar döner (IDOR-safe).  &#x60;timestamp_file_url&#x60; doğrudan tarayıcıdan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı / şifreli olabilir). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Dosya adı / açıklamada arama (max 100 karakter) (optional)</param>
        /// <param name="status">Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional)</param>
        /// <param name="from">Damga günü alt sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="to">Damga günü üst sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <returns>ApiResponse of ApiV1TimestampsGet200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1TimestampsGet200Response> ApiV1TimestampsGetWithHttpInfo(int? page = default, int? limit = default, string? q = default, string? status = default, DateOnly? from = default, DateOnly? to = default, string? sort = default)
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
            if (status != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "status", status));
            }
            if (from != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "from", from));
            }
            if (to != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "to", to));
            }
            if (sort != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "sort", sort));
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Get<ApiV1TimestampsGet200Response>("/api/v1/timestamps", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1TimestampsGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zaman damgası listesi Hesabın (workspace) manuel oluşturduğu zaman damgalarını listeler. Sözleşme tamamlanınca otomatik üretilen damgalar bu listede GÖSTERİLMEZ (onlar ilgili sözleşme sayfasından indirilir). Yalnızca API key&#39;in kendi workspace&#39;indeki kayıtlar döner (IDOR-safe).  &#x60;timestamp_file_url&#x60; doğrudan tarayıcıdan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı / şifreli olabilir). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Dosya adı / açıklamada arama (max 100 karakter) (optional)</param>
        /// <param name="status">Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional)</param>
        /// <param name="from">Damga günü alt sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="to">Damga günü üst sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TimestampsGet200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1TimestampsGet200Response> ApiV1TimestampsGetAsync(int? page = default, int? limit = default, string? q = default, string? status = default, DateOnly? from = default, DateOnly? to = default, string? sort = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TimestampsGet200Response> localVarResponse = await ApiV1TimestampsGetWithHttpInfoAsync(page, limit, q, status, from, to, sort, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zaman damgası listesi Hesabın (workspace) manuel oluşturduğu zaman damgalarını listeler. Sözleşme tamamlanınca otomatik üretilen damgalar bu listede GÖSTERİLMEZ (onlar ilgili sözleşme sayfasından indirilir). Yalnızca API key&#39;in kendi workspace&#39;indeki kayıtlar döner (IDOR-safe).  &#x60;timestamp_file_url&#x60; doğrudan tarayıcıdan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı / şifreli olabilir). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit"> (optional, default to 25)</param>
        /// <param name="q">Dosya adı / açıklamada arama (max 100 karakter) (optional)</param>
        /// <param name="status">Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional)</param>
        /// <param name="from">Damga günü alt sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="to">Damga günü üst sınırı (YYYY-MM-DD) (optional)</param>
        /// <param name="sort">Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (&#39;-&#39; öneki azalan) (optional, default to &quot;-createdAt&quot;)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TimestampsGet200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1TimestampsGet200Response>> ApiV1TimestampsGetWithHttpInfoAsync(int? page = default, int? limit = default, string? q = default, string? status = default, DateOnly? from = default, DateOnly? to = default, string? sort = default, System.Threading.CancellationToken cancellationToken = default)
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
            if (status != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "status", status));
            }
            if (from != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "from", from));
            }
            if (to != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "to", to));
            }
            if (sort != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "sort", sort));
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.GetAsync<ApiV1TimestampsGet200Response>("/api/v1/timestamps", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1TimestampsGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zaman damgası detayı Tek bir zaman damgası kaydının detayını döner. Yalnızca API key&#39;in kendi workspace&#39;indeki kayıt getirilebilir (IDOR-safe); başka bir workspace&#39;in kaydı 404 döner. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiV1TimestampsIdGet200Response</returns>
        public ApiV1TimestampsIdGet200Response ApiV1TimestampsIdGet(Guid id)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TimestampsIdGet200Response> localVarResponse = ApiV1TimestampsIdGetWithHttpInfo(id);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zaman damgası detayı Tek bir zaman damgası kaydının detayını döner. Yalnızca API key&#39;in kendi workspace&#39;indeki kayıt getirilebilir (IDOR-safe); başka bir workspace&#39;in kaydı 404 döner. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of ApiV1TimestampsIdGet200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1TimestampsIdGet200Response> ApiV1TimestampsIdGetWithHttpInfo(Guid id)
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
            var localVarResponse = this.Client.Get<ApiV1TimestampsIdGet200Response>("/api/v1/timestamps/{id}", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1TimestampsIdGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zaman damgası detayı Tek bir zaman damgası kaydının detayını döner. Yalnızca API key&#39;in kendi workspace&#39;indeki kayıt getirilebilir (IDOR-safe); başka bir workspace&#39;in kaydı 404 döner. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TimestampsIdGet200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1TimestampsIdGet200Response> ApiV1TimestampsIdGetAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TimestampsIdGet200Response> localVarResponse = await ApiV1TimestampsIdGetWithHttpInfoAsync(id, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zaman damgası detayı Tek bir zaman damgası kaydının detayını döner. Yalnızca API key&#39;in kendi workspace&#39;indeki kayıt getirilebilir (IDOR-safe); başka bir workspace&#39;in kaydı 404 döner. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TimestampsIdGet200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1TimestampsIdGet200Response>> ApiV1TimestampsIdGetWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
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

            var localVarResponse = await this.AsynchronousClient.GetAsync<ApiV1TimestampsIdGet200Response>("/api/v1/timestamps/{id}", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1TimestampsIdGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zaman damgası oluştur (eser tescil) Dosyanın SHA-256 hash&#39;ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - &#x60;owner_first_name&#x60; / &#x60;owner_last_name&#x60; alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Güvenli elektronik imza için ayrı imzalama akışını kullanın.  **İdempotency:** &#x60;Idempotency-Key&#x60; header&#39;ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı &#x60;id&#x60; döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - &#x60;multipart/form-data&#x60;: &#x60;file&#x60; alanıyla ikili dosya yükleme - &#x60;application/json&#x60;: &#x60;file_base64&#x60; alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="file">Damgalanacak dosya (maks. 50 MB)</param>
        /// <param name="idempotencyKey">Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  (optional)</param>
        /// <param name="description">Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)</param>
        /// <param name="ownerFirstName">Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <param name="ownerLastName">Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <returns>ApiV1TimestampsPost201Response</returns>
        public ApiV1TimestampsPost201Response ApiV1TimestampsPost(FileParameter file, string? idempotencyKey = default, string? description = default, string? ownerFirstName = default, string? ownerLastName = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TimestampsPost201Response> localVarResponse = ApiV1TimestampsPostWithHttpInfo(file, idempotencyKey, description, ownerFirstName, ownerLastName);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zaman damgası oluştur (eser tescil) Dosyanın SHA-256 hash&#39;ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - &#x60;owner_first_name&#x60; / &#x60;owner_last_name&#x60; alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Güvenli elektronik imza için ayrı imzalama akışını kullanın.  **İdempotency:** &#x60;Idempotency-Key&#x60; header&#39;ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı &#x60;id&#x60; döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - &#x60;multipart/form-data&#x60;: &#x60;file&#x60; alanıyla ikili dosya yükleme - &#x60;application/json&#x60;: &#x60;file_base64&#x60; alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="file">Damgalanacak dosya (maks. 50 MB)</param>
        /// <param name="idempotencyKey">Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  (optional)</param>
        /// <param name="description">Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)</param>
        /// <param name="ownerFirstName">Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <param name="ownerLastName">Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <returns>ApiResponse of ApiV1TimestampsPost201Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1TimestampsPost201Response> ApiV1TimestampsPostWithHttpInfo(FileParameter file, string? idempotencyKey = default, string? description = default, string? ownerFirstName = default, string? ownerLastName = default)
        {
            // verify the required parameter 'file' is set
            if (file == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'file' when calling TimestampsApi->ApiV1TimestampsPost");

            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
                "multipart/form-data",
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/json"
            };

            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            if (idempotencyKey != null)
            {
                localVarRequestOptions.HeaderParameters.Add("Idempotency-Key", ImzalaApiClient.Client.ClientUtils.ParameterToString(idempotencyKey)); // header parameter
            }
            localVarRequestOptions.FileParameters.Add("file", file);
            if (description != null)
            {
                localVarRequestOptions.FormParameters.Add("description", ImzalaApiClient.Client.ClientUtils.ParameterToString(description)); // form parameter
            }
            if (ownerFirstName != null)
            {
                localVarRequestOptions.FormParameters.Add("owner_first_name", ImzalaApiClient.Client.ClientUtils.ParameterToString(ownerFirstName)); // form parameter
            }
            if (ownerLastName != null)
            {
                localVarRequestOptions.FormParameters.Add("owner_last_name", ImzalaApiClient.Client.ClientUtils.ParameterToString(ownerLastName)); // form parameter
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Post<ApiV1TimestampsPost201Response>("/api/v1/timestamps", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1TimestampsPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zaman damgası oluştur (eser tescil) Dosyanın SHA-256 hash&#39;ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - &#x60;owner_first_name&#x60; / &#x60;owner_last_name&#x60; alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Güvenli elektronik imza için ayrı imzalama akışını kullanın.  **İdempotency:** &#x60;Idempotency-Key&#x60; header&#39;ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı &#x60;id&#x60; döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - &#x60;multipart/form-data&#x60;: &#x60;file&#x60; alanıyla ikili dosya yükleme - &#x60;application/json&#x60;: &#x60;file_base64&#x60; alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="file">Damgalanacak dosya (maks. 50 MB)</param>
        /// <param name="idempotencyKey">Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  (optional)</param>
        /// <param name="description">Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)</param>
        /// <param name="ownerFirstName">Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <param name="ownerLastName">Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TimestampsPost201Response</returns>
        public async System.Threading.Tasks.Task<ApiV1TimestampsPost201Response> ApiV1TimestampsPostAsync(FileParameter file, string? idempotencyKey = default, string? description = default, string? ownerFirstName = default, string? ownerLastName = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TimestampsPost201Response> localVarResponse = await ApiV1TimestampsPostWithHttpInfoAsync(file, idempotencyKey, description, ownerFirstName, ownerLastName, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zaman damgası oluştur (eser tescil) Dosyanın SHA-256 hash&#39;ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - &#x60;owner_first_name&#x60; / &#x60;owner_last_name&#x60; alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Güvenli elektronik imza için ayrı imzalama akışını kullanın.  **İdempotency:** &#x60;Idempotency-Key&#x60; header&#39;ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı &#x60;id&#x60; döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - &#x60;multipart/form-data&#x60;: &#x60;file&#x60; alanıyla ikili dosya yükleme - &#x60;application/json&#x60;: &#x60;file_base64&#x60; alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="file">Damgalanacak dosya (maks. 50 MB)</param>
        /// <param name="idempotencyKey">Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez.  (optional)</param>
        /// <param name="description">Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)</param>
        /// <param name="ownerFirstName">Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <param name="ownerLastName">Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TimestampsPost201Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1TimestampsPost201Response>> ApiV1TimestampsPostWithHttpInfoAsync(FileParameter file, string? idempotencyKey = default, string? description = default, string? ownerFirstName = default, string? ownerLastName = default, System.Threading.CancellationToken cancellationToken = default)
        {
            // verify the required parameter 'file' is set
            if (file == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'file' when calling TimestampsApi->ApiV1TimestampsPost");


            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
                "multipart/form-data",
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/json"
            };


            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            if (idempotencyKey != null)
            {
                localVarRequestOptions.HeaderParameters.Add("Idempotency-Key", ImzalaApiClient.Client.ClientUtils.ParameterToString(idempotencyKey)); // header parameter
            }
            localVarRequestOptions.FileParameters.Add("file", file);
            if (description != null)
            {
                localVarRequestOptions.FormParameters.Add("description", ImzalaApiClient.Client.ClientUtils.ParameterToString(description)); // form parameter
            }
            if (ownerFirstName != null)
            {
                localVarRequestOptions.FormParameters.Add("owner_first_name", ImzalaApiClient.Client.ClientUtils.ParameterToString(ownerFirstName)); // form parameter
            }
            if (ownerLastName != null)
            {
                localVarRequestOptions.FormParameters.Add("owner_last_name", ImzalaApiClient.Client.ClientUtils.ParameterToString(ownerLastName)); // form parameter
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PostAsync<ApiV1TimestampsPost201Response>("/api/v1/timestamps", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1TimestampsPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

    }
}
