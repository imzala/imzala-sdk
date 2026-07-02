using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;
using ImzalaSdk;
using Moq;
using Xunit;

namespace ImzalaSdk.Tests;

/// <summary>
/// Envelope-unwrap tests — mirrors client.test.ts (B1, vi.spyOn on the
/// generated *Api.prototype) and test_client.py (B2, unittest.mock.patch.object
/// on the generated *Api methods): mock the vendored client's interface
/// (IDemandsApi/ITemplatesApi/IRemindersApi/ITimestampsApi/IAccountApi — Moq
/// needs an interface or virtual member, which the httpclient C# generator
/// conveniently emits for every Api class) and assert the resource classes
/// unwrap {success,data} to the inner data.
/// </summary>
public class ClientTests
{
    /// <summary>
    /// Default retry config (2 retries / 300ms base) for tests that don't exercise
    /// retry behavior themselves — see RetryTests.cs / PaginationTests.cs for those.
    /// </summary>
    private static readonly RetryConfig DefaultRetry = new();

    [Fact]
    public async Task Demands_GetAsync_unwraps_success_data_to_inner_data()
    {
        var id = Guid.NewGuid();
        var mockDemands = new Mock<IDemandsApi>();
        mockDemands
            .Setup(a => a.ApiV1DemandsIdGetAsync(id, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1DemandsIdGet200Response(true, new DemandStatus(id: id, status: DemandStatus.StatusEnum.PENDING)));
        var mockReminders = new Mock<IRemindersApi>();

        var resource = new DemandsResource(mockDemands.Object, mockReminders.Object, DefaultRetry);
        var result = await resource.GetAsync(id);

        Assert.Equal(id, result.Id);
        Assert.Equal(DemandStatus.StatusEnum.PENDING, result.Status);
    }

    [Fact]
    public async Task Me_calls_AccountApi_and_unwraps()
    {
        var userId = Guid.NewGuid();
        var mockAccount = new Mock<IAccountApi>();
        mockAccount
            .Setup(a => a.ApiV1MeGetAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1MeGet200Response(true, new ApiV1MeGet200ResponseData(id: userId, email: "a@b.com")));

        var account = new AccountResource(mockAccount.Object, DefaultRetry);
        var result = await account.MeAsync();

        Assert.Equal(userId, result.Id);
        Assert.Equal("a@b.com", result.Email);
    }

    [Fact]
    public async Task Templates_ListAsync_forwards_page_limit_and_unwraps()
    {
        var mockTemplates = new Mock<ITemplatesApi>();
        mockTemplates
            .Setup(a => a.ApiV1TemplatesGetAsync(2, 10, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1TemplatesGet200Response(true, new ApiV1TemplatesGet200ResponseData(
                templates: new List<TemplateSummary> { new(id: Guid.NewGuid()) },
                total: 1,
                page: 2,
                limit: 10)));

        var resource = new TemplatesResource(mockTemplates.Object, DefaultRetry);
        var result = await resource.ListAsync(page: 2, limit: 10);

        mockTemplates.Verify(a => a.ApiV1TemplatesGetAsync(2, 10, It.IsAny<CancellationToken>()), Times.Once);
        Assert.Single(result.Templates);
        Assert.Equal(1, result.Total);
    }

    [Fact]
    public async Task Demands_SendReminderAsync_routes_through_RemindersApi_not_DemandsApi()
    {
        var id = Guid.NewGuid();
        var mockDemands = new Mock<IDemandsApi>();
        var mockReminders = new Mock<IRemindersApi>();
        mockReminders
            .Setup(a => a.ApiV1DemandsIdRemindersPostAsync(id, It.Is<TriggerReminderRequest>(r => r.Force), It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1DemandsIdRemindersPost200Response(true, new ApiV1DemandsIdRemindersPost200ResponseData(demandId: id)));

        var resource = new DemandsResource(mockDemands.Object, mockReminders.Object, DefaultRetry);
        var result = await resource.SendReminderAsync(id, new TriggerReminderRequest(force: true));

        mockReminders.Verify(a => a.ApiV1DemandsIdRemindersPostAsync(id, It.Is<TriggerReminderRequest>(r => r.Force), It.IsAny<CancellationToken>()), Times.Once);
        mockDemands.VerifyNoOtherCalls();
        Assert.Equal(id, result.DemandId);
    }

    [Fact]
    public async Task Demands_SendReminderAsync_defaults_body_to_empty_request_when_omitted()
    {
        var id = Guid.NewGuid();
        var mockDemands = new Mock<IDemandsApi>();
        var mockReminders = new Mock<IRemindersApi>();
        mockReminders
            .Setup(a => a.ApiV1DemandsIdRemindersPostAsync(id, It.Is<TriggerReminderRequest>(r => r.Force == false), It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1DemandsIdRemindersPost200Response(true, new ApiV1DemandsIdRemindersPost200ResponseData(demandId: id)));

        var resource = new DemandsResource(mockDemands.Object, mockReminders.Object, DefaultRetry);
        await resource.SendReminderAsync(id);

        mockReminders.Verify(a => a.ApiV1DemandsIdRemindersPostAsync(id, It.IsAny<TriggerReminderRequest>(), It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task Embed_CreateSessionAsync_maps_partyId_and_unwraps()
    {
        var demandId = Guid.NewGuid();
        var partyId = Guid.NewGuid();
        var mockDemands = new Mock<IDemandsApi>();
        mockDemands
            .Setup(a => a.ApiV1DemandsIdEmbedSessionPostAsync(demandId, It.Is<ApiV1DemandsIdEmbedSessionPostRequest>(r => r.PartyId == partyId), It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1DemandsIdEmbedSessionPost200Response(true, new ApiV1DemandsIdEmbedSessionPost200ResponseData(embedToken: "tok", embedUrl: "https://e.imzala.org/embed/sign?token=tok")));

        var resource = new EmbedResource(mockDemands.Object);
        var result = await resource.CreateSessionAsync(demandId, partyId);

        Assert.Equal("tok", result.EmbedToken);
    }

    [Fact]
    public async Task Demands_UploadDocumentAsync_JSON_encodes_parties_order_and_builds_FileParameters()
    {
        var mockDemands = new Mock<IDemandsApi>();
        List<FileParameter>? capturedFiles = null;
        string? capturedParties = null;
        string? capturedOrder = null;

        mockDemands
            .Setup(a => a.ApiV1DemandsUploadPostAsync(
                It.IsAny<List<FileParameter>>(),
                It.IsAny<string>(),
                It.IsAny<string?>(),
                It.IsAny<string?>(),
                It.IsAny<string?>(),
                It.IsAny<CancellationToken>()))
            .Callback<List<FileParameter>, string, string?, string?, string?, CancellationToken>((files, parties, order, title, description, ct) =>
            {
                capturedFiles = files;
                capturedParties = parties;
                capturedOrder = order;
            })
            .ReturnsAsync(new ApiV1DemandsUploadPost201Response(true, new CreatedDemandUpload(id: Guid.NewGuid())));

        var resource = new DemandsResource(mockDemands.Object, new Mock<IRemindersApi>().Object, DefaultRetry);
        var result = await resource.UploadDocumentAsync(new UploadDemandParams
        {
            Files = new[] { new FileInput { Content = "hello"u8.ToArray(), FileName = "a.pdf", ContentType = "application/pdf" } },
            Parties = new[] { new UploadPartyInput { FirstName = "Ada", LastName = "Lovelace", Email = "ada@example.com" } },
            Order = new[] { 0 },
            Title = "Test",
        });

        Assert.NotNull(capturedFiles);
        Assert.Single(capturedFiles!);
        Assert.Equal("a.pdf", capturedFiles![0].Name);
        Assert.Equal("application/pdf", capturedFiles[0].ContentType);
        Assert.Equal("""[{"first_name":"Ada","last_name":"Lovelace","email":"ada@example.com"}]""", capturedParties);
        Assert.Equal("[0]", capturedOrder);
        Assert.NotEqual(Guid.Empty, result.Id);
    }

    [Fact]
    public async Task Timestamps_CreateAsync_builds_FileParameter_from_bytes_and_unwraps()
    {
        var mockTimestamps = new Mock<ITimestampsApi>();
        FileParameter? capturedFile = null;
        string? capturedIdempotencyKey = null;

        mockTimestamps
            .Setup(a => a.ApiV1TimestampsPostAsync(
                It.IsAny<FileParameter>(),
                It.IsAny<string?>(),
                It.IsAny<string?>(),
                It.IsAny<string?>(),
                It.IsAny<string?>(),
                It.IsAny<CancellationToken>()))
            .Callback<FileParameter, string?, string?, string?, string?, CancellationToken>((file, idempotencyKey, description, ownerFirstName, ownerLastName, ct) =>
            {
                capturedFile = file;
                capturedIdempotencyKey = idempotencyKey;
            })
            .ReturnsAsync(new ApiV1TimestampsPost201Response(true, new TimestampRecord(id: Guid.NewGuid(), fileSha256: "abc")));

        var resource = new TimestampsResource(mockTimestamps.Object);
        var result = await resource.CreateAsync(new CreateTimestampParams
        {
            Content = "hello"u8.ToArray(),
            FileName = "eser.pdf",
            IdempotencyKey = "idem-1",
        });

        Assert.Equal("eser.pdf", capturedFile!.Name);
        Assert.Equal("idem-1", capturedIdempotencyKey);
        Assert.Equal("abc", result.FileSha256);
    }

    [Fact]
    public async Task Throws_ImzalaError_when_server_returns_success_false_on_2xx()
    {
        var mockTemplates = new Mock<ITemplatesApi>();
        mockTemplates
            .Setup(a => a.ApiV1TemplatesIdGetAsync(It.IsAny<Guid>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1TemplatesIdGet200Response(false, null!));

        var resource = new TemplatesResource(mockTemplates.Object, DefaultRetry);

        await Assert.ThrowsAsync<ImzalaError>(() => resource.GetAsync(Guid.NewGuid()));
    }

    [Fact]
    public void Construction_requires_an_apiKey()
    {
        var ex = Assert.Throws<ArgumentException>(() => new Imzala(string.Empty));
        Assert.Contains("apiKey is required", ex.Message);
    }

    [Fact]
    public void Construction_defaults_baseUrl_to_prod()
    {
        var imzala = new Imzala("imz_test");
        Assert.NotNull(imzala.Templates);
        Assert.NotNull(imzala.Demands);
        Assert.NotNull(imzala.Embed);
        Assert.NotNull(imzala.Timestamps);
    }

    [Fact]
    public void Construction_honors_a_custom_baseUrl()
    {
        // Construction itself must not throw for a custom (e.g. test-environment) baseUrl.
        var exception = Record.Exception(() => new Imzala("imz_test", "https://test-api.imzala.org"));
        Assert.Null(exception);
    }
}
