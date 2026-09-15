using GeneratedFileParameter = ImzalaApiClient.Client.FileParameter;

namespace ImzalaSdk;

/// <summary>
/// Retry settings shared by the resources: the GET auto-retry (<see cref="MaxRetries"/>
/// + exponential backoff) and the single retry of a write sent with an
/// Idempotency-Key (which waits Retry-After, or <see cref="RetryBaseDelayMs"/> when
/// the server gave none). Holding a config never makes a write retryable: only
/// <see cref="Http.UnwrapRetryableGet{TResponse, TData}"/> and
/// <see cref="Http.UnwrapIdempotentWrite{TResponse, TData}"/> read it.
///
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

    /// <summary>Waits before a retry. Replaced in tests so nothing really sleeps; receives the caller's cancellation token.</summary>
    public Func<TimeSpan, CancellationToken, Task> Delay { get; init; } = Task.Delay;
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
        RetryConfig retry,
        CancellationToken cancellationToken = default)
    {
        var attempt = 0;
        for (; ; )
        {
            try
            {
                return await Unwrap(Invoke(requestFn), success, data).ConfigureAwait(false);
            }
            catch (Exception err)
            {
                var mapped = err as ImzalaError ?? ErrorMapper.Map(err);
                if (attempt >= retry.MaxRetries || !IsRetryableStatus(mapped.StatusCode))
                {
                    throw mapped;
                }

                var delayMs = ComputeDelayMs(mapped, attempt, retry.RetryBaseDelayMs);
                // A server asking for a longer wait than we are willing to block for
                // gets the error back instead of a silent long sleep.
                if (delayMs > MaxRetryWaitMs)
                {
                    throw mapped;
                }

                await retry.Delay(TimeSpan.FromMilliseconds(delayMs), cancellationToken).ConfigureAwait(false);
                attempt++;
            }
        }
    }

    /// <summary>
    /// Longest wait (ms) the SDK blocks the caller for before a retry. When the
    /// server asks for more (Retry-After), the error is thrown instead: applies to
    /// the GET auto-retry and to the one retry of an idempotent write.
    /// </summary>
    public const long MaxRetryWaitMs = 60_000;

    /// <summary>
    /// Bounded, safe retry for write calls.
    ///
    /// Writes are normally never retried: a repeated create produces a second
    /// demand. The one exception is a write sent with an Idempotency-Key. The
    /// server does not treat a second request with the same key as a new record,
    /// so after a 429 it is safe to wait for Retry-After (or the base delay when
    /// there is none) and try exactly once more. A second 429, and any other error
    /// including 5xx, is thrown. If the server asks for a longer wait than
    /// <paramref name="maxWaitMs"/> (60 s by default), the 429 is thrown instead of
    /// blocking the caller.
    ///
    /// Only use this for endpoints whose Idempotency-Key the server honours (demand
    /// create, document upload, timestamp create). On any other endpoint the "safe"
    /// retry could create a duplicate. Without a key (or with an empty one) this
    /// behaves exactly like <see cref="Unwrap{TResponse, TData}"/>.
    ///
    /// <paramref name="requestFn"/> is called again for the retry, so it must build
    /// its request (including any file streams) from scratch on each call.
    /// </summary>
    public static async Task<TData> UnwrapIdempotentWrite<TResponse, TData>(
        Func<Task<TResponse>> requestFn,
        Func<TResponse, bool> success,
        Func<TResponse, TData> data,
        string? idempotencyKey,
        RetryConfig retry,
        CancellationToken cancellationToken,
        long maxWaitMs = MaxRetryWaitMs)
    {
        ValidateHeaderValue(idempotencyKey, "Idempotency-Key");

        ImzalaError first;
        try
        {
            return await Unwrap(Invoke(requestFn), success, data).ConfigureAwait(false);
        }
        catch (ImzalaError err)
        {
            first = err;
        }

        if (string.IsNullOrEmpty(idempotencyKey) || first.StatusCode != 429)
        {
            throw first;
        }

        long waitMs = first is ImzalaRateLimitError { RetryAfter: { } retryAfterSeconds }
            ? Math.Max(0, (long)Math.Round(retryAfterSeconds * 1000))
            : retry.RetryBaseDelayMs;
        if (waitMs > maxWaitMs)
        {
            throw first;
        }

        await retry.Delay(TimeSpan.FromMilliseconds(waitMs), cancellationToken).ConfigureAwait(false);
        return await Unwrap(Invoke(requestFn), success, data).ConfigureAwait(false);
    }

    /// <summary>
    /// A required idempotency key sent as a body field: it must be a non-empty
    /// printable ASCII string. Checked before the request is built.
    /// </summary>
    internal static void ValidateIdempotencyKey(string? value, string name)
    {
        if (string.IsNullOrEmpty(value))
        {
            var inner = new ArgumentException($"{name} is required and must be a non-empty string.", nameof(value));
            throw new ImzalaValidationError(inner.Message, null, null, null, inner);
        }

        ValidateHeaderValue(value, name);
    }

    /// <summary>
    /// Rejects a header value that is not printable ASCII before anything is sent.
    /// The generated client adds header values without validation: a line break
    /// would go on the wire and start a new header (so the server could see a
    /// different key, or none, while the SDK still treats the write as keyed), and
    /// non-ASCII characters fail inside the HTTP stack only after a connection was
    /// opened. Both surface as <see cref="ImzalaValidationError"/> without a status.
    /// </summary>
    internal static void ValidateHeaderValue(string? value, string headerName)
    {
        if (value is null)
        {
            return;
        }

        foreach (var ch in value)
        {
            if (ch < 0x20 || ch > 0x7E)
            {
                var inner = new ArgumentException(
                    $"{headerName} may only contain printable ASCII characters (no line breaks, no non-ASCII letters).",
                    nameof(value));
                throw new ImzalaValidationError(inner.Message, null, null, null, inner);
            }
        }
    }

    /// <summary>Starts a request, turning a synchronous throw while building it into a faulted task so it is mapped like any other failure.</summary>
    private static Task<TResponse> Invoke<TResponse>(Func<Task<TResponse>> requestFn)
    {
        try
        {
            return requestFn();
        }
        catch (Exception err)
        {
            return Task.FromException<TResponse>(err);
        }
    }

    /// <summary>429 (rate limited) and 5xx (server error) are treated as transient. Everything else (4xx) is a client error and is never retried.</summary>
    private static bool IsRetryableStatus(int? statusCode) =>
        statusCode == 429 || statusCode is >= 500 and <= 599;

    /// <summary>Exponential backoff with jitter, honoring <c>Retry-After</c> on 429s (already parsed onto <see cref="ImzalaRateLimitError.RetryAfter"/> by <see cref="ErrorMapper"/>).</summary>
    private static long ComputeDelayMs(ImzalaError error, int attempt, int baseDelayMs)
    {
        if (error is ImzalaRateLimitError { RetryAfter: { } retryAfterSeconds })
        {
            return Math.Max(0, (long)Math.Round(retryAfterSeconds * 1000));
        }

        var backoff = baseDelayMs * Math.Pow(2, attempt);
        var jitter = Random.Shared.NextDouble() * baseDelayMs;
        return (long)(backoff + jitter);
    }
}
