package org.imzala;

import org.imzala.client.generated.ApiException;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * Every imzala.org API response uses the same envelope: {@code {success:
 * true, data: {...}}} on success, or a non-2xx status with {@code
 * {success: false, error/message: ...}} on failure (surfaced by the
 * vendored generated client as a thrown {@code ApiException}, not a
 * resolved-but-failed response).
 *
 * <p>{@link #unwrap} invokes a generated-client call, unwraps its {@code
 * data}, and normalizes any failure (thrown checked {@code ApiException},
 * or a {@code {success:false}} body on an otherwise-2xx response) into a
 * typed {@link ImzalaException} — see {@link ErrorMapper}. Every resource
 * method in {@code TemplatesResource}/{@code DemandsResource}/{@code
 * EmbedResource}/{@code TimestampsResource}/{@link Imzala#me()} routes
 * through this. Mirrors {@code http.ts}'s {@code unwrap<T>()} (TS), {@code
 * client.py}'s {@code _unwrap()} (Python), and {@code Http.cs}'s {@code
 * Unwrap<TResponse, TData>()} (C#).
 */
final class Http {

  private Http() {
  }

  /** The vendored generated client's *Api methods are synchronous and declare {@code throws ApiException} — this bridges that checked-exception signature into a lambda. */
  @FunctionalInterface
  interface ApiCall<T> {
    T call() throws ApiException;
  }

  static <TResponse, TData> TData unwrap(
      ApiCall<TResponse> call,
      Function<TResponse, Boolean> success,
      Function<TResponse, TData> data) {
    TResponse response;
    try {
      response = call.call();
    } catch (ApiException err) {
      throw ErrorMapper.map(err);
    }

    if (response == null || !Boolean.TRUE.equals(success.apply(response))) {
      throw new ImzalaException("imzala.org API request failed");
    }

    return data.apply(response);
  }

  /**
   * Like {@link #unwrap}, but adds safe auto-retry for <b>GET-only,
   * idempotent</b> resource methods ({@code templates().list/get/usage},
   * {@code demands().get}, {@code me()}). Retries on 429 (rate limited —
   * honors {@link ImzalaRateLimitException#getRetryAfter()}) and 5xx
   * (server error) with exponential backoff + jitter; any other status
   * (400, 401, 404, 409, 422, ...) is thrown immediately, same as {@link
   * #unwrap}.
   *
   * <p><b>SAFETY — never call this with a non-GET request.</b> There is
   * deliberately no {@code method}/verb parameter and no way to opt a
   * POST/PUT/PATCH/DELETE call into retrying: this is not a
   * caller-configurable behavior. Retrying a write (e.g. {@code
   * demands().create}, {@code demands().sendReminder}) could duplicate a
   * demand or double-send a reminder — those resource methods must keep
   * calling the plain {@link #unwrap} above, once, with no retry loop
   * reachable. Mirrors {@code http.ts}'s {@code unwrapRetryableGet()} (TS),
   * {@code client.py}'s equivalent (Python), and {@code Http.cs}'s {@code
   * UnwrapRetryableGet<TResponse, TData>()} (C#).
   *
   * <p>{@code call} is re-invoked (not a single already-executed {@link
   * ApiCall}) on each attempt, since retrying means re-issuing the
   * underlying HTTP request.
   */
  static <TResponse, TData> TData unwrapRetryableGet(
      ApiCall<TResponse> call,
      Function<TResponse, Boolean> success,
      Function<TResponse, TData> data,
      RetryConfig retry) {
    int attempt = 0;
    for (;;) {
      try {
        return unwrap(call, success, data);
      } catch (ImzalaException err) {
        if (attempt >= retry.getMaxRetries() || !isRetryableStatus(err.getStatusCode())) {
          throw err;
        }
        sleep(computeDelayMs(err, attempt, retry.getRetryBaseDelayMs()));
        attempt += 1;
      }
    }
  }

  /** 429 (rate limited) and 5xx (server error) are treated as transient. Everything else (4xx, or no status at all e.g. a {success:false} envelope) is never retried. */
  private static boolean isRetryableStatus(Integer statusCode) {
    if (statusCode == null) {
      return false;
    }
    return statusCode == 429 || (statusCode >= 500 && statusCode <= 599);
  }

  /** Exponential backoff with jitter, honoring {@code Retry-After} on 429s (already parsed onto {@link ImzalaRateLimitException#getRetryAfter()} by {@link ErrorMapper}). */
  private static long computeDelayMs(ImzalaException err, int attempt, long baseDelayMs) {
    if (err instanceof ImzalaRateLimitException rateLimitErr && rateLimitErr.getRetryAfter() != null) {
      return Math.max(0, Math.round(rateLimitErr.getRetryAfter() * 1000));
    }
    long backoff = baseDelayMs * (1L << attempt);
    long jitter = baseDelayMs > 0 ? ThreadLocalRandom.current().nextLong(baseDelayMs + 1) : 0;
    return backoff + jitter;
  }

  private static void sleep(long millis) {
    if (millis <= 0) {
      return;
    }
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ImzalaException("Retry bekleme sırasında kesildi (interrupted)", null, null, null, e);
    }
  }
}
