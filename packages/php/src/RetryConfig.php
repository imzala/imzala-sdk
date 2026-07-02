<?php

declare(strict_types=1);

namespace Imzala;

/**
 * Safe auto-retry policy for GET-only, idempotent facade methods — see
 * {@see Http::unwrapRetryableGet()}. Mirrors {@code RetryConfig} in
 * {@code http.ts} (Node), the retry kwargs on Python's client, C#'s
 * {@code RetryOptions}, and Java's equivalent.
 *
 * @internal constructed once by {@see ImzalaClient} from its constructor
 *     options and threaded into every resource that has GET methods.
 */
final class RetryConfig
{
    /**
     * @param int $maxRetries max retry attempts, not counting the initial
     *     try. {@code 0} disables retry. Callers should clamp to {@code >= 0}
     *     before constructing (see {@see ImzalaClient}).
     * @param int $retryBaseDelayMs base delay (ms) for the exponential
     *     backoff between retries.
     */
    public function __construct(
        public readonly int $maxRetries,
        public readonly int $retryBaseDelayMs,
    ) {
    }
}
