package org.imzala;

/**
 * Retry settings shared by the resources: the GET auto-retry ({@code
 * maxRetries} + exponential backoff) and the single retry of a write sent
 * with an Idempotency-Key (which waits Retry-After, or {@code
 * retryBaseDelayMs} when the server gave none). Package-private: the public
 * surface is the {@code maxRetries}/{@code retryBaseDelayMs} constructor
 * parameters on {@link Imzala}, mirroring the Node SDK's {@code RetryConfig}.
 *
 * <p>Holding a config never makes a write retryable. Only {@link
 * Http#unwrapRetryableGet} (GET methods) and {@link
 * Http#unwrapIdempotentWrite} (demand create, document upload, timestamp
 * create, each only with a key) read it.
 */
final class RetryConfig {

  private final int maxRetries;
  private final long retryBaseDelayMs;
  private final Http.Sleeper sleeper;

  /**
   * @param maxRetries max retry attempts (not counting the initial try), clamped to {@code >= 0}
   * @param retryBaseDelayMs base delay (ms) for exponential backoff between retries, clamped to {@code >= 0}
   */
  RetryConfig(int maxRetries, long retryBaseDelayMs) {
    this(maxRetries, retryBaseDelayMs, Http.THREAD_SLEEPER);
  }

  /** Same as above, with the wait function replaced (tests use this so nothing really sleeps). */
  RetryConfig(int maxRetries, long retryBaseDelayMs, Http.Sleeper sleeper) {
    this.maxRetries = Math.max(0, maxRetries);
    this.retryBaseDelayMs = Math.max(0, retryBaseDelayMs);
    this.sleeper = sleeper != null ? sleeper : Http.THREAD_SLEEPER;
  }

  int getMaxRetries() {
    return maxRetries;
  }

  long getRetryBaseDelayMs() {
    return retryBaseDelayMs;
  }

  Http.Sleeper getSleeper() {
    return sleeper;
  }
}
