<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Imzala\Client\Model\ApiV1DemandsDemandIdDispatchPost200ResponseData;
use Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsGet200ResponseData;
use Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPost201ResponseData;
use Imzala\Client\Model\ApiV1TemplatesIdDelete200ResponseData;
use Imzala\FileInput;
use Imzala\ImzalaClient;
use Imzala\ImzalaException;
use Imzala\ImzalaRateLimitException;
use Imzala\ImzalaValidationException;
use Imzala\Tests\Support\LocalHttpServer;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

/**
 * Multi-document envelope endpoints (demands()->documents()) and
 * demands()->dispatch(). These tests run the real vendored generated client
 * and Guzzle against a local HTTP server, so they check what actually goes
 * over the wire: the path slots, the multipart field that carries the
 * idempotency key, and how a 409 replay body is read back. Mirrors
 * packages/node/src/__tests__/envelope.test.ts.
 */
final class EnvelopeTest extends TestCase
{
    private const DEMAND = '11111111-1111-4111-8111-111111111111';
    private const DOC = '22222222-2222-4222-8222-222222222222';
    private const DOC_B = '33333333-3333-4333-8333-333333333333';
    private const PARTY = '44444444-4444-4444-8444-444444444444';

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

    private static function pdf(): FileInput
    {
        return new FileInput('%PDF-1.7 test', 'kira.pdf', 'application/pdf');
    }

    /** @return array<string, mixed> */
    private static function doc(): array
    {
        return ['id' => self::DOC, 'order' => 1, 'title' => 'Kira sözleşmesi', 'doc_kind' => 'CONTRACT'];
    }

    /** @return array{status:int, body:mixed} */
    private static function ok(mixed $data): array
    {
        return ['status' => 200, 'body' => ['success' => true, 'data' => $data]];
    }

    /** @return array{status:int, body:mixed, headers:array<string,string>} */
    private static function rateLimited(string $retryAfter = '0'): array
    {
        return [
            'status' => 429,
            'body' => ['success' => false, 'error' => 'Çok fazla istek', 'code' => 'RATE_LIMIT_EXCEEDED'],
            'headers' => ['Retry-After' => $retryAfter],
        ];
    }

    /** @return array{status:int, body:mixed} */
    private static function disabled(): array
    {
        return ['status' => 409, 'body' => ['success' => false, 'error' => 'Kapalı', 'code' => 'ENVELOPE_MULTI_DOC_DISABLED']];
    }

    /** @return array{status:int, body:mixed} */
    private static function conflict(string $code, bool $withDocument = false): array
    {
        $body = ['success' => false, 'error' => 'x', 'code' => $code];
        if ($withDocument) {
            $body['data'] = ['document' => self::doc()];
        }
        return ['status' => 409, 'body' => $body];
    }

    // --- path slots and bodies on the wire ---------------------------------

    public function testListSendsDemandIdInThePathAndViewAsAQueryParameter(): void
    {
        $srv = $this->server([self::ok(['documents' => [self::doc()]])]);
        $result = self::client($srv)->demands()->documents()->list(self::DEMAND, 'wizard');
        $this->assertInstanceOf(ApiV1DemandsDemandIdDocumentsGet200ResponseData::class, $result);
        $this->assertSame(self::DOC, $result->getDocuments()[0]->getId());
        $this->assertSame('Kira sözleşmesi', $result->getDocuments()[0]->getTitle());
        $req = $srv->requests();
        $this->assertCount(1, $req);
        $this->assertSame('GET', $req[0]['method']);
        $this->assertSame('/api/v1/demands/' . self::DEMAND . '/documents?view=wizard', $req[0]['uri']);
    }

    public function testListWithoutViewSendsNoQueryString(): void
    {
        $srv = $this->server([self::ok(['documents' => []])]);
        self::client($srv)->demands()->documents()->list(self::DEMAND);
        $this->assertSame('/api/v1/demands/' . self::DEMAND . '/documents', $srv->requests()[0]['uri']);
    }

