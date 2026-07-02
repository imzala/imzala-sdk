<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Imzala\Client\Api\DemandsApi;
use Imzala\Client\Api\RemindersApi;
use Imzala\Client\Api\TemplatesApi;
use Imzala\Client\ApiException;
use Imzala\Client\Model\ApiV1DemandsIdGet200Response;
use Imzala\Client\Model\ApiV1TemplatesGet200Response;
use Imzala\Client\Model\ApiV1TemplatesGet200ResponseData;
use Imzala\Client\Model\ApiV1TemplatesIdGet404Response;
use Imzala\Client\Model\DemandStatus;
use Imzala\DemandsResource;
use Imzala\ImzalaException;
use Imzala\ImzalaRateLimitException;
use Imzala\RetryConfig;
use Imzala\TemplatesResource;
use PHPUnit\Framework\TestCase;

/**
 * Safe auto-retry — mirrors {@code retry.test.ts} (Node). Every mock here
 * is of the vendored generated {@see TemplatesApi}/{@see DemandsApi}
 * (the same seam {@code tests/ClientTest.php} uses), so these tests
 * exercise the *real* {@see \Imzala\Http::unwrapRetryableGet()} — not a
 * reimplementation of it.
 *
 * Keep retries near-instant in tests: `retryBaseDelayMs: 1` (real prod
 * default is 300).
 */
final class RetryTest extends TestCase
{
    private const FAST_RETRY_MAX = 2;
    private const FAST_RETRY_BASE_DELAY_MS = 1;

    private static function fastRetry(): RetryConfig
    {
        return new RetryConfig(self::FAST_RETRY_MAX, self::FAST_RETRY_BASE_DELAY_MS);
    }

    public function testGetRetriesTwiceOn429ThenSucceedsOnThirdAttemptCallCountThree(): void
    {
        $data = new ApiV1TemplatesGet200ResponseData(['templates' => [], 'total' => 0, 'page' => 1, 'limit' => 20]);
        $envelope = new ApiV1TemplatesGet200Response(['success' => true, 'data' => $data]);

        $callCount = 0;
        $api = $this->createMock(TemplatesApi::class);
        $api->expects($this->exactly(3))
            ->method('apiV1TemplatesGetWithHttpInfo')
            ->willReturnCallback(function () use (&$callCount, $envelope) {
                $callCount++;
                if ($callCount < 3) {
                    // Neither 429 nor 5xx is a declared response schema for
                    // this operation (see generated/lib/Api/TemplatesApi.php)
                    // — the vendored generated client throws ApiException
                    // for it, it doesn't return it normally.
                    throw new ApiException('Rate limited', 429, [], null);
                }
                return [$envelope, 200, []];
            });

        $resource = new TemplatesResource($api, self::fastRetry());
        $result = $resource->list();

        $this->assertSame($data, $result);
        $this->assertSame(3, $callCount);
    }

    public function testGetRetriesOn5xxServerErrorAndSucceeds(): void
    {
        $data = new DemandStatus();
        $envelope = new ApiV1DemandsIdGet200Response(['success' => true, 'data' => $data]);

        $callCount = 0;
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->exactly(2))
            ->method('apiV1DemandsIdGetWithHttpInfo')
            ->willReturnCallback(function () use (&$callCount, $envelope) {
                $callCount++;
                if ($callCount < 2) {
                    throw new ApiException('Service unavailable', 503, [], null);
                }
                return [$envelope, 200, []];
            });

        $remindersApi = $this->createMock(RemindersApi::class);
        $resource = new DemandsResource($api, $remindersApi, self::fastRetry());

        $result = $resource->get('demand-1');

        $this->assertSame($data, $result);
        $this->assertSame(2, $callCount);
    }

    public function testGetDoesNotRetryOnNon429FourXxThrownImmediately(): void
    {
        // 404 *is* a declared response schema for apiV1TemplatesIdGet (see
        // generated/lib/Api/TemplatesApi.php) — the generated client
        // returns it normally (doesn't throw), exercising the *other* half
        // of Http::unwrap()'s two failure paths (a returned non-2xx
        // envelope, not a thrown ApiException). Either way it must map to
        // a thrown ImzalaException and must NOT be retried.
        $errorResponse = new ApiV1TemplatesIdGet404Response(['success' => false, 'error' => 'TEMPLATE_NOT_FOUND']);

        $api = $this->createMock(TemplatesApi::class);
        $api->expects($this->once())
            ->method('apiV1TemplatesIdGetWithHttpInfo')
            ->with('missing')
            ->willReturn([$errorResponse, 404, []]);

        $resource = new TemplatesResource($api, self::fastRetry());

        $this->expectException(ImzalaException::class);
        $resource->get('missing');
    }

    public function testMaxRetriesZeroDisablesRetryEntirelyEvenOnRetryable429(): void
    {
        $api = $this->createMock(TemplatesApi::class);
        $api->expects($this->once())
            ->method('apiV1TemplatesGetWithHttpInfo')
            ->willThrowException(new ApiException('Rate limited', 429, [], null));

        $resource = new TemplatesResource($api, new RetryConfig(0, self::FAST_RETRY_BASE_DELAY_MS));

        $this->expectException(ImzalaRateLimitException::class);
        $resource->list();
    }

    public function testExhaustsRetriesAndThrowsTypedErrorWhenEveryAttemptFails(): void
    {
        $api = $this->createMock(TemplatesApi::class);
        // initial attempt + 2 retries (maxRetries: 2) = 3 calls total
        $api->expects($this->exactly(3))
            ->method('apiV1TemplatesGetWithHttpInfo')
            ->willThrowException(new ApiException('Service unavailable', 503, [], null));

        $resource = new TemplatesResource($api, self::fastRetry());

        $this->expectException(ImzalaException::class);
        $resource->list();
    }

    // --- SAFETY: writes are never retried ---------------------------------

    public function testPostCreateThatReturns429ThrowsImmediatelyNoRetryCallCountOne(): void
    {
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsPostWithHttpInfo')
            ->willThrowException(new ApiException('Rate limited', 429, [], null));

        $remindersApi = $this->createMock(RemindersApi::class);
        $resource = new DemandsResource($api, $remindersApi, self::fastRetry());

        $this->expectException(ImzalaRateLimitException::class);
        // A retried demands()->create() POST would create a DUPLICATE
        // demand — must never retry, even though 429 is normally
        // retryable for GET requests.
        $resource->create(['template_id' => 'tpl-1']);
    }

    public function testPostCreateThatReturns503ThrowsImmediatelyNoRetryCallCountOne(): void
    {
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsPostWithHttpInfo')
            ->willThrowException(new ApiException('Service unavailable', 503, [], null));

        $remindersApi = $this->createMock(RemindersApi::class);
        $resource = new DemandsResource($api, $remindersApi, self::fastRetry());

        $this->expectException(ImzalaException::class);
        $resource->create(['template_id' => 'tpl-1']);
    }
}
