<?php

declare(strict_types=1);

namespace Imzala;

use Throwable;

/** Rate limited (429). {@see self::getRetryAfter()} is seconds, when the server provided one. */
final class ImzalaRateLimitException extends ImzalaException
{
    private ?float $retryAfter;

    public function __construct(
        string $message,
        ?int $statusCode = null,
        ?string $body = null,
        ?string $errorCode = null,
        ?float $retryAfter = null,
        ?Throwable $previous = null
    ) {
        parent::__construct($message, $statusCode, $body, $errorCode, $previous);
        $this->retryAfter = $retryAfter;
    }

    /** Seconds to wait before retrying, when the server provided one (from the response body or the {@code Retry-After} header). {@code null} otherwise. */
    public function getRetryAfter(): ?float
    {
        return $this->retryAfter;
    }
}