    public function testCreatePostsTheJsonBody(): void
    {
        $srv = $this->server([['status' => 201, 'body' => ['success' => true, 'data' => ['document' => self::doc()]]]]);
        $result = self::client($srv)->demands()->documents()->create(self::DEMAND, [
            'title' => 'KVKK aydınlatma',
            'doc_kind' => 'KVKK_NOTICE',
            'is_required' => false,
            'signature_required' => false,
        ]);
        $this->assertInstanceOf(ApiV1DemandsDemandIdDocumentsPost201ResponseData::class, $result);
        $this->assertSame(self::DOC, $result->getDocument()->getId());
        $req = $srv->requests()[0];
        $this->assertSame('POST', $req['method']);
        $this->assertSame('/api/v1/demands/' . self::DEMAND . '/documents', $req['uri']);
        $this->assertSame(
            ['title' => 'KVKK aydınlatma', 'doc_kind' => 'KVKK_NOTICE', 'is_required' => false, 'signature_required' => false],
            json_decode($req['body'], true),
        );
    }

    public function testUpdateKeepsDemandIdAndDocIdInTheirOwnSlots(): void
    {
        $srv = $this->server([self::ok(['document' => self::doc()])]);
        $result = self::client($srv)->demands()->documents()->update(self::DEMAND, self::DOC, ['title' => 'Yeni başlık']);
        $this->assertInstanceOf(ApiV1DemandsDemandIdDocumentsPost201ResponseData::class, $result);
        $req = $srv->requests()[0];
        $this->assertSame('PATCH', $req['method']);
        $this->assertSame('/api/v1/demands/' . self::DEMAND . '/documents/' . self::DOC, $req['uri']);
        $this->assertSame(['title' => 'Yeni başlık'], json_decode($req['body'], true));
    }

    public function testDeleteKeepsDemandIdAndDocIdInTheirOwnSlots(): void
    {
        $srv = $this->server([self::ok(['id' => self::DOC, 'deleted' => true])]);
        $result = self::client($srv)->demands()->documents()->delete(self::DEMAND, self::DOC);
        $this->assertInstanceOf(ApiV1TemplatesIdDelete200ResponseData::class, $result);
        $this->assertSame(self::DOC, $result->getId());
        $this->assertTrue($result->getDeleted());
        $req = $srv->requests()[0];
        $this->assertSame('DELETE', $req['method']);
        $this->assertSame('/api/v1/demands/' . self::DEMAND . '/documents/' . self::DOC, $req['uri']);
    }

    public function testReorderSendsDocumentIdsInTheGivenOrder(): void
    {
        $srv = $this->server([self::ok(['documents' => [self::doc()]])]);
        $result = self::client($srv)->demands()->documents()->reorder(self::DEMAND, [self::DOC_B, self::DOC]);
        $this->assertInstanceOf(ApiV1DemandsDemandIdDocumentsGet200ResponseData::class, $result);
        $req = $srv->requests()[0];
        $this->assertSame('PUT', $req['method']);
        $this->assertSame('/api/v1/demands/' . self::DEMAND . '/documents/order', $req['uri']);
        $this->assertSame(['document_ids' => [self::DOC_B, self::DOC]], json_decode($req['body'], true));
    }

    public function testSetAssignmentsKeepsDemandIdAndDocIdApartAndSendsPartyIds(): void
    {
        $srv = $this->server([self::ok(['document' => self::doc()])]);
        $result = self::client($srv)->demands()->documents()->setAssignments(self::DEMAND, self::DOC, [self::PARTY]);
        $this->assertInstanceOf(ApiV1DemandsDemandIdDocumentsPost201ResponseData::class, $result);
        $req = $srv->requests()[0];
        $this->assertSame('PUT', $req['method']);
        $this->assertSame('/api/v1/demands/' . self::DEMAND . '/documents/' . self::DOC . '/assignments', $req['uri']);
        $this->assertSame(['party_ids' => [self::PARTY]], json_decode($req['body'], true));
    }

