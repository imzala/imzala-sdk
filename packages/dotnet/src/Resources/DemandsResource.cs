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

    private static readonly JsonSerializerOptions PartiesJsonOptions = new()
    {
        DefaultIgnoreCondition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull,
    };

    internal DemandsResource(IDemandsApi api, IRemindersApi remindersApi)
    {
        _api = api;
        _remindersApi = remindersApi;
    }

    /// <summary>Creates a new demand (contract) from a template.</summary>
    public Task<CreatedDemand> CreateAsync(CreateDemandRequest body, CancellationToken cancellationToken = default) =>
        Http.Unwrap(_api.ApiV1DemandsPostAsync(body, cancellationToken), r => r.Success, r => r.Data);

    /// <summary>Returns a demand's status + per-party signing progress.</summary>
    public Task<DemandStatus> GetAsync(Guid id, CancellationToken cancellationToken = default) =>
        Http.Unwrap(_api.ApiV1DemandsIdGetAsync(id, cancellationToken), r => r.Success, r => r.Data);

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
}
