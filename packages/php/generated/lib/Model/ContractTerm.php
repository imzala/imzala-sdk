<?php
/**
 * ContractTerm
 *
 * PHP version 8.1
 *
 * @category Class
 * @package  Imzala\Client
 * @author   OpenAPI Generator team
 * @link     https://openapi-generator.tech
 */

/**
 * imzala External API
 *
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.10.2 · **Son güncelleme:** 2026-10-06  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır (aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Onaylayan (onay adımı) Şablonda bir taraf **Onaylayan** olarak işaretlenmişse, o şablondan oluşturulan sözleşmede bu taraf belgeyi imzalamaz, onaylar. Onaylayan içeren sözleşmelerde davetler iki aşamada gönderilir: - Sözleşme oluşturulurken (`POST /api/v1/demands`,   `POST /api/v1/demands/bulk`) ve `POST /api/v1/demands/{demandId}/dispatch`   çağrıldığında önce yalnızca onay bekleyen onaylayanlar davet edilir.   Sıralı sözleşmede (`ordered: true`) onaylayanlar da kendi sıralarıyla   davet edilir. - İmzacılar, tüm onaylayanlar onayladıktan sonra otomatik olarak davet   edilir. - Bir onaylayan sözleşmeyi reddederse imzacılar davet edilmez;   `dispatch` çağrısı da kimseye davet göndermez. - Onay vermiş bir onaylayan `dispatch` ile yeniden davet edilmez.  Onaylayan içermeyen sözleşmelerde davet davranışı değişmez.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  `403 AI_TOOL_ACCESS_DISABLED`: organizasyon yöneticisi, organizasyonun yapay zekâ araçlarıyla erişimini kapatmıştır. Bu kontrol resmî İmzala MCP istemcisini (`User-Agent` başlığı `imzala-mcp/` ile başlayan istekler) tanır ve yalnız organizasyon çalışma alanını hedefleyen isteklerde uygulanır. Yanıt gövdesi:  ```json {   \"error\": \"AI_TOOL_ACCESS_DISABLED\",   \"code\": \"AI_TOOL_ACCESS_DISABLED\",   \"message\": \"Kurumunuz yapay zekâ araçlarının erişimini kapattı.\" } ```  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez. - Hatırlatma yalnızca davet sırası gelmiş taraflara gönderilir. Onaylayan   onayını beklerken imzacılar, sıralı sözleşmede sırası henüz gelmemiş   taraflar ve bir onaylayan reddettiyse tüm taraflar `details[]` içinde   `skipped` olarak `reason: \"party_not_eligible\"` ile döner.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (11) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı | | `contract.expiring` | Takip edilen sözleşme bitiş tarihine yaklaşıyor | | `contract.ended` | Takip edilen sözleşmenin platformdaki bitiş tarihi geçti | | `contract.advanced` | Otomatik yenilenen sözleşmenin takip edilen bitişi ileri alındı |  > **Not (contract.\\* olayları):** Bunlar platformun **takip amaçlı** > kayıtlarıdır; sözleşmenin hukuken yenilendiğini veya sona erdiğini > BELİRTMEZ. Detay için `ContractTerm` şemasına ve `state` alanına bakın.  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type`: yukarıdaki 11 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme.
 *
 * The version of the OpenAPI document: 1.10.2
 * Contact: destek@imzala.org
 * Generated by: https://openapi-generator.tech
 * Generator version: 7.23.0
 */

/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

namespace Imzala\Client\Model;

use \ArrayAccess;
use \Imzala\Client\ObjectSerializer;

/**
 * ContractTerm Class Doc Comment
 *
 * @category Class
 * @description Sözleşme süre/yenileme takibi. Sözleşmede takip hiç tanımlanmadıysa (&#x60;start_mode&#x60; VE &#x60;end_date&#x60; boşsa) bu alan &#x60;null&#x60; döner.  Bu, hukuki bir yenileme veya sona erme beyanı değildir; platformun takip amaçlı kaydıdır. Sözleşmenin fiilen ne zaman sona erdiği veya yenilendiği, tarafların kendi sözleşme hükümlerine ve yürürlükteki hukuka tabidir. Bu alan yalnız hatırlatma ve görünürlük sağlar.
 * @package  Imzala\Client
 * @author   OpenAPI Generator team
 * @link     https://openapi-generator.tech
 * @implements \ArrayAccess<string, mixed>
 */
