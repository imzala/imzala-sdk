package org.imzala;

/**
 * Standard {@code RateLimit-*} response headers attached to a 429. The server
 * does not send {@code X-RateLimit-*}, so those are never read. Each field is
 * {@code null} when its header was absent or not a whole number.
 */
public final class RateLimitInfo {

  private final Integer limit;
  private final Integer remaining;
  private final Integer reset;
  private final String policy;

  public RateLimitInfo(Integer limit, Integer remaining, Integer reset, String policy) {
    this.limit = limit;
    this.remaining = remaining;
    this.reset = reset;
    this.policy = policy;
  }

  /** {@code RateLimit-Limit}: requests allowed per window. Defaults to 60 but can be lowered per API key, so read it rather than assuming. */
  public Integer getLimit() {
    return limit;
  }

  /** {@code RateLimit-Remaining}: requests left in the current window. */
  public Integer getRemaining() {
    return remaining;
  }

  /** {@code RateLimit-Reset}: seconds until the window resets. */
  public Integer getReset() {
    return reset;
  }

  /** {@code RateLimit-Policy}: raw policy string, e.g. {@code 60;w=60}. */
  public String getPolicy() {
    return policy;
  }

  @Override
  public String toString() {
    return "RateLimitInfo{limit=" + limit + ", remaining=" + remaining + ", reset=" + reset + ", policy=" + policy + "}";
  }
}
