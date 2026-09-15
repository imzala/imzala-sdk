using System.Text.RegularExpressions;
using ImzalaApiClient.Api;
using ImzalaApiClient.Model;
using ImzalaSdk;
using Moq;
using Newtonsoft.Json;
using Xunit;
using GeneratedApiException = ImzalaApiClient.Client.ApiException;
using GeneratedMultimap = ImzalaApiClient.Client.Multimap<string, string>;

namespace ImzalaSdk.Tests;

/// <summary>
/// Keeps the .NET error catalogue in step with the public spec and with the
/// Node SDK catalogue (packages/node/src/errorCodes.ts), and covers how codes,
/// Retry-After and RateLimit-* headers are read off an error response.
/// </summary>
public class ErrorCodesTests
{
    /// <summary>Same list as packages/node/src/__tests__/errorCodes.test.ts.</summary>
    private static readonly Dictionary<string, string> NotErrorCodes = new()
    {
        ["ANCHOR_TEXT_NOT_FOUND"] = "field layout diagnostic code inside a response body",
        ["CREATE_FAILED"] = "per-row result code in the bulk 200 response",
        ["DISPATCH_FAILED"] = "per-party invitation result code in a 200 response",
        ["DISPATCH_SKIPPED"] = "invitation result code in a 200 response",
        ["RECIPIENT_QUOTA_EXCEEDED"] = "per-party invitation result code in a 200 response",
        ["ON_ANCHOR_MISS_NOT_RELAXED"] = "warning in a 200 response, not an error",
        ["DEAD_LETTER"] = "webhook delivery status",
        ["DEMAND_ID"] = "shell variable in a curl example",
        ["ENVELOPE_DECISION_ENFORCE"] = "server feature flag name",
        ["FIELD_LAYOUT"] = "template kind value",
        ["FILLABLE_TYPES"] = "server constant name in prose",
        ["IMZALA_WEBHOOK_SECRET"] = "environment variable in a code sample",
        ["KVKK_CONSENT"] = "doc_kind value",
        ["KVKK_NOTICE"] = "doc_kind value",
        ["PRICE_LIST"] = "doc_kind value",
        ["WEBHOOK_TIMEOUT_MS"] = "server environment variable",
    };

    /// <summary>Prose also contains single upper-case words (API, PDF, KVKK), so the spec-to-catalogue direction only considers underscore tokens.</summary>
    private static readonly Regex CodeToken = new(@"\b[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)+\b", RegexOptions.CultureInvariant);

    /// <summary>Walks up from the test binary directory to the repository root, so the working directory does not matter.</summary>
    private static string RepoRoot()
    {
        var dir = new DirectoryInfo(AppContext.BaseDirectory);
        while (dir != null)
        {
            if (File.Exists(Path.Combine(dir.FullName, "spec", "openapi.v1.yaml")) &&
                File.Exists(Path.Combine(dir.FullName, "packages", "node", "src", "errorCodes.ts")))
            {
                return dir.FullName;
            }
            dir = dir.Parent;
        }
        throw new InvalidOperationException("repository root not found above " + AppContext.BaseDirectory);
    }

    private static readonly Lazy<string> SpecText = new(() => File.ReadAllText(Path.Combine(RepoRoot(), "spec", "openapi.v1.yaml")));

    private static bool InSpec(string token) =>
        Regex.IsMatch(SpecText.Value, @"\b" + Regex.Escape(token) + @"\b");

    private static Dictionary<string, string> NodeCatalogue()
    {
        var src = File.ReadAllText(Path.Combine(RepoRoot(), "packages", "node", "src", "errorCodes.ts"));
        var body = src.Split("export const IMZALA_ERROR_CODES = {", 2)[1].Split("} as const;", 2)[0];
        var result = new Dictionary<string, string>();
        foreach (System.Text.RegularExpressions.Match m in Regex.Matches(body, @"^\s*([A-Z][A-Z0-9_]*):\s*'([^']*)',", RegexOptions.Multiline))
        {
            result[m.Groups[1].Value] = m.Groups[2].Value;
        }
        Assert.NotEmpty(result);
        return result;
    }

    // --- catalogue <-> spec ---

    [Fact]
    public void Every_catalogued_code_appears_in_the_public_spec()
    {
        var missing = ErrorCodes.Codes.Keys.Where(c => !InSpec(c)).ToList();
        Assert.Empty(missing);
    }

    [Fact]
    public void Every_code_like_token_in_the_spec_is_catalogued_or_excluded()
    {
        var unclassified = CodeToken.Matches(SpecText.Value)
            .Select(m => m.Value)
            .Distinct()
            .Where(t => !ErrorCodes.IsKnown(t) && !NotErrorCodes.ContainsKey(t))
            .ToList();
        Assert.Empty(unclassified);
    }