class ContractTerm implements ModelInterface, ArrayAccess, \JsonSerializable
{
    public const DISCRIMINATOR = null;

    /**
     * The original name of the model.
     *
     * @var string
     */
    protected static $openAPIModelName = 'ContractTerm';

    /**
     * Array of property to type mappings. Used for (de)serialization
     *
     * @var string[]
     */
    protected static $openAPITypes = [
        'start_mode' => 'string',
        'start_date' => '\DateTime',
        'duration_months' => 'int',
        'fixed_end_date' => '\DateTime',
        'end_date' => '\DateTime',
        'end_date_signed' => '\DateTime',
        'renewal_type' => 'string',
        'renewal_period_months' => 'int',
        'notice_days' => 'int',
        'notice_deadline' => '\DateTime',
        'reminder_offsets' => 'int[]',
        'notify_counterparty' => 'bool',
        'state' => 'string',
        'days_left' => 'int',
        'renewal_stopped_at' => '\DateTime'
    ];

    /**
     * Array of property to format mappings. Used for (de)serialization
     *
     * @var string[]
     * @phpstan-var array<string, string|null>
     * @psalm-var array<string, string|null>
     */
    protected static $openAPIFormats = [
        'start_mode' => null,
        'start_date' => 'date',
        'duration_months' => null,
        'fixed_end_date' => 'date',
        'end_date' => 'date',
        'end_date_signed' => 'date',
        'renewal_type' => null,
        'renewal_period_months' => null,
        'notice_days' => null,
        'notice_deadline' => 'date',
        'reminder_offsets' => null,
        'notify_counterparty' => null,
        'state' => null,
        'days_left' => null,
        'renewal_stopped_at' => 'date-time'
    ];

    /**
     * Array of nullable properties. Used for (de)serialization
     *
     * @var boolean[]
     */
    protected static array $openAPINullables = [
        'start_mode' => true,
        'start_date' => true,
        'duration_months' => true,
        'fixed_end_date' => true,
        'end_date' => true,
        'end_date_signed' => true,
        'renewal_type' => true,
        'renewal_period_months' => true,
        'notice_days' => true,
        'notice_deadline' => true,
        'reminder_offsets' => true,
        'notify_counterparty' => false,
        'state' => false,
        'days_left' => true,
        'renewal_stopped_at' => true
    ];

    /**
     * If a nullable field gets set to null, insert it here
     *
     * @var boolean[]
     */
    protected array $openAPINullablesSetToNull = [];

    /**
     * Array of property to type mappings. Used for (de)serialization
     *
     * @return array
     */
    public static function openAPITypes()
    {
        return self::$openAPITypes;
    }

    /**
     * Array of property to format mappings. Used for (de)serialization
     *
     * @return array
     */
    public static function openAPIFormats()
    {
        return self::$openAPIFormats;
    }

    /**
     * Array of nullable properties
     *
     * @return array
     */
    protected static function openAPINullables(): array
    {
        return self::$openAPINullables;
    }

    /**
     * Array of nullable field names deliberately set to null
     *
     * @return boolean[]
     */
    private function getOpenAPINullablesSetToNull(): array
    {
        return $this->openAPINullablesSetToNull;
    }

    /**
     * Setter - Array of nullable field names deliberately set to null
     *
     * @param boolean[] $openAPINullablesSetToNull
     */
    private function setOpenAPINullablesSetToNull(array $openAPINullablesSetToNull): void
    {
        $this->openAPINullablesSetToNull = $openAPINullablesSetToNull;
    }

    /**
     * Checks if a property is nullable
     *
     * @param string $property
     * @return bool
     */
    public static function isNullable(string $property): bool
    {
        return self::openAPINullables()[$property] ?? false;
    }

    /**
     * Checks if a nullable property is set to null.
     *
     * @param string $property
     * @return bool
     */
    public function isNullableSetToNull(string $property): bool
    {
        return in_array($property, $this->getOpenAPINullablesSetToNull(), true);
    }

