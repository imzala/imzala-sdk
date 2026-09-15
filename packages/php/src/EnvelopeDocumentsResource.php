<?php

declare(strict_types=1);

namespace Imzala;

use Imzala\Client\Api\DemandsApi;
use Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest;
use Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsDocIdPatchRequest;
use Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsGet200ResponseData;
use Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsOrderPutRequest;
use Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPost201ResponseData;
use Imzala\Client\Model\ApiV1DemandsDemandIdDocumentsPostRequest;
use Imzala\Client\Model\ApiV1TemplatesIdDelete200ResponseData;
use Imzala\Client\ObjectSerializer;

/**
 * {@code $imzala->demands()->documents()}: the documents of a multi-document
 * envelope. While multi-document envelopes are not enabled for the account,
 * every method here throws {@code ENVELOPE_MULTI_DOC_DISABLED} (409); a list
 * call never returns an empty list in that case.
 *
 * <p>Document methods spend no credit; credit is charged on
 * {@see DemandsResource::dispatch()}.
 */
final class EnvelopeDocumentsResource
{
    public function __construct(
        private readonly DemandsApi $api,
        private readonly RetryConfig $retryConfig,
    ) {
    }

    /**
     * Lists the documents of an envelope. GET, safe to auto-retry.
     *
     * @param string|null $view {@code 'wizard'} returns the full shape ({@code assigned_party_ids}, {@code decision_count})
     */
    public function list(string $demandId, ?string $view = null): ApiV1DemandsDemandIdDocumentsGet200ResponseData
    {
        return Http::unwrapRetryableGet(
            fn () => $this->api->apiV1DemandsDemandIdDocumentsGetWithHttpInfo($demandId, $view),
            $this->retryConfig,
        );
    }

    /**
     * Adds a document without a file (metadata only); its order is assigned
     * automatically. No idempotency key, so never retried. POST.
     *
     * @param ApiV1DemandsDemandIdDocumentsPostRequest|array<string, mixed> $body
     *     {@code title} is required; {@code doc_kind}, {@code is_required} and
     *     {@code signature_required} are optional
     */
    public function create(string $demandId, ApiV1DemandsDemandIdDocumentsPostRequest|array $body): ApiV1DemandsDemandIdDocumentsPost201ResponseData
    {
        $request = $body instanceof ApiV1DemandsDemandIdDocumentsPostRequest ? $body : new ApiV1DemandsDemandIdDocumentsPostRequest($body);
        return Http::unwrap(fn () => $this->api->apiV1DemandsDemandIdDocumentsPostWithHttpInfo($demandId, $request));
    }

    /**
     * Uploads one file as one document. Call it again for each further
     * document.
     *
     * <p>{@code $idempotencyKey} is required and is sent as the
     * {@code idempotency_key} multipart field (not as a header). It is checked
     * before anything is sent: an empty key, or one with a character outside
     * printable ASCII, throws {@see ImzalaValidationException}. Because the
     * server keeps the key, one retry is made after a 429 (waiting at most 60
     * seconds; the file is sent again). If a document was already uploaded
     * with the same key, the server answers 409 {@code IDEMPOTENT_REPLAY}
     * with that document, and this method returns it as a normal result. Any
     * other 409 is thrown. No credit is spent.
     *
     * <p>The file is written to a throwaway temp file for the generated
     * client's multipart layer (see {@see FileInput}); it is deleted before
     * this method returns, success or failure.
     *
     * @param string|null $docKind CONTRACT, KVKK_NOTICE, KVKK_CONSENT, PREINFO, PRICE_LIST or OTHER; server default OTHER
     * @param bool|null $isRequired server default {@code true}
     */
    public function upload(
        string $demandId,
        FileInput $file,
        string $title,
        string $idempotencyKey,
        ?string $docKind = null,
        ?bool $isRequired = null,
    ): ApiV1DemandsDemandIdDocumentsPost201ResponseData {
        Http::assertIdempotencyKey($idempotencyKey, 'idempotencyKey');

        $splFile = $file->toSplFileObject();
        $tempPath = $splFile->getRealPath();
        try {
            return Http::unwrapIdempotentWrite(
                // Named arguments: the generated signature has defaults for
                // doc_kind and is_required; null skips the form field.
                fn () => $this->api->apiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo(
                    demand_id: $demandId,
                    file: $splFile,
                    idempotency_key: $idempotencyKey,
                    title: $title,
                    doc_kind: $docKind,
                    is_required: $isRequired === null ? null : ($isRequired ? 'true' : 'false'),
                ),
                $idempotencyKey,
                $this->retryConfig->retryBaseDelayMs,
            );
        } catch (ImzalaException $e) {
            $replayed = self::replayedDocument($e);
            if ($replayed !== null) {
                return $replayed;
            }
            throw $e;
        } finally {
            unset($splFile);
            if (is_string($tempPath)) {
                FileInput::cleanupTempPath($tempPath);
            }
        }
    }