    [Fact]
    public void The_exclusion_list_has_no_stale_or_overlapping_entries()
    {
        foreach (var token in NotErrorCodes.Keys)
        {
            Assert.True(InSpec(token), token);
            Assert.False(ErrorCodes.IsKnown(token), token);
        }
    }

    [Fact]
    public void Every_description_is_a_non_empty_single_line_without_an_em_dash()
    {
        foreach (var (code, text) in ErrorCodes.Codes)
        {
            Assert.False(string.IsNullOrWhiteSpace(text), code);
            Assert.DoesNotContain("\n", text);
            Assert.DoesNotContain("\u2014", text);
        }
    }

    [Fact]
    public void Unknown_or_missing_codes_return_null_instead_of_throwing()
    {
        Assert.Null(ErrorCodes.Describe("NEVER_A_REAL_CODE"));
        Assert.Null(ErrorCodes.Describe(null));
        Assert.False(ErrorCodes.IsKnown(null));
        Assert.False(ErrorCodes.IsKnown("ToString"));
        Assert.Equal(ErrorCodes.Codes["TEMPLATE_IN_USE"], ErrorCodes.Describe("TEMPLATE_IN_USE"));
    }

    [Fact]
    public void The_catalogue_is_read_only()
    {
        Assert.False(ErrorCodes.Codes is IDictionary<string, string> { IsReadOnly: false });
    }

    // --- catalogue <-> Node SDK ---

    [Fact]
    public void Same_code_set_as_the_node_catalogue()
    {
        Assert.Equal(NodeCatalogue().Keys.OrderBy(k => k, StringComparer.Ordinal), ErrorCodes.Codes.Keys.OrderBy(k => k, StringComparer.Ordinal));
        Assert.Equal(73, ErrorCodes.Codes.Count);
    }

    [Fact]
    public void Same_descriptions_as_the_node_catalogue()
    {
        var node = NodeCatalogue();
        var drifted = ErrorCodes.Codes.Where(e => !node.TryGetValue(e.Key, out var v) || v != e.Value).Select(e => e.Key).ToList();
        Assert.Empty(drifted);
    }

    // --- error body shapes ---

    private static GeneratedApiException Api(int status, string body, GeneratedMultimap? headers = null) =>
        new(status, $"Error calling SomeMethod: {body}", body, headers ?? new GeneratedMultimap());

    private static GeneratedMultimap Headers(params (string Name, string Value)[] values)
    {
        var headers = new GeneratedMultimap();
        foreach (var (name, value) in values)
        {
            headers.Add(name, value);
        }
        return headers;
    }

    [Fact]
    public void Error_code_with_message_reads_the_code_from_error()
    {
        var e = ErrorMapper.Map(Api(400, """{"success":false,"error":"PAGE_ID_REQUIRED","message":"Each item must have an integer page_id"}"""));
        Assert.Equal("PAGE_ID_REQUIRED", e.Code);
        Assert.Equal(ErrorCodes.Codes["PAGE_ID_REQUIRED"], e.CodeDescription);
    }

    [Fact]
    public void Human_text_with_code_reads_the_code_never_the_text()
    {
        var e = ErrorMapper.Map(Api(400, """{"success":false,"error":"Geçersiz sayfa numarası (page >= 1 olmalı)","code":"INVALID_PAGE"}"""));
        Assert.Equal("INVALID_PAGE", e.Code);
        Assert.Equal("Geçersiz sayfa numarası (page >= 1 olmalı)", e.Message);
        Assert.Equal(ErrorCodes.Codes["INVALID_PAGE"], e.CodeDescription);
    }

    [Fact]
    public void Code_wins_over_a_code_shaped_error_string()
    {
        var e = ErrorMapper.Map(Api(429, """{"success":false,"error":"TOO_MANY_REQUESTS","code":"RECIPIENT_RESEND_LIMIT"}"""));
        Assert.Equal("RECIPIENT_RESEND_LIMIT", e.Code);
    }

    [Fact]
    public void Nested_error_object_reads_the_nested_code()
    {
        var e = ErrorMapper.Map(Api(409, """{"success":false,"error":{"code":"DEMAND_NOT_DISPATCHABLE","message":"x"}}"""));
        Assert.Equal("DEMAND_NOT_DISPATCHABLE", e.Code);
    }

    [Fact]
    public void A_single_word_code_in_error_is_still_a_code()
    {
        Assert.Equal("UNAUTHORIZED", ErrorMapper.Map(Api(401, """{"error":"UNAUTHORIZED"}""")).Code);
    }

