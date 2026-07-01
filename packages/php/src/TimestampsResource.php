<?php

declare(strict_types=1);

namespace Imzala;

use Imzala\Client\Api\TimestampsApi;
use Imzala\Client\Model\TimestampRecord;

/** {@code $imzala->timestamps()} — backed by the vendored generated {@see TimestampsApi}. */
final class TimestampsResource
{
    public function __construct(private readonly TimestampsApi $api)
    {
    }

    /**
     * RFC 3161-timestamps a file via TÜBİTAK KAMU SM TSA (existence +
     * integrity proof — not a signature; see {@see TimestampRecord} for
     * details). Pass {@see CreateTimestampParams::withIdempotencyKey()}
     * to make retries safe (5-minute window, no duplicate credit spend).
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
            return Http::unwrap(fn () => $this->api->apiV1TimestampsPostWithHttpInfo(
                $splFile,
                $params->getIdempotencyKey(),
                $params->getDescription(),
                $params->getOwnerFirstName(),
                $params->getOwnerLastName(),
            ));
        } finally {
            unset($splFile);
            if (is_string($tempPath)) {
                FileInput::cleanupTempPath($tempPath);
            }
        }
    }
}
