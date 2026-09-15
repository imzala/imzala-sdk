<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Imzala\Client\Api\ContactsApi;
use Imzala\Client\Api\DemandsApi;
use Imzala\Client\Api\RemindersApi;
use Imzala\Client\Api\TimestampsApi;
use Imzala\Client\ApiException;
use Imzala\Client\Model\ApiV1DemandsPost201Response;
use Imzala\Client\Model\ApiV1DemandsUploadPost201Response;
use Imzala\Client\Model\ApiV1TimestampsPost201Response;
use Imzala\Client\Model\CreatedDemand;
use Imzala\Client\Model\CreatedDemandUpload;
use Imzala\Client\Model\TimestampRecord;
use Imzala\ContactsResource;
use Imzala\CreateTimestampParams;
use Imzala\DemandsResource;
use Imzala\FileInput;
use Imzala\Http;
use Imzala\ImzalaException;
use Imzala\ImzalaRateLimitException;
use Imzala\RetryConfig;
use Imzala\TimestampsResource;
use Imzala\UploadDemandParams;
use Imzala\UploadPartyInput;
use PHPUnit\Framework\TestCase;

/**
 * One safe retry for writes that carry an Idempotency-Key. Mirrors the
 * `unwrapIdempotentWrite` block of packages/node/src/__tests__/retry.test.ts.
 * No real sleeping: the wait function is injected, or the wait is zero.
 */
final class IdempotentWriteTest extends TestCase
{
    private static function rateLimited(?string $retryAfter = null): ApiException
    {
        $headers = $retryAfter !== null ? ['Retry-After' => [$retryAfter]] : [];
        return new ApiException('[429] rate limited', 429, $headers, '{"success":false,"code":"RATE_LIMIT_EXCEEDED"}');
    }

    /** @return callable():array{0:mixed,1:int,2:array} */
    private static function sequence(array $steps, int &$calls): callable
    {
        return static function () use (&$steps, &$calls) {
            $calls++;
            $step = array_shift($steps);
            if ($step instanceof \Throwable) {
                throw $step;
            }
            return [new ApiV1DemandsPost201Response(['success' => true, 'data' => $step]), 201, []];
        };
    }

    private static function ok(): CreatedDemand
    {
        return new CreatedDemand(['id' => 'ok']);
    }

    public function testAWriteWithoutAnIdempotencyKeyIsNeverRetriedOn429(): void
    {
        $calls = 0;
        $slept = [];
        $call = self::sequence([self::rateLimited('0'), self::ok()], $calls);
        try {
            Http::unwrapIdempotentWrite($call, null, 1, function (float $ms) use (&$slept) {
                $slept[] = $ms;
            });
            $this->fail('expected ImzalaRateLimitException');
        } catch (ImzalaRateLimitException) {
        }
        $this->assertSame(1, $calls);
        $this->assertSame([], $slept);
    }

    public function testAnEmptyIdempotencyKeyCountsAsNoKey(): void
    {
        $calls = 0;
        $call = self::sequence([self::rateLimited('0'), self::ok()], $calls);
        $this->expectException(ImzalaRateLimitException::class);
        try {
            Http::unwrapIdempotentWrite($call, '', 1, static fn () => null);
        } finally {
            $this->assertSame(1, $calls);
        }
    }

    public function testAWriteWithAnIdempotencyKeyIsRetriedExactlyOnceOn429(): void
    {
        $calls = 0;
        $call = self::sequence([self::rateLimited('0'), self::ok()], $calls);
        $result = Http::unwrapIdempotentWrite($call, 'k-1', 1, static fn () => null);
        $this->assertInstanceOf(CreatedDemand::class, $result);
        $this->assertSame('ok', $result->getId());
        $this->assertSame(2, $calls);
    }

    public function testASecond429IsThrownNotRetriedAgain(): void
    {
        $calls = 0;
        $call = self::sequence([self::rateLimited('0'), self::rateLimited('0'), self::ok()], $calls);
        try {
            Http::unwrapIdempotentWrite($call, 'k-2', 1, static fn () => null);
            $this->fail('expected ImzalaRateLimitException');
        } catch (ImzalaRateLimitException) {
        }
        $this->assertSame(2, $calls);
    }

    public function testNon429ErrorsAreNotRetriedEven5xx(): void
    {
        foreach ([409, 500, 503] as $status) {
            $calls = 0;
            $call = self::sequence([new ApiException("[{$status}]", $status, [], '{"success":false}'), self::ok()], $calls);
            try {
                Http::unwrapIdempotentWrite($call, 'k-3', 1, static fn () => null);
                $this->fail("expected ImzalaException for {$status}");
            } catch (ImzalaException $e) {
                $this->assertSame($status, $e->getStatusCode());
            }
            $this->assertSame(1, $calls, (string) $status);
        }
    }

