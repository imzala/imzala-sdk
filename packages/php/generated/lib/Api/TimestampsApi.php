<?php
/**
 * TimestampsApi
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
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.8.14 · **Son güncelleme:** 2026-09-15  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır (aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (8) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 8 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme.
 *
 * The version of the OpenAPI document: 1.8.14
 * Contact: destek@imzala.org
 * Generated by: https://openapi-generator.tech
 * Generator version: 7.23.0
 */

/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

namespace Imzala\Client\Api;

use GuzzleHttp\Client;
use GuzzleHttp\ClientInterface;
use GuzzleHttp\Exception\ConnectException;
use GuzzleHttp\Exception\RequestException;
use GuzzleHttp\Psr7\MultipartStream;
use GuzzleHttp\Psr7\Request;
use GuzzleHttp\RequestOptions;
use Psr\Http\Message\RequestInterface;
use Psr\Http\Message\ResponseInterface;
use Imzala\Client\ApiException;
use Imzala\Client\Configuration;
use Imzala\Client\FormDataProcessor;
use Imzala\Client\HeaderSelector;
use Imzala\Client\ObjectSerializer;

/**
 * TimestampsApi Class Doc Comment
 *
 * @category Class
 * @package  Imzala\Client
 * @author   OpenAPI Generator team
 * @link     https://openapi-generator.tech
 */
class TimestampsApi
{
    /**
     * @var ClientInterface
     */
    protected $client;

    /**
     * @var Configuration
     */
    protected $config;

    /**
     * @var HeaderSelector
     */
    protected $headerSelector;

    /**
     * @var int Host index
     */
    protected $hostIndex;

    /** @var string[] $contentTypes **/
    public const contentTypes = [
        'apiV1TimestampsGet' => [
            'application/json',
        ],
        'apiV1TimestampsIdGet' => [
            'application/json',
        ],
        'apiV1TimestampsPost' => [
            'multipart/form-data',
            'application/json',
        ],
    ];

    /**
     * @param ClientInterface $client
     * @param Configuration   $config
     * @param HeaderSelector  $selector
     * @param int             $hostIndex (Optional) host index to select the list of hosts if defined in the OpenAPI spec
     */
    public function __construct(
        ?ClientInterface $client = null,
        ?Configuration $config = null,
        ?HeaderSelector $selector = null,
        int $hostIndex = 0
    ) {
        $this->client = $client ?: new Client();
        $this->config = $config ?: Configuration::getDefaultConfiguration();
        $this->headerSelector = $selector ?: new HeaderSelector();
        $this->hostIndex = $hostIndex;
    }

    /**
     * Set the host index
     *
     * @param int $hostIndex Host index (required)
     */
    public function setHostIndex($hostIndex): void
    {
        $this->hostIndex = $hostIndex;
    }

    /**
     * Get the host index
     *
     * @return int Host index
     */
    public function getHostIndex()
    {
        return $this->hostIndex;
    }

    /**
     * @return Configuration
     */
    public function getConfig()
    {
        return $this->config;
    }

    /**
     * Operation apiV1TimestampsGet
     *
     * Zaman damgası listesi
     *
     * @param  int|null $page page (optional, default to 1)
     * @param  int|null $limit limit (optional, default to 25)
     * @param  string|null $q Dosya adı / açıklamada arama (max 100 karakter) (optional)
     * @param  string|null $status Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional)
     * @param  \DateTime|null $from Damga günü alt sınırı (YYYY-MM-DD) (optional)
     * @param  \DateTime|null $to Damga günü üst sınırı (YYYY-MM-DD) (optional)
     * @param  string|null $sort Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (&#39;-&#39; öneki azalan) (optional, default to '-createdAt')
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \Imzala\Client\Model\ApiV1TimestampsGet200Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response|\Imzala\Client\Model\ApiError
     */
    public function apiV1TimestampsGet($page = 1, $limit = 25, $q = null, $status = null, $from = null, $to = null, $sort = '-createdAt', string $contentType = self::contentTypes['apiV1TimestampsGet'][0])
    {
        list($response) = $this->apiV1TimestampsGetWithHttpInfo($page, $limit, $q, $status, $from, $to, $sort, $contentType);
        return $response;
    }

