using System.Security.Cryptography;
using System.Text;
using ImzalaSdk;
using Xunit;

namespace ImzalaSdk.Tests;

/// <summary>
/// Mirrors webhook.test.ts (B1) / test_webhook.py (B2) — the fixture below is
/// the same WebhookEnvelope shape (src/services/webhook/WebhookSigner.ts in
/// imzala-service): HMAC-SHA256(rawBody, secret) hex digest, 'sha256='-prefixed.
/// </summary>
public class WebhookTests
{
    private const string Secret = "whsec_1e6f1c2b3a4d5e6f7081920a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6";

    private static readonly string RawBody =
        """{"id":"evt_abc123","type":"demand.completed","created_at":"2026-07-01T00:00:00.000Z","data":{"demand_id":"d1"}}""";

    private static string Sign(string body, string key = Secret)
    {
        using var hmac = new HMACSHA256(Encoding.UTF8.GetBytes(key));
        var digest = hmac.ComputeHash(Encoding.UTF8.GetBytes(body));
        return "sha256=" + Convert.ToHexString(digest).ToLowerInvariant();
    }

    [Fact]
    public void Returns_true_for_a_correctly_signed_string_body()
    {
        Assert.True(Imzala.VerifyWebhook(Secret, RawBody, Sign(RawBody)));
    }

    [Fact]
    public void Returns_true_for_a_correctly_signed_byte_array_body_same_bytes_as_string_case()
    {
        var bytes = Encoding.UTF8.GetBytes(RawBody);
        Assert.True(Imzala.VerifyWebhook(Secret, bytes, Sign(RawBody)));
    }

    [Fact]
    public void Returns_false_for_the_wrong_secret()
    {
        Assert.False(Imzala.VerifyWebhook(
            "whsec_wrong_00000000000000000000000000000000000000000000000000",
            RawBody,
            Sign(RawBody)));
    }

    [Fact]
    public void Returns_false_when_the_body_was_tampered_with_after_signing()
    {
        var tampered = RawBody.Replace("d1", "d2-evil");
        Assert.False(Imzala.VerifyWebhook(Secret, tampered, Sign(RawBody)));
    }

    [Fact]
    public void Returns_false_for_a_malformed_header_missing_the_sha256_prefix_length_mismatch()
    {
        var digestOnly = Sign(RawBody).Replace("sha256=", "");
        Assert.False(Imzala.VerifyWebhook(Secret, RawBody, digestOnly));
    }

    [Fact]
    public void Returns_false_for_a_garbage_header_of_the_same_length_no_throw()
    {
        var valid = Sign(RawBody);
        var garbage = "sha256=" + new string('0', valid.Length - "sha256=".Length);
        Assert.False(Imzala.VerifyWebhook(Secret, RawBody, garbage));
    }

    [Fact]
    public void Returns_false_never_throws_for_a_missing_or_empty_header()
    {
        Assert.False(Imzala.VerifyWebhook(Secret, RawBody, null));
        Assert.False(Imzala.VerifyWebhook(Secret, RawBody, string.Empty));
    }

    [Fact]
    public void Returns_false_for_an_empty_secret_rather_than_signing_with_an_empty_key()
    {
        Assert.False(Imzala.VerifyWebhook(string.Empty, RawBody, Sign(RawBody, key: string.Empty)));
    }
}
