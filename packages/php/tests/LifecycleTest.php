<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Imzala\Client\Api\DemandsApi;
use Imzala\Client\Api\RemindersApi;
use Imzala\Client\Api\TemplatesApi;
use Imzala\Client\Model\ApiV1DemandsGet200Response;
use Imzala\Client\Model\ApiV1DemandsGet200ResponseData;
use Imzala\Client\Model\ApiV1DemandsIdCancelPost200Response;
use Imzala\Client\Model\ApiV1DemandsIdCancelPost200ResponseData;
use Imzala\Client\Model\ApiV1DemandsIdCancelPostRequest;
use Imzala\Client\Model\ApiV1DemandsIdPartiesPartyIdResendPost200Response;
use Imzala\Client\Model\ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData;
use Imzala\Client\Model\ApiV1DemandsIdTimelineGet200Response;
use Imzala\Client\Model\ApiV1DemandsIdTimelineGet200ResponseData;
use Imzala\Client\Model\ApiV1TemplatesIdDelete200Response;
use Imzala\Client\Model\ApiV1TemplatesIdDelete200ResponseData;
use Imzala\Client\Model\ApiV1TemplatesIdPatch200Response;
use Imzala\Client\Model\ApiV1TemplatesIdPatch200ResponseData;
use Imzala\Client\Model\ApiV1TemplatesIdPatchRequest;
use Imzala\DemandsResource;
use Imzala\RetryConfig;
use Imzala\TemplatesResource;
use PHPUnit\Framework\TestCase;
use SplFileObject;

/**
 * Mirrors {@code packages/node/src/__tests__/lifecycle.test.ts} — the 9 new
 * v1 lifecycle facade methods added to {@see DemandsResource} and
 * {@see TemplatesResource}: wiring (correct generated {@code ...WithHttpInfo}
 * method + args), {@code {success, data}} envelope unwrap, and raw-bytes
 * handling for the two binary endpoints.
 */
final class LifecycleTest extends TestCase
{
    /** Retry is orthogonal to these tests — no retries expected/exercised. */
    private static function noRetry(): RetryConfig
    {
        return new RetryConfig(0, 0);
    }

    private function demands(DemandsApi $api): DemandsResource
    {
        return new DemandsResource($api, $this->createMock(RemindersApi::class), self::noRetry());
    }

    // ------------------------------------------------------------------ demands

    public function testListForwardsFiltersAndUnwrapsCountsOnlyData(): void
    {
        $data = new ApiV1DemandsGet200ResponseData();
        $envelope = new ApiV1DemandsGet200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsGetWithHttpInfo')
            ->with('PENDING', null, null, null, 't1', 1, 20, null)
            ->willReturn([$envelope, 200, []]);

        $result = $this->demands($api)->list(status: 'PENDING', templateId: 't1', page: 1, limit: 20);
        $this->assertSame($data, $result);
    }

    public function testGetTimelineUnwrapsMaskedEvents(): void
    {
        $data = new ApiV1DemandsIdTimelineGet200ResponseData();
        $envelope = new ApiV1DemandsIdTimelineGet200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsIdTimelineGetWithHttpInfo')
            ->with('d1')
            ->willReturn([$envelope, 200, []]);

        $this->assertSame($data, $this->demands($api)->getTimeline('d1'));
    }

    public function testCancelPostsReasonBodyAndUnwraps(): void
    {
        $data = new ApiV1DemandsIdCancelPost200ResponseData();
        $envelope = new ApiV1DemandsIdCancelPost200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsIdCancelPostWithHttpInfo')
            ->with(
                'd1',
                $this->callback(
                    fn ($req) => $req instanceof ApiV1DemandsIdCancelPostRequest && $req->getReason() === 'vazgeçildi'
                )
            )
            ->willReturn([$envelope, 200, []]);

        $this->assertSame($data, $this->demands($api)->cancel('d1', ['reason' => 'vazgeçildi']));
    }