    public function testThrowsInsteadOfWaitingWhenRetryAfterExceedsTheCap(): void
    {
        $calls = 0;
        $slept = [];
        $call = self::sequence([self::rateLimited('61'), self::ok()], $calls);
        try {
            Http::unwrapIdempotentWrite($call, 'k-6', 1, function (float $ms) use (&$slept) {
                $slept[] = $ms;
            });
            $this->fail('expected ImzalaRateLimitException');
        } catch (ImzalaRateLimitException $e) {
            $this->assertSame(61.0, $e->getRetryAfter());
        }
        $this->assertSame(1, $calls);
        $this->assertSame([], $slept);
    }

    public function testExactlySixtySecondsIsStillWaited(): void
    {
        $calls = 0;
        $slept = [];
        $call = self::sequence([self::rateLimited('60'), self::ok()], $calls);
        Http::unwrapIdempotentWrite($call, 'k-8', 1, function (float $ms) use (&$slept) {
            $slept[] = $ms;
        });
        $this->assertSame([60000.0], $slept);
        $this->assertSame(2, $calls);
    }

    public function testACustomCapIsHonoured(): void
    {
        $calls = 0;
        $call = self::sequence([self::rateLimited('2'), self::ok()], $calls);
        $this->expectException(ImzalaRateLimitException::class);
        try {
            Http::unwrapIdempotentWrite($call, 'k-7', 1, static fn () => null, 1000);
        } finally {
            $this->assertSame(1, $calls);
        }
    }

    public function testWaitsForRetryAfterBeforeTheRetry(): void
    {
        $calls = 0;
        $events = [];
        $steps = [self::rateLimited('2'), self::ok()];
        $call = function () use (&$steps, &$calls, &$events) {
            $calls++;
            $events[] = 'call';
            $step = array_shift($steps);
            if ($step instanceof \Throwable) {
                throw $step;
            }
            return [new ApiV1DemandsPost201Response(['success' => true, 'data' => $step]), 201, []];
        };
        Http::unwrapIdempotentWrite($call, 'k-5', 1, function (float $ms) use (&$events) {
            $events[] = "sleep:{$ms}";
        });
        $this->assertSame(['call', 'sleep:2000', 'call'], $events);
    }

    public function testWithoutRetryAfterTheBaseDelayIsWaited(): void
    {
        $calls = 0;
        $slept = [];
        $call = self::sequence([self::rateLimited(null), self::ok()], $calls);
        Http::unwrapIdempotentWrite($call, 'k-9', 250, function (float $ms) use (&$slept) {
            $slept[] = $ms;
        });
        $this->assertSame([250.0], $slept);
        $this->assertSame(2, $calls);
    }

    // --- facade wiring: which writes may retry ---

    private static function noWait(): RetryConfig
    {
        return new RetryConfig(0, 0);
    }

    public function testDemandsCreateWithKeyRetriesOnceAndSendsTheKeyInItsOwnSlot(): void
    {
        $data = new CreatedDemand();
        $envelope = new ApiV1DemandsPost201Response(['success' => true, 'data' => $data]);
        $api = $this->createMock(DemandsApi::class);
        $calls = 0;
        $api->expects($this->exactly(2))
            ->method('apiV1DemandsPostWithHttpInfo')
            ->willReturnCallback(function (...$args) use (&$calls, $envelope) {
                $calls++;
                $this->assertSame('siparis-2026-001', $args[1]);
                if ($calls === 1) {
                    throw self::rateLimited('0');
                }
                return [$envelope, 201, []];
            });
        $resource = new DemandsResource($api, $this->createMock(RemindersApi::class), self::noWait());
        $this->assertSame($data, $resource->create(['template_id' => 'tpl-1'], 'siparis-2026-001'));
    }

    public function testDemandsCreateWithoutKeyIsASingleAttempt(): void
    {
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsPostWithHttpInfo')
            ->willReturnCallback(function (...$args) {
                $this->assertNull($args[1]);
                throw self::rateLimited('0');
            });
        $resource = new DemandsResource($api, $this->createMock(RemindersApi::class), self::noWait());
        $this->expectException(ImzalaRateLimitException::class);
        $resource->create(['template_id' => 'tpl-1']);
    }

