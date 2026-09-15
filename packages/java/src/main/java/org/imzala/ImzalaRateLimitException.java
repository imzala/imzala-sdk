package org.imzala;

/**
 * Rate limited (429). {@link #getRetryAfter()} is seconds, when the server provided one.
 *
 * <p>Several different limits answer with 429 and each has its own code (e.g.
 * {@code RATE_LIMIT_EXCEEDED}, {@code TOO_MANY_REQUESTS}, {@code RATE_LIMITED},
 * {@code RECIPIENT_RESEND_LIMIT}, {@code MAX_SMS_REMINDERS_REACHED}), so
 * branch on this class or on the status code, not on one particular code.
 */
public final class ImzalaRateLimitException extends ImzalaException {

  private final Double retryAfter;
  private final RateLimitInfo rateLimit;

  public ImzalaRateLimitException(String message, Integer statusCode, String body, String code, Double retryAfter, Throwable cause) {
    this(message, statusCode, body, code, retryAfter, null, cause);
  }

  public ImzalaRateLimitException(
      String message, Integer statusCode, String body, String code, Double retryAfter, RateLimitInfo rateLimit, Throwable cause) {
    super(message, statusCode, body, code, cause);
    this.retryAfter = retryAfter;
    this.rateLimit = rateLimit;
  }

  /** Seconds to wait before retrying, when the server provided one (from the response body or the {@code Retry-After} header, which may be a number of seconds or an HTTP date). {@code null} otherwise. */
  public Double getRetryAfter() {
    return retryAfter;
  }

  /** The standard {@code RateLimit-*} headers of the response, or {@code null} when the server sent none. */
  public RateLimitInfo getRateLimit() {
    return rateLimit;
  }
}
