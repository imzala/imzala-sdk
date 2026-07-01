using ImzalaApiClient.Api;
using ImzalaApiClient.Model;

namespace ImzalaSdk;

/// <summary><c>imzala.Timestamps.*</c> — <c>ImzalaApiClient.Api.TimestampsApi</c> under the hood.</summary>
public sealed class TimestampsResource
{
    private readonly ITimestampsApi _api;

    internal TimestampsResource(ITimestampsApi api) => _api = api;

    /// <summary>
    /// RFC 3161-timestamps a file via TÜBİTAK KAMU SM TSA (existence + integrity
    /// proof — not a signature; see <see cref="TimestampRecord"/> for details).
    /// Pass <see cref="CreateTimestampParams.IdempotencyKey"/> to make retries
    /// safe (5-minute window, no duplicate credit spend).
    /// </summary>
    public Task<TimestampRecord> CreateAsync(CreateTimestampParams request, CancellationToken cancellationToken = default)
    {
        var file = new FileInput
        {
            Content = request.Content,
            FileName = request.FileName,
            ContentType = request.ContentType,
        }.ToFileParameter();

        return Http.Unwrap(
            _api.ApiV1TimestampsPostAsync(file, request.IdempotencyKey, request.Description, request.OwnerFirstName, request.OwnerLastName, cancellationToken),
            r => r.Success,
            r => r.Data);
    }
}
