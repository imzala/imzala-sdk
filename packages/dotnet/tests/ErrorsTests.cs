using GeneratedApiException = ImzalaApiClient.Client.ApiException;
using GeneratedMultimap = ImzalaApiClient.Client.Multimap<string, string>;
using ImzalaSdk;
using Xunit;

namespace ImzalaSdk.Tests;

/// <summary>
/// Mirrors errors.test.ts (B1) / test_errors.py (B2) — constructs a
/// generated-client ApiException the way the vendored httpclient library
/// throws it (status + raw-JSON-text body + headers) and asserts
/// ErrorMapper.Map(...) produces the right ImzalaError subclass.
/// </summary>
public class ErrorsTests
{
    private static GeneratedApiException FakeApiException(int status, string jsonBody, GeneratedMultimap? headers = null) =>
        new(status, $"Error calling SomeMethod: {jsonBody}", jsonBody, headers ?? new GeneratedMultimap());

    [Fact]
    public void Maps_401_to_ImzalaAuthError()
    {
        var err = ErrorMapper.Map(FakeApiException(401, """{"success":false,"error":"INVALID_API_KEY","message":"Invalid API key"}"""));

        var auth = Assert.IsType<ImzalaAuthError>(err);
        Assert.IsAssignableFrom<ImzalaError>(auth);
        Assert.Equal(401, auth.StatusCode);
        Assert.Equal("INVALID_API_KEY", auth.Code);
        Assert.Equal("Invalid API key", auth.Message);
    }

    [Fact]
    public void Maps_403_to_ImzalaAuthError()
    {
        var err = ErrorMapper.Map(FakeApiException(403, """{"success":false,"error":"INSUFFICIENT_SCOPE"}"""));

        var auth = Assert.IsType<ImzalaAuthError>(err);
        Assert.Equal(403, auth.StatusCode);
        Assert.Equal("INSUFFICIENT_SCOPE", auth.Code);
    }

    [Fact]
    public void Maps_429_to_ImzalaRateLimitError_reading_retryAfter_from_nested_error_object()
    {
        var err = ErrorMapper.Map(FakeApiException(429, """{"success":false,"error":{"code":"RATE_LIMITED","message":"Too many requests","retry_after_seconds":42}}"""));

        var rateLimited = Assert.IsType<ImzalaRateLimitError>(err);
        Assert.IsAssignableFrom<ImzalaError>(rateLimited);
        Assert.Equal(42, rateLimited.RetryAfter);
        Assert.Equal("RATE_LIMITED", rateLimited.Code);
        Assert.Equal("Too many requests", rateLimited.Message);
    }

    [Fact]
    public void Maps_429_retryAfter_from_RetryAfter_header_when_body_omits_it()
    {
        var headers = new GeneratedMultimap();
        headers.Add("Retry-After", "30");

        var err = ErrorMapper.Map(FakeApiException(429, """{"success":false,"error":"RATE_LIMITED"}""", headers));

        var rateLimited = Assert.IsType<ImzalaRateLimitError>(err);
        Assert.Equal(30, rateLimited.RetryAfter);
    }

    [Theory]
    [InlineData("NaN")]
    [InlineData("Infinity")]
    [InlineData("-5")]
    public void Ignores_an_unusable_RetryAfter_header(string value)
    {
        var headers = new GeneratedMultimap();
        headers.Add("Retry-After", value);

        var err = ErrorMapper.Map(FakeApiException(429, """{"success":false,"error":"RATE_LIMITED"}""", headers));

        var rateLimited = Assert.IsType<ImzalaRateLimitError>(err);
        Assert.Null(rateLimited.RetryAfter);
    }

    [Fact]
    public void Maps_422_to_ImzalaValidationError()
    {
        var err = ErrorMapper.Map(FakeApiException(422, """{"success":false,"message":"Invalid payload"}"""));

        var validation = Assert.IsType<ImzalaValidationError>(err);
        Assert.IsAssignableFrom<ImzalaError>(validation);
        Assert.Equal(422, validation.StatusCode);
        Assert.Equal("Invalid payload", validation.Message);
    }

    [Fact]
    public void Falls_back_to_base_ImzalaError_for_unmapped_statuses_404_500()
    {
        var err404 = ErrorMapper.Map(FakeApiException(404, """{"success":false,"error":"DEMAND_NOT_FOUND"}"""));
        Assert.IsType<ImzalaError>(err404);
        Assert.IsNotType<ImzalaAuthError>(err404);
        Assert.IsNotType<ImzalaRateLimitError>(err404);
        Assert.IsNotType<ImzalaValidationError>(err404);
        Assert.Equal(404, err404.StatusCode);
        Assert.Equal("DEMAND_NOT_FOUND", err404.Code);

        var err500 = ErrorMapper.Map(FakeApiException(500, """{"success":false}"""));
        Assert.Equal(500, err500.StatusCode);
    }

