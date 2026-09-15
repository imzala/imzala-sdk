using System.Runtime.CompilerServices;
using ImzalaApiClient.Api;
using ImzalaApiClient.Model;

namespace ImzalaSdk;

/// <summary><c>imzala.Contacts.*</c>, backed by <c>ImzalaApiClient.Api.ContactsApi</c>.</summary>
public sealed class ContactsResource
{
    private readonly IContactsApi _api;
    private readonly RetryConfig _retry;

    internal ContactsResource(IContactsApi api, RetryConfig retry)
    {
        _api = api;
        _retry = retry;
    }

    /// <summary>Lists contacts in your workspace (one page). GET, safe to auto-retry.</summary>
    /// <param name="q">Search in name, e-mail, phone and identity number (max 100 characters).</param>
    /// <param name="page">1-based page number.</param>
    /// <param name="limit">Page size, 10 to 100, default 25.</param>
    /// <param name="sort">One of createdAt, updatedAt, first_name, last_name, email; a <c>-</c> prefix sorts descending. Server default <c>-createdAt</c>.</param>
    /// <param name="companyId">Only contacts of this company.</param>
    /// <param name="archived"><c>true</c> lists only archived contacts; the server default is active contacts.</param>
    /// <param name="cancellationToken">Cancellation token.</param>
    public Task<ApiV1ContactsGet200ResponseData> ListAsync(
        string? q = null,
        int? page = null,
        int? limit = null,
        string? sort = null,
        Guid? companyId = null,
        bool? archived = null,
        CancellationToken cancellationToken = default) =>
        Http.UnwrapRetryableGet(
            () => _api.ApiV1ContactsGetAsync(page: page, limit: limit, q: q, sort: sort, companyId: companyId, archived: archived, cancellationToken: cancellationToken),
            r => r.Success,
            r => r.Data,
            _retry,
            cancellationToken);

    /// <summary>
    /// Walks every page of contacts matching the filters, yielding one contact at a
    /// time. Filters are kept on every page; paging starts at <paramref name="page"/>
    /// (default 1) and stops on an empty or short page, or once <c>Total</c> contacts
    /// were yielded.
    /// </summary>
    public async IAsyncEnumerable<ContactSummary> ListAllAsync(
        string? q = null,
        int? page = null,
        int? limit = null,
        string? sort = null,
        Guid? companyId = null,
        bool? archived = null,
        [EnumeratorCancellation] CancellationToken cancellationToken = default)
    {
        var currentPage = page ?? 1;
        var yielded = 0;

        while (true)
        {
            var result = await ListAsync(q, currentPage, limit, sort, companyId, archived, cancellationToken).ConfigureAwait(false);
            var contacts = result?.Contacts ?? new List<ContactSummary>();

            foreach (var contact in contacts)
            {
                yield return contact;
            }

            yielded += contacts.Count;

            if (contacts.Count == 0 || yielded >= result!.Total)
            {
                yield break;
            }

            var effectiveLimit = result.Limit > 0 ? result.Limit : limit ?? 0;
            if (effectiveLimit > 0 && contacts.Count < effectiveLimit)
            {
                yield break;
            }

            currentPage = result.Page > 0 ? result.Page + 1 : currentPage + 1;
        }
    }

    /// <summary>
    /// Adds a contact. An active contact with the same e-mail or phone throws
    /// <c>CONTACT_DUPLICATE</c>. This endpoint has no idempotency key, so it is never
    /// retried. POST.
    /// </summary>
    public Task<ContactSummary> CreateAsync(ApiV1ContactsPostRequest body, CancellationToken cancellationToken = default) =>
        Http.Unwrap(_api.ApiV1ContactsPostAsync(apiV1ContactsPostRequest: body, cancellationToken: cancellationToken), r => r.Success, r => r.Data);
}
