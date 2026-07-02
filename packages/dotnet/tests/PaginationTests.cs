using ImzalaApiClient.Api;
using ImzalaApiClient.Model;
using ImzalaSdk;
using Moq;
using Xunit;

namespace ImzalaSdk.Tests;

/// <summary>
/// <c>TemplatesResource.ListAllAsync</c> pagination iterator tests — mirrors
/// <c>pagination.test.ts</c> (TS, B1) / <c>test_pagination.py</c> (Python, B2). Confirmed
/// real response shape from the generated model: <c>ApiV1TemplatesGet200ResponseData</c>
/// has <c>Templates</c>/<c>Total</c>/<c>Page</c>/<c>Limit</c>.
/// </summary>
public class PaginationTests
{
    private static readonly RetryConfig NoRetryNeeded = new();

    private static ApiV1TemplatesGet200Response Page(int page, int limit, int total, int count) =>
        new(true, new ApiV1TemplatesGet200ResponseData(
            templates: Enumerable.Range(0, count).Select(_ => new TemplateSummary(id: Guid.NewGuid())).ToList(),
            total: total,
            page: page,
            limit: limit));

    [Fact]
    public async Task ListAllAsync_walks_two_full_pages_plus_a_short_page_then_stops()
    {
        var mockTemplates = new Mock<ITemplatesApi>();
        mockTemplates.Setup(a => a.ApiV1TemplatesGetAsync(1, 2, It.IsAny<CancellationToken>())).ReturnsAsync(Page(page: 1, limit: 2, total: 5, count: 2));
        mockTemplates.Setup(a => a.ApiV1TemplatesGetAsync(2, 2, It.IsAny<CancellationToken>())).ReturnsAsync(Page(page: 2, limit: 2, total: 5, count: 2));
        mockTemplates.Setup(a => a.ApiV1TemplatesGetAsync(3, 2, It.IsAny<CancellationToken>())).ReturnsAsync(Page(page: 3, limit: 2, total: 5, count: 1));

        var resource = new TemplatesResource(mockTemplates.Object, NoRetryNeeded);
        var results = new List<TemplateSummary>();
        await foreach (var t in resource.ListAllAsync(limit: 2))
        {
            results.Add(t);
        }

        Assert.Equal(5, results.Count);
        mockTemplates.Verify(a => a.ApiV1TemplatesGetAsync(1, 2, It.IsAny<CancellationToken>()), Times.Once);
        mockTemplates.Verify(a => a.ApiV1TemplatesGetAsync(2, 2, It.IsAny<CancellationToken>()), Times.Once);
        mockTemplates.Verify(a => a.ApiV1TemplatesGetAsync(3, 2, It.IsAny<CancellationToken>()), Times.Once);
        // No 4th call — the short (count:1 < limit:2) page 3 must stop the loop.
        mockTemplates.Verify(a => a.ApiV1TemplatesGetAsync(4, 2, It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task ListAllAsync_stops_when_total_reached_exactly_on_a_full_page_no_extra_call()
    {
        var mockTemplates = new Mock<ITemplatesApi>();
        mockTemplates.Setup(a => a.ApiV1TemplatesGetAsync(1, 2, It.IsAny<CancellationToken>())).ReturnsAsync(Page(page: 1, limit: 2, total: 4, count: 2));
        mockTemplates.Setup(a => a.ApiV1TemplatesGetAsync(2, 2, It.IsAny<CancellationToken>())).ReturnsAsync(Page(page: 2, limit: 2, total: 4, count: 2));

        var resource = new TemplatesResource(mockTemplates.Object, NoRetryNeeded);
        var results = new List<TemplateSummary>();
        await foreach (var t in resource.ListAllAsync(limit: 2))
        {
            results.Add(t);
        }

        Assert.Equal(4, results.Count);
        // Proves no infinite loop: page 2 is an exactly-full page (count == limit), so only
        // the Total-reached check stops the iterator — a regression here would hang forever
        // (or throw on a mock with no page-3 setup).
        mockTemplates.Verify(a => a.ApiV1TemplatesGetAsync(3, 2, It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task ListAllAsync_empty_first_page_yields_nothing_no_infinite_loop()
    {
        var mockTemplates = new Mock<ITemplatesApi>();
        mockTemplates.Setup(a => a.ApiV1TemplatesGetAsync(1, 10, It.IsAny<CancellationToken>())).ReturnsAsync(Page(page: 1, limit: 10, total: 0, count: 0));

        var resource = new TemplatesResource(mockTemplates.Object, NoRetryNeeded);
        var results = new List<TemplateSummary>();
        await foreach (var t in resource.ListAllAsync(limit: 10))
        {
            results.Add(t);
        }

        Assert.Empty(results);
        mockTemplates.Verify(a => a.ApiV1TemplatesGetAsync(1, 10, It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task ListAsync_single_page_behavior_is_unaffected_by_pagination_addition()
    {
        var mockTemplates = new Mock<ITemplatesApi>();
        mockTemplates.Setup(a => a.ApiV1TemplatesGetAsync(1, 2, It.IsAny<CancellationToken>())).ReturnsAsync(Page(page: 1, limit: 2, total: 5, count: 2));

        var resource = new TemplatesResource(mockTemplates.Object, NoRetryNeeded);
        var result = await resource.ListAsync(page: 1, limit: 2);

        Assert.Equal(2, result.Templates.Count);
        Assert.Equal(5, result.Total);
        mockTemplates.Verify(a => a.ApiV1TemplatesGetAsync(1, 2, It.IsAny<CancellationToken>()), Times.Once);
    }
}
