using System.Text.Json;
using ImzalaApiClient.Api;
using ImzalaApiClient.Model;

namespace ImzalaSdk;

/// <summary>
/// <c>imzala.Demands.*</c>. Backed by both <c>ImzalaApiClient.Api.DemandsApi</c>
/// and <c>ImzalaApiClient.Api.RemindersApi</c> — see <see cref="SendReminderAsync"/>.
/// </summary>
public sealed class DemandsResource
{
    private readonly IDemandsApi _api;
    private readonly IRemindersApi _remindersApi;
    private readonly RetryConfig _retry;

    private static readonly JsonSerializerOptions PartiesJsonOptions = new()
    {
        DefaultIgnoreCondition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull,
        // Keep non-ASCII names readable (Ayşe, not Ay\u015fe) while still
        // escaping HTML-sensitive characters, unlike UnsafeRelaxedJsonEscaping.
        Encoder = System.Text.Encodings.Web.JavaScriptEncoder.Create(System.Text.Unicode.UnicodeRanges.All),
    };

    internal DemandsResource(IDemandsApi api, IRemindersApi remindersApi, RetryConfig retry)
    {
        _api = api;
        _remindersApi = remindersApi;
        _retry = retry;
        Documents = new EnvelopeDocumentsResource(api, retry);
    }

    /// <summary><c>imzala.Demands.Documents.ListAsync/CreateAsync/UploadAsync/UpdateAsync/DeleteAsync/ReorderAsync/SetAssignmentsAsync</c>: the documents of a multi-document envelope.</summary>
    public EnvelopeDocumentsResource Documents { get; }

    /// <summary>
    /// Sends the demand for signing with the server default for invitations
    /// (invitations on). See <see cref="DispatchAsync(Guid, string, CancellationToken)"/>.
    /// </summary>
    public Task<ApiV1DemandsDemandIdDispatchPost200ResponseData> DispatchAsync(Guid id, CancellationToken cancellationToken = default) =>
        DispatchAsync(id, (ApiV1DemandsDemandIdDispatchPostRequestSendInvitations?)null, cancellationToken);

    /// <summary>
    /// Sends the demand for signing; <c>false</c> sends no invitations. See
    /// <see cref="DispatchAsync(Guid, string, CancellationToken)"/>.
    /// </summary>
    public Task<ApiV1DemandsDemandIdDispatchPost200ResponseData> DispatchAsync(Guid id, bool sendInvitations, CancellationToken cancellationToken = default) =>
        DispatchAsync(id, new ApiV1DemandsDemandIdDispatchPostRequestSendInvitations(sendInvitations), cancellationToken);

    /// <summary>
    /// Sends the demand for signing: reconciles credit, moves a <c>DRAFT</c> to
    /// <c>PENDING</c> and sends invitations. This is where credit is charged; the
    /// <see cref="Documents"/> methods charge nothing. Calling it again for a demand
    /// that is already out charges nothing more (<c>Dispatched == false</c>). Throws
    /// for <c>DISPATCH_NO_PARTIES</c>, <c>DISPATCH_TOO_MANY</c>,
    /// <c>QES_NOT_SUPPORTED_MULTI_DOCUMENT</c>, <c>INSUFFICIENT_CREDITS</c> and
    /// others. No idempotency key, so never retried, not even after a 429. POST.
    /// </summary>
    /// <param name="id">The demand.</param>
    /// <param name="sendInvitations">
    /// Narrows which channels invitations go out on. <c>"false"</c>, <c>"0"</c>,
    /// <c>"off"</c>, <c>"no"</c>, <c>"hayir"</c>, <c>"hayır"</c> send no invitations;
    /// <c>"email"</c> and <c>"sms"</c> limit the channel; <c>"true"</c>, <c>"1"</c>,
    /// <c>"all"</c> keep them all. It can only narrow the demand's own notification
    /// settings. An unknown value throws <c>INVALID_SEND_INVITATIONS</c>.
    /// </param>
    /// <param name="cancellationToken">Cancellation token.</param>
    public Task<ApiV1DemandsDemandIdDispatchPost200ResponseData> DispatchAsync(Guid id, string sendInvitations, CancellationToken cancellationToken = default) =>
        DispatchAsync(id, new ApiV1DemandsDemandIdDispatchPostRequestSendInvitations(sendInvitations), cancellationToken);

