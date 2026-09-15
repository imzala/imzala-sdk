using ImzalaApiClient.Api;
using ImzalaApiClient.Model;
using Newtonsoft.Json.Linq;

namespace ImzalaSdk;

/// <summary>
/// <c>imzala.Demands.Documents</c>: the documents of a multi-document envelope.
/// While multi-document envelopes are not enabled for the account, every method
/// here throws <c>ENVELOPE_MULTI_DOC_DISABLED</c> (409); a list call never
/// returns an empty list in that case.
///
/// Document methods spend no credit; credit is charged on
/// <see cref="DemandsResource.DispatchAsync(Guid, CancellationToken)"/>.
/// </summary>
public sealed class EnvelopeDocumentsResource
{
    private readonly IDemandsApi _api;
    private readonly RetryConfig _retry;

    internal EnvelopeDocumentsResource(IDemandsApi api, RetryConfig retry)
    {
        _api = api;
        _retry = retry;
    }

    /// <summary>Lists the documents of an envelope. GET, safe to auto-retry.</summary>
    /// <param name="demandId">The demand (envelope).</param>
    /// <param name="view"><c>"wizard"</c> returns the full shape (<c>assigned_party_ids</c>, <c>decision_count</c>); <c>null</c> for the default.</param>
    /// <param name="cancellationToken">Cancellation token.</param>
    public Task<ApiV1DemandsDemandIdDocumentsGet200ResponseData> ListAsync(Guid demandId, string? view = null, CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(
            () => _api.ApiV1DemandsDemandIdDocumentsGetAsync(demandId, view, cancellationToken),
            r => r.Success,
            r => r.Data,
            _retry,
            cancellationToken);

    /// <summary>
    /// Adds a document without a file (metadata only); its order is assigned
    /// automatically. <c>title</c> is required; <c>doc_kind</c>, <c>is_required</c>
    /// and <c>signature_required</c> are optional. No idempotency key, so never
    /// retried. POST.
    /// </summary>
    public Task<ApiV1DemandsDemandIdDocumentsPost201ResponseData> CreateAsync(Guid demandId, ApiV1DemandsDemandIdDocumentsPostRequest body, CancellationToken cancellationToken = default) =>
        Http.Unwrap(_api.ApiV1DemandsDemandIdDocumentsPostAsync(demandId, body, cancellationToken), r => r.Success, r => r.Data);

    /// <summary>
    /// Uploads one file as one document. Call it again for each further document.
    ///
    /// <see cref="UploadEnvelopeDocumentParams.IdempotencyKey"/> is required and is
    /// sent as the <c>idempotency_key</c> multipart field (not as a header). It is
    /// checked before anything is sent: a missing or empty key, or one with a
    /// character outside printable ASCII, throws <see cref="ImzalaValidationError"/>.
    /// Because the server keeps the key, one retry is made after a 429 (waiting
    /// Retry-After, at most 60 s; the file stream is rebuilt and sent again). If a
    /// document was already uploaded with the same key, the server answers 409
    /// <c>IDEMPOTENT_REPLAY</c> with that document, and this method returns it as a
    /// normal result. Any other 409 is thrown. No credit is spent. POST.
    /// </summary>
    public async Task<ApiV1DemandsDemandIdDocumentsPost201ResponseData> UploadAsync(Guid demandId, UploadEnvelopeDocumentParams request, CancellationToken cancellationToken = default)
    {
        Http.ValidateIdempotencyKey(request.IdempotencyKey, "IdempotencyKey");
        var isRequired = request.IsRequired is null ? null : (request.IsRequired.Value ? "true" : "false");

        try
        {
            return await Http.UnwrapIdempotentWrite(
                // Named arguments: docKind and isRequired share a type, so a
                // swapped positional argument would still compile. The file
                // stream is rebuilt on every attempt: a retry cannot resend a
                // stream the first attempt already read.
                () => _api.ApiV1DemandsDemandIdDocumentsUploadPostAsync(
                    demandId: demandId,
                    file: request.File.ToFileParameter(),
                    idempotencyKey: request.IdempotencyKey,
                    title: request.Title,
                    docKind: request.DocKind,
                    isRequired: isRequired,
                    cancellationToken: cancellationToken),
                r => r.Success,
                r => r.Data,
                request.IdempotencyKey,
                _retry,
                cancellationToken).ConfigureAwait(false);
        }
        catch (ImzalaError err)
        {
            var replayed = ReplayedDocument(err);
            if (replayed is not null)
            {
                return replayed;
            }
            throw;
        }
    }

    /// <summary>
    /// Updates only the fields you set. Unset booleans are serialised as
    /// <c>null</c>, which the server treats as "not sent" (unchanged), so a
    /// title-only update leaves <c>is_required</c> and <c>signature_required</c>
    /// as they are. Never retried. PATCH.
    /// </summary>
    public Task<ApiV1DemandsDemandIdDocumentsPost201ResponseData> UpdateAsync(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdPatchRequest body, CancellationToken cancellationToken = default) =>
        Http.Unwrap(_api.ApiV1DemandsDemandIdDocumentsDocIdPatchAsync(demandId, docId, body, cancellationToken), r => r.Success, r => r.Data);

    /// <summary>
    /// Deletes a document; the last document of an envelope cannot be deleted
    /// (<c>CANNOT_DELETE_LAST_DOCUMENT</c>). Never retried. DELETE.
    /// </summary>
    public Task<ApiV1TemplatesIdDelete200ResponseData> DeleteAsync(Guid demandId, Guid docId, CancellationToken cancellationToken = default) =>
        Http.Unwrap(_api.ApiV1DemandsDemandIdDocumentsDocIdDeleteAsync(demandId, docId, cancellationToken), r => r.Success, r => r.Data);

    /// <summary>
    /// Sets the order of all documents. <paramref name="documentIds"/> must contain
    /// exactly the envelope's documents (<c>ORDER_SET_MISMATCH</c>). Never retried. PUT.
    /// </summary>
    public Task<ApiV1DemandsDemandIdDocumentsGet200ResponseData> ReorderAsync(Guid demandId, IEnumerable<Guid> documentIds, CancellationToken cancellationToken = default)
    {
        var body = new ApiV1DemandsDemandIdDocumentsOrderPutRequest(documentIds.ToList());
        return Http.Unwrap(_api.ApiV1DemandsDemandIdDocumentsOrderPutAsync(demandId, body, cancellationToken), r => r.Success, r => r.Data);
    }

    /// <summary>Replaces the set of parties assigned to a document. Never retried. PUT.</summary>
    public Task<ApiV1DemandsDemandIdDocumentsPost201ResponseData> SetAssignmentsAsync(Guid demandId, Guid docId, IEnumerable<Guid> partyIds, CancellationToken cancellationToken = default)
    {
        var body = new ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest(partyIds.ToList());
        return Http.Unwrap(_api.ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutAsync(demandId, docId, body, cancellationToken), r => r.Success, r => r.Data);
    }

    /// <summary>
    /// A 409 <c>IDEMPOTENT_REPLAY</c> carries the earlier document under
    /// <c>data.document</c>. The spec declares no schema for that status, so the
    /// generated client throws and <see cref="ImzalaError.Body"/> holds the raw
    /// JSON; it is read back into the same model a 200 would produce.
    /// </summary>
    private static ApiV1DemandsDemandIdDocumentsPost201ResponseData? ReplayedDocument(ImzalaError err)
    {
        if (err.StatusCode != 409 || err.Code != "IDEMPOTENT_REPLAY" || string.IsNullOrEmpty(err.Body))
        {
            return null;
        }

        try
        {
            if (JObject.Parse(err.Body)["data"] is not JObject data || data["document"] is not JObject)
            {
                return null;
            }
            var replayed = data.ToObject<ApiV1DemandsDemandIdDocumentsPost201ResponseData>();
            return replayed?.Document is null ? null : replayed;
        }
        catch (Newtonsoft.Json.JsonException)
        {
            return null;
        }
    }
}
