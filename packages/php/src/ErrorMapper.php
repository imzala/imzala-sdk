<?php

declare(strict_types=1);

namespace Imzala;

use Imzala\Client\ApiException;
use JsonSerializable;
use Throwable;

/**
 * Maps a failed API call — either a thrown {@see ApiException} or a
 * normally-*returned* error envelope (see the big comment on {@see Http})
 * — to the appropriate {@see ImzalaException} subclass, based on HTTP
 * status code.
 *
 * imzala.org error envelopes are not fully uniform across endpoints: most
 * are {@code {success:false, error:"<code>", message:"<text>"}}, but some
 * (e.g. the reminders 429) nest a {@code {code, message, retry_after_seconds}}
 * object under {@code error} instead of a plain string — {@see self::extractMessage}
 * / {@see self::extractErrorCode} handle both shapes, mirroring
 * {@code errors.ts}'s / {@code errors.py}'s / {@code Errors.cs}'s /
 * {@code ErrorMapper.java}'s equivalents.
 *
 * @internal
 */
final class ErrorMapper
{
    private function __construct()
    {
    }

    /**
     * Maps a thrown vendored-generated {@see ApiException} (network error,
     * or a status code this particular operation didn't declare a response
     * schema for — see {@see Http}) to a typed {@see ImzalaException}.
     */
    public static function fromException(ApiException $e): ImzalaException
    {
        $statusCode = $e->getCode();
        $body = self::toArray($e->getResponseBody());
        $message = self::extractMessage($body) ?? $e->getMessage();
        $errorCode = self::extractErrorCode($body);
        $retryAfter = self::extractRetryAfter($body, $e->getResponseHeaders());
        $bodyText = self::bodyToString($e->getResponseBody());

        return self::classify($statusCode > 0 ? $statusCode : null, $message, $bodyText, $errorCode, $retryAfter, $e);
    }

    /**
     * Maps a *normally returned* (not thrown) response whose status code
     * or envelope indicates failure to a typed {@see ImzalaException}.
     *
     * @param mixed $data the deserialized response body — a generated
     *     Model instance, {@see \stdClass}, array, or {@code null}
     * @param array<string, string[]>|null $headers raw HTTP response headers (for the {@code Retry-After} fallback)
     */
    public static function fromResponse($data, int $statusCode, ?array $headers, ?Throwable $previous = null): ImzalaException
    {
        $body = self::toArray($data);
        $message = self::extractMessage($body) ?? sprintf('imzala.org API request failed (HTTP %d)', $statusCode);
        $errorCode = self::extractErrorCode($body);
        $retryAfter = self::extractRetryAfter($body, $headers);
        $bodyText = $body !== [] ? (string) json_encode($body) : null;

        return self::classify($statusCode, $message, $bodyText, $errorCode, $retryAfter, $previous);
    }

    private static function classify(
        ?int $statusCode,
        string $message,
        ?string $body,
        ?string $errorCode,
        ?float $retryAfter,
        ?Throwable $previous
    ): ImzalaException {
        return match ($statusCode) {
            401, 403 => new ImzalaAuthException($message, $statusCode, $body, $errorCode, $previous),
            429 => new ImzalaRateLimitException($message, $statusCode, $body, $errorCode, $retryAfter, $previous),
            422 => new ImzalaValidationException($message, $statusCode, $body, $errorCode, $previous),
            default => new ImzalaException($message, $statusCode, $body, $errorCode, $previous),
        };
    }

    /**
     * Normalizes a generated Model instance ({@see JsonSerializable}),
     * {@see \stdClass}, array, JSON string, or {@code null} into a plain
     * associative array — so error-shape extraction below works
     * regardless of which per-operation response model class the PHP
     * generator produced for a given status code.
     *
     * @return array<string, mixed>
     */
    private static function toArray(mixed $data): array
    {
        if ($data === null) {
            return [];
        }

        if (is_array($data)) {
            return $data;
        }

        if (is_string($data)) {
            $decoded = json_decode($data, true);
            return is_array($decoded) ? $decoded : [];
        }

        if ($data instanceof JsonSerializable || is_object($data)) {
            $encoded = json_encode($data);
            if ($encoded === false) {
                return [];
            }
            $decoded = json_decode($encoded, true);
            return is_array($decoded) ? $decoded : [];
        }

        return [];
    }

    private static function bodyToString(mixed $data): ?string
    {
        if ($data === null) {
            return null;
        }
        if (is_string($data)) {
            return $data;
        }
        $encoded = json_encode($data);
        return $encoded === false ? null : $encoded;
    }

    /**
     * @param array<string, mixed> $body
     */
    private static function extractMessage(array $body): ?string
    {
        if (isset($body['message']) && is_string($body['message'])) {
            return $body['message'];
        }

        $error = $body['error'] ?? null;
        if (is_string($error)) {
            return $error;
        }
        if (is_array($error)) {
            if (isset($error['message']) && is_string($error['message'])) {
                return $error['message'];
            }
            if (isset($error['code']) && is_string($error['code'])) {
                return $error['code'];
            }
        }

        return null;
    }

    /**
     * @param array<string, mixed> $body
     */
    private static function extractErrorCode(array $body): ?string
    {
        $error = $body['error'] ?? null;
        if (is_string($error)) {
            return $error;
        }
        if (is_array($error) && isset($error['code']) && is_string($error['code'])) {
            return $error['code'];
        }

        return null;
    }

    /**
     * @param array<string, mixed> $body
     * @param array<string, string[]>|null $headers
     */
    private static function extractRetryAfter(array $body, ?array $headers): ?float
    {
        if (isset($body['retry_after_seconds']) && is_numeric($body['retry_after_seconds'])) {
            return (float) $body['retry_after_seconds'];
        }

        $error = $body['error'] ?? null;
        if (is_array($error) && isset($error['retry_after_seconds']) && is_numeric($error['retry_after_seconds'])) {
            return (float) $error['retry_after_seconds'];
        }

        if ($headers !== null) {
            foreach ($headers as $name => $values) {
                if (strcasecmp((string) $name, 'Retry-After') === 0) {
                    $value = is_array($values) ? ($values[0] ?? null) : $values;
                    if (is_numeric($value)) {
                        return (float) $value;
                    }
                }
            }
        }

        return null;
    }
}