    public function testDispatchSendsNoSendInvitationsFieldByDefault(): void
    {
        $srv = $this->server([self::ok(['demand_id' => self::DEMAND, 'status' => 'PENDING', 'dispatched' => true])]);
        $result = self::client($srv)->demands()->dispatch(self::DEMAND);
        $this->assertInstanceOf(ApiV1DemandsDemandIdDispatchPost200ResponseData::class, $result);
        $this->assertSame(self::DEMAND, $result->getDemandId());
        $this->assertTrue($result->getDispatched());
        $req = $srv->requests()[0];
        $this->assertSame('POST', $req['method']);
        $this->assertSame('/api/v1/demands/' . self::DEMAND . '/dispatch', $req['uri']);
        $sent = $req['body'] !== '' ? json_decode($req['body'], true) : [];
        $this->assertArrayNotHasKey('send_invitations', $sent);
    }

    public function testDispatchForwardsABooleanOrAStringSendInvitationsValueUnchanged(): void
    {
        $srv = $this->server([self::ok(['dispatched' => true])]);
        self::client($srv)->demands()->dispatch(self::DEMAND, false);
        self::client($srv)->demands()->dispatch(self::DEMAND, 'email');
        self::client($srv)->demands()->dispatch(self::DEMAND, true);
        $req = $srv->requests();
        $this->assertSame(['send_invitations' => false], json_decode($req[0]['body'], true));
        $this->assertSame(['send_invitations' => 'email'], json_decode($req[1]['body'], true));
        $this->assertSame(['send_invitations' => true], json_decode($req[2]['body'], true));
    }

    // --- upload -------------------------------------------------------------

    public function testUploadSendsTheIdempotencyKeyAsTheIdempotencyKeyBodyFieldNotAsAHeader(): void
    {
        $srv = $this->server([self::ok(['document' => self::doc()])]);
        $result = self::client($srv)->demands()->documents()->upload(
            self::DEMAND,
            self::pdf(),
            title: 'Kira sözleşmesi',
            idempotencyKey: 'siparis-42',
            docKind: 'CONTRACT',
            isRequired: false,
        );
        $this->assertInstanceOf(ApiV1DemandsDemandIdDocumentsPost201ResponseData::class, $result);
        $this->assertSame(self::DOC, $result->getDocument()->getId());
        $req = $srv->requests()[0];
        $this->assertSame('POST', $req['method']);
        $this->assertSame('/api/v1/demands/' . self::DEMAND . '/documents/upload', $req['uri']);
        $this->assertArrayNotHasKey('idempotency-key', $req['headers']);
        $this->assertStringStartsWith('multipart/form-data', $req['headers']['content-type']);
        $this->assertSame('siparis-42', $req['form']['idempotency_key']);
        $this->assertSame('Kira sözleşmesi', $req['form']['title']);
        $this->assertSame('CONTRACT', $req['form']['doc_kind']);
        $this->assertSame('false', $req['form']['is_required']);
        $this->assertSame('kira.pdf', $req['files']['file']['name']);
        $this->assertSame('%PDF-1.7 test', $req['files']['file']['content']);
    }

    public function testUploadOmitsDocKindAndIsRequiredWhenNotGiven(): void
    {
        $srv = $this->server([self::ok(['document' => self::doc()])]);
        self::client($srv)->demands()->documents()->upload(self::DEMAND, self::pdf(), 'T', 'k-1');
        $form = $srv->requests()[0]['form'];
        $this->assertArrayNotHasKey('doc_kind', $form);
        $this->assertArrayNotHasKey('is_required', $form);
    }

    /** @return array<string, array{string}> */
    public static function badKeys(): array
    {
        return [
            'empty' => [''],
            'non-ASCII' => ['sipariş-1'],
            'line break' => ["a\r\nX-Evil: 1"],
        ];
    }

    #[DataProvider('badKeys')]
    public function testUploadRejectsABadIdempotencyKeyLocallyWithoutSendingAnything(string $key): void
    {
        $srv = $this->server([self::ok(['document' => self::doc()])]);
        try {
            self::client($srv)->demands()->documents()->upload(self::DEMAND, self::pdf(), 'T', $key);
            $this->fail('expected ImzalaValidationException');
        } catch (ImzalaValidationException $e) {
            $this->assertNull($e->getStatusCode());
        }
        $this->assertCount(0, $srv->requests());
    }

