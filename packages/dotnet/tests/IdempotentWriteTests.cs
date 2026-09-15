using System.Reflection;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;
using ImzalaSdk;
using Moq;
using Xunit;
using GeneratedApiException = ImzalaApiClient.Client.ApiException;
using GeneratedMultimap = ImzalaApiClient.Client.Multimap<string, string>;

namespace ImzalaSdk.Tests;

/// <summary>
/// A write is retried only when it carries an Idempotency-Key and the server
/// answered 429, exactly once, waiting Retry-After (capped at 60 s). Mirrors
/// the Node <c>unwrapIdempotentWrite</c> tests and Java IdempotentWriteTest.
/// Waits go through an injected delay, so nothing here really sleeps.
/// </summary>
public class IdempotentWriteTests
{
    private readonly List<TimeSpan> _waits = new();
    private readonly List<CancellationToken> _waitTokens = new();

    private RetryConfig Retry(int maxRetries = 2) => new()
    {
        MaxRetries = maxRetries,
        RetryBaseDelayMs = 300,
        Delay = (wait, ct) =>
        {
            _waits.Add(wait);
            _waitTokens.Add(ct);
            return Task.CompletedTask;
        },
    };

    private static GeneratedApiException Status(int status, string body, GeneratedMultimap? headers = null) =>
        new(status, $"Error calling SomeMethod: {body}", body, headers ?? new GeneratedMultimap());

    private static GeneratedApiException TooMany(string? retryAfterSeconds)
    {
        var headers = new GeneratedMultimap();
        if (retryAfterSeconds != null)
        {
            headers.Add("Retry-After", retryAfterSeconds);
        }
        return Status(429, """{"success":false,"code":"RATE_LIMIT_EXCEEDED"}""", headers);
    }

    private static ApiV1DemandsPost201Response Created() => new(true, new CreatedDemand(id: Guid.NewGuid()));

    private Task<CreatedDemand> RunWrite(Queue<object> script, Counter counter, string? key, long? maxWaitMs = null, CancellationToken ct = default)
    {
        Func<Task<ApiV1DemandsPost201Response>> call = () =>
        {
            counter.Value++;
            var next = script.Dequeue();
            return next is Exception ex ? Task.FromException<ApiV1DemandsPost201Response>(ex) : Task.FromResult((ApiV1DemandsPost201Response)next);
        };
        return maxWaitMs is { } cap
            ? Http.UnwrapIdempotentWrite(call, r => r.Success, r => r.Data, key, Retry(), ct, cap)
            : Http.UnwrapIdempotentWrite(call, r => r.Success, r => r.Data, key, Retry(), ct);
    }

    private sealed class Counter
    {
        public int Value;
    }

    // --- Http.UnwrapIdempotentWrite ---

    [Fact]
    public async Task A_write_without_an_idempotency_key_is_never_retried_on_429()
    {
        var n = new Counter();
        await Assert.ThrowsAsync<ImzalaRateLimitError>(() => RunWrite(new Queue<object>(new object[] { TooMany("1"), Created() }), n, null));
        Assert.Equal(1, n.Value);
        Assert.Empty(_waits);
    }

    [Fact]
    public async Task An_empty_idempotency_key_counts_as_no_key()
    {
        var n = new Counter();
        await Assert.ThrowsAsync<ImzalaRateLimitError>(() => RunWrite(new Queue<object>(new object[] { TooMany("1"), Created() }), n, ""));
        Assert.Equal(1, n.Value);
    }

    [Fact]
    public async Task A_write_with_an_idempotency_key_is_retried_exactly_once_on_429()
    {
        var n = new Counter();
        var result = await RunWrite(new Queue<object>(new object[] { TooMany("2"), Created() }), n, "order-42");
        Assert.NotEqual(Guid.Empty, result.Id);
        Assert.Equal(2, n.Value);
        Assert.Equal(new[] { TimeSpan.FromSeconds(2) }, _waits);
    }

    [Fact]
    public async Task A_second_429_is_thrown_not_retried_again()
    {
        var n = new Counter();
        await Assert.ThrowsAsync<ImzalaRateLimitError>(() => RunWrite(new Queue<object>(new object[] { TooMany("1"), TooMany("1"), Created() }), n, "order-42"));
        Assert.Equal(2, n.Value);
    }