    /**
     * Array of attributes where the key is the local name,
     * and the value is the original name
     *
     * @var string[]
     */
    protected static $attributeMap = [
        'start_mode' => 'start_mode',
        'start_date' => 'start_date',
        'duration_months' => 'duration_months',
        'fixed_end_date' => 'fixed_end_date',
        'end_date' => 'end_date',
        'end_date_signed' => 'end_date_signed',
        'renewal_type' => 'renewal_type',
        'renewal_period_months' => 'renewal_period_months',
        'notice_days' => 'notice_days',
        'notice_deadline' => 'notice_deadline',
        'reminder_offsets' => 'reminder_offsets',
        'notify_counterparty' => 'notify_counterparty',
        'state' => 'state',
        'days_left' => 'days_left',
        'renewal_stopped_at' => 'renewal_stopped_at'
    ];

    /**
     * Array of attributes to setter functions (for deserialization of responses)
     *
     * @var string[]
     */
    protected static $setters = [
        'start_mode' => 'setStartMode',
        'start_date' => 'setStartDate',
        'duration_months' => 'setDurationMonths',
        'fixed_end_date' => 'setFixedEndDate',
        'end_date' => 'setEndDate',
        'end_date_signed' => 'setEndDateSigned',
        'renewal_type' => 'setRenewalType',
        'renewal_period_months' => 'setRenewalPeriodMonths',
        'notice_days' => 'setNoticeDays',
        'notice_deadline' => 'setNoticeDeadline',
        'reminder_offsets' => 'setReminderOffsets',
        'notify_counterparty' => 'setNotifyCounterparty',
        'state' => 'setState',
        'days_left' => 'setDaysLeft',
        'renewal_stopped_at' => 'setRenewalStoppedAt'
    ];

    /**
     * Array of attributes to getter functions (for serialization of requests)
     *
     * @var string[]
     */
    protected static $getters = [
        'start_mode' => 'getStartMode',
        'start_date' => 'getStartDate',
        'duration_months' => 'getDurationMonths',
        'fixed_end_date' => 'getFixedEndDate',
        'end_date' => 'getEndDate',
        'end_date_signed' => 'getEndDateSigned',
        'renewal_type' => 'getRenewalType',
        'renewal_period_months' => 'getRenewalPeriodMonths',
        'notice_days' => 'getNoticeDays',
        'notice_deadline' => 'getNoticeDeadline',
        'reminder_offsets' => 'getReminderOffsets',
        'notify_counterparty' => 'getNotifyCounterparty',
        'state' => 'getState',
        'days_left' => 'getDaysLeft',
        'renewal_stopped_at' => 'getRenewalStoppedAt'
    ];

    /**
     * Array of attributes where the key is the local name,
     * and the value is the original name
     *
     * @return array
     */
    public static function attributeMap()
    {
        return self::$attributeMap;
    }

    /**
     * Array of attributes to setter functions (for deserialization of responses)
     *
     * @return array
     */
    public static function setters()
    {
        return self::$setters;
    }

    /**
     * Array of attributes to getter functions (for serialization of requests)
     *
     * @return array
     */
    public static function getters()
    {
        return self::$getters;
    }

    /**
     * The original name of the model.
     *
     * @return string
     */
    public function getModelName()
    {
        return self::$openAPIModelName;
    }

    public const START_MODE_FIXED_DATE = 'FIXED_DATE';
    public const START_MODE_ON_FIRST_SIGNATURE = 'ON_FIRST_SIGNATURE';
    public const START_MODE_ON_COMPLETION = 'ON_COMPLETION';
    public const RENEWAL_TYPE_AUTO_RENEW = 'AUTO_RENEW';
    public const RENEWAL_TYPE_FIXED_TERM = 'FIXED_TERM';
    public const STATE_UNTRACKED = 'UNTRACKED';
    public const STATE_ACTIVE = 'ACTIVE';
    public const STATE_ENDED = 'ENDED';
    public const STATE_STOPPED = 'STOPPED';

    /**
     * Gets allowable values of the enum
     *
     * @return string[]
     */
    public function getStartModeAllowableValues()
    {
        return [
            self::START_MODE_FIXED_DATE,
            self::START_MODE_ON_FIRST_SIGNATURE,
            self::START_MODE_ON_COMPLETION,
        ];
    }

    /**
     * Gets allowable values of the enum
     *
     * @return string[]
     */
    public function getRenewalTypeAllowableValues()
    {
        return [
            self::RENEWAL_TYPE_AUTO_RENEW,
            self::RENEWAL_TYPE_FIXED_TERM,
        ];
    }

