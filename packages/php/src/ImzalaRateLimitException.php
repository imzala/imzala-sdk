<?php

declare(strict_types=1);

namespace Imzala;

use Throwable;

/**
 * Rate limited (429). {@see self::getRetryAfter()} is seconds, when the server provided one.
 *
 * Several different limits answer with 429 and each has its own code (e.g.
 * {@code RATE_LIMIT_EXCEEDED}, {@code TOO_MANY_REQUESTS}, {@code RATE_LIMITED},
 * {@code RECIPIENT_RESEND_LIMIT}, {@code MAX_SMS_REMINDERS_REACHED}), so branch
 * on this class or on the status code, not on one particular code.
 */
final class ImzalaRateLimitException extends ImzalaException
{
    private ?float $retryAfter;
    private ?RateLimitInfo $rateLimit;

    public function __construct(
        string $message,
        ?int $statusCode = null,
        ?string $body = null,
        ?string $errorCode = null,
        ?float $retryAfter = null,
        ?Throwable $previous = null,
        ?RateLimitInfo $rateLimit = null
    ) {
        parent::__construct($message, $statusCode, $body, $errorCode, $previous);
        $this->retryAfter = $retryAfter;
        $this->rateLimit = $rateLimit;
    }

    /** Seconds to wait before retrying, when the server provided one (from the response body or the {@code Retry-After} header). {@code null} otherwise. */
    public function getRetryAfter(): ?float
    {
        return $this->retryAfter;
    }

    /** The standard {@code RateLimit-*} headers of the response, or {@code null} when the server sent none. */
    public function getRateLimit(): ?RateLimitInfo
    {
        return $this->rateLimit;
    }
}
