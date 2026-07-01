package org.imzala;

/** Missing/invalid API key (401) or disabled key / insufficient scope (403). */
public final class ImzalaAuthException extends ImzalaException {

  public ImzalaAuthException(String message, Integer statusCode, String body, String code, Throwable cause) {
    super(message, statusCode, body, code, cause);
  }
}
