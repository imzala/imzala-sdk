using GeneratedFileParameter = ImzalaApiClient.Client.FileParameter;

namespace ImzalaSdk;

/// <summary>
/// Auto-retry configuration for safe, idempotent GET-backed resource methods.
/// Mirrors the TS SDK's <c>RetryConfig</c> (<c>http.ts</c>) and the Python
/// SDK's <c>_RetryConfig</c> (<c>client.py</c>). Internal — consumers set
/// <c>maxRetries</c>/<c>retryBaseDelayMs</c> via the flattened <see cref="Imzala"/>
/// constructor parameters, not this type directly.
/// </summary>
internal sealed class RetryConfig
{
    /// <summary>Max retry attempts (not counting the initial try). <c>0</c> disables retry.</summary>
    public int MaxRetries { get; init; } = 2;

    /// <summary>Base delay (ms) for exponential backoff between retries.</summary>
    public int RetryBaseDelayMs { get; init; } = 300;
}

/// <summary>
/// Every imzala.org API response uses the same envelope: <c>{success: true,
/// data: {...}}</c> on success, or a non-2xx status with <c>{success: false,
/// error/message: ...}</c> on failure (surfaced by the vendored generated
/// client as a thrown <c>ImzalaApiClient.Client.ApiException</c>, not a
/// resolved-but-failed response).
///
/// <see cref="Unwrap{TResponse, TData}"/> awaits a generated-client call,
/// unwraps its <c>Data</c> property, and normalizes any failure (thrown
/// exception, or a <c>{success:false}</c> body on an otherwise-2xx response)
/// into a typed <see cref="ImzalaError"/> — see <c>Errors.cs</c>. Every
/// resource method in <c>src/Resources/*</c> routes through this. Mirrors
/// <c>http.ts</c>'s <c>unwrap&lt;T&gt;()</c> (TS) and <c>client.py</c>'s
/// <c>_unwrap()</c> (Python); C#'s nominal typing (each generated response is
/// its own concrete class, not a structural <c>{success,data}</c> shape) means
/// the caller passes explicit <c>Success</c>/<c>Data</c> selectors instead of
/// relying on duck typing.
/// </summary>
internal static class Http
{
    public static async Task<TData> Unwrap<TResponse, TData>(
        Task<TResponse> call,
        Func<TResponse, bool> success,
        Func<TResponse, TData> data)
    {
        TResponse response;
        try
        {
            response = await call.ConfigureAwait(false);
        }
        catch (Exception err)
        {
            throw ErrorMapper.Map(err);
        }

        if (response is null || !success(response))
        {
            throw new ImzalaError("imzala.org API request failed");
        }

        return data(response);
    }

    /// <summary>
    /// Awaits a generated-client call that returns a raw binary body (an
    /// <c>application/pdf</c> response, materialized by the vendored client as a
    /// <see cref="GeneratedFileParameter"/> wrapping a <see cref="Stream"/>) and
    /// reads it fully into a <c>byte[]</c>. Used by the binary demand downloads
    /// (<c>Demands.GetPdfAsync</c> / <c>Demands.GetCertificateAsync</c>) — these
    /// endpoints don't return the <c>{success, data}</c> JSON envelope, so they
    /// bypass <see cref="Unwrap{TResponse, TData}"/>, but they still funnel any
    /// thrown generated exception through <see cref="ErrorMapper"/> so callers
    /// get the same typed <see cref="ImzalaError"/> taxonomy. GET-only — never
    /// auto-retried here (the download methods are still GETs; retry wasn't wired
    /// for binary because a partially-read response stream can't be replayed).
    /// </summary>
    public static async Task<byte[]> UnwrapBinary(Task<GeneratedFileParameter> call)
    {
        GeneratedFileParameter response;
        try
        {
            response = await call.ConfigureAwait(false);
        }
        catch (Exception err)
        {
            throw ErrorMapper.Map(err);
        }

        if (response is null)
        {
            throw new ImzalaError("imzala.org API returned an empty binary response");
        }

        return await ReadAllBytesAsync(response.Content).ConfigureAwait(false);
    }

    private static async Task<byte[]> ReadAllBytesAsync(Stream? content)
    {
        if (content is null)
        {
            return Array.Empty<byte>();
        }

        if (content.CanSeek && content.Position != 0)
        {
            content.Position = 0;
        }

        if (content is MemoryStream ms)
        {
            return ms.ToArray();
        }

        using var buffer = new MemoryStream();
        await content.CopyToAsync(buffer).ConfigureAwait(false);
        return buffer.ToArray();
    }

    /// <summary>
    /// Like <see cref="Unwrap{TResponse, TData}"/>, but adds safe auto-retry for
    /// <b>GET-only, idempotent</b> resource methods (<c>Templates.List/Get/UsageAsync</c>,
    /// <c>Demands.GetAsync</c>, <c>MeAsync</c>). Retries on 429 (rate limited — honors
    /// <c>Retry-After</c>) and 5xx (server error) with exponential backoff + jitter; any
    /// other status (400/401/404/409/422/...) is thrown immediately, same as
    /// <see cref="Unwrap{TResponse, TData}"/>.
    ///
    /// <b>SAFETY — never call this with a non-GET request.</b> There is deliberately no
    /// <c>method</c> parameter and no way to opt a POST/PUT/PATCH/DELETE call into
    /// retrying: this is not a caller-configurable behavior. Retrying a write (e.g.
    /// <c>Demands.CreateAsync</c>, <c>Demands.SendReminderAsync</c>) could duplicate a
    /// demand or double-send a reminder — those resource methods must keep using the
    /// plain <see cref="Unwrap{TResponse, TData}"/> above, once, with no retry loop.
    ///
    /// <paramref name="requestFn"/> is a thunk (not an already-created <see cref="Task"/>)
    /// because retrying means re-issuing the underlying HTTP request — a completed task
    /// can't be replayed.
    /// </summary>
    public static async Task<TData> UnwrapRetryableGet<TResponse, TData>(
        Func<Task<TResponse>> requestFn,
        Func<TResponse, bool> success,
        Func<TResponse, TData> data,
        RetryConfig retry)
    {
        var attempt = 0;
        for (; ; )
        {
            try
            {
                return await Unwrap(requestFn(), success, data).ConfigureAwait(false);
            }
            catch (Exception err)
            {
                var mapped = err as ImzalaError ?? ErrorMapper.Map(err);
                if (attempt >= retry.MaxRetries || !IsRetryableStatus(mapped.StatusCode))
                {
                    throw mapped;
                }

                await Task.Delay(ComputeDelayMs(mapped, attempt, retry.RetryBaseDelayMs)).ConfigureAwait(false);
                attempt++;
            }
        }
    }

    /// <summary>429 (rate limited) and 5xx (server error) are treated as transient. Everything else (4xx) is a client error and is never retried.</summary>
    private static bool IsRetryableStatus(int? statusCode) =>
        statusCode == 429 || statusCode is >= 500 and <= 599;

    /// <summary>Exponential backoff with jitter, honoring <c>Retry-After</c> on 429s (already parsed onto <see cref="ImzalaRateLimitError.RetryAfter"/> by <see cref="ErrorMapper"/>).</summary>
    private static int ComputeDelayMs(ImzalaError error, int attempt, int baseDelayMs)
    {
        if (error is ImzalaRateLimitError { RetryAfter: { } retryAfterSeconds })
        {
            return Math.Max(0, (int)(retryAfterSeconds * 1000));
        }

        var backoff = baseDelayMs * Math.Pow(2, attempt);
        var jitter = Random.Shared.NextDouble() * baseDelayMs;
        return (int)(backoff + jitter);
    }
}
