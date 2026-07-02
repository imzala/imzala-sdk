import type { AxiosPromise } from 'axios';
import { ImzalaError, ImzalaRateLimitError, extractErrorMessage, mapAxiosError } from './errors';

/**
 * Every imzala.org API response uses the same envelope:
 * `{success: true, data: {...}}` on success, or a non-2xx status with
 * `{success: false, error/message: ...}` on failure.
 *
 * `unwrap` awaits a generated-client call, unwraps `data`, and normalizes
 * any failure (HTTP error status, network error, or a `{success:false}`
 * body on an otherwise-2xx response) into a typed `ImzalaError` — see
 * ./errors. Every facade method in ./index routes through this.
 */
export async function unwrap<T>(
  promise: AxiosPromise<{ success?: boolean; data?: T }>,
): Promise<T> {
  let response;
  try {
    response = await promise;
  } catch (err) {
    throw mapAxiosError(err);
  }

  const body = response.data;
  if (!body || body.success === false) {
    throw new ImzalaError(extractErrorMessage(body) ?? 'imzala.org API request failed', {
      statusCode: response.status,
      body,
    });
  }

  return body.data as T;
}

export interface RetryConfig {
  /** Max retry attempts (not counting the initial try). `0` disables retry. */
  maxRetries: number;
  /** Base delay (ms) for exponential backoff between retries. */
  retryBaseDelayMs: number;
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/** 429 (rate limited) and 5xx (server error) are treated as transient. Everything else (4xx) is a client error and is never retried. */
function isRetryableStatus(statusCode: number | undefined): boolean {
  if (statusCode === 429) return true;
  return typeof statusCode === 'number' && statusCode >= 500 && statusCode <= 599;
}

/** Exponential backoff with jitter, honoring `Retry-After` on 429s (already parsed onto `ImzalaRateLimitError.retryAfter` by `mapAxiosError`). */
function computeDelayMs(error: ImzalaError, attempt: number, baseDelayMs: number): number {
  if (error instanceof ImzalaRateLimitError && typeof error.retryAfter === 'number') {
    return Math.max(0, error.retryAfter * 1000);
  }
  const backoff = baseDelayMs * 2 ** attempt;
  const jitter = Math.random() * baseDelayMs;
  return backoff + jitter;
}

/**
 * Like `unwrap`, but adds safe auto-retry for **GET-only, idempotent**
 * facade methods (`templates.list/get/usage`, `demands.get`, `me()`).
 * Retries on 429 (rate limited — honors `Retry-After`) and 5xx (server
 * error) with exponential backoff + jitter; any other status (400, 401,
 * 404, 409, 422, ...) is thrown immediately, same as `unwrap`.
 *
 * **SAFETY — never call this with a non-GET request.** There is
 * deliberately no `method` parameter and no way to opt a POST/PUT/PATCH/
 * DELETE call into retrying: this is not a caller-configurable behavior.
 * Retrying a write (e.g. `demands.create`, `demands.sendReminder`) could
 * duplicate a demand or double-send a reminder — those facade methods must
 * keep using the plain `unwrap()` above, once, with no retry loop.
 *
 * `requestFn` is a thunk (not an already-created promise) because retrying
 * means re-issuing the underlying HTTP request — a settled promise can't be
 * replayed.
 */
export async function unwrapRetryableGet<T>(
  requestFn: () => AxiosPromise<{ success?: boolean; data?: T }>,
  retry: RetryConfig,
): Promise<T> {
  let attempt = 0;
  for (;;) {
    try {
      return await unwrap(requestFn());
    } catch (err) {
      const mapped = err instanceof ImzalaError ? err : mapAxiosError(err);
      if (attempt >= retry.maxRetries || !isRetryableStatus(mapped.statusCode)) {
        throw mapped;
      }
      await sleep(computeDelayMs(mapped, attempt, retry.retryBaseDelayMs));
      attempt += 1;
    }
  }
}
