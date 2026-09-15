package org.imzala;

import org.imzala.client.generated.api.ReportsApi;
import org.imzala.client.generated.model.ApiV1ReportsGet200ResponseData;

/** {@code imzala.reports()}, backed by the generated {@code ReportsApi}. */
public final class ReportsResource {

  private final ReportsApi api;
  private final RetryConfig retryConfig;

  ReportsResource(ReportsApi api, RetryConfig retryConfig) {
    this.api = api;
    this.retryConfig = retryConfig;
  }

  /**
   * Returns aggregate demand counts for your workspace (pending, completed,
   * cancelled, expired, created this month). Counts only, no personal data.
   * GET, safe to auto-retry.
   */
  public ApiV1ReportsGet200ResponseData get() {
    return Http.unwrapRetryableGet(
        api::apiV1ReportsGet,
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        retryConfig);
  }
}
