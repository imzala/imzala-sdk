<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Generator;
use Imzala\Client\Api\ContactsApi;
use Imzala\Client\Api\DemandsApi;
use Imzala\Client\Api\RemindersApi;
use Imzala\Client\Api\ReportsApi;
use Imzala\Client\Api\TemplatesApi;
use Imzala\Client\Api\TimestampsApi;
use Imzala\Client\Model\ApiV1ContactsGet200Response;
use Imzala\Client\Model\ApiV1ContactsGet200ResponseData;
use Imzala\Client\Model\ApiV1ContactsPost201Response;
use Imzala\Client\Model\ApiV1ContactsPostRequest;
use Imzala\Client\Model\ApiV1DemandsBulkPost200Response;
use Imzala\Client\Model\ApiV1DemandsBulkPost200ResponseData;
use Imzala\Client\Model\ApiV1DemandsBulkPostRequest;
use Imzala\Client\Model\ApiV1DemandsUploadPost201Response;
use Imzala\Client\Model\ApiV1FieldTemplatesGet200Response;
use Imzala\Client\Model\ApiV1FieldTemplatesGet200ResponseData;
use Imzala\Client\Model\ApiV1FieldTemplatesIdGet200Response;
use Imzala\Client\Model\ApiV1FieldTemplatesIdPreviewLayoutPost200Response;
use Imzala\Client\Model\ApiV1ReportsGet200Response;
use Imzala\Client\Model\ApiV1ReportsGet200ResponseData;
use Imzala\Client\Model\ApiV1TemplatesIdGet200Response;
use Imzala\Client\Model\ApiV1TimestampsGet200Response;
use Imzala\Client\Model\ApiV1TimestampsGet200ResponseData;
use Imzala\Client\Model\ApiV1TimestampsIdGet200Response;
use Imzala\Client\Model\ContactSummary;
use Imzala\Client\Model\CreateDemandRequest;
use Imzala\Client\Model\CreatedDemandUpload;
use Imzala\Client\Model\FieldLayoutPreview;
use Imzala\Client\Model\FieldTemplateDetail;
use Imzala\Client\Model\TimestampListItem;
use Imzala\Client\ObjectSerializer;
use Imzala\ContactsResource;
use Imzala\DemandsResource;
use Imzala\FieldTemplatesResource;
use Imzala\FileInput;
use Imzala\Http;
use Imzala\ImzalaClient;
use Imzala\ImzalaException;
use Imzala\ReportsResource;
use Imzala\RetryConfig;
use Imzala\TimestampsResource;
use Imzala\UploadDemandParams;
use Imzala\UploadPartyInput;
use PHPUnit\Framework\TestCase;

/**
 * Field templates, contacts, reports, timestamp listing, bulk create, per
 * document PDFs and the new upload fields. Mirrors resources.test.ts (Node).
 * Every generated call is checked slot by slot: new optional parameters are
 * inserted in the middle of generated signatures, so a positional call would
 * silently shift values.
 */
final class ResourcesTest extends TestCase
{
    private static function noRetry(): RetryConfig
    {
        return new RetryConfig(0, 0);
    }

    private function demands(DemandsApi $api): DemandsResource
    {
        return new DemandsResource($api, $this->createMock(RemindersApi::class), self::noRetry());
    }

    // --- field templates ---

    public function testFieldTemplatesListForwardsPaging(): void
    {
        $data = new ApiV1FieldTemplatesGet200ResponseData();
        $api = $this->createMock(TemplatesApi::class);
        $api->expects($this->once())
            ->method('apiV1FieldTemplatesGetWithHttpInfo')
            ->willReturnCallback(function (...$args) use ($data) {
                $this->assertSame([2, 50], array_slice($args, 0, 2));
                return [new ApiV1FieldTemplatesGet200Response(['success' => true, 'data' => $data]), 200, []];
            });
        $resource = new FieldTemplatesResource($api, $this->createMock(DemandsApi::class), self::noRetry());
        $this->assertSame($data, $resource->list(2, 50));
    }

