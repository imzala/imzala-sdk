<?php

declare(strict_types=1);

namespace Imzala;

use GuzzleHttp\Client as GuzzleClient;
use Imzala\Client\Api\AccountApi;
use Imzala\Client\Api\DemandsApi;
use Imzala\Client\Api\RemindersApi;
use Imzala\Client\Api\TemplatesApi;
use Imzala\Client\Api\TimestampsApi;
use Imzala\Client\Configuration;
use Imzala\Client\Model\ApiV1MeGet200ResponseData;
use InvalidArgumentException;

/**
 * imzala.org server-side SDK — an ergonomic, hand-written facade over the
 * vendored (openapi-generator {@code php} / {@code guzzle}) client in
 * {@code Imzala\Client}. Every resource method unwraps the {@code
 * {success, data}} response envelope and throws a typed {@see
 * ImzalaException} on failure, instead of returning raw generated-client
 * exceptions or (see {@see Http}) a raw error-shape model object.
 *
 * <p><b>Server-only.</b> Constructed with a raw {@code X-API-Key} — never
 * embed this in a client-side (browser JS bundle, mobile app, ...)
 * application where the key could be extracted.
 *
 * <pre>{@code
 * $imzala = new \Imzala\ImzalaClient(getenv('IMZALA_API_KEY'));
 * $demand = $imzala->demands()->create([
 *     'template_id' => $templateId,
 *     'party_mapping' => $partyMapping,
 * ]);
 * }</pre>
 *
 * <p>Named {@code ImzalaClient} (not bare {@code Imzala}) so the fully
 * qualified {@code Imzala\ImzalaClient} can never collide with the
 * vendored generated client's own namespace, {@code Imzala\Client\*} — a
 * sibling of this class, not an ancestor/descendant of it. Same
 * collision-avoidance strategy as B3 (C#)'s {@code ImzalaApiClient}
 * package name and B5 (Java)'s {@code org.imzala.client.generated}
 * package.
 */
final class ImzalaClient
{
    public const DEFAULT_BASE_URL = 'https://api-prd.imzala.org';
    private const DEFAULT_TIMEOUT_SECONDS = 30.0;
    private const DEFAULT_MAX_RETRIES = 2;
    private const DEFAULT_RETRY_BASE_DELAY_MS = 300;

    private readonly AccountApi $accountApi;
    private readonly TemplatesResource $templatesResource;
    private readonly DemandsResource $demandsResource;
    private readonly EmbedResource $embedResource;
    private readonly TimestampsResource $timestampsResource;
    private readonly RetryConfig $retryConfig;

    /**
     * @param string $apiKey {@code imz_<64 hex>} — from Dashboard → Geliştirici → API Anahtarları, or Hesap Ayarları → API Anahtarları.
     * @param string $baseUrl defaults to {@code https://api-prd.imzala.org}. Use {@code https://test-api.imzala.org} for the test environment.
     * @param float $timeoutSeconds per-request timeout, in seconds. Defaults to 30.
     * @param int $maxRetries max auto-retry attempts for safe, idempotent
     *     **GET** requests that fail with 429 (rate limited) or 5xx (server
     *     error). Defaults to 2. Set to {@code 0} to disable. Writes
     *     ({@code demands()->create()}, {@code sendReminder()}, ...) are
     *     never retried, regardless of this setting — see {@see Http::unwrapRetryableGet()}.
     * @param int $retryBaseDelayMs base delay (ms) for the exponential backoff between retries. Defaults to 300.
     */
    public function __construct(
        string $apiKey,
        string $baseUrl = self::DEFAULT_BASE_URL,
        float $timeoutSeconds = self::DEFAULT_TIMEOUT_SECONDS,
        int $maxRetries = self::DEFAULT_MAX_RETRIES,
        int $retryBaseDelayMs = self::DEFAULT_RETRY_BASE_DELAY_MS
    ) {
        if ($apiKey === '') {
            throw new InvalidArgumentException('new ImzalaClient(apiKey) — apiKey is required.');
        }

        $config = new Configuration();
        $config->setApiKey('X-API-Key', $apiKey);
        $config->setHost($baseUrl !== '' ? $baseUrl : self::DEFAULT_BASE_URL);

        $httpClient = new GuzzleClient(['timeout' => $timeoutSeconds]);

        $this->retryConfig = new RetryConfig(max(0, $maxRetries), max(0, $retryBaseDelayMs));

        $this->accountApi = new AccountApi($httpClient, $config);
        $demandsApi = new DemandsApi($httpClient, $config);
        $remindersApi = new RemindersApi($httpClient, $config);
        $templatesApi = new TemplatesApi($httpClient, $config);
        $timestampsApi = new TimestampsApi($httpClient, $config);

        $this->templatesResource = new TemplatesResource($templatesApi, $this->retryConfig);
        $this->demandsResource = new DemandsResource($demandsApi, $remindersApi, $this->retryConfig);
        $this->embedResource = new EmbedResource($demandsApi);
        $this->timestampsResource = new TimestampsResource($timestampsApi);
    }

