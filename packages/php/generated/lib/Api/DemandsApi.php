<?php
/**
 * DemandsApi
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
 * imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.6.0 · **Son güncelleme:** 2026-06-30  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API key kullanıyorsanız `X-Workspace-Id` header'ı göndermeniz gerekir (organizasyon UUID'si). Kişisel anahtarlar için bu header gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - 60 istek/dakika per API key - Aşılırsa 429 döner  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (6) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 6 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme.
 *
 * The version of the OpenAPI document: 1.7.0
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
 * DemandsApi Class Doc Comment
 *
 * @category Class
 * @package  Imzala\Client
 * @author   OpenAPI Generator team
 * @link     https://openapi-generator.tech
 */
class DemandsApi
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
        'apiV1DemandsGet' => [
            'application/json',
        ],
        'apiV1DemandsIdCancelPost' => [
            'application/json',
        ],
        'apiV1DemandsIdCertificateGet' => [
            'application/json',
        ],
        'apiV1DemandsIdDelete' => [
            'application/json',
        ],
        'apiV1DemandsIdEmbedSessionPost' => [
            'application/json',
        ],
        'apiV1DemandsIdGet' => [
            'application/json',
        ],
        'apiV1DemandsIdItemsPost' => [
            'application/json',
        ],
        'apiV1DemandsIdPartiesPartyIdResendPost' => [
            'application/json',
        ],
        'apiV1DemandsIdPdfGet' => [
            'application/json',
        ],
        'apiV1DemandsIdTimelineGet' => [
            'application/json',
        ],
        'apiV1DemandsPost' => [
            'application/json',
        ],
        'apiV1DemandsUploadPost' => [
            'multipart/form-data',
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
     * Operation apiV1DemandsGet
     *
     * Sözleşme listesi (counts-only, PII&#39;siz)
     *
     * @param  string|null $status status (optional)
     * @param  string|null $q Başlık araması (optional)
     * @param  \DateTime|null $from from (optional)
     * @param  \DateTime|null $to to (optional)
     * @param  string|null $template_id template_id (optional)
     * @param  int|null $page page (optional, default to 1)
     * @param  int|null $limit Sayfa boyutu (page_size ile aynı) (optional, default to 20)
     * @param  string|null $sort alan:yön (ör. createdAt:desc) (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \Imzala\Client\Model\ApiV1DemandsGet200Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response
     */
    public function apiV1DemandsGet($status = null, $q = null, $from = null, $to = null, $template_id = null, $page = 1, $limit = 20, $sort = null, string $contentType = self::contentTypes['apiV1DemandsGet'][0])
    {
        list($response) = $this->apiV1DemandsGetWithHttpInfo($status, $q, $from, $to, $template_id, $page, $limit, $sort, $contentType);
        return $response;
    }

    /**
     * Operation apiV1DemandsGetWithHttpInfo
     *
     * Sözleşme listesi (counts-only, PII&#39;siz)
     *
     * @param  string|null $status (optional)
     * @param  string|null $q Başlık araması (optional)
     * @param  \DateTime|null $from (optional)
     * @param  \DateTime|null $to (optional)
     * @param  string|null $template_id (optional)
     * @param  int|null $page (optional, default to 1)
     * @param  int|null $limit Sayfa boyutu (page_size ile aynı) (optional, default to 20)
     * @param  string|null $sort alan:yön (ör. createdAt:desc) (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \Imzala\Client\Model\ApiV1DemandsGet200Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1DemandsGetWithHttpInfo($status = null, $q = null, $from = null, $to = null, $template_id = null, $page = 1, $limit = 20, $sort = null, string $contentType = self::contentTypes['apiV1DemandsGet'][0])
    {
        $request = $this->apiV1DemandsGetRequest($status, $q, $from, $to, $template_id, $page, $limit, $sort, $contentType);

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
                        '\Imzala\Client\Model\ApiV1DemandsGet200Response',
                        $request,
                        $response,
                    );
                case 401:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesGet401Response',
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
                '\Imzala\Client\Model\ApiV1DemandsGet200Response',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 200:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1DemandsGet200Response',
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
            }
        

            throw $e;
        }
    }

    /**
     * Operation apiV1DemandsGetAsync
     *
     * Sözleşme listesi (counts-only, PII&#39;siz)
     *
     * @param  string|null $status (optional)
     * @param  string|null $q Başlık araması (optional)
     * @param  \DateTime|null $from (optional)
     * @param  \DateTime|null $to (optional)
     * @param  string|null $template_id (optional)
     * @param  int|null $page (optional, default to 1)
     * @param  int|null $limit Sayfa boyutu (page_size ile aynı) (optional, default to 20)
     * @param  string|null $sort alan:yön (ör. createdAt:desc) (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsGetAsync($status = null, $q = null, $from = null, $to = null, $template_id = null, $page = 1, $limit = 20, $sort = null, string $contentType = self::contentTypes['apiV1DemandsGet'][0])
    {
        return $this->apiV1DemandsGetAsyncWithHttpInfo($status, $q, $from, $to, $template_id, $page, $limit, $sort, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1DemandsGetAsyncWithHttpInfo
     *
     * Sözleşme listesi (counts-only, PII&#39;siz)
     *
     * @param  string|null $status (optional)
     * @param  string|null $q Başlık araması (optional)
     * @param  \DateTime|null $from (optional)
     * @param  \DateTime|null $to (optional)
     * @param  string|null $template_id (optional)
     * @param  int|null $page (optional, default to 1)
     * @param  int|null $limit Sayfa boyutu (page_size ile aynı) (optional, default to 20)
     * @param  string|null $sort alan:yön (ör. createdAt:desc) (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsGetAsyncWithHttpInfo($status = null, $q = null, $from = null, $to = null, $template_id = null, $page = 1, $limit = 20, $sort = null, string $contentType = self::contentTypes['apiV1DemandsGet'][0])
    {
        $returnType = '\Imzala\Client\Model\ApiV1DemandsGet200Response';
        $request = $this->apiV1DemandsGetRequest($status, $q, $from, $to, $template_id, $page, $limit, $sort, $contentType);

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
     * Create request for operation 'apiV1DemandsGet'
     *
     * @param  string|null $status (optional)
     * @param  string|null $q Başlık araması (optional)
     * @param  \DateTime|null $from (optional)
     * @param  \DateTime|null $to (optional)
     * @param  string|null $template_id (optional)
     * @param  int|null $page (optional, default to 1)
     * @param  int|null $limit Sayfa boyutu (page_size ile aynı) (optional, default to 20)
     * @param  string|null $sort alan:yön (ör. createdAt:desc) (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1DemandsGetRequest($status = null, $q = null, $from = null, $to = null, $template_id = null, $page = 1, $limit = 20, $sort = null, string $contentType = self::contentTypes['apiV1DemandsGet'][0])
    {










        $resourcePath = '/api/v1/demands';
        $formParams = [];
        $queryParams = [];
        $headerParams = [];
        $httpBody = '';
        $multipart = false;

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
            $q,
            'q', // param base name
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
            $template_id,
            'template_id', // param base name
            'string', // openApiType
            'form', // style
            true, // explode
            false // required
        ) ?? []);
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
     * Operation apiV1DemandsIdCancelPost
     *
     * Sözleşme iptal (void)
     *
     * @param  string $id id (required)
     * @param  \Imzala\Client\Model\ApiV1DemandsIdCancelPostRequest|null $api_v1_demands_id_cancel_post_request api_v1_demands_id_cancel_post_request (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdCancelPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \Imzala\Client\Model\ApiV1DemandsIdCancelPost200Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response
     */
    public function apiV1DemandsIdCancelPost($id, $api_v1_demands_id_cancel_post_request = null, string $contentType = self::contentTypes['apiV1DemandsIdCancelPost'][0])
    {
        list($response) = $this->apiV1DemandsIdCancelPostWithHttpInfo($id, $api_v1_demands_id_cancel_post_request, $contentType);
        return $response;
    }

    /**
     * Operation apiV1DemandsIdCancelPostWithHttpInfo
     *
     * Sözleşme iptal (void)
     *
     * @param  string $id (required)
     * @param  \Imzala\Client\Model\ApiV1DemandsIdCancelPostRequest|null $api_v1_demands_id_cancel_post_request (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdCancelPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \Imzala\Client\Model\ApiV1DemandsIdCancelPost200Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1DemandsIdCancelPostWithHttpInfo($id, $api_v1_demands_id_cancel_post_request = null, string $contentType = self::contentTypes['apiV1DemandsIdCancelPost'][0])
    {
        $request = $this->apiV1DemandsIdCancelPostRequest($id, $api_v1_demands_id_cancel_post_request, $contentType);

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
                        '\Imzala\Client\Model\ApiV1DemandsIdCancelPost200Response',
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
                '\Imzala\Client\Model\ApiV1DemandsIdCancelPost200Response',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 200:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1DemandsIdCancelPost200Response',
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
     * Operation apiV1DemandsIdCancelPostAsync
     *
     * Sözleşme iptal (void)
     *
     * @param  string $id (required)
     * @param  \Imzala\Client\Model\ApiV1DemandsIdCancelPostRequest|null $api_v1_demands_id_cancel_post_request (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdCancelPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdCancelPostAsync($id, $api_v1_demands_id_cancel_post_request = null, string $contentType = self::contentTypes['apiV1DemandsIdCancelPost'][0])
    {
        return $this->apiV1DemandsIdCancelPostAsyncWithHttpInfo($id, $api_v1_demands_id_cancel_post_request, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1DemandsIdCancelPostAsyncWithHttpInfo
     *
     * Sözleşme iptal (void)
     *
     * @param  string $id (required)
     * @param  \Imzala\Client\Model\ApiV1DemandsIdCancelPostRequest|null $api_v1_demands_id_cancel_post_request (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdCancelPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdCancelPostAsyncWithHttpInfo($id, $api_v1_demands_id_cancel_post_request = null, string $contentType = self::contentTypes['apiV1DemandsIdCancelPost'][0])
    {
        $returnType = '\Imzala\Client\Model\ApiV1DemandsIdCancelPost200Response';
        $request = $this->apiV1DemandsIdCancelPostRequest($id, $api_v1_demands_id_cancel_post_request, $contentType);

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
     * Create request for operation 'apiV1DemandsIdCancelPost'
     *
     * @param  string $id (required)
     * @param  \Imzala\Client\Model\ApiV1DemandsIdCancelPostRequest|null $api_v1_demands_id_cancel_post_request (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdCancelPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1DemandsIdCancelPostRequest($id, $api_v1_demands_id_cancel_post_request = null, string $contentType = self::contentTypes['apiV1DemandsIdCancelPost'][0])
    {

        // verify the required parameter 'id' is set
        if ($id === null || (is_array($id) && count($id) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $id when calling apiV1DemandsIdCancelPost'
            );
        }



        $resourcePath = '/api/v1/demands/{id}/cancel';
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
        if (isset($api_v1_demands_id_cancel_post_request)) {
            if (stripos($headers['Content-Type'], 'application/json') !== false) {
                # if Content-Type contains "application/json", json_encode the body
                $httpBody = \GuzzleHttp\Utils::jsonEncode(ObjectSerializer::sanitizeForSerialization($api_v1_demands_id_cancel_post_request));
            } else {
                $httpBody = $api_v1_demands_id_cancel_post_request;
            }
        } elseif (count($formParams) > 0) {
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
     * Operation apiV1DemandsIdCertificateGet
     *
     * Tamamlanma sertifikası (PAdES B-T)
     *
     * @param  string $id id (required)
     * @param  string|null $lang tr | en (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdCertificateGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \SplFileObject|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response
     */
    public function apiV1DemandsIdCertificateGet($id, $lang = null, string $contentType = self::contentTypes['apiV1DemandsIdCertificateGet'][0])
    {
        list($response) = $this->apiV1DemandsIdCertificateGetWithHttpInfo($id, $lang, $contentType);
        return $response;
    }

    /**
     * Operation apiV1DemandsIdCertificateGetWithHttpInfo
     *
     * Tamamlanma sertifikası (PAdES B-T)
     *
     * @param  string $id (required)
     * @param  string|null $lang tr | en (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdCertificateGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \SplFileObject|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1DemandsIdCertificateGetWithHttpInfo($id, $lang = null, string $contentType = self::contentTypes['apiV1DemandsIdCertificateGet'][0])
    {
        $request = $this->apiV1DemandsIdCertificateGetRequest($id, $lang, $contentType);

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
                        '\SplFileObject',
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
                '\SplFileObject',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 200:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\SplFileObject',
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
     * Operation apiV1DemandsIdCertificateGetAsync
     *
     * Tamamlanma sertifikası (PAdES B-T)
     *
     * @param  string $id (required)
     * @param  string|null $lang tr | en (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdCertificateGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdCertificateGetAsync($id, $lang = null, string $contentType = self::contentTypes['apiV1DemandsIdCertificateGet'][0])
    {
        return $this->apiV1DemandsIdCertificateGetAsyncWithHttpInfo($id, $lang, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1DemandsIdCertificateGetAsyncWithHttpInfo
     *
     * Tamamlanma sertifikası (PAdES B-T)
     *
     * @param  string $id (required)
     * @param  string|null $lang tr | en (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdCertificateGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdCertificateGetAsyncWithHttpInfo($id, $lang = null, string $contentType = self::contentTypes['apiV1DemandsIdCertificateGet'][0])
    {
        $returnType = '\SplFileObject';
        $request = $this->apiV1DemandsIdCertificateGetRequest($id, $lang, $contentType);

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
     * Create request for operation 'apiV1DemandsIdCertificateGet'
     *
     * @param  string $id (required)
     * @param  string|null $lang tr | en (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdCertificateGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1DemandsIdCertificateGetRequest($id, $lang = null, string $contentType = self::contentTypes['apiV1DemandsIdCertificateGet'][0])
    {

        // verify the required parameter 'id' is set
        if ($id === null || (is_array($id) && count($id) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $id when calling apiV1DemandsIdCertificateGet'
            );
        }



        $resourcePath = '/api/v1/demands/{id}/certificate';
        $formParams = [];
        $queryParams = [];
        $headerParams = [];
        $httpBody = '';
        $multipart = false;

        // query params
        $queryParams = array_merge($queryParams, ObjectSerializer::toQueryValue(
            $lang,
            'lang', // param base name
            'string', // openApiType
            'form', // style
            true, // explode
            false // required
        ) ?? []);


        // path params
        if ($id !== null) {
            $resourcePath = str_replace(
                '{id}',
                ObjectSerializer::toPathValue($id),
                $resourcePath
            );
        }


        $headers = $this->headerSelector->selectHeaders(
            ['application/pdf', 'application/json', ],
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
     * Operation apiV1DemandsIdDelete
     *
     * Sözleşme sil (yalnızca tamamlanmamış)
     *
     * @param  string $id id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdDelete'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \Imzala\Client\Model\ApiV1TemplatesIdDelete200Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiV1DemandsIdDelete409Response
     */
    public function apiV1DemandsIdDelete($id, string $contentType = self::contentTypes['apiV1DemandsIdDelete'][0])
    {
        list($response) = $this->apiV1DemandsIdDeleteWithHttpInfo($id, $contentType);
        return $response;
    }

    /**
     * Operation apiV1DemandsIdDeleteWithHttpInfo
     *
     * Sözleşme sil (yalnızca tamamlanmamış)
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdDelete'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \Imzala\Client\Model\ApiV1TemplatesIdDelete200Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiV1DemandsIdDelete409Response, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1DemandsIdDeleteWithHttpInfo($id, string $contentType = self::contentTypes['apiV1DemandsIdDelete'][0])
    {
        $request = $this->apiV1DemandsIdDeleteRequest($id, $contentType);

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
                        '\Imzala\Client\Model\ApiV1TemplatesIdDelete200Response',
                        $request,
                        $response,
                    );
                case 404:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
                        $request,
                        $response,
                    );
                case 409:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1DemandsIdDelete409Response',
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
                '\Imzala\Client\Model\ApiV1TemplatesIdDelete200Response',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 200:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TemplatesIdDelete200Response',
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
                case 409:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1DemandsIdDelete409Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
            }
        

            throw $e;
        }
    }

    /**
     * Operation apiV1DemandsIdDeleteAsync
     *
     * Sözleşme sil (yalnızca tamamlanmamış)
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdDelete'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdDeleteAsync($id, string $contentType = self::contentTypes['apiV1DemandsIdDelete'][0])
    {
        return $this->apiV1DemandsIdDeleteAsyncWithHttpInfo($id, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1DemandsIdDeleteAsyncWithHttpInfo
     *
     * Sözleşme sil (yalnızca tamamlanmamış)
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdDelete'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdDeleteAsyncWithHttpInfo($id, string $contentType = self::contentTypes['apiV1DemandsIdDelete'][0])
    {
        $returnType = '\Imzala\Client\Model\ApiV1TemplatesIdDelete200Response';
        $request = $this->apiV1DemandsIdDeleteRequest($id, $contentType);

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
     * Create request for operation 'apiV1DemandsIdDelete'
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdDelete'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1DemandsIdDeleteRequest($id, string $contentType = self::contentTypes['apiV1DemandsIdDelete'][0])
    {

        // verify the required parameter 'id' is set
        if ($id === null || (is_array($id) && count($id) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $id when calling apiV1DemandsIdDelete'
            );
        }


        $resourcePath = '/api/v1/demands/{id}';
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
            'DELETE',
            $operationHost . $resourcePath . ($query ? "?{$query}" : ''),
            $headers,
            $httpBody
        );
    }

    /**
     * Operation apiV1DemandsIdEmbedSessionPost
     *
     * Gömülü imza oturumu başlat (embed token mint)
     *
     * @param  string $id Sözleşme (demand) ID (required)
     * @param  \Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPostRequest $api_v1_demands_id_embed_session_post_request api_v1_demands_id_embed_session_post_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdEmbedSessionPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPost200Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiError
     */
    public function apiV1DemandsIdEmbedSessionPost($id, $api_v1_demands_id_embed_session_post_request, string $contentType = self::contentTypes['apiV1DemandsIdEmbedSessionPost'][0])
    {
        list($response) = $this->apiV1DemandsIdEmbedSessionPostWithHttpInfo($id, $api_v1_demands_id_embed_session_post_request, $contentType);
        return $response;
    }

    /**
     * Operation apiV1DemandsIdEmbedSessionPostWithHttpInfo
     *
     * Gömülü imza oturumu başlat (embed token mint)
     *
     * @param  string $id Sözleşme (demand) ID (required)
     * @param  \Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPostRequest $api_v1_demands_id_embed_session_post_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdEmbedSessionPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPost200Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response|\Imzala\Client\Model\ApiError|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiError, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1DemandsIdEmbedSessionPostWithHttpInfo($id, $api_v1_demands_id_embed_session_post_request, string $contentType = self::contentTypes['apiV1DemandsIdEmbedSessionPost'][0])
    {
        $request = $this->apiV1DemandsIdEmbedSessionPostRequest($id, $api_v1_demands_id_embed_session_post_request, $contentType);

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
                        '\Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPost200Response',
                        $request,
                        $response,
                    );
                case 400:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
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
                case 404:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
                        $request,
                        $response,
                    );
                case 409:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
                        $request,
                        $response,
                    );
                case 429:
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
                '\Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPost200Response',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 200:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPost200Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 400:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
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
                case 404:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 409:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
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
            }
        

            throw $e;
        }
    }

    /**
     * Operation apiV1DemandsIdEmbedSessionPostAsync
     *
     * Gömülü imza oturumu başlat (embed token mint)
     *
     * @param  string $id Sözleşme (demand) ID (required)
     * @param  \Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPostRequest $api_v1_demands_id_embed_session_post_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdEmbedSessionPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdEmbedSessionPostAsync($id, $api_v1_demands_id_embed_session_post_request, string $contentType = self::contentTypes['apiV1DemandsIdEmbedSessionPost'][0])
    {
        return $this->apiV1DemandsIdEmbedSessionPostAsyncWithHttpInfo($id, $api_v1_demands_id_embed_session_post_request, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1DemandsIdEmbedSessionPostAsyncWithHttpInfo
     *
     * Gömülü imza oturumu başlat (embed token mint)
     *
     * @param  string $id Sözleşme (demand) ID (required)
     * @param  \Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPostRequest $api_v1_demands_id_embed_session_post_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdEmbedSessionPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdEmbedSessionPostAsyncWithHttpInfo($id, $api_v1_demands_id_embed_session_post_request, string $contentType = self::contentTypes['apiV1DemandsIdEmbedSessionPost'][0])
    {
        $returnType = '\Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPost200Response';
        $request = $this->apiV1DemandsIdEmbedSessionPostRequest($id, $api_v1_demands_id_embed_session_post_request, $contentType);

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
     * Create request for operation 'apiV1DemandsIdEmbedSessionPost'
     *
     * @param  string $id Sözleşme (demand) ID (required)
     * @param  \Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPostRequest $api_v1_demands_id_embed_session_post_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdEmbedSessionPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1DemandsIdEmbedSessionPostRequest($id, $api_v1_demands_id_embed_session_post_request, string $contentType = self::contentTypes['apiV1DemandsIdEmbedSessionPost'][0])
    {

        // verify the required parameter 'id' is set
        if ($id === null || (is_array($id) && count($id) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $id when calling apiV1DemandsIdEmbedSessionPost'
            );
        }

        // verify the required parameter 'api_v1_demands_id_embed_session_post_request' is set
        if ($api_v1_demands_id_embed_session_post_request === null || (is_array($api_v1_demands_id_embed_session_post_request) && count($api_v1_demands_id_embed_session_post_request) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $api_v1_demands_id_embed_session_post_request when calling apiV1DemandsIdEmbedSessionPost'
            );
        }


        $resourcePath = '/api/v1/demands/{id}/embed-session';
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
        if (isset($api_v1_demands_id_embed_session_post_request)) {
            if (stripos($headers['Content-Type'], 'application/json') !== false) {
                # if Content-Type contains "application/json", json_encode the body
                $httpBody = \GuzzleHttp\Utils::jsonEncode(ObjectSerializer::sanitizeForSerialization($api_v1_demands_id_embed_session_post_request));
            } else {
                $httpBody = $api_v1_demands_id_embed_session_post_request;
            }
        } elseif (count($formParams) > 0) {
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
     * Operation apiV1DemandsIdGet
     *
     * Sözleşme durumu + imza ilerlemesi
     *
     * @param  string $id id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \Imzala\Client\Model\ApiV1DemandsIdGet200Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response
     */
    public function apiV1DemandsIdGet($id, string $contentType = self::contentTypes['apiV1DemandsIdGet'][0])
    {
        list($response) = $this->apiV1DemandsIdGetWithHttpInfo($id, $contentType);
        return $response;
    }

    /**
     * Operation apiV1DemandsIdGetWithHttpInfo
     *
     * Sözleşme durumu + imza ilerlemesi
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \Imzala\Client\Model\ApiV1DemandsIdGet200Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1DemandsIdGetWithHttpInfo($id, string $contentType = self::contentTypes['apiV1DemandsIdGet'][0])
    {
        $request = $this->apiV1DemandsIdGetRequest($id, $contentType);

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
                        '\Imzala\Client\Model\ApiV1DemandsIdGet200Response',
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
                '\Imzala\Client\Model\ApiV1DemandsIdGet200Response',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 200:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1DemandsIdGet200Response',
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
     * Operation apiV1DemandsIdGetAsync
     *
     * Sözleşme durumu + imza ilerlemesi
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdGetAsync($id, string $contentType = self::contentTypes['apiV1DemandsIdGet'][0])
    {
        return $this->apiV1DemandsIdGetAsyncWithHttpInfo($id, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1DemandsIdGetAsyncWithHttpInfo
     *
     * Sözleşme durumu + imza ilerlemesi
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdGetAsyncWithHttpInfo($id, string $contentType = self::contentTypes['apiV1DemandsIdGet'][0])
    {
        $returnType = '\Imzala\Client\Model\ApiV1DemandsIdGet200Response';
        $request = $this->apiV1DemandsIdGetRequest($id, $contentType);

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
     * Create request for operation 'apiV1DemandsIdGet'
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1DemandsIdGetRequest($id, string $contentType = self::contentTypes['apiV1DemandsIdGet'][0])
    {

        // verify the required parameter 'id' is set
        if ($id === null || (is_array($id) && count($id) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $id when calling apiV1DemandsIdGet'
            );
        }


        $resourcePath = '/api/v1/demands/{id}';
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
     * Operation apiV1DemandsIdItemsPost
     *
     * Sözleşmeye alan yerleştir (replace)
     *
     * @param  string $id id (required)
     * @param  \Imzala\Client\Model\UpsertItemsRequest $upsert_items_request upsert_items_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdItemsPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \Imzala\Client\Model\UpsertItemsResponse|\Imzala\Client\Model\ApiV1TemplatesGet401Response
     */
    public function apiV1DemandsIdItemsPost($id, $upsert_items_request, string $contentType = self::contentTypes['apiV1DemandsIdItemsPost'][0])
    {
        list($response) = $this->apiV1DemandsIdItemsPostWithHttpInfo($id, $upsert_items_request, $contentType);
        return $response;
    }

    /**
     * Operation apiV1DemandsIdItemsPostWithHttpInfo
     *
     * Sözleşmeye alan yerleştir (replace)
     *
     * @param  string $id (required)
     * @param  \Imzala\Client\Model\UpsertItemsRequest $upsert_items_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdItemsPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \Imzala\Client\Model\UpsertItemsResponse|\Imzala\Client\Model\ApiV1TemplatesGet401Response, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1DemandsIdItemsPostWithHttpInfo($id, $upsert_items_request, string $contentType = self::contentTypes['apiV1DemandsIdItemsPost'][0])
    {
        $request = $this->apiV1DemandsIdItemsPostRequest($id, $upsert_items_request, $contentType);

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
                        '\Imzala\Client\Model\UpsertItemsResponse',
                        $request,
                        $response,
                    );
                case 401:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesGet401Response',
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
                '\Imzala\Client\Model\UpsertItemsResponse',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 200:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\UpsertItemsResponse',
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
            }
        

            throw $e;
        }
    }

    /**
     * Operation apiV1DemandsIdItemsPostAsync
     *
     * Sözleşmeye alan yerleştir (replace)
     *
     * @param  string $id (required)
     * @param  \Imzala\Client\Model\UpsertItemsRequest $upsert_items_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdItemsPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdItemsPostAsync($id, $upsert_items_request, string $contentType = self::contentTypes['apiV1DemandsIdItemsPost'][0])
    {
        return $this->apiV1DemandsIdItemsPostAsyncWithHttpInfo($id, $upsert_items_request, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1DemandsIdItemsPostAsyncWithHttpInfo
     *
     * Sözleşmeye alan yerleştir (replace)
     *
     * @param  string $id (required)
     * @param  \Imzala\Client\Model\UpsertItemsRequest $upsert_items_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdItemsPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdItemsPostAsyncWithHttpInfo($id, $upsert_items_request, string $contentType = self::contentTypes['apiV1DemandsIdItemsPost'][0])
    {
        $returnType = '\Imzala\Client\Model\UpsertItemsResponse';
        $request = $this->apiV1DemandsIdItemsPostRequest($id, $upsert_items_request, $contentType);

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
     * Create request for operation 'apiV1DemandsIdItemsPost'
     *
     * @param  string $id (required)
     * @param  \Imzala\Client\Model\UpsertItemsRequest $upsert_items_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdItemsPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1DemandsIdItemsPostRequest($id, $upsert_items_request, string $contentType = self::contentTypes['apiV1DemandsIdItemsPost'][0])
    {

        // verify the required parameter 'id' is set
        if ($id === null || (is_array($id) && count($id) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $id when calling apiV1DemandsIdItemsPost'
            );
        }

        // verify the required parameter 'upsert_items_request' is set
        if ($upsert_items_request === null || (is_array($upsert_items_request) && count($upsert_items_request) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $upsert_items_request when calling apiV1DemandsIdItemsPost'
            );
        }


        $resourcePath = '/api/v1/demands/{id}/items';
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
        if (isset($upsert_items_request)) {
            if (stripos($headers['Content-Type'], 'application/json') !== false) {
                # if Content-Type contains "application/json", json_encode the body
                $httpBody = \GuzzleHttp\Utils::jsonEncode(ObjectSerializer::sanitizeForSerialization($upsert_items_request));
            } else {
                $httpBody = $upsert_items_request;
            }
        } elseif (count($formParams) > 0) {
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
     * Operation apiV1DemandsIdPartiesPartyIdResendPost
     *
     * Tekil tarafa imza davetini tekrar gönder
     *
     * @param  string $id id (required)
     * @param  string $party_id party_id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdPartiesPartyIdResendPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \Imzala\Client\Model\ApiV1DemandsIdPartiesPartyIdResendPost200Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response
     */
    public function apiV1DemandsIdPartiesPartyIdResendPost($id, $party_id, string $contentType = self::contentTypes['apiV1DemandsIdPartiesPartyIdResendPost'][0])
    {
        list($response) = $this->apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo($id, $party_id, $contentType);
        return $response;
    }

    /**
     * Operation apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo
     *
     * Tekil tarafa imza davetini tekrar gönder
     *
     * @param  string $id (required)
     * @param  string $party_id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdPartiesPartyIdResendPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \Imzala\Client\Model\ApiV1DemandsIdPartiesPartyIdResendPost200Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo($id, $party_id, string $contentType = self::contentTypes['apiV1DemandsIdPartiesPartyIdResendPost'][0])
    {
        $request = $this->apiV1DemandsIdPartiesPartyIdResendPostRequest($id, $party_id, $contentType);

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
                        '\Imzala\Client\Model\ApiV1DemandsIdPartiesPartyIdResendPost200Response',
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
                '\Imzala\Client\Model\ApiV1DemandsIdPartiesPartyIdResendPost200Response',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 200:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1DemandsIdPartiesPartyIdResendPost200Response',
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
     * Operation apiV1DemandsIdPartiesPartyIdResendPostAsync
     *
     * Tekil tarafa imza davetini tekrar gönder
     *
     * @param  string $id (required)
     * @param  string $party_id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdPartiesPartyIdResendPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdPartiesPartyIdResendPostAsync($id, $party_id, string $contentType = self::contentTypes['apiV1DemandsIdPartiesPartyIdResendPost'][0])
    {
        return $this->apiV1DemandsIdPartiesPartyIdResendPostAsyncWithHttpInfo($id, $party_id, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1DemandsIdPartiesPartyIdResendPostAsyncWithHttpInfo
     *
     * Tekil tarafa imza davetini tekrar gönder
     *
     * @param  string $id (required)
     * @param  string $party_id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdPartiesPartyIdResendPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdPartiesPartyIdResendPostAsyncWithHttpInfo($id, $party_id, string $contentType = self::contentTypes['apiV1DemandsIdPartiesPartyIdResendPost'][0])
    {
        $returnType = '\Imzala\Client\Model\ApiV1DemandsIdPartiesPartyIdResendPost200Response';
        $request = $this->apiV1DemandsIdPartiesPartyIdResendPostRequest($id, $party_id, $contentType);

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
     * Create request for operation 'apiV1DemandsIdPartiesPartyIdResendPost'
     *
     * @param  string $id (required)
     * @param  string $party_id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdPartiesPartyIdResendPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1DemandsIdPartiesPartyIdResendPostRequest($id, $party_id, string $contentType = self::contentTypes['apiV1DemandsIdPartiesPartyIdResendPost'][0])
    {

        // verify the required parameter 'id' is set
        if ($id === null || (is_array($id) && count($id) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $id when calling apiV1DemandsIdPartiesPartyIdResendPost'
            );
        }

        // verify the required parameter 'party_id' is set
        if ($party_id === null || (is_array($party_id) && count($party_id) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $party_id when calling apiV1DemandsIdPartiesPartyIdResendPost'
            );
        }


        $resourcePath = '/api/v1/demands/{id}/parties/{partyId}/resend';
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
        // path params
        if ($party_id !== null) {
            $resourcePath = str_replace(
                '{partyId}',
                ObjectSerializer::toPathValue($party_id),
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
            'POST',
            $operationHost . $resourcePath . ($query ? "?{$query}" : ''),
            $headers,
            $httpBody
        );
    }

    /**
     * Operation apiV1DemandsIdPdfGet
     *
     * İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
     *
     * @param  string $id id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdPdfGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \SplFileObject|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response
     */
    public function apiV1DemandsIdPdfGet($id, string $contentType = self::contentTypes['apiV1DemandsIdPdfGet'][0])
    {
        list($response) = $this->apiV1DemandsIdPdfGetWithHttpInfo($id, $contentType);
        return $response;
    }

    /**
     * Operation apiV1DemandsIdPdfGetWithHttpInfo
     *
     * İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdPdfGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \SplFileObject|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1DemandsIdPdfGetWithHttpInfo($id, string $contentType = self::contentTypes['apiV1DemandsIdPdfGet'][0])
    {
        $request = $this->apiV1DemandsIdPdfGetRequest($id, $contentType);

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
                        '\SplFileObject',
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
                '\SplFileObject',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 200:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\SplFileObject',
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
     * Operation apiV1DemandsIdPdfGetAsync
     *
     * İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdPdfGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdPdfGetAsync($id, string $contentType = self::contentTypes['apiV1DemandsIdPdfGet'][0])
    {
        return $this->apiV1DemandsIdPdfGetAsyncWithHttpInfo($id, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1DemandsIdPdfGetAsyncWithHttpInfo
     *
     * İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdPdfGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdPdfGetAsyncWithHttpInfo($id, string $contentType = self::contentTypes['apiV1DemandsIdPdfGet'][0])
    {
        $returnType = '\SplFileObject';
        $request = $this->apiV1DemandsIdPdfGetRequest($id, $contentType);

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
     * Create request for operation 'apiV1DemandsIdPdfGet'
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdPdfGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1DemandsIdPdfGetRequest($id, string $contentType = self::contentTypes['apiV1DemandsIdPdfGet'][0])
    {

        // verify the required parameter 'id' is set
        if ($id === null || (is_array($id) && count($id) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $id when calling apiV1DemandsIdPdfGet'
            );
        }


        $resourcePath = '/api/v1/demands/{id}/pdf';
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
            ['application/pdf', 'application/json', ],
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
     * Operation apiV1DemandsIdTimelineGet
     *
     * İmza denetim izi (maskeli)
     *
     * @param  string $id id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdTimelineGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \Imzala\Client\Model\ApiV1DemandsIdTimelineGet200Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response
     */
    public function apiV1DemandsIdTimelineGet($id, string $contentType = self::contentTypes['apiV1DemandsIdTimelineGet'][0])
    {
        list($response) = $this->apiV1DemandsIdTimelineGetWithHttpInfo($id, $contentType);
        return $response;
    }

    /**
     * Operation apiV1DemandsIdTimelineGetWithHttpInfo
     *
     * İmza denetim izi (maskeli)
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdTimelineGet'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \Imzala\Client\Model\ApiV1DemandsIdTimelineGet200Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1DemandsIdTimelineGetWithHttpInfo($id, string $contentType = self::contentTypes['apiV1DemandsIdTimelineGet'][0])
    {
        $request = $this->apiV1DemandsIdTimelineGetRequest($id, $contentType);

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
                        '\Imzala\Client\Model\ApiV1DemandsIdTimelineGet200Response',
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
                '\Imzala\Client\Model\ApiV1DemandsIdTimelineGet200Response',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 200:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1DemandsIdTimelineGet200Response',
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
     * Operation apiV1DemandsIdTimelineGetAsync
     *
     * İmza denetim izi (maskeli)
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdTimelineGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdTimelineGetAsync($id, string $contentType = self::contentTypes['apiV1DemandsIdTimelineGet'][0])
    {
        return $this->apiV1DemandsIdTimelineGetAsyncWithHttpInfo($id, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1DemandsIdTimelineGetAsyncWithHttpInfo
     *
     * İmza denetim izi (maskeli)
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdTimelineGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsIdTimelineGetAsyncWithHttpInfo($id, string $contentType = self::contentTypes['apiV1DemandsIdTimelineGet'][0])
    {
        $returnType = '\Imzala\Client\Model\ApiV1DemandsIdTimelineGet200Response';
        $request = $this->apiV1DemandsIdTimelineGetRequest($id, $contentType);

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
     * Create request for operation 'apiV1DemandsIdTimelineGet'
     *
     * @param  string $id (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsIdTimelineGet'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1DemandsIdTimelineGetRequest($id, string $contentType = self::contentTypes['apiV1DemandsIdTimelineGet'][0])
    {

        // verify the required parameter 'id' is set
        if ($id === null || (is_array($id) && count($id) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $id when calling apiV1DemandsIdTimelineGet'
            );
        }


        $resourcePath = '/api/v1/demands/{id}/timeline';
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
     * Operation apiV1DemandsPost
     *
     * Sözleşme oluştur (şablondan)
     *
     * @param  \Imzala\Client\Model\CreateDemandRequest $create_demand_request create_demand_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \Imzala\Client\Model\ApiV1DemandsPost201Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response
     */
    public function apiV1DemandsPost($create_demand_request, string $contentType = self::contentTypes['apiV1DemandsPost'][0])
    {
        list($response) = $this->apiV1DemandsPostWithHttpInfo($create_demand_request, $contentType);
        return $response;
    }

    /**
     * Operation apiV1DemandsPostWithHttpInfo
     *
     * Sözleşme oluştur (şablondan)
     *
     * @param  \Imzala\Client\Model\CreateDemandRequest $create_demand_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \Imzala\Client\Model\ApiV1DemandsPost201Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1DemandsPostWithHttpInfo($create_demand_request, string $contentType = self::contentTypes['apiV1DemandsPost'][0])
    {
        $request = $this->apiV1DemandsPostRequest($create_demand_request, $contentType);

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
                        '\Imzala\Client\Model\ApiV1DemandsPost201Response',
                        $request,
                        $response,
                    );
                case 400:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
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
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
                        $request,
                        $response,
                    );
                case 403:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesGet401Response',
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
                '\Imzala\Client\Model\ApiV1DemandsPost201Response',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 201:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1DemandsPost201Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 400:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
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
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 403:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TemplatesGet401Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
            }
        

            throw $e;
        }
    }

    /**
     * Operation apiV1DemandsPostAsync
     *
     * Sözleşme oluştur (şablondan)
     *
     * @param  \Imzala\Client\Model\CreateDemandRequest $create_demand_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsPostAsync($create_demand_request, string $contentType = self::contentTypes['apiV1DemandsPost'][0])
    {
        return $this->apiV1DemandsPostAsyncWithHttpInfo($create_demand_request, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1DemandsPostAsyncWithHttpInfo
     *
     * Sözleşme oluştur (şablondan)
     *
     * @param  \Imzala\Client\Model\CreateDemandRequest $create_demand_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsPostAsyncWithHttpInfo($create_demand_request, string $contentType = self::contentTypes['apiV1DemandsPost'][0])
    {
        $returnType = '\Imzala\Client\Model\ApiV1DemandsPost201Response';
        $request = $this->apiV1DemandsPostRequest($create_demand_request, $contentType);

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
     * Create request for operation 'apiV1DemandsPost'
     *
     * @param  \Imzala\Client\Model\CreateDemandRequest $create_demand_request (required)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1DemandsPostRequest($create_demand_request, string $contentType = self::contentTypes['apiV1DemandsPost'][0])
    {

        // verify the required parameter 'create_demand_request' is set
        if ($create_demand_request === null || (is_array($create_demand_request) && count($create_demand_request) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $create_demand_request when calling apiV1DemandsPost'
            );
        }


        $resourcePath = '/api/v1/demands';
        $formParams = [];
        $queryParams = [];
        $headerParams = [];
        $httpBody = '';
        $multipart = false;





        $headers = $this->headerSelector->selectHeaders(
            ['application/json', ],
            $contentType,
            $multipart
        );

        // for model (json/xml)
        if (isset($create_demand_request)) {
            if (stripos($headers['Content-Type'], 'application/json') !== false) {
                # if Content-Type contains "application/json", json_encode the body
                $httpBody = \GuzzleHttp\Utils::jsonEncode(ObjectSerializer::sanitizeForSerialization($create_demand_request));
            } else {
                $httpBody = $create_demand_request;
            }
        } elseif (count($formParams) > 0) {
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
     * Operation apiV1DemandsUploadPost
     *
     * Dosya upload ile sözleşme oluştur (şablonsuz)
     *
     * @param  \SplFileObject[] $files 1 belge VEYA 1-20 görsel (required)
     * @param  string $parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu). (required)
     * @param  string|null $order Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)
     * @param  string|null $title title (optional)
     * @param  string|null $description description (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsUploadPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return \Imzala\Client\Model\ApiV1DemandsUploadPost201Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response
     */
    public function apiV1DemandsUploadPost($files, $parties, $order = null, $title = null, $description = null, string $contentType = self::contentTypes['apiV1DemandsUploadPost'][0])
    {
        list($response) = $this->apiV1DemandsUploadPostWithHttpInfo($files, $parties, $order, $title, $description, $contentType);
        return $response;
    }

    /**
     * Operation apiV1DemandsUploadPostWithHttpInfo
     *
     * Dosya upload ile sözleşme oluştur (şablonsuz)
     *
     * @param  \SplFileObject[] $files 1 belge VEYA 1-20 görsel (required)
     * @param  string $parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu). (required)
     * @param  string|null $order Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)
     * @param  string|null $title (optional)
     * @param  string|null $description (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsUploadPost'] to see the possible values for this operation
     *
     * @throws \Imzala\Client\ApiException on non-2xx response or if the response body is not in the expected format
     * @throws \InvalidArgumentException
     * @return array of \Imzala\Client\Model\ApiV1DemandsUploadPost201Response|\Imzala\Client\Model\ApiV1TemplatesIdGet404Response|\Imzala\Client\Model\ApiV1TemplatesGet401Response, HTTP status code, HTTP response headers (array of strings)
     */
    public function apiV1DemandsUploadPostWithHttpInfo($files, $parties, $order = null, $title = null, $description = null, string $contentType = self::contentTypes['apiV1DemandsUploadPost'][0])
    {
        $request = $this->apiV1DemandsUploadPostRequest($files, $parties, $order, $title, $description, $contentType);

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
                        '\Imzala\Client\Model\ApiV1DemandsUploadPost201Response',
                        $request,
                        $response,
                    );
                case 400:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
                        $request,
                        $response,
                    );
                case 401:
                    return $this->handleResponseWithDataType(
                        '\Imzala\Client\Model\ApiV1TemplatesGet401Response',
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
                '\Imzala\Client\Model\ApiV1DemandsUploadPost201Response',
                $request,
                $response,
            );
        } catch (ApiException $e) {
            switch ($e->getCode()) {
                case 201:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1DemandsUploadPost201Response',
                        $e->getResponseHeaders()
                    );
                    $e->setResponseObject($data);
                    throw $e;
                case 400:
                    $data = ObjectSerializer::deserialize(
                        $e->getResponseBody(),
                        '\Imzala\Client\Model\ApiV1TemplatesIdGet404Response',
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
            }
        

            throw $e;
        }
    }

    /**
     * Operation apiV1DemandsUploadPostAsync
     *
     * Dosya upload ile sözleşme oluştur (şablonsuz)
     *
     * @param  \SplFileObject[] $files 1 belge VEYA 1-20 görsel (required)
     * @param  string $parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu). (required)
     * @param  string|null $order Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)
     * @param  string|null $title (optional)
     * @param  string|null $description (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsUploadPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsUploadPostAsync($files, $parties, $order = null, $title = null, $description = null, string $contentType = self::contentTypes['apiV1DemandsUploadPost'][0])
    {
        return $this->apiV1DemandsUploadPostAsyncWithHttpInfo($files, $parties, $order, $title, $description, $contentType)
            ->then(
                function ($response) {
                    return $response[0];
                }
            );
    }

    /**
     * Operation apiV1DemandsUploadPostAsyncWithHttpInfo
     *
     * Dosya upload ile sözleşme oluştur (şablonsuz)
     *
     * @param  \SplFileObject[] $files 1 belge VEYA 1-20 görsel (required)
     * @param  string $parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu). (required)
     * @param  string|null $order Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)
     * @param  string|null $title (optional)
     * @param  string|null $description (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsUploadPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Promise\PromiseInterface
     */
    public function apiV1DemandsUploadPostAsyncWithHttpInfo($files, $parties, $order = null, $title = null, $description = null, string $contentType = self::contentTypes['apiV1DemandsUploadPost'][0])
    {
        $returnType = '\Imzala\Client\Model\ApiV1DemandsUploadPost201Response';
        $request = $this->apiV1DemandsUploadPostRequest($files, $parties, $order, $title, $description, $contentType);

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
     * Create request for operation 'apiV1DemandsUploadPost'
     *
     * @param  \SplFileObject[] $files 1 belge VEYA 1-20 görsel (required)
     * @param  string $parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu). (required)
     * @param  string|null $order Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) (optional)
     * @param  string|null $title (optional)
     * @param  string|null $description (optional)
     * @param  string $contentType The value for the Content-Type header. Check self::contentTypes['apiV1DemandsUploadPost'] to see the possible values for this operation
     *
     * @throws \InvalidArgumentException
     * @return \GuzzleHttp\Psr7\Request
     */
    public function apiV1DemandsUploadPostRequest($files, $parties, $order = null, $title = null, $description = null, string $contentType = self::contentTypes['apiV1DemandsUploadPost'][0])
    {

        // verify the required parameter 'files' is set
        if ($files === null || (is_array($files) && count($files) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $files when calling apiV1DemandsUploadPost'
            );
        }

        // verify the required parameter 'parties' is set
        if ($parties === null || (is_array($parties) && count($parties) === 0)) {
            throw new \InvalidArgumentException(
                'Missing the required parameter $parties when calling apiV1DemandsUploadPost'
            );
        }





        $resourcePath = '/api/v1/demands/upload';
        $formParams = [];
        $queryParams = [];
        $headerParams = [];
        $httpBody = '';
        $multipart = false;




        // form params
        $formDataProcessor = new FormDataProcessor();

        $formData = $formDataProcessor->prepare([
            'files' => $files,
            'order' => $order,
            'title' => $title,
            'description' => $description,
            'parties' => $parties,
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