    [Fact]
    public void A_plain_human_readable_error_string_is_not_reported_as_a_code()
    {
        var e = ErrorMapper.Map(Api(404, """{"success":false,"error":"Sözleşme bulunamadı"}"""));
        Assert.Null(e.Code);
        Assert.Equal("Sözleşme bulunamadı", e.Message);
        Assert.Null(e.CodeDescription);
    }

    [Fact]
    public void Short_or_lowercase_error_strings_are_not_codes()
    {
        Assert.Null(ErrorMapper.Map(Api(400, """{"error":"OK"}""")).Code);
        Assert.Null(ErrorMapper.Map(Api(400, """{"error":"not_found"}""")).Code);
        Assert.Null(ErrorMapper.Map(Api(400, """{"error":"BAD CODE"}""")).Code);
    }

    [Fact]
    public void Unknown_code_is_kept_without_a_description()
    {
        var e = ErrorMapper.Map(Api(409, """{"success":false,"code":"SOME_FUTURE_CODE"}"""));
        Assert.Equal("SOME_FUTURE_CODE", e.Code);
        Assert.Null(e.CodeDescription);
    }

    [Fact]
    public void Constructing_an_error_directly_fills_the_code_description()
    {
        Assert.Equal(ErrorCodes.Codes["TEMPLATE_IN_USE"], new ImzalaError("x", 409, null, "TEMPLATE_IN_USE").CodeDescription);
        Assert.Null(new ImzalaError("x").CodeDescription);
        Assert.Equal(ErrorCodes.Codes["RATE_LIMITED"], new ImzalaRateLimitError("x", 429, null, "RATE_LIMITED").CodeDescription);
    }

    // --- 2xx + success:false through the real generated model ---

    /// <summary>
    /// Known limit, same as Python, PHP and Java: the generated model for a 200
    /// declares only <c>success</c> and <c>data</c>, and the generated client's
    /// deserializer drops unknown properties. The body's <c>error</c> and
    /// <c>code</c> are gone before the facade sees the response, so the code
    /// cannot be read on this path. The call still fails loudly.
    /// </summary>
    [Fact]
    public async Task Success_false_on_a_2xx_with_the_real_generated_model_still_throws_but_has_no_code()
    {
        var settings = new ImzalaApiClient.Client.ApiClient().SerializerSettings;
        var model = JsonConvert.DeserializeObject<ApiV1TemplatesIdGet200Response>(
            """{"success":false,"error":"Geçersiz istek","code":"VALIDATION_FAIL"}""", settings);
        Assert.NotNull(model);
        Assert.False(model!.Success);

        var id = Guid.NewGuid();
        var api = new Mock<ITemplatesApi>();
        api.Setup(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>())).ReturnsAsync(model);

