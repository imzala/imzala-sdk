using System.Security.Cryptography;
using System.Text;
using ImzalaApiClient.Api;
using ImzalaApiClient.Model;
using GeneratedConfiguration = ImzalaApiClient.Client.Configuration;

namespace ImzalaSdk;

/// <summary>
/// imzala.org server-side SDK — an ergonomic, hand-written facade over the
/// vendored (openapi-generator <c>csharp</c>/<c>httpclient</c>) client in
/// <c>ImzalaApiClient</c>. Every method unwraps the <c>{success, data}</c>
/// response envelope and throws a typed <see cref="ImzalaError"/> (see
/// <c>Errors.cs</c>) on failure, instead of returning raw generated-client
/// exceptions.
///
/// <b>Server-only.</b> Constructed with a raw <c>X-API-Key</c> — never embed
/// this in a client-side (WASM/MAUI/Unity) application where the key could be
/// extracted from the binary.
///
/// <example>
/// <code>
/// var imzala = new Imzala(Environment.GetEnvironmentVariable("IMZALA_API_KEY")!);
/// var demand = await imzala.Demands.CreateAsync(new CreateDemandRequest(templateId, partyMapping));
/// </code>
/// </example>
/// </summary>
public sealed class Imzala
{
    private const string DefaultBaseUrl = "https://api-prd.imzala.org";
    private const int DefaultTimeoutMs = 30_000;

    /// <summary><c>imzala.Templates.*</c> — list/get/usage.</summary>
    public TemplatesResource Templates { get; }

    /// <summary><c>imzala.Demands.*</c> — create/get/addItems/uploadDocument/sendReminder.</summary>
    public DemandsResource Demands { get; }

    /// <summary><c>imzala.Embed.*</c> — embedded (iframe) signing session minting.</summary>
    public EmbedResource Embed { get; }

    /// <summary><c>imzala.Timestamps.*</c> — RFC 3161 timestamping.</summary>
    public TimestampsResource Timestamps { get; }

    private readonly AccountResource _account;

    /// <param name="apiKey"><c>imz_&lt;64 hex&gt;</c> — from Dashboard → Geliştirici → API Anahtarları, or Hesap Ayarları → API Anahtarları.</param>
    /// <param name="baseUrl">Defaults to <c>https://api-prd.imzala.org</c>. Use <c>https://test-api.imzala.org</c> for the test environment.</param>
    /// <param name="timeoutMs">Per-request timeout, in milliseconds. Defaults to 30000.</param>
    public Imzala(string apiKey, string baseUrl = DefaultBaseUrl, int timeoutMs = DefaultTimeoutMs)
    {
        if (string.IsNullOrEmpty(apiKey))
        {
            throw new ArgumentException("new Imzala(apiKey) — apiKey is required.", nameof(apiKey));
        }

        var configuration = new GeneratedConfiguration
        {
            BasePath = baseUrl,
            Timeout = TimeSpan.FromMilliseconds(timeoutMs),
        };
        configuration.ApiKey["X-API-Key"] = apiKey;

        _account = new AccountResource(new AccountApi(configuration));
        var demandsApi = new DemandsApi(configuration);
        var remindersApi = new RemindersApi(configuration);
        var templatesApi = new TemplatesApi(configuration);
        var timestampsApi = new TimestampsApi(configuration);

        Templates = new TemplatesResource(templatesApi);
        Demands = new DemandsResource(demandsApi, remindersApi);
        Embed = new EmbedResource(demandsApi);
        Timestamps = new TimestampsResource(timestampsApi);
    }

    /// <summary>Returns the calling API key's owner info (id, email, name, workspace, remaining credits). Requires the <c>timestamps</c> scope.</summary>
    public Task<ApiV1MeGet200ResponseData> MeAsync(CancellationToken cancellationToken = default) =>
        _account.MeAsync(cancellationToken);

    /// <summary>
    /// Verifies an imzala.org webhook delivery's <c>X-Imzala-Signature-256</c> header.
    ///
    /// Mirrors the backend algorithm exactly (<c>src/services/webhook/WebhookSigner.ts</c>
    /// in imzala-service): <c>'sha256=' + HMAC-SHA256(rawBody, secret)</c> as
    /// lowercase hex, compared with a fixed-time equality check
    /// (<see cref="CryptographicOperations.FixedTimeEquals"/>).
    /// </summary>
    /// <param name="secret">the <c>whsec_&lt;64-hex&gt;</c> secret shown once when the webhook was created in the dashboard.</param>
    /// <param name="rawBody">the <b>exact, unparsed</b> request body bytes. Parsing the JSON and re-serializing it can change byte-for-byte content (key order, whitespace) and break verification — read the raw request body, don't re-serialize a deserialized model.</param>
    /// <param name="signatureHeader">the raw <c>X-Imzala-Signature-256</c> header value (e.g. <c>sha256=&lt;hex&gt;</c>).</param>
    /// <returns><c>true</c> if the signature is valid. Returns <c>false</c> — never throws — for a wrong signature, a malformed/missing header, or any other verification failure.</returns>
    public static bool VerifyWebhook(string secret, byte[] rawBody, string? signatureHeader)
    {
        if (string.IsNullOrEmpty(secret) || string.IsNullOrEmpty(signatureHeader))
        {
            return false;
        }

        try
        {
            using var hmac = new HMACSHA256(Encoding.UTF8.GetBytes(secret));
            var digest = hmac.ComputeHash(rawBody);
            var expected = "sha256=" + Convert.ToHexString(digest).ToLowerInvariant();

            var expectedBytes = Encoding.UTF8.GetBytes(expected);
            var actualBytes = Encoding.UTF8.GetBytes(signatureHeader);

            // CryptographicOperations.FixedTimeEquals returns false (does not
            // throw) when the spans have different lengths — unlike Node's
            // crypto.timingSafeEqual, no manual length pre-check is needed to
            // keep this method's "never throws" contract.
            return CryptographicOperations.FixedTimeEquals(expectedBytes, actualBytes);
        }
        catch
        {
            return false;
        }
    }

    /// <inheritdoc cref="VerifyWebhook(string, byte[], string?)"/>
    public static bool VerifyWebhook(string secret, string rawBody, string? signatureHeader) =>
        VerifyWebhook(secret, Encoding.UTF8.GetBytes(rawBody ?? string.Empty), signatureHeader);
}