    [Theory]
    [InlineData(500)]
    [InlineData(503)]
    [InlineData(409)]
    [InlineData(400)]
    public async Task Non_429_errors_are_not_retried_even_5xx(int status)
    {
        var n = new Counter();
        var err = await Assert.ThrowsAnyAsync<ImzalaError>(() =>
            RunWrite(new Queue<object>(new object[] { Status(status, """{"success":false}"""), Created() }), n, "order-42"));
        Assert.Equal(status, err.StatusCode);
        Assert.Equal(1, n.Value);
        Assert.Empty(_waits);
    }

    [Fact]
    public async Task A_network_error_is_not_retried()
    {
        var n = new Counter();
        await Assert.ThrowsAsync<ImzalaError>(() =>
            RunWrite(new Queue<object>(new object[] { new HttpRequestException("reset"), Created() }), n, "order-42"));
        Assert.Equal(1, n.Value);
    }

    [Fact]
    public async Task Throws_instead_of_waiting_when_retry_after_exceeds_the_cap()
    {
        var n = new Counter();
        var err = await Assert.ThrowsAsync<ImzalaRateLimitError>(() => RunWrite(new Queue<object>(new object[] { TooMany("61"), Created() }), n, "order-42"));
        Assert.Equal(61, err.RetryAfter);
        Assert.Equal(1, n.Value);
        Assert.Empty(_waits);
    }

    [Fact]
    public async Task Exactly_sixty_seconds_is_still_waited()
    {
        var n = new Counter();
        await RunWrite(new Queue<object>(new object[] { TooMany("60"), Created() }), n, "order-42");
        Assert.Equal(2, n.Value);
        Assert.Equal(new[] { TimeSpan.FromSeconds(60) }, _waits);
    }

    [Fact]
    public async Task A_custom_cap_is_honoured()
    {
        var n = new Counter();
        await Assert.ThrowsAsync<ImzalaRateLimitError>(() => RunWrite(new Queue<object>(new object[] { TooMany("2"), Created() }), n, "order-42", maxWaitMs: 1000));
        Assert.Equal(1, n.Value);
    }

    [Fact]
    public async Task Without_retry_after_the_base_delay_is_waited()
    {
        var n = new Counter();
        await RunWrite(new Queue<object>(new object[] { TooMany(null), Created() }), n, "order-42");
        Assert.Equal(new[] { TimeSpan.FromMilliseconds(300) }, _waits);
    }

    [Fact]
    public async Task The_cancellation_token_reaches_the_wait()
    {
        using var cts = new CancellationTokenSource();
        var n = new Counter();
        await RunWrite(new Queue<object>(new object[] { TooMany("1"), Created() }), n, "order-42", ct: cts.Token);
        Assert.Equal(new[] { cts.Token }, _waitTokens);
    }

    [Fact]
    public async Task A_cancelled_wait_does_not_send_the_retry()
    {
        using var cts = new CancellationTokenSource();
        cts.Cancel();
        var calls = 0;
        var retry = new RetryConfig { RetryBaseDelayMs = 300 };
        Func<Task<ApiV1DemandsPost201Response>> call = () =>
        {
            calls++;
            return Task.FromException<ApiV1DemandsPost201Response>(TooMany("1"));
        };

        await Assert.ThrowsAnyAsync<OperationCanceledException>(() =>
            Http.UnwrapIdempotentWrite(call, r => r.Success, r => r.Data, "order-42", retry, cts.Token));
        Assert.Equal(1, calls);
    }

    // --- header-unsafe keys, through the real generated client ---

    public static IEnumerable<object[]> UnsafeKeys() => new[]
    {
        new object[] { "sipariş-1" },
        new object[] { "a\nb" },
        new object[] { "a\r\nX-Evil: 1" },
        new object[] { "tab\tkey" },
    };

