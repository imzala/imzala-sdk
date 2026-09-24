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

    /// <summary>E.164 format (e.g. <c>"+905551112233"</c>).</summary>
    [JsonPropertyName("phone")]
    public string? Phone { get; init; }

    /// <summary>
    /// Field template role for this party, sent as <c>template_party_id</c>. Required on
    /// every party when <see cref="UploadDemandParams.FieldTemplateId"/> is set; each role
    /// of the template must be mapped exactly once.
    /// </summary>
    [JsonPropertyName("template_party_id")]
    public Guid? TemplatePartyId { get; init; }
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

    /// <summary>
    /// Makes the upload safe to retry: a second request with the same key does not
    /// create a second demand. With a key, one retry is made after a 429.
    /// </summary>
    public string? IdempotencyKey { get; init; }

    /// <summary>
    /// Field template (<c>kind: FIELD_LAYOUT</c>) whose layout is applied to the upload.
    /// The upload must then be a single PDF, and every party needs
    /// <see cref="UploadPartyInput.TemplatePartyId"/>. The layout is resolved before the
    /// demand is created or credit is spent; a 422 creates nothing. Dry-run first with
    /// <c>FieldTemplates.PreviewLayoutAsync</c>.
    /// </summary>
    public Guid? FieldTemplateId { get; init; }

    /// <summary>
    /// <c>"block"</c> or <c>"drop"</c>; only with a field template. Can tighten the
    /// template, never relax it: omitted means <c>block</c>; <c>drop</c> applies only if
    /// every affected field is already set to drop in the template (otherwise
    /// <c>block</c> is used and the response carries an <c>ON_ANCHOR_MISS_NOT_RELAXED</c>
    /// warning). Signature fields are never dropped.
    /// </summary>
    public string? OnAnchorMiss { get; init; }

    /// <summary>
    /// Sends signing invitations in the same request. <b>Off by default on this
    /// endpoint.</b> <c>"true"</c> or <c>"all"</c> uses every channel, <c>"email"</c>
    /// limits it to e-mail, <c>"sms"</c> to phone channels (SMS and WhatsApp),
    /// <c>"false"</c> sends nothing. It can only narrow: a channel switched off in the
    /// demand's or party's notification settings is not turned back on.
    /// </summary>
    public string? SendInvitations { get; init; }

    /// <summary>
    /// Deliberately bypass the duplicate check (<c>DUPLICATE_SUSPECTED</c>). Only
    /// meaningful for calls without an idempotency key.
    /// </summary>
    public bool Force { get; init; }
}

/// <summary>Parameters for <c>Demands.Documents.UploadAsync</c>: one file as one document of a multi-document envelope.</summary>
public sealed class UploadEnvelopeDocumentParams
{
    /// <summary>Exactly one file per document. Call <c>UploadAsync</c> again for each further document.</summary>
    public required FileInput File { get; init; }

    public required string Title { get; init; }

    /// <summary>
    /// Required. Sent as the <c>idempotency_key</c> multipart field (not as a
    /// header). Printable ASCII only. Uploading again with the same key creates no
    /// new document; the earlier document is returned instead. Checked by
    /// <c>UploadAsync</c> before anything is sent.
    /// </summary>
    public required string IdempotencyKey { get; init; }

    /// <summary>CONTRACT, KVKK_NOTICE, KVKK_CONSENT, PREINFO, PRICE_LIST or OTHER. Server default: OTHER.</summary>
    public string? DocKind { get; init; }

    /// <summary>Server default: <c>true</c>.</summary>
    public bool? IsRequired { get; init; }
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