    /** {@code $imzala->templates()->list()/get($id)/usage($id)}. */
    public function templates(): TemplatesResource
    {
        return $this->templatesResource;
    }

    /** {@code $imzala->demands()->create(...)/get($id)/uploadDocument(...)/addItems($id,...)/sendReminder($id,...)}. */
    public function demands(): DemandsResource
    {
        return $this->demandsResource;
    }

    /** {@code $imzala->embed()->createSession($demandId, $partyId)}. */
    public function embed(): EmbedResource
    {
        return $this->embedResource;
    }

    /** {@code $imzala->timestamps()->create(...)}. */
    public function timestamps(): TimestampsResource
    {
        return $this->timestampsResource;
    }

    /** Returns the calling API key's owner info (id, email, name, workspace, remaining credits). Requires the {@code timestamps} scope. GET — safe to auto-retry. */
    public function me(): ApiV1MeGet200ResponseData
    {
        return Http::unwrapRetryableGet(fn () => $this->accountApi->apiV1MeGetWithHttpInfo(), $this->retryConfig);
    }

    /**
     * Verifies an imzala.org webhook delivery's {@code X-Imzala-Signature-256}
     * header.
     *
     * <p>Mirrors the backend algorithm exactly ({@code
     * src/services/webhook/WebhookSigner.ts} in imzala-service): {@code
     * 'sha256=' + HMAC-SHA256(rawBody, secret)} as lowercase hex, compared
     * with a fixed-time equality check ({@see hash_equals()}, which is
     * timing-safe regardless of argument order or length mismatch since
     * PHP 7.3 — https://wiki.php.net/rfc/hash_equals-improvement).
     *
     * @param string $secret the {@code whsec_<64-hex>} secret shown once when the webhook was created in the dashboard
     * @param string $rawBody the <b>exact, unparsed</b> request body bytes. Parsing the JSON and re-serializing it can change byte-for-byte content (key order, whitespace) and break verification — read the raw request body (e.g. {@code file_get_contents('php://input')}), don't re-encode a decoded array
     * @param string|null $signatureHeader the raw {@code X-Imzala-Signature-256} header value (e.g. {@code sha256=<hex>})
     * @return bool {@code true} if the signature is valid. Returns {@code false} — never throws — for a wrong signature, a malformed/missing header, an empty secret, or any other verification failure
     */
    public static function verifyWebhook(string $secret, string $rawBody, ?string $signatureHeader): bool
    {
        if ($secret === '' || $signatureHeader === null || $signatureHeader === '') {
            return false;
        }

        $expected = 'sha256=' . hash_hmac('sha256', $rawBody, $secret);

        return hash_equals($expected, $signatureHeader);
    }
}