    public function testCancelDefaultsToEmptyRequestBody(): void
    {
        $data = new ApiV1DemandsIdCancelPost200ResponseData();
        $envelope = new ApiV1DemandsIdCancelPost200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsIdCancelPostWithHttpInfo')
            ->with(
                'd1',
                $this->callback(
                    fn ($req) => $req instanceof ApiV1DemandsIdCancelPostRequest && $req->getReason() === null
                )
            )
            ->willReturn([$envelope, 200, []]);

        $this->assertSame($data, $this->demands($api)->cancel('d1'));
    }

    public function testResendPartyTargetsASingleParty(): void
    {
        $data = new ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData();
        $envelope = new ApiV1DemandsIdPartiesPartyIdResendPost200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo')
            ->with('d1', 'p9')
            ->willReturn([$envelope, 200, []]);

        $this->assertSame($data, $this->demands($api)->resendParty('d1', 'p9'));
    }

    public function testDeleteUnwrapsDeletionResult(): void
    {
        $data = new ApiV1TemplatesIdDelete200ResponseData();
        $envelope = new ApiV1TemplatesIdDelete200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsIdDeleteWithHttpInfo')
            ->with('d1')
            ->willReturn([$envelope, 200, []]);

        $this->assertSame($data, $this->demands($api)->delete('d1'));
    }

    public function testGetPdfReturnsRawBytesFromSplFileObject(): void
    {
        $tmpPath = tempnam(sys_get_temp_dir(), 'imzpdf');
        $this->assertIsString($tmpPath);
        file_put_contents($tmpPath, '%PDF-1.7 fake bytes');

        try {
            $splFile = new SplFileObject($tmpPath, 'r');

            $api = $this->createMock(DemandsApi::class);
            $api->expects($this->once())
                ->method('apiV1DemandsIdPdfGetWithHttpInfo')
                ->with('d1')
                ->willReturn([$splFile, 200, []]);

            $out = $this->demands($api)->getPdf('d1');

            $this->assertIsString($out);
            $this->assertStringContainsString('%PDF-1.7', $out);
        } finally {
            @unlink($tmpPath);
        }
    }

    public function testGetCertificateForwardsLangAndReturnsRawBytes(): void
    {
        // Exercises the plain-string binary branch (the generated client can
        // hand back either a SplFileObject or a raw string body).
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsIdCertificateGetWithHttpInfo')
            ->with('d1', 'en')
            ->willReturn(['%PDF cert bytes', 200, []]);

        $out = $this->demands($api)->getCertificate('d1', 'en');

        $this->assertSame('%PDF cert bytes', $out);
    }

    public function testGetCertificateWithoutLangPassesNull(): void
    {
        $api = $this->createMock(DemandsApi::class);
        $api->expects($this->once())
            ->method('apiV1DemandsIdCertificateGetWithHttpInfo')
            ->with('d1', null)
            ->willReturn(['%PDF cert', 200, []]);

        $this->assertSame('%PDF cert', $this->demands($api)->getCertificate('d1'));
    }

    // ---------------------------------------------------------------- templates

    public function testTemplateUpdatePatchesMetadataAndUnwraps(): void
    {
        $data = new ApiV1TemplatesIdPatch200ResponseData();
        $envelope = new ApiV1TemplatesIdPatch200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(TemplatesApi::class);
        $api->expects($this->once())
            ->method('apiV1TemplatesIdPatchWithHttpInfo')
            ->with(
                't1',
                $this->callback(
                    fn ($req) => $req instanceof ApiV1TemplatesIdPatchRequest && $req->getName() === 'Yeni Ad'
                )
            )
            ->willReturn([$envelope, 200, []]);

        $resource = new TemplatesResource($api, self::noRetry());
        $this->assertSame($data, $resource->update('t1', ['name' => 'Yeni Ad']));
    }

    public function testTemplateDeleteSoftDeletesAndUnwraps(): void
    {
        $data = new ApiV1TemplatesIdDelete200ResponseData();
        $envelope = new ApiV1TemplatesIdDelete200Response(['success' => true, 'data' => $data]);

        $api = $this->createMock(TemplatesApi::class);
        $api->expects($this->once())
            ->method('apiV1TemplatesIdDeleteWithHttpInfo')
            ->with('t1')
            ->willReturn([$envelope, 200, []]);

        $resource = new TemplatesResource($api, self::noRetry());
        $this->assertSame($data, $resource->delete('t1'));
    }
}
