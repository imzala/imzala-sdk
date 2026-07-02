<?php

declare(strict_types=1);

namespace Imzala;

use Imzala\Client\ApiException;

/**
 * Every imzala.org API response uses the same envelope: {@code {success:
 * true, data: {...}}} on success, or a non-2xx status with {@code
 * {success: false, error/message: ...}} on failure.
 *
 * <p><b>The one real PHP-specific gotcha in this vertical</b> (worth
 * flagging explicitly — none of the TS/Python/C#/Java verticals have it):
 * the vendored generated client's plain per-operation methods (e.g. {@code
 * DemandsApi::apiV1DemandsPost()}) do <b>NOT</b> throw {@see ApiException}
 * for every non-2xx response the way the other 4 SDKs' generated clients
 * do. This downconverted OpenAPI spec declares response *schemas* for
 * most error status codes per operation (401/403/404/409/422/429/500,
 * whichever the endpoint actually returns) — and the PHP generator
 * template treats every *declared* status code, success or error alike,
 * as a normal return path: it deserializes the body to that status code's
 * documented model class and returns it, discarding the status code
 * entirely (the plain method is literally {@code list($response) =
 * $this->xWithHttpInfo(...); return $response;}). {@see ApiException} is
 * only thrown for a genuine network failure, or a status code the
 * operation's spec entry didn't declare a schema for.
 *
 * <p>Concretely: calling the plain {@code apiV1DemandsPost()} on a 401
 * response returns an {@code ApiV1TemplatesGet401Response} object — same
 * as a 201 success returns an {@code ApiV1DemandsPost201Response} object
 * — with no exception and no status code available to distinguish them.
 * The facade therefore calls the {@code ...WithHttpInfo()} variant
 * everywhere (never the plain method), which returns a {@code [data,
 * statusCode, headers]} tuple, and inspects {@code statusCode} itself to
 * decide success vs. failure and which {@see ImzalaException} subclass to
 * throw — see {@see self::unwrap}.
 *
 * <p>Mirrors {@code http.ts}'s {@code unwrap<T>()} (TS), {@code
 * client.py}'s {@code _unwrap()} (Python), {@code Http.cs}'s {@code
 * Unwrap<TResponse, TData>()} (C#), and {@code Http.java}'s {@code
 * unwrap()} (Java) — every resource class in {@code src/*Resource.php}
 * and {@see ImzalaClient::me()} routes through this.
 *
 * @internal
 */
final class Http
{
    private function __construct()
    {
    }

    /**
     * @param callable():array{0:mixed,1:int,2:array<string,string[]>} $call
     *     a generated client's {@code ...WithHttpInfo(...)} call — NOT the
     *     plain method, see class docs
     */
    public static function unwrap(callable $call): mixed
    {
        try {
            [$data, $statusCode, $headers] = $call();
        } catch (ApiException $e) {
            throw ErrorMapper::fromException($e);
        }

        if ($statusCode < 200 || $statusCode >= 300) {
            throw ErrorMapper::fromResponse($data, $statusCode, $headers);
        }

        $success = (is_object($data) && method_exists($data, 'getSuccess')) ? $data->getSuccess() : null;
        if ($success !== true) {
            throw ErrorMapper::fromResponse($data, $statusCode, $headers);
        }

        return (is_object($data) && method_exists($data, 'getData')) ? $data->getData() : null;
    }

    /**
     * Like {@see self::unwrap()}, but adds safe auto-retry for **GET-only,
     * idempotent** facade methods ({@code templates->list/get/usage},
     * {@code demands->get}, {@code me()}). Retries on 429 (rate limited —
     * honors {@code Retry-After}/{@see ImzalaRateLimitException::getRetryAfter()})
     * and 5xx (server error) with exponential backoff + jitter; any other
     * status (400/401/403/404/409/422/...) is thrown immediately, same as
     * {@see self::unwrap()}. Mirrors {@code unwrapRetryableGet()} in
     * {@code http.ts} (Node).
     *
     * <p>Whatever status code the underlying vendored generated client's
     * {@code ...WithHttpInfo()} call produces — whether it's a normally
     * *returned* declared-schema response (see the class doc above, e.g.
     * this spec's 401 on {@code apiV1TemplatesGet}) or a *thrown* {@see
     * ApiException} for an undeclared status (this spec never declares
     * 429/5xx response schemas on any of the 5 retryable GET operations, so
     * those always arrive as a thrown {@see ApiException}) — {@see
     * self::unwrap()} above already normalizes both into a single thrown
     * {@see ImzalaException} with {@see ImzalaException::getStatusCode()}
     * set. This method only has to inspect *that*, once, in one place.
     *
     * <p><b>SAFETY — never call this with a non-GET request.</b> There is
     * deliberately no {@code $method} parameter and no way to opt a
     * POST/PUT/PATCH/DELETE call into retrying: this is not a
     * caller-configurable behavior. Retrying a write (e.g. {@code
     * demands->create()}, {@code sendReminder()}) could duplicate a demand
     * or double-send a reminder — those facade methods must keep calling
     * {@see self::unwrap()} above, once, with no retry loop reachable.
     *
     * <p>{@code $call} is a thunk (not an already-invoked value) because
     * retrying means re-issuing the underlying HTTP request — {@see
     * self::unwrap()} itself already requires this (it invokes {@code
     * $call()} internally), so simply passing the same closure back in on
     * each loop iteration re-issues the real API call every attempt.
     *
     * @param callable():array{0:mixed,1:int,2:array<string,string[]>} $call
     *     a generated client's {@code ...WithHttpInfo(...)} call — NOT the
     *     plain method, see class docs
     */
    public static function unwrapRetryableGet(callable $call, RetryConfig $retry): mixed
    {
        $attempt = 0;
        for (;;) {
            try {
                return self::unwrap($call);
            } catch (ImzalaException $e) {
                if ($attempt >= $retry->maxRetries || !self::isRetryableStatus($e->getStatusCode())) {
                    throw $e;
                }
                self::sleepMs(self::computeDelayMs($e, $attempt, $retry->retryBaseDelayMs));
                $attempt++;
            }
        }
    }

    /** 429 (rate limited) and 5xx (server error) are treated as transient. Everything else (4xx, network-error-with-no-status) is never retried. */
    private static function isRetryableStatus(?int $statusCode): bool
    {
        if ($statusCode === 429) {
            return true;
        }
        return $statusCode !== null && $statusCode >= 500 && $statusCode <= 599;
    }

    /** Exponential backoff with jitter, honoring {@code Retry-After} on 429s (already parsed onto {@see ImzalaRateLimitException::getRetryAfter()} by {@see ErrorMapper}). */
    private static function computeDelayMs(ImzalaException $error, int $attempt, int $baseDelayMs): float
    {
        if ($error instanceof ImzalaRateLimitException && $error->getRetryAfter() !== null) {
            return max(0.0, $error->getRetryAfter() * 1000);
        }

        $backoff = $baseDelayMs * (2 ** $attempt);
        $jitter = $baseDelayMs > 0 ? mt_rand(0, $baseDelayMs) : 0;

        return (float) ($backoff + $jitter);
    }

    private static function sleepMs(float $ms): void
    {
        if ($ms <= 0) {
            return;
        }
        usleep((int) round($ms * 1000));
    }
}
