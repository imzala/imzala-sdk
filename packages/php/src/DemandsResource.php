<?php

declare(strict_types=1);

namespace Imzala;

use Imzala\Client\Api\DemandsApi;
use Imzala\Client\Api\RemindersApi;
use Imzala\Client\Model\ApiV1DemandsIdRemindersPost200ResponseData;
use Imzala\Client\Model\CreateDemandRequest;
use Imzala\Client\Model\CreatedDemand;
use Imzala\Client\Model\CreatedDemandUpload;
use Imzala\Client\Model\DemandStatus;
use Imzala\Client\Model\TriggerReminderRequest;
use Imzala\Client\Model\UpsertItemsRequest;
use Imzala\Client\Model\UpsertItemsResponseData;
use SplFileObject;

/**
 * {@code $imzala->demands()}. Backed by both the vendored generated
 * {@see DemandsApi} and {@see RemindersApi} — see {@see self::sendReminder}.
 */
final class DemandsResource
{
    public function __construct(
        private readonly DemandsApi $api,
        private readonly RemindersApi $remindersApi,
        private readonly RetryConfig $retryConfig,
    ) {
    }

    /**
     * Creates a new demand (contract) from a template.
     *
     * @param CreateDemandRequest|array<string, mixed> $body a generated
     *     {@see CreateDemandRequest} instance, or a plain associative
     *     array with the same (snake_case) keys — e.g. {@code
     *     ['template_id' => $id, 'party_mapping' => [...]]}
     */
    public function create(CreateDemandRequest|array $body): CreatedDemand
    {
        $request = $body instanceof CreateDemandRequest ? $body : new CreateDemandRequest($body);
        return Http::unwrap(fn () => $this->api->apiV1DemandsPostWithHttpInfo($request));
    }

    /** Returns a demand's status + per-party signing progress. GET — safe to auto-retry. */
    public function get(string $id): DemandStatus
    {
        return Http::unwrapRetryableGet(fn () => $this->api->apiV1DemandsIdGetWithHttpInfo($id), $this->retryConfig);
    }

    /**
     * Places (replaces) signature/form fields on a demand's pages. See
     * {@see UpsertItemsRequest}'s {@code page_ids} for full-replace vs
     * per-page-replace semantics.
     *
     * @param UpsertItemsRequest|array<string, mixed> $body
     */
    public function addItems(string $id, UpsertItemsRequest|array $body): UpsertItemsResponseData
    {
        $request = $body instanceof UpsertItemsRequest ? $body : new UpsertItemsRequest($body);
        return Http::unwrap(fn () => $this->api->apiV1DemandsIdItemsPostWithHttpInfo($id, $request));
    }

    /**
     * Creates a demand directly from an uploaded document (no template) —
     * a single PDF/DOC/DOCX/ODT/RTF/TXT, or 1-20 images merged into one
     * PDF.
     *
     * <p>See {@see FileInput} for why this writes each file to a
     * throwaway temp file — the vendored generated client's multipart
     * layer requires a real path on disk. Temp files (and their parent
     * temp directories) are always deleted before this method returns,
     * success or failure.
     */
    public function uploadDocument(UploadDemandParams $params): CreatedDemandUpload
    {
        /** @var SplFileObject[] $splFiles */
        $splFiles = [];
        $tempPaths = [];

        try {
            foreach ($params->getFiles() as $fileInput) {
                $splFile = $fileInput->toSplFileObject();
                $splFiles[] = $splFile;
                $tempPaths[] = $splFile->getRealPath();
            }

            $partiesJson = (string) json_encode(array_map(
                static fn (UploadPartyInput $party) => $party->toArray(),
                $params->getParties()
            ));
            $orderJson = $params->getOrder() !== null ? (string) json_encode($params->getOrder()) : null;

            return Http::unwrap(fn () => $this->api->apiV1DemandsUploadPostWithHttpInfo(
                $splFiles,
                $partiesJson,
                $orderJson,
                $params->getTitle(),
                $params->getDescription(),
            ));
        } finally {
            unset($splFiles);
            foreach ($tempPaths as $tempPath) {
                if (is_string($tempPath)) {
                    FileInput::cleanupTempPath($tempPath);
                }
            }
        }
    }

    /**
     * Triggers an immediate SMS/email reminder to a demand's unsigned
     * parties, with default options (equivalent to {@code {}}).
     */
    public function sendReminder(string $id, TriggerReminderRequest|array|null $body = null): ApiV1DemandsIdRemindersPost200ResponseData
    {
        $request = match (true) {
            $body instanceof TriggerReminderRequest => $body,
            is_array($body) => new TriggerReminderRequest($body),
            default => new TriggerReminderRequest(),
        };

        // Routes through the vendored generated RemindersApi, not
        // DemandsApi — the OpenAPI spec groups `POST
        // /api/v1/demands/{id}/reminders` under a `Reminders` tag even
        // though the route lives under `demands`. Same gotcha B1 (TS),
        // B2 (Python), B3 (C#), and B5 (Java) flagged for their
        // generators.
        return Http::unwrap(fn () => $this->remindersApi->apiV1DemandsIdRemindersPostWithHttpInfo($id, $request));
    }
}
