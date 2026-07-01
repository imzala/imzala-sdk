<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Imzala\Client\ApiException;
use Imzala\ErrorMapper;
use Imzala\ImzalaAuthException;
use Imzala\ImzalaException;
use Imzala\ImzalaRateLimitException;
use Imzala\ImzalaValidationException;
use PHPUnit\Framework\TestCase;

/**
 * Covers both failure paths described in {@see \Imzala\Http}'s class doc:
 *
 * 1. {@see ErrorMapper::fromResponse()} — the PHP-generator-specific path
 *    where a *declared* error status code (401/403/404/409/422/429/500/...)
 *    comes back as a normal return value (no exception), so {@see
 *    \Imzala\Http::unwrap()} has to classify it itself from the tuple's
 *    status code.
 * 2. {@see ErrorMapper::fromException()} — the vendored generated client's
 *    own thrown {@see ApiException}, for a genuine network failure or a
 *    status code the operation didn't declare a schema for.
 */
final class ErrorsTest extends TestCase
{
    // --- fromResponse() — declared-status-returned-normally path ---

    public function testFromResponse401MapsToAuthException(): void
    {
        $body = (object) ['success' => false, 'error' => 'INVALID_API_KEY', 'message' => 'geçersiz API anahtarı'];
        $e = ErrorMapper::fromResponse($body, 401, []);

        $this->assertInstanceOf(ImzalaAuthException::class, $e);
        $this->assertSame(401, $e->getStatusCode());
        $this->assertSame('geçersiz API anahtarı', $e->getMessage());
        $this->assertSame('INVALID_API_KEY', $e->getErrorCode());
    }

    public function testFromResponse403MapsToAuthException(): void
    {
        $body = (object) ['success' => false, 'error' => 'INSUFFICIENT_SCOPE'];
        $e = ErrorMapper::fromResponse($body, 403, []);

        $this->assertInstanceOf(ImzalaAuthException::class, $e);
        $this->assertSame(403, $e->getStatusCode());
        // No top-level `message` — falls back to the flat `error` string, same as errors.ts/errors.py/ErrorMapper.java.
        $this->assertSame('INSUFFICIENT_SCOPE', $e->getMessage());
        $this->assertSame('INSUFFICIENT_SCOPE', $e->getErrorCode());
    }

    public function testFromResponse429WithNestedErrorObjectExtractsRetryAfterFromBody(): void
    {
        $body = (object) [
            'success' => false,
            'error' => (object) ['code' => 'RATE_LIMITED', 'message' => '5 dakika içinde tekrar deneyin', 'retry_after_seconds' => 240],
        ];
        $e = ErrorMapper::fromResponse($body, 429, []);

        $this->assertInstanceOf(ImzalaRateLimitException::class, $e);
        $this->assertSame('5 dakika içinde tekrar deneyin', $e->getMessage());
        $this->assertSame('RATE_LIMITED', $e->getErrorCode());
        $this->assertSame(240.0, $e->getRetryAfter());
    }

    public function testFromResponse429FallsBackToRetryAfterHeaderWhenBodyOmitsIt(): void
    {
        $body = (object) ['success' => false, 'error' => 'RATE_LIMITED'];
        $e = ErrorMapper::fromResponse($body, 429, ['Retry-After' => ['30']]);

        $this->assertInstanceOf(ImzalaRateLimitException::class, $e);
        $this->assertSame(30.0, $e->getRetryAfter());
    }

    public function testFromResponse422MapsToValidationException(): void
    {
        $body = (object) ['success' => false, 'error' => 'VALIDATION_ERROR', 'message' => 'template_id zorunlu'];
        $e = ErrorMapper::fromResponse($body, 422, []);

        $this->assertInstanceOf(ImzalaValidationException::class, $e);
        $this->assertSame('template_id zorunlu', $e->getMessage());
    }

    public function testFromResponse404And500MapToBaseException(): void
    {
        $notFound = ErrorMapper::fromResponse((object) ['success' => false, 'error' => 'NOT_FOUND'], 404, []);
        $serverError = ErrorMapper::fromResponse((object) ['success' => false, 'error' => 'INTERNAL'], 500, []);

        $this->assertInstanceOf(ImzalaException::class, $notFound);
        $this->assertNotInstanceOf(ImzalaAuthException::class, $notFound);
        $this->assertNotInstanceOf(ImzalaRateLimitException::class, $notFound);
        $this->assertNotInstanceOf(ImzalaValidationException::class, $notFound);

        $this->assertInstanceOf(ImzalaException::class, $serverError);
    }

    public function testFromResponseWithNoExtractableMessageUsesGenericFallback(): void
    {
        $e = ErrorMapper::fromResponse((object) ['success' => false], 500, []);
        $this->assertSame('imzala.org API request failed (HTTP 500)', $e->getMessage());
    }

    public function testFromResponseHandlesNullData(): void
    {
        $e = ErrorMapper::fromResponse(null, 500, null);
        $this->assertInstanceOf(ImzalaException::class, $e);
        $this->assertSame(500, $e->getStatusCode());
    }

    // --- fromException() — genuinely-thrown ApiException path ---

    public function testFromExceptionDecodesJsonResponseBodyAndMapsStatus(): void
    {
        $apiException = new ApiException(
            'boom',
            409,
            [],
            json_encode(['success' => false, 'error' => 'CONFLICT', 'message' => 'zaten var'])
        );

        $e = ErrorMapper::fromException($apiException);

        $this->assertInstanceOf(ImzalaException::class, $e);
        $this->assertNotInstanceOf(ImzalaAuthException::class, $e);
        $this->assertSame(409, $e->getStatusCode());
        $this->assertSame('zaten var', $e->getMessage());
        $this->assertSame('CONFLICT', $e->getErrorCode());
        $this->assertSame($apiException, $e->getPrevious());
    }

    public function testFromExceptionMalformedJsonBodyFallsBackToExceptionMessage(): void
    {
        $apiException = new ApiException('[401] Error connecting to the API', 401, [], 'not-json-{{{');

        $e = ErrorMapper::fromException($apiException);

        $this->assertInstanceOf(ImzalaAuthException::class, $e);
        $this->assertSame('[401] Error connecting to the API', $e->getMessage());
    }

    public function testFromExceptionNullBodyNetworkErrorFallsBackToExceptionMessageAndNullStatus(): void
    {
        $apiException = new ApiException('Connection refused', 0, null, null);

        $e = ErrorMapper::fromException($apiException);

        $this->assertInstanceOf(ImzalaException::class, $e);
        $this->assertNull($e->getStatusCode());
        $this->assertSame('Connection refused', $e->getMessage());
    }

    public function testFromExceptionRetryAfterHeaderFallback(): void
    {
        $apiException = new ApiException('[429] rate limited', 429, ['Retry-After' => ['12']], null);

        $e = ErrorMapper::fromException($apiException);

        $this->assertInstanceOf(ImzalaRateLimitException::class, $e);
        $this->assertSame(12.0, $e->getRetryAfter());
    }
}