    /**
     * Updates only the fields you send ({@code title}, {@code doc_kind},
     * {@code is_required}, {@code signature_required}). Never retried. PATCH.
     *
     * @param ApiV1DemandsDemandIdDocumentsDocIdPatchRequest|array<string, mixed> $body
     */
    public function update(
        string $demandId,
        string $docId,
        ApiV1DemandsDemandIdDocumentsDocIdPatchRequest|array $body,
    ): ApiV1DemandsDemandIdDocumentsPost201ResponseData {
        $request = $body instanceof ApiV1DemandsDemandIdDocumentsDocIdPatchRequest ? $body : new ApiV1DemandsDemandIdDocumentsDocIdPatchRequest($body);
        return Http::unwrap(fn () => $this->api->apiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo($demandId, $docId, $request));
    }

    /**
     * Deletes a document; the last document of an envelope cannot be deleted
     * ({@code CANNOT_DELETE_LAST_DOCUMENT}). Never retried. DELETE.
     */
    public function delete(string $demandId, string $docId): ApiV1TemplatesIdDelete200ResponseData
    {
        return Http::unwrap(fn () => $this->api->apiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo($demandId, $docId));
    }

    /**
     * Sets the order of all documents. {@code $documentIds} must contain
     * exactly the envelope's documents ({@code ORDER_SET_MISMATCH}). Never
     * retried. PUT.
     *
     * @param list<string> $documentIds
     */
    public function reorder(string $demandId, array $documentIds): ApiV1DemandsDemandIdDocumentsGet200ResponseData
    {
        $request = new ApiV1DemandsDemandIdDocumentsOrderPutRequest(['document_ids' => $documentIds]);
        return Http::unwrap(fn () => $this->api->apiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo($demandId, $request));
    }

    /**
     * Replaces the set of parties assigned to a document. Never retried. PUT.
     *
     * @param list<string> $partyIds
     */
    public function setAssignments(string $demandId, string $docId, array $partyIds): ApiV1DemandsDemandIdDocumentsPost201ResponseData
    {
        $request = new ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest(['party_ids' => $partyIds]);
        return Http::unwrap(fn () => $this->api->apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo($demandId, $docId, $request));
    }

    /**
     * A 409 {@code IDEMPOTENT_REPLAY} carries the earlier document under
     * {@code data.document}. The spec declares no schema for that status, so
     * the generated client throws and {@see ImzalaException::getBody()} holds
     * the raw JSON; it is read back into the same model a 200 would produce.
     */
    private static function replayedDocument(ImzalaException $e): ?ApiV1DemandsDemandIdDocumentsPost201ResponseData
    {
        if ($e->getStatusCode() !== 409 || $e->getErrorCode() !== 'IDEMPOTENT_REPLAY' || $e->getBody() === null) {
            return null;
        }
        $body = json_decode($e->getBody());
        if (!is_object($body) || !isset($body->data) || !is_object($body->data) || !isset($body->data->document)) {
            return null;
        }
        $data = ObjectSerializer::deserialize($body->data, '\\' . ApiV1DemandsDemandIdDocumentsPost201ResponseData::class);
        return $data instanceof ApiV1DemandsDemandIdDocumentsPost201ResponseData && $data->getDocument() !== null ? $data : null;
    }
}
