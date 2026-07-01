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
}