    public function testFieldTemplatesListDefaultsToServerPaging(): void
    {
        $api = $this->createMock(TemplatesApi::class);
        $api->expects($this->once())
            ->method('apiV1FieldTemplatesGetWithHttpInfo')
            ->willReturnCallback(function (...$args) {
                $this->assertNull($args[0] ?? null);
                $this->assertNull($args[1] ?? null);
                return [new ApiV1FieldTemplatesGet200Response(['success' => true, 'data' => new ApiV1FieldTemplatesGet200ResponseData()]), 200, []];
            });
        (new FieldTemplatesResource($api, $this->createMock(DemandsApi::class), self::noRetry()))->list();
    }

    public function testFieldTemplatesGetIsRetriedAsAGet(): void
    {
        $data = new FieldTemplateDetail();
        $api = $this->createMock(TemplatesApi::class);
        $calls = 0;
        $api->expects($this->exactly(2))
            ->method('apiV1FieldTemplatesIdGetWithHttpInfo')
            ->with('ft-1')
            ->willReturnCallback(function () use (&$calls, $data) {
                if (++$calls === 1) {
                    throw new \Imzala\Client\ApiException('[503]', 503, [], null);
                }
                return [new ApiV1FieldTemplatesIdGet200Response(['success' => true, 'data' => $data]), 200, []];
            });
        $resource = new FieldTemplatesResource($api, $this->createMock(DemandsApi::class), new RetryConfig(1, 0));
        $this->assertSame($data, $resource->get('ft-1'));
    }

    public function testPreviewLayoutSendsOnePdfAndOnAnchorMissInTheirSlots(): void
    {
        $data = new FieldLayoutPreview();
        $captured = null;
        $templatesApi = $this->createMock(TemplatesApi::class);
        $demandsApi = $this->createMock(DemandsApi::class);
        $demandsApi->expects($this->once())
            ->method('apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo')
            ->willReturnCallback(function (...$args) use (&$captured, $data) {
                $this->assertSame('ft-1', $args[0]);
                $this->assertIsArray($args[1]);
                $this->assertCount(1, $args[1]);
                $this->assertInstanceOf(\SplFileObject::class, $args[1][0]);
                $captured = $args[1][0]->getRealPath();
                $this->assertStringEndsWith('sozlesme.pdf', $captured);
                $this->assertSame('drop', $args[2]);
                return [new ApiV1FieldTemplatesIdPreviewLayoutPost200Response(['success' => true, 'data' => $data]), 200, []];
            });
        $resource = new FieldTemplatesResource($templatesApi, $demandsApi, self::noRetry());

        $this->assertSame($data, $resource->previewLayout('ft-1', [new FileInput('%PDF-1.4', 'sozlesme.pdf')], 'drop'));
        $this->assertIsString($captured);
        $this->assertFileDoesNotExist($captured);
        $this->assertDirectoryDoesNotExist(dirname($captured));
    }

    public function testPreviewLayoutIsNotRetriedAndCleansUpOnFailure(): void
    {
        $captured = null;
        $demandsApi = $this->createMock(DemandsApi::class);
        $demandsApi->expects($this->once())
            ->method('apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo')
            ->willReturnCallback(function (...$args) use (&$captured) {
                $captured = $args[1][0]->getRealPath();
                throw new \Imzala\Client\ApiException('[429]', 429, ['Retry-After' => ['0']], '{"success":false}');
            });
        $resource = new FieldTemplatesResource($this->createMock(TemplatesApi::class), $demandsApi, new RetryConfig(2, 0));
        try {
            $resource->previewLayout('ft-1', [new FileInput('%PDF-1.4', 'sozlesme.pdf')]);
            $this->fail('expected ImzalaException');
        } catch (ImzalaException $e) {
            $this->assertSame(429, $e->getStatusCode());
        }
        $this->assertIsString($captured);
        $this->assertFileDoesNotExist($captured);
    }

    // --- contacts ---

