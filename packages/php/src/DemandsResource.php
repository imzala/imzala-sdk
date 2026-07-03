<?php

declare(strict_types=1);

namespace Imzala;

use Imzala\Client\Api\DemandsApi;
use Imzala\Client\Api\RemindersApi;
use Imzala\Client\Model\ApiV1DemandsGet200ResponseData;
use Imzala\Client\Model\ApiV1DemandsIdCancelPost200ResponseData;
use Imzala\Client\Model\ApiV1DemandsIdCancelPostRequest;
use Imzala\Client\Model\ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData;
use Imzala\Client\Model\ApiV1DemandsIdRemindersPost200ResponseData;
use Imzala\Client\Model\ApiV1DemandsIdTimelineGet200ResponseData;
use Imzala\Client\Model\ApiV1TemplatesIdDelete200ResponseData;
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

    /**
     * Lists your demands — <b>counts-only</b> (id/title/status/timestamps +
     * {@code parties_total}/{@code parties_signed}, with NO party
     * names/emails/phones). Filter by status/date/template, paginate with
     * {@code $page}/{@code $limit}. GET — safe to auto-retry. For per-party
     * detail use {@see self::get()}.
     *
     * @param string|null $status     filter by demand status (DRAFT / PENDING / COMPLETED / CANCELLED / EXPIRED)
     * @param string|null $q          title search
     * @param string|null $from       ISO date (YYYY-MM-DD) lower bound on creation
     * @param string|null $to         ISO date (YYYY-MM-DD) upper bound on creation
     * @param string|null $templateId only demands created from this template
     * @param string|null $sort       {@code field:direction}, e.g. {@code createdAt:desc}
     */
    public function list(
        ?string $status = null,
        ?string $q = null,
        ?string $from = null,
        ?string $to = null,
        ?string $templateId = null,
        ?int $page = null,
        ?int $limit = null,
        ?string $sort = null,
    ): ApiV1DemandsGet200ResponseData {
        return Http::unwrapRetryableGet(
            fn () => $this->api->apiV1DemandsGetWithHttpInfo($status, $q, $from, $to, $templateId, $page, $limit, $sort),
            $this->retryConfig,
        );
    }

    /**
     * Downloads the signed contract PDF (only once {@code status ===
     * 'COMPLETED'}). Returns the raw bytes as a PHP {@code string} (PHP models
     * binary as a byte string) — write it to disk with {@see file_put_contents()}
     * or stream it on. Requires the API key's owner to own the demand. GET.
     */
    public function getPdf(string $id): string
    {
        return Http::unwrapBinary(fn () => $this->api->apiV1DemandsIdPdfGetWithHttpInfo($id));
    }

    /**
     * Downloads the completion certificate (PAdES B-T sealed audit document)
     * as raw bytes ({@code string}). Only produced for {@code COMPLETED}
     * demands. Pass {@code $lang = 'en'} for English. GET.
     */
    public function getCertificate(string $id, ?string $lang = null): string
    {
        return Http::unwrapBinary(fn () => $this->api->apiV1DemandsIdCertificateGetWithHttpInfo($id, $lang));
    }

    /**
     * Returns the signing audit trail (view/sign/reject events). PII-masked:
     * {@code ip_masked} (last octet hidden), actor name+email masked, no raw
     * IP/device. GET — safe to auto-retry.
     */
    public function getTimeline(string $id): ApiV1DemandsIdTimelineGet200ResponseData
    {
        return Http::unwrapRetryableGet(
            fn () => $this->api->apiV1DemandsIdTimelineGetWithHttpInfo($id),
            $this->retryConfig,
        );
    }

    /**
     * Cancels (voids) a pending demand — sets it to {@code CANCELLED} and
     * stops any scheduled reminders. A {@code COMPLETED} (or already-cancelled)
     * demand can't be cancelled (throws). POST — never auto-retried.
     *
     * @param ApiV1DemandsIdCancelPostRequest|array<string, mixed>|null $body a
     *     generated request instance, or a plain associative array — e.g.
     *     {@code ['reason' => 'vazgeçildi']}. Defaults to no reason.
     */
    public function cancel(string $id, ApiV1DemandsIdCancelPostRequest|array|null $body = null): ApiV1DemandsIdCancelPost200ResponseData
    {
        $request = match (true) {
            $body instanceof ApiV1DemandsIdCancelPostRequest => $body,
            is_array($body) => new ApiV1DemandsIdCancelPostRequest($body),
            default => new ApiV1DemandsIdCancelPostRequest(),
        };

        return Http::unwrap(fn () => $this->api->apiV1DemandsIdCancelPostWithHttpInfo($id, $request));
    }

    /**
     * Re-sends the signing invitation to a single party (by {@code $partyId}
     * from the demand's create/get response). Can't resend to a party who has
     * already signed or declined, or one whose turn hasn't come in ordered
     * signing (throws). POST — never auto-retried.
     */
    public function resendParty(string $id, string $partyId): ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData
    {
        return Http::unwrap(fn () => $this->api->apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo($id, $partyId));
    }

    /**
     * Deletes a demand and all its data. Only NON-completed demands can be
     * deleted via the API — a {@code COMPLETED} demand (signed document + audit
     * trail) returns 409 and must be removed from the dashboard. DELETE —
     * never auto-retried.
     */
    public function delete(string $id): ApiV1TemplatesIdDelete200ResponseData
    {
        return Http::unwrap(fn () => $this->api->apiV1DemandsIdDeleteWithHttpInfo($id));
    }
}
