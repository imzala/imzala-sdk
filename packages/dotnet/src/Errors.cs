using System.Globalization;
using System.Text.Json;
using System.Text.RegularExpressions;
using GeneratedApiException = ImzalaApiClient.Client.ApiException;
using GeneratedMultimap = ImzalaApiClient.Client.Multimap<string, string>;

namespace ImzalaSdk;

/// <summary>
/// Base error type thrown by every <see cref="Imzala"/> facade method. Normalizes
/// the vendored client's <see cref="GeneratedApiException"/>, network/timeout
/// failures, and a <c>{success:false}</c> response envelope on an otherwise-2xx
/// response into a single throwable shape — callers never need to reach into
/// <c>ImzalaApiClient.Client</c> internals.
///
/// Thrown directly (not as a subclass) for statuses that don't have a dedicated
/// subclass below (400, 404, 409, 500, ...) — same 4-class taxonomy as the
/// TS (<c>@imzala/node</c>) and Python (<c>imzala</c>) SDKs.
/// </summary>
public class ImzalaError : Exception
{
    /// <summary>HTTP status code, when the error originated from an HTTP response.</summary>
    public int? StatusCode { get; }

    /// <summary>Raw response body (unparsed text), when available.</summary>
    public string? Body { get; }

    /// <summary>Machine-readable error code from the response envelope, when present (e.g. <c>"INVALID_API_KEY"</c>).</summary>
    public string? Code { get; }

    /// <summary>
    /// One-line Turkish explanation of <see cref="Code"/> from <see cref="ErrorCodes"/>.
    /// <c>null</c> when there is no code or the SDK does not know it.
    /// </summary>
    public string? CodeDescription => ErrorCodes.Describe(Code);

    public ImzalaError(string message, int? statusCode = null, string? body = null, string? code = null, Exception? innerException = null)
        : base(message, innerException)
    {
        StatusCode = statusCode;
        Body = body;
        Code = code;
    }
}

/// <summary>Missing/invalid API key (401) or disabled key / insufficient scope (403).</summary>
public sealed class ImzalaAuthError : ImzalaError
{
    public ImzalaAuthError(string message, int? statusCode = null, string? body = null, string? code = null, Exception? innerException = null)
        : base(message, statusCode, body, code, innerException)
    {
    }
}

/// <summary>
/// Rate limited (429). <see cref="RetryAfter"/> is seconds, when the server provided one.
///
/// Several different limits answer with 429 and each has its own code (e.g.
/// <c>RATE_LIMIT_EXCEEDED</c>, <c>TOO_MANY_REQUESTS</c>, <c>RATE_LIMITED</c>,
/// <c>RECIPIENT_RESEND_LIMIT</c>, <c>MAX_SMS_REMINDERS_REACHED</c>), so branch on
/// this class or on the status code, not on one particular code.
/// </summary>
public sealed class ImzalaRateLimitError : ImzalaError
{
    /// <summary>Seconds to wait before retrying, when the server provided one (from the response body or the <c>Retry-After</c> header, which may be a number of seconds or an HTTP date).</summary>
    public double? RetryAfter { get; }

    /// <summary>The standard <c>RateLimit-*</c> headers of the response, or <c>null</c> when the server sent none.</summary>
    public RateLimitInfo? RateLimit { get; init; }

    public ImzalaRateLimitError(string message, int? statusCode = null, string? body = null, string? code = null, double? retryAfter = null, Exception? innerException = null)
        : base(message, statusCode, body, code, innerException)
    {
        RetryAfter = retryAfter;
    }
}

/// <summary>
/// Request payload failed validation: a 422 from the server, or a parameter the
/// client rejected before sending anything (then <see cref="ImzalaError.StatusCode"/>
/// is <c>null</c> and <see cref="Exception.InnerException"/> holds the original exception).
/// </summary>
public sealed class ImzalaValidationError : ImzalaError
{
    public ImzalaValidationError(string message, int? statusCode = null, string? body = null, string? code = null, Exception? innerException = null)
        : base(message, statusCode, body, code, innerException)
    {
    }
}

/// <summary>
/// Standard <c>RateLimit-*</c> response headers attached to a 429. The server does
/// not send <c>X-RateLimit-*</c>, so those are never read. Each field is <c>null</c>
/// when its header was absent or not a whole number.
/// </summary>
public sealed class RateLimitInfo
{
    /// <summary><c>RateLimit-Limit</c>: requests allowed per window. Defaults to 60 but can be lowered per API key, so read it rather than assuming.</summary>
    public int? Limit { get; init; }