    public function testContactsListForwardsEveryFilterInItsSlot(): void
    {
        $data = new ApiV1ContactsGet200ResponseData();
        $api = $this->createMock(ContactsApi::class);
        $api->expects($this->once())
            ->method('apiV1ContactsGetWithHttpInfo')
            ->willReturnCallback(function (...$args) use ($data) {
                // generated order: page, limit, q, sort, company_id, archived
                $this->assertSame([3, 50, 'Yılmaz', '-updatedAt', 'co-1', true], array_slice($args, 0, 6));
                return [new ApiV1ContactsGet200Response(['success' => true, 'data' => $data]), 200, []];
            });
        $resource = new ContactsResource($api, self::noRetry());
        $this->assertSame($data, $resource->list(q: 'Yılmaz', page: 3, limit: 50, sort: '-updatedAt', companyId: 'co-1', archived: true));
    }

    public function testContactsListAllWalksPagesAndKeepsFilters(): void
    {
        $pages = [
            1 => new ApiV1ContactsGet200ResponseData(['contacts' => [new ContactSummary(['id' => 'c1']), new ContactSummary(['id' => 'c2'])], 'total' => 3, 'page' => 1, 'limit' => 2]),
            2 => new ApiV1ContactsGet200ResponseData(['contacts' => [new ContactSummary(['id' => 'c3'])], 'total' => 3, 'page' => 2, 'limit' => 2]),
        ];
        $seen = [];
        $api = $this->createMock(ContactsApi::class);
        $api->expects($this->exactly(2))
            ->method('apiV1ContactsGetWithHttpInfo')
            ->willReturnCallback(function (...$args) use (&$seen, $pages) {
                $seen[] = $args[0];
                $this->assertSame('Ayşe', $args[2]);
                return [new ApiV1ContactsGet200Response(['success' => true, 'data' => $pages[$args[0]]]), 200, []];
            });
        $resource = new ContactsResource($api, self::noRetry());
        $gen = $resource->listAll(q: 'Ayşe', limit: 2);
        $this->assertInstanceOf(Generator::class, $gen);
        $ids = array_map(static fn (ContactSummary $c) => $c->getId(), iterator_to_array($gen, false));
        $this->assertSame(['c1', 'c2', 'c3'], $ids);
        $this->assertSame([1, 2], $seen);
    }

    public function testContactsCreateAcceptsAnArray(): void
    {
        $data = new ContactSummary();
        $api = $this->createMock(ContactsApi::class);
        $api->expects($this->once())
            ->method('apiV1ContactsPostWithHttpInfo')
            ->with($this->callback(fn ($req) => $req instanceof ApiV1ContactsPostRequest && $req->getEmail() === 'ayse@example.com'))
            ->willReturn([new ApiV1ContactsPost201Response(['success' => true, 'data' => $data]), 201, []]);
        $resource = new ContactsResource($api, self::noRetry());
        $this->assertSame($data, $resource->create([
            'first_name' => 'Ayşe', 'last_name' => 'Yılmaz', 'email' => 'ayse@example.com', 'phone' => '+905551112233',
        ]));
    }

    // --- reports ---

    public function testReportsGetUnwraps(): void
    {
        $data = new ApiV1ReportsGet200ResponseData();
        $api = $this->createMock(ReportsApi::class);
        $api->expects($this->once())
            ->method('apiV1ReportsGetWithHttpInfo')
            ->willReturn([new ApiV1ReportsGet200Response(['success' => true, 'data' => $data]), 200, []]);
        $this->assertSame($data, (new ReportsResource($api, self::noRetry()))->get());
    }

    // --- timestamps ---

    public function testTimestampsListForwardsEveryFilterInItsSlot(): void
    {
        $data = new ApiV1TimestampsGet200ResponseData();
        $api = $this->createMock(TimestampsApi::class);
        $api->expects($this->once())
            ->method('apiV1TimestampsGetWithHttpInfo')
            ->willReturnCallback(function (...$args) use ($data) {
                // generated order: page, limit, q, status, from, to, sort
                $this->assertSame([2, 30, 'eser', 'COMPLETED', '2026-01-01', '2026-02-01', 'createdAt'], array_slice($args, 0, 7));
                return [new ApiV1TimestampsGet200Response(['success' => true, 'data' => $data]), 200, []];
            });
        $resource = new TimestampsResource($api, self::noRetry());
        $this->assertSame($data, $resource->list(
            q: 'eser', status: 'COMPLETED', from: '2026-01-01', to: '2026-02-01', page: 2, limit: 30, sort: 'createdAt'
        ));
    }

