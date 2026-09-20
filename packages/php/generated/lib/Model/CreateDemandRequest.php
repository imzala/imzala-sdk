<?php
/**
 * CreateDemandRequest
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
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.8.18 · **Son güncelleme:** 2026-09-20  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır (aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (8) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 8 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme.
 *
 * The version of the OpenAPI document: 1.8.18
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
 * CreateDemandRequest Class Doc Comment
 *
 * @category Class
 * @package  Imzala\Client
 * @author   OpenAPI Generator team
 * @link     https://openapi-generator.tech
 * @implements \ArrayAccess<string, mixed>
 */
class CreateDemandRequest implements ModelInterface, ArrayAccess, \JsonSerializable
{
    public const DISCRIMINATOR = null;

    /**
     * The original name of the model.
     *
     * @var string
     */
    protected static $openAPIModelName = 'CreateDemandRequest';

    /**
     * Array of property to type mappings. Used for (de)serialization
     *
     * @var string[]
     */
    protected static $openAPITypes = [
        'template_id' => 'string',
        'title' => 'string',
        'description' => 'string',
        'idempotency_key' => 'string',
        'force' => 'bool',
        'party_mapping' => '\Imzala\Client\Model\PartyMappingInput[]',
        'documents' => '\Imzala\Client\Model\DocumentSelectionInput',
        'variables' => 'array<string,\Imzala\Client\Model\PartyMappingInputVariablesValue>',
        'has_timestamp' => 'bool',
        'allowed_signature_variants' => 'string[]',
        'dispatch_notifications' => 'bool',
        'send_sms_notifications' => 'bool',
        'send_email_notifications' => 'bool',
        'sms_title' => 'string',
        'sms_content' => 'string',
        'email_content' => 'string',
        'expiry_date' => '\DateTime',
        'require_tc_verification' => 'bool',
        'require_biometric_verification' => 'bool',
        'reminder_settings' => '\Imzala\Client\Model\ReminderSettings'
    ];

    /**
     * Array of property to format mappings. Used for (de)serialization
     *
     * @var string[]
     * @phpstan-var array<string, string|null>
     * @psalm-var array<string, string|null>
     */
    protected static $openAPIFormats = [
        'template_id' => 'uuid',
        'title' => null,
        'description' => null,
        'idempotency_key' => null,
        'force' => null,
        'party_mapping' => null,
        'documents' => null,
        'variables' => null,
        'has_timestamp' => null,
        'allowed_signature_variants' => null,
        'dispatch_notifications' => null,
        'send_sms_notifications' => null,
        'send_email_notifications' => null,
        'sms_title' => null,
        'sms_content' => null,
        'email_content' => null,
        'expiry_date' => 'date-time',
        'require_tc_verification' => null,
        'require_biometric_verification' => null,
        'reminder_settings' => null
    ];

    /**
     * Array of nullable properties. Used for (de)serialization
     *
     * @var boolean[]
     */
    protected static array $openAPINullables = [
        'template_id' => false,
        'title' => false,
        'description' => false,
        'idempotency_key' => false,
        'force' => false,
        'party_mapping' => false,
        'documents' => false,
        'variables' => false,
        'has_timestamp' => false,
        'allowed_signature_variants' => true,
        'dispatch_notifications' => false,
        'send_sms_notifications' => false,
        'send_email_notifications' => false,
        'sms_title' => false,
        'sms_content' => false,
        'email_content' => false,
        'expiry_date' => false,
        'require_tc_verification' => false,
        'require_biometric_verification' => false,
        'reminder_settings' => false
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
        'template_id' => 'template_id',
        'title' => 'title',
        'description' => 'description',
        'idempotency_key' => 'idempotency_key',
        'force' => 'force',
        'party_mapping' => 'party_mapping',
        'documents' => 'documents',
        'variables' => 'variables',
        'has_timestamp' => 'has_timestamp',
        'allowed_signature_variants' => 'allowed_signature_variants',
        'dispatch_notifications' => 'dispatch_notifications',
        'send_sms_notifications' => 'send_sms_notifications',
        'send_email_notifications' => 'send_email_notifications',
        'sms_title' => 'sms_title',
        'sms_content' => 'sms_content',
        'email_content' => 'email_content',
        'expiry_date' => 'expiry_date',
        'require_tc_verification' => 'require_tc_verification',
        'require_biometric_verification' => 'require_biometric_verification',
        'reminder_settings' => 'reminder_settings'
    ];