    private Task<ApiV1DemandsDemandIdDispatchPost200ResponseData> DispatchAsync(Guid id, ApiV1DemandsDemandIdDispatchPostRequestSendInvitations? sendInvitations, CancellationToken cancellationToken)
    {
        // The generated oneOf wrapper serialises its actual instance (a bare
        // boolean or string); a null wrapper leaves the field out of the body.
        var body = new ApiV1DemandsDemandIdDispatchPostRequest { SendInvitations = sendInvitations! };
        return Http.Unwrap(_api.ApiV1DemandsDemandIdDispatchPostAsync(id, body, cancellationToken), r => r.Success, r => r.Data);
    }

    /// <summary>
    /// Creates a new demand (contract) from a template, without an idempotency
    /// key: a single attempt, never retried (a retried create would produce a
    /// duplicate demand). See <see cref="CreateAsync(CreateDemandRequest, string?, CancellationToken)"/>.
    /// </summary>
    public Task<CreatedDemand> CreateAsync(CreateDemandRequest body, CancellationToken cancellationToken = default) =>
        CreateAsync(body, idempotencyKey: null, cancellationToken);

    /// <summary>
    /// Creates a new demand (contract) from a template.
    ///
    /// Without <paramref name="idempotencyKey"/> this is a single attempt: a
    /// retried create would produce a duplicate demand. With a key, a repeated
    /// request does not create a second demand, so one retry is made after a 429
    /// (waiting Retry-After, at most 60 s). A key reused with a different body
    /// throws <c>IDEMPOTENCY_KEY_REUSED</c>. An <c>expiry_date</c> that is not a
    /// real calendar day throws <c>INVALID_EXPIRY_DATE</c>.
    /// </summary>
    /// <param name="body">The demand to create.</param>
    /// <param name="idempotencyKey">Your own reference for this request (e.g. an order number), sent as the <c>Idempotency-Key</c> header; <c>null</c> for none.</param>
    /// <param name="cancellationToken">Cancellation token, also honoured while waiting for the retry.</param>
    public Task<CreatedDemand> CreateAsync(CreateDemandRequest body, string? idempotencyKey, CancellationToken cancellationToken = default) =>
        Http.UnwrapIdempotentWrite(
            () => _api.ApiV1DemandsPostAsync(createDemandRequest: body, idempotencyKey: idempotencyKey, cancellationToken: cancellationToken),
            r => r.Success,
            r => r.Data,
            idempotencyKey,
            _retry,
            cancellationToken);

    /// <summary>
    /// Creates up to 10 demands from one template in a single request. Rows are
    /// created independently; check <c>Failed</c> and each result's <c>Status</c>.
    /// More than 10 rows throws <c>BULK_MAX_10</c>: split larger lists yourself.
    ///
    /// This endpoint has no idempotency key, so it is never retried: a retried
    /// batch would create the demands again. POST.
    /// </summary>
    public Task<ApiV1DemandsBulkPost200ResponseData> CreateBulkAsync(ApiV1DemandsBulkPostRequest body, CancellationToken cancellationToken = default) =>
        Http.Unwrap(
            _api.ApiV1DemandsBulkPostAsync(apiV1DemandsBulkPostRequest: body, xWorkspaceId: null, cancellationToken: cancellationToken),
            r => r.Success,
            r => r.Data);