    private static async Task AssertRejectedLocally(string key, Func<Imzala, Task> call)
    {
        var listener = new System.Net.Sockets.TcpListener(System.Net.IPAddress.Loopback, 0);
        listener.Start();
        try
        {
            var port = ((System.Net.IPEndPoint)listener.LocalEndpoint).Port;
            var imzala = new Imzala("imz_test", $"http://127.0.0.1:{port}", timeoutMs: 2000, maxRetries: 0);

            var err = await Assert.ThrowsAsync<ImzalaValidationError>(() => call(imzala));

            Assert.Null(err.StatusCode);
            Assert.IsType<ArgumentException>(err.InnerException);
            // Nothing reached the server: not even a TCP connection was opened.
            Assert.False(listener.Pending(), $"a connection was opened for key {key}");
        }
        finally
        {
            listener.Stop();
        }
    }

    [Theory]
    [MemberData(nameof(UnsafeKeys))]
    public Task Demands_CreateAsync_rejects_a_header_unsafe_key_before_sending(string key) =>
        AssertRejectedLocally(key, imzala => imzala.Demands.CreateAsync(
            new CreateDemandRequest(templateId: Guid.NewGuid(), partyMapping: new List<PartyMappingInput>()), key));

    [Theory]
    [MemberData(nameof(UnsafeKeys))]
    public Task Demands_UploadDocumentAsync_rejects_a_header_unsafe_key_before_sending(string key) =>
        AssertRejectedLocally(key, imzala => imzala.Demands.UploadDocumentAsync(Upload(key)));

    [Theory]
    [MemberData(nameof(UnsafeKeys))]
    public Task Timestamps_CreateAsync_rejects_a_header_unsafe_key_before_sending(string key) =>
        AssertRejectedLocally(key, imzala => imzala.Timestamps.CreateAsync(new CreateTimestampParams
        {
            Content = "hello"u8.ToArray(),
            FileName = "eser.pdf",
            IdempotencyKey = key,
        }));

    [Fact]
    public async Task A_printable_ascii_key_with_spaces_is_accepted()
    {
        var n = new Counter();
        await RunWrite(new Queue<object>(new object[] { Created() }), n, "SIP 2026/42-a");
        Assert.Equal(1, n.Value);
    }

    // --- GET auto-retry wait cap ---

    [Fact]
    public async Task Get_throws_without_waiting_when_retry_after_exceeds_sixty_seconds()
    {
        var id = Guid.NewGuid();
        var api = new Mock<ITemplatesApi>();
        api.SetupSequence(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>()))
            .ThrowsAsync(TooMany("61"))
            .ReturnsAsync(new ApiV1TemplatesIdGet200Response(true, new TemplateDetail(id: id)));

