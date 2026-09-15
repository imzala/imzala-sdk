<?php

declare(strict_types=1);

namespace Imzala;

/**
 * Standard {@code RateLimit-*} response headers, attached to {@see
 * ImzalaRateLimitException::getRateLimit()}. The server does not send
 * {@code X-RateLimit-*}, so those are never read.
 */
final class RateLimitInfo
{
    /**
     * @param int|float|null $limit {@code RateLimit-Limit}: requests allowed per window. Defaults to 60 but can be lowered per API key, so read it rather than assuming.
     * @param int|float|null $remaining {@code RateLimit-Remaining}: requests left in the current window.
     * @param int|float|null $reset {@code RateLimit-Reset}: seconds until the window resets.
     * @param string|null $policy {@code RateLimit-Policy}: raw policy string, e.g. {@code 60;w=60}.
     */
    public function __construct(
        public readonly int|float|null $limit = null,
        public readonly int|float|null $remaining = null,
        public readonly int|float|null $reset = null,
        public readonly ?string $policy = null,
    ) {
    }
}
