package org.imzala;

import org.imzala.client.generated.api.TemplatesApi;
import org.imzala.client.generated.model.ApiV1TemplatesGet200ResponseData;
import org.imzala.client.generated.model.ApiV1TemplatesIdDelete200ResponseData;
import org.imzala.client.generated.model.ApiV1TemplatesIdPatch200ResponseData;
import org.imzala.client.generated.model.ApiV1TemplatesIdPatchRequest;
import org.imzala.client.generated.model.TemplateDetail;
import org.imzala.client.generated.model.TemplateSummary;
import org.imzala.client.generated.model.TemplateUsage;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/** {@code imzala.templates()} — backed by the vendored generated {@code TemplatesApi}. */
public final class TemplatesResource {

  private final TemplatesApi api;
  private final RetryConfig retryConfig;

  TemplatesResource(TemplatesApi api, RetryConfig retryConfig) {
    this.api = api;
    this.retryConfig = retryConfig;
  }

  /** Lists your active templates (one page). GET — safe to auto-retry. */
  public ApiV1TemplatesGet200ResponseData list() {
    return list(null, null);
  }

  /** Lists your active templates (one page). GET — safe to auto-retry. */
  public ApiV1TemplatesGet200ResponseData list(Integer page, Integer limit) {
    return Http.unwrapRetryableGet(
        () -> api.apiV1TemplatesGet(page, limit),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        retryConfig);
  }

  /** Returns a template's parties + fillable variables. GET — safe to auto-retry. */
  public TemplateDetail get(UUID id) {
    return Http.unwrapRetryableGet(
        () -> api.apiV1TemplatesIdGet(id),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        retryConfig);
  }

  /** Returns a ready-to-use integration guide (endpoint, required headers, example curl+JSON) for a template. GET — safe to auto-retry. */
  public TemplateUsage usage(UUID id) {
    return Http.unwrapRetryableGet(
        () -> api.apiV1TemplatesIdUsageGet(id),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        retryConfig);
  }

  /**
   * Updates a template's metadata (name / description / category). The
   * page/field/party structure can't be changed via the API — edit that in
   * the dashboard. Build the body with {@code new
   * ApiV1TemplatesIdPatchRequest().name("...").description("...").category("...")}.
   * PATCH — never auto-retried.
   */
  public ApiV1TemplatesIdPatch200ResponseData update(UUID id, ApiV1TemplatesIdPatchRequest body) {
    return Http.unwrap(
        () -> api.apiV1TemplatesIdPatch(id, body),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /**
   * Deletes (soft-deletes) a template. Existing demands created from it are
   * unaffected. DELETE — never auto-retried.
   */
  public ApiV1TemplatesIdDelete200ResponseData delete(UUID id) {
    return Http.unwrap(
        () -> api.apiV1TemplatesIdDelete(id),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /**
   * Walks every page of your active templates, transparently, yielding one
   * template at a time. Internally calls the (retry-wrapped) {@link
   * #list(Integer, Integer)} and increments {@code page} — from the
   * response's own {@code page} field, not a locally-tracked counter —
   * until a page comes back short (fewer items than the effective page
   * size) or the response's {@code total} has been reached, or a page is
   * empty — whichever happens first, so it always terminates even against
   * a misbehaving/empty result set. {@link #list} itself is untouched
   * (still single-page).
   *
   * <pre>{@code
   * for (TemplateSummary template : imzala.templates().listAll()) {
   *     System.out.println(template.getId() + " " + template.getName());
   * }
   * }</pre>
   */
  public Iterable<TemplateSummary> listAll() {
    return listAll(null, null);
  }

  /** @see #listAll() */
  public Iterable<TemplateSummary> listAll(Integer page, Integer limit) {
    return () -> new TemplatePageIterator(page, limit);
  }

  /**
   * Lazily fetches one page at a time from {@link #list(Integer, Integer)}
   * on demand, buffering the current page's items. Not thread-safe — like
   * every standard {@link Iterator}, a fresh instance is created per
   * {@link Iterable#iterator()} call (see {@link #listAll(Integer,
   * Integer)}), so concurrent iteration doesn't share state.
   */
  private final class TemplatePageIterator implements Iterator<TemplateSummary> {

    private final Integer requestedLimit;
    private Integer nextPage;
    private List<TemplateSummary> buffer = List.of();
    private int bufferIndex = 0;
    private int yielded = 0;
    private boolean exhausted = false;

    TemplatePageIterator(Integer page, Integer limit) {
      this.nextPage = page != null ? page : 1;
      this.requestedLimit = limit;
    }

    @Override
    public boolean hasNext() {
      fetchNextPageIfNeeded();
      return bufferIndex < buffer.size();
    }

    @Override
    public TemplateSummary next() {
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

      ApiV1TemplatesGet200ResponseData result = list(nextPage, requestedLimit);
      List<TemplateSummary> templates = result.getTemplates() != null ? result.getTemplates() : List.of();

      buffer = templates;
      bufferIndex = 0;
      yielded += templates.size();

      if (templates.isEmpty()) {
        exhausted = true;
        return;
      }

      Integer total = result.getTotal();
      if (total != null && yielded >= total) {
        exhausted = true;
        return;
      }

      Integer effectiveLimit = result.getLimit() != null ? result.getLimit() : requestedLimit;
      if (effectiveLimit != null && templates.size() < effectiveLimit) {
        exhausted = true;
        return;
      }

      nextPage = (result.getPage() != null ? result.getPage() : nextPage) + 1;
    }
  }
}
