package org.imzala;

/**
 * Request payload failed validation: a 422 from the server, or a value
 * rejected locally before sending (e.g. an Idempotency-Key that is not a valid
 * HTTP header value). In the latter case {@link #getStatusCode()} is null.
 */
public final class ImzalaValidationException extends ImzalaException {

  public ImzalaValidationException(String message, Integer statusCode, String body, String code, Throwable cause) {
    super(message, statusCode, body, code, cause);
  }
}
