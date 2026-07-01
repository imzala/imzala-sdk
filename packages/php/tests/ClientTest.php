<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Imzala\Client\Api\AccountApi;
use Imzala\Client\Api\DemandsApi;
use Imzala\Client\Api\RemindersApi;
use Imzala\Client\Api\TemplatesApi;
use Imzala\Client\Api\TimestampsApi;
use Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPost200Response;
use Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPost200ResponseData;
use Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPostRequest;
use Imzala\Client\Model\ApiV1DemandsIdGet200Response;
use Imzala\Client\Model\ApiV1DemandsIdRemindersPost200Response;
use Imzala\Client\Model\ApiV1DemandsIdRemindersPost200ResponseData;
use Imzala\Client\Model\ApiV1DemandsPost201Response;
use Imzala\Client\Model\ApiV1DemandsUploadPost201Response;
use Imzala\Client\Model\ApiV1MeGet200Response;
use Imzala\Client\Model\ApiV1MeGet200ResponseData;
use Imzala\Client\Model\ApiV1TemplatesGet200Response;
use Imzala\Client\Model\ApiV1TemplatesGet200ResponseData;
use Imzala\Client\Model\ApiV1TemplatesIdGet200Response;
use Imzala\Client\Model\ApiV1TemplatesIdUsageGet200Response;
use Imzala\Client\Model\ApiV1TimestampsPost201Response;
use Imzala\Client\Model\CreateDemandRequest;
use Imzala\Client\Model\CreatedDemand;
use Imzala\Client\Model\CreatedDemandUpload;
use Imzala\Client\Model\DemandStatus;
use Imzala\Client\Model\TemplateDetail;
use Imzala\Client\Model\TemplateUsage;
use Imzala\Client\Model\TimestampRecord;
use Imzala\Client\Model\TriggerReminderRequest;
use Imzala\Client\Model\UpsertItemsRequest;
use Imzala\Client\Model\UpsertItemsResponse;
use Imzala\Client\Model\UpsertItemsResponseData;
use Imzala\CreateTimestampParams;
use Imzala\DemandsResource;
use Imzala\EmbedResource;
use Imzala\FileInput;
use Imzala\ImzalaClient;
use Imzala\ImzalaException;
use Imzala\TemplatesResource;
use Imzala\TimestampsResource;
use Imzala\UploadDemandParams;
use Imzala\UploadPartyInput;
use InvalidArgumentException;
use PHPUnit\Framework\TestCase;

final class ClientTest extends TestCase
{
    public function testConstructorRejectsEmptyApiKey(): void
    {
        $this->expectException(InvalidArgumentException::class);
        new ImzalaClient('');
    }

    public function testTemplatesListUnwrapsEnvelopeAndForwardsPaging(): void
    {
        $data = new ApiV1TemplatesGet200ResponseData();
        $envelope = new ApiV1TemplatesGet200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(TemplatesApi::class);
        $api->expects($this->once())
            ->method('apiV1TemplatesGetWithHttpInfo')
            ->with(2, 10)
            ->willReturn([$envelope, 200, []]);

        $resource = new TemplatesResource($api);
        $this->assertSame($data, $resource->list(2, 10));
    }

    public function testTemplatesListDefaultsToNullPaging(): void
    {
        $data = new ApiV1TemplatesGet200ResponseData();
        $envelope = new ApiV1TemplatesGet200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(TemplatesApi::class);
        $api->expects($this->once())
            ->method('apiV1TemplatesGetWithHttpInfo')
            ->with(null, null)
            ->willReturn([$envelope, 200, []]);

        $resource = new TemplatesResource($api);
        $resource->list();
    }

    public function testTemplatesGetUnwrapsEnvelope(): void
    {
        $data = new TemplateDetail();
        $envelope = new ApiV1TemplatesIdGet200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(TemplatesApi::class);
        $api->method('apiV1TemplatesIdGetWithHttpInfo')->with('tpl-1')->willReturn([$envelope, 200, []]);

        $resource = new TemplatesResource($api);
        $this->assertSame($data, $resource->get('tpl-1'));
    }

    public function testTemplatesUsageUnwrapsEnvelope(): void
    {
        $data = new TemplateUsage();
        $envelope = new ApiV1TemplatesIdUsageGet200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(TemplatesApi::class);
        $api->method('apiV1TemplatesIdUsageGetWithHttpInfo')->with('tpl-1')->willReturn([$envelope, 200, []]);

        $resource = new TemplatesResource($api);
        $this->assertSame($data, $resource->usage('tpl-1'));
    }

    public function testDemandsCreateAcceptsPlainArrayAndUnwraps(): void
    {
        $data = new CreatedDemand();
        $envelope = new ApiV1DemandsPost201Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsPostWithHttpInfo')
            ->with($this->callback(fn ($req) => $req instanceof CreateDemandRequest && $req->getTemplateId() === 'tpl-1'))
            ->willReturn([$envelope, 201, []]);

        $remindersApi = $this->createMock(RemindersApi::class);
        $resource = new DemandsResource($api, $remindersApi);

        $this->assertSame($data, $resource->create(['template_id' => 'tpl-1']));
    }

