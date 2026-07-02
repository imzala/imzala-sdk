using System.Runtime.CompilerServices;
using ImzalaApiClient.Api;
using ImzalaApiClient.Model;

namespace ImzalaSdk;

/// <summary><c>imzala.Templates.*</c> — <c>ImzalaApiClient.Api.TemplatesApi</c> under the hood.</summary>
public sealed class TemplatesResource
{
    private readonly ITemplatesApi _api;
    private readonly RetryConfig _retry;

    internal TemplatesResource(ITemplatesApi api, RetryConfig retry)
    {
        _api = api;
        _retry = retry;
    }

    /// <summary>Lists your active templates (one page). GET — safe to auto-retry.</summary>
    public Task<ApiV1TemplatesGet200ResponseData> ListAsync(int? page = null, int? limit = null, CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(
            () => _api.ApiV1TemplatesGetAsync(page, limit, cancellationToken),
            r => r.Success,
            r => r.Data,
            _retry);

    /// <summary>Returns a template's parties + fillable variables. GET — safe to auto-retry.</summary>
    public Task<TemplateDetail> GetAsync(Guid id, CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(() => _api.ApiV1TemplatesIdGetAsync(id, cancellationToken), r => r.Success, r => r.Data, _retry);

    /// <summary>Returns a ready-to-use integration guide (endpoint, required headers, example curl+JSON) for a template. GET — safe to auto-retry.</summary>
    public Task<TemplateUsage> UsageAsync(Guid id, CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(() => _api.ApiV1TemplatesIdUsageGetAsync(id, cancellationToken), r => r.Success, r => r.Data, _retry);

    /// <summary>
    /// Walks every page of your active templates, transparently, yielding one
    /// template at a time. Internally calls <see cref="ListAsync"/> (itself
    /// retry-wrapped) and increments the page number until a page comes back
    /// short (fewer items than the requested page size) or the response's
    /// <c>Total</c> has been reached — whichever happens first — so it always
    /// terminates even against a misbehaving/empty result set.
    /// </summary>
    /// <example>
    /// <code>
    /// await foreach (var template in imzala.Templates.ListAllAsync())
    /// {
    ///     Console.WriteLine($"{template.Id} {template.Name}");
    /// }
    /// </code>
    /// </example>
    public async IAsyncEnumerable<TemplateSummary> ListAllAsync(
        int? page = null,
        int? limit = null,
        [EnumeratorCancellation] CancellationToken cancellationToken = default)
    {
        var requestedLimit = limit;
        var currentPage = page ?? 1;
        var yielded = 0;

        while (true)
        {
            var result = await ListAsync(currentPage, requestedLimit, cancellationToken).ConfigureAwait(false);
            var templates = result.Templates ?? new List<TemplateSummary>();

            foreach (var template in templates)
            {
                yield return template;
            }

            yielded += templates.Count;

            if (templates.Count == 0)
            {
                yield break;
            }

            if (yielded >= result.Total)
            {
                yield break;
            }

            var effectiveLimit = requestedLimit ?? result.Limit;
            if (effectiveLimit > 0 && templates.Count < effectiveLimit)
            {
                yield break;
            }

            currentPage = result.Page > 0 ? result.Page + 1 : currentPage + 1;
        }
    }
}
