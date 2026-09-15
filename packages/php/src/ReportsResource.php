<?php

declare(strict_types=1);

namespace Imzala;

use Imzala\Client\Api\ReportsApi;
use Imzala\Client\Model\ApiV1ReportsGet200ResponseData;

/** {@code $imzala->reports()}, backed by the vendored generated {@see ReportsApi}. */
final class ReportsResource
{
    public function __construct(
        private readonly ReportsApi $api,
        private readonly RetryConfig $retryConfig,
    ) {
    }

    /**
     * Returns aggregate demand counts for your workspace (pending, completed,
     * cancelled, expired, created this month). Counts only, no personal data.
     * GET, safe to auto-retry.
     */
    public function get(): ApiV1ReportsGet200ResponseData
    {
        return Http::unwrapRetryableGet(fn () => $this->api->apiV1ReportsGetWithHttpInfo(), $this->retryConfig);
    }
}