    public function testUploadRetriesOnceAfterA429AndSendsTheFileAgain(): void
    {
        $srv = $this->server([self::rateLimited(), self::ok(['document' => self::doc()])]);
        $result = self::client($srv)->demands()->documents()->upload(self::DEMAND, self::pdf(), 'T', 'k-1');
        $this->assertSame(self::DOC, $result->getDocument()->getId());
        $req = $srv->requests();
        $this->assertCount(2, $req);
        $this->assertSame('%PDF-1.7 test', $req[1]['files']['file']['content']);
        $this->assertSame('k-1', $req[1]['form']['idempotency_key']);
    }

    public function testUploadDoesNotRetryWhenRetryAfterExceedsTheCap(): void
    {
        $srv = $this->server([self::rateLimited('61')]);
        try {
            self::client($srv)->demands()->documents()->upload(self::DEMAND, self::pdf(), 'T', 'k-1');
            $this->fail('expected ImzalaRateLimitException');
        } catch (ImzalaRateLimitException $e) {
            $this->assertSame(429, $e->getStatusCode());
        }
        $this->assertCount(1, $srv->requests());
    }

    public function testUploadReturnsTheEarlierDocumentOnA409IdempotentReplayInsteadOfThrowing(): void
    {
        $srv = $this->server([self::conflict('IDEMPOTENT_REPLAY', true)]);
        $result = self::client($srv)->demands()->documents()->upload(self::DEMAND, self::pdf(), 'T', 'k-1');
        $this->assertInstanceOf(ApiV1DemandsDemandIdDocumentsPost201ResponseData::class, $result);
        $this->assertSame(self::DOC, $result->getDocument()->getId());
        $this->assertSame('Kira sözleşmesi', $result->getDocument()->getTitle());
        $this->assertCount(1, $srv->requests());
    }

    public function testUploadTreatsAReplayThatFollowsA429RetryAsSuccessToo(): void
    {
        $srv = $this->server([self::rateLimited(), self::conflict('IDEMPOTENT_REPLAY', true)]);
        $result = self::client($srv)->demands()->documents()->upload(self::DEMAND, self::pdf(), 'T', 'k-1');
        $this->assertSame(self::DOC, $result->getDocument()->getId());
        $this->assertCount(2, $srv->requests());
    }

    public function testUploadThrowsA409ReplayWithoutADocument(): void
    {
        $srv = $this->server([self::conflict('IDEMPOTENT_REPLAY')]);
        try {
            self::client($srv)->demands()->documents()->upload(self::DEMAND, self::pdf(), 'T', 'k-1');
            $this->fail('expected ImzalaException');
        } catch (ImzalaException $e) {
            $this->assertSame('IDEMPOTENT_REPLAY', $e->getErrorCode());
        }
    }

    public function testUploadThrowsAnyOther409CodeForExampleSigningAlreadyStarted(): void
    {
        $srv = $this->server([self::conflict('SIGNING_ALREADY_STARTED', true)]);
        try {
            self::client($srv)->demands()->documents()->upload(self::DEMAND, self::pdf(), 'T', 'k-1');
            $this->fail('expected ImzalaException');
        } catch (ImzalaException $e) {
            $this->assertSame('SIGNING_ALREADY_STARTED', $e->getErrorCode());
            $this->assertSame(409, $e->getStatusCode());
        }
    }

    public function testUploadThrowsEnvelopeMultiDocDisabled(): void
    {
        $srv = $this->server([self::disabled()]);
        try {
            self::client($srv)->demands()->documents()->upload(self::DEMAND, self::pdf(), 'T', 'k-1');
            $this->fail('expected ImzalaException');
        } catch (ImzalaException $e) {
            $this->assertSame('ENVELOPE_MULTI_DOC_DISABLED', $e->getErrorCode());
        }
    }

    // --- feature switched off -----------------------------------------------