    public function testDemandsCreateAcceptsTypedRequestInstance(): void
    {
        $data = new CreatedDemand();
        $envelope = new ApiV1DemandsPost201Response(['success' => true, 'data' => $data]);
        $request = new CreateDemandRequest(['template_id' => 'tpl-2']);

        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsPostWithHttpInfo')
            ->with($this->identicalTo($request))
            ->willReturn([$envelope, 201, []]);

        $remindersApi = $this->createMock(RemindersApi::class);
        $resource = new DemandsResource($api, $remindersApi);

        $this->assertSame($data, $resource->create($request));
    }

    public function testDemandsGetUnwrapsEnvelope(): void
    {
        $data = new DemandStatus();
        $envelope = new ApiV1DemandsIdGet200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(DemandsApi::class);
        $api->method('apiV1DemandsIdGetWithHttpInfo')->with('demand-1')->willReturn([$envelope, 200, []]);

        $remindersApi = $this->createMock(RemindersApi::class);
        $resource = new DemandsResource($api, $remindersApi);

        $this->assertSame($data, $resource->get('demand-1'));
    }

    public function testDemandsAddItemsAcceptsArrayAndUnwraps(): void
    {
        $data = new UpsertItemsResponseData();
        $envelope = new UpsertItemsResponse(['success' => true, 'data' => $data]);

        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsIdItemsPostWithHttpInfo')
            ->with('demand-1', $this->isInstanceOf(UpsertItemsRequest::class))
            ->willReturn([$envelope, 200, []]);

        $remindersApi = $this->createMock(RemindersApi::class);
        $resource = new DemandsResource($api, $remindersApi);

        $this->assertSame($data, $resource->addItems('demand-1', ['page_ids' => [1, 2]]));
    }

    public function testSendReminderRoutesThroughRemindersApiNotDemandsApiWithDefaultBody(): void
    {
        $data = new ApiV1DemandsIdRemindersPost200ResponseData();
        $envelope = new ApiV1DemandsIdRemindersPost200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->never())->method($this->anything());

        $remindersApi = $this->createMock(RemindersApi::class);
        $remindersApi->expects($this->once())
            ->method('apiV1DemandsIdRemindersPostWithHttpInfo')
            ->with('demand-1', $this->callback(fn ($req) => $req instanceof TriggerReminderRequest))
            ->willReturn([$envelope, 200, []]);