    /// <summary><c>RateLimit-Remaining</c>: requests left in the current window.</summary>
    public int? Remaining { get; init; }

    /// <summary><c>RateLimit-Reset</c>: seconds until the window resets.</summary>
    public int? Reset { get; init; }

    /// <summary><c>RateLimit-Policy</c>: raw policy string, e.g. <c>60;w=60</c>.</summary>
    public string? Policy { get; init; }
}

/// <summary>
/// Maps any exception thrown while calling the vendored generated client (or a
/// bare <c>{success:false}</c> envelope) to the appropriate <see cref="ImzalaError"/>
/// subclass, based on HTTP status code.
///
/// imzala.org error envelopes are not fully uniform across endpoints: most are
/// <c>{success:false, error:"&lt;code&gt;", message:"&lt;text&gt;"}</c>, but some (e.g. the
/// reminders 429) nest a <c>{code, message, retry_after_seconds}</c> object under
/// <c>error</c> instead of a plain string — <see cref="ExtractErrorMessage"/> /
/// <see cref="ExtractErrorCode"/> handle both shapes, mirroring
/// <c>errors.ts</c>'s <c>extractErrorMessage</c>/<c>extractErrorCode</c> and
/// <c>errors.py</c>'s equivalents.
/// </summary>
internal static class ErrorMapper
{
    public static ImzalaError Map(Exception err)
    {
        if (err is ImzalaError already) return already;

        if (IsLocalValidationFailure(err))
        {
            return new ImzalaValidationError(err.Message, null, null, null, err);
        }

        if (err is GeneratedApiException apiEx)
        {
            var status = apiEx.ErrorCode;
            var bodyText = apiEx.ErrorContent as string;
            var json = TryParseJson(bodyText);
            var message = ExtractErrorMessage(json) ?? apiEx.Message;
            var code = ExtractErrorCode(json);

            switch (status)
            {
                case 401:
                case 403:
                    return new ImzalaAuthError(message, status, bodyText, code, apiEx);
                case 429:
                    var retryAfter = ExtractRetryAfter(json, apiEx.Headers);
                    return new ImzalaRateLimitError(message, status, bodyText, code, retryAfter, apiEx)
                    {
                        RateLimit = ExtractRateLimitInfo(apiEx.Headers),
                    };
                case 422:
                    return new ImzalaValidationError(message, status, bodyText, code, apiEx);
                default:
                    return new ImzalaError(message, status, bodyText, code, apiEx);
            }
        }

        return new ImzalaError(err.Message, null, null, null, err);
    }

    /// <summary>
    /// The generated client checks some parameters itself and throws before any
    /// request is sent: a missing required parameter comes back as an
    /// <see cref="GeneratedApiException"/> with a made-up 400 and no response
    /// headers or body, and argument checks as <see cref="ArgumentException"/>.
    /// Neither is a server answer, so neither gets a status code.
    /// </summary>
    private static bool IsLocalValidationFailure(Exception err) =>
        err is ArgumentException ||
        (err is GeneratedApiException { ErrorContent: null, Headers: null } local &&
         local.Message.StartsWith("Missing required parameter", StringComparison.Ordinal));

    private static JsonElement? TryParseJson(string? text)
    {
        if (string.IsNullOrEmpty(text)) return null;
        try
        {
            using var doc = JsonDocument.Parse(text);
            return doc.RootElement.Clone();
        }
        catch (JsonException)
        {
            return null;
        }
    }

    internal static string? ExtractErrorMessage(JsonElement? body)
    {
        if (body is not { ValueKind: JsonValueKind.Object } b) return null;

        if (b.TryGetProperty("message", out var msg) && msg.ValueKind == JsonValueKind.String)
            return msg.GetString();

        if (b.TryGetProperty("error", out var error))
        {
            if (error.ValueKind == JsonValueKind.String)
                return error.GetString();

            if (error.ValueKind == JsonValueKind.Object)
            {
                if (error.TryGetProperty("message", out var nestedMsg) && nestedMsg.ValueKind == JsonValueKind.String)
                    return nestedMsg.GetString();
                if (error.TryGetProperty("code", out var nestedCode) && nestedCode.ValueKind == JsonValueKind.String)
                    return nestedCode.GetString();
            }
        }

        return null;
    }

