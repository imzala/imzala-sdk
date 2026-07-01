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
}