    /**
     * Gets allowable values of the enum
     *
     * @return string[]
     */
    public function getStateAllowableValues()
    {
        return [
            self::STATE_UNTRACKED,
            self::STATE_ACTIVE,
            self::STATE_ENDED,
            self::STATE_STOPPED,
        ];
    }

    /**
     * Associative array for storing property values
     *
     * @var mixed[]
     */
    protected $container = [];

    /**
     * Constructor
     *
     * @param mixed[]|null $data Associated array of property values
     *                      initializing the model
     */
    public function __construct(?array $data = null)
    {
        $this->setIfExists('start_mode', $data ?? [], null);
        $this->setIfExists('start_date', $data ?? [], null);
        $this->setIfExists('duration_months', $data ?? [], null);
        $this->setIfExists('fixed_end_date', $data ?? [], null);
        $this->setIfExists('end_date', $data ?? [], null);
        $this->setIfExists('end_date_signed', $data ?? [], null);
        $this->setIfExists('renewal_type', $data ?? [], null);
        $this->setIfExists('renewal_period_months', $data ?? [], null);
        $this->setIfExists('notice_days', $data ?? [], null);
        $this->setIfExists('notice_deadline', $data ?? [], null);
        $this->setIfExists('reminder_offsets', $data ?? [], null);
        $this->setIfExists('notify_counterparty', $data ?? [], null);
        $this->setIfExists('state', $data ?? [], null);
        $this->setIfExists('days_left', $data ?? [], null);
        $this->setIfExists('renewal_stopped_at', $data ?? [], null);
    }

    /**
     * Sets $this->container[$variableName] to the given data or to the given default Value; if $variableName
     * is nullable and its value is set to null in the $fields array, then mark it as "set to null" in the
     * $this->openAPINullablesSetToNull array
     *
     * @param string $variableName
     * @param array  $fields
     * @param mixed  $defaultValue
     */
    private function setIfExists(string $variableName, array $fields, $defaultValue): void
    {
        if (self::isNullable($variableName) && array_key_exists($variableName, $fields) && is_null($fields[$variableName])) {
            $this->openAPINullablesSetToNull[] = $variableName;
        }

        $this->container[$variableName] = $fields[$variableName] ?? $defaultValue;
    }