    /// <summary>Returns a demand's status + per-party signing progress. GET — safe to auto-retry.</summary>
    public Task<DemandStatus> GetAsync(Guid id, CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(() => _api.ApiV1DemandsIdGetAsync(id, cancellationToken), r => r.Success, r => r.Data, _retry, cancellationToken);

    /// <summary>
    /// Places (replaces) signature/form fields on a demand's pages.
    /// See <see cref="UpsertItemsRequest.PageIds"/> for full-replace vs per-page-replace semantics.
    /// Every item needs an integer <c>page_id</c> (<c>PAGE_ID_REQUIRED</c>); an
    /// unknown <c>item_type</c> throws <c>INVALID_ITEM_TYPE</c>. POST, never auto-retried.
    /// </summary>
    public Task<UpsertItemsResponseData> AddItemsAsync(Guid id, UpsertItemsRequest body, CancellationToken cancellationToken = default) =>
        Http.Unwrap(_api.ApiV1DemandsIdItemsPostAsync(id, body, cancellationToken), r => r.Success, r => r.Data);

    /// <summary>
    /// Creates a demand directly from an uploaded document (no template) — a
    /// single PDF/DOC/DOCX/ODT/RTF/TXT, or 1-20 images merged into one PDF.
    ///
    /// Without <see cref="UploadDemandParams.IdempotencyKey"/> this is a single
    /// attempt. With a key, one retry is made after a 429 (waiting Retry-After, at
    /// most 60 s). Signing invitations are not sent unless
    /// <see cref="UploadDemandParams.SendInvitations"/> asks for them. POST.
    /// </summary>
    public Task<CreatedDemandUpload> UploadDocumentAsync(UploadDemandParams request, CancellationToken cancellationToken = default)
    {
        var partiesJson = JsonSerializer.Serialize(request.Parties, PartiesJsonOptions);
        var orderJson = request.Order is not null ? JsonSerializer.Serialize(request.Order) : null;
        var force = request.Force ? "true" : null;

        return Http.UnwrapIdempotentWrite(
            // Named arguments: the generated signature inserts new optional
            // parameters between existing ones and several share a type, so a
            // shifted positional argument would still compile. The file streams
            // are rebuilt on every attempt: a retry cannot resend a stream the
            // first attempt already read.
            () => _api.ApiV1DemandsUploadPostAsync(
                files: request.Files.Select(f => f.ToFileParameter()).ToList(),
                parties: partiesJson,
                idempotencyKey: request.IdempotencyKey,
                order: orderJson,
                title: request.Title,
                description: request.Description,
                fieldTemplateId: request.FieldTemplateId,
                force: force,
                sendInvitations: request.SendInvitations,
                onAnchorMiss: request.OnAnchorMiss,
                cancellationToken: cancellationToken),
            r => r.Success,
            r => r.Data,
            request.IdempotencyKey,
            _retry,
            cancellationToken);
    }

    /// <summary>
    /// Triggers an immediate SMS/email reminder to a demand's unsigned parties.
    /// Independent of the template/demand's scheduled <c>reminder_settings</c>.
    /// Subject to a 5-minute anti-spam window (override with <c>Force = true</c>)
    /// and a hard per-person cap of 3 reminders per channel (not overridable).
    /// The anti-spam window answers 429 <c>RATE_LIMITED</c>. A draft, completed,
    /// cancelled or expired demand throws 409 (<c>DEMAND_NOT_DISPATCHED</c>,
    /// <c>DEMAND_NOT_DISPATCHABLE</c>, <c>DEMAND_EXPIRED</c>). POST, never auto-retried.
    ///
    /// Routes through <c>RemindersApi</c>, not <c>DemandsApi</c> — the OpenAPI
    /// spec groups <c>POST /api/v1/demands/{id}/reminders</c> under a
    /// <c>Reminders</c> tag even though the route lives under <c>demands</c>.
    /// Same gotcha B1 (TS) and B2 (Python) flagged for their generators.
    /// </summary>
    public Task<ApiV1DemandsIdRemindersPost200ResponseData> SendReminderAsync(Guid id, TriggerReminderRequest? body = null, CancellationToken cancellationToken = default) =>
        Http.Unwrap(
            _remindersApi.ApiV1DemandsIdRemindersPostAsync(id, body ?? new TriggerReminderRequest(), cancellationToken),
            r => r.Success,
            r => r.Data);

    /// <summary>
    /// Lists your demands — counts-only (id/title/status/timestamps +
    /// <c>PartiesTotal</c>/<c>PartiesSigned</c>, NO party names/emails/phones).
    /// Filter by status/date/template, paginate with <paramref name="page"/>/
    /// <paramref name="limit"/>. GET — safe to auto-retry. For per-party detail
    /// use <see cref="GetAsync"/>.
    /// </summary>
    /// <param name="status">Filter by demand status (DRAFT / PENDING / COMPLETED / CANCELLED / EXPIRED).</param>
    /// <param name="q">Title search.</param>
    /// <param name="from">Lower bound (inclusive) on creation date.</param>
    /// <param name="to">Upper bound (inclusive) on creation date.</param>
    /// <param name="templateId">Only demands created from this template.</param>
    /// <param name="page">1-based page number.</param>
    /// <param name="limit">Page size.</param>
    /// <param name="sort"><c>field:direction</c>, e.g. <c>createdAt:desc</c>.</param>
    /// <param name="cancellationToken">Cancellation token.</param>
    public Task<ApiV1DemandsGet200ResponseData> ListAsync(
        string? status = null,
        string? q = null,
        DateOnly? from = null,
        DateOnly? to = null,
        Guid? templateId = null,
        int? page = null,
        int? limit = null,
        string? sort = null,
        CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(
            () => _api.ApiV1DemandsGetAsync(status: status, q: q, from: from, to: to, templateId: templateId, page: page, limit: limit, sort: sort, cancellationToken: cancellationToken),
            r => r.Success,
            r => r.Data,
            _retry,
            cancellationToken);

    /// <summary>
    /// Downloads the signed contract PDF (only once <c>Status == COMPLETED</c>) as
    /// the raw bytes — write them to disk or stream them on. Requires the API
    /// key's owner to own the demand. GET — safe to auto-retry.
    /// </summary>
    public Task<byte[]> GetPdfAsync(Guid id, CancellationToken cancellationToken = default) =>
        Http.UnwrapBinary(_api.ApiV1DemandsIdPdfGetAsync(id, cancellationToken));

    /// <summary>
    /// Downloads the PDF of one document in a multi-document envelope as raw
    /// bytes. For the whole contract use <see cref="GetPdfAsync"/>. GET.
    /// </summary>
    /// <param name="id">The demand (envelope).</param>
    /// <param name="documentId">The document inside it.</param>
    /// <param name="cancellationToken">Cancellation token.</param>
    public Task<byte[]> GetDocumentPdfAsync(Guid id, Guid documentId, CancellationToken cancellationToken = default) =>
        Http.UnwrapBinary(_api.ApiV1DemandsIdBelgeDocumentIdPdfGetAsync(id: id, documentId: documentId, cancellationToken: cancellationToken));

    /// <summary>
    /// Downloads the completion certificate (PAdES B-T sealed audit document) as
    /// raw bytes. Only produced for <c>COMPLETED</c> demands. Pass
    /// <paramref name="lang"/> = <c>"en"</c> for English. GET — safe to auto-retry.
    /// </summary>
    public Task<byte[]> GetCertificateAsync(Guid id, string? lang = null, CancellationToken cancellationToken = default) =>
        Http.UnwrapBinary(_api.ApiV1DemandsIdCertificateGetAsync(id, lang, cancellationToken));

    /// <summary>
    /// Returns the signing audit trail (view/sign/reject events). PII-masked:
    /// <c>IpMasked</c> (last octet hidden), actor name+email masked, no raw
    /// IP/device. GET — safe to auto-retry.
    /// </summary>
    public Task<ApiV1DemandsIdTimelineGet200ResponseData> GetTimelineAsync(Guid id, CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(() => _api.ApiV1DemandsIdTimelineGetAsync(id, cancellationToken), r => r.Success, r => r.Data, _retry, cancellationToken);

    /// <summary>
    /// Cancels (voids) a pending demand — sets it to <c>CANCELLED</c> and stops any
    /// scheduled reminders. A <c>COMPLETED</c> (or already-cancelled) demand can't
    /// be cancelled (throws). POST — never auto-retried.
    /// </summary>
    public Task<ApiV1DemandsIdCancelPost200ResponseData> CancelAsync(Guid id, string? reason = null, CancellationToken cancellationToken = default) =>
        Http.Unwrap(
            _api.ApiV1DemandsIdCancelPostAsync(id, new ApiV1DemandsIdCancelPostRequest(reason), cancellationToken),
            r => r.Success,
            r => r.Data);

    /// <summary>
    /// Re-sends the signing invitation to a single party (by <paramref name="partyId"/>
    /// from the demand's create/get response). Can't resend to a party who has
    /// already signed or declined, or one whose turn hasn't come in ordered
    /// signing (throws). POST — never auto-retried.
    /// </summary>
    public Task<ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData> ResendPartyAsync(Guid id, Guid partyId, CancellationToken cancellationToken = default) =>
        Http.Unwrap(
            _api.ApiV1DemandsIdPartiesPartyIdResendPostAsync(id, partyId, cancellationToken),
            r => r.Success,
            r => r.Data);

    /// <summary>
    /// Deletes a demand and all its data. Only NON-completed demands can be
    /// deleted via the API — a <c>COMPLETED</c> demand (signed document + audit
    /// trail) returns 409 and must be removed from the dashboard. DELETE — never
    /// auto-retried.
    /// </summary>
    public Task<ApiV1TemplatesIdDelete200ResponseData> DeleteAsync(Guid id, CancellationToken cancellationToken = default) =>
        Http.Unwrap(_api.ApiV1DemandsIdDeleteAsync(id, cancellationToken), r => r.Success, r => r.Data);
}
