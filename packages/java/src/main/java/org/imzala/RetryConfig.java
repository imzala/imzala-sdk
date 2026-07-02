package org.imzala;

/**
 * Safe auto-retry settings for the **GET-only, idempotent** resource
 * methods ({@code templates().list/get/usage}, {@code demands().get},
 * {@code me()}) — threaded into {@link TemplatesResource}, {@link
 * DemandsResource}, and {@link AccountResource} by {@link Imzala}'s
 * constructor. Package-private: the public surface is just the two
 * {@code maxRetries}/{@code retryBaseDelayMs} constructor params on {@link
 * Imzala}, mirroring the Node SDK's {@code RetryConfig} (TS) / .NET's
 * {@code internal sealed class RetryConfig} (C#) — same 4-language pattern.
 *
 * <p>{@link EmbedResource} and {@link TimestampsResource} (write-only —
 * {@code createSession}/{@code create}) are never constructed with one,
 * so there is no code path that can wire retry into a POST.
 */
final class RetryConfig {

  private final int maxRetries;
  private final long retryBaseDelayMs;

  /**
   * @param maxRetries max retry attempts (not counting the initial try), clamped to {@code >= 0}
   * @param retryBaseDelayMs base delay (ms) for exponential backoff between retries, clamped to {@code >= 0}
   */
  RetryConfig(int maxRetries, long retryBaseDelayMs) {
    this.maxRetries = Math.max(0, maxRetries);
    this.retryBaseDelayMs = Math.max(0, retryBaseDelayMs);
  }

  int getMaxRetries() {
    return maxRetries;
  }

  long getRetryBaseDelayMs() {
    return retryBaseDelayMs;
  }
}