        await Assert.ThrowsAsync<ImzalaRateLimitError>(() => new TemplatesResource(api.Object, Retry()).GetAsync(id));
        api.Verify(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>()), Times.Once);
        Assert.Empty(_waits);
    }

    [Fact]
    public async Task Get_still_waits_exactly_sixty_seconds_through_the_injected_delay_with_the_callers_token()
    {
        using var cts = new CancellationTokenSource();
        var id = Guid.NewGuid();
        var api = new Mock<ITemplatesApi>();
        api.SetupSequence(a => a.ApiV1TemplatesIdGetAsync(id, It.IsAny<CancellationToken>()))
            .ThrowsAsync(TooMany("60"))
            .ReturnsAsync(new ApiV1TemplatesIdGet200Response(true, new TemplateDetail(id: id)));

        var result = await new TemplatesResource(api.Object, Retry()).GetAsync(id, cts.Token);

        Assert.Equal(id, result.Id);
        Assert.Equal(new[] { TimeSpan.FromSeconds(60) }, _waits);
        Assert.Equal(new[] { cts.Token }, _waitTokens);
    }

    // --- resources that honour a key ---

    private DemandsResource Demands(Mock<IDemandsApi> api) => new(api.Object, new Mock<IRemindersApi>().Object, Retry());

    [Fact]
    public async Task Demands_CreateAsync_with_key_retries_once_and_sends_the_key_in_its_own_slot()
    {
        var body = new CreateDemandRequest(templateId: Guid.NewGuid(), partyMapping: new List<PartyMappingInput>());
        var api = new Mock<IDemandsApi>();
        api.SetupSequence(a => a.ApiV1DemandsPostAsync(body, "order-42", It.IsAny<CancellationToken>()))
            .ThrowsAsync(TooMany("1"))
            .ReturnsAsync(Created());

        await Demands(api).CreateAsync(body, "order-42");

        api.Verify(a => a.ApiV1DemandsPostAsync(body, "order-42", It.IsAny<CancellationToken>()), Times.Exactly(2));
        api.VerifyNoOtherCalls();
    }

    [Fact]
    public async Task Demands_CreateAsync_without_key_is_a_single_attempt_and_sends_no_key()
    {
        var body = new CreateDemandRequest(templateId: Guid.NewGuid(), partyMapping: new List<PartyMappingInput>());
        var api = new Mock<IDemandsApi>();
        api.Setup(a => a.ApiV1DemandsPostAsync(body, It.IsAny<string?>(), It.IsAny<CancellationToken>())).ThrowsAsync(TooMany("1"));

        await Assert.ThrowsAsync<ImzalaRateLimitError>(() => Demands(api).CreateAsync(body));

        api.Verify(a => a.ApiV1DemandsPostAsync(body, null, It.IsAny<CancellationToken>()), Times.Once);
        api.VerifyNoOtherCalls();
    }

    [Fact]
    public async Task Demands_CreateBulkAsync_is_never_retried_on_429()
    {
        var body = new ApiV1DemandsBulkPostRequest(templateId: Guid.NewGuid(), rows: new List<ApiV1DemandsBulkPostRequestRowsInner>());
        var api = new Mock<IDemandsApi>();
        api.Setup(a => a.ApiV1DemandsBulkPostAsync(It.IsAny<ApiV1DemandsBulkPostRequest>(), It.IsAny<Guid?>(), It.IsAny<CancellationToken>())).ThrowsAsync(TooMany("1"));

        await Assert.ThrowsAsync<ImzalaRateLimitError>(() => Demands(api).CreateBulkAsync(body));

        api.Verify(a => a.ApiV1DemandsBulkPostAsync(It.IsAny<ApiV1DemandsBulkPostRequest>(), It.IsAny<Guid?>(), It.IsAny<CancellationToken>()), Times.Once);
        Assert.Empty(_waits);
    }

    [Fact]
    public void CreateBulkAsync_and_Contacts_CreateAsync_accept_no_idempotency_key()
    {
        foreach (var method in new[]
                 {
                     typeof(DemandsResource).GetMethods().Where(m => m.Name == nameof(DemandsResource.CreateBulkAsync)),
                     typeof(ContactsResource).GetMethods().Where(m => m.Name == nameof(ContactsResource.CreateAsync)),
                 }.SelectMany(m => m))
        {
            Assert.DoesNotContain(method.GetParameters(), p => p.ParameterType == typeof(string));
        }
    }

    [Fact]
    public async Task Contacts_CreateAsync_is_never_retried_on_429()
    {
        var api = new Mock<IContactsApi>();
        api.Setup(a => a.ApiV1ContactsPostAsync(It.IsAny<ApiV1ContactsPostRequest>(), It.IsAny<CancellationToken>())).ThrowsAsync(TooMany("1"));

        await Assert.ThrowsAsync<ImzalaRateLimitError>(() =>
            new ContactsResource(api.Object, Retry()).CreateAsync(new ApiV1ContactsPostRequest(firstName: "Ayşe", lastName: "Yılmaz")));

        api.Verify(a => a.ApiV1ContactsPostAsync(It.IsAny<ApiV1ContactsPostRequest>(), It.IsAny<CancellationToken>()), Times.Once);
        Assert.Empty(_waits);
    }

    private static UploadDemandParams Upload(string? key) => new()
    {
        Files = new[] { new FileInput { Content = "hello"u8.ToArray(), FileName = "a.pdf", ContentType = "application/pdf" } },
        Parties = new[] { new UploadPartyInput { FirstName = "Ayşe", LastName = "Yılmaz", Email = "ayse@example.com" } },
        IdempotencyKey = key,
    };

    private static void SetupUpload(Mock<IDemandsApi> api, Queue<object> script, List<string> fileContents, List<string?> keys)
    {
        api.Setup(a => a.ApiV1DemandsUploadPostAsync(
                It.IsAny<List<FileParameter>>(), It.IsAny<string>(), It.IsAny<string?>(), It.IsAny<string?>(),
                It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<Guid?>(), It.IsAny<string?>(),
                It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<CancellationToken>()))
            .Returns<List<FileParameter>, string, string?, string?, string?, string?, Guid?, string?, string?, string?, CancellationToken>(
                (files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss, ct) =>
                {
                    // Reading the stream here consumes it, like a real HTTP send would.
                    using var reader = new StreamReader(files[0].Content);
                    fileContents.Add(reader.ReadToEnd());
                    keys.Add(idempotencyKey);
                    var next = script.Dequeue();
                    return next is Exception ex
                        ? Task.FromException<ApiV1DemandsUploadPost201Response>(ex)
                        : Task.FromResult((ApiV1DemandsUploadPost201Response)next);
                });
    }

    [Fact]
    public async Task UploadDocumentAsync_with_key_retries_once_with_the_key_in_slot_three_and_fresh_file_bytes()
    {
        var api = new Mock<IDemandsApi>();
        var contents = new List<string>();
        var keys = new List<string?>();
        SetupUpload(api, new Queue<object>(new object[] { TooMany("1"), new ApiV1DemandsUploadPost201Response(true, new CreatedDemandUpload(id: Guid.NewGuid())) }), contents, keys);

        await Demands(api).UploadDocumentAsync(Upload("upload-7"));

        Assert.Equal(new[] { "upload-7", "upload-7" }, keys);
        // The retry must not resend a stream the first attempt already consumed.
        Assert.Equal(new[] { "hello", "hello" }, contents);
    }

    [Fact]
    public async Task UploadDocumentAsync_without_key_is_a_single_attempt()
    {
        var api = new Mock<IDemandsApi>();
        var contents = new List<string>();
        var keys = new List<string?>();
        SetupUpload(api, new Queue<object>(new object[] { TooMany("1"), new ApiV1DemandsUploadPost201Response(true, new CreatedDemandUpload(id: Guid.NewGuid())) }), contents, keys);

        await Assert.ThrowsAsync<ImzalaRateLimitError>(() => Demands(api).UploadDocumentAsync(Upload(null)));

        Assert.Equal(new string?[] { null }, keys);
    }

    [Fact]
    public async Task Timestamps_CreateAsync_with_key_retries_once_with_the_key_in_slot_two_and_fresh_file_bytes()
    {
        var api = new Mock<ITimestampsApi>();
        var contents = new List<string>();
        var keys = new List<string?>();
        var script = new Queue<object>(new object[] { TooMany("1"), new ApiV1TimestampsPost201Response(true, new TimestampRecord(id: Guid.NewGuid())) });
        api.Setup(a => a.ApiV1TimestampsPostAsync(It.IsAny<FileParameter>(), It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<CancellationToken>()))
            .Returns<FileParameter, string?, string?, string?, string?, CancellationToken>((file, idempotencyKey, description, ownerFirstName, ownerLastName, ct) =>
            {
                using var reader = new StreamReader(file.Content);
                contents.Add(reader.ReadToEnd());
                keys.Add(idempotencyKey);
                var next = script.Dequeue();
                return next is Exception ex
                    ? Task.FromException<ApiV1TimestampsPost201Response>(ex)
                    : Task.FromResult((ApiV1TimestampsPost201Response)next);
            });

        await new TimestampsResource(api.Object, Retry()).CreateAsync(new CreateTimestampParams
        {
            Content = "hello"u8.ToArray(),
            FileName = "eser.pdf",
            IdempotencyKey = "stamp-9",
        });

        Assert.Equal(new[] { "stamp-9", "stamp-9" }, keys);
        Assert.Equal(new[] { "hello", "hello" }, contents);
    }

    [Fact]
    public async Task Timestamps_CreateAsync_without_key_is_a_single_attempt()
    {
        var api = new Mock<ITimestampsApi>();
        api.Setup(a => a.ApiV1TimestampsPostAsync(It.IsAny<FileParameter>(), It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<CancellationToken>()))
            .ThrowsAsync(TooMany("1"));

        await Assert.ThrowsAsync<ImzalaRateLimitError>(() => new TimestampsResource(api.Object, Retry()).CreateAsync(new CreateTimestampParams
        {
            Content = "hello"u8.ToArray(),
            FileName = "eser.pdf",
        }));

        api.Verify(a => a.ApiV1TimestampsPostAsync(It.IsAny<FileParameter>(), null, It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<CancellationToken>()), Times.Once);
    }
}
