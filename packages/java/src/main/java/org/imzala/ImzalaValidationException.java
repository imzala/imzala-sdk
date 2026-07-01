package org.imzala;

/** Request payload failed validation (422). */
public final class ImzalaValidationException extends ImzalaException {

  public ImzalaValidationException(String message, Integer statusCode, String body, String code, Throwable cause) {
    super(message, statusCode, body, code, cause);
  }
}