    /**
     * Operation apiV1TimestampsGetWithHttpInfo
     *
     * Zaman damgası listesi
     *
     * @param  int|null $page (optional, default to 1)
     * @param  int|null $limit (optional, default to 25)
     * @param  string|null $q Dosya adı / açıklamada arama (max 100 karakter) (optional)
     * @param  string|null $status Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional)
     * @param  \DateTime|null $from Damga günü alt sınırı (YYYY-MM-DD) (optional)
     * @param  \DateTime|null $to Damga günü üst sınırı (YYYY-MM-DD) (optional)
     * @param  string|null $sort Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (&#39;-&#39; öneki azalan) (optional, default to '-createdAt')
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \Imzala\Client\Model\ApiV1TimestampsGet200Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response|\Imzala\Client\Model\ApiError, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1TimestampsGetWithHttpInfo($page = 1, $limit = 25, $q = null, $status = null, $from = null, $to = null, $sort = '-createdAt', string $contentType = self::contentTypes['apiV1TimestampsGet'][0])
    {
        $request = $this->apiV1TimestampsGetRequest($page, $limit, $q, $status, $from, $to, $sort, $contentType);

        try {
            $options = $this->createHttpClientOption();
            try {
                $response = $this->client->send($request, $options);
            } catch (RequestException $e) {
                throw new ApiException(
                    "[{$e->getCode()}] {$e->getMessage()}",
                    (int) $e->getCode(),
                    $e->getResponse() ? $e->getResponse()->getHeaders() : null,
                    $e->getResponse() ? (string) $e->getResponse()->getBody() : null
                );
            } catch (ConnectException $e) {
                throw new ApiException(
                    "[{$e->getCode()}] {$e->getMessage()}",
                    (int) $e->getCode(),
                    null,
                    null
                );
            }

            $statusCode = $response->getStatusCode();


            switch($statusCode) {
                case 200:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TimestampsGet200Response',
                        $request,
                        $response,
                    );
                case 401:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesGet401Response',
                        $request,
                        $response,
                    );
                case 403:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiError',
                        $request,
                        $response,
                    );
            }

            

            if ($statusCode < 200 || $statusCode > 299) {
                throw new ApiException(
                    sprintf(
                        '[%d] Error connecting to the API (%s)',
                        $statusCode,
                        (string) $request->getUri()
                    ),
                    $statusCode,
                    $response->getHeaders(),
                    (string) $response->getBody()
                );
            }

            return $this->handleResponseWithDataType(
                '\Imzala\Client\Model\ApiV1TimestampsGet200Response',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 200:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TimestampsGet200Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 401:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TemplatesGet401Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 403:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiError',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
            }
        

            throw $e;
        }
    }

    /**
     * Operation apiV1TimestampsGetAsync
     *
     * Zaman damgası listesi
     *
     * @param  int|null $page (optional, default to 1)
     * @param  int|null $limit (optional, default to 25)
     * @param  string|null $q Dosya adı / açıklamada arama (max 100 karakter) (optional)
     * @param  string|null $status Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional)
     * @param  \DateTime|null $from Damga günü alt sınırı (YYYY-MM-DD) (optional)
     * @param  \DateTime|null $to Damga günü üst sınırı (YYYY-MM-DD) (optional)
     * @param  string|null $sort Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (&#39;-&#39; öneki azalan) (optional, default to '-createdAt')
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1TimestampsGetAsync($page = 1, $limit = 25, $q = null, $status = null, $from = null, $to = null, $sort = '-createdAt', string $contentType = self::contentTypes['apiV1TimestampsGet'][0])
    {
        return $this->apiV1TimestampsGetAsyncWithHttpInfo($page, $limit, $q, $status, $from, $to, $sort, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1TimestampsGetAsyncWithHttpInfo
     *
     * Zaman damgası listesi
     *
     * @param  int|null $page (optional, default to 1)
     * @param  int|null $limit (optional, default to 25)
     * @param  string|null $q Dosya adı / açıklamada arama (max 100 karakter) (optional)
     * @param  string|null $status Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional)
     * @param  \DateTime|null $from Damga günü alt sınırı (YYYY-MM-DD) (optional)
     * @param  \DateTime|null $to Damga günü üst sınırı (YYYY-MM-DD) (optional)
     * @param  string|null $sort Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (&#39;-&#39; öneki azalan) (optional, default to '-createdAt')
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1TimestampsGetAsyncWithHttpInfo($page = 1, $limit = 25, $q = null, $status = null, $from = null, $to = null, $sort = '-createdAt', string $contentType = self::contentTypes['apiV1TimestampsGet'][0])
    {
        $returnType = '\Imzala\Client\Model\ApiV1TimestampsGet200Response';
        $request = $this->apiV1TimestampsGetRequest($page, $limit, $q, $status, $from, $to, $sort, $contentType);

        return $this->client
            ->sendAsync($request, $this->createHttpClientOption())
            ->then(
                function ($response) use ($returnType) {
                    if ($returnType === '\SplFileObject') {
                        $content = $response->getBody(); //stream goes to serializer
                    } else {
                        $content = (string) $response->getBody();
                        if ($returnType !== 'string') {
                            $content = json_decode($content);
                        }
                    }

                    return [
                        ObjectSerializer::deserialize($content, $returnType, []),
                        $response->getStatusCode(),
                        $response->getHeaders()
                    ];
                },
                function ($exception) {
                    $response = $exception->getResponse();
                    $statusCode = $response->getStatusCode();
                    throw new ApiException(
                        sprintf(
                            '[%d] Error connecting to the API (%s)',
                            $statusCode,
                            $exception->getRequest()->getUri()
                        ),
                        $statusCode,
                        $response->getHeaders(),
                        (string) $response->getBody()
                    );
                }
            );
    }

    /**
     * Create request for operation 'apiV1TimestampsGet'
     *
     * @param  int|null $page (optional, default to 1)
     * @param  int|null $limit (optional, default to 25)
     * @param  string|null $q Dosya adı / açıklamada arama (max 100 karakter) (optional)
     * @param  string|null $status Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID (optional)
     * @param  \DateTime|null $from Damga günü alt sınırı (YYYY-MM-DD) (optional)
     * @param  \DateTime|null $to Damga günü üst sınırı (YYYY-MM-DD) (optional)
     * @param  string|null $sort Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (&#39;-&#39; öneki azalan) (optional, default to '-createdAt')
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1TimestampsGetRequest($page = 1, $limit = 25, $q = null, $status = null, $from = null, $to = null, $sort = '-createdAt', string $contentType = self::contentTypes['apiV1TimestampsGet'][0])
    {


        if ($limit !== null && $limit > 100) {
            throw new \InvalidArgumentException('invalid value for "$limit" when calling TimestampsApi.apiV1TimestampsGet, must be smaller than or equal to 100.');
        }
        if ($limit !== null && $limit < 10) {
            throw new \InvalidArgumentException('invalid value for "$limit" when calling TimestampsApi.apiV1TimestampsGet, must be bigger than or equal to 10.');
        }
        






        $resourcePath = '/api/v1/timestamps';
        $formParams = [];
        $queryParams = [];
        $headerParams = [];
        $httpBody = '';
        $multipart = false;

        // query params
        $queryParams = array_merge($queryParams, ObjectSerializer::toQueryValue(
            $page,
            'page', // param base name
            'integer', // openApiType
            'form', // style
            true, // explode
            false // required
        ) ?? []);
        // query params
        $queryParams = array_merge($queryParams, ObjectSerializer::toQueryValue(
            $limit,
            'limit', // param base name
            'integer', // openApiType
            'form', // style
            true, // explode
            false // required
        ) ?? []);
        // query params
        $queryParams = array_merge($queryParams, ObjectSerializer::toQueryValue(
            $q,
            'q', // param base name
            'string', // openApiType
            'form', // style
            true, // explode
            false // required
        ) ?? []);
        // query params
        $queryParams = array_merge($queryParams, ObjectSerializer::toQueryValue(
            $status,
            'status', // param base name
            'string', // openApiType
            'form', // style
            true, // explode
            false // required
        ) ?? []);
        // query params
        $queryParams = array_merge($queryParams, ObjectSerializer::toQueryValue(
            $from,
            'from', // param base name
            'string', // openApiType
            'form', // style
            true, // explode
            false // required
        ) ?? []);
        // query params
        $queryParams = array_merge($queryParams, ObjectSerializer::toQueryValue(
            $to,
            'to', // param base name
            'string', // openApiType
            'form', // style
            true, // explode
            false // required
        ) ?? []);
        // query params
        $queryParams = array_merge($queryParams, ObjectSerializer::toQueryValue(
            $sort,
            'sort', // param base name
            'string', // openApiType
            'form', // style
            true, // explode
            false // required
        ) ?? []);




        $headers = $this->headerSelector->selectHeaders(
            ['application/json', ],
            $contentType,
            $multipart
        );

        // for model (json/xml)
        if (count($formParams) > 0) {
            if ($multipart) {
                $multipartContents = [];
                foreach ($formParams as $formParamName => $formParamValue) {
                    $formParamValueItems = is_array($formParamValue) ? $formParamValue : [$formParamValue];
                    foreach ($formParamValueItems as $formParamValueItem) {
                        $multipartContents[] = [
                            'name' => $formParamName,
                            'contents' => $formParamValueItem
                        ];
                    }
                }
                // for HTTP post (form)
                $httpBody = new MultipartStream($multipartContents);

            } elseif (stripos($headers['Content-Type'], 'application/json') !== false) {
                # if Content-Type contains "application/json", json_encode the form parameters
                $httpBody = \GuzzleHttp\Utils::jsonEncode($formParams);
            } else {
                // for HTTP post (form)
                $httpBody = ObjectSerializer::buildQuery($formParams);
            }
        }

        // this endpoint requires API key authentication
        $apiKey = $this->config->getApiKeyWithPrefix('X-API-Key');
        if ($apiKey !== null) {
            $headers['X-API-Key'] = $apiKey;
        }

        $defaultHeaders = [];
        if ($this->config->getUserAgent()) {
            $defaultHeaders['User-Agent'] = $this->config->getUserAgent();
        }

        $headers = array_merge(
            $defaultHeaders,
            $headerParams,
            $headers
        );

        $operationHost = $this->config->getHost();
        $query = ObjectSerializer::buildQuery($queryParams);
        return new Request(
            'GET',
            $operationHost . $resourcePath . ($query ? "?{$query}" : ''),
            $headers,
            $httpBody
        );
    }

    /**
     * Operation apiV1TimestampsIdGet
     *
     * Zaman damgası detayı
     *
     * @param  string $id id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsIdGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \Imzala\Client\Model\ApiV1TimestampsIdGet200Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response
     */
    public function apiV1TimestampsIdGet($id, string $contentType = self::contentTypes['apiV1TimestampsIdGet'][0])
    {
        list($response) = $this->apiV1TimestampsIdGetWithHttpInfo($id, $contentType);
        return $response;
    }