    /**
     * Array of attributes to setter functions (for deserialization of responses)
     *
     * @var string[]
     */
    protected static $setters = [
        'template_id' => 'setTemplateId',
        'title' => 'setTitle',
        'description' => 'setDescription',
        'idempotency_key' => 'setIdempotencyKey',
        'force' => 'setForce',
        'party_mapping' => 'setPartyMapping',
        'documents' => 'setDocuments',
        'variables' => 'setVariables',
        'has_timestamp' => 'setHasTimestamp',
        'allowed_signature_variants' => 'setAllowedSignatureVariants',
        'dispatch_notifications' => 'setDispatchNotifications',
        'send_sms_notifications' => 'setSendSmsNotifications',
        'send_email_notifications' => 'setSendEmailNotifications',
        'sms_title' => 'setSmsTitle',
        'sms_content' => 'setSmsContent',
        'email_content' => 'setEmailContent',
        'expiry_date' => 'setExpiryDate',
        'require_tc_verification' => 'setRequireTcVerification',
        'require_biometric_verification' => 'setRequireBiometricVerification',
        'reminder_settings' => 'setReminderSettings'
    ];

    /**
     * Array of attributes to getter functions (for serialization of requests)
     *
     * @var string[]
     */
    protected static $getters = [
        'template_id' => 'getTemplateId',
        'title' => 'getTitle',
        'description' => 'getDescription',
        'idempotency_key' => 'getIdempotencyKey',
        'force' => 'getForce',
        'party_mapping' => 'getPartyMapping',
        'documents' => 'getDocuments',
        'variables' => 'getVariables',
        'has_timestamp' => 'getHasTimestamp',
        'allowed_signature_variants' => 'getAllowedSignatureVariants',
        'dispatch_notifications' => 'getDispatchNotifications',
        'send_sms_notifications' => 'getSendSmsNotifications',
        'send_email_notifications' => 'getSendEmailNotifications',
        'sms_title' => 'getSmsTitle',
        'sms_content' => 'getSmsContent',
        'email_content' => 'getEmailContent',
        'expiry_date' => 'getExpiryDate',
        'require_tc_verification' => 'getRequireTcVerification',
        'require_biometric_verification' => 'getRequireBiometricVerification',
        'reminder_settings' => 'getReminderSettings'
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

    public const ALLOWED_SIGNATURE_VARIANTS_DRAW = 'draw';
    public const ALLOWED_SIGNATURE_VARIANTS_TYPE = 'type';
    public const ALLOWED_SIGNATURE_VARIANTS_UPLOAD = 'upload';
    public const ALLOWED_SIGNATURE_VARIANTS_PHONE = 'phone';

    /**
     * Gets allowable values of the enum
     *
     * @return string[]
     */
    public function getAllowedSignatureVariantsAllowableValues()
    {
        return [
            self::ALLOWED_SIGNATURE_VARIANTS_DRAW,
            self::ALLOWED_SIGNATURE_VARIANTS_TYPE,
            self::ALLOWED_SIGNATURE_VARIANTS_UPLOAD,
            self::ALLOWED_SIGNATURE_VARIANTS_PHONE,
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
        $this->setIfExists('template_id', $data ?? [], null);
        $this->setIfExists('title', $data ?? [], null);
        $this->setIfExists('description', $data ?? [], null);
        $this->setIfExists('idempotency_key', $data ?? [], null);
        $this->setIfExists('force', $data ?? [], null);
        $this->setIfExists('party_mapping', $data ?? [], null);
        $this->setIfExists('documents', $data ?? [], null);
        $this->setIfExists('variables', $data ?? [], null);
        $this->setIfExists('has_timestamp', $data ?? [], false);
        $this->setIfExists('allowed_signature_variants', $data ?? [], null);
        $this->setIfExists('dispatch_notifications', $data ?? [], true);
        $this->setIfExists('send_sms_notifications', $data ?? [], true);
        $this->setIfExists('send_email_notifications', $data ?? [], true);
        $this->setIfExists('sms_title', $data ?? [], 'CODECK');
        $this->setIfExists('sms_content', $data ?? [], null);
        $this->setIfExists('email_content', $data ?? [], null);
        $this->setIfExists('expiry_date', $data ?? [], null);
        $this->setIfExists('require_tc_verification', $data ?? [], false);
        $this->setIfExists('require_biometric_verification', $data ?? [], false);
        $this->setIfExists('reminder_settings', $data ?? [], null);
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

        if ($this->container['template_id'] === null) {
            $invalidProperties[] = "'template_id' can't be null";
        }
        if (!is_null($this->container['idempotency_key']) && (mb_strlen($this->container['idempotency_key']) > 255)) {
            $invalidProperties[] = "invalid value for 'idempotency_key', the character length must be smaller than or equal to 255.";
        }

        if ($this->container['party_mapping'] === null) {
            $invalidProperties[] = "'party_mapping' can't be null";
        }
        if ((count($this->container['party_mapping']) < 1)) {
            $invalidProperties[] = "invalid value for 'party_mapping', number of items must be greater than or equal to 1.";
        }

        if (!is_null($this->container['allowed_signature_variants']) && (count($this->container['allowed_signature_variants']) > 4)) {
            $invalidProperties[] = "invalid value for 'allowed_signature_variants', number of items must be less than or equal to 4.";
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
     * Gets template_id
     *
     * @return string
     */
    public function getTemplateId()
    {
        return $this->container['template_id'];
    }

    /**
     * Sets template_id
     *
     * @param string $template_id GET /api/v1/templates listesinden veya dashboard'dan kopyalayın
     *
     * @return self
     */
    public function setTemplateId($template_id)
    {
        if (is_null($template_id)) {
            throw new \InvalidArgumentException('non-nullable template_id cannot be null');
        }
        $this->container['template_id'] = $template_id;

        return $this;
    }

    /**
     * Gets title
     *
     * @return string|null
     */
    public function getTitle()
    {
        return $this->container['title'];
    }

    /**
     * Sets title
     *
     * @param string|null $title Sözleşme başlığı (yoksa template adı kullanılır)
     *
     * @return self
     */
    public function setTitle($title)
    {
        if (is_null($title)) {
            throw new \InvalidArgumentException('non-nullable title cannot be null');
        }
        $this->container['title'] = $title;

        return $this;
    }

    /**
     * Gets description
     *
     * @return string|null
     */
    public function getDescription()
    {
        return $this->container['description'];
    }

    /**
     * Sets description
     *
     * @param string|null $description description
     *
     * @return self
     */
    public function setDescription($description)
    {
        if (is_null($description)) {
            throw new \InvalidArgumentException('non-nullable description cannot be null');
        }
        $this->container['description'] = $description;

        return $this;
    }

    /**
     * Gets idempotency_key
     *
     * @return string|null
     */
    public function getIdempotencyKey()
    {
        return $this->container['idempotency_key'];
    }

    /**
     * Sets idempotency_key
     *
     * @param string|null $idempotency_key `Idempotency-Key` başlığının gövde karşılığı; başlık ekleyemeyen istemciler için. İkisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.
     *
     * @return self
     */
    public function setIdempotencyKey($idempotency_key)
    {
        if (is_null($idempotency_key)) {
            throw new \InvalidArgumentException('non-nullable idempotency_key cannot be null');
        }
        if ((mb_strlen($idempotency_key) > 255)) {
            throw new \InvalidArgumentException('invalid length for $idempotency_key when calling CreateDemandRequest., must be smaller than or equal to 255.');
        }

        $this->container['idempotency_key'] = $idempotency_key;

        return $this;
    }

    /**
     * Gets force
     *
     * @return bool|null
     */
    public function getForce()
    {
        return $this->container['force'];
    }

    /**
     * Sets force
     *
     * @param bool|null $force Kopya kapısını bilerek geç. Yalnız idempotency anahtarı GÖNDERİLMEYEN çağrılarda anlamlıdır: aynı şablondan aynı taraflara aynı başlık ve değişkenlerle 10 dakika içinde ikinci bir sözleşmeyi kasten göndermek için.
     *
     * @return self
     */
    public function setForce($force)
    {
        if (is_null($force)) {
            throw new \InvalidArgumentException('non-nullable force cannot be null');
        }
        $this->container['force'] = $force;

        return $this;
    }

    /**
     * Gets party_mapping
     *
     * @return \Imzala\Client\Model\PartyMappingInput[]
     */
    public function getPartyMapping()
    {
        return $this->container['party_mapping'];
    }

    /**
     * Sets party_mapping
     *
     * @param \Imzala\Client\Model\PartyMappingInput[] $party_mapping party_mapping
     *
     * @return self
     */
    public function setPartyMapping($party_mapping)
    {
        if (is_null($party_mapping)) {
            throw new \InvalidArgumentException('non-nullable party_mapping cannot be null');
        }


        if ((count($party_mapping) < 1)) {
            throw new \InvalidArgumentException('invalid length for $party_mapping when calling CreateDemandRequest., number of items must be greater than or equal to 1.');
        }
        $this->container['party_mapping'] = $party_mapping;

        return $this;
    }

    /**
     * Gets documents
     *
     * @return \Imzala\Client\Model\DocumentSelectionInput|null
     */
    public function getDocuments()
    {
        return $this->container['documents'];
    }

    /**
     * Sets documents
     *
     * @param \Imzala\Client\Model\DocumentSelectionInput|null $documents documents
     *
     * @return self
     */
    public function setDocuments($documents)
    {
        if (is_null($documents)) {
            throw new \InvalidArgumentException('non-nullable documents cannot be null');
        }
        $this->container['documents'] = $documents;

        return $this;
    }

    /**
     * Gets variables
     *
     * @return array<string,\Imzala\Client\Model\PartyMappingInputVariablesValue>|null
     */
    public function getVariables()
    {
        return $this->container['variables'];
    }

    /**
     * Sets variables
     *
     * @param array<string,\Imzala\Client\Model\PartyMappingInputVariablesValue>|null $variables **Root scope** — partilerden bağımsız field'lara gönderilen değerler. Item'ın template_party_id'si NULL ise (partisiz) buradan dolar. Multi-party şablonda kira_baslangic_tarihi gibi paylaşılan field'lar.
     *
     * @return self
     */
    public function setVariables($variables)
    {
        if (is_null($variables)) {
            throw new \InvalidArgumentException('non-nullable variables cannot be null');
        }
        $this->container['variables'] = $variables;

        return $this;
    }

    /**
     * Gets has_timestamp
     *
     * @return bool|null
     */
    public function getHasTimestamp()
    {
        return $this->container['has_timestamp'];
    }

    /**
     * Sets has_timestamp
     *
     * @param bool|null $has_timestamp TÜBİTAK zaman damgası
     *
     * @return self
     */
    public function setHasTimestamp($has_timestamp)
    {
        if (is_null($has_timestamp)) {
            throw new \InvalidArgumentException('non-nullable has_timestamp cannot be null');
        }
        $this->container['has_timestamp'] = $has_timestamp;

        return $this;
    }

    /**
     * Gets allowed_signature_variants
     *
     * @return string[]|null
     */
    public function getAllowedSignatureVariants()
    {
        return $this->container['allowed_signature_variants'];
    }

    /**
     * Sets allowed_signature_variants
     *
     * @param string[]|null $allowed_signature_variants İmzacının kullanabileceği dijital imza yöntemleri ve görünme sırası. Dizinin SIRASI imza ekranındaki sekme sırasıdır; listede olmayan yöntem o sözleşmede kapalıdır. Değerler: `draw` (parmakla veya fareyle çizerek), `type` (adını yazarak), `upload` (imza görselini dosya olarak yükleyerek), `phone` (telefonda çizerek).  Gönderilmezse (veya `null` gönderilirse) şablonun ayarı, şablonda da tanımlı değilse organizasyonun ayarı geçerli olur; hiçbiri tanımlı değilse dört yöntem varsayılan sırayla açıktır.  Bu alan toleranslı okunur: boş dizi, dizi olmayan bir değer veya tanınmayan bir değer gönderilirse alan YOK SAYILIR ve üst katmandan devralınır. Yani boş dizi göndermek \"tüm yöntemleri kapat\" anlamına GELMEZ; en az bir yöntem her zaman açık kalır. Büyük harfli değerler (`DRAW`) kabul edilir, tekrar eden değerler teke indirilir.  Yalnız `phone` içeren bir liste reddedilir (400 `SIGNATURE_VARIANTS_PHONE_ONLY`): telefonda çizim mobil cihazda kullanılamadığı için tek başına bırakılırsa imzacı yöntemsiz kalır. En az bir `phone` olmayan yöntem bırakın.  Ayar sözleşme düzeyindedir; imzacı bazında farklılaştırılamaz. `party_mapping` girdilerine yazılan böyle bir alan yok sayılır.
     *
     * @return self
     */
    public function setAllowedSignatureVariants($allowed_signature_variants)
    {
        if (is_null($allowed_signature_variants)) {
            array_push($this->openAPINullablesSetToNull, 'allowed_signature_variants');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('allowed_signature_variants', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        $allowedValues = $this->getAllowedSignatureVariantsAllowableValues();
        if (!is_null($allowed_signature_variants) && array_diff($allowed_signature_variants, $allowedValues)) {
            throw new \InvalidArgumentException(
                sprintf(
                    "Invalid value for 'allowed_signature_variants', must be one of '%s'",
                    implode("', '", $allowedValues)
                )
            );
        }

        if (!is_null($allowed_signature_variants) && (count($allowed_signature_variants) > 4)) {
            throw new \InvalidArgumentException('invalid value for $allowed_signature_variants when calling CreateDemandRequest., number of items must be less than or equal to 4.');
        }
        $this->container['allowed_signature_variants'] = $allowed_signature_variants;

        return $this;
    }

    /**
     * Gets dispatch_notifications
     *
     * @return bool|null
     */
    public function getDispatchNotifications()
    {
        return $this->container['dispatch_notifications'];
    }

    /**
     * Sets dispatch_notifications
     *
     * @param bool|null $dispatch_notifications `false` verilirse sözleşme yalnız oluşturulur, davet gönderilmez; daha sonra `POST /api/v1/demands/{demandId}/dispatch` ile gönderilir (çok-belgeli zarf akışında belgeler bu arada eklenir). Varsayılan açık: davetler oluşturma anında gider.
     *
     * @return self
     */
    public function setDispatchNotifications($dispatch_notifications)
    {
        if (is_null($dispatch_notifications)) {
            throw new \InvalidArgumentException('non-nullable dispatch_notifications cannot be null');
        }
        $this->container['dispatch_notifications'] = $dispatch_notifications;

        return $this;
    }

    /**
     * Gets send_sms_notifications
     *
     * @return bool|null
     */
    public function getSendSmsNotifications()
    {
        return $this->container['send_sms_notifications'];
    }

    /**
     * Sets send_sms_notifications
     *
     * @param bool|null $send_sms_notifications send_sms_notifications
     *
     * @return self
     */
    public function setSendSmsNotifications($send_sms_notifications)
    {
        if (is_null($send_sms_notifications)) {
            throw new \InvalidArgumentException('non-nullable send_sms_notifications cannot be null');
        }
        $this->container['send_sms_notifications'] = $send_sms_notifications;

        return $this;
    }

    /**
     * Gets send_email_notifications
     *
     * @return bool|null
     */
    public function getSendEmailNotifications()
    {
        return $this->container['send_email_notifications'];
    }

    /**
     * Sets send_email_notifications
     *
     * @param bool|null $send_email_notifications send_email_notifications
     *
     * @return self
     */
    public function setSendEmailNotifications($send_email_notifications)
    {
        if (is_null($send_email_notifications)) {
            throw new \InvalidArgumentException('non-nullable send_email_notifications cannot be null');
        }
        $this->container['send_email_notifications'] = $send_email_notifications;

        return $this;
    }

    /**
     * Gets sms_title
     *
     * @return string|null
     */
    public function getSmsTitle()
    {
        return $this->container['sms_title'];
    }

    /**
     * Sets sms_title
     *
     * @param string|null $sms_title SMS gönderici adı
     *
     * @return self
     */
    public function setSmsTitle($sms_title)
    {
        if (is_null($sms_title)) {
            throw new \InvalidArgumentException('non-nullable sms_title cannot be null');
        }
        $this->container['sms_title'] = $sms_title;

        return $this;
    }

    /**
     * Gets sms_content
     *
     * @return string|null
     */
    public function getSmsContent()
    {
        return $this->container['sms_content'];
    }

    /**
     * Sets sms_content
     *
     * @param string|null $sms_content Custom SMS gövdesi. **Sadece** çağıran organizasyon **PRO veya ENTERPRISE planda** ise ve aktif `OrganizationSmsConfig` (sender_name dolu) varsa kabul edilir; aksi halde 403 `SMS_CUSTOMIZATION_NOT_ALLOWED` döner.  FREE/BASIC planda olan veya kendi SMS sağlayıcısı tanımlı olmayan müşterilerin marka itibarını korumak için sistem default sağlayıcısı (Codeck NetGSM) ile gönderim yapılır ve özel metin reddedilir. Kendi sağlayıcınızı tanımlamak için Dashboard → Organizasyon → SMS Ayarları sayfasını kullanın.  Boş string / null gönderirseniz \"clear\" olarak yorumlanır (gating'den geçer).
     *
     * @return self
     */
    public function setSmsContent($sms_content)
    {
        if (is_null($sms_content)) {
            throw new \InvalidArgumentException('non-nullable sms_content cannot be null');
        }
        $this->container['sms_content'] = $sms_content;

        return $this;
    }

    /**
     * Gets email_content
     *
     * @return string|null
     */
    public function getEmailContent()
    {
        return $this->container['email_content'];
    }

    /**
     * Sets email_content
     *
     * @param string|null $email_content Custom e-posta gövdesi
     *
     * @return self
     */
    public function setEmailContent($email_content)
    {
        if (is_null($email_content)) {
            throw new \InvalidArgumentException('non-nullable email_content cannot be null');
        }
        $this->container['email_content'] = $email_content;

        return $this;
    }

    /**
     * Gets expiry_date
     *
     * @return \DateTime|null
     */
    public function getExpiryDate()
    {
        return $this->container['expiry_date'];
    }

    /**
     * Sets expiry_date
     *
     * @param \DateTime|null $expiry_date expiry_date
     *
     * @return self
     */
    public function setExpiryDate($expiry_date)
    {
        if (is_null($expiry_date)) {
            throw new \InvalidArgumentException('non-nullable expiry_date cannot be null');
        }
        $this->container['expiry_date'] = $expiry_date;

        return $this;
    }

    /**
     * Gets require_tc_verification
     *
     * @return bool|null
     */
    public function getRequireTcVerification()
    {
        return $this->container['require_tc_verification'];
    }

    /**
     * Sets require_tc_verification
     *
     * @param bool|null $require_tc_verification require_tc_verification
     *
     * @return self
     */
    public function setRequireTcVerification($require_tc_verification)
    {
        if (is_null($require_tc_verification)) {
            throw new \InvalidArgumentException('non-nullable require_tc_verification cannot be null');
        }
        $this->container['require_tc_verification'] = $require_tc_verification;

        return $this;
    }

    /**
     * Gets require_biometric_verification
     *
     * @return bool|null
     */
    public function getRequireBiometricVerification()
    {
        return $this->container['require_biometric_verification'];
    }

    /**
     * Sets require_biometric_verification
     *
     * @param bool|null $require_biometric_verification require_biometric_verification
     *
     * @return self
     */
    public function setRequireBiometricVerification($require_biometric_verification)
    {
        if (is_null($require_biometric_verification)) {
            throw new \InvalidArgumentException('non-nullable require_biometric_verification cannot be null');
        }
        $this->container['require_biometric_verification'] = $require_biometric_verification;

        return $this;
    }

    /**
     * Gets reminder_settings
     *
     * @return \Imzala\Client\Model\ReminderSettings|null
     */
    public function getReminderSettings()
    {
        return $this->container['reminder_settings'];
    }

    /**
     * Sets reminder_settings
     *
     * @param \Imzala\Client\Model\ReminderSettings|null $reminder_settings Bu sözleşme için hatırlatma ayarlarını **şablon default'unu override** ederek belirtir. Yollanmazsa şablonun `reminder_*` alanları kullanılır (PUT /api/templates/:id ile dashboard'dan kaydedilen değerler); şablonda da yoksa `{enabled:true, intervals_hours:[48], max_reminders:1, channels:[\"email\"]}` default'u uygulanır. Demand oluşumunda `ReminderConfig` satırı yaratılır ve BullMQ kuyruğuna scheduled hatırlatmalar yazılır.
     *
     * @return self
     */
    public function setReminderSettings($reminder_settings)
    {
        if (is_null($reminder_settings)) {
            throw new \InvalidArgumentException('non-nullable reminder_settings cannot be null');
        }
        $this->container['reminder_settings'] = $reminder_settings;

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