    public function testTimestampsGetUnwraps(): void
    {
        $data = new TimestampListItem();
        $api = $this->createMock(TimestampsApi::class);
        $api->expects($this->once())
            ->method('apiV1TimestampsIdGetWithHttpInfo')
            ->with('ts-1')
            ->willReturn([new ApiV1TimestampsIdGet200Response(['success' => true, 'data' => $data]), 200, []]);
        $this->assertSame($data, (new TimestampsResource($api, self::noRetry()))->get('ts-1'));
    }

    public function testTimestampsResourceStillConstructsWithOnlyTheApi(): void
    {
        $this->assertInstanceOf(TimestampsResource::class, new TimestampsResource($this->createMock(TimestampsApi::class)));
    }

    // --- demands ---

    public function testCreateBulkSendsTheBodyAndNoWorkspaceHeader(): void
    {
        $data = new ApiV1DemandsBulkPost200ResponseData();
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsBulkPostWithHttpInfo')
            ->willReturnCallback(function (...$args) use ($data) {
                $this->assertInstanceOf(ApiV1DemandsBulkPostRequest::class, $args[0]);
                $this->assertSame('tpl-1', $args[0]->getTemplateId());
                $this->assertNull($args[1] ?? null);
                return [new ApiV1DemandsBulkPost200Response(['success' => true, 'data' => $data]), 200, []];
            });
        $this->assertSame($data, $this->demands($api)->createBulk(['template_id' => 'tpl-1', 'rows' => []]));
    }

    public function testCreateKeepsTheTypedRequestAndPutsTheKeyInSlotTwo(): void
    {
        $request = new CreateDemandRequest(['template_id' => 'tpl-1']);
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsPostWithHttpInfo')
            ->willReturnCallback(function (...$args) use ($request) {
                $this->assertSame($request, $args[0]);
                $this->assertSame('siparis-1', $args[1]);
                return [new \Imzala\Client\Model\ApiV1DemandsPost201Response(['success' => true, 'data' => new \Imzala\Client\Model\CreatedDemand()]), 201, []];
            });
        $this->demands($api)->create($request, 'siparis-1');
    }

    public function testGetDocumentPdfReturnsBytesAndForwardsBothIds(): void
    {
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo')
            ->with('demand-1', 'doc-2')
            ->willReturn(['%PDF-1.7 belge', 200, []]);
        $this->assertSame('%PDF-1.7 belge', $this->demands($api)->getDocumentPdf('demand-1', 'doc-2'));
    }

    public function testGetDocumentPdfThrowsOnADeclared404(): void
    {
        $api = $this->createMock(DemandsApi::class);
        $api->method('apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo')
            ->willReturn([(object) ['success' => false, 'error' => 'DEMAND_NOT_FOUND'], 404, []]);
        $this->expectException(ImzalaException::class);
        $this->demands($api)->getDocumentPdf('demand-1', 'doc-2');
    }

    public function testUploadDocumentPutsEveryNewFieldInItsOwnSlot(): void
    {
        $data = new CreatedDemandUpload();
        $args = null;
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsUploadPostWithHttpInfo')
            ->willReturnCallback(function (...$a) use (&$args, $data) {
                $args = $a;
                return [new ApiV1DemandsUploadPost201Response(['success' => true, 'data' => $data]), 201, []];
            });

        $params = (new UploadDemandParams(
            [new FileInput('%PDF-1.4', 'sozlesme.pdf')],
            [new UploadPartyInput('Ayşe', 'Yılmaz', 'ayse@example.com', '+905551112233', 'rol-1')]
        ))
            ->withOrder([0])
            ->withTitle('Başlık')
            ->withDescription('Açıklama')
            ->withIdempotencyKey('yukleme-1')
            ->withFieldTemplateId('ft-1')
            ->withForce(true)
            ->withSendInvitations('sms')
            ->withOnAnchorMiss('drop');

        $this->assertSame($data, $this->demands($api)->uploadDocument($params));

        // generated order: files, parties, idempotency_key, order, title,
        // description, field_template_id, force, send_invitations, on_anchor_miss
        $this->assertIsArray($args[0]);
        $parties = json_decode($args[1], true);
        $this->assertSame('rol-1', $parties[0]['template_party_id']);
        $this->assertSame('+905551112233', $parties[0]['phone']);
        $this->assertSame(
            ['yukleme-1', '[0]', 'Başlık', 'Açıklama', 'ft-1', 'true', 'sms', 'drop'],
            array_slice($args, 2, 8)
        );
    }

