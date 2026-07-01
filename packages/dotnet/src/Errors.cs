using System.Text.Json;
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

    /// <summary>Machine-readable error code from the response envelope, when present.</summary>
    public string? Code { get; }

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

/// <summary>Rate limited (429). <see cref="RetryAfter"/> is seconds, when the server provided one.</summary>
public sealed class ImzalaRateLimitError : ImzalaError
{
    public double? RetryAfter { get; }

    public ImzalaRateLimitError(string message, int? statusCode = null, string? body = null, string? code = null, double? retryAfter = null, Exception? innerException = null)
        : base(message, statusCode, body, code, innerException)
    {
        RetryAfter = retryAfter;
    }
}

/// <summary>Request payload failed validation (422).</summary>
public sealed class ImzalaValidationError : ImzalaError
{
    public ImzalaValidationError(string message, int? statusCode = null, string? body = null, string? code = null, Exception? innerException = null)
        : base(message, statusCode, body, code, innerException)
    {
    }
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
                    return new ImzalaRateLimitError(message, status, bodyText, code, retryAfter, apiEx);
                case 422:
                    return new ImzalaValidationError(message, status, bodyText, code, apiEx);
                default:
                    return new ImzalaError(message, status, bodyText, code, apiEx);
            }
        }

        return new ImzalaError(err.Message, null, null, null, err);
    }

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

    internal static string? ExtractErrorCode(JsonElement? body)
    {
        if (body is not { ValueKind: JsonValueKind.Object } b) return null;

        if (b.TryGetProperty("error", out var error))
        {
            if (error.ValueKind == JsonValueKind.String)
                return error.GetString();

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
        if (headerValue != null && double.TryParse(headerValue, out var n))
            return n;

        return null;
    }

    private static string? FindHeaderIgnoreCase(GeneratedMultimap? headers, string name)
    {
        if (headers == null) return null;
        foreach (var kvp in headers)
        {
            if (string.Equals(kvp.Key, name, StringComparison.OrdinalIgnoreCase) && kvp.Value.Count > 0)
                return kvp.Value[0];
        }
        return null;
    }
}