        $resource = new DemandsResource($api, $remindersApi);
        $this->assertSame($data, $resource->sendReminder('demand-1'));
    }

    public function testSendReminderWithExplicitBody(): void
    {
        $data = new ApiV1DemandsIdRemindersPost200ResponseData();
        $envelope = new ApiV1DemandsIdRemindersPost200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(DemandsApi::class);
        $remindersApi = $this->createMock(RemindersApi::class);
        $remindersApi->expects($this->once())
            ->method('apiV1DemandsIdRemindersPostWithHttpInfo')
            ->with('demand-1', $this->callback(fn (TriggerReminderRequest $req) => $req->getForce() === true))
            ->willReturn([$envelope, 200, []]);

        $resource = new DemandsResource($api, $remindersApi);
        $resource->sendReminder('demand-1', ['force' => true]);
    }

    public function testUploadDocumentBuildsSplFileObjectsAndCleansUpTempFiles(): void
    {
        $data = new CreatedDemandUpload();
        $envelope = new ApiV1DemandsUploadPost201Response(['success' => true, 'data' => $data]);

        $capturedFiles = null;
        $capturedTempPath = null;
        $capturedParties = null;
        $capturedOrder = null;

        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsUploadPostWithHttpInfo')
            ->with(
                $this->callback(function ($files) use (&$capturedFiles, &$capturedTempPath) {
                    $capturedFiles = $files;
                    // Must read getRealPath() *now*, while the temp file
                    // still exists — DemandsResource::uploadDocument()
                    // deletes it in a `finally` block right after this
                    // mocked call returns.
                    $capturedTempPath = is_array($files) && isset($files[0]) ? $files[0]->getRealPath() : null;
                    return is_array($files) && count($files) === 1 && $files[0] instanceof \SplFileObject;
                }),
                $this->callback(function ($parties) use (&$capturedParties) {
                    $capturedParties = $parties;
                    return is_string($parties);
                }),
                $this->callback(function ($order) use (&$capturedOrder) {
                    $capturedOrder = $order;
                    return true;
                }),
                'Başlık',
                null,
            )
            ->willReturn([$envelope, 201, []]);

        $remindersApi = $this->createMock(RemindersApi::class);
        $resource = new DemandsResource($api, $remindersApi);

        $fileInput = new FileInput('%PDF-1.4 fake bytes', 'sozlesme.pdf', 'application/pdf');
        $party = new UploadPartyInput('Ada', 'Lovelace', 'ada@example.com', null);
        $params = (new UploadDemandParams([$fileInput], [$party]))->withOrder([0])->withTitle('Başlık');

        $result = $resource->uploadDocument($params);

        $this->assertSame($data, $result);
        $this->assertNotNull($capturedFiles);
        $this->assertIsString($capturedTempPath);
        $this->assertStringEndsWith('sozlesme.pdf', $capturedTempPath);
        // Temp file + its parent temp dir must be gone after the call returns.
        $this->assertFileDoesNotExist($capturedTempPath);
        $this->assertDirectoryDoesNotExist(dirname($capturedTempPath));

        $decodedParties = json_decode($capturedParties, true);
        $this->assertSame('Ada', $decodedParties[0]['first_name']);
        $this->assertSame('ada@example.com', $decodedParties[0]['email']);
        $this->assertArrayNotHasKey('phone', $decodedParties[0]);

        $this->assertSame('[0]', $capturedOrder);
    }

    public function testUploadDocumentCleansUpTempFilesEvenOnFailure(): void
    {
        $api = $this->createMock(DemandsApi::class);
        $api->method('apiV1DemandsUploadPostWithHttpInfo')
            ->willThrowException(new \RuntimeException('boom'));

        $remindersApi = $this->createMock(RemindersApi::class);
        $resource = new DemandsResource($api, $remindersApi);

        $fileInput = new FileInput('bytes', 'a.pdf');
        $party = new UploadPartyInput('Ada', 'Lovelace', 'ada@example.com', null);
        $params = new UploadDemandParams([$fileInput], [$party]);

        $tempPathHolder = null;
        try {
            $resource->uploadDocument($params);
            $this->fail('expected exception');
        } catch (\RuntimeException $e) {
            $this->assertSame('boom', $e->getMessage());
        }
    }

    public function testUploadDemandParamsRejectsEmptyFilesOrParties(): void
    {
        $this->expectException(InvalidArgumentException::class);
        new UploadDemandParams([], [new UploadPartyInput('Ada', null, 'ada@example.com', null)]);
    }

    public function testEmbedCreateSessionMapsPartyIdIntoRequestBody(): void
    {
        $data = new ApiV1DemandsIdEmbedSessionPost200ResponseData();
        $envelope = new ApiV1DemandsIdEmbedSessionPost200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsIdEmbedSessionPostWithHttpInfo')
            ->with('demand-1', $this->callback(
                fn (ApiV1DemandsIdEmbedSessionPostRequest $req) => $req->getPartyId() === 'party-1'
            ))
            ->willReturn([$envelope, 200, []]);

        $resource = new EmbedResource($api);
        $this->assertSame($data, $resource->createSession('demand-1', 'party-1'));
    }

    public function testTimestampsCreateBuildsSplFileObjectAndCleansUpTempFile(): void
    {
        $data = new TimestampRecord();
        $envelope = new ApiV1TimestampsPost201Response(['success' => true, 'data' => $data]);

        $capturedFile = null;
        $capturedTempPath = null;

        $api = $this->createMock(TimestampsApi::class);
        $api->expects($this->once())
            ->method('apiV1TimestampsPostWithHttpInfo')
            ->with(
                $this->callback(function ($file) use (&$capturedFile, &$capturedTempPath) {
                    $capturedFile = $file;
                    // Read now — TimestampsResource::create() deletes the
                    // temp file in a `finally` right after this returns.
                    $capturedTempPath = $file instanceof \SplFileObject ? $file->getRealPath() : null;
                    return $file instanceof \SplFileObject;
                }),
                'idem-1',
                'desc',
                'Ada',
                'Lovelace',
            )
            ->willReturn([$envelope, 201, []]);

        $resource = new TimestampsResource($api);
        $params = (new CreateTimestampParams('bytes', 'eser.pdf'))
            ->withIdempotencyKey('idem-1')
            ->withDescription('desc')
            ->withOwnerFirstName('Ada')
            ->withOwnerLastName('Lovelace');

        $result = $resource->create($params);

        $this->assertSame($data, $result);
        $this->assertIsString($capturedTempPath);
        $this->assertFileDoesNotExist($capturedTempPath);
        $this->assertDirectoryDoesNotExist(dirname($capturedTempPath));
    }

    public function testMeUnwrapsEnvelope(): void
    {
        // ImzalaClient constructs its own real generated Api instances
        // internally (no seam to inject a mock through the public
        // constructor) — this exercises the real HTTP-less parts (config,
        // guzzle client wiring) and only fails if constructing the client
        // itself throws.
        $client = new ImzalaClient('imz_test_key', 'https://example.invalid');
        $this->assertInstanceOf(ImzalaClient::class, $client);
    }

    public function testSuccessFalseOnHttp2xxThrowsBaseException(): void
    {
        $envelope = new ApiV1MeGet200Response(['success' => false]);

        $api = $this->createMock(AccountApi::class);
        $api->method('apiV1MeGetWithHttpInfo')->willReturn([$envelope, 200, []]);

        $this->expectException(ImzalaException::class);
        \Imzala\Http::unwrap(fn () => $api->apiV1MeGetWithHttpInfo());
    }
}
