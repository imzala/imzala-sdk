using ImzalaApiClient.Api;
using ImzalaApiClient.Model;

namespace ImzalaSdk;

/// <summary><c>imzala.Reports.*</c>, backed by <c>ImzalaApiClient.Api.ReportsApi</c>.</summary>
public sealed class ReportsResource
{
    private readonly IReportsApi _api;
    private readonly RetryConfig _retry;

    internal ReportsResource(IReportsApi api, RetryConfig retry)
    {
        _api = api;
        _retry = retry;
    }

    /// <summary>
    /// Returns aggregate demand counts for your workspace (pending, completed,
    /// cancelled, expired, created this month). Counts only, no personal data.
    /// GET, safe to auto-retry.
    /// </summary>
    public Task<ApiV1ReportsGet200ResponseData> GetAsync(CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(() => _api.ApiV1ReportsGetAsync(cancellationToken), r => r.Success, r => r.Data, _retry, cancellationToken);
}
