using System.Text;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;
using Moq;
using Xunit;

namespace ImzalaSdk.Tests;

/// <summary>
/// v1 lifecycle facade tests — mirrors the Node SDK's
/// <c>src/__tests__/lifecycle.test.ts</c>: mock the vendored client's
/// interface (IDemandsApi / ITemplatesApi) and assert each new facade method
/// (a) calls the right generated method with the right args, (b) unwraps the
/// <c>{success, data}</c> envelope to the inner data, and (c) — for the binary
/// downloads — materializes the <c>FileParameter</c> stream into a
/// <c>byte[]</c>.
/// </summary>
public class LifecycleTests
{
    private static readonly RetryConfig DefaultRetry = new();

    private static DemandsResource Demands(Mock<IDemandsApi> demands) =>
        new(demands.Object, new Mock<IRemindersApi>().Object, DefaultRetry);

    // ---- demands ----------------------------------------------------------

    [Fact]
    public async Task Demands_ListAsync_forwards_filters_and_unwraps_counts_only_data()
    {
        var demandId = Guid.NewGuid();
        var templateId = Guid.NewGuid();
        var mock = new Mock<IDemandsApi>();
        mock
            .Setup(a => a.ApiV1DemandsGetAsync(
                "PENDING", It.IsAny<string?>(), It.IsAny<DateOnly?>(), It.IsAny<DateOnly?>(),
                templateId, 1, 20, It.IsAny<string?>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1DemandsGet200Response(true, new ApiV1DemandsGet200ResponseData(
                demands: new List<ApiV1DemandsGet200ResponseDataDemandsInner>
                {
                    new(id: demandId, partiesTotal: 2, partiesSigned: 1),
                },
                total: 1,
                page: 1,
                limit: 20)));

        var result = await Demands(mock).ListAsync(status: "PENDING", templateId: templateId, page: 1, limit: 20);

        mock.Verify(a => a.ApiV1DemandsGetAsync(
            "PENDING", It.IsAny<string?>(), It.IsAny<DateOnly?>(), It.IsAny<DateOnly?>(),
            templateId, 1, 20, It.IsAny<string?>(), It.IsAny<CancellationToken>()), Times.Once);
        Assert.Equal(1, result.Total);
        Assert.Single(result.Demands);
        Assert.Equal(demandId, result.Demands[0].Id);
        Assert.Equal(2, result.Demands[0].PartiesTotal);
        Assert.Equal(1, result.Demands[0].PartiesSigned);
    }

    [Fact]
    public async Task Demands_GetTimelineAsync_unwraps_masked_events()
    {
        var id = Guid.NewGuid();
        var mock = new Mock<IDemandsApi>();
        mock
            .Setup(a => a.ApiV1DemandsIdTimelineGetAsync(id, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1DemandsIdTimelineGet200Response(true, new ApiV1DemandsIdTimelineGet200ResponseData(
                events: new List<ApiV1DemandsIdTimelineGet200ResponseDataEventsInner>
                {
                    new(id: "e1", eventType: "SIGNED", ipMasked: "1.2.3.***"),
                })));

        var result = await Demands(mock).GetTimelineAsync(id);

        Assert.Single(result.Events);
        Assert.Equal("1.2.3.***", result.Events[0].IpMasked);
        Assert.Equal("SIGNED", result.Events[0].EventType);
    }

    [Fact]
    public async Task Demands_CancelAsync_posts_the_reason_body_and_unwraps()
    {
        var id = Guid.NewGuid();
        var mock = new Mock<IDemandsApi>();
        mock
            .Setup(a => a.ApiV1DemandsIdCancelPostAsync(id, It.Is<ApiV1DemandsIdCancelPostRequest>(r => r.Reason == "vazgeçildi"), It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1DemandsIdCancelPost200Response(true, new ApiV1DemandsIdCancelPost200ResponseData(id: id, status: "CANCELLED")));

        var result = await Demands(mock).CancelAsync(id, "vazgeçildi");

        mock.Verify(a => a.ApiV1DemandsIdCancelPostAsync(id, It.Is<ApiV1DemandsIdCancelPostRequest>(r => r.Reason == "vazgeçildi"), It.IsAny<CancellationToken>()), Times.Once);
        Assert.Equal("CANCELLED", result.Status);
    }

    [Fact]
    public async Task Demands_ResendPartyAsync_targets_a_single_party()
    {
        var id = Guid.NewGuid();
        var partyId = Guid.NewGuid();
        var mock = new Mock<IDemandsApi>();
        mock
            .Setup(a => a.ApiV1DemandsIdPartiesPartyIdResendPostAsync(id, partyId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1DemandsIdPartiesPartyIdResendPost200Response(true, new ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData(sent: new List<string> { "email" })));

        var result = await Demands(mock).ResendPartyAsync(id, partyId);

        mock.Verify(a => a.ApiV1DemandsIdPartiesPartyIdResendPostAsync(id, partyId, It.IsAny<CancellationToken>()), Times.Once);
        Assert.Equal(new List<string> { "email" }, result.Sent);
    }

    [Fact]
    public async Task Demands_DeleteAsync_unwraps_deletion_result()
    {
        var id = Guid.NewGuid();
        var mock = new Mock<IDemandsApi>();
        mock
            .Setup(a => a.ApiV1DemandsIdDeleteAsync(id, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1TemplatesIdDelete200Response(true, new ApiV1TemplatesIdDelete200ResponseData(id: id, deleted: true)));

        var result = await Demands(mock).DeleteAsync(id);

        Assert.Equal(id, result.Id);
        Assert.True(result.Deleted);
    }

    [Fact]
    public async Task Demands_GetPdfAsync_returns_raw_bytes_from_the_FileParameter_stream()
    {
        var id = Guid.NewGuid();
        var bytes = Encoding.UTF8.GetBytes("%PDF-1.7 fake");
        var mock = new Mock<IDemandsApi>();
        mock
            .Setup(a => a.ApiV1DemandsIdPdfGetAsync(id, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new FileParameter(new MemoryStream(bytes)));

        var result = await Demands(mock).GetPdfAsync(id);

        Assert.Equal(bytes, result);
        Assert.Contains("%PDF-1.7", Encoding.UTF8.GetString(result));
    }

    [Fact]
    public async Task Demands_GetCertificateAsync_forwards_lang_and_returns_bytes()
    {
        var id = Guid.NewGuid();
        var bytes = Encoding.UTF8.GetBytes("%PDF cert");
        var mock = new Mock<IDemandsApi>();
        mock
            .Setup(a => a.ApiV1DemandsIdCertificateGetAsync(id, "en", It.IsAny<CancellationToken>()))
            .ReturnsAsync(new FileParameter(new MemoryStream(bytes)));

        var result = await Demands(mock).GetCertificateAsync(id, "en");

        mock.Verify(a => a.ApiV1DemandsIdCertificateGetAsync(id, "en", It.IsAny<CancellationToken>()), Times.Once);
        Assert.Equal(bytes, result);
    }

    // ---- templates --------------------------------------------------------

    [Fact]
    public async Task Templates_UpdateAsync_patches_metadata_and_unwraps()
    {
        var id = Guid.NewGuid();
        var mock = new Mock<ITemplatesApi>();
        mock
            .Setup(a => a.ApiV1TemplatesIdPatchAsync(id, It.Is<ApiV1TemplatesIdPatchRequest>(r => r.Name == "Yeni Ad"), It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1TemplatesIdPatch200Response(true, new ApiV1TemplatesIdPatch200ResponseData(id: id, name: "Yeni Ad")));

        var resource = new TemplatesResource(mock.Object, DefaultRetry);
        var result = await resource.UpdateAsync(id, name: "Yeni Ad");

        mock.Verify(a => a.ApiV1TemplatesIdPatchAsync(id, It.Is<ApiV1TemplatesIdPatchRequest>(r => r.Name == "Yeni Ad"), It.IsAny<CancellationToken>()), Times.Once);
        Assert.Equal("Yeni Ad", result.Name);
    }

    [Fact]
    public async Task Templates_DeleteAsync_soft_deletes_and_unwraps()
    {
        var id = Guid.NewGuid();
        var mock = new Mock<ITemplatesApi>();
        mock
            .Setup(a => a.ApiV1TemplatesIdDeleteAsync(id, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1TemplatesIdDelete200Response(true, new ApiV1TemplatesIdDelete200ResponseData(id: id, deleted: true)));

        var resource = new TemplatesResource(mock.Object, DefaultRetry);
        var result = await resource.DeleteAsync(id);

        Assert.Equal(id, result.Id);
        Assert.True(result.Deleted);
    }
}