    /**
     * Show all the invalid properties with reasons.
     *
     * @return array invalid properties with reasons
     */
    public function listInvalidProperties()
    {
        $invalidProperties = [];

        if ($this->container['start_mode'] === null && !$this->isNullableSetToNull('start_mode')) {
            $invalidProperties[] = "'start_mode' is required";
        }
        $allowedValues = $this->getStartModeAllowableValues();
        if (!is_null($this->container['start_mode']) && !in_array($this->container['start_mode'], $allowedValues, true)) {
            $invalidProperties[] = sprintf(
                "invalid value '%s' for 'start_mode', must be one of '%s'",
                $this->container['start_mode'],
                implode("', '", $allowedValues)
            );
        }

        if ($this->container['start_date'] === null && !$this->isNullableSetToNull('start_date')) {
            $invalidProperties[] = "'start_date' is required";
        }
        if ($this->container['duration_months'] === null && !$this->isNullableSetToNull('duration_months')) {
            $invalidProperties[] = "'duration_months' is required";
        }
        if (!is_null($this->container['duration_months']) && ($this->container['duration_months'] > 600)) {
            $invalidProperties[] = "invalid value for 'duration_months', must be smaller than or equal to 600.";
        }

        if (!is_null($this->container['duration_months']) && ($this->container['duration_months'] < 1)) {
            $invalidProperties[] = "invalid value for 'duration_months', must be bigger than or equal to 1.";
        }

        if ($this->container['fixed_end_date'] === null && !$this->isNullableSetToNull('fixed_end_date')) {
            $invalidProperties[] = "'fixed_end_date' is required";
        }
        if ($this->container['end_date'] === null && !$this->isNullableSetToNull('end_date')) {
            $invalidProperties[] = "'end_date' is required";
        }
        if ($this->container['end_date_signed'] === null && !$this->isNullableSetToNull('end_date_signed')) {
            $invalidProperties[] = "'end_date_signed' is required";
        }
        if ($this->container['renewal_type'] === null && !$this->isNullableSetToNull('renewal_type')) {
            $invalidProperties[] = "'renewal_type' is required";
        }
        $allowedValues = $this->getRenewalTypeAllowableValues();
        if (!is_null($this->container['renewal_type']) && !in_array($this->container['renewal_type'], $allowedValues, true)) {
            $invalidProperties[] = sprintf(
                "invalid value '%s' for 'renewal_type', must be one of '%s'",
                $this->container['renewal_type'],
                implode("', '", $allowedValues)
            );
        }

        if ($this->container['renewal_period_months'] === null && !$this->isNullableSetToNull('renewal_period_months')) {
            $invalidProperties[] = "'renewal_period_months' is required";
        }
        if (!is_null($this->container['renewal_period_months']) && ($this->container['renewal_period_months'] > 600)) {
            $invalidProperties[] = "invalid value for 'renewal_period_months', must be smaller than or equal to 600.";
        }

        if (!is_null($this->container['renewal_period_months']) && ($this->container['renewal_period_months'] < 1)) {
            $invalidProperties[] = "invalid value for 'renewal_period_months', must be bigger than or equal to 1.";
        }

        if ($this->container['notice_days'] === null && !$this->isNullableSetToNull('notice_days')) {
            $invalidProperties[] = "'notice_days' is required";
        }
        if (!is_null($this->container['notice_days']) && ($this->container['notice_days'] > 3650)) {
            $invalidProperties[] = "invalid value for 'notice_days', must be smaller than or equal to 3650.";
        }

        if (!is_null($this->container['notice_days']) && ($this->container['notice_days'] < 0)) {
            $invalidProperties[] = "invalid value for 'notice_days', must be bigger than or equal to 0.";
        }

        if ($this->container['notice_deadline'] === null && !$this->isNullableSetToNull('notice_deadline')) {
            $invalidProperties[] = "'notice_deadline' is required";
        }
        if ($this->container['reminder_offsets'] === null && !$this->isNullableSetToNull('reminder_offsets')) {
            $invalidProperties[] = "'reminder_offsets' is required";
        }
        if (!is_null($this->container['reminder_offsets']) && (count($this->container['reminder_offsets']) > 5)) {
            $invalidProperties[] = "invalid value for 'reminder_offsets', number of items must be less than or equal to 5.";
        }

        if ($this->container['notify_counterparty'] === null) {
            $invalidProperties[] = "'notify_counterparty' can't be null";
        }
        if ($this->container['state'] === null) {
            $invalidProperties[] = "'state' can't be null";
        }
        $allowedValues = $this->getStateAllowableValues();
        if (!is_null($this->container['state']) && !in_array($this->container['state'], $allowedValues, true)) {
            $invalidProperties[] = sprintf(
                "invalid value '%s' for 'state', must be one of '%s'",
                $this->container['state'],
                implode("', '", $allowedValues)
            );
        }

        if ($this->container['days_left'] === null && !$this->isNullableSetToNull('days_left')) {
            $invalidProperties[] = "'days_left' is required";
        }
        if ($this->container['renewal_stopped_at'] === null && !$this->isNullableSetToNull('renewal_stopped_at')) {
            $invalidProperties[] = "'renewal_stopped_at' is required";
        }
        return $invalidProperties;
    }

    /**
     * Validate all the properties in the model
     * return true if all passed
     *
     * @return bool True if all properties are valid
     */
    public function valid()
    {
        return count($this->listInvalidProperties()) === 0;
    }


    /**
     * Gets start_mode
     *
     * @return string|null
     */
    public function getStartMode()
    {
        return $this->container['start_mode'];
    }

