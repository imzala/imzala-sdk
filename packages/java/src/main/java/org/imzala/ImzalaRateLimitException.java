package org.imzala;

/** Rate limited (429). {@link #getRetryAfter()} is seconds, when the server provided one. */
public final class ImzalaRateLimitException extends ImzalaException {

  private final Double retryAfter;

  public ImzalaRateLimitException(String message, Integer statusCode, String body, String code, Double retryAfter, Throwable cause) {
    super(message, statusCode, body, code, cause);
    this.retryAfter = retryAfter;
  }

  /** Seconds to wait before retrying, when the server provided one (from the response body or the {@code Retry-After} header). {@code null} otherwise. */
  public Double getRetryAfter() {
    return retryAfter;
  }
}
