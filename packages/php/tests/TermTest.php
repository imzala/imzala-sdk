<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Imzala\Client\Model\ApiV1DemandsIdArchivePost200ResponseData;
use Imzala\Client\Model\ApiV1DemandsIdTermPatch200ResponseData;
use Imzala\Client\Model\ApiV1DemandsIdUnarchivePost200ResponseData;
use Imzala\ImzalaClient;
use Imzala\ImzalaException;
use Imzala\ImzalaRateLimitException;
use Imzala\Tests\Support\LocalHttpServer;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

/**
 * Contract term tracking and archive helpers, checked on the wire against a
 * local HTTP server: path slots, the partial-update body (null is sent, an
 * omitted key is not), the archive filter on list, and that none of these
 * writes is retried. Mirrors packages/node/src/__tests__/term.test.ts.
 */
final class TermTest extends TestCase
{
    private const DEMAND = '11111111-1111-4111-8111-111111111111';

    /** @var list<LocalHttpServer> */
    private array $servers = [];

    protected function tearDown(): void
    {
        foreach ($this->servers as $server) {
            $server->stop();
        }
        $this->servers = [];
    }

    /** @param list<array{status:int, body:mixed, headers?:array<string,string>}> $replies */
    private function server(array $replies): LocalHttpServer
    {
        $server = new LocalHttpServer($replies);
        $this->servers[] = $server;
        return $server;
    }

    private static function client(LocalHttpServer $server): ImzalaClient
    {
        return new ImzalaClient('imz_test', $server->baseUrl, 5.0, 2, 1);
    }

    /** @return array{status:int, body:mixed} */
    private static function ok(mixed $data): array
    {
        return ['status' => 200, 'body' => ['success' => true, 'data' => $data]];
    }

    /** @return array{status:int, body:mixed} */
    private static function error(int $status, string $code, array $extra = []): array
    {
        return ['status' => $status, 'body' => ['success' => false, 'error' => 'x', 'code' => $code] + $extra];
    }

    /** @return array<string, mixed> */
    private static function term(): array
    {
        return [
            'start_mode' => 'ON_COMPLETION',
            'duration_months' => 12,
            'renewal_type' => 'AUTO_RENEW',
            'renewal_period_months' => 12,
            'notice_days' => 30,
            'reminder_offsets' => [30, 7],
            'notify_counterparty' => false,
            'state' => 'UNTRACKED',
        ];
    }

    public function testUpdateTermPatchesOnlyTheSentKeysAndSendsNull(): void
    {
        $srv = $this->server([self::ok(['term' => self::term()])]);
        $result = self::client($srv)->demands()->updateTerm(self::DEMAND, [
            'term_start_mode' => 'ON_COMPLETION',
            'term_duration_months' => 12,
            'renewal_type' => 'AUTO_RENEW',
            'notice_days' => null,
        ]);
        $this->assertInstanceOf(ApiV1DemandsIdTermPatch200ResponseData::class, $result);
        $this->assertSame('AUTO_RENEW', $result->getTerm()->getRenewalType());
        $req = $srv->requests()[0];
        $this->assertSame('PATCH', $req['method']);
        $this->assertSame('/api/v1/demands/' . self::DEMAND . '/term', $req['uri']);
        // null clears a key on the server; keys that were not passed stay out.
        $this->assertEquals(
            [
                'term_start_mode' => 'ON_COMPLETION',
                'term_duration_months' => 12,
                'renewal_type' => 'AUTO_RENEW',
                'notice_days' => null,
            ],
            json_decode($req['body'], true),
        );
    }

    public function testUpdateTermSendsNotifyCounterpartyOnlyWhenGiven(): void
    {
        $srv = $this->server([self::ok(['term' => self::term()]), self::ok(['term' => self::term()])]);
        self::client($srv)->demands()->updateTerm(self::DEMAND, ['notice_days' => 30]);
        self::client($srv)->demands()->updateTerm(self::DEMAND, ['notify_counterparty' => false]);
        $this->assertSame(['notice_days' => 30], json_decode($srv->requests()[0]['body'], true));
        $this->assertSame(['notify_counterparty' => false], json_decode($srv->requests()[1]['body'], true));
    }