    /**
     * Operation apiV1TimestampsIdGetWithHttpInfo
     *
     * Zaman damgası detayı
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsIdGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \Imzala\Client\Model\ApiV1TimestampsIdGet200Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1TimestampsIdGetWithHttpInfo($id, string $contentType = self::contentTypes['apiV1TimestampsIdGet'][0])
    {
        $request = $this->apiV1TimestampsIdGetRequest($id, $contentType);

        try {
            $options = $this->createHttpClientOption();
            try {
                $response = $this->client->send($request, $options);
            } catch (RequestException $e) {
                throw new ApiException(
                    "[{$e->getCode()}] {$e->getMessage()}",
                    (int) $e->getCode(),
                    $e->getResponse() ? $e->getResponse()->getHeaders() : null,
                    $e->getResponse() ? (string) $e->getResponse()->getBody() : null
                );
            } catch (ConnectException $e) {
                throw new ApiException(
                    "[{$e->getCode()}] {$e->getMessage()}",
                    (int) $e->getCode(),
                    null,
                    null
                );
            }

            $statusCode = $response->getStatusCode();


            switch($statusCode) {
                case 200:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TimestampsIdGet200Response',
                        $request,
                        $response,
                    );
                case 401:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesGet401Response',
                        $request,
                        $response,
                    );
                case 404:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
                        $request,
                        $response,
                    );
            }

            

            if ($statusCode < 200 || $statusCode > 299) {
                throw new ApiException(
                    sprintf(
                        '[%d] Error connecting to the API (%s)',
                        $statusCode,
                        (string) $request->getUri()
                    ),
                    $statusCode,
                    $response->getHeaders(),
                    (string) $response->getBody()
                );
            }

            return $this->handleResponseWithDataType(
                '\Imzala\Client\Model\ApiV1TimestampsIdGet200Response',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 200:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TimestampsIdGet200Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 401:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TemplatesGet401Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 404:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
            }
        

            throw $e;
        }
    }

    /**
     * Operation apiV1TimestampsIdGetAsync
     *
     * Zaman damgası detayı
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsIdGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1TimestampsIdGetAsync($id, string $contentType = self::contentTypes['apiV1TimestampsIdGet'][0])
    {
        return $this->apiV1TimestampsIdGetAsyncWithHttpInfo($id, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1TimestampsIdGetAsyncWithHttpInfo
     *
     * Zaman damgası detayı
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsIdGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1TimestampsIdGetAsyncWithHttpInfo($id, string $contentType = self::contentTypes['apiV1TimestampsIdGet'][0])
    {
        $returnType = '\Imzala\Client\Model\ApiV1TimestampsIdGet200Response';
        $request = $this->apiV1TimestampsIdGetRequest($id, $contentType);

        return $this->client
            ->sendAsync($request, $this->createHttpClientOption())
            ->then(
                function ($response) use ($returnType) {
                    if ($returnType === '\SplFileObject') {
                        $content = $response->getBody(); //stream goes to serializer
                    } else {
                        $content = (string) $response->getBody();
                        if ($returnType !== 'string') {
                            $content = json_decode($content);
                        }
                    }

                    return [
                        ObjectSerializer::deserialize($content, $returnType, []),
                        $response->getStatusCode(),
                        $response->getHeaders()
                    ];
                },
                function ($exception) {
                    $response = $exception->getResponse();
                    $statusCode = $response->getStatusCode();
                    throw new ApiException(
                        sprintf(
                            '[%d] Error connecting to the API (%s)',
                            $statusCode,
                            $exception->getRequest()->getUri()
                        ),
                        $statusCode,
                        $response->getHeaders(),
                        (string) $response->getBody()
                    );
                }
            );
    }

    /**
     * Create request for operation 'apiV1TimestampsIdGet'
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsIdGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1TimestampsIdGetRequest($id, string $contentType = self::contentTypes['apiV1TimestampsIdGet'][0])
    {

        // verify the required parameter 'id' is set
        if ($id === null || (is_array($id) && count($id) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $id when calling apiV1TimestampsIdGet'
            );
        }


        $resourcePath = '/api/v1/timestamps/{id}';
        $formParams = [];
        $queryParams = [];
        $headerParams = [];
        $httpBody = '';
        $multipart = false;



        // path params
        if ($id !== null) {
            $resourcePath = str_replace(
                '{id}',
                ObjectSerializer::toPathValue($id),
                $resourcePath
            );
        }


        $headers = $this->headerSelector->selectHeaders(
            ['application/json', ],
            $contentType,
            $multipart
        );

        // for model (json/xml)
        if (count($formParams) > 0) {
            if ($multipart) {
                $multipartContents = [];
                foreach ($formParams as $formParamName => $formParamValue) {
                    $formParamValueItems = is_array($formParamValue) ? $formParamValue : [$formParamValue];
                    foreach ($formParamValueItems as $formParamValueItem) {
                        $multipartContents[] = [
                            'name' => $formParamName,
                            'contents' => $formParamValueItem
                        ];
                    }
                }
                // for HTTP post (form)
                $httpBody = new MultipartStream($multipartContents);

            } elseif (stripos($headers['Content-Type'], 'application/json') !== false) {
                # if Content-Type contains "application/json", json_encode the form parameters
                $httpBody = \GuzzleHttp\Utils::jsonEncode($formParams);
            } else {
                // for HTTP post (form)
                $httpBody = ObjectSerializer::buildQuery($formParams);
            }
        }

        // this endpoint requires API key authentication
        $apiKey = $this->config->getApiKeyWithPrefix('X-API-Key');
        if ($apiKey !== null) {
            $headers['X-API-Key'] = $apiKey;
        }

        $defaultHeaders = [];
        if ($this->config->getUserAgent()) {
            $defaultHeaders['User-Agent'] = $this->config->getUserAgent();
        }

        $headers = array_merge(
            $defaultHeaders,
            $headerParams,
            $headers
        );

        $operationHost = $this->config->getHost();
        $query = ObjectSerializer::buildQuery($queryParams);
        return new Request(
            'GET',
            $operationHost . $resourcePath . ($query ? "?{$query}" : ''),
            $headers,
            $httpBody
        );
    }

    /**
     * Operation apiV1TimestampsPost
     *
     * Zaman damgası oluştur (eser tescil)
     *
     * @param  \SplFileObject $file Damgalanacak dosya (maks. 50 MB) (required)
     * @param  string|null $idempotency_key Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez. (optional)
     * @param  string|null $description Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)
     * @param  string|null $owner_first_name Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)
     * @param  string|null $owner_last_name Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \Imzala\Client\Model\ApiV1TimestampsPost201Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiError
     */
    public function apiV1TimestampsPost($file, $idempotency_key = null, $description = null, $owner_first_name = null, $owner_last_name = null, string $contentType = self::contentTypes['apiV1TimestampsPost'][0])
    {
        list($response) = $this->apiV1TimestampsPostWithHttpInfo($file, $idempotency_key, $description, $owner_first_name, $owner_last_name, $contentType);
        return $response;
    }