        var e = await Assert.ThrowsAsync<ImzalaError>(() => new TemplatesResource(api.Object, new RetryConfig { MaxRetries = 0 }).GetAsync(id));
        Assert.Null(e.Code);
    }

    // --- local parameter validation in the generated client ---

    [Fact]
    public async Task A_missing_required_parameter_caught_by_the_real_generated_client_is_a_validation_error_without_status()
    {
        // The generated client refuses a null body before any request is sent
        // (the port is never contacted), with a made-up 400 status.
        var imzala = new Imzala("imz_test", "http://127.0.0.1:9");

        var e = await Assert.ThrowsAsync<ImzalaValidationError>(() => imzala.Contacts.CreateAsync(null!));

        Assert.Null(e.StatusCode);
        Assert.IsType<GeneratedApiException>(e.InnerException);
        Assert.Contains("Missing required parameter", e.Message);
    }

    [Fact]
    public void An_argument_exception_from_the_generated_client_is_a_validation_error_without_status()
    {
        var inner = new ArgumentOutOfRangeException("limit");
        var e = ErrorMapper.Map(inner);

        var validation = Assert.IsType<ImzalaValidationError>(e);
        Assert.Null(validation.StatusCode);
        Assert.Same(inner, validation.InnerException);
    }

    [Fact]
    public void A_real_400_response_stays_a_plain_error_with_its_status()
    {
        var e = ErrorMapper.Map(Api(400, """{"success":false,"error":"Missing required parameter","code":"VALIDATION_FAIL"}"""));
        Assert.IsNotType<ImzalaValidationError>(e);
        Assert.Equal(400, e.StatusCode);
    }

    // --- rate limit ---

    [Fact]
    public void Attaches_the_description_and_reads_retry_after_from_the_header()
    {
        var e = ErrorMapper.Map(Api(429, """{"success":false,"error":"Çok fazla istek","code":"RATE_LIMIT_EXCEEDED"}""", Headers(("retry-after", "30"))));
        var rl = Assert.IsType<ImzalaRateLimitError>(e);
        Assert.Equal("RATE_LIMIT_EXCEEDED", rl.Code);
        Assert.Equal(ErrorCodes.Codes["RATE_LIMIT_EXCEEDED"], rl.CodeDescription);
        Assert.Equal(30, rl.RetryAfter);
    }

    [Fact]
    public void An_http_date_retry_after_is_converted_to_seconds()
    {
        var at = DateTimeOffset.UtcNow.AddSeconds(30).ToString("R");
        var rl = Assert.IsType<ImzalaRateLimitError>(ErrorMapper.Map(Api(429, """{"success":false}""", Headers(("Retry-After", at)))));
        Assert.InRange(rl.RetryAfter!.Value, 28, 30);
    }

    [Fact]
    public void A_past_http_date_retry_after_is_zero_not_negative()
    {
        var at = DateTimeOffset.UtcNow.AddSeconds(-120).ToString("R");
        var rl = Assert.IsType<ImzalaRateLimitError>(ErrorMapper.Map(Api(429, """{"success":false}""", Headers(("Retry-After", at)))));
        Assert.Equal(0, rl.RetryAfter);
    }

    [Fact]
    public void An_unparseable_retry_after_is_null()
    {
        var rl = Assert.IsType<ImzalaRateLimitError>(ErrorMapper.Map(Api(429, """{"success":false}""", Headers(("Retry-After", "yakında")))));
        Assert.Null(rl.RetryAfter);
    }

    [Fact]
    public void Reads_retry_after_seconds_from_the_body_when_there_is_no_header()
    {
        var rl = Assert.IsType<ImzalaRateLimitError>(ErrorMapper.Map(Api(429,
            """{"success":false,"error":"Çok fazla istek gönderildi. Lütfen bir dakika bekleyin.","code":"RATE_LIMIT_EXCEEDED","retry_after_seconds":60}""",
            Headers(("ratelimit-limit", "60"), ("ratelimit-policy", "60;w=60")))));
        Assert.Equal("RATE_LIMIT_EXCEEDED", rl.Code);
        Assert.Equal(60, rl.RetryAfter);
    }

    [Fact]
    public void Reads_the_standard_rate_limit_headers_and_ignores_x_rate_limit()
    {
        var rl = Assert.IsType<ImzalaRateLimitError>(ErrorMapper.Map(Api(429,
            """{"success":false,"code":"RATE_LIMIT_EXCEEDED","retry_after_seconds":60}""",
            Headers(("X-RateLimit-Limit", "999"), ("RateLimit-Limit", "5"), ("RateLimit-Remaining", "0"), ("RateLimit-Reset", "42"), ("RateLimit-Policy", "5;w=60")))));
        Assert.NotNull(rl.RateLimit);
        Assert.Equal(5, rl.RateLimit!.Limit);
        Assert.Equal(0, rl.RateLimit.Remaining);
        Assert.Equal(42, rl.RateLimit.Reset);
        Assert.Equal("5;w=60", rl.RateLimit.Policy);
    }

    [Fact]
    public void Header_names_are_read_case_insensitively()
    {
        var rl = Assert.IsType<ImzalaRateLimitError>(ErrorMapper.Map(Api(429, """{"success":false}""",
            Headers(("ratelimit-limit", "60"), ("RETRY-AFTER", "7")))));
        Assert.Equal(60, rl.RateLimit!.Limit);
        Assert.Null(rl.RateLimit.Remaining);
        Assert.Null(rl.RateLimit.Policy);
        Assert.Equal(7, rl.RetryAfter);
    }

    [Fact]
    public void Only_x_rate_limit_headers_leave_rate_limit_null()
    {
        var rl = Assert.IsType<ImzalaRateLimitError>(ErrorMapper.Map(Api(429, """{"success":false}""",
            Headers(("X-RateLimit-Limit", "60"), ("X-RateLimit-Remaining", "0")))));
        Assert.Null(rl.RateLimit);
    }

    [Fact]
    public void Leaves_rate_limit_null_when_the_server_sent_no_rate_limit_headers()
    {
        var rl = Assert.IsType<ImzalaRateLimitError>(ErrorMapper.Map(Api(429, """{"success":false,"error":{"code":"RATE_LIMITED"}}""")));
        Assert.Null(rl.RateLimit);
        Assert.Equal("RATE_LIMITED", rl.Code);
    }

    [Fact]
    public void The_existing_rate_limit_constructor_still_works()
    {
        var rl = new ImzalaRateLimitError("x", 429, null, null, 5.0, null);
        Assert.Equal(5.0, rl.RetryAfter);
        Assert.Null(rl.RateLimit);
    }
}
