using ImzalaApiClient.Api;
using ImzalaApiClient.Model;

namespace ImzalaSdk;

/// <summary>
/// <c>imzala.FieldTemplates.*</c>. A field template is separate from a contract
/// template: it describes where fields land on an uploaded PDF, located by anchor
/// text. Use it with <see cref="UploadDemandParams.FieldTemplateId"/>.
/// </summary>
public sealed class FieldTemplatesResource
{
    private readonly ITemplatesApi _templatesApi;
    private readonly IDemandsApi _demandsApi;
    private readonly RetryConfig _retry;

    internal FieldTemplatesResource(ITemplatesApi templatesApi, IDemandsApi demandsApi, RetryConfig retry)
    {
        _templatesApi = templatesApi;
        _demandsApi = demandsApi;
        _retry = retry;
    }

    /// <summary>
    /// Lists your field templates (one page). <paramref name="limit"/> is clamped to
    /// 1..100; <paramref name="page"/> below 1 throws <c>INVALID_PAGE</c>. GET, safe to auto-retry.
    /// </summary>
    public Task<ApiV1FieldTemplatesGet200ResponseData> ListAsync(int? page = null, int? limit = null, CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(
            () => _templatesApi.ApiV1FieldTemplatesGetAsync(page: page, limit: limit, cancellationToken: cancellationToken),
            r => r.Success,
            r => r.Data,
            _retry,
            cancellationToken);

    /// <summary>
    /// Returns a field template's roles and field counts. A contract template id
    /// throws <c>TEMPLATE_NOT_FOUND</c>: the two are different kinds. GET, safe to auto-retry.
    /// </summary>
    public Task<FieldTemplateDetail> GetAsync(Guid id, CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(() => _templatesApi.ApiV1FieldTemplatesIdGetAsync(id, cancellationToken), r => r.Success, r => r.Data, _retry, cancellationToken);

    /// <summary>
    /// Dry run: tries the field template's layout on a PDF without creating anything
    /// or spending credit, and reports resolved fields and unresolved anchors
    /// separately. The cheapest way to avoid surprises before an upload with
    /// <see cref="UploadDemandParams.FieldTemplateId"/>. Rate limited per user. POST,
    /// but side-effect free; never auto-retried.
    /// </summary>
    /// <param name="id">The field template.</param>
    /// <param name="files">Exactly one PDF to try the layout on (the field is a list on the wire).</param>
    /// <param name="onAnchorMiss"><c>"block"</c> or <c>"drop"</c>. When omitted, <c>block</c> is applied, not the template's own setting; <c>drop</c> only applies where the template already allows it.</param>
    /// <param name="cancellationToken">Cancellation token.</param>
    public Task<FieldLayoutPreview> PreviewLayoutAsync(Guid id, IReadOnlyList<FileInput> files, string? onAnchorMiss = null, CancellationToken cancellationToken = default) =>
        Http.Unwrap(
            _demandsApi.ApiV1FieldTemplatesIdPreviewLayoutPostAsync(
                id: id,
                files: files.Select(f => f.ToFileParameter()).ToList(),
                onAnchorMiss: onAnchorMiss,
                cancellationToken: cancellationToken),
            r => r.Success,
            r => r.Data);
}
