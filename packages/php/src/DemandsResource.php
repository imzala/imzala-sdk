<?php

declare(strict_types=1);

namespace Imzala;

use Imzala\Client\Api\DemandsApi;
use Imzala\Client\Api\RemindersApi;
use Imzala\Client\Model\ApiV1DemandsBulkPost200ResponseData;
use Imzala\Client\Model\ApiV1DemandsBulkPostRequest;
use Imzala\Client\Model\ApiV1DemandsDemandIdDispatchPost200ResponseData;
use Imzala\Client\Model\ApiV1DemandsDemandIdDispatchPostRequest;
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
    private readonly EnvelopeDocumentsResource $documents;

    public function __construct(
        private readonly DemandsApi $api,
        private readonly RemindersApi $remindersApi,
        private readonly RetryConfig $retryConfig,
    ) {
        $this->documents = new EnvelopeDocumentsResource($api, $retryConfig);
    }

    /** {@code $imzala->demands()->documents()->list($demandId)/create(...)/upload(...)/update(...)/delete(...)/reorder(...)/setAssignments(...)}: the documents of a multi-document envelope. */
    public function documents(): EnvelopeDocumentsResource
    {
        return $this->documents;
    }

    /**
     * Sends the demand for signing: reconciles credit, moves a {@code DRAFT}
     * to {@code PENDING} and sends invitations. This is where credit is
     * charged; the {@see self::documents()} methods charge nothing. Calling
     * it again for a demand that is already out charges nothing more
     * ({@code dispatched: false}). Throws for {@code DISPATCH_NO_PARTIES},
     * {@code DISPATCH_TOO_MANY}, {@code QES_NOT_SUPPORTED_MULTI_DOCUMENT},
     * {@code INSUFFICIENT_CREDITS} and others. No idempotency key, so never
     * retried, not even after a 429. POST.
     *
     * @param bool|string|null $sendInvitations narrows which channels
     *     invitations go out on. {@code null} (default) means the server
     *     default (invitations on). {@code false}, {@code 'false'}, {@code '0'},
     *     {@code 'off'}, {@code 'no'}, {@code 'hayir'}, {@code 'hayır'} send no
     *     invitations; {@code 'email'} and {@code 'sms'} limit the channel. It
     *     can only narrow the demand's own notification settings. An unknown
     *     value throws {@code INVALID_SEND_INVITATIONS}.
     */
    public function dispatch(string $id, bool|string|null $sendInvitations = null): ApiV1DemandsDemandIdDispatchPost200ResponseData
    {
        $request = new ApiV1DemandsDemandIdDispatchPostRequest(
            $sendInvitations === null ? [] : ['send_invitations' => $sendInvitations]
        );
        return Http::unwrap(fn () => $this->api->apiV1DemandsDemandIdDispatchPostWithHttpInfo($id, $request));
    }

    /**
     * Creates a new demand (contract) from a template.
     *
     * <p>Without {@code $idempotencyKey} this is a single attempt: a retried
     * create would produce a duplicate demand. With a key, a repeated request
     * does not create a second demand, so one retry is made after a 429. A
     * key reused with a different body throws {@code IDEMPOTENCY_KEY_REUSED}.
     *
     * <p>An {@code expiry_date} that is not a real calendar day throws
     * {@code INVALID_EXPIRY_DATE}.
     *
     * @param CreateDemandRequest|array<string, mixed> $body a generated
     *     {@see CreateDemandRequest} instance, or a plain associative
     *     array with the same (snake_case) keys — e.g. {@code
     *     ['template_id' => $id, 'party_mapping' => [...]]}
     * @param string|null $idempotencyKey your own reference for this request (e.g. an order number); sent as the Idempotency-Key header
     */
    public function create(CreateDemandRequest|array $body, ?string $idempotencyKey = null): CreatedDemand
    {
        $request = $body instanceof CreateDemandRequest ? $body : new CreateDemandRequest($body);
        return Http::unwrapIdempotentWrite(
            fn () => $this->api->apiV1DemandsPostWithHttpInfo(
                create_demand_request: $request,
                idempotency_key: $idempotencyKey,
            ),
            $idempotencyKey,
            $this->retryConfig->retryBaseDelayMs,
        );
    }

    /**
     * Creates up to 10 demands from one template in a single request. Rows
     * are created independently; check {@code failed} and each result's
     * {@code status}.
     *
     * <p>This endpoint has no idempotency key, so it is never retried: a
     * retried batch would create the demands again. Split larger lists into
     * batches of 10 yourself.
     *
     * @param ApiV1DemandsBulkPostRequest|array<string, mixed> $body a generated
     *     request instance, or a plain associative array with the same keys
     */
    public function createBulk(ApiV1DemandsBulkPostRequest|array $body): ApiV1DemandsBulkPost200ResponseData
    {
        $request = $body instanceof ApiV1DemandsBulkPostRequest ? $body : new ApiV1DemandsBulkPostRequest($body);
        return Http::unwrap(fn () => $this->api->apiV1DemandsBulkPostWithHttpInfo(api_v1_demands_bulk_post_request: $request));
    }

    /** Returns a demand's status + per-party signing progress. GET — safe to auto-retry. */
    public function get(string $id): DemandStatus
    {
        return Http::unwrapRetryableGet(fn () => $this->api->apiV1DemandsIdGetWithHttpInfo($id), $this->retryConfig);
    }

    /**
     * Places (replaces) signature/form fields on a demand's pages. See
     * {@see UpsertItemsRequest}'s {@code page_ids} for full-replace vs
     * per-page-replace semantics. Every item needs an integer {@code page_id}
     * ({@code PAGE_ID_REQUIRED}); an unknown {@code item_type} throws {@code
     * INVALID_ITEM_TYPE}.
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
     * <p>Without an idempotency key this is a single attempt. With {@see
     * UploadDemandParams::withIdempotencyKey()}, a repeated request does not
     * create a second demand, so one retry is made after a 429. Invitations
     * are not sent unless {@see UploadDemandParams::withSendInvitations()} is
     * set.
     *
     * <p>See {@see FileInput} for why this writes each file to a
     * throwaway temp file — the vendored generated client's multipart
     * layer requires a real path on disk. Temp files (and their parent
     * temp directories) are always deleted before this method returns,
     * success or failure.
     *
     * @throws \JsonException when a party or the order cannot be encoded (e.g. invalid UTF-8); nothing is sent
     */
    public function uploadDocument(UploadDemandParams $params): CreatedDemandUpload
    {
        // Encode before touching the filesystem: a failed encode must never
        // turn into an empty party list on the wire.
        $jsonFlags = JSON_THROW_ON_ERROR | JSON_UNESCAPED_UNICODE;
        $partiesJson = json_encode(array_map(
            static fn (UploadPartyInput $party) => $party->toArray(),
            $params->getParties()
        ), $jsonFlags);
        $orderJson = $params->getOrder() !== null ? json_encode($params->getOrder(), $jsonFlags) : null;

        /** @var SplFileObject[] $splFiles */
        $splFiles = [];
        $tempPaths = [];

        try {
            foreach ($params->getFiles() as $fileInput) {
                $splFile = $fileInput->toSplFileObject();
                $splFiles[] = $splFile;
                $tempPaths[] = $splFile->getRealPath();
            }

            // Named arguments: the generated signature inserts new optional
            // parameters between existing ones, so positional arguments would
            // silently shift values into the wrong slot.
            return Http::unwrapIdempotentWrite(
                fn () => $this->api->apiV1DemandsUploadPostWithHttpInfo(
                    files: $splFiles,
                    parties: $partiesJson,
                    idempotency_key: $params->getIdempotencyKey(),
                    order: $orderJson,
                    title: $params->getTitle(),
                    description: $params->getDescription(),
                    field_template_id: $params->getFieldTemplateId(),
                    force: $params->getForce() ? 'true' : null,
                    send_invitations: $params->getSendInvitations(),
                    on_anchor_miss: $params->getOnAnchorMiss(),
                ),
                $params->getIdempotencyKey(),
                $this->retryConfig->retryBaseDelayMs,
            );
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
     * parties, with default options (equivalent to {@code {}}). Independent
     * of the template/demand's scheduled reminder settings. Subject to a
     * 5-minute anti-spam window (429 {@code RATE_LIMITED}, override with
     * {@code ['force' => true]}) and a hard per-person cap of 3 reminders per
     * channel (not overridable). A draft, completed, cancelled or expired
     * demand throws 409 ({@code DEMAND_NOT_DISPATCHED}, {@code
     * DEMAND_NOT_DISPATCHABLE}, {@code DEMAND_EXPIRED}). POST, never
     * auto-retried (a retried call could double-send).
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
     * Downloads the PDF of one document in a multi-document envelope as raw
     * bytes ({@code string}). For the whole contract use {@see self::getPdf()}.
     * GET.
     */
    public function getDocumentPdf(string $id, string $documentId): string
    {
        return Http::unwrapBinary(fn () => $this->api->apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo($id, $documentId));
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