    public function testDemandsCreateBulkIsNeverRetriedOn429(): void
    {
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsBulkPostWithHttpInfo')
            ->willThrowException(self::rateLimited('0'));
        $resource = new DemandsResource($api, $this->createMock(RemindersApi::class), self::noWait());
        $this->expectException(ImzalaRateLimitException::class);
        $resource->createBulk(['template_id' => 'tpl-1', 'rows' => []]);
    }

    public function testDemandsCreateBulkAcceptsNoIdempotencyKey(): void
    {
        $params = (new \ReflectionMethod(DemandsResource::class, 'createBulk'))->getParameters();
        $this->assertCount(1, $params);
        $params = (new \ReflectionMethod(ContactsResource::class, 'create'))->getParameters();
        $this->assertCount(1, $params);
    }

    public function testContactsCreateIsNeverRetriedOn429(): void
    {
        $api = $this->createMock(ContactsApi::class);
        $api->expects($this->once())
            ->method('apiV1ContactsPostWithHttpInfo')
            ->willThrowException(self::rateLimited('0'));
        $resource = new ContactsResource($api, self::noWait());
        $this->expectException(ImzalaRateLimitException::class);
        $resource->create(['first_name' => 'Ayşe', 'last_name' => 'Yılmaz', 'email' => 'ayse@example.com']);
    }

    public function testUploadDocumentWithKeyRetriesOnce(): void
    {
        $data = new CreatedDemandUpload();
        $envelope = new ApiV1DemandsUploadPost201Response(['success' => true, 'data' => $data]);
        $api = $this->createMock(DemandsApi::class);
        $calls = 0;
        $api->expects($this->exactly(2))
            ->method('apiV1DemandsUploadPostWithHttpInfo')
            ->willReturnCallback(function (...$args) use (&$calls, $envelope) {
                $calls++;
                $this->assertSame('yukleme-1', $args[2]);
                // The retry must still find the temp file on disk.
                $this->assertFileExists($args[0][0]->getPathname());
                if ($calls === 1) {
                    throw self::rateLimited('0');
                }
                return [$envelope, 201, []];
            });
        $resource = new DemandsResource($api, $this->createMock(RemindersApi::class), self::noWait());
        $params = (new UploadDemandParams(
            [new FileInput('%PDF-1.4', 'sozlesme.pdf')],
            [new UploadPartyInput('Ayşe', 'Yılmaz', 'ayse@example.com')]
        ))->withIdempotencyKey('yukleme-1');
        $this->assertSame($data, $resource->uploadDocument($params));
    }

    public function testUploadDocumentWithoutKeyIsASingleAttempt(): void
    {
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsUploadPostWithHttpInfo')
            ->willThrowException(self::rateLimited('0'));
        $resource = new DemandsResource($api, $this->createMock(RemindersApi::class), self::noWait());
        $params = new UploadDemandParams(
            [new FileInput('%PDF-1.4', 'sozlesme.pdf')],
            [new UploadPartyInput('Ayşe', 'Yılmaz', 'ayse@example.com')]
        );
        $this->expectException(ImzalaRateLimitException::class);
        $resource->uploadDocument($params);
    }

    public function testTimestampsCreateWithKeyRetriesOnce(): void
    {
        $data = new TimestampRecord();
        $envelope = new ApiV1TimestampsPost201Response(['success' => true, 'data' => $data]);
        $api = $this->createMock(TimestampsApi::class);
        $calls = 0;
        $api->expects($this->exactly(2))
            ->method('apiV1TimestampsPostWithHttpInfo')
            ->willReturnCallback(function (...$args) use (&$calls, $envelope) {
                $calls++;
                $this->assertFileExists($args[0]->getPathname());
                if ($calls === 1) {
                    throw self::rateLimited('0');
                }
                return [$envelope, 201, []];
            });
        $resource = new TimestampsResource($api, self::noWait());
        $params = (new CreateTimestampParams('bytes', 'eser.pdf'))->withIdempotencyKey('damga-1');
        $this->assertSame($data, $resource->create($params));
    }

    public function testTimestampsCreateWithoutKeyIsASingleAttempt(): void
    {
        $api = $this->createMock(TimestampsApi::class);
        $api->expects($this->once())
            ->method('apiV1TimestampsPostWithHttpInfo')
            ->willThrowException(self::rateLimited('0'));
        $resource = new TimestampsResource($api, self::noWait());
        $this->expectException(ImzalaRateLimitException::class);
        $resource->create(new CreateTimestampParams('bytes', 'eser.pdf'));
    }
}
