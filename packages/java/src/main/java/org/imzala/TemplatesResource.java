package org.imzala;

import org.imzala.client.generated.api.TemplatesApi;
import org.imzala.client.generated.model.ApiV1TemplatesGet200ResponseData;
import org.imzala.client.generated.model.TemplateDetail;
import org.imzala.client.generated.model.TemplateUsage;

import java.util.UUID;

/** {@code imzala.templates()} — backed by the vendored generated {@code TemplatesApi}. */
public final class TemplatesResource {

  private final TemplatesApi api;

  TemplatesResource(TemplatesApi api) {
    this.api = api;
  }

  /** Lists your active templates. */
  public ApiV1TemplatesGet200ResponseData list() {
    return list(null, null);
  }

  /** Lists your active templates. */
  public ApiV1TemplatesGet200ResponseData list(Integer page, Integer limit) {
    return Http.unwrap(
        () -> api.apiV1TemplatesGet(page, limit),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /** Returns a template's parties + fillable variables. */
  public TemplateDetail get(UUID id) {
    return Http.unwrap(
        () -> api.apiV1TemplatesIdGet(id),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /** Returns a ready-to-use integration guide (endpoint, required headers, example curl+JSON) for a template. */
  public TemplateUsage usage(UUID id) {
    return Http.unwrap(
        () -> api.apiV1TemplatesIdUsageGet(id),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }
}