    public function testUpdateTermThrowsTermInvalidWithTheRejectedField(): void
    {
        $srv = $this->server([self::error(400, 'TERM_INVALID', ['field' => 'term_fixed_end_date'])]);
        try {
            self::client($srv)->demands()->updateTerm(self::DEMAND, ['term_fixed_end_date' => '2027-01-31', 'term_duration_months' => 12]);
            $this->fail('expected ImzalaException');
        } catch (ImzalaException $e) {
            $this->assertSame('TERM_INVALID', $e->getErrorCode());
            $this->assertSame('term_fixed_end_date', json_decode((string) $e->getBody(), true)['field']);
        }
        $this->assertCount(1, $srv->requests());
    }

    public function testArchivePostsToTheArchivePath(): void
    {
        $srv = $this->server([self::ok(['archived_at' => '2026-09-28T09:00:00.000Z'])]);
        $result = self::client($srv)->demands()->archive(self::DEMAND);
        $this->assertInstanceOf(ApiV1DemandsIdArchivePost200ResponseData::class, $result);
        $this->assertNotNull($result->getArchivedAt());
        $req = $srv->requests()[0];
        $this->assertSame('POST', $req['method']);
        $this->assertSame('/api/v1/demands/' . self::DEMAND . '/archive', $req['uri']);
    }

    public function testUnarchivePostsToTheUnarchivePath(): void
    {
        $srv = $this->server([self::ok(['archived_at' => null])]);
        $result = self::client($srv)->demands()->unarchive(self::DEMAND);
        $this->assertInstanceOf(ApiV1DemandsIdUnarchivePost200ResponseData::class, $result);
        $req = $srv->requests()[0];
        $this->assertSame('POST', $req['method']);
        $this->assertSame('/api/v1/demands/' . self::DEMAND . '/unarchive', $req['uri']);
    }

    /** @return iterable<array{string}> */
    public static function archiveErrors(): iterable
    {
        yield ['DEMAND_NOT_ARCHIVABLE'];
        yield ['DEMAND_REJECTED_CANCEL_FIRST'];
    }

    #[DataProvider('archiveErrors')]
    public function testArchiveThrows(string $code): void
    {
        $srv = $this->server([self::error(409, $code)]);
        try {
            self::client($srv)->demands()->archive(self::DEMAND);
            $this->fail('expected ImzalaException');
        } catch (ImzalaException $e) {
            $this->assertSame($code, $e->getErrorCode());
        }
        $this->assertCount(1, $srv->requests());
    }

    public function testDeleteOfAnArchivedDemandThrowsDemandArchived(): void
    {
        $srv = $this->server([self::error(409, 'DEMAND_ARCHIVED')]);
        try {
            self::client($srv)->demands()->delete(self::DEMAND);
            $this->fail('expected ImzalaException');
        } catch (ImzalaException $e) {
            $this->assertSame('DEMAND_ARCHIVED', $e->getErrorCode());
        }
    }

    public function testListSendsTheArchiveFilterOnlyWhenSet(): void
    {
        $empty = self::ok(['demands' => [], 'pagination' => ['page' => 1, 'limit' => 20, 'total' => 0]]);
        $srv = $this->server([$empty, $empty]);
        self::client($srv)->demands()->list(archived: 'exclude');
        self::client($srv)->demands()->list();
        parse_str((string) parse_url($srv->requests()[0]['uri'], PHP_URL_QUERY), $first);
        parse_str((string) parse_url($srv->requests()[1]['uri'], PHP_URL_QUERY), $second);
        $this->assertSame('exclude', $first['archived'] ?? null);
        $this->assertArrayNotHasKey('archived', $second);
    }

    /** @return iterable<string, array{callable(ImzalaClient): mixed}> */
    public static function writes(): iterable
    {
        yield 'updateTerm' => [fn (ImzalaClient $c) => $c->demands()->updateTerm(self::DEMAND, ['notice_days' => 30])];
        yield 'archive' => [fn (ImzalaClient $c) => $c->demands()->archive(self::DEMAND)];
        yield 'unarchive' => [fn (ImzalaClient $c) => $c->demands()->unarchive(self::DEMAND)];
    }

    #[DataProvider('writes')]
    public function testRateLimitedWriteIsNotRetried(callable $call): void
    {
        $srv = $this->server([
            [
                'status' => 429,
                'body' => ['success' => false, 'error' => 'Çok fazla istek', 'code' => 'RATE_LIMIT_EXCEEDED'],
                'headers' => ['Retry-After' => '0'],
            ],
            self::ok([]),
        ]);
        try {
            $call(self::client($srv));
            $this->fail('expected ImzalaRateLimitException');
        } catch (ImzalaRateLimitException) {
        }
        $this->assertCount(1, $srv->requests());
    }
}