    /**
     * Sets start_mode
     *
     * @param string|null $start_mode Takibin ne zaman başladığı. `FIXED_DATE`: `start_date`'ten. `ON_FIRST_SIGNATURE` / `ON_COMPLETION`: ilk imza / tüm imzalar tamamlandığı anda otomatik (o ana kadar `end_date` bilinmez).
     *
     * @return self
     */
    public function setStartMode($start_mode)
    {
        if (is_null($start_mode)) {
            array_push($this->openAPINullablesSetToNull, 'start_mode');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('start_mode', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        $allowedValues = $this->getStartModeAllowableValues();
        if (!is_null($start_mode) && !in_array($start_mode, $allowedValues, true)) {
            throw new \InvalidArgumentException(
                sprintf(
                    "Invalid value '%s' for 'start_mode', must be one of '%s'",
                    $start_mode,
                    implode("', '", $allowedValues)
                )
            );
        }
        $this->container['start_mode'] = $start_mode;

        return $this;
    }

    /**
     * Gets start_date
     *
     * @return \DateTime|null
     */
    public function getStartDate()
    {
        return $this->container['start_date'];
    }

    /**
     * Sets start_date
     *
     * @param \DateTime|null $start_date start_date
     *
     * @return self
     */
    public function setStartDate($start_date)
    {
        if (is_null($start_date)) {
            array_push($this->openAPINullablesSetToNull, 'start_date');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('start_date', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        $this->container['start_date'] = $start_date;

        return $this;
    }

    /**
     * Gets duration_months
     *
     * @return int|null
     */
    public function getDurationMonths()
    {
        return $this->container['duration_months'];
    }

    /**
     * Sets duration_months
     *
     * @param int|null $duration_months duration_months
     *
     * @return self
     */
    public function setDurationMonths($duration_months)
    {
        if (is_null($duration_months)) {
            array_push($this->openAPINullablesSetToNull, 'duration_months');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('duration_months', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }

        if (!is_null($duration_months) && ($duration_months > 600)) {
            throw new \InvalidArgumentException('invalid value for $duration_months when calling ContractTerm., must be smaller than or equal to 600.');
        }
        if (!is_null($duration_months) && ($duration_months < 1)) {
            throw new \InvalidArgumentException('invalid value for $duration_months when calling ContractTerm., must be bigger than or equal to 1.');
        }

        $this->container['duration_months'] = $duration_months;

        return $this;
    }

    /**
     * Gets fixed_end_date
     *
     * @return \DateTime|null
     */
    public function getFixedEndDate()
    {
        return $this->container['fixed_end_date'];
    }

    /**
     * Sets fixed_end_date
     *
     * @param \DateTime|null $fixed_end_date fixed_end_date
     *
     * @return self
     */
    public function setFixedEndDate($fixed_end_date)
    {
        if (is_null($fixed_end_date)) {
            array_push($this->openAPINullablesSetToNull, 'fixed_end_date');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('fixed_end_date', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        $this->container['fixed_end_date'] = $fixed_end_date;

        return $this;
    }

    /**
     * Gets end_date
     *
     * @return \DateTime|null
     */
    public function getEndDate()
    {
        return $this->container['end_date'];
    }

    /**
     * Sets end_date
     *
     * @param \DateTime|null $end_date Takip edilen bitiş tarihi. `renewal_type: AUTO_RENEW` sözleşmelerde her dönemde (günlük olarak) bir sonraki döneme ileri alınır; bkz. `contract.advanced` webhook olayı.
     *
     * @return self
     */
    public function setEndDate($end_date)
    {
        if (is_null($end_date)) {
            array_push($this->openAPINullablesSetToNull, 'end_date');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('end_date', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        $this->container['end_date'] = $end_date;

        return $this;
    }

    /**
     * Gets end_date_signed
     *
     * @return \DateTime|null
     */
    public function getEndDateSigned()
    {
        return $this->container['end_date_signed'];
    }

    /**
     * Sets end_date_signed
     *
     * @param \DateTime|null $end_date_signed İmzalanan belge metninde bitiş tarihi bir değişken olarak yazılıysa, sözleşme tamamlandığı anda `end_date`'in dondurulmuş kopyası. `end_date` sonradan ileri alınsa bile bu alan DEĞİŞMEZ. İmzalanan metinle takip değeri arasındaki tarihsel referans budur.
     *
     * @return self
     */
    public function setEndDateSigned($end_date_signed)
    {
        if (is_null($end_date_signed)) {
            array_push($this->openAPINullablesSetToNull, 'end_date_signed');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('end_date_signed', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        $this->container['end_date_signed'] = $end_date_signed;

        return $this;
    }

    /**
     * Gets renewal_type
     *
     * @return string|null
     */
    public function getRenewalType()
    {
        return $this->container['renewal_type'];
    }

    /**
     * Sets renewal_type
     *
     * @param string|null $renewal_type renewal_type
     *
     * @return self
     */
    public function setRenewalType($renewal_type)
    {
        if (is_null($renewal_type)) {
            array_push($this->openAPINullablesSetToNull, 'renewal_type');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('renewal_type', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        $allowedValues = $this->getRenewalTypeAllowableValues();
        if (!is_null($renewal_type) && !in_array($renewal_type, $allowedValues, true)) {
            throw new \InvalidArgumentException(
                sprintf(
                    "Invalid value '%s' for 'renewal_type', must be one of '%s'",
                    $renewal_type,
                    implode("', '", $allowedValues)
                )
            );
        }
        $this->container['renewal_type'] = $renewal_type;

        return $this;
    }

    /**
     * Gets renewal_period_months
     *
     * @return int|null
     */
    public function getRenewalPeriodMonths()
    {
        return $this->container['renewal_period_months'];
    }

    /**
     * Sets renewal_period_months
     *
     * @param int|null $renewal_period_months renewal_period_months
     *
     * @return self
     */
    public function setRenewalPeriodMonths($renewal_period_months)
    {
        if (is_null($renewal_period_months)) {
            array_push($this->openAPINullablesSetToNull, 'renewal_period_months');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('renewal_period_months', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }

        if (!is_null($renewal_period_months) && ($renewal_period_months > 600)) {
            throw new \InvalidArgumentException('invalid value for $renewal_period_months when calling ContractTerm., must be smaller than or equal to 600.');
        }
        if (!is_null($renewal_period_months) && ($renewal_period_months < 1)) {
            throw new \InvalidArgumentException('invalid value for $renewal_period_months when calling ContractTerm., must be bigger than or equal to 1.');
        }

        $this->container['renewal_period_months'] = $renewal_period_months;

        return $this;
    }

    /**
     * Gets notice_days
     *
     * @return int|null
     */
    public function getNoticeDays()
    {
        return $this->container['notice_days'];
    }

    /**
     * Sets notice_days
     *
     * @param int|null $notice_days Fesih ihbarı için bitişten önce kaç gün gerektiği.
     *
     * @return self
     */
    public function setNoticeDays($notice_days)
    {
        if (is_null($notice_days)) {
            array_push($this->openAPINullablesSetToNull, 'notice_days');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('notice_days', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }

        if (!is_null($notice_days) && ($notice_days > 3650)) {
            throw new \InvalidArgumentException('invalid value for $notice_days when calling ContractTerm., must be smaller than or equal to 3650.');
        }
        if (!is_null($notice_days) && ($notice_days < 0)) {
            throw new \InvalidArgumentException('invalid value for $notice_days when calling ContractTerm., must be bigger than or equal to 0.');
        }

        $this->container['notice_days'] = $notice_days;

        return $this;
    }

    /**
     * Gets notice_deadline
     *
     * @return \DateTime|null
     */
    public function getNoticeDeadline()
    {
        return $this->container['notice_deadline'];
    }

    /**
     * Sets notice_deadline
     *
     * @param \DateTime|null $notice_deadline Hesaplanır (`end_date - notice_days`); `end_date` veya `notice_days` boşsa `null`.
     *
     * @return self
     */
    public function setNoticeDeadline($notice_deadline)
    {
        if (is_null($notice_deadline)) {
            array_push($this->openAPINullablesSetToNull, 'notice_deadline');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('notice_deadline', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        $this->container['notice_deadline'] = $notice_deadline;

        return $this;
    }

    /**
     * Gets reminder_offsets
     *
     * @return int[]|null
     */
    public function getReminderOffsets()
    {
        return $this->container['reminder_offsets'];
    }

    /**
     * Sets reminder_offsets
     *
     * @param int[]|null $reminder_offsets Bitişten kaç gün önce hatırlatma gönderileceği (gün sayıları).
     *
     * @return self
     */
    public function setReminderOffsets($reminder_offsets)
    {
        if (is_null($reminder_offsets)) {
            array_push($this->openAPINullablesSetToNull, 'reminder_offsets');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('reminder_offsets', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }

        if (!is_null($reminder_offsets) && (count($reminder_offsets) > 5)) {
            throw new \InvalidArgumentException('invalid value for $reminder_offsets when calling ContractTerm., number of items must be less than or equal to 5.');
        }
        $this->container['reminder_offsets'] = $reminder_offsets;

        return $this;
    }

    /**
     * Gets notify_counterparty
     *
     * @return bool
     */
    public function getNotifyCounterparty()
    {
        return $this->container['notify_counterparty'];
    }

    /**
     * Sets notify_counterparty
     *
     * @param bool $notify_counterparty Karşı tarafa da hatırlatma gönderilsin mi.
     *
     * @return self
     */
    public function setNotifyCounterparty($notify_counterparty)
    {
        if (is_null($notify_counterparty)) {
            throw new \InvalidArgumentException('non-nullable notify_counterparty cannot be null');
        }
        $this->container['notify_counterparty'] = $notify_counterparty;

        return $this;
    }

    /**
     * Gets state
     *
     * @return string
     */
    public function getState()
    {
        return $this->container['state'];
    }

    /**
     * Sets state
     *
     * @param string $state `UNTRACKED`: takip yok (`end_date` bilinmiyor). `ACTIVE`: takip sürüyor. `ENDED`: `FIXED_TERM` sözleşmenin bitiş tarihi geçti. `STOPPED`: yenileme takibi kullanıcı tarafından durduruldu.
     *
     * @return self
     */
    public function setState($state)
    {
        if (is_null($state)) {
            throw new \InvalidArgumentException('non-nullable state cannot be null');
        }
        $allowedValues = $this->getStateAllowableValues();
        if (!in_array($state, $allowedValues, true)) {
            throw new \InvalidArgumentException(
                sprintf(
                    "Invalid value '%s' for 'state', must be one of '%s'",
                    $state,
                    implode("', '", $allowedValues)
                )
            );
        }
        $this->container['state'] = $state;

        return $this;
    }

    /**
     * Gets days_left
     *
     * @return int|null
     */
    public function getDaysLeft()
    {
        return $this->container['days_left'];
    }

    /**
     * Sets days_left
     *
     * @param int|null $days_left Bugünden `end_date`'e gün sayısı (negatifse geçmişte).
     *
     * @return self
     */
    public function setDaysLeft($days_left)
    {
        if (is_null($days_left)) {
            array_push($this->openAPINullablesSetToNull, 'days_left');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('days_left', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        $this->container['days_left'] = $days_left;

        return $this;
    }

    /**
     * Gets renewal_stopped_at
     *
     * @return \DateTime|null
     */
    public function getRenewalStoppedAt()
    {
        return $this->container['renewal_stopped_at'];
    }

    /**
     * Sets renewal_stopped_at
     *
     * @param \DateTime|null $renewal_stopped_at renewal_stopped_at
     *
     * @return self
     */
    public function setRenewalStoppedAt($renewal_stopped_at)
    {
        if (is_null($renewal_stopped_at)) {
            array_push($this->openAPINullablesSetToNull, 'renewal_stopped_at');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('renewal_stopped_at', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        $this->container['renewal_stopped_at'] = $renewal_stopped_at;

        return $this;
    }
    /**
     * Returns true if offset exists. False otherwise.
     *
     * @param integer|string $offset Offset
     *
     * @return boolean
     */
    public function offsetExists(mixed $offset): bool
    {
        return isset($this->container[$offset]);
    }

    /**
     * Gets offset.
     *
     * @param integer|string $offset Offset
     *
     * @return mixed|null
     */
    #[\ReturnTypeWillChange]
    public function offsetGet(mixed $offset)
    {
        return $this->container[$offset] ?? null;
    }

    /**
     * Sets value based on offset.
     *
     * @param int|null $offset Offset
     * @param mixed    $value  Value to be set
     *
     * @return void
     */
    public function offsetSet($offset, $value): void
    {
        if (is_null($offset)) {
            $this->container[] = $value;
        } else {
            $this->container[$offset] = $value;
        }
    }

    /**
     * Unsets offset.
     *
     * @param integer|string $offset Offset
     *
     * @return void
     */
    public function offsetUnset(mixed $offset): void
    {
        unset($this->container[$offset]);
    }

    /**
     * Serializes the object to a value that can be serialized natively by json_encode().
     * @link https://www.php.net/manual/en/jsonserializable.jsonserialize.php
     *
     * @return mixed Returns data which can be serialized by json_encode(), which is a value
     * of any type other than a resource.
     */
    #[\ReturnTypeWillChange]
    public function jsonSerialize()
    {
       return ObjectSerializer::sanitizeForSerialization($this);
    }

    /**
     * Gets the string presentation of the object
     *
     * @return string
     */
    public function __toString()
    {
        return json_encode(
            ObjectSerializer::sanitizeForSerialization($this),
            JSON_PRETTY_PRINT
        );
    }

    /**
     * Gets a header-safe presentation of the object
     *
     * @return string
     */
    public function toHeaderValue()
    {
        return json_encode(ObjectSerializer::sanitizeForSerialization($this));
    }
}


