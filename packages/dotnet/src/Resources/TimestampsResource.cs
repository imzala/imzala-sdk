using ImzalaApiClient.Api;
using ImzalaApiClient.Model;

namespace ImzalaSdk;

/// <summary><c>imzala.Timestamps.*</c> — <c>ImzalaApiClient.Api.TimestampsApi</c> under the hood.</summary>
public sealed class TimestampsResource
{
    private readonly ITimestampsApi _api;
    private readonly RetryConfig _retry;

    internal TimestampsResource(ITimestampsApi api)
        : this(api, new RetryConfig { MaxRetries = 0 })
    {
    }

    internal TimestampsResource(ITimestampsApi api, RetryConfig retry)
    {
        _api = api;
        _retry = retry;
    }

    /// <summary>
    /// RFC 3161-timestamps a file via TÜBİTAK KAMU SM TSA (existence + integrity
    /// proof — not a signature; see <see cref="TimestampRecord"/> for details).
    /// Pass <see cref="CreateTimestampParams.IdempotencyKey"/> to make retries
    /// safe (5-minute window, no duplicate credit spend); with a key, one retry is
    /// made after a 429 (waiting Retry-After, at most 60 s). Without a key this is
    /// a single attempt.
    /// </summary>
    public Task<TimestampRecord> CreateAsync(CreateTimestampParams request, CancellationToken cancellationToken = default)
    {
        var input = new FileInput
        {
            Content = request.Content,
            FileName = request.FileName,
            ContentType = request.ContentType,
        };

        return Http.UnwrapIdempotentWrite(
            // Named arguments pin each slot; the file stream is rebuilt on every
            // attempt so a retry never resends an already-read stream.
            () => _api.ApiV1TimestampsPostAsync(
                file: input.ToFileParameter(),
                idempotencyKey: request.IdempotencyKey,
                description: request.Description,
                ownerFirstName: request.OwnerFirstName,
                ownerLastName: request.OwnerLastName,
                cancellationToken: cancellationToken),
            r => r.Success,
            r => r.Data,
            request.IdempotencyKey,
            _retry,
            cancellationToken);
    }

    /// <summary>Lists your timestamp records (one page). GET, safe to auto-retry.</summary>
    /// <param name="q">Search in file name and description (max 100 characters).</param>
    /// <param name="status">Comma-separated status filter, e.g. <c>"ACTIVE,VERIFIED"</c> (ACTIVE, VERIFIED, EXPIRED, INVALID).</param>
    /// <param name="from">Lower bound on the stamp day.</param>
    /// <param name="to">Upper bound on the stamp day.</param>
    /// <param name="page">1-based page number.</param>
    /// <param name="limit">Page size, 10 to 100, default 25.</param>
    /// <param name="sort">One of createdAt, timestamp_date, original_file_name, original_file_size, status; a <c>-</c> prefix sorts descending. Server default <c>-createdAt</c>.</param>
    /// <param name="cancellationToken">Cancellation token.</param>
    public Task<ApiV1TimestampsGet200ResponseData> ListAsync(
        string? q = null,
        string? status = null,
        DateOnly? from = null,
        DateOnly? to = null,
        int? page = null,
        int? limit = null,
        string? sort = null,
        CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(
            () => _api.ApiV1TimestampsGetAsync(page: page, limit: limit, q: q, status: status, from: from, to: to, sort: sort, cancellationToken: cancellationToken),
            r => r.Success,
            r => r.Data,
            _retry,
            cancellationToken);

    /// <summary>Returns one timestamp record. GET, safe to auto-retry.</summary>
    public Task<TimestampListItem> GetAsync(Guid id, CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(() => _api.ApiV1TimestampsIdGetAsync(id, cancellationToken), r => r.Success, r => r.Data, _retry, cancellationToken);
}
