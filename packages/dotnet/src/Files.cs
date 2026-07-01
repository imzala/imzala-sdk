using System.Text.Json.Serialization;
using GeneratedFileParameter = ImzalaApiClient.Client.FileParameter;

namespace ImzalaSdk;

/// <summary>
/// .NET-native file input for multipart endpoints (<c>Demands.UploadDocumentAsync</c>,
/// <c>Timestamps.CreateAsync</c>). Servers hold file bytes as <c>byte[]</c> (from
/// <c>IFormFile</c>, <c>File.ReadAllBytes</c>, an upstream download, ...) — the
/// facade accepts bytes + filename and builds the vendored client's
/// <c>FileParameter</c> itself (unlike TS, which has to cast <c>node:buffer</c>'s
/// <c>File</c> to the DOM-lib-sourced generated type — the httpclient C#
/// generator's <c>FileParameter(filename, contentType, Stream)</c> constructor is
/// already ergonomic, no cast/shim needed).
/// </summary>
public sealed class FileInput
{
    /// <summary>Raw file bytes.</summary>
    public required byte[] Content { get; init; }

    /// <summary>Original filename, including extension (e.g. <c>"sozlesme.pdf"</c>). Required — the server infers processing (PDF vs image vs office doc) from the extension.</summary>
    public required string FileName { get; init; }

    /// <summary>MIME type. Best-effort inferred server-side from the extension when omitted.</summary>
    public string? ContentType { get; init; }

    /// <summary>Builds the vendored client's <c>FileParameter</c> from this input's bytes.</summary>
    internal GeneratedFileParameter ToFileParameter() =>
        new(FileName, ContentType ?? "application/octet-stream", new MemoryStream(Content));
}

/// <summary>One signing party for <c>Demands.UploadDocumentAsync</c>. Email or phone (or both) required per party.</summary>
public sealed class UploadPartyInput
{
    [JsonPropertyName("first_name")]
    public required string FirstName { get; init; }

    [JsonPropertyName("last_name")]
    public required string LastName { get; init; }

    [JsonPropertyName("email")]
    public string? Email { get; init; }

    /// <summary>E.164 format (e.g. <c>"+905551234567"</c>).</summary>
    [JsonPropertyName("phone")]
    public string? Phone { get; init; }
}

/// <summary>Parameters for <c>Demands.UploadDocumentAsync</c> — creates a demand directly from an uploaded document (no template).</summary>
public sealed class UploadDemandParams
{
    /// <summary>One document OR 1-20 images — merged server-side into a single PDF.</summary>
    public required IReadOnlyList<FileInput> Files { get; init; }

    public required IReadOnlyList<UploadPartyInput> Parties { get; init; }

    /// <summary>Reorders a multi-image upload — indices into <see cref="Files"/>.</summary>
    public IReadOnlyList<int>? Order { get; init; }

    public string? Title { get; init; }

    public string? Description { get; init; }
}

/// <summary>Parameters for <c>Timestamps.CreateAsync</c>.</summary>
public sealed class CreateTimestampParams
{
    public required byte[] Content { get; init; }

    public required string FileName { get; init; }

    public string? ContentType { get; init; }

    /// <summary>Client-generated idempotency key (UUID recommended) — replays within 5 minutes return the original result without spending a credit.</summary>
    public string? IdempotencyKey { get; init; }

    public string? Description { get; init; }

    public string? OwnerFirstName { get; init; }

    public string? OwnerLastName { get; init; }
}
