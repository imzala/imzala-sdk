<?php

declare(strict_types=1);

namespace Imzala;

use Imzala\Client\Api\DemandsApi;
use Imzala\Client\Api\TemplatesApi;
use Imzala\Client\Model\ApiV1FieldTemplatesGet200ResponseData;
use Imzala\Client\Model\FieldLayoutPreview;
use Imzala\Client\Model\FieldTemplateDetail;
use SplFileObject;

/**
 * {@code $imzala->fieldTemplates()}. A field template is separate from a
 * contract template: it describes where fields land on an uploaded PDF,
 * located by anchor text.
 */
final class FieldTemplatesResource
{
    public function __construct(
        private readonly TemplatesApi $templatesApi,
        private readonly DemandsApi $demandsApi,
        private readonly RetryConfig $retryConfig,
    ) {
    }

    /**
     * Lists your field templates (one page). GET, safe to auto-retry.
     *
     * @param int|null $limit 1 to 100; larger values are clamped, not rejected
     */
    public function list(?int $page = null, ?int $limit = null): ApiV1FieldTemplatesGet200ResponseData
    {
        return Http::unwrapRetryableGet(
            fn () => $this->templatesApi->apiV1FieldTemplatesGetWithHttpInfo(page: $page, limit: $limit),
            $this->retryConfig,
        );
    }

    /**
     * Returns a field template's roles and field counts. A contract template
     * id throws {@code TEMPLATE_NOT_FOUND}: the two are different kinds. GET,
     * safe to auto-retry.
     */
    public function get(string $id): FieldTemplateDetail
    {
        return Http::unwrapRetryableGet(fn () => $this->templatesApi->apiV1FieldTemplatesIdGetWithHttpInfo($id), $this->retryConfig);
    }

    /**
     * Dry run: tries the field template's layout on a PDF without creating
     * anything or spending credit, and reports resolved fields and unresolved
     * anchors separately. The cheapest way to avoid surprises before {@see
     * DemandsResource::uploadDocument()} with {@see
     * UploadDemandParams::withFieldTemplateId()}. Rate limited per user. POST,
     * but side-effect free; never auto-retried.
     *
     * @param FileInput[] $files exactly one PDF (the field is a list on the wire)
     * @param string|null $onAnchorMiss {@code 'block'} or {@code 'drop'}; see {@see UploadDemandParams::withOnAnchorMiss()}
     */
    public function previewLayout(string $id, array $files, ?string $onAnchorMiss = null): FieldLayoutPreview
    {
        /** @var SplFileObject[] $splFiles */
        $splFiles = [];
        $tempPaths = [];

        try {
            foreach ($files as $fileInput) {
                $splFile = $fileInput->toSplFileObject();
                $splFiles[] = $splFile;
                $tempPaths[] = $splFile->getRealPath();
            }

            return Http::unwrap(fn () => $this->demandsApi->apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo(
                id: $id,
                files: $splFiles,
                on_anchor_miss: $onAnchorMiss,
            ));
        } finally {
            unset($splFiles, $splFile);
            foreach ($tempPaths as $tempPath) {
                if (is_string($tempPath)) {
                    FileInput::cleanupTempPath($tempPath);
                }
            }
        }
    }
}
