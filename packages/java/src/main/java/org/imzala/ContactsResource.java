package org.imzala;

import org.imzala.client.generated.api.ContactsApi;
import org.imzala.client.generated.model.ApiV1ContactsGet200ResponseData;
import org.imzala.client.generated.model.ApiV1ContactsPostRequest;
import org.imzala.client.generated.model.ContactSummary;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** {@code imzala.contacts()}, backed by the generated {@code ContactsApi}. */
public final class ContactsResource {

  private final ContactsApi api;
  private final RetryConfig retryConfig;

  ContactsResource(ContactsApi api, RetryConfig retryConfig) {
    this.api = api;
    this.retryConfig = retryConfig;
  }

  /** Lists contacts in your workspace (first page, no filters). GET, safe to auto-retry. */
  public ApiV1ContactsGet200ResponseData list() {
    return list(null);
  }

  /** Lists contacts in your workspace (one page). {@code limit} is 10 to 100, default 25. GET, safe to auto-retry. */
  public ApiV1ContactsGet200ResponseData list(ListContactsParams params) {
    ListContactsParams p = params != null ? params : new ListContactsParams();
    return Http.unwrapRetryableGet(
        () -> api.apiV1ContactsGet(
            /* page */ p.getPage(),
            /* limit */ p.getLimit(),
            /* q */ p.getQ(),
            /* sort */ p.getSort(),
            /* companyId */ p.getCompanyId(),
            /* archived */ p.getArchived()),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        retryConfig);
  }

  /** Walks every page of contacts, yielding one contact at a time. */
  public Iterable<ContactSummary> listAll() {
    return listAll(null);
  }

  /**
   * Walks every page of contacts matching {@code params}, yielding one contact
   * at a time. Filters are kept on every page; paging starts at {@code
   * params.page} (default 1) and stops on an empty or short page, or once
   * {@code total} contacts were yielded.
   */
  public Iterable<ContactSummary> listAll(ListContactsParams params) {
    ListContactsParams base = params != null ? params : new ListContactsParams();
    return () -> new ContactPageIterator(base);
  }

  /**
   * Adds a contact. An active contact with the same e-mail or phone throws
   * {@code CONTACT_DUPLICATE}. This endpoint has no idempotency key, so it is
   * never retried. POST.
   */
  public ContactSummary create(ApiV1ContactsPostRequest body) {
    return Http.unwrap(
        () -> api.apiV1ContactsPost(body),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  private final class ContactPageIterator implements Iterator<ContactSummary> {

    private final ListContactsParams base;
    private int nextPage;
    private List<ContactSummary> buffer = List.of();
    private int bufferIndex = 0;
    private int yielded = 0;
    private boolean exhausted = false;

    ContactPageIterator(ListContactsParams base) {
      this.base = base;
      this.nextPage = base.getPage() != null ? base.getPage() : 1;
    }

    @Override
    public boolean hasNext() {
      fetchNextPageIfNeeded();
      return bufferIndex < buffer.size();
    }

    @Override
    public ContactSummary next() {
      fetchNextPageIfNeeded();
      if (bufferIndex >= buffer.size()) {
        throw new NoSuchElementException();
      }
      return buffer.get(bufferIndex++);
    }

    private void fetchNextPageIfNeeded() {
      if (bufferIndex < buffer.size() || exhausted) {
        return;
      }

      ListContactsParams pageParams = new ListContactsParams()
          .q(base.getQ())
          .sort(base.getSort())
          .companyId(base.getCompanyId())
          .archived(base.getArchived())
          .limit(base.getLimit())
          .page(nextPage);
      ApiV1ContactsGet200ResponseData result = list(pageParams);
      List<ContactSummary> contacts = result != null && result.getContacts() != null ? result.getContacts() : List.of();

      buffer = contacts;
      bufferIndex = 0;
      yielded += contacts.size();

      if (contacts.isEmpty()) {
        exhausted = true;
        return;
      }
      Integer total = result.getTotal();
      if (total != null && yielded >= total) {
        exhausted = true;
        return;
      }
      Integer effectiveLimit = result.getLimit() != null ? result.getLimit() : base.getLimit();
      if (effectiveLimit != null && contacts.size() < effectiveLimit) {
        exhausted = true;
        return;
      }
      nextPage = (result.getPage() != null ? result.getPage() : nextPage) + 1;
    }
  }
}
