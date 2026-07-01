using ImzalaApiClient.Api;
using ImzalaApiClient.Model;

namespace ImzalaSdk;

/// <summary><c>imzala.Embed.*</c> — <c>ImzalaApiClient.Api.DemandsApi</c> under the hood (embed-session lives on the demands route).</summary>
public sealed class EmbedResource
{
    private readonly IDemandsApi _api;

    internal EmbedResource(IDemandsApi api) => _api = api;

    /// <summary>
    /// Mints a short-lived, single-use embed signing token for a demand's
    /// party. The returned <c>embed_url</c> is meant for an <c>&lt;iframe&gt;</c>.
    ///
    /// Signatures obtained this way are SES by default (AES if TC/biometric
    /// verification ran) — this flow never produces QES.
    /// </summary>
    /// <param name="demandId">The demand to mint a session for.</param>
    /// <param name="partyId">The party to mint an embed session for — from <c>signing_urls[].party_id</c> in the demand's create/get response.</param>
    /// <param name="cancellationToken">Cancellation token.</param>
    public Task<ApiV1DemandsIdEmbedSessionPost200ResponseData> CreateSessionAsync(Guid demandId, Guid partyId, CancellationToken cancellationToken = default) =>
        Http.Unwrap(
            _api.ApiV1DemandsIdEmbedSessionPostAsync(demandId, new ApiV1DemandsIdEmbedSessionPostRequest(partyId), cancellationToken),
            r => r.Success,
            r => r.Data);
}
