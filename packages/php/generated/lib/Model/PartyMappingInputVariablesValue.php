<?php
/**
 * PartyMappingInputVariablesValue
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
 * PartyMappingInputVariablesValue Class Doc Comment
 *
 * @category Class
 * @package  Imzala\Client
 * @author   OpenAPI Generator team
 * @link     https://openapi-generator.tech
 * @implements \ArrayAccess<string, mixed>
 */
class PartyMappingInputVariablesValue implements ModelInterface, ArrayAccess, \JsonSerializable
{
    public const DISCRIMINATOR = null;

    /**
     * The original name of the model.
     *
     * @var string
     */
    protected static $openAPIModelName = 'PartyMappingInput_variables_value';

    /**
     * Array of property to type mappings. Used for (de)serialization
     *
     * @var string[]
     */
    protected static $openAPITypes = [
        'company_name' => 'string',
        'personal_name' => 'string',
        'address' => 'string',
        'company_address' => 'string',
        'personal_address' => 'string',
        'tax_number' => 'string',
        'tax_office' => 'string',
        'id_number' => 'string',
        'phone' => 'string',
        'company_phone' => 'string',
        'personal_phone' => 'string',
        'email' => 'string'
    ];

    /**
     * Array of property to format mappings. Used for (de)serialization
     *
     * @var string[]
     * @phpstan-var array<string, string|null>
     * @psalm-var array<string, string|null>
     */
    protected static $openAPIFormats = [
        'company_name' => null,
        'personal_name' => null,
        'address' => null,
        'company_address' => null,
        'personal_address' => null,
        'tax_number' => null,
        'tax_office' => null,
        'id_number' => null,
        'phone' => null,
        'company_phone' => null,
        'personal_phone' => null,
        'email' => null
    ];

    /**
     * Array of nullable properties. Used for (de)serialization
     *
     * @var boolean[]
     */
    protected static array $openAPINullables = [
        'company_name' => true,
        'personal_name' => true,
        'address' => true,
        'company_address' => true,
        'personal_address' => true,
        'tax_number' => true,
        'tax_office' => true,
        'id_number' => true,
        'phone' => true,
        'company_phone' => true,
        'personal_phone' => true,
        'email' => true
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
        'company_name' => 'companyName',
        'personal_name' => 'personalName',
        'address' => 'address',
        'company_address' => 'companyAddress',
        'personal_address' => 'personalAddress',
        'tax_number' => 'taxNumber',
        'tax_office' => 'taxOffice',
        'id_number' => 'idNumber',
        'phone' => 'phone',
        'company_phone' => 'companyPhone',
        'personal_phone' => 'personalPhone',
        'email' => 'email'
    ];

    /**
     * Array of attributes to setter functions (for deserialization of responses)
     *
     * @var string[]
     */
    protected static $setters = [
        'company_name' => 'setCompanyName',
        'personal_name' => 'setPersonalName',
        'address' => 'setAddress',
        'company_address' => 'setCompanyAddress',
        'personal_address' => 'setPersonalAddress',
        'tax_number' => 'setTaxNumber',
        'tax_office' => 'setTaxOffice',
        'id_number' => 'setIdNumber',
        'phone' => 'setPhone',
        'company_phone' => 'setCompanyPhone',
        'personal_phone' => 'setPersonalPhone',
        'email' => 'setEmail'
    ];