    [Fact]
    public void Wraps_a_plain_exception_as_a_base_ImzalaError()
    {
        var err = ErrorMapper.Map(new InvalidOperationException("ECONNREFUSED"));

        Assert.IsType<ImzalaError>(err);
        Assert.IsNotType<ImzalaAuthError>(err);
        Assert.Equal("ECONNREFUSED", err.Message);
        Assert.Null(err.StatusCode);
        Assert.IsType<InvalidOperationException>(err.InnerException);
    }

    [Fact]
    public void Passes_an_already_typed_ImzalaError_through_unchanged()
    {
        var original = new ImzalaValidationError("bad payload", statusCode: 422);
        Assert.Same(original, ErrorMapper.Map(original));
    }

    [Fact]
    public void Every_subclass_is_an_ImzalaError_and_an_Exception()
    {
        ImzalaError[] errors =
        [
            new ImzalaAuthError("a"),
            new ImzalaRateLimitError("b"),
            new ImzalaValidationError("c"),
        ];

        foreach (var err in errors)
        {
            Assert.IsAssignableFrom<ImzalaError>(err);
            Assert.IsAssignableFrom<Exception>(err);
        }
    }

    [Fact]
    public void Malformed_JSON_body_falls_back_to_the_generated_exception_message()
    {
        var err = ErrorMapper.Map(FakeApiException(500, "not json at all"));

        Assert.Equal(500, err.StatusCode);
        Assert.Contains("Error calling SomeMethod", err.Message);
    }
    [Theory]
    [InlineData(503, "DOCUMENT_VERIFICATION_DISABLED", "DOCUMENT_VERIFICATION_DISABLED")]
    [InlineData(400, "VERIFICATION_CONTEXT_REQUIRED", "VERIFICATION_POLICY_CONFLICT")]
    [InlineData(400, "FUTURE_COLLECTION_ERROR", "future reason")]
    public void Collection_envelopes_preserve_wrapper_and_origin(int status, string code, string reason)
    {
        var body = System.Text.Json.JsonSerializer.Serialize(new { success = false, error = reason, code });
        var err = ErrorMapper.Map(FakeApiException(status, body));
        Assert.IsType<ImzalaError>(err);
        Assert.Equal(status, err.StatusCode);
        Assert.Equal(code, err.Code);
        Assert.Equal(reason, err.Message);
    }
    [Fact]
    public void Collection_properties_preserve_nullable_wire_values_and_constructor_ABI()
    {
        var id = Guid.Parse("11111111-1111-4111-8111-111111111111");
        var single = new ImzalaApiClient.Model.CreateDemandRequest(templateId: id, partyMapping: new());
        single.DocumentCollectionEnabled = true;
        single.DocumentCollectionTemplateId = id;
        single.DocumentCollectionTiming = ImzalaApiClient.Model.CreateDemandRequest.DocumentCollectionTimingEnum.AFTERSIGNING;
        single.DocumentCollectionBlockSigning = false;
        var bulk = new ImzalaApiClient.Model.ApiV1DemandsBulkPostRequestOptions();
        bulk.DocumentCollectionEnabled = true;
        bulk.DocumentCollectionTemplateId = id;
        bulk.DocumentCollectionTiming = ImzalaApiClient.Model.ApiV1DemandsBulkPostRequestOptions.DocumentCollectionTimingEnum.AFTERSIGNING;
        bulk.DocumentCollectionBlockSigning = false;
        foreach (var json in new[] { single.ToJson(), bulk.ToJson() })
        {
            var body = Newtonsoft.Json.Linq.JObject.Parse(json);
            Assert.True(body["document_collection_enabled"]!.ToObject<bool>());
            Assert.Equal(id.ToString(), body["document_collection_template_id"]!.ToObject<string>());
            Assert.Equal("AFTER_SIGNING", body["document_collection_timing"]!.ToObject<string>());
            Assert.False(body["document_collection_block_signing"]!.ToObject<bool>());
        }
        single.DocumentCollectionEnabled = null;
        single.DocumentCollectionTemplateId = null;
        single.DocumentCollectionTiming = null;
        single.DocumentCollectionBlockSigning = null;
        var nullBody = Newtonsoft.Json.Linq.JObject.Parse(single.ToJson());
        foreach (var field in new[] { "document_collection_enabled", "document_collection_template_id", "document_collection_timing", "document_collection_block_signing" })
            Assert.Equal(Newtonsoft.Json.Linq.JTokenType.Null, nullBody[field]!.Type);
        Assert.Equal(19, typeof(ImzalaApiClient.Model.CreateDemandRequest).GetConstructors().Single(c => c.GetParameters().Length > 0).GetParameters().Length);
        Assert.Equal(9, typeof(ImzalaApiClient.Model.ApiV1DemandsBulkPostRequestOptions).GetConstructors().Single(c => c.GetParameters().Length > 0).GetParameters().Length);
    }
}
