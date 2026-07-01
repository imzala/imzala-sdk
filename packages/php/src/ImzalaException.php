<?php

declare(strict_types=1);

namespace Imzala;

use Throwable;

/**
 * Base error type thrown by every {@see ImzalaClient} facade method.
 * Normalizes the vendored generated client's
 * {@see \Imzala\Client\ApiException}, network/timeout failures, and a
 * {@code {success:false}} response envelope (returned on an otherwise-2xx
 * *or* non-2xx response — see {@see Http} for why) into a single throwable
 * shape — callers never need to reach into {@code Imzala\Client} internals.
 *
 * Thrown directly (not as a subclass) for statuses that don't have a
 * dedicated subclass below (400, 404, 409, 500, ...) — same 4-class
 * taxonomy as the TS ({@code @imzala/node}), Python ({@code imzala}), C#
 * ({@code Imzala}), and Java ({@code org.imzala:imzala-java}) SDKs.
 */
class ImzalaException extends \RuntimeException
{
    private ?int $statusCode;
    private ?string $body;
    private ?string $errorCode;

    /**
     * @param string $message human-readable error message
     * @param int|null $statusCode HTTP status code, when the error originated from an HTTP response
     * @param string|null $body raw response body (unparsed text), when available
     * @param string|null $errorCode machine-readable error code from the response envelope, when present
     * @param Throwable|null $previous the underlying exception (vendored generated ApiException, network error, ...), for {@see self::getPrevious()}
     */
    public function __construct(
        string $message,
        ?int $statusCode = null,
        ?string $body = null,
        ?string $errorCode = null,
        ?Throwable $previous = null
    ) {
        parent::__construct($message, 0, $previous);
        $this->statusCode = $statusCode;
        $this->body = $body;
        $this->errorCode = $errorCode;
    }

    /** HTTP status code, when the error originated from an HTTP response. {@code null} otherwise (e.g. a network error). */
    public function getStatusCode(): ?int
    {
        return $this->statusCode;
    }

    /** Raw response body (unparsed text), when available. */
    public function getBody(): ?string
    {
        return $this->body;
    }

    /** Machine-readable error code from the response envelope, when present (e.g. {@code "INVALID_API_KEY"}). */
    public function getErrorCode(): ?string
    {
        return $this->errorCode;
    }
}