    /**
     * Array of attributes to getter functions (for serialization of requests)
     *
     * @var string[]
     */
    protected static $getters = [
        'company_name' => 'getCompanyName',
        'personal_name' => 'getPersonalName',
        'address' => 'getAddress',
        'company_address' => 'getCompanyAddress',
        'personal_address' => 'getPersonalAddress',
        'tax_number' => 'getTaxNumber',
        'tax_office' => 'getTaxOffice',
        'id_number' => 'getIdNumber',
        'phone' => 'getPhone',
        'company_phone' => 'getCompanyPhone',
        'personal_phone' => 'getPersonalPhone',
        'email' => 'getEmail'
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
        $this->setIfExists('company_name', $data ?? [], null);
        $this->setIfExists('personal_name', $data ?? [], null);
        $this->setIfExists('address', $data ?? [], null);
        $this->setIfExists('company_address', $data ?? [], null);
        $this->setIfExists('personal_address', $data ?? [], null);
        $this->setIfExists('tax_number', $data ?? [], null);
        $this->setIfExists('tax_office', $data ?? [], null);
        $this->setIfExists('id_number', $data ?? [], null);
        $this->setIfExists('phone', $data ?? [], null);
        $this->setIfExists('company_phone', $data ?? [], null);
        $this->setIfExists('personal_phone', $data ?? [], null);
        $this->setIfExists('email', $data ?? [], null);
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

        if (!is_null($this->container['company_name']) && (mb_strlen($this->container['company_name']) > 500)) {
            $invalidProperties[] = "invalid value for 'company_name', the character length must be smaller than or equal to 500.";
        }

        if (!is_null($this->container['personal_name']) && (mb_strlen($this->container['personal_name']) > 500)) {
            $invalidProperties[] = "invalid value for 'personal_name', the character length must be smaller than or equal to 500.";
        }

        if (!is_null($this->container['address']) && (mb_strlen($this->container['address']) > 500)) {
            $invalidProperties[] = "invalid value for 'address', the character length must be smaller than or equal to 500.";
        }

        if (!is_null($this->container['company_address']) && (mb_strlen($this->container['company_address']) > 500)) {
            $invalidProperties[] = "invalid value for 'company_address', the character length must be smaller than or equal to 500.";
        }

        if (!is_null($this->container['personal_address']) && (mb_strlen($this->container['personal_address']) > 500)) {
            $invalidProperties[] = "invalid value for 'personal_address', the character length must be smaller than or equal to 500.";
        }

        if (!is_null($this->container['tax_number']) && (mb_strlen($this->container['tax_number']) > 500)) {
            $invalidProperties[] = "invalid value for 'tax_number', the character length must be smaller than or equal to 500.";
        }

        if (!is_null($this->container['tax_office']) && (mb_strlen($this->container['tax_office']) > 500)) {
            $invalidProperties[] = "invalid value for 'tax_office', the character length must be smaller than or equal to 500.";
        }

        if (!is_null($this->container['id_number']) && (mb_strlen($this->container['id_number']) > 500)) {
            $invalidProperties[] = "invalid value for 'id_number', the character length must be smaller than or equal to 500.";
        }

        if (!is_null($this->container['phone']) && (mb_strlen($this->container['phone']) > 500)) {
            $invalidProperties[] = "invalid value for 'phone', the character length must be smaller than or equal to 500.";
        }

        if (!is_null($this->container['company_phone']) && (mb_strlen($this->container['company_phone']) > 500)) {
            $invalidProperties[] = "invalid value for 'company_phone', the character length must be smaller than or equal to 500.";
        }

        if (!is_null($this->container['personal_phone']) && (mb_strlen($this->container['personal_phone']) > 500)) {
            $invalidProperties[] = "invalid value for 'personal_phone', the character length must be smaller than or equal to 500.";
        }

        if (!is_null($this->container['email']) && (mb_strlen($this->container['email']) > 500)) {
            $invalidProperties[] = "invalid value for 'email', the character length must be smaller than or equal to 500.";
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
     * Gets company_name
     *
     * @return string|null
     */
    public function getCompanyName()
    {
        return $this->container['company_name'];
    }

    /**
     * Sets company_name
     *
     * @param string|null $company_name company_name
     *
     * @return self
     */
    public function setCompanyName($company_name)
    {
        if (is_null($company_name)) {
            array_push($this->openAPINullablesSetToNull, 'company_name');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('company_name', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        if (!is_null($company_name) && (mb_strlen($company_name) > 500)) {
            throw new \InvalidArgumentException('invalid length for $company_name when calling PartyMappingInputVariablesValue., must be smaller than or equal to 500.');
        }

        $this->container['company_name'] = $company_name;

        return $this;
    }

    /**
     * Gets personal_name
     *
     * @return string|null
     */
    public function getPersonalName()
    {
        return $this->container['personal_name'];
    }

    /**
     * Sets personal_name
     *
     * @param string|null $personal_name personal_name
     *
     * @return self
     */
    public function setPersonalName($personal_name)
    {
        if (is_null($personal_name)) {
            array_push($this->openAPINullablesSetToNull, 'personal_name');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('personal_name', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        if (!is_null($personal_name) && (mb_strlen($personal_name) > 500)) {
            throw new \InvalidArgumentException('invalid length for $personal_name when calling PartyMappingInputVariablesValue., must be smaller than or equal to 500.');
        }

        $this->container['personal_name'] = $personal_name;

        return $this;
    }

    /**
     * Gets address
     *
     * @return string|null
     */
    public function getAddress()
    {
        return $this->container['address'];
    }

    /**
     * Sets address
     *
     * @param string|null $address address
     *
     * @return self
     */
    public function setAddress($address)
    {
        if (is_null($address)) {
            array_push($this->openAPINullablesSetToNull, 'address');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('address', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        if (!is_null($address) && (mb_strlen($address) > 500)) {
            throw new \InvalidArgumentException('invalid length for $address when calling PartyMappingInputVariablesValue., must be smaller than or equal to 500.');
        }

        $this->container['address'] = $address;

        return $this;
    }

    /**
     * Gets company_address
     *
     * @return string|null
     */
    public function getCompanyAddress()
    {
        return $this->container['company_address'];
    }

    /**
     * Sets company_address
     *
     * @param string|null $company_address company_address
     *
     * @return self
     */
    public function setCompanyAddress($company_address)
    {
        if (is_null($company_address)) {
            array_push($this->openAPINullablesSetToNull, 'company_address');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('company_address', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        if (!is_null($company_address) && (mb_strlen($company_address) > 500)) {
            throw new \InvalidArgumentException('invalid length for $company_address when calling PartyMappingInputVariablesValue., must be smaller than or equal to 500.');
        }

        $this->container['company_address'] = $company_address;

        return $this;
    }

    /**
     * Gets personal_address
     *
     * @return string|null
     */
    public function getPersonalAddress()
    {
        return $this->container['personal_address'];
    }

    /**
     * Sets personal_address
     *
     * @param string|null $personal_address personal_address
     *
     * @return self
     */
    public function setPersonalAddress($personal_address)
    {
        if (is_null($personal_address)) {
            array_push($this->openAPINullablesSetToNull, 'personal_address');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('personal_address', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        if (!is_null($personal_address) && (mb_strlen($personal_address) > 500)) {
            throw new \InvalidArgumentException('invalid length for $personal_address when calling PartyMappingInputVariablesValue., must be smaller than or equal to 500.');
        }

        $this->container['personal_address'] = $personal_address;

        return $this;
    }

    /**
     * Gets tax_number
     *
     * @return string|null
     */
    public function getTaxNumber()
    {
        return $this->container['tax_number'];
    }

    /**
     * Sets tax_number
     *
     * @param string|null $tax_number tax_number
     *
     * @return self
     */
    public function setTaxNumber($tax_number)
    {
        if (is_null($tax_number)) {
            array_push($this->openAPINullablesSetToNull, 'tax_number');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('tax_number', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        if (!is_null($tax_number) && (mb_strlen($tax_number) > 500)) {
            throw new \InvalidArgumentException('invalid length for $tax_number when calling PartyMappingInputVariablesValue., must be smaller than or equal to 500.');
        }

        $this->container['tax_number'] = $tax_number;

        return $this;
    }

    /**
     * Gets tax_office
     *
     * @return string|null
     */
    public function getTaxOffice()
    {
        return $this->container['tax_office'];
    }

    /**
     * Sets tax_office
     *
     * @param string|null $tax_office tax_office
     *
     * @return self
     */
    public function setTaxOffice($tax_office)
    {
        if (is_null($tax_office)) {
            array_push($this->openAPINullablesSetToNull, 'tax_office');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('tax_office', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        if (!is_null($tax_office) && (mb_strlen($tax_office) > 500)) {
            throw new \InvalidArgumentException('invalid length for $tax_office when calling PartyMappingInputVariablesValue., must be smaller than or equal to 500.');
        }

        $this->container['tax_office'] = $tax_office;

        return $this;
    }

    /**
     * Gets id_number
     *
     * @return string|null
     */
    public function getIdNumber()
    {
        return $this->container['id_number'];
    }

    /**
     * Sets id_number
     *
     * @param string|null $id_number id_number
     *
     * @return self
     */
    public function setIdNumber($id_number)
    {
        if (is_null($id_number)) {
            array_push($this->openAPINullablesSetToNull, 'id_number');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('id_number', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        if (!is_null($id_number) && (mb_strlen($id_number) > 500)) {
            throw new \InvalidArgumentException('invalid length for $id_number when calling PartyMappingInputVariablesValue., must be smaller than or equal to 500.');
        }

        $this->container['id_number'] = $id_number;

        return $this;
    }

    /**
     * Gets phone
     *
     * @return string|null
     */
    public function getPhone()
    {
        return $this->container['phone'];
    }

    /**
     * Sets phone
     *
     * @param string|null $phone phone
     *
     * @return self
     */
    public function setPhone($phone)
    {
        if (is_null($phone)) {
            array_push($this->openAPINullablesSetToNull, 'phone');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('phone', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        if (!is_null($phone) && (mb_strlen($phone) > 500)) {
            throw new \InvalidArgumentException('invalid length for $phone when calling PartyMappingInputVariablesValue., must be smaller than or equal to 500.');
        }

        $this->container['phone'] = $phone;

        return $this;
    }

    /**
     * Gets company_phone
     *
     * @return string|null
     */
    public function getCompanyPhone()
    {
        return $this->container['company_phone'];
    }

    /**
     * Sets company_phone
     *
     * @param string|null $company_phone company_phone
     *
     * @return self
     */
    public function setCompanyPhone($company_phone)
    {
        if (is_null($company_phone)) {
            array_push($this->openAPINullablesSetToNull, 'company_phone');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('company_phone', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        if (!is_null($company_phone) && (mb_strlen($company_phone) > 500)) {
            throw new \InvalidArgumentException('invalid length for $company_phone when calling PartyMappingInputVariablesValue., must be smaller than or equal to 500.');
        }

        $this->container['company_phone'] = $company_phone;

        return $this;
    }

    /**
     * Gets personal_phone
     *
     * @return string|null
     */
    public function getPersonalPhone()
    {
        return $this->container['personal_phone'];
    }

    /**
     * Sets personal_phone
     *
     * @param string|null $personal_phone personal_phone
     *
     * @return self
     */
    public function setPersonalPhone($personal_phone)
    {
        if (is_null($personal_phone)) {
            array_push($this->openAPINullablesSetToNull, 'personal_phone');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('personal_phone', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        if (!is_null($personal_phone) && (mb_strlen($personal_phone) > 500)) {
            throw new \InvalidArgumentException('invalid length for $personal_phone when calling PartyMappingInputVariablesValue., must be smaller than or equal to 500.');
        }

        $this->container['personal_phone'] = $personal_phone;

        return $this;
    }

    /**
     * Gets email
     *
     * @return string|null
     */
    public function getEmail()
    {
        return $this->container['email'];
    }

    /**
     * Sets email
     *
     * @param string|null $email email
     *
     * @return self
     */
    public function setEmail($email)
    {
        if (is_null($email)) {
            array_push($this->openAPINullablesSetToNull, 'email');
        } else {
            $nullablesSetToNull = $this->getOpenAPINullablesSetToNull();
            $index = array_search('email', $nullablesSetToNull);
            if ($index !== FALSE) {
                unset($nullablesSetToNull[$index]);
                $this->setOpenAPINullablesSetToNull($nullablesSetToNull);
            }
        }
        if (!is_null($email) && (mb_strlen($email) > 500)) {
            throw new \InvalidArgumentException('invalid length for $email when calling PartyMappingInputVariablesValue., must be smaller than or equal to 500.');
        }

        $this->container['email'] = $email;

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


