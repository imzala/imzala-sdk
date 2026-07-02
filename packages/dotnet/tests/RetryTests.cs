using ImzalaApiClient.Api;
using ImzalaApiClient.Model;
using ImzalaSdk;
using Moq;
using Xunit;
using GeneratedApiException = ImzalaApiClient.Client.ApiException;
using GeneratedMultimap = ImzalaApiClient.Client.Multimap<string, string>;

namespace ImzalaSdk.Tests;

/// <summary>
/// Safe auto-retry tests — mirrors <c>retry.test.ts</c> (TS, B1) and
/// <c>test_retry.py</c> (Python, B2). Uses a tiny <see cref="RetryConfig.RetryBaseDelayMs"/>
/// (1ms) so the exponential-backoff+jitter sleeps in <c>Http.UnwrapRetryableGet</c> keep
/// the suite fast (no fake-timer/mocked-sleep plumbing needed — total added wall time is
/// a handful of milliseconds).
/// </summary>
public class RetryTests
{
    private static GeneratedApiException FakeApiException(int status, string jsonBody) =>
        new(status, $"Error calling SomeMethod: {jsonBody}", jsonBody, new GeneratedMultimap());

    private static readonly RetryConfig FastRetry = new() { MaxRetries = 2, RetryBaseDelayMs = 1 };
    private static readonly RetryConfig NoRetry = new() { MaxRetries = 0, RetryBaseDelayMs = 1 };

    [Fact]
    public async Task GET_429_then_429_then_200_succeeds_call_count_is_three()
    {
        var id = Guid.NewGuid();
        var mockTemplates = new Mock<ITemplatesApi>();
        mockTemplates
            .SetupSequence(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>()))
            .ThrowsAsync(FakeApiException(429, """{"success":false,"error":"RATE_LIMITED"}"""))
            .ThrowsAsync(FakeApiException(429, """{"success":false,"error":"RATE_LIMITED"}"""))
            .ReturnsAsync(new ApiV1TemplatesIdGet200Response(true, new TemplateDetail(id: id)));

        var resource = new TemplatesResource(mockTemplates.Object, FastRetry);
        var result = await resource.GetAsync(id);

        Assert.Equal(id, result.Id);
        mockTemplates.Verify(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>()), Times.Exactly(3));
    }

    [Fact]
    public async Task GET_5xx_is_retried_and_succeeds()
    {
        var id = Guid.NewGuid();
        var mockTemplates = new Mock<ITemplatesApi>();
        mockTemplates
            .SetupSequence(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>()))
            .ThrowsAsync(FakeApiException(503, """{"success":false,"error":"SERVER_ERROR"}"""))
            .ReturnsAsync(new ApiV1TemplatesIdGet200Response(true, new TemplateDetail(id: id)));

        var resource = new TemplatesResource(mockTemplates.Object, FastRetry);
        var result = await resource.GetAsync(id);

        Assert.Equal(id, result.Id);
        mockTemplates.Verify(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>()), Times.Exactly(2));
    }

    [Fact]
    public async Task GET_non_429_4xx_is_not_retried()
    {
        var id = Guid.NewGuid();
        var mockTemplates = new Mock<ITemplatesApi>();
        mockTemplates
            .Setup(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>()))
            .ThrowsAsync(FakeApiException(404, """{"success":false,"error":"TEMPLATE_NOT_FOUND"}"""));

        var resource = new TemplatesResource(mockTemplates.Object, FastRetry);

        var err = await Assert.ThrowsAsync<ImzalaError>(() => resource.GetAsync(id));
        Assert.Equal(404, err.StatusCode);
        mockTemplates.Verify(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task MaxRetries_zero_disables_retry()
    {
        var id = Guid.NewGuid();
        var mockTemplates = new Mock<ITemplatesApi>();
        mockTemplates
            .Setup(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>()))
            .ThrowsAsync(FakeApiException(429, """{"success":false,"error":"RATE_LIMITED"}"""));

        var resource = new TemplatesResource(mockTemplates.Object, NoRetry);

        await Assert.ThrowsAsync<ImzalaRateLimitError>(() => resource.GetAsync(id));
        mockTemplates.Verify(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task GET_exhausts_retries_and_throws_typed_error()
    {
        var id = Guid.NewGuid();
        var mockTemplates = new Mock<ITemplatesApi>();
        mockTemplates
            .Setup(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>()))
            .ThrowsAsync(FakeApiException(503, """{"success":false,"error":"SERVER_ERROR"}"""));

        var resource = new TemplatesResource(mockTemplates.Object, FastRetry);

        var err = await Assert.ThrowsAsync<ImzalaError>(() => resource.GetAsync(id));
        Assert.Equal(503, err.StatusCode);
        // 1 initial attempt + MaxRetries (2) retries = 3 calls total.
        mockTemplates.Verify(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>()), Times.Exactly(3));
    }

    // --- SAFETY: POST/write methods must never be retried, even on a normally-retryable status ---

    [Fact]
    public async Task SAFETY_POST_Create_429_throws_immediately_no_retry()
    {
        var mockDemands = new Mock<IDemandsApi>();
        mockDemands
            .Setup(a => a.ApiV1DemandsPostAsync(It.IsAny<CreateDemandRequest>(), It.IsAny<CancellationToken>()))
            .ThrowsAsync(FakeApiException(429, """{"success":false,"error":"RATE_LIMITED"}"""));
        var mockReminders = new Mock<IRemindersApi>();

        var resource = new DemandsResource(mockDemands.Object, mockReminders.Object, FastRetry);
        var body = new CreateDemandRequest(templateId: Guid.NewGuid(), partyMapping: new List<PartyMappingInput>());

        await Assert.ThrowsAsync<ImzalaRateLimitError>(() => resource.CreateAsync(body));

        // A retried demands.create would create a duplicate demand — must be called exactly once.
        mockDemands.Verify(a => a.ApiV1DemandsPostAsync(It.IsAny<CreateDemandRequest>(), It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task SAFETY_POST_Create_503_throws_immediately_no_retry()
    {
        var mockDemands = new Mock<IDemandsApi>();
        mockDemands
            .Setup(a => a.ApiV1DemandsPostAsync(It.IsAny<CreateDemandRequest>(), It.IsAny<CancellationToken>()))
            .ThrowsAsync(FakeApiException(503, """{"success":false,"error":"SERVER_ERROR"}"""));
        var mockReminders = new Mock<IRemindersApi>();

        var resource = new DemandsResource(mockDemands.Object, mockReminders.Object, FastRetry);
        var body = new CreateDemandRequest(templateId: Guid.NewGuid(), partyMapping: new List<PartyMappingInput>());

        await Assert.ThrowsAsync<ImzalaError>(() => resource.CreateAsync(body));

        mockDemands.Verify(a => a.ApiV1DemandsPostAsync(It.IsAny<CreateDemandRequest>(), It.IsAny<CancellationToken>()), Times.Once);
    }
}