    public function testListThrowsEnvelopeMultiDocDisabledAndDoesNotReturnAnEmptyList(): void
    {
        $srv = $this->server([self::disabled()]);
        try {
            self::client($srv)->demands()->documents()->list(self::DEMAND);
            $this->fail('expected ImzalaException');
        } catch (ImzalaException $e) {
            $this->assertSame('ENVELOPE_MULTI_DOC_DISABLED', $e->getErrorCode());
            $this->assertSame(409, $e->getStatusCode());
        }
        $this->assertCount(1, $srv->requests());
    }

    /** @return array<string, array{callable(ImzalaClient):mixed}> */
    public static function unkeyedWrites(): array
    {
        return [
            'create' => [static fn (ImzalaClient $c) => $c->demands()->documents()->create(self::DEMAND, ['title' => 'T'])],
            'update' => [static fn (ImzalaClient $c) => $c->demands()->documents()->update(self::DEMAND, self::DOC, ['title' => 'T'])],
            'delete' => [static fn (ImzalaClient $c) => $c->demands()->documents()->delete(self::DEMAND, self::DOC)],
            'reorder' => [static fn (ImzalaClient $c) => $c->demands()->documents()->reorder(self::DEMAND, [self::DOC])],
            'setAssignments' => [static fn (ImzalaClient $c) => $c->demands()->documents()->setAssignments(self::DEMAND, self::DOC, [self::PARTY])],
        ];
    }

    #[DataProvider('unkeyedWrites')]
    public function testDocumentWritesThrowEnvelopeMultiDocDisabled(callable $call): void
    {
        $srv = $this->server([self::disabled()]);
        try {
            $call(self::client($srv));
            $this->fail('expected ImzalaException');
        } catch (ImzalaException $e) {
            $this->assertSame('ENVELOPE_MULTI_DOC_DISABLED', $e->getErrorCode());
        }
    }

    // --- writes without an idempotency key are never retried ----------------

    /** @return array<string, array{callable(ImzalaClient):mixed}> */
    public static function unkeyedWritesAndDispatch(): array
    {
        return self::unkeyedWrites() + [
            'dispatch' => [static fn (ImzalaClient $c) => $c->demands()->dispatch(self::DEMAND)],
        ];
    }

    #[DataProvider('unkeyedWritesAndDispatch')]
    public function testA429IsThrownAfterExactlyOneRequest(callable $call): void
    {
        $srv = $this->server([self::rateLimited(), self::ok([])]);
        try {
            $call(self::client($srv));
            $this->fail('expected ImzalaRateLimitException');
        } catch (ImzalaRateLimitException) {
        }
        $this->assertCount(1, $srv->requests());
    }

    public function testListIsRetriedAfterA429(): void
    {
        $srv = $this->server([self::rateLimited(), self::ok(['documents' => []])]);
        $result = self::client($srv)->demands()->documents()->list(self::DEMAND);
        $this->assertSame([], $result->getDocuments());
        $this->assertCount(2, $srv->requests());
    }

    // --- dispatch errors ----------------------------------------------------

    /** @return array<string, array{string}> */
    public static function dispatchErrorCodes(): array
    {
        return [
            'DISPATCH_NO_PARTIES' => ['DISPATCH_NO_PARTIES'],
            'DISPATCH_TOO_MANY' => ['DISPATCH_TOO_MANY'],
            'QES_NOT_SUPPORTED_MULTI_DOCUMENT' => ['QES_NOT_SUPPORTED_MULTI_DOCUMENT'],
        ];
    }

    #[DataProvider('dispatchErrorCodes')]
    public function testDispatchThrowsTheServerCode(string $code): void
    {
        $srv = $this->server([self::conflict($code)]);
        try {
            self::client($srv)->demands()->dispatch(self::DEMAND);
            $this->fail('expected ImzalaException');
        } catch (ImzalaException $e) {
            $this->assertSame($code, $e->getErrorCode());
            $this->assertSame(409, $e->getStatusCode());
        }
        $this->assertCount(1, $srv->requests());
    }
}
