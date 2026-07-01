using ImzalaApiClient.Api;
using ImzalaApiClient.Model;

namespace ImzalaSdk;

/// <summary><c>imzala.Templates.*</c> — <c>ImzalaApiClient.Api.TemplatesApi</c> under the hood.</summary>
public sealed class TemplatesResource
{
    private readonly ITemplatesApi _api;

    internal TemplatesResource(ITemplatesApi api) => _api = api;

    /// <summary>Lists your active templates.</summary>
    public Task<ApiV1TemplatesGet200ResponseData> ListAsync(int? page = null, int? limit = null, CancellationToken cancellationToken = default) =>
        Http.Unwrap(_api.ApiV1TemplatesGetAsync(page, limit, cancellationToken), r => r.Success, r => r.Data);

    /// <summary>Returns a template's parties + fillable variables.</summary>
    public Task<TemplateDetail> GetAsync(Guid id, CancellationToken cancellationToken = default) =>
        Http.Unwrap(_api.ApiV1TemplatesIdGetAsync(id, cancellationToken), r => r.Success, r => r.Data);

    /// <summary>Returns a ready-to-use integration guide (endpoint, required headers, example curl+JSON) for a template.</summary>
    public Task<TemplateUsage> UsageAsync(Guid id, CancellationToken cancellationToken = default) =>
        Http.Unwrap(_api.ApiV1TemplatesIdUsageGetAsync(id, cancellationToken), r => r.Success, r => r.Data);
}