    /**
     * Operation apiV1TimestampsPostWithHttpInfo
     *
     * Zaman damgası oluştur (eser tescil)
     *
     * @param  \SplFileObject $file Damgalanacak dosya (maks. 50 MB) (required)
     * @param  string|null $idempotency_key Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez. (optional)
     * @param  string|null $description Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)
     * @param  string|null $owner_first_name Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)
     * @param  string|null $owner_last_name Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \Imzala\Client\Model\ApiV1TimestampsPost201Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiError, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1TimestampsPostWithHttpInfo($file, $idempotency_key = null, $description = null, $owner_first_name = null, $owner_last_name = null, string $contentType = self::contentTypes['apiV1TimestampsPost'][0])
    {
        $request = $this->apiV1TimestampsPostRequest($file, $idempotency_key, $description, $owner_first_name, $owner_last_name, $contentType);

        try {
            $options = $this->createHttpClientOption();
            try {
                $response = $this->client->send($request, $options);
            } catch (RequestException $e) {
                throw new ApiException(
                    "[{$e->getCode()}] {$e->getMessage()}",
                    (int) $e->getCode(),
                    $e->getResponse() ? $e->getResponse()->getHeaders() : null,
                    $e->getResponse() ? (string) $e->getResponse()->getBody() : null
                );
            } catch (ConnectException $e) {
                throw new ApiException(
                    "[{$e->getCode()}] {$e->getMessage()}",
                    (int) $e->getCode(),
                    null,
                    null
                );
            }

            $statusCode = $response->getStatusCode();


            switch($statusCode) {
                case 201:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TimestampsPost201Response',
                        $request,
                        $response,
                    );
                case 401:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesGet401Response',
                        $request,
                        $response,
                    );
                case 402:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiError',
                        $request,
                        $response,
                    );
                case 403:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiError',
                        $request,
                        $response,
                    );
                case 415:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiError',
                        $request,
                        $response,
                    );
                case 422:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiError',
                        $request,
                        $response,
                    );
                case 429:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiError',
                        $request,
                        $response,
                    );
                case 500:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiError',
                        $request,
                        $response,
                    );
                case 503:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiError',
                        $request,
                        $response,
                    );
            }

            

            if ($statusCode < 200 || $statusCode > 299) {
                throw new ApiException(
                    sprintf(
                        '[%d] Error connecting to the API (%s)',
                        $statusCode,
                        (string) $request->getUri()
                    ),
                    $statusCode,
                    $response->getHeaders(),
                    (string) $response->getBody()
                );
            }

            return $this->handleResponseWithDataType(
                '\Imzala\Client\Model\ApiV1TimestampsPost201Response',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 201:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TimestampsPost201Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 401:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TemplatesGet401Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 402:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiError',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 403:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiError',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 415:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiError',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 422:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiError',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 429:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiError',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 500:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiError',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 503:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiError',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
            }
        

            throw $e;
        }
    }

    /**
     * Operation apiV1TimestampsPostAsync
     *
     * Zaman damgası oluştur (eser tescil)
     *
     * @param  \SplFileObject $file Damgalanacak dosya (maks. 50 MB) (required)
     * @param  string|null $idempotency_key Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez. (optional)
     * @param  string|null $description Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)
     * @param  string|null $owner_first_name Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)
     * @param  string|null $owner_last_name Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1TimestampsPostAsync($file, $idempotency_key = null, $description = null, $owner_first_name = null, $owner_last_name = null, string $contentType = self::contentTypes['apiV1TimestampsPost'][0])
    {
        return $this->apiV1TimestampsPostAsyncWithHttpInfo($file, $idempotency_key, $description, $owner_first_name, $owner_last_name, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1TimestampsPostAsyncWithHttpInfo
     *
     * Zaman damgası oluştur (eser tescil)
     *
     * @param  \SplFileObject $file Damgalanacak dosya (maks. 50 MB) (required)
     * @param  string|null $idempotency_key Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez. (optional)
     * @param  string|null $description Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)
     * @param  string|null $owner_first_name Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)
     * @param  string|null $owner_last_name Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1TimestampsPostAsyncWithHttpInfo($file, $idempotency_key = null, $description = null, $owner_first_name = null, $owner_last_name = null, string $contentType = self::contentTypes['apiV1TimestampsPost'][0])
    {
        $returnType = '\Imzala\Client\Model\ApiV1TimestampsPost201Response';
        $request = $this->apiV1TimestampsPostRequest($file, $idempotency_key, $description, $owner_first_name, $owner_last_name, $contentType);

        return $this->client
            ->sendAsync($request, $this->createHttpClientOption())
            ->then(
                function ($response) use ($returnType) {
                    if ($returnType === '\SplFileObject') {
                        $content = $response->getBody(); //stream goes to serializer
                    } else {
                        $content = (string) $response->getBody();
                        if ($returnType !== 'string') {
                            $content = json_decode($content);
                        }
                    }

                    return [
                        ObjectSerializer::deserialize($content, $returnType, []),
                        $response->getStatusCode(),
                        $response->getHeaders()
                    ];
                },
                function ($exception) {
                    $response = $exception->getResponse();
                    $statusCode = $response->getStatusCode();
                    throw new ApiException(
                        sprintf(
                            '[%d] Error connecting to the API (%s)',
                            $statusCode,
                            $exception->getRequest()->getUri()
                        ),
                        $statusCode,
                        $response->getHeaders(),
                        (string) $response->getBody()
                    );
                }
            );
    }

    /**
     * Create request for operation 'apiV1TimestampsPost'
     *
     * @param  \SplFileObject $file Damgalanacak dosya (maks. 50 MB) (required)
     * @param  string|null $idempotency_key Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez. (optional)
     * @param  string|null $description Kayıt açıklaması (opsiyonel, max 500 karakter) (optional)
     * @param  string|null $owner_first_name Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) (optional)
     * @param  string|null $owner_last_name Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1TimestampsPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1TimestampsPostRequest($file, $idempotency_key = null, $description = null, $owner_first_name = null, $owner_last_name = null, string $contentType = self::contentTypes['apiV1TimestampsPost'][0])
    {

        // verify the required parameter 'file' is set
        if ($file === null || (is_array($file) && count($file) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $file when calling apiV1TimestampsPost'
            );
        }






        $resourcePath = '/api/v1/timestamps';
        $formParams = [];
        $queryParams = [];
        $headerParams = [];
        $httpBody = '';
        $multipart = false;


        // header params
        if ($idempotency_key !== null) {
            $headerParams['Idempotency-Key'] = ObjectSerializer::toHeaderValue($idempotency_key);
        }


        // form params
        $formDataProcessor = new FormDataProcessor();

        $formData = $formDataProcessor->prepare([
            'file' => $file,
            'description' => $description,
            'owner_first_name' => $owner_first_name,
            'owner_last_name' => $owner_last_name,
        ]);

        $formParams = $formDataProcessor->flatten($formData);
        $multipart = $formDataProcessor->has_file;

        $multipart = true;
        $headers = $this->headerSelector->selectHeaders(
            ['application/json', ],
            $contentType,
            $multipart
        );

        // for model (json/xml)
        if (count($formParams) > 0) {
            if ($multipart) {
                $multipartContents = [];
                foreach ($formParams as $formParamName => $formParamValue) {
                    $formParamValueItems = is_array($formParamValue) ? $formParamValue : [$formParamValue];
                    foreach ($formParamValueItems as $formParamValueItem) {
                        $multipartContents[] = [
                            'name' => $formParamName,
                            'contents' => $formParamValueItem
                        ];
                    }
                }
                // for HTTP post (form)
                $httpBody = new MultipartStream($multipartContents);

            } elseif (stripos($headers['Content-Type'], 'application/json') !== false) {
                # if Content-Type contains "application/json", json_encode the form parameters
                $httpBody = \GuzzleHttp\Utils::jsonEncode($formParams);
            } else {
                // for HTTP post (form)
                $httpBody = ObjectSerializer::buildQuery($formParams);
            }
        }

        // this endpoint requires API key authentication
        $apiKey = $this->config->getApiKeyWithPrefix('X-API-Key');
        if ($apiKey !== null) {
            $headers['X-API-Key'] = $apiKey;
        }

        $defaultHeaders = [];
        if ($this->config->getUserAgent()) {
            $defaultHeaders['User-Agent'] = $this->config->getUserAgent();
        }

        $headers = array_merge(
            $defaultHeaders,
            $headerParams,
            $headers
        );

        $operationHost = $this->config->getHost();
        $query = ObjectSerializer::buildQuery($queryParams);
        return new Request(
            'POST',
            $operationHost . $resourcePath . ($query ? "?{$query}" : ''),
            $headers,
            $httpBody
        );
    }

    /**
     * Create http client option
     *
     * @throws \RuntimeException on file opening failure
     * @return array of http client options
     */
    protected function createHttpClientOption()
    {
        $options = [];
        if ($this->config->getDebug()) {
            $options[RequestOptions::DEBUG] = fopen($this->config->getDebugFile(), 'a');
            if (!$options[RequestOptions::DEBUG]) {
                throw new \RuntimeException('Failed to open the debug file: ' . $this->config->getDebugFile());
            }
        }

        if ($this->config->getCertFile()) {
            $options[RequestOptions::CERT] = $this->config->getCertFile();
        }

        if ($this->config->getKeyFile()) {
            $options[RequestOptions::SSL_KEY] = $this->config->getKeyFile();
        }

        return $options;
    }

    private function handleResponseWithDataType(
        string $dataType,
        RequestInterface $request,
        ResponseInterface $response
    ): array {
        if ($dataType === '\SplFileObject') {
            $content = $response->getBody(); //stream goes to serializer
        } else {
            $content = (string) $response->getBody();
            if ($dataType !== 'string') {
                try {
                    $content = json_decode($content, false, 512, JSON_THROW_ON_ERROR);
                } catch (\JsonException $exception) {
                    throw new ApiException(
                        sprintf(
                            'Error JSON decoding server response (%s)',
                            $request->getUri()
                        ),
                        $response->getStatusCode(),
                        $response->getHeaders(),
                        $content
                    );
                }
            }
        }

        return [
            ObjectSerializer::deserialize($content, $dataType, []),
            $response->getStatusCode(),
            $response->getHeaders()
        ];
    }

    private function responseWithinRangeCode(
        string $rangeCode,
        int $statusCode
    ): bool {
        $left = (int) ($rangeCode[0].'00');
        $right = (int) ($rangeCode[0].'99');

        return $statusCode >= $left && $statusCode <= $right;
    }
}