    public function testUploadDocumentLeavesOptionalFieldsUnsetByDefault(): void
    {
        $args = null;
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsUploadPostWithHttpInfo')
            ->willReturnCallback(function (...$a) use (&$args) {
                $args = $a;
                return [new ApiV1DemandsUploadPost201Response(['success' => true, 'data' => new CreatedDemandUpload()]), 201, []];
            });
        $params = (new UploadDemandParams(
            [new FileInput('%PDF-1.4', 'sozlesme.pdf')],
            [new UploadPartyInput('Ayşe', 'Yılmaz', 'ayse@example.com')]
        ))->withForce(false);
        $this->demands($api)->uploadDocument($params);

        for ($i = 2; $i <= 9; $i++) {
            $this->assertNull($args[$i] ?? null, "slot {$i}");
        }
        $this->assertArrayNotHasKey('template_party_id', json_decode($args[1], true)[0]);
    }

    public function testUploadDocumentKeepsTurkishCharactersUnescaped(): void
    {
        $parties = null;
        $api = $this->createMock(DemandsApi::class);
        $api->method('apiV1DemandsUploadPostWithHttpInfo')
            ->willReturnCallback(function (...$a) use (&$parties) {
                $parties = $a[1];
                return [new ApiV1DemandsUploadPost201Response(['success' => true, 'data' => new CreatedDemandUpload()]), 201, []];
            });
        $params = new UploadDemandParams(
            [new FileInput('%PDF-1.4', 'sozlesme.pdf')],
            [new UploadPartyInput('Ayşe', 'Yılmaz', 'ayse@example.com')]
        );
        $this->demands($api)->uploadDocument($params);
        $this->assertStringContainsString('"first_name":"Ayşe"', $parties);
        $this->assertStringContainsString('"last_name":"Yılmaz"', $parties);
    }

    public function testUploadDocumentRejectsInvalidUtf8InsteadOfSendingAnEmptyPartyList(): void
    {
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->never())->method('apiV1DemandsUploadPostWithHttpInfo');
        $params = new UploadDemandParams(
            [new FileInput('%PDF-1.4', 'sozlesme.pdf')],
            [new UploadPartyInput("Ay\xC3\x28e", 'Yılmaz', 'ayse@example.com')]
        );
        $this->expectException(\JsonException::class);
        $this->demands($api)->uploadDocument($params);
    }

    // --- 2xx + success:false through the real generated model ---

    public function testSuccessFalseOnA2xxWithTheRealGeneratedModelStillThrows(): void
    {
        // The generated deserializer keeps only the fields the 200 schema
        // declares (success, data). `error` and `code` in the body are
        // dropped before the facade sees them, so the code cannot be read
        // on this path; the call still fails loudly.
        $model = ObjectSerializer::deserialize(
            json_decode('{"success":false,"error":"Geçersiz istek","code":"VALIDATION_FAIL"}'),
            '\Imzala\Client\Model\ApiV1TemplatesIdGet200Response',
            []
        );
        $this->assertInstanceOf(ApiV1TemplatesIdGet200Response::class, $model);

        try {
            Http::unwrap(static fn () => [$model, 200, []]);
            $this->fail('expected ImzalaException');
        } catch (ImzalaException $e) {
            $this->assertSame(200, $e->getStatusCode());
            $this->assertNull($e->getErrorCode());
        }
    }

    // --- client wiring ---

    public function testClientExposesTheNewResources(): void
    {
        $client = new ImzalaClient('imz_test_key', 'https://example.invalid');
        $this->assertInstanceOf(FieldTemplatesResource::class, $client->fieldTemplates());
        $this->assertInstanceOf(ContactsResource::class, $client->contacts());
        $this->assertInstanceOf(ReportsResource::class, $client->reports());
        $this->assertInstanceOf(TimestampsResource::class, $client->timestamps());
    }
}
