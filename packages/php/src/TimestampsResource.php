<?php

declare(strict_types=1);

namespace Imzala;

use Imzala\Client\Api\TimestampsApi;
use Imzala\Client\Model\ApiV1TimestampsGet200ResponseData;
use Imzala\Client\Model\TimestampListItem;
use Imzala\Client\Model\TimestampRecord;

/** {@code $imzala->timestamps()} — backed by the vendored generated {@see TimestampsApi}. */
final class TimestampsResource
{
    private readonly RetryConfig $retryConfig;

    /**
     * @param RetryConfig|null $retryConfig defaults to the client defaults (2 GET retries, 300 ms base delay)
     */
    public function __construct(private readonly TimestampsApi $api, ?RetryConfig $retryConfig = null)
    {
        $this->retryConfig = $retryConfig ?? new RetryConfig(2, 300);
    }

    /**
     * RFC 3161-timestamps a file via TÜBİTAK KAMU SM TSA (existence +
     * integrity proof — not a signature; see {@see TimestampRecord} for
     * details). Pass {@see CreateTimestampParams::withIdempotencyKey()}
     * to make retries safe (5-minute window, no duplicate credit spend);
     * with a key, one retry is made after a 429.
     *
     * <p>See {@see FileInput} for why this writes {@see
     * CreateTimestampParams::getContent()} to a throwaway temp file — the
     * vendored generated client's multipart layer requires a real path
     * on disk. The temp file (and its parent temp directory) is always
     * deleted before this method returns, success or failure.
     */
    public function create(CreateTimestampParams $params): TimestampRecord
    {
        $fileInput = new FileInput($params->getContent(), $params->getFileName(), $params->getContentType());
        $splFile = $fileInput->toSplFileObject();
        $tempPath = $splFile->getRealPath();

        try {
            return Http::unwrapIdempotentWrite(
                fn () => $this->api->apiV1TimestampsPostWithHttpInfo(
                    file: $splFile,
                    idempotency_key: $params->getIdempotencyKey(),
                    description: $params->getDescription(),
                    owner_first_name: $params->getOwnerFirstName(),
                    owner_last_name: $params->getOwnerLastName(),
                ),
                $params->getIdempotencyKey(),
                $this->retryConfig->retryBaseDelayMs,
            );
        } finally {
            unset($splFile);
            if (is_string($tempPath)) {
                FileInput::cleanupTempPath($tempPath);
            }
        }
    }

    /**
     * Lists your timestamp records (one page). GET, safe to auto-retry.
     *
     * @param string|null $from ISO date (YYYY-MM-DD) lower bound
     * @param string|null $to   ISO date (YYYY-MM-DD) upper bound
     */
    public function list(
        ?string $q = null,
        ?string $status = null,
        ?string $from = null,
        ?string $to = null,
        ?int $page = null,
        ?int $limit = null,
        ?string $sort = null,
    ): ApiV1TimestampsGet200ResponseData {
        return Http::unwrapRetryableGet(
            fn () => $this->api->apiV1TimestampsGetWithHttpInfo(
                page: $page,
                limit: $limit,
                q: $q,
                status: $status,
                from: $from,
                to: $to,
                sort: $sort,
            ),
            $this->retryConfig,
        );
    }

    /** Returns one timestamp record. GET, safe to auto-retry. */
    public function get(string $id): TimestampListItem
    {
        return Http::unwrapRetryableGet(fn () => $this->api->apiV1TimestampsIdGetWithHttpInfo($id), $this->retryConfig);
    }
}
