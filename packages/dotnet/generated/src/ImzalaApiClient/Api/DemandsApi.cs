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
    public interface IDemandsApiSync : IApiAccessor
    {
        #region Synchronous Operations
        /// <summary>
        /// Toplu sözleşme oluştur (tek şablondan N alıcı)
        /// </summary>
        /// <remarks>
        /// Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir). - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1DemandsBulkPostRequest"></param>
        /// <param name="xWorkspaceId">Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  (optional)</param>
        /// <returns>ApiV1DemandsBulkPost200Response</returns>
        ApiV1DemandsBulkPost200Response ApiV1DemandsBulkPost(ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, Guid? xWorkspaceId = default);

        /// <summary>
        /// Toplu sözleşme oluştur (tek şablondan N alıcı)
        /// </summary>
        /// <remarks>
        /// Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir). - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1DemandsBulkPostRequest"></param>
        /// <param name="xWorkspaceId">Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsBulkPost200Response</returns>
        ApiResponse<ApiV1DemandsBulkPost200Response> ApiV1DemandsBulkPostWithHttpInfo(ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, Guid? xWorkspaceId = default);
        /// <summary>
        /// Zarfı imzaya gönder (yayınla + davet)
        /// </summary>
        /// <remarks>
        /// Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDispatchPostRequest"> (optional)</param>
        /// <returns>ApiV1DemandsDemandIdDispatchPost200Response</returns>
        ApiV1DemandsDemandIdDispatchPost200Response ApiV1DemandsDemandIdDispatchPost(Guid demandId, ApiV1DemandsDemandIdDispatchPostRequest? apiV1DemandsDemandIdDispatchPostRequest = default);

        /// <summary>
        /// Zarfı imzaya gönder (yayınla + davet)
        /// </summary>
        /// <remarks>
        /// Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDispatchPostRequest"> (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDispatchPost200Response</returns>
        ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response> ApiV1DemandsDemandIdDispatchPostWithHttpInfo(Guid demandId, ApiV1DemandsDemandIdDispatchPostRequest? apiV1DemandsDemandIdDispatchPostRequest = default);
        /// <summary>
        /// Belgeye imzacı ata (tam-küme replace)
        /// </summary>
        /// <remarks>
        /// Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest"></param>
        /// <returns>ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        ApiV1DemandsDemandIdDocumentsPost201Response ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest);

        /// <summary>
        /// Belgeye imzacı ata (tam-küme replace)
        /// </summary>
        /// <remarks>
        /// Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest"></param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest);
        /// <summary>
        /// Belgeyi zarftan sil
        /// </summary>
        /// <remarks>
        /// Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <returns>ApiV1TemplatesIdDelete200Response</returns>
        ApiV1TemplatesIdDelete200Response ApiV1DemandsDemandIdDocumentsDocIdDelete(Guid demandId, Guid docId);

        /// <summary>
        /// Belgeyi zarftan sil
        /// </summary>
        /// <remarks>
        /// Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <returns>ApiResponse of ApiV1TemplatesIdDelete200Response</returns>
        ApiResponse<ApiV1TemplatesIdDelete200Response> ApiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo(Guid demandId, Guid docId);
        /// <summary>
        /// Belge metadata güncelle
        /// </summary>
        /// <remarks>
        /// Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdPatchRequest"></param>
        /// <returns>ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        ApiV1DemandsDemandIdDocumentsPost201Response ApiV1DemandsDemandIdDocumentsDocIdPatch(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest);

        /// <summary>
        /// Belge metadata güncelle
        /// </summary>
        /// <remarks>
        /// Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdPatchRequest"></param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest);
        /// <summary>
        /// Zarf belge listesi
        /// </summary>
        /// <remarks>
        /// Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="view">&#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). (optional)</param>
        /// <returns>ApiV1DemandsDemandIdDocumentsGet200Response</returns>
        ApiV1DemandsDemandIdDocumentsGet200Response ApiV1DemandsDemandIdDocumentsGet(Guid demandId, string? view = default);

        /// <summary>
        /// Zarf belge listesi
        /// </summary>
        /// <remarks>
        /// Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="view">&#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDocumentsGet200Response</returns>
        ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> ApiV1DemandsDemandIdDocumentsGetWithHttpInfo(Guid demandId, string? view = default);
        /// <summary>
        /// Zarftaki belgelerin sırasını değiştir
        /// </summary>
        /// <remarks>
        /// Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsOrderPutRequest"></param>
        /// <returns>ApiV1DemandsDemandIdDocumentsGet200Response</returns>
        ApiV1DemandsDemandIdDocumentsGet200Response ApiV1DemandsDemandIdDocumentsOrderPut(Guid demandId, ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest);

        /// <summary>
        /// Zarftaki belgelerin sırasını değiştir
        /// </summary>
        /// <remarks>
        /// Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsOrderPutRequest"></param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDocumentsGet200Response</returns>
        ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> ApiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo(Guid demandId, ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest);
        /// <summary>
        /// Zarfa metadata-only belge ekle
        /// </summary>
        /// <remarks>
        /// Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsPostRequest"></param>
        /// <returns>ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        ApiV1DemandsDemandIdDocumentsPost201Response ApiV1DemandsDemandIdDocumentsPost(Guid demandId, ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest);

        /// <summary>
        /// Zarfa metadata-only belge ekle
        /// </summary>
        /// <remarks>
        /// Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsPostRequest"></param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsPostWithHttpInfo(Guid demandId, ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest);
        /// <summary>
        /// Zarfa dosya yükle (belge başına tek dosya)
        /// </summary>
        /// <remarks>
        /// Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="file">PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.</param>
        /// <param name="idempotencyKey">Zorunlu tekrar-koruma anahtarı.</param>
        /// <param name="title"></param>
        /// <param name="docKind"> (optional, default to OTHER)</param>
        /// <param name="isRequired">Multipart alanı — string olarak gönderilir. (optional, default to true)</param>
        /// <returns>ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        ApiV1DemandsDemandIdDocumentsPost201Response ApiV1DemandsDemandIdDocumentsUploadPost(Guid demandId, FileParameter file, string idempotencyKey, string title, string? docKind = default, string? isRequired = default);

        /// <summary>
        /// Zarfa dosya yükle (belge başına tek dosya)
        /// </summary>
        /// <remarks>
        /// Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="file">PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.</param>
        /// <param name="idempotencyKey">Zorunlu tekrar-koruma anahtarı.</param>
        /// <param name="title"></param>
        /// <param name="docKind"> (optional, default to OTHER)</param>
        /// <param name="isRequired">Multipart alanı — string olarak gönderilir. (optional, default to true)</param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo(Guid demandId, FileParameter file, string idempotencyKey, string title, string? docKind = default, string? isRequired = default);
        /// <summary>
        /// Sözleşme listesi (counts-only, PII&#39;siz)
        /// </summary>
        /// <remarks>
        /// Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="status"> (optional)</param>
        /// <param name="q">Başlık araması (optional)</param>
        /// <param name="from"> (optional)</param>
        /// <param name="to"> (optional)</param>
        /// <param name="templateId"> (optional)</param>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit">Sayfa boyutu (page_size ile aynı) (optional, default to 20)</param>
        /// <param name="sort">alan:yön (ör. createdAt:desc) (optional)</param>
        /// <returns>ApiV1DemandsGet200Response</returns>
        ApiV1DemandsGet200Response ApiV1DemandsGet(string? status = default, string? q = default, DateOnly? from = default, DateOnly? to = default, Guid? templateId = default, int? page = default, int? limit = default, string? sort = default);

        /// <summary>
        /// Sözleşme listesi (counts-only, PII&#39;siz)
        /// </summary>
        /// <remarks>
        /// Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="status"> (optional)</param>
        /// <param name="q">Başlık araması (optional)</param>
        /// <param name="from"> (optional)</param>
        /// <param name="to"> (optional)</param>
        /// <param name="templateId"> (optional)</param>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit">Sayfa boyutu (page_size ile aynı) (optional, default to 20)</param>
        /// <param name="sort">alan:yön (ör. createdAt:desc) (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsGet200Response</returns>
        ApiResponse<ApiV1DemandsGet200Response> ApiV1DemandsGetWithHttpInfo(string? status = default, string? q = default, DateOnly? from = default, DateOnly? to = default, Guid? templateId = default, int? page = default, int? limit = default, string? sort = default);
        /// <summary>
        /// Belge-özgü imzalı PDF (çok-belgeli zarf)
        /// </summary>
        /// <remarks>
        /// Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="documentId">Zarftaki belgenin kimliği.</param>
        /// <returns>FileParameter</returns>
        FileParameter ApiV1DemandsIdBelgeDocumentIdPdfGet(Guid id, Guid documentId);

        /// <summary>
        /// Belge-özgü imzalı PDF (çok-belgeli zarf)
        /// </summary>
        /// <remarks>
        /// Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="documentId">Zarftaki belgenin kimliği.</param>
        /// <returns>ApiResponse of FileParameter</returns>
        ApiResponse<FileParameter> ApiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo(Guid id, Guid documentId);
        /// <summary>
        /// Sözleşme iptal (void)
        /// </summary>
        /// <remarks>
        /// Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="apiV1DemandsIdCancelPostRequest"> (optional)</param>
        /// <returns>ApiV1DemandsIdCancelPost200Response</returns>
        ApiV1DemandsIdCancelPost200Response ApiV1DemandsIdCancelPost(Guid id, ApiV1DemandsIdCancelPostRequest? apiV1DemandsIdCancelPostRequest = default);

        /// <summary>
        /// Sözleşme iptal (void)
        /// </summary>
        /// <remarks>
        /// Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="apiV1DemandsIdCancelPostRequest"> (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsIdCancelPost200Response</returns>
        ApiResponse<ApiV1DemandsIdCancelPost200Response> ApiV1DemandsIdCancelPostWithHttpInfo(Guid id, ApiV1DemandsIdCancelPostRequest? apiV1DemandsIdCancelPostRequest = default);
        /// <summary>
        /// Tamamlanma sertifikası (PAdES B-T)
        /// </summary>
        /// <remarks>
        /// Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="lang">tr | en (optional)</param>
        /// <returns>FileParameter</returns>
        FileParameter ApiV1DemandsIdCertificateGet(Guid id, string? lang = default);

        /// <summary>
        /// Tamamlanma sertifikası (PAdES B-T)
        /// </summary>
        /// <remarks>
        /// Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="lang">tr | en (optional)</param>
        /// <returns>ApiResponse of FileParameter</returns>
        ApiResponse<FileParameter> ApiV1DemandsIdCertificateGetWithHttpInfo(Guid id, string? lang = default);
        /// <summary>
        /// Sözleşme sil (yalnızca tamamlanmamış)
        /// </summary>
        /// <remarks>
        /// Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiV1TemplatesIdDelete200Response</returns>
        ApiV1TemplatesIdDelete200Response ApiV1DemandsIdDelete(Guid id);

        /// <summary>
        /// Sözleşme sil (yalnızca tamamlanmamış)
        /// </summary>
        /// <remarks>
        /// Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of ApiV1TemplatesIdDelete200Response</returns>
        ApiResponse<ApiV1TemplatesIdDelete200Response> ApiV1DemandsIdDeleteWithHttpInfo(Guid id);
        /// <summary>
        /// Gömülü imza oturumu başlat (embed token mint)
        /// </summary>
        /// <remarks>
        /// Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Sözleşme (demand) ID</param>
        /// <param name="apiV1DemandsIdEmbedSessionPostRequest"></param>
        /// <returns>ApiV1DemandsIdEmbedSessionPost200Response</returns>
        ApiV1DemandsIdEmbedSessionPost200Response ApiV1DemandsIdEmbedSessionPost(Guid id, ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest);

        /// <summary>
        /// Gömülü imza oturumu başlat (embed token mint)
        /// </summary>
        /// <remarks>
        /// Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Sözleşme (demand) ID</param>
        /// <param name="apiV1DemandsIdEmbedSessionPostRequest"></param>
        /// <returns>ApiResponse of ApiV1DemandsIdEmbedSessionPost200Response</returns>
        ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response> ApiV1DemandsIdEmbedSessionPostWithHttpInfo(Guid id, ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest);
        /// <summary>
        /// Sözleşme durumu + imza ilerlemesi
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiV1DemandsIdGet200Response</returns>
        ApiV1DemandsIdGet200Response ApiV1DemandsIdGet(Guid id);

        /// <summary>
        /// Sözleşme durumu + imza ilerlemesi
        /// </summary>
        /// <remarks>
        /// 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of ApiV1DemandsIdGet200Response</returns>
        ApiResponse<ApiV1DemandsIdGet200Response> ApiV1DemandsIdGetWithHttpInfo(Guid id);
        /// <summary>
        /// Sözleşmeye alan yerleştir (replace)
        /// </summary>
        /// <remarks>
        /// Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |- -- -- -- -- -- --|- -- -- -- -- -- -- -- -- -- --|- -- -- -- -- -- -- -- --| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="upsertItemsRequest"></param>
        /// <returns>UpsertItemsResponse</returns>
        UpsertItemsResponse ApiV1DemandsIdItemsPost(Guid id, UpsertItemsRequest upsertItemsRequest);

        /// <summary>
        /// Sözleşmeye alan yerleştir (replace)
        /// </summary>
        /// <remarks>
        /// Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |- -- -- -- -- -- --|- -- -- -- -- -- -- -- -- -- --|- -- -- -- -- -- -- -- --| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="upsertItemsRequest"></param>
        /// <returns>ApiResponse of UpsertItemsResponse</returns>
        ApiResponse<UpsertItemsResponse> ApiV1DemandsIdItemsPostWithHttpInfo(Guid id, UpsertItemsRequest upsertItemsRequest);
        /// <summary>
        /// Tekil tarafa imza davetini tekrar gönder
        /// </summary>
        /// <remarks>
        /// Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="partyId"></param>
        /// <returns>ApiV1DemandsIdPartiesPartyIdResendPost200Response</returns>
        ApiV1DemandsIdPartiesPartyIdResendPost200Response ApiV1DemandsIdPartiesPartyIdResendPost(Guid id, Guid partyId);

        /// <summary>
        /// Tekil tarafa imza davetini tekrar gönder
        /// </summary>
        /// <remarks>
        /// Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="partyId"></param>
        /// <returns>ApiResponse of ApiV1DemandsIdPartiesPartyIdResendPost200Response</returns>
        ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response> ApiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo(Guid id, Guid partyId);
        /// <summary>
        /// İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
        /// </summary>
        /// <remarks>
        /// Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>FileParameter</returns>
        FileParameter ApiV1DemandsIdPdfGet(Guid id);

        /// <summary>
        /// İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
        /// </summary>
        /// <remarks>
        /// Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of FileParameter</returns>
        ApiResponse<FileParameter> ApiV1DemandsIdPdfGetWithHttpInfo(Guid id);
        /// <summary>
        /// İmza denetim izi (maskeli)
        /// </summary>
        /// <remarks>
        /// Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiV1DemandsIdTimelineGet200Response</returns>
        ApiV1DemandsIdTimelineGet200Response ApiV1DemandsIdTimelineGet(Guid id);

        /// <summary>
        /// İmza denetim izi (maskeli)
        /// </summary>
        /// <remarks>
        /// Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of ApiV1DemandsIdTimelineGet200Response</returns>
        ApiResponse<ApiV1DemandsIdTimelineGet200Response> ApiV1DemandsIdTimelineGetWithHttpInfo(Guid id);
        /// <summary>
        /// Sözleşme oluştur (şablondan)
        /// </summary>
        /// <remarks>
        /// Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="createDemandRequest"></param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <returns>ApiV1DemandsPost201Response</returns>
        ApiV1DemandsPost201Response ApiV1DemandsPost(CreateDemandRequest createDemandRequest, string? idempotencyKey = default);

        /// <summary>
        /// Sözleşme oluştur (şablondan)
        /// </summary>
        /// <remarks>
        /// Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="createDemandRequest"></param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsPost201Response</returns>
        ApiResponse<ApiV1DemandsPost201Response> ApiV1DemandsPostWithHttpInfo(CreateDemandRequest createDemandRequest, string? idempotencyKey = default);
        /// <summary>
        /// Dosya upload ile sözleşme oluştur (şablonsuz)
        /// </summary>
        /// <remarks>
        /// Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="files">1 belge VEYA 1-20 görsel</param>
        /// <param name="parties">JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir. </param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <param name="order">Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)</param>
        /// <param name="title"> (optional)</param>
        /// <param name="description"> (optional)</param>
        /// <param name="fieldTemplateId">Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  (optional)</param>
        /// <param name="force">Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional)</param>
        /// <param name="sendInvitations">&#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Hangi kanalın gideceğini sözleşmenin bildirim ayarları   ve tarafın &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayrakları belirler.   Bu uçtan yaratılan sözleşmelerde ikisi de açık doğar:   pratikte **hem SMS hem e-posta** gider. Kanal seçimi   henüz yoktur. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** bu ucun idempotency   anahtarı yoktur. İstemci zaman aşımında isteği körlemesine   tekrarlarsanız **yeni bir sözleşme, yeni kredi ve ikinci   bir davet seti** oluşur. Zaman aşımında yeniden göndermek   yerine &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  (optional)</param>
        /// <param name="onAnchorMiss">Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  (optional)</param>
        /// <returns>ApiV1DemandsUploadPost201Response</returns>
        ApiV1DemandsUploadPost201Response ApiV1DemandsUploadPost(List<FileParameter> files, string parties, string? idempotencyKey = default, string? order = default, string? title = default, string? description = default, Guid? fieldTemplateId = default, string? force = default, string? sendInvitations = default, string? onAnchorMiss = default);

        /// <summary>
        /// Dosya upload ile sözleşme oluştur (şablonsuz)
        /// </summary>
        /// <remarks>
        /// Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="files">1 belge VEYA 1-20 görsel</param>
        /// <param name="parties">JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir. </param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <param name="order">Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)</param>
        /// <param name="title"> (optional)</param>
        /// <param name="description"> (optional)</param>
        /// <param name="fieldTemplateId">Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  (optional)</param>
        /// <param name="force">Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional)</param>
        /// <param name="sendInvitations">&#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Hangi kanalın gideceğini sözleşmenin bildirim ayarları   ve tarafın &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayrakları belirler.   Bu uçtan yaratılan sözleşmelerde ikisi de açık doğar:   pratikte **hem SMS hem e-posta** gider. Kanal seçimi   henüz yoktur. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** bu ucun idempotency   anahtarı yoktur. İstemci zaman aşımında isteği körlemesine   tekrarlarsanız **yeni bir sözleşme, yeni kredi ve ikinci   bir davet seti** oluşur. Zaman aşımında yeniden göndermek   yerine &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  (optional)</param>
        /// <param name="onAnchorMiss">Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsUploadPost201Response</returns>
        ApiResponse<ApiV1DemandsUploadPost201Response> ApiV1DemandsUploadPostWithHttpInfo(List<FileParameter> files, string parties, string? idempotencyKey = default, string? order = default, string? title = default, string? description = default, Guid? fieldTemplateId = default, string? force = default, string? sendInvitations = default, string? onAnchorMiss = default);
        /// <summary>
        /// Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
        /// </summary>
        /// <remarks>
        /// Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Alan Şablonu (FIELD_LAYOUT) kimliği</param>
        /// <param name="files">Tek PDF belge</param>
        /// <param name="onAnchorMiss">&#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  (optional)</param>
        /// <returns>ApiV1FieldTemplatesIdPreviewLayoutPost200Response</returns>
        ApiV1FieldTemplatesIdPreviewLayoutPost200Response ApiV1FieldTemplatesIdPreviewLayoutPost(Guid id, List<FileParameter> files, string? onAnchorMiss = default);

        /// <summary>
        /// Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
        /// </summary>
        /// <remarks>
        /// Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Alan Şablonu (FIELD_LAYOUT) kimliği</param>
        /// <param name="files">Tek PDF belge</param>
        /// <param name="onAnchorMiss">&#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  (optional)</param>
        /// <returns>ApiResponse of ApiV1FieldTemplatesIdPreviewLayoutPost200Response</returns>
        ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response> ApiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo(Guid id, List<FileParameter> files, string? onAnchorMiss = default);
        #endregion Synchronous Operations
    }

    /// <summary>
    /// Represents a collection of functions to interact with the API endpoints
    /// </summary>
    public interface IDemandsApiAsync : IApiAccessor
    {
        #region Asynchronous Operations
        /// <summary>
        /// Toplu sözleşme oluştur (tek şablondan N alıcı)
        /// </summary>
        /// <remarks>
        /// Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir). - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1DemandsBulkPostRequest"></param>
        /// <param name="xWorkspaceId">Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsBulkPost200Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsBulkPost200Response> ApiV1DemandsBulkPostAsync(ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, Guid? xWorkspaceId = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Toplu sözleşme oluştur (tek şablondan N alıcı)
        /// </summary>
        /// <remarks>
        /// Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir). - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1DemandsBulkPostRequest"></param>
        /// <param name="xWorkspaceId">Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsBulkPost200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsBulkPost200Response>> ApiV1DemandsBulkPostWithHttpInfoAsync(ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, Guid? xWorkspaceId = default, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Zarfı imzaya gönder (yayınla + davet)
        /// </summary>
        /// <remarks>
        /// Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDispatchPostRequest"> (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDispatchPost200Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsDemandIdDispatchPost200Response> ApiV1DemandsDemandIdDispatchPostAsync(Guid demandId, ApiV1DemandsDemandIdDispatchPostRequest? apiV1DemandsDemandIdDispatchPostRequest = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Zarfı imzaya gönder (yayınla + davet)
        /// </summary>
        /// <remarks>
        /// Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDispatchPostRequest"> (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDispatchPost200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response>> ApiV1DemandsDemandIdDispatchPostWithHttpInfoAsync(Guid demandId, ApiV1DemandsDemandIdDispatchPostRequest? apiV1DemandsDemandIdDispatchPostRequest = default, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Belgeye imzacı ata (tam-küme replace)
        /// </summary>
        /// <remarks>
        /// Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutAsync(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Belgeye imzacı ata (tam-küme replace)
        /// </summary>
        /// <remarks>
        /// Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDocumentsPost201Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>> ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfoAsync(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Belgeyi zarftan sil
        /// </summary>
        /// <remarks>
        /// Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TemplatesIdDelete200Response</returns>
        System.Threading.Tasks.Task<ApiV1TemplatesIdDelete200Response> ApiV1DemandsDemandIdDocumentsDocIdDeleteAsync(Guid demandId, Guid docId, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Belgeyi zarftan sil
        /// </summary>
        /// <remarks>
        /// Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TemplatesIdDelete200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1TemplatesIdDelete200Response>> ApiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfoAsync(Guid demandId, Guid docId, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Belge metadata güncelle
        /// </summary>
        /// <remarks>
        /// Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdPatchRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsDocIdPatchAsync(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Belge metadata güncelle
        /// </summary>
        /// <remarks>
        /// Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdPatchRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDocumentsPost201Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>> ApiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfoAsync(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Zarf belge listesi
        /// </summary>
        /// <remarks>
        /// Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="view">&#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDocumentsGet200Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsDemandIdDocumentsGet200Response> ApiV1DemandsDemandIdDocumentsGetAsync(Guid demandId, string? view = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Zarf belge listesi
        /// </summary>
        /// <remarks>
        /// Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="view">&#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDocumentsGet200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response>> ApiV1DemandsDemandIdDocumentsGetWithHttpInfoAsync(Guid demandId, string? view = default, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Zarftaki belgelerin sırasını değiştir
        /// </summary>
        /// <remarks>
        /// Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsOrderPutRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDocumentsGet200Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsDemandIdDocumentsGet200Response> ApiV1DemandsDemandIdDocumentsOrderPutAsync(Guid demandId, ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Zarftaki belgelerin sırasını değiştir
        /// </summary>
        /// <remarks>
        /// Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsOrderPutRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDocumentsGet200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response>> ApiV1DemandsDemandIdDocumentsOrderPutWithHttpInfoAsync(Guid demandId, ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Zarfa metadata-only belge ekle
        /// </summary>
        /// <remarks>
        /// Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsPostRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsPostAsync(Guid demandId, ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Zarfa metadata-only belge ekle
        /// </summary>
        /// <remarks>
        /// Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsPostRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDocumentsPost201Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>> ApiV1DemandsDemandIdDocumentsPostWithHttpInfoAsync(Guid demandId, ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Zarfa dosya yükle (belge başına tek dosya)
        /// </summary>
        /// <remarks>
        /// Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="file">PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.</param>
        /// <param name="idempotencyKey">Zorunlu tekrar-koruma anahtarı.</param>
        /// <param name="title"></param>
        /// <param name="docKind"> (optional, default to OTHER)</param>
        /// <param name="isRequired">Multipart alanı — string olarak gönderilir. (optional, default to true)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsUploadPostAsync(Guid demandId, FileParameter file, string idempotencyKey, string title, string? docKind = default, string? isRequired = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Zarfa dosya yükle (belge başına tek dosya)
        /// </summary>
        /// <remarks>
        /// Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="file">PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.</param>
        /// <param name="idempotencyKey">Zorunlu tekrar-koruma anahtarı.</param>
        /// <param name="title"></param>
        /// <param name="docKind"> (optional, default to OTHER)</param>
        /// <param name="isRequired">Multipart alanı — string olarak gönderilir. (optional, default to true)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDocumentsPost201Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>> ApiV1DemandsDemandIdDocumentsUploadPostWithHttpInfoAsync(Guid demandId, FileParameter file, string idempotencyKey, string title, string? docKind = default, string? isRequired = default, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Sözleşme listesi (counts-only, PII&#39;siz)
        /// </summary>
        /// <remarks>
        /// Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="status"> (optional)</param>
        /// <param name="q">Başlık araması (optional)</param>
        /// <param name="from"> (optional)</param>
        /// <param name="to"> (optional)</param>
        /// <param name="templateId"> (optional)</param>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit">Sayfa boyutu (page_size ile aynı) (optional, default to 20)</param>
        /// <param name="sort">alan:yön (ör. createdAt:desc) (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsGet200Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsGet200Response> ApiV1DemandsGetAsync(string? status = default, string? q = default, DateOnly? from = default, DateOnly? to = default, Guid? templateId = default, int? page = default, int? limit = default, string? sort = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Sözleşme listesi (counts-only, PII&#39;siz)
        /// </summary>
        /// <remarks>
        /// Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="status"> (optional)</param>
        /// <param name="q">Başlık araması (optional)</param>
        /// <param name="from"> (optional)</param>
        /// <param name="to"> (optional)</param>
        /// <param name="templateId"> (optional)</param>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit">Sayfa boyutu (page_size ile aynı) (optional, default to 20)</param>
        /// <param name="sort">alan:yön (ör. createdAt:desc) (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsGet200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsGet200Response>> ApiV1DemandsGetWithHttpInfoAsync(string? status = default, string? q = default, DateOnly? from = default, DateOnly? to = default, Guid? templateId = default, int? page = default, int? limit = default, string? sort = default, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Belge-özgü imzalı PDF (çok-belgeli zarf)
        /// </summary>
        /// <remarks>
        /// Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="documentId">Zarftaki belgenin kimliği.</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of FileParameter</returns>
        System.Threading.Tasks.Task<FileParameter> ApiV1DemandsIdBelgeDocumentIdPdfGetAsync(Guid id, Guid documentId, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Belge-özgü imzalı PDF (çok-belgeli zarf)
        /// </summary>
        /// <remarks>
        /// Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="documentId">Zarftaki belgenin kimliği.</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (FileParameter)</returns>
        System.Threading.Tasks.Task<ApiResponse<FileParameter>> ApiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfoAsync(Guid id, Guid documentId, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Sözleşme iptal (void)
        /// </summary>
        /// <remarks>
        /// Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="apiV1DemandsIdCancelPostRequest"> (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsIdCancelPost200Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsIdCancelPost200Response> ApiV1DemandsIdCancelPostAsync(Guid id, ApiV1DemandsIdCancelPostRequest? apiV1DemandsIdCancelPostRequest = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Sözleşme iptal (void)
        /// </summary>
        /// <remarks>
        /// Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="apiV1DemandsIdCancelPostRequest"> (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsIdCancelPost200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsIdCancelPost200Response>> ApiV1DemandsIdCancelPostWithHttpInfoAsync(Guid id, ApiV1DemandsIdCancelPostRequest? apiV1DemandsIdCancelPostRequest = default, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Tamamlanma sertifikası (PAdES B-T)
        /// </summary>
        /// <remarks>
        /// Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="lang">tr | en (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of FileParameter</returns>
        System.Threading.Tasks.Task<FileParameter> ApiV1DemandsIdCertificateGetAsync(Guid id, string? lang = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Tamamlanma sertifikası (PAdES B-T)
        /// </summary>
        /// <remarks>
        /// Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="lang">tr | en (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (FileParameter)</returns>
        System.Threading.Tasks.Task<ApiResponse<FileParameter>> ApiV1DemandsIdCertificateGetWithHttpInfoAsync(Guid id, string? lang = default, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Sözleşme sil (yalnızca tamamlanmamış)
        /// </summary>
        /// <remarks>
        /// Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TemplatesIdDelete200Response</returns>
        System.Threading.Tasks.Task<ApiV1TemplatesIdDelete200Response> ApiV1DemandsIdDeleteAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Sözleşme sil (yalnızca tamamlanmamış)
        /// </summary>
        /// <remarks>
        /// Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TemplatesIdDelete200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1TemplatesIdDelete200Response>> ApiV1DemandsIdDeleteWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Gömülü imza oturumu başlat (embed token mint)
        /// </summary>
        /// <remarks>
        /// Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Sözleşme (demand) ID</param>
        /// <param name="apiV1DemandsIdEmbedSessionPostRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsIdEmbedSessionPost200Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsIdEmbedSessionPost200Response> ApiV1DemandsIdEmbedSessionPostAsync(Guid id, ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Gömülü imza oturumu başlat (embed token mint)
        /// </summary>
        /// <remarks>
        /// Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Sözleşme (demand) ID</param>
        /// <param name="apiV1DemandsIdEmbedSessionPostRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsIdEmbedSessionPost200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response>> ApiV1DemandsIdEmbedSessionPostWithHttpInfoAsync(Guid id, ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Sözleşme durumu + imza ilerlemesi
        /// </summary>
        /// <remarks>
        /// 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsIdGet200Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsIdGet200Response> ApiV1DemandsIdGetAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Sözleşme durumu + imza ilerlemesi
        /// </summary>
        /// <remarks>
        /// 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsIdGet200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsIdGet200Response>> ApiV1DemandsIdGetWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Sözleşmeye alan yerleştir (replace)
        /// </summary>
        /// <remarks>
        /// Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |- -- -- -- -- -- --|- -- -- -- -- -- -- -- -- -- --|- -- -- -- -- -- -- -- --| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="upsertItemsRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of UpsertItemsResponse</returns>
        System.Threading.Tasks.Task<UpsertItemsResponse> ApiV1DemandsIdItemsPostAsync(Guid id, UpsertItemsRequest upsertItemsRequest, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Sözleşmeye alan yerleştir (replace)
        /// </summary>
        /// <remarks>
        /// Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |- -- -- -- -- -- --|- -- -- -- -- -- -- -- -- -- --|- -- -- -- -- -- -- -- --| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="upsertItemsRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (UpsertItemsResponse)</returns>
        System.Threading.Tasks.Task<ApiResponse<UpsertItemsResponse>> ApiV1DemandsIdItemsPostWithHttpInfoAsync(Guid id, UpsertItemsRequest upsertItemsRequest, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Tekil tarafa imza davetini tekrar gönder
        /// </summary>
        /// <remarks>
        /// Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="partyId"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsIdPartiesPartyIdResendPost200Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsIdPartiesPartyIdResendPost200Response> ApiV1DemandsIdPartiesPartyIdResendPostAsync(Guid id, Guid partyId, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Tekil tarafa imza davetini tekrar gönder
        /// </summary>
        /// <remarks>
        /// Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="partyId"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsIdPartiesPartyIdResendPost200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response>> ApiV1DemandsIdPartiesPartyIdResendPostWithHttpInfoAsync(Guid id, Guid partyId, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
        /// </summary>
        /// <remarks>
        /// Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of FileParameter</returns>
        System.Threading.Tasks.Task<FileParameter> ApiV1DemandsIdPdfGetAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
        /// </summary>
        /// <remarks>
        /// Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (FileParameter)</returns>
        System.Threading.Tasks.Task<ApiResponse<FileParameter>> ApiV1DemandsIdPdfGetWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// İmza denetim izi (maskeli)
        /// </summary>
        /// <remarks>
        /// Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsIdTimelineGet200Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsIdTimelineGet200Response> ApiV1DemandsIdTimelineGetAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// İmza denetim izi (maskeli)
        /// </summary>
        /// <remarks>
        /// Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsIdTimelineGet200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsIdTimelineGet200Response>> ApiV1DemandsIdTimelineGetWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Sözleşme oluştur (şablondan)
        /// </summary>
        /// <remarks>
        /// Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="createDemandRequest"></param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsPost201Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsPost201Response> ApiV1DemandsPostAsync(CreateDemandRequest createDemandRequest, string? idempotencyKey = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Sözleşme oluştur (şablondan)
        /// </summary>
        /// <remarks>
        /// Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="createDemandRequest"></param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsPost201Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsPost201Response>> ApiV1DemandsPostWithHttpInfoAsync(CreateDemandRequest createDemandRequest, string? idempotencyKey = default, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Dosya upload ile sözleşme oluştur (şablonsuz)
        /// </summary>
        /// <remarks>
        /// Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="files">1 belge VEYA 1-20 görsel</param>
        /// <param name="parties">JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir. </param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <param name="order">Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)</param>
        /// <param name="title"> (optional)</param>
        /// <param name="description"> (optional)</param>
        /// <param name="fieldTemplateId">Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  (optional)</param>
        /// <param name="force">Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional)</param>
        /// <param name="sendInvitations">&#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Hangi kanalın gideceğini sözleşmenin bildirim ayarları   ve tarafın &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayrakları belirler.   Bu uçtan yaratılan sözleşmelerde ikisi de açık doğar:   pratikte **hem SMS hem e-posta** gider. Kanal seçimi   henüz yoktur. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** bu ucun idempotency   anahtarı yoktur. İstemci zaman aşımında isteği körlemesine   tekrarlarsanız **yeni bir sözleşme, yeni kredi ve ikinci   bir davet seti** oluşur. Zaman aşımında yeniden göndermek   yerine &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  (optional)</param>
        /// <param name="onAnchorMiss">Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsUploadPost201Response</returns>
        System.Threading.Tasks.Task<ApiV1DemandsUploadPost201Response> ApiV1DemandsUploadPostAsync(List<FileParameter> files, string parties, string? idempotencyKey = default, string? order = default, string? title = default, string? description = default, Guid? fieldTemplateId = default, string? force = default, string? sendInvitations = default, string? onAnchorMiss = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Dosya upload ile sözleşme oluştur (şablonsuz)
        /// </summary>
        /// <remarks>
        /// Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="files">1 belge VEYA 1-20 görsel</param>
        /// <param name="parties">JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir. </param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <param name="order">Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)</param>
        /// <param name="title"> (optional)</param>
        /// <param name="description"> (optional)</param>
        /// <param name="fieldTemplateId">Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  (optional)</param>
        /// <param name="force">Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional)</param>
        /// <param name="sendInvitations">&#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Hangi kanalın gideceğini sözleşmenin bildirim ayarları   ve tarafın &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayrakları belirler.   Bu uçtan yaratılan sözleşmelerde ikisi de açık doğar:   pratikte **hem SMS hem e-posta** gider. Kanal seçimi   henüz yoktur. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** bu ucun idempotency   anahtarı yoktur. İstemci zaman aşımında isteği körlemesine   tekrarlarsanız **yeni bir sözleşme, yeni kredi ve ikinci   bir davet seti** oluşur. Zaman aşımında yeniden göndermek   yerine &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  (optional)</param>
        /// <param name="onAnchorMiss">Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsUploadPost201Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1DemandsUploadPost201Response>> ApiV1DemandsUploadPostWithHttpInfoAsync(List<FileParameter> files, string parties, string? idempotencyKey = default, string? order = default, string? title = default, string? description = default, Guid? fieldTemplateId = default, string? force = default, string? sendInvitations = default, string? onAnchorMiss = default, System.Threading.CancellationToken cancellationToken = default);
        /// <summary>
        /// Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
        /// </summary>
        /// <remarks>
        /// Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Alan Şablonu (FIELD_LAYOUT) kimliği</param>
        /// <param name="files">Tek PDF belge</param>
        /// <param name="onAnchorMiss">&#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1FieldTemplatesIdPreviewLayoutPost200Response</returns>
        System.Threading.Tasks.Task<ApiV1FieldTemplatesIdPreviewLayoutPost200Response> ApiV1FieldTemplatesIdPreviewLayoutPostAsync(Guid id, List<FileParameter> files, string? onAnchorMiss = default, System.Threading.CancellationToken cancellationToken = default);

        /// <summary>
        /// Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
        /// </summary>
        /// <remarks>
        /// Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 
        /// </remarks>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Alan Şablonu (FIELD_LAYOUT) kimliği</param>
        /// <param name="files">Tek PDF belge</param>
        /// <param name="onAnchorMiss">&#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1FieldTemplatesIdPreviewLayoutPost200Response)</returns>
        System.Threading.Tasks.Task<ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response>> ApiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfoAsync(Guid id, List<FileParameter> files, string? onAnchorMiss = default, System.Threading.CancellationToken cancellationToken = default);
        #endregion Asynchronous Operations
    }

    /// <summary>
    /// Represents a collection of functions to interact with the API endpoints
    /// </summary>
    public interface IDemandsApi : IDemandsApiSync, IDemandsApiAsync
    {

    }

    /// <summary>
    /// Represents a collection of functions to interact with the API endpoints
    /// </summary>
    public partial class DemandsApi : IDisposable, IDemandsApi
    {
        private ImzalaApiClient.Client.ExceptionFactory _exceptionFactory = (name, response) => null;

        /// <summary>
        /// Initializes a new instance of the <see cref="DemandsApi"/> class.
        /// **IMPORTANT** This will also create an instance of HttpClient, which is less than ideal.
        /// It's better to reuse the <see href="https://docs.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests#issues-with-the-original-httpclient-class-available-in-net">HttpClient and HttpClientHandler</see>.
        /// </summary>
        /// <returns></returns>
        public DemandsApi() : this((string)null)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="DemandsApi"/> class.
        /// **IMPORTANT** This will also create an instance of HttpClient, which is less than ideal.
        /// It's better to reuse the <see href="https://docs.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests#issues-with-the-original-httpclient-class-available-in-net">HttpClient and HttpClientHandler</see>.
        /// </summary>
        /// <param name="basePath">The target service's base path in URL format.</param>
        /// <exception cref="ArgumentException"></exception>
        /// <returns></returns>
        public DemandsApi(string basePath)
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
        /// Initializes a new instance of the <see cref="DemandsApi"/> class using Configuration object.
        /// **IMPORTANT** This will also create an instance of HttpClient, which is less than ideal.
        /// It's better to reuse the <see href="https://docs.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests#issues-with-the-original-httpclient-class-available-in-net">HttpClient and HttpClientHandler</see>.
        /// </summary>
        /// <param name="configuration">An instance of Configuration.</param>
        /// <exception cref="ArgumentNullException"></exception>
        /// <returns></returns>
        public DemandsApi(ImzalaApiClient.Client.Configuration configuration)
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
        /// Initializes a new instance of the <see cref="DemandsApi"/> class.
        /// </summary>
        /// <param name="client">An instance of HttpClient.</param>
        /// <param name="handler">An optional instance of HttpClientHandler that is used by HttpClient.</param>
        /// <exception cref="ArgumentNullException"></exception>
        /// <returns></returns>
        /// <remarks>
        /// Some configuration settings will not be applied without passing an HttpClientHandler.
        /// The features affected are: Setting and Retrieving Cookies, Client Certificates, Proxy settings.
        /// </remarks>
        public DemandsApi(HttpClient client, HttpClientHandler handler = null) : this(client, (string)null, handler)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="DemandsApi"/> class.
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
        public DemandsApi(HttpClient client, string basePath, HttpClientHandler handler = null)
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
        /// Initializes a new instance of the <see cref="DemandsApi"/> class using Configuration object.
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
        public DemandsApi(HttpClient client, ImzalaApiClient.Client.Configuration configuration, HttpClientHandler handler = null)
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
        /// Initializes a new instance of the <see cref="DemandsApi"/> class
        /// using a Configuration object and client instance.
        /// </summary>
        /// <param name="client">The client interface for synchronous API access.</param>
        /// <param name="asyncClient">The client interface for asynchronous API access.</param>
        /// <param name="configuration">The configuration object.</param>
        /// <exception cref="ArgumentNullException"></exception>
        public DemandsApi(ImzalaApiClient.Client.ISynchronousClient client, ImzalaApiClient.Client.IAsynchronousClient asyncClient, ImzalaApiClient.Client.IReadableConfiguration configuration)
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
        /// Toplu sözleşme oluştur (tek şablondan N alıcı) Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir). - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1DemandsBulkPostRequest"></param>
        /// <param name="xWorkspaceId">Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  (optional)</param>
        /// <returns>ApiV1DemandsBulkPost200Response</returns>
        public ApiV1DemandsBulkPost200Response ApiV1DemandsBulkPost(ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, Guid? xWorkspaceId = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsBulkPost200Response> localVarResponse = ApiV1DemandsBulkPostWithHttpInfo(apiV1DemandsBulkPostRequest, xWorkspaceId);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Toplu sözleşme oluştur (tek şablondan N alıcı) Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir). - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1DemandsBulkPostRequest"></param>
        /// <param name="xWorkspaceId">Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsBulkPost200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsBulkPost200Response> ApiV1DemandsBulkPostWithHttpInfo(ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, Guid? xWorkspaceId = default)
        {
            // verify the required parameter 'apiV1DemandsBulkPostRequest' is set
            if (apiV1DemandsBulkPostRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1DemandsBulkPostRequest' when calling DemandsApi->ApiV1DemandsBulkPost");

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

            if (xWorkspaceId != null)
            {
                localVarRequestOptions.HeaderParameters.Add("X-Workspace-Id", ImzalaApiClient.Client.ClientUtils.ParameterToString(xWorkspaceId)); // header parameter
            }
            localVarRequestOptions.Data = apiV1DemandsBulkPostRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Post<ApiV1DemandsBulkPost200Response>("/api/v1/demands/bulk", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsBulkPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Toplu sözleşme oluştur (tek şablondan N alıcı) Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir). - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1DemandsBulkPostRequest"></param>
        /// <param name="xWorkspaceId">Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsBulkPost200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsBulkPost200Response> ApiV1DemandsBulkPostAsync(ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, Guid? xWorkspaceId = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsBulkPost200Response> localVarResponse = await ApiV1DemandsBulkPostWithHttpInfoAsync(apiV1DemandsBulkPostRequest, xWorkspaceId, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Toplu sözleşme oluştur (tek şablondan N alıcı) Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir). - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="apiV1DemandsBulkPostRequest"></param>
        /// <param name="xWorkspaceId">Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsBulkPost200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsBulkPost200Response>> ApiV1DemandsBulkPostWithHttpInfoAsync(ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, Guid? xWorkspaceId = default, System.Threading.CancellationToken cancellationToken = default)
        {
            // verify the required parameter 'apiV1DemandsBulkPostRequest' is set
            if (apiV1DemandsBulkPostRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1DemandsBulkPostRequest' when calling DemandsApi->ApiV1DemandsBulkPost");


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

            if (xWorkspaceId != null)
            {
                localVarRequestOptions.HeaderParameters.Add("X-Workspace-Id", ImzalaApiClient.Client.ClientUtils.ParameterToString(xWorkspaceId)); // header parameter
            }
            localVarRequestOptions.Data = apiV1DemandsBulkPostRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PostAsync<ApiV1DemandsBulkPost200Response>("/api/v1/demands/bulk", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsBulkPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zarfı imzaya gönder (yayınla + davet) Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDispatchPostRequest"> (optional)</param>
        /// <returns>ApiV1DemandsDemandIdDispatchPost200Response</returns>
        public ApiV1DemandsDemandIdDispatchPost200Response ApiV1DemandsDemandIdDispatchPost(Guid demandId, ApiV1DemandsDemandIdDispatchPostRequest? apiV1DemandsDemandIdDispatchPostRequest = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response> localVarResponse = ApiV1DemandsDemandIdDispatchPostWithHttpInfo(demandId, apiV1DemandsDemandIdDispatchPostRequest);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zarfı imzaya gönder (yayınla + davet) Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDispatchPostRequest"> (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDispatchPost200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response> ApiV1DemandsDemandIdDispatchPostWithHttpInfo(Guid demandId, ApiV1DemandsDemandIdDispatchPostRequest? apiV1DemandsDemandIdDispatchPostRequest = default)
        {
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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsDemandIdDispatchPostRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Post<ApiV1DemandsDemandIdDispatchPost200Response>("/api/v1/demands/{demandId}/dispatch", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDispatchPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zarfı imzaya gönder (yayınla + davet) Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDispatchPostRequest"> (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDispatchPost200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsDemandIdDispatchPost200Response> ApiV1DemandsDemandIdDispatchPostAsync(Guid demandId, ApiV1DemandsDemandIdDispatchPostRequest? apiV1DemandsDemandIdDispatchPostRequest = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response> localVarResponse = await ApiV1DemandsDemandIdDispatchPostWithHttpInfoAsync(demandId, apiV1DemandsDemandIdDispatchPostRequest, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zarfı imzaya gönder (yayınla + davet) Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDispatchPostRequest"> (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDispatchPost200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response>> ApiV1DemandsDemandIdDispatchPostWithHttpInfoAsync(Guid demandId, ApiV1DemandsDemandIdDispatchPostRequest? apiV1DemandsDemandIdDispatchPostRequest = default, System.Threading.CancellationToken cancellationToken = default)
        {

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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsDemandIdDispatchPostRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PostAsync<ApiV1DemandsDemandIdDispatchPost200Response>("/api/v1/demands/{demandId}/dispatch", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDispatchPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Belgeye imzacı ata (tam-küme replace) Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest"></param>
        /// <returns>ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        public ApiV1DemandsDemandIdDocumentsPost201Response ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> localVarResponse = ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Belgeye imzacı ata (tam-küme replace) Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest"></param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest)
        {
            // verify the required parameter 'apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest' is set
            if (apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPut");

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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.PathParameters.Add("docId", ImzalaApiClient.Client.ClientUtils.ParameterToString(docId)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Put<ApiV1DemandsDemandIdDocumentsPost201Response>("/api/v1/demands/{demandId}/documents/{docId}/assignments", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPut", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Belgeye imzacı ata (tam-küme replace) Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutAsync(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> localVarResponse = await ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfoAsync(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Belgeye imzacı ata (tam-küme replace) Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDocumentsPost201Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>> ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfoAsync(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            // verify the required parameter 'apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest' is set
            if (apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPut");


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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.PathParameters.Add("docId", ImzalaApiClient.Client.ClientUtils.ParameterToString(docId)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PutAsync<ApiV1DemandsDemandIdDocumentsPost201Response>("/api/v1/demands/{demandId}/documents/{docId}/assignments", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPut", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Belgeyi zarftan sil Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <returns>ApiV1TemplatesIdDelete200Response</returns>
        public ApiV1TemplatesIdDelete200Response ApiV1DemandsDemandIdDocumentsDocIdDelete(Guid demandId, Guid docId)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdDelete200Response> localVarResponse = ApiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo(demandId, docId);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Belgeyi zarftan sil Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <returns>ApiResponse of ApiV1TemplatesIdDelete200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdDelete200Response> ApiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo(Guid demandId, Guid docId)
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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.PathParameters.Add("docId", ImzalaApiClient.Client.ClientUtils.ParameterToString(docId)); // path parameter

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Delete<ApiV1TemplatesIdDelete200Response>("/api/v1/demands/{demandId}/documents/{docId}", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsDocIdDelete", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Belgeyi zarftan sil Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TemplatesIdDelete200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1TemplatesIdDelete200Response> ApiV1DemandsDemandIdDocumentsDocIdDeleteAsync(Guid demandId, Guid docId, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdDelete200Response> localVarResponse = await ApiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfoAsync(demandId, docId, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Belgeyi zarftan sil Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TemplatesIdDelete200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdDelete200Response>> ApiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfoAsync(Guid demandId, Guid docId, System.Threading.CancellationToken cancellationToken = default)
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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.PathParameters.Add("docId", ImzalaApiClient.Client.ClientUtils.ParameterToString(docId)); // path parameter

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.DeleteAsync<ApiV1TemplatesIdDelete200Response>("/api/v1/demands/{demandId}/documents/{docId}", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsDocIdDelete", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Belge metadata güncelle Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdPatchRequest"></param>
        /// <returns>ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        public ApiV1DemandsDemandIdDocumentsPost201Response ApiV1DemandsDemandIdDocumentsDocIdPatch(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> localVarResponse = ApiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Belge metadata güncelle Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdPatchRequest"></param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest)
        {
            // verify the required parameter 'apiV1DemandsDemandIdDocumentsDocIdPatchRequest' is set
            if (apiV1DemandsDemandIdDocumentsDocIdPatchRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1DemandsDemandIdDocumentsDocIdPatchRequest' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsDocIdPatch");

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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.PathParameters.Add("docId", ImzalaApiClient.Client.ClientUtils.ParameterToString(docId)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsDemandIdDocumentsDocIdPatchRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Patch<ApiV1DemandsDemandIdDocumentsPost201Response>("/api/v1/demands/{demandId}/documents/{docId}", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsDocIdPatch", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Belge metadata güncelle Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdPatchRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsDocIdPatchAsync(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> localVarResponse = await ApiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfoAsync(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Belge metadata güncelle Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="docId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsDocIdPatchRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDocumentsPost201Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>> ApiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfoAsync(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            // verify the required parameter 'apiV1DemandsDemandIdDocumentsDocIdPatchRequest' is set
            if (apiV1DemandsDemandIdDocumentsDocIdPatchRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1DemandsDemandIdDocumentsDocIdPatchRequest' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsDocIdPatch");


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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.PathParameters.Add("docId", ImzalaApiClient.Client.ClientUtils.ParameterToString(docId)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsDemandIdDocumentsDocIdPatchRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PatchAsync<ApiV1DemandsDemandIdDocumentsPost201Response>("/api/v1/demands/{demandId}/documents/{docId}", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsDocIdPatch", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zarf belge listesi Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="view">&#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). (optional)</param>
        /// <returns>ApiV1DemandsDemandIdDocumentsGet200Response</returns>
        public ApiV1DemandsDemandIdDocumentsGet200Response ApiV1DemandsDemandIdDocumentsGet(Guid demandId, string? view = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> localVarResponse = ApiV1DemandsDemandIdDocumentsGetWithHttpInfo(demandId, view);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zarf belge listesi Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="view">&#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDocumentsGet200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> ApiV1DemandsDemandIdDocumentsGetWithHttpInfo(Guid demandId, string? view = default)
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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            if (view != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "view", view));
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Get<ApiV1DemandsDemandIdDocumentsGet200Response>("/api/v1/demands/{demandId}/documents", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zarf belge listesi Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="view">&#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDocumentsGet200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsDemandIdDocumentsGet200Response> ApiV1DemandsDemandIdDocumentsGetAsync(Guid demandId, string? view = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> localVarResponse = await ApiV1DemandsDemandIdDocumentsGetWithHttpInfoAsync(demandId, view, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zarf belge listesi Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="view">&#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDocumentsGet200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response>> ApiV1DemandsDemandIdDocumentsGetWithHttpInfoAsync(Guid demandId, string? view = default, System.Threading.CancellationToken cancellationToken = default)
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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            if (view != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "view", view));
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.GetAsync<ApiV1DemandsDemandIdDocumentsGet200Response>("/api/v1/demands/{demandId}/documents", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zarftaki belgelerin sırasını değiştir Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsOrderPutRequest"></param>
        /// <returns>ApiV1DemandsDemandIdDocumentsGet200Response</returns>
        public ApiV1DemandsDemandIdDocumentsGet200Response ApiV1DemandsDemandIdDocumentsOrderPut(Guid demandId, ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> localVarResponse = ApiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo(demandId, apiV1DemandsDemandIdDocumentsOrderPutRequest);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zarftaki belgelerin sırasını değiştir Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsOrderPutRequest"></param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDocumentsGet200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> ApiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo(Guid demandId, ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest)
        {
            // verify the required parameter 'apiV1DemandsDemandIdDocumentsOrderPutRequest' is set
            if (apiV1DemandsDemandIdDocumentsOrderPutRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1DemandsDemandIdDocumentsOrderPutRequest' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsOrderPut");

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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsDemandIdDocumentsOrderPutRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Put<ApiV1DemandsDemandIdDocumentsGet200Response>("/api/v1/demands/{demandId}/documents/order", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsOrderPut", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zarftaki belgelerin sırasını değiştir Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsOrderPutRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDocumentsGet200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsDemandIdDocumentsGet200Response> ApiV1DemandsDemandIdDocumentsOrderPutAsync(Guid demandId, ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> localVarResponse = await ApiV1DemandsDemandIdDocumentsOrderPutWithHttpInfoAsync(demandId, apiV1DemandsDemandIdDocumentsOrderPutRequest, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zarftaki belgelerin sırasını değiştir Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsOrderPutRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDocumentsGet200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response>> ApiV1DemandsDemandIdDocumentsOrderPutWithHttpInfoAsync(Guid demandId, ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            // verify the required parameter 'apiV1DemandsDemandIdDocumentsOrderPutRequest' is set
            if (apiV1DemandsDemandIdDocumentsOrderPutRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1DemandsDemandIdDocumentsOrderPutRequest' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsOrderPut");


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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsDemandIdDocumentsOrderPutRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PutAsync<ApiV1DemandsDemandIdDocumentsGet200Response>("/api/v1/demands/{demandId}/documents/order", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsOrderPut", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zarfa metadata-only belge ekle Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsPostRequest"></param>
        /// <returns>ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        public ApiV1DemandsDemandIdDocumentsPost201Response ApiV1DemandsDemandIdDocumentsPost(Guid demandId, ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> localVarResponse = ApiV1DemandsDemandIdDocumentsPostWithHttpInfo(demandId, apiV1DemandsDemandIdDocumentsPostRequest);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zarfa metadata-only belge ekle Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsPostRequest"></param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsPostWithHttpInfo(Guid demandId, ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest)
        {
            // verify the required parameter 'apiV1DemandsDemandIdDocumentsPostRequest' is set
            if (apiV1DemandsDemandIdDocumentsPostRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1DemandsDemandIdDocumentsPostRequest' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsPost");

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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsDemandIdDocumentsPostRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Post<ApiV1DemandsDemandIdDocumentsPost201Response>("/api/v1/demands/{demandId}/documents", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zarfa metadata-only belge ekle Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsPostRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsPostAsync(Guid demandId, ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> localVarResponse = await ApiV1DemandsDemandIdDocumentsPostWithHttpInfoAsync(demandId, apiV1DemandsDemandIdDocumentsPostRequest, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zarfa metadata-only belge ekle Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="apiV1DemandsDemandIdDocumentsPostRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDocumentsPost201Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>> ApiV1DemandsDemandIdDocumentsPostWithHttpInfoAsync(Guid demandId, ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            // verify the required parameter 'apiV1DemandsDemandIdDocumentsPostRequest' is set
            if (apiV1DemandsDemandIdDocumentsPostRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1DemandsDemandIdDocumentsPostRequest' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsPost");


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

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsDemandIdDocumentsPostRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PostAsync<ApiV1DemandsDemandIdDocumentsPost201Response>("/api/v1/demands/{demandId}/documents", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zarfa dosya yükle (belge başına tek dosya) Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="file">PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.</param>
        /// <param name="idempotencyKey">Zorunlu tekrar-koruma anahtarı.</param>
        /// <param name="title"></param>
        /// <param name="docKind"> (optional, default to OTHER)</param>
        /// <param name="isRequired">Multipart alanı — string olarak gönderilir. (optional, default to true)</param>
        /// <returns>ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        public ApiV1DemandsDemandIdDocumentsPost201Response ApiV1DemandsDemandIdDocumentsUploadPost(Guid demandId, FileParameter file, string idempotencyKey, string title, string? docKind = default, string? isRequired = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> localVarResponse = ApiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo(demandId, file, idempotencyKey, title, docKind, isRequired);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zarfa dosya yükle (belge başına tek dosya) Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="file">PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.</param>
        /// <param name="idempotencyKey">Zorunlu tekrar-koruma anahtarı.</param>
        /// <param name="title"></param>
        /// <param name="docKind"> (optional, default to OTHER)</param>
        /// <param name="isRequired">Multipart alanı — string olarak gönderilir. (optional, default to true)</param>
        /// <returns>ApiResponse of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo(Guid demandId, FileParameter file, string idempotencyKey, string title, string? docKind = default, string? isRequired = default)
        {
            // verify the required parameter 'file' is set
            if (file == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'file' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsUploadPost");

            // verify the required parameter 'idempotencyKey' is set
            if (idempotencyKey == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'idempotencyKey' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsUploadPost");

            // verify the required parameter 'title' is set
            if (title == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'title' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsUploadPost");

            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
                "multipart/form-data"
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/json"
            };

            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.FileParameters.Add("file", file);
            localVarRequestOptions.FormParameters.Add("idempotency_key", ImzalaApiClient.Client.ClientUtils.ParameterToString(idempotencyKey)); // form parameter
            localVarRequestOptions.FormParameters.Add("title", ImzalaApiClient.Client.ClientUtils.ParameterToString(title)); // form parameter
            if (docKind != null)
            {
                localVarRequestOptions.FormParameters.Add("doc_kind", ImzalaApiClient.Client.ClientUtils.ParameterToString(docKind)); // form parameter
            }
            if (isRequired != null)
            {
                localVarRequestOptions.FormParameters.Add("is_required", ImzalaApiClient.Client.ClientUtils.ParameterToString(isRequired)); // form parameter
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Post<ApiV1DemandsDemandIdDocumentsPost201Response>("/api/v1/demands/{demandId}/documents/upload", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsUploadPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Zarfa dosya yükle (belge başına tek dosya) Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="file">PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.</param>
        /// <param name="idempotencyKey">Zorunlu tekrar-koruma anahtarı.</param>
        /// <param name="title"></param>
        /// <param name="docKind"> (optional, default to OTHER)</param>
        /// <param name="isRequired">Multipart alanı — string olarak gönderilir. (optional, default to true)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsDemandIdDocumentsPost201Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsDemandIdDocumentsPost201Response> ApiV1DemandsDemandIdDocumentsUploadPostAsync(Guid demandId, FileParameter file, string idempotencyKey, string title, string? docKind = default, string? isRequired = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> localVarResponse = await ApiV1DemandsDemandIdDocumentsUploadPostWithHttpInfoAsync(demandId, file, idempotencyKey, title, docKind, isRequired, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Zarfa dosya yükle (belge başına tek dosya) Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="demandId"></param>
        /// <param name="file">PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.</param>
        /// <param name="idempotencyKey">Zorunlu tekrar-koruma anahtarı.</param>
        /// <param name="title"></param>
        /// <param name="docKind"> (optional, default to OTHER)</param>
        /// <param name="isRequired">Multipart alanı — string olarak gönderilir. (optional, default to true)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsDemandIdDocumentsPost201Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response>> ApiV1DemandsDemandIdDocumentsUploadPostWithHttpInfoAsync(Guid demandId, FileParameter file, string idempotencyKey, string title, string? docKind = default, string? isRequired = default, System.Threading.CancellationToken cancellationToken = default)
        {
            // verify the required parameter 'file' is set
            if (file == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'file' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsUploadPost");

            // verify the required parameter 'idempotencyKey' is set
            if (idempotencyKey == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'idempotencyKey' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsUploadPost");

            // verify the required parameter 'title' is set
            if (title == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'title' when calling DemandsApi->ApiV1DemandsDemandIdDocumentsUploadPost");


            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
                "multipart/form-data"
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/json"
            };


            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            localVarRequestOptions.PathParameters.Add("demandId", ImzalaApiClient.Client.ClientUtils.ParameterToString(demandId)); // path parameter
            localVarRequestOptions.FileParameters.Add("file", file);
            localVarRequestOptions.FormParameters.Add("idempotency_key", ImzalaApiClient.Client.ClientUtils.ParameterToString(idempotencyKey)); // form parameter
            localVarRequestOptions.FormParameters.Add("title", ImzalaApiClient.Client.ClientUtils.ParameterToString(title)); // form parameter
            if (docKind != null)
            {
                localVarRequestOptions.FormParameters.Add("doc_kind", ImzalaApiClient.Client.ClientUtils.ParameterToString(docKind)); // form parameter
            }
            if (isRequired != null)
            {
                localVarRequestOptions.FormParameters.Add("is_required", ImzalaApiClient.Client.ClientUtils.ParameterToString(isRequired)); // form parameter
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PostAsync<ApiV1DemandsDemandIdDocumentsPost201Response>("/api/v1/demands/{demandId}/documents/upload", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsDemandIdDocumentsUploadPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Sözleşme listesi (counts-only, PII&#39;siz) Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="status"> (optional)</param>
        /// <param name="q">Başlık araması (optional)</param>
        /// <param name="from"> (optional)</param>
        /// <param name="to"> (optional)</param>
        /// <param name="templateId"> (optional)</param>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit">Sayfa boyutu (page_size ile aynı) (optional, default to 20)</param>
        /// <param name="sort">alan:yön (ör. createdAt:desc) (optional)</param>
        /// <returns>ApiV1DemandsGet200Response</returns>
        public ApiV1DemandsGet200Response ApiV1DemandsGet(string? status = default, string? q = default, DateOnly? from = default, DateOnly? to = default, Guid? templateId = default, int? page = default, int? limit = default, string? sort = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsGet200Response> localVarResponse = ApiV1DemandsGetWithHttpInfo(status, q, from, to, templateId, page, limit, sort);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Sözleşme listesi (counts-only, PII&#39;siz) Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="status"> (optional)</param>
        /// <param name="q">Başlık araması (optional)</param>
        /// <param name="from"> (optional)</param>
        /// <param name="to"> (optional)</param>
        /// <param name="templateId"> (optional)</param>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit">Sayfa boyutu (page_size ile aynı) (optional, default to 20)</param>
        /// <param name="sort">alan:yön (ör. createdAt:desc) (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsGet200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsGet200Response> ApiV1DemandsGetWithHttpInfo(string? status = default, string? q = default, DateOnly? from = default, DateOnly? to = default, Guid? templateId = default, int? page = default, int? limit = default, string? sort = default)
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

            if (status != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "status", status));
            }
            if (q != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "q", q));
            }
            if (from != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "from", from));
            }
            if (to != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "to", to));
            }
            if (templateId != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "template_id", templateId));
            }
            if (page != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "page", page));
            }
            if (limit != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "limit", limit));
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
            var localVarResponse = this.Client.Get<ApiV1DemandsGet200Response>("/api/v1/demands", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Sözleşme listesi (counts-only, PII&#39;siz) Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="status"> (optional)</param>
        /// <param name="q">Başlık araması (optional)</param>
        /// <param name="from"> (optional)</param>
        /// <param name="to"> (optional)</param>
        /// <param name="templateId"> (optional)</param>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit">Sayfa boyutu (page_size ile aynı) (optional, default to 20)</param>
        /// <param name="sort">alan:yön (ör. createdAt:desc) (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsGet200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsGet200Response> ApiV1DemandsGetAsync(string? status = default, string? q = default, DateOnly? from = default, DateOnly? to = default, Guid? templateId = default, int? page = default, int? limit = default, string? sort = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsGet200Response> localVarResponse = await ApiV1DemandsGetWithHttpInfoAsync(status, q, from, to, templateId, page, limit, sort, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Sözleşme listesi (counts-only, PII&#39;siz) Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="status"> (optional)</param>
        /// <param name="q">Başlık araması (optional)</param>
        /// <param name="from"> (optional)</param>
        /// <param name="to"> (optional)</param>
        /// <param name="templateId"> (optional)</param>
        /// <param name="page"> (optional, default to 1)</param>
        /// <param name="limit">Sayfa boyutu (page_size ile aynı) (optional, default to 20)</param>
        /// <param name="sort">alan:yön (ör. createdAt:desc) (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsGet200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsGet200Response>> ApiV1DemandsGetWithHttpInfoAsync(string? status = default, string? q = default, DateOnly? from = default, DateOnly? to = default, Guid? templateId = default, int? page = default, int? limit = default, string? sort = default, System.Threading.CancellationToken cancellationToken = default)
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

            if (status != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "status", status));
            }
            if (q != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "q", q));
            }
            if (from != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "from", from));
            }
            if (to != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "to", to));
            }
            if (templateId != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "template_id", templateId));
            }
            if (page != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "page", page));
            }
            if (limit != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "limit", limit));
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

            var localVarResponse = await this.AsynchronousClient.GetAsync<ApiV1DemandsGet200Response>("/api/v1/demands", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Belge-özgü imzalı PDF (çok-belgeli zarf) Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="documentId">Zarftaki belgenin kimliği.</param>
        /// <returns>FileParameter</returns>
        public FileParameter ApiV1DemandsIdBelgeDocumentIdPdfGet(Guid id, Guid documentId)
        {
            ImzalaApiClient.Client.ApiResponse<FileParameter> localVarResponse = ApiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo(id, documentId);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Belge-özgü imzalı PDF (çok-belgeli zarf) Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="documentId">Zarftaki belgenin kimliği.</param>
        /// <returns>ApiResponse of FileParameter</returns>
        public ImzalaApiClient.Client.ApiResponse<FileParameter> ApiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo(Guid id, Guid documentId)
        {
            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/pdf",
                "application/json"
            };

            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter
            localVarRequestOptions.PathParameters.Add("document_id", ImzalaApiClient.Client.ClientUtils.ParameterToString(documentId)); // path parameter

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Get<FileParameter>("/api/v1/demands/{id}/belge/{document_id}/pdf", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdBelgeDocumentIdPdfGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Belge-özgü imzalı PDF (çok-belgeli zarf) Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="documentId">Zarftaki belgenin kimliği.</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of FileParameter</returns>
        public async System.Threading.Tasks.Task<FileParameter> ApiV1DemandsIdBelgeDocumentIdPdfGetAsync(Guid id, Guid documentId, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<FileParameter> localVarResponse = await ApiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfoAsync(id, documentId, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Belge-özgü imzalı PDF (çok-belgeli zarf) Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="documentId">Zarftaki belgenin kimliği.</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (FileParameter)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<FileParameter>> ApiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfoAsync(Guid id, Guid documentId, System.Threading.CancellationToken cancellationToken = default)
        {

            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/pdf",
                "application/json"
            };


            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter
            localVarRequestOptions.PathParameters.Add("document_id", ImzalaApiClient.Client.ClientUtils.ParameterToString(documentId)); // path parameter

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.GetAsync<FileParameter>("/api/v1/demands/{id}/belge/{document_id}/pdf", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdBelgeDocumentIdPdfGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Sözleşme iptal (void) Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="apiV1DemandsIdCancelPostRequest"> (optional)</param>
        /// <returns>ApiV1DemandsIdCancelPost200Response</returns>
        public ApiV1DemandsIdCancelPost200Response ApiV1DemandsIdCancelPost(Guid id, ApiV1DemandsIdCancelPostRequest? apiV1DemandsIdCancelPostRequest = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdCancelPost200Response> localVarResponse = ApiV1DemandsIdCancelPostWithHttpInfo(id, apiV1DemandsIdCancelPostRequest);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Sözleşme iptal (void) Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="apiV1DemandsIdCancelPostRequest"> (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsIdCancelPost200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdCancelPost200Response> ApiV1DemandsIdCancelPostWithHttpInfo(Guid id, ApiV1DemandsIdCancelPostRequest? apiV1DemandsIdCancelPostRequest = default)
        {
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

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsIdCancelPostRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Post<ApiV1DemandsIdCancelPost200Response>("/api/v1/demands/{id}/cancel", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdCancelPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Sözleşme iptal (void) Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="apiV1DemandsIdCancelPostRequest"> (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsIdCancelPost200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsIdCancelPost200Response> ApiV1DemandsIdCancelPostAsync(Guid id, ApiV1DemandsIdCancelPostRequest? apiV1DemandsIdCancelPostRequest = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdCancelPost200Response> localVarResponse = await ApiV1DemandsIdCancelPostWithHttpInfoAsync(id, apiV1DemandsIdCancelPostRequest, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Sözleşme iptal (void) Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="apiV1DemandsIdCancelPostRequest"> (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsIdCancelPost200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdCancelPost200Response>> ApiV1DemandsIdCancelPostWithHttpInfoAsync(Guid id, ApiV1DemandsIdCancelPostRequest? apiV1DemandsIdCancelPostRequest = default, System.Threading.CancellationToken cancellationToken = default)
        {

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

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsIdCancelPostRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PostAsync<ApiV1DemandsIdCancelPost200Response>("/api/v1/demands/{id}/cancel", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdCancelPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Tamamlanma sertifikası (PAdES B-T) Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="lang">tr | en (optional)</param>
        /// <returns>FileParameter</returns>
        public FileParameter ApiV1DemandsIdCertificateGet(Guid id, string? lang = default)
        {
            ImzalaApiClient.Client.ApiResponse<FileParameter> localVarResponse = ApiV1DemandsIdCertificateGetWithHttpInfo(id, lang);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Tamamlanma sertifikası (PAdES B-T) Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="lang">tr | en (optional)</param>
        /// <returns>ApiResponse of FileParameter</returns>
        public ImzalaApiClient.Client.ApiResponse<FileParameter> ApiV1DemandsIdCertificateGetWithHttpInfo(Guid id, string? lang = default)
        {
            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/pdf",
                "application/json"
            };

            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter
            if (lang != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "lang", lang));
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Get<FileParameter>("/api/v1/demands/{id}/certificate", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdCertificateGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Tamamlanma sertifikası (PAdES B-T) Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="lang">tr | en (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of FileParameter</returns>
        public async System.Threading.Tasks.Task<FileParameter> ApiV1DemandsIdCertificateGetAsync(Guid id, string? lang = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<FileParameter> localVarResponse = await ApiV1DemandsIdCertificateGetWithHttpInfoAsync(id, lang, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Tamamlanma sertifikası (PAdES B-T) Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="lang">tr | en (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (FileParameter)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<FileParameter>> ApiV1DemandsIdCertificateGetWithHttpInfoAsync(Guid id, string? lang = default, System.Threading.CancellationToken cancellationToken = default)
        {

            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/pdf",
                "application/json"
            };


            var localVarContentType = ImzalaApiClient.Client.ClientUtils.SelectHeaderContentType(_contentTypes);
            if (localVarContentType != null) localVarRequestOptions.HeaderParameters.Add("Content-Type", localVarContentType);

            var localVarAccept = ImzalaApiClient.Client.ClientUtils.SelectHeaderAccept(_accepts);
            if (localVarAccept != null) localVarRequestOptions.HeaderParameters.Add("Accept", localVarAccept);

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter
            if (lang != null)
            {
                localVarRequestOptions.QueryParameters.Add(ImzalaApiClient.Client.ClientUtils.ParameterToMultiMap("", "lang", lang));
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.GetAsync<FileParameter>("/api/v1/demands/{id}/certificate", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdCertificateGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Sözleşme sil (yalnızca tamamlanmamış) Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiV1TemplatesIdDelete200Response</returns>
        public ApiV1TemplatesIdDelete200Response ApiV1DemandsIdDelete(Guid id)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdDelete200Response> localVarResponse = ApiV1DemandsIdDeleteWithHttpInfo(id);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Sözleşme sil (yalnızca tamamlanmamış) Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of ApiV1TemplatesIdDelete200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdDelete200Response> ApiV1DemandsIdDeleteWithHttpInfo(Guid id)
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
            var localVarResponse = this.Client.Delete<ApiV1TemplatesIdDelete200Response>("/api/v1/demands/{id}", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdDelete", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Sözleşme sil (yalnızca tamamlanmamış) Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1TemplatesIdDelete200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1TemplatesIdDelete200Response> ApiV1DemandsIdDeleteAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdDelete200Response> localVarResponse = await ApiV1DemandsIdDeleteWithHttpInfoAsync(id, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Sözleşme sil (yalnızca tamamlanmamış) Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1TemplatesIdDelete200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1TemplatesIdDelete200Response>> ApiV1DemandsIdDeleteWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
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

            var localVarResponse = await this.AsynchronousClient.DeleteAsync<ApiV1TemplatesIdDelete200Response>("/api/v1/demands/{id}", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdDelete", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Gömülü imza oturumu başlat (embed token mint) Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Sözleşme (demand) ID</param>
        /// <param name="apiV1DemandsIdEmbedSessionPostRequest"></param>
        /// <returns>ApiV1DemandsIdEmbedSessionPost200Response</returns>
        public ApiV1DemandsIdEmbedSessionPost200Response ApiV1DemandsIdEmbedSessionPost(Guid id, ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response> localVarResponse = ApiV1DemandsIdEmbedSessionPostWithHttpInfo(id, apiV1DemandsIdEmbedSessionPostRequest);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Gömülü imza oturumu başlat (embed token mint) Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Sözleşme (demand) ID</param>
        /// <param name="apiV1DemandsIdEmbedSessionPostRequest"></param>
        /// <returns>ApiResponse of ApiV1DemandsIdEmbedSessionPost200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response> ApiV1DemandsIdEmbedSessionPostWithHttpInfo(Guid id, ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest)
        {
            // verify the required parameter 'apiV1DemandsIdEmbedSessionPostRequest' is set
            if (apiV1DemandsIdEmbedSessionPostRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1DemandsIdEmbedSessionPostRequest' when calling DemandsApi->ApiV1DemandsIdEmbedSessionPost");

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

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsIdEmbedSessionPostRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Post<ApiV1DemandsIdEmbedSessionPost200Response>("/api/v1/demands/{id}/embed-session", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdEmbedSessionPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Gömülü imza oturumu başlat (embed token mint) Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Sözleşme (demand) ID</param>
        /// <param name="apiV1DemandsIdEmbedSessionPostRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsIdEmbedSessionPost200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsIdEmbedSessionPost200Response> ApiV1DemandsIdEmbedSessionPostAsync(Guid id, ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response> localVarResponse = await ApiV1DemandsIdEmbedSessionPostWithHttpInfoAsync(id, apiV1DemandsIdEmbedSessionPostRequest, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Gömülü imza oturumu başlat (embed token mint) Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Sözleşme (demand) ID</param>
        /// <param name="apiV1DemandsIdEmbedSessionPostRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsIdEmbedSessionPost200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response>> ApiV1DemandsIdEmbedSessionPostWithHttpInfoAsync(Guid id, ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            // verify the required parameter 'apiV1DemandsIdEmbedSessionPostRequest' is set
            if (apiV1DemandsIdEmbedSessionPostRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'apiV1DemandsIdEmbedSessionPostRequest' when calling DemandsApi->ApiV1DemandsIdEmbedSessionPost");


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

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter
            localVarRequestOptions.Data = apiV1DemandsIdEmbedSessionPostRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PostAsync<ApiV1DemandsIdEmbedSessionPost200Response>("/api/v1/demands/{id}/embed-session", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdEmbedSessionPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Sözleşme durumu + imza ilerlemesi 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiV1DemandsIdGet200Response</returns>
        public ApiV1DemandsIdGet200Response ApiV1DemandsIdGet(Guid id)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdGet200Response> localVarResponse = ApiV1DemandsIdGetWithHttpInfo(id);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Sözleşme durumu + imza ilerlemesi 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of ApiV1DemandsIdGet200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdGet200Response> ApiV1DemandsIdGetWithHttpInfo(Guid id)
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
            var localVarResponse = this.Client.Get<ApiV1DemandsIdGet200Response>("/api/v1/demands/{id}", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Sözleşme durumu + imza ilerlemesi 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsIdGet200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsIdGet200Response> ApiV1DemandsIdGetAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdGet200Response> localVarResponse = await ApiV1DemandsIdGetWithHttpInfoAsync(id, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Sözleşme durumu + imza ilerlemesi 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsIdGet200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdGet200Response>> ApiV1DemandsIdGetWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
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

            var localVarResponse = await this.AsynchronousClient.GetAsync<ApiV1DemandsIdGet200Response>("/api/v1/demands/{id}", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Sözleşmeye alan yerleştir (replace) Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |- -- -- -- -- -- --|- -- -- -- -- -- -- -- -- -- --|- -- -- -- -- -- -- -- --| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="upsertItemsRequest"></param>
        /// <returns>UpsertItemsResponse</returns>
        public UpsertItemsResponse ApiV1DemandsIdItemsPost(Guid id, UpsertItemsRequest upsertItemsRequest)
        {
            ImzalaApiClient.Client.ApiResponse<UpsertItemsResponse> localVarResponse = ApiV1DemandsIdItemsPostWithHttpInfo(id, upsertItemsRequest);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Sözleşmeye alan yerleştir (replace) Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |- -- -- -- -- -- --|- -- -- -- -- -- -- -- -- -- --|- -- -- -- -- -- -- -- --| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="upsertItemsRequest"></param>
        /// <returns>ApiResponse of UpsertItemsResponse</returns>
        public ImzalaApiClient.Client.ApiResponse<UpsertItemsResponse> ApiV1DemandsIdItemsPostWithHttpInfo(Guid id, UpsertItemsRequest upsertItemsRequest)
        {
            // verify the required parameter 'upsertItemsRequest' is set
            if (upsertItemsRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'upsertItemsRequest' when calling DemandsApi->ApiV1DemandsIdItemsPost");

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

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter
            localVarRequestOptions.Data = upsertItemsRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Post<UpsertItemsResponse>("/api/v1/demands/{id}/items", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdItemsPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Sözleşmeye alan yerleştir (replace) Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |- -- -- -- -- -- --|- -- -- -- -- -- -- -- -- -- --|- -- -- -- -- -- -- -- --| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="upsertItemsRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of UpsertItemsResponse</returns>
        public async System.Threading.Tasks.Task<UpsertItemsResponse> ApiV1DemandsIdItemsPostAsync(Guid id, UpsertItemsRequest upsertItemsRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<UpsertItemsResponse> localVarResponse = await ApiV1DemandsIdItemsPostWithHttpInfoAsync(id, upsertItemsRequest, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Sözleşmeye alan yerleştir (replace) Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |- -- -- -- -- -- --|- -- -- -- -- -- -- -- -- -- --|- -- -- -- -- -- -- -- --| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="upsertItemsRequest"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (UpsertItemsResponse)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<UpsertItemsResponse>> ApiV1DemandsIdItemsPostWithHttpInfoAsync(Guid id, UpsertItemsRequest upsertItemsRequest, System.Threading.CancellationToken cancellationToken = default)
        {
            // verify the required parameter 'upsertItemsRequest' is set
            if (upsertItemsRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'upsertItemsRequest' when calling DemandsApi->ApiV1DemandsIdItemsPost");


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

            localVarRequestOptions.PathParameters.Add("id", ImzalaApiClient.Client.ClientUtils.ParameterToString(id)); // path parameter
            localVarRequestOptions.Data = upsertItemsRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PostAsync<UpsertItemsResponse>("/api/v1/demands/{id}/items", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdItemsPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Tekil tarafa imza davetini tekrar gönder Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="partyId"></param>
        /// <returns>ApiV1DemandsIdPartiesPartyIdResendPost200Response</returns>
        public ApiV1DemandsIdPartiesPartyIdResendPost200Response ApiV1DemandsIdPartiesPartyIdResendPost(Guid id, Guid partyId)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response> localVarResponse = ApiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo(id, partyId);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Tekil tarafa imza davetini tekrar gönder Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="partyId"></param>
        /// <returns>ApiResponse of ApiV1DemandsIdPartiesPartyIdResendPost200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response> ApiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo(Guid id, Guid partyId)
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
            localVarRequestOptions.PathParameters.Add("partyId", ImzalaApiClient.Client.ClientUtils.ParameterToString(partyId)); // path parameter

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Post<ApiV1DemandsIdPartiesPartyIdResendPost200Response>("/api/v1/demands/{id}/parties/{partyId}/resend", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdPartiesPartyIdResendPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Tekil tarafa imza davetini tekrar gönder Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="partyId"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsIdPartiesPartyIdResendPost200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsIdPartiesPartyIdResendPost200Response> ApiV1DemandsIdPartiesPartyIdResendPostAsync(Guid id, Guid partyId, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response> localVarResponse = await ApiV1DemandsIdPartiesPartyIdResendPostWithHttpInfoAsync(id, partyId, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Tekil tarafa imza davetini tekrar gönder Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="partyId"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsIdPartiesPartyIdResendPost200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response>> ApiV1DemandsIdPartiesPartyIdResendPostWithHttpInfoAsync(Guid id, Guid partyId, System.Threading.CancellationToken cancellationToken = default)
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
            localVarRequestOptions.PathParameters.Add("partyId", ImzalaApiClient.Client.ClientUtils.ParameterToString(partyId)); // path parameter

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PostAsync<ApiV1DemandsIdPartiesPartyIdResendPost200Response>("/api/v1/demands/{id}/parties/{partyId}/resend", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdPartiesPartyIdResendPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme) Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>FileParameter</returns>
        public FileParameter ApiV1DemandsIdPdfGet(Guid id)
        {
            ImzalaApiClient.Client.ApiResponse<FileParameter> localVarResponse = ApiV1DemandsIdPdfGetWithHttpInfo(id);
            return localVarResponse.Data;
        }

        /// <summary>
        /// İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme) Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of FileParameter</returns>
        public ImzalaApiClient.Client.ApiResponse<FileParameter> ApiV1DemandsIdPdfGetWithHttpInfo(Guid id)
        {
            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/pdf",
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
            var localVarResponse = this.Client.Get<FileParameter>("/api/v1/demands/{id}/pdf", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdPdfGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme) Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of FileParameter</returns>
        public async System.Threading.Tasks.Task<FileParameter> ApiV1DemandsIdPdfGetAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<FileParameter> localVarResponse = await ApiV1DemandsIdPdfGetWithHttpInfoAsync(id, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme) Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (FileParameter)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<FileParameter>> ApiV1DemandsIdPdfGetWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
        {

            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
            };

            // to determine the Accept header
            string[] _accepts = new string[] {
                "application/pdf",
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

            var localVarResponse = await this.AsynchronousClient.GetAsync<FileParameter>("/api/v1/demands/{id}/pdf", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdPdfGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// İmza denetim izi (maskeli) Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiV1DemandsIdTimelineGet200Response</returns>
        public ApiV1DemandsIdTimelineGet200Response ApiV1DemandsIdTimelineGet(Guid id)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdTimelineGet200Response> localVarResponse = ApiV1DemandsIdTimelineGetWithHttpInfo(id);
            return localVarResponse.Data;
        }

        /// <summary>
        /// İmza denetim izi (maskeli) Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <returns>ApiResponse of ApiV1DemandsIdTimelineGet200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdTimelineGet200Response> ApiV1DemandsIdTimelineGetWithHttpInfo(Guid id)
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
            var localVarResponse = this.Client.Get<ApiV1DemandsIdTimelineGet200Response>("/api/v1/demands/{id}/timeline", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdTimelineGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// İmza denetim izi (maskeli) Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsIdTimelineGet200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsIdTimelineGet200Response> ApiV1DemandsIdTimelineGetAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdTimelineGet200Response> localVarResponse = await ApiV1DemandsIdTimelineGetWithHttpInfoAsync(id, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// İmza denetim izi (maskeli) Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id"></param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsIdTimelineGet200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsIdTimelineGet200Response>> ApiV1DemandsIdTimelineGetWithHttpInfoAsync(Guid id, System.Threading.CancellationToken cancellationToken = default)
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

            var localVarResponse = await this.AsynchronousClient.GetAsync<ApiV1DemandsIdTimelineGet200Response>("/api/v1/demands/{id}/timeline", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsIdTimelineGet", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Sözleşme oluştur (şablondan) Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="createDemandRequest"></param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <returns>ApiV1DemandsPost201Response</returns>
        public ApiV1DemandsPost201Response ApiV1DemandsPost(CreateDemandRequest createDemandRequest, string? idempotencyKey = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsPost201Response> localVarResponse = ApiV1DemandsPostWithHttpInfo(createDemandRequest, idempotencyKey);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Sözleşme oluştur (şablondan) Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="createDemandRequest"></param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsPost201Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsPost201Response> ApiV1DemandsPostWithHttpInfo(CreateDemandRequest createDemandRequest, string? idempotencyKey = default)
        {
            // verify the required parameter 'createDemandRequest' is set
            if (createDemandRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'createDemandRequest' when calling DemandsApi->ApiV1DemandsPost");

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

            if (idempotencyKey != null)
            {
                localVarRequestOptions.HeaderParameters.Add("Idempotency-Key", ImzalaApiClient.Client.ClientUtils.ParameterToString(idempotencyKey)); // header parameter
            }
            localVarRequestOptions.Data = createDemandRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Post<ApiV1DemandsPost201Response>("/api/v1/demands", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Sözleşme oluştur (şablondan) Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="createDemandRequest"></param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsPost201Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsPost201Response> ApiV1DemandsPostAsync(CreateDemandRequest createDemandRequest, string? idempotencyKey = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsPost201Response> localVarResponse = await ApiV1DemandsPostWithHttpInfoAsync(createDemandRequest, idempotencyKey, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Sözleşme oluştur (şablondan) Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="createDemandRequest"></param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsPost201Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsPost201Response>> ApiV1DemandsPostWithHttpInfoAsync(CreateDemandRequest createDemandRequest, string? idempotencyKey = default, System.Threading.CancellationToken cancellationToken = default)
        {
            // verify the required parameter 'createDemandRequest' is set
            if (createDemandRequest == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'createDemandRequest' when calling DemandsApi->ApiV1DemandsPost");


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

            if (idempotencyKey != null)
            {
                localVarRequestOptions.HeaderParameters.Add("Idempotency-Key", ImzalaApiClient.Client.ClientUtils.ParameterToString(idempotencyKey)); // header parameter
            }
            localVarRequestOptions.Data = createDemandRequest;

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PostAsync<ApiV1DemandsPost201Response>("/api/v1/demands", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Dosya upload ile sözleşme oluştur (şablonsuz) Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="files">1 belge VEYA 1-20 görsel</param>
        /// <param name="parties">JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir. </param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <param name="order">Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)</param>
        /// <param name="title"> (optional)</param>
        /// <param name="description"> (optional)</param>
        /// <param name="fieldTemplateId">Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  (optional)</param>
        /// <param name="force">Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional)</param>
        /// <param name="sendInvitations">&#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Hangi kanalın gideceğini sözleşmenin bildirim ayarları   ve tarafın &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayrakları belirler.   Bu uçtan yaratılan sözleşmelerde ikisi de açık doğar:   pratikte **hem SMS hem e-posta** gider. Kanal seçimi   henüz yoktur. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** bu ucun idempotency   anahtarı yoktur. İstemci zaman aşımında isteği körlemesine   tekrarlarsanız **yeni bir sözleşme, yeni kredi ve ikinci   bir davet seti** oluşur. Zaman aşımında yeniden göndermek   yerine &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  (optional)</param>
        /// <param name="onAnchorMiss">Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  (optional)</param>
        /// <returns>ApiV1DemandsUploadPost201Response</returns>
        public ApiV1DemandsUploadPost201Response ApiV1DemandsUploadPost(List<FileParameter> files, string parties, string? idempotencyKey = default, string? order = default, string? title = default, string? description = default, Guid? fieldTemplateId = default, string? force = default, string? sendInvitations = default, string? onAnchorMiss = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsUploadPost201Response> localVarResponse = ApiV1DemandsUploadPostWithHttpInfo(files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Dosya upload ile sözleşme oluştur (şablonsuz) Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="files">1 belge VEYA 1-20 görsel</param>
        /// <param name="parties">JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir. </param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <param name="order">Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)</param>
        /// <param name="title"> (optional)</param>
        /// <param name="description"> (optional)</param>
        /// <param name="fieldTemplateId">Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  (optional)</param>
        /// <param name="force">Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional)</param>
        /// <param name="sendInvitations">&#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Hangi kanalın gideceğini sözleşmenin bildirim ayarları   ve tarafın &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayrakları belirler.   Bu uçtan yaratılan sözleşmelerde ikisi de açık doğar:   pratikte **hem SMS hem e-posta** gider. Kanal seçimi   henüz yoktur. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** bu ucun idempotency   anahtarı yoktur. İstemci zaman aşımında isteği körlemesine   tekrarlarsanız **yeni bir sözleşme, yeni kredi ve ikinci   bir davet seti** oluşur. Zaman aşımında yeniden göndermek   yerine &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  (optional)</param>
        /// <param name="onAnchorMiss">Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  (optional)</param>
        /// <returns>ApiResponse of ApiV1DemandsUploadPost201Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1DemandsUploadPost201Response> ApiV1DemandsUploadPostWithHttpInfo(List<FileParameter> files, string parties, string? idempotencyKey = default, string? order = default, string? title = default, string? description = default, Guid? fieldTemplateId = default, string? force = default, string? sendInvitations = default, string? onAnchorMiss = default)
        {
            // verify the required parameter 'files' is set
            if (files == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'files' when calling DemandsApi->ApiV1DemandsUploadPost");

            // verify the required parameter 'parties' is set
            if (parties == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'parties' when calling DemandsApi->ApiV1DemandsUploadPost");

            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
                "multipart/form-data"
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
            localVarRequestOptions.FileParameters.Add("files", files);
            if (order != null)
            {
                localVarRequestOptions.FormParameters.Add("order", ImzalaApiClient.Client.ClientUtils.ParameterToString(order)); // form parameter
            }
            if (title != null)
            {
                localVarRequestOptions.FormParameters.Add("title", ImzalaApiClient.Client.ClientUtils.ParameterToString(title)); // form parameter
            }
            if (description != null)
            {
                localVarRequestOptions.FormParameters.Add("description", ImzalaApiClient.Client.ClientUtils.ParameterToString(description)); // form parameter
            }
            localVarRequestOptions.FormParameters.Add("parties", ImzalaApiClient.Client.ClientUtils.ParameterToString(parties)); // form parameter
            if (fieldTemplateId != null)
            {
                localVarRequestOptions.FormParameters.Add("field_template_id", ImzalaApiClient.Client.ClientUtils.ParameterToString(fieldTemplateId)); // form parameter
            }
            if (force != null)
            {
                localVarRequestOptions.FormParameters.Add("force", ImzalaApiClient.Client.ClientUtils.ParameterToString(force)); // form parameter
            }
            if (sendInvitations != null)
            {
                localVarRequestOptions.FormParameters.Add("send_invitations", ImzalaApiClient.Client.ClientUtils.ParameterToString(sendInvitations)); // form parameter
            }
            if (onAnchorMiss != null)
            {
                localVarRequestOptions.FormParameters.Add("on_anchor_miss", ImzalaApiClient.Client.ClientUtils.ParameterToString(onAnchorMiss)); // form parameter
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Post<ApiV1DemandsUploadPost201Response>("/api/v1/demands/upload", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsUploadPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Dosya upload ile sözleşme oluştur (şablonsuz) Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="files">1 belge VEYA 1-20 görsel</param>
        /// <param name="parties">JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir. </param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <param name="order">Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)</param>
        /// <param name="title"> (optional)</param>
        /// <param name="description"> (optional)</param>
        /// <param name="fieldTemplateId">Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  (optional)</param>
        /// <param name="force">Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional)</param>
        /// <param name="sendInvitations">&#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Hangi kanalın gideceğini sözleşmenin bildirim ayarları   ve tarafın &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayrakları belirler.   Bu uçtan yaratılan sözleşmelerde ikisi de açık doğar:   pratikte **hem SMS hem e-posta** gider. Kanal seçimi   henüz yoktur. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** bu ucun idempotency   anahtarı yoktur. İstemci zaman aşımında isteği körlemesine   tekrarlarsanız **yeni bir sözleşme, yeni kredi ve ikinci   bir davet seti** oluşur. Zaman aşımında yeniden göndermek   yerine &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  (optional)</param>
        /// <param name="onAnchorMiss">Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1DemandsUploadPost201Response</returns>
        public async System.Threading.Tasks.Task<ApiV1DemandsUploadPost201Response> ApiV1DemandsUploadPostAsync(List<FileParameter> files, string parties, string? idempotencyKey = default, string? order = default, string? title = default, string? description = default, Guid? fieldTemplateId = default, string? force = default, string? sendInvitations = default, string? onAnchorMiss = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1DemandsUploadPost201Response> localVarResponse = await ApiV1DemandsUploadPostWithHttpInfoAsync(files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Dosya upload ile sözleşme oluştur (şablonsuz) Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="files">1 belge VEYA 1-20 görsel</param>
        /// <param name="parties">JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir. </param>
        /// <param name="idempotencyKey">Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)</param>
        /// <param name="order">Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)</param>
        /// <param name="title"> (optional)</param>
        /// <param name="description"> (optional)</param>
        /// <param name="fieldTemplateId">Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  (optional)</param>
        /// <param name="force">Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional)</param>
        /// <param name="sendInvitations">&#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Hangi kanalın gideceğini sözleşmenin bildirim ayarları   ve tarafın &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayrakları belirler.   Bu uçtan yaratılan sözleşmelerde ikisi de açık doğar:   pratikte **hem SMS hem e-posta** gider. Kanal seçimi   henüz yoktur. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** bu ucun idempotency   anahtarı yoktur. İstemci zaman aşımında isteği körlemesine   tekrarlarsanız **yeni bir sözleşme, yeni kredi ve ikinci   bir davet seti** oluşur. Zaman aşımında yeniden göndermek   yerine &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  (optional)</param>
        /// <param name="onAnchorMiss">Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1DemandsUploadPost201Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1DemandsUploadPost201Response>> ApiV1DemandsUploadPostWithHttpInfoAsync(List<FileParameter> files, string parties, string? idempotencyKey = default, string? order = default, string? title = default, string? description = default, Guid? fieldTemplateId = default, string? force = default, string? sendInvitations = default, string? onAnchorMiss = default, System.Threading.CancellationToken cancellationToken = default)
        {
            // verify the required parameter 'files' is set
            if (files == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'files' when calling DemandsApi->ApiV1DemandsUploadPost");

            // verify the required parameter 'parties' is set
            if (parties == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'parties' when calling DemandsApi->ApiV1DemandsUploadPost");


            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
                "multipart/form-data"
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
            localVarRequestOptions.FileParameters.Add("files", files);
            if (order != null)
            {
                localVarRequestOptions.FormParameters.Add("order", ImzalaApiClient.Client.ClientUtils.ParameterToString(order)); // form parameter
            }
            if (title != null)
            {
                localVarRequestOptions.FormParameters.Add("title", ImzalaApiClient.Client.ClientUtils.ParameterToString(title)); // form parameter
            }
            if (description != null)
            {
                localVarRequestOptions.FormParameters.Add("description", ImzalaApiClient.Client.ClientUtils.ParameterToString(description)); // form parameter
            }
            localVarRequestOptions.FormParameters.Add("parties", ImzalaApiClient.Client.ClientUtils.ParameterToString(parties)); // form parameter
            if (fieldTemplateId != null)
            {
                localVarRequestOptions.FormParameters.Add("field_template_id", ImzalaApiClient.Client.ClientUtils.ParameterToString(fieldTemplateId)); // form parameter
            }
            if (force != null)
            {
                localVarRequestOptions.FormParameters.Add("force", ImzalaApiClient.Client.ClientUtils.ParameterToString(force)); // form parameter
            }
            if (sendInvitations != null)
            {
                localVarRequestOptions.FormParameters.Add("send_invitations", ImzalaApiClient.Client.ClientUtils.ParameterToString(sendInvitations)); // form parameter
            }
            if (onAnchorMiss != null)
            {
                localVarRequestOptions.FormParameters.Add("on_anchor_miss", ImzalaApiClient.Client.ClientUtils.ParameterToString(onAnchorMiss)); // form parameter
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PostAsync<ApiV1DemandsUploadPost201Response>("/api/v1/demands/upload", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1DemandsUploadPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Alan Şablonu (FIELD_LAYOUT) kimliği</param>
        /// <param name="files">Tek PDF belge</param>
        /// <param name="onAnchorMiss">&#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  (optional)</param>
        /// <returns>ApiV1FieldTemplatesIdPreviewLayoutPost200Response</returns>
        public ApiV1FieldTemplatesIdPreviewLayoutPost200Response ApiV1FieldTemplatesIdPreviewLayoutPost(Guid id, List<FileParameter> files, string? onAnchorMiss = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response> localVarResponse = ApiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo(id, files, onAnchorMiss);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Alan Şablonu (FIELD_LAYOUT) kimliği</param>
        /// <param name="files">Tek PDF belge</param>
        /// <param name="onAnchorMiss">&#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  (optional)</param>
        /// <returns>ApiResponse of ApiV1FieldTemplatesIdPreviewLayoutPost200Response</returns>
        public ImzalaApiClient.Client.ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response> ApiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo(Guid id, List<FileParameter> files, string? onAnchorMiss = default)
        {
            // verify the required parameter 'files' is set
            if (files == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'files' when calling DemandsApi->ApiV1FieldTemplatesIdPreviewLayoutPost");

            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
                "multipart/form-data"
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
            localVarRequestOptions.FileParameters.Add("files", files);
            if (onAnchorMiss != null)
            {
                localVarRequestOptions.FormParameters.Add("on_anchor_miss", ImzalaApiClient.Client.ClientUtils.ParameterToString(onAnchorMiss)); // form parameter
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request
            var localVarResponse = this.Client.Post<ApiV1FieldTemplatesIdPreviewLayoutPost200Response>("/api/v1/field-templates/{id}/preview-layout", localVarRequestOptions, this.Configuration);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1FieldTemplatesIdPreviewLayoutPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

        /// <summary>
        /// Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Alan Şablonu (FIELD_LAYOUT) kimliği</param>
        /// <param name="files">Tek PDF belge</param>
        /// <param name="onAnchorMiss">&#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiV1FieldTemplatesIdPreviewLayoutPost200Response</returns>
        public async System.Threading.Tasks.Task<ApiV1FieldTemplatesIdPreviewLayoutPost200Response> ApiV1FieldTemplatesIdPreviewLayoutPostAsync(Guid id, List<FileParameter> files, string? onAnchorMiss = default, System.Threading.CancellationToken cancellationToken = default)
        {
            ImzalaApiClient.Client.ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response> localVarResponse = await ApiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfoAsync(id, files, onAnchorMiss, cancellationToken).ConfigureAwait(false);
            return localVarResponse.Data;
        }

        /// <summary>
        /// Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 
        /// </summary>
        /// <exception cref="ImzalaApiClient.Client.ApiException">Thrown when fails to make API call</exception>
        /// <param name="id">Alan Şablonu (FIELD_LAYOUT) kimliği</param>
        /// <param name="files">Tek PDF belge</param>
        /// <param name="onAnchorMiss">&#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  (optional)</param>
        /// <param name="cancellationToken">Cancellation Token to cancel the request.</param>
        /// <returns>Task of ApiResponse (ApiV1FieldTemplatesIdPreviewLayoutPost200Response)</returns>
        public async System.Threading.Tasks.Task<ImzalaApiClient.Client.ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response>> ApiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfoAsync(Guid id, List<FileParameter> files, string? onAnchorMiss = default, System.Threading.CancellationToken cancellationToken = default)
        {
            // verify the required parameter 'files' is set
            if (files == null)
                throw new ImzalaApiClient.Client.ApiException(400, "Missing required parameter 'files' when calling DemandsApi->ApiV1FieldTemplatesIdPreviewLayoutPost");


            ImzalaApiClient.Client.RequestOptions localVarRequestOptions = new ImzalaApiClient.Client.RequestOptions();

            string[] _contentTypes = new string[] {
                "multipart/form-data"
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
            localVarRequestOptions.FileParameters.Add("files", files);
            if (onAnchorMiss != null)
            {
                localVarRequestOptions.FormParameters.Add("on_anchor_miss", ImzalaApiClient.Client.ClientUtils.ParameterToString(onAnchorMiss)); // form parameter
            }

            // authentication (ApiKeyAuth) required
            if (!string.IsNullOrEmpty(this.Configuration.GetApiKeyWithPrefix("X-API-Key")))
            {
                localVarRequestOptions.HeaderParameters.Add("X-API-Key", this.Configuration.GetApiKeyWithPrefix("X-API-Key"));
            }

            // make the HTTP request

            var localVarResponse = await this.AsynchronousClient.PostAsync<ApiV1FieldTemplatesIdPreviewLayoutPost200Response>("/api/v1/field-templates/{id}/preview-layout", localVarRequestOptions, this.Configuration, cancellationToken).ConfigureAwait(false);

            if (this.ExceptionFactory != null)
            {
                Exception _exception = this.ExceptionFactory("ApiV1FieldTemplatesIdPreviewLayoutPost", localVarResponse);
                if (_exception != null) throw _exception;
            }

            return localVarResponse;
        }

    }
}
