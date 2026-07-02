using ImzalaApiClient.Api;
using ImzalaApiClient.Model;

namespace ImzalaSdk;

/// <summary>Backs <c>imzala.MeAsync()</c> — <c>ImzalaApiClient.Api.AccountApi</c> under the hood.</summary>
internal sealed class AccountResource
{
    private readonly IAccountApi _api;
    private readonly RetryConfig _retry;

    internal AccountResource(IAccountApi api, RetryConfig retry)
    {
        _api = api;
        _retry = retry;
    }

    /// <summary>Returns the calling API key's owner info (id, email, name, workspace, remaining credits). Requires the <c>timestamps</c> scope. GET — safe to auto-retry.</summary>
    public Task<ApiV1MeGet200ResponseData> MeAsync(CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(() => _api.ApiV1MeGetAsync(cancellationToken), r => r.Success, r => r.Data, _retry);
}
