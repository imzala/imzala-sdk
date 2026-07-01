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


package org.imzala.client.generated.api;

import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.model.ApiError;
import org.imzala.client.generated.model.ApiV1DemandsIdEmbedSessionPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdEmbedSessionPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsIdGet200Response;
import org.imzala.client.generated.model.ApiV1DemandsPost201Response;
import org.imzala.client.generated.model.ApiV1DemandsUploadPost201Response;
import org.imzala.client.generated.model.ApiV1TemplatesGet401Response;
import org.imzala.client.generated.model.ApiV1TemplatesIdGet404Response;
import org.imzala.client.generated.model.CreateDemandRequest;
import java.io.File;
import java.util.UUID;
import org.imzala.client.generated.model.UpsertItemsRequest;
import org.imzala.client.generated.model.UpsertItemsResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * API tests for DemandsApi
 */
@Disabled
public class DemandsApiTest {

    private final DemandsApi api = new DemandsApi();

    
    /**
     * Gömülü imza oturumu başlat (embed token mint)
     *
     * Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \&quot;güvenli\&quot; veya \&quot;nitelikli\&quot; sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). 
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void apiV1DemandsIdEmbedSessionPostTest() throws ApiException {
        UUID id = null;
        ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest = null;
        ApiV1DemandsIdEmbedSessionPost200Response response = 
        api.apiV1DemandsIdEmbedSessionPost(id, apiV1DemandsIdEmbedSessionPostRequest);
        
        // TODO: test validations
    }
    
    /**
     * Sözleşme durumu + imza ilerlemesi
     *
     * 
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void apiV1DemandsIdGetTest() throws ApiException {
        UUID id = null;
        ApiV1DemandsIdGet200Response response = 
        api.apiV1DemandsIdGet(id);
        
        // TODO: test validations
    }
    
    /**
     * Sözleşmeye alan yerleştir (replace)
     *
     * Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |-------------|---------------------|-----------------| | &#x60;signature&#x60; | ✅ | (yok) | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | ❌ | &#x60;{ stampData: \&quot;data:image/png;base64,...\&quot; }&#x60; |  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void apiV1DemandsIdItemsPostTest() throws ApiException {
        UUID id = null;
        UpsertItemsRequest upsertItemsRequest = null;
        UpsertItemsResponse response = 
        api.apiV1DemandsIdItemsPost(id, upsertItemsRequest);
        
        // TODO: test validations
    }
    
    /**
     * Sözleşme oluştur (şablondan)
     *
     * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı 
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void apiV1DemandsPostTest() throws ApiException {
        CreateDemandRequest createDemandRequest = null;
        ApiV1DemandsPost201Response response = 
        api.apiV1DemandsPost(createDemandRequest);
        
        // TODO: test validations
    }
    
    /**
     * Dosya upload ile sözleşme oluştur (şablonsuz)
     *
     * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void apiV1DemandsUploadPostTest() throws ApiException {
        List<File> files = null;
        String parties = null;
        String order = null;
        String title = null;
        String description = null;
        ApiV1DemandsUploadPost201Response response = 
        api.apiV1DemandsUploadPost(files, parties, order, title, description);
        
        // TODO: test validations
    }
    
}