    /// <summary>
    /// Upper case, digits and underscores only, at least three characters: covers
    /// <c>TEMPLATE_IN_USE</c>, <c>BULK_MAX_10</c> and single-word codes such as
    /// <c>UNAUTHORIZED</c>, never a human-readable sentence.
    /// </summary>
    private static readonly Regex CodeShape = new("^[A-Z][A-Z0-9_]{2,}$", RegexOptions.CultureInvariant);

    /// <summary>
    /// Bodies come as <c>{error:"CODE", message}</c>, <c>{error:"text", code:"CODE"}</c>
    /// (rate limits, CodedError) or <c>{error:{code, message}}</c> (reminders); some
    /// carry only a human-readable <c>error</c> string and no code at all.
    /// <c>code</c> wins; <c>error</c> counts only when it is shaped like a code.
    /// </summary>
    internal static string? ExtractErrorCode(JsonElement? body)
    {
        if (body is not { ValueKind: JsonValueKind.Object } b) return null;

        if (b.TryGetProperty("code", out var direct) && direct.ValueKind == JsonValueKind.String)
            return direct.GetString();

        if (b.TryGetProperty("error", out var error))
        {
            if (error.ValueKind == JsonValueKind.String)
            {
                var text = error.GetString();
                return text != null && CodeShape.IsMatch(text) ? text : null;
            }

            if (error.ValueKind == JsonValueKind.Object &&
                error.TryGetProperty("code", out var nestedCode) &&
                nestedCode.ValueKind == JsonValueKind.String)
            {
                return nestedCode.GetString();
            }
        }

        return null;
    }

    private static double? ExtractRetryAfter(JsonElement? body, GeneratedMultimap? headers)
    {
        if (body is { ValueKind: JsonValueKind.Object } b)
        {
            if (b.TryGetProperty("retry_after_seconds", out var direct) && direct.ValueKind == JsonValueKind.Number)
                return direct.GetDouble();

            if (b.TryGetProperty("error", out var error) && error.ValueKind == JsonValueKind.Object &&
                error.TryGetProperty("retry_after_seconds", out var nested) && nested.ValueKind == JsonValueKind.Number)
            {
                return nested.GetDouble();
            }
        }

        var headerValue = FindHeaderIgnoreCase(headers, "Retry-After");
        if (headerValue == null) return null;

        if (double.TryParse(headerValue, NumberStyles.Float, CultureInfo.InvariantCulture, out var n))
            return n;

        // Retry-After may also be an HTTP date.
        if (DateTimeOffset.TryParseExact(headerValue, "r", CultureInfo.InvariantCulture, DateTimeStyles.AssumeUniversal, out var at))
            return Math.Max(0, Math.Ceiling((at - DateTimeOffset.UtcNow).TotalSeconds));

        return null;
    }

    /// <summary>Reads the standard <c>RateLimit-*</c> headers; <c>X-RateLimit-*</c> is not sent by the server and is ignored.</summary>
    private static RateLimitInfo? ExtractRateLimitInfo(GeneratedMultimap? headers)
    {
        var limit = IntHeader(headers, "RateLimit-Limit");
        var remaining = IntHeader(headers, "RateLimit-Remaining");
        var reset = IntHeader(headers, "RateLimit-Reset");
        var policy = FindHeaderIgnoreCase(headers, "RateLimit-Policy");
        if (limit == null && remaining == null && reset == null && policy == null) return null;
        return new RateLimitInfo { Limit = limit, Remaining = remaining, Reset = reset, Policy = policy };
    }

    private static int? IntHeader(GeneratedMultimap? headers, string name) =>
        int.TryParse(FindHeaderIgnoreCase(headers, name), NumberStyles.Integer, CultureInfo.InvariantCulture, out var value) ? value : null;

    private static string? FindHeaderIgnoreCase(GeneratedMultimap? headers, string name)
    {
        if (headers == null) return null;
        foreach (var kvp in headers)
        {
            if (!string.Equals(kvp.Key, name, StringComparison.OrdinalIgnoreCase) || kvp.Value == null) continue;
            foreach (var value in kvp.Value)
            {
                if (!string.IsNullOrWhiteSpace(value)) return value.Trim();
            }
        }
        return null;
    }
}
