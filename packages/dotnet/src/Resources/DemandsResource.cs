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
    };

    internal DemandsResource(IDemandsApi api, IRemindersApi remindersApi, RetryConfig retry)
    {
        _api = api;
        _remindersApi = remindersApi;
        _retry = retry;
    }

    /// <summary>
    /// Creates a new demand (contract) from a template. POST — never
    /// auto-retried (a retried create would produce a duplicate demand).
    /// </summary>
    public Task<CreatedDemand> CreateAsync(CreateDemandRequest body, CancellationToken cancellationToken = default) =>
        Http.Unwrap(_api.ApiV1DemandsPostAsync(body, cancellationToken), r => r.Success, r => r.Data);

    /// <summary>Returns a demand's status + per-party signing progress. GET — safe to auto-retry.</summary>
    public Task<DemandStatus> GetAsync(Guid id, CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(() => _api.ApiV1DemandsIdGetAsync(id, cancellationToken), r => r.Success, r => r.Data, _retry);

    /// <summary>
    /// Places (replaces) signature/form fields on a demand's pages.
    /// See <see cref="UpsertItemsRequest.PageIds"/> for full-replace vs per-page-replace semantics.
    /// </summary>
    public Task<UpsertItemsResponseData> AddItemsAsync(Guid id, UpsertItemsRequest body, CancellationToken cancellationToken = default) =>
        Http.Unwrap(_api.ApiV1DemandsIdItemsPostAsync(id, body, cancellationToken), r => r.Success, r => r.Data);

    /// <summary>
    /// Creates a demand directly from an uploaded document (no template) — a
    /// single PDF/DOC/DOCX/ODT/RTF/TXT, or 1-20 images merged into one PDF.
    /// </summary>
    public Task<CreatedDemandUpload> UploadDocumentAsync(UploadDemandParams request, CancellationToken cancellationToken = default)
    {
        var files = request.Files.Select(f => f.ToFileParameter()).ToList();
        var partiesJson = JsonSerializer.Serialize(request.Parties, PartiesJsonOptions);
        var orderJson = request.Order is not null ? JsonSerializer.Serialize(request.Order) : null;

        return Http.Unwrap(
            _api.ApiV1DemandsUploadPostAsync(files, partiesJson, orderJson, request.Title, request.Description, cancellationToken),
            r => r.Success,
            r => r.Data);
    }

    /// <summary>
    /// Triggers an immediate SMS/email reminder to a demand's unsigned parties.
    /// Independent of the template/demand's scheduled <c>reminder_settings</c>.
    /// Subject to a 5-minute anti-spam window (override with <c>Force = true</c>)
    /// and a hard per-person cap of 3 reminders per channel (not overridable).
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
            () => _api.ApiV1DemandsGetAsync(status, q, from, to, templateId, page, limit, sort, cancellationToken),
            r => r.Success,
            r => r.Data,
            _retry);

    /// <summary>
    /// Downloads the signed contract PDF (only once <c>Status == COMPLETED</c>) as
    /// the raw bytes — write them to disk or stream them on. Requires the API
    /// key's owner to own the demand. GET — safe to auto-retry.
    /// </summary>
    public Task<byte[]> GetPdfAsync(Guid id, CancellationToken cancellationToken = default) =>
        Http.UnwrapBinary(_api.ApiV1DemandsIdPdfGetAsync(id, cancellationToken));

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
        Http.UnwrapRetryableGet(() => _api.ApiV1DemandsIdTimelineGetAsync(id, cancellationToken), r => r.Success, r => r.Data, _retry);

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
