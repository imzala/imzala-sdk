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

  /** Waits the given number of milliseconds. Injectable through {@link RetryConfig} so tests never sleep. */
  @FunctionalInterface
  interface Sleeper {
    void sleep(long millis);
  }

  static final Sleeper THREAD_SLEEPER = Http::sleep;

  /**
   * Longest wait (ms) the SDK blocks the caller for before a retry. When the
   * server asks for more (Retry-After), the error is thrown instead: applies
   * to the GET auto-retry and to the one retry of an idempotent write.
   */
  static final long MAX_RETRY_WAIT_MS = 60_000L;

  static <TResponse, TData> TData unwrap(
      ApiCall<TResponse> call,
      Function<TResponse, Boolean> success,
      Function<TResponse, TData> data) {
    TResponse response;
    try {
      response = call.call();
    } catch (ApiException err) {
      throw ErrorMapper.map(err);
    } catch (IllegalArgumentException err) {
      // Rejected locally before any request was sent, e.g. an Idempotency-Key
      // with non-ASCII characters is not a valid HTTP header value.
      throw new ImzalaValidationException(err.getMessage(), null, null, null, err);
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
   * <p>If a wait would exceed {@link #MAX_RETRY_WAIT_MS} (60 s), for example
   * a long Retry-After, the error is thrown instead of blocking the caller.
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
        long delayMs = computeDelayMs(err, attempt, retry.getRetryBaseDelayMs());
        // A server asking for a longer wait than we are willing to block for
        // gets the error back instead of a silent long sleep.
        if (delayMs > MAX_RETRY_WAIT_MS) {
          throw err;
        }
        retry.getSleeper().sleep(delayMs);
        attempt += 1;
      }
    }
  }

  /**
   * Bounded, safe retry for write calls.
   *
   * <p>Writes are normally never retried: a repeated create produces a second
   * demand. The one exception is a write sent with an Idempotency-Key. The
   * server does not treat a second request with the same key as a new
   * record, so after a 429 it is safe to wait for Retry-After (or the base
   * delay when there is none) and try exactly once more. A second 429, and
   * any other error including 5xx, is thrown.
   *
   * <p>If the server asks for a longer wait than {@link #MAX_RETRY_WAIT_MS}
   * (60 s), the 429 is thrown instead of blocking the caller.
   *
   * <p>Only use this for endpoints whose Idempotency-Key the server honours
   * (demand create, document upload, timestamp create). On any other
   * endpoint the "safe" retry could create a duplicate.
   *
   * <p>Without a key (or with an empty one) this behaves exactly like {@link
   * #unwrap}.
   */
  static <TResponse, TData> TData unwrapIdempotentWrite(
      ApiCall<TResponse> call,
      Function<TResponse, Boolean> success,
      Function<TResponse, TData> data,
      String idempotencyKey,
      RetryConfig retry) {
    return unwrapIdempotentWrite(call, success, data, idempotencyKey, retry, MAX_RETRY_WAIT_MS);
  }

  static <TResponse, TData> TData unwrapIdempotentWrite(
      ApiCall<TResponse> call,
      Function<TResponse, Boolean> success,
      Function<TResponse, TData> data,
      String idempotencyKey,
      RetryConfig retry,
      long maxWaitMs) {
    try {
      return unwrap(call, success, data);
    } catch (ImzalaException err) {
      Integer status = err.getStatusCode();
      boolean replayable = idempotencyKey != null && !idempotencyKey.isEmpty() && status != null && status == 429;
      if (!replayable) {
        throw err;
      }
      long waitMs = err instanceof ImzalaRateLimitException rateLimitErr && rateLimitErr.getRetryAfter() != null
          ? Math.max(0, Math.round(rateLimitErr.getRetryAfter() * 1000))
          : retry.getRetryBaseDelayMs();
      if (waitMs > maxWaitMs) {
        throw err;
      }
      retry.getSleeper().sleep(waitMs);
      return unwrap(call, success, data);
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
