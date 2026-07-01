package org.imzala;

/**
 * Base error type thrown by every {@link Imzala} facade method. Normalizes
 * the vendored generated client's {@code org.imzala.client.generated.ApiException},
 * network/timeout failures, and a {@code {success:false}} response envelope
 * on an otherwise-2xx response into a single throwable shape — callers
 * never need to reach into {@code org.imzala.client.generated} internals.
 *
 * <p>Unchecked (extends {@link RuntimeException}), unlike the vendored
 * generated client's own checked {@code ApiException} — every {@link Imzala}
 * facade method converts that checked exception into one of these, so
 * callers don't need a {@code throws} clause or a mandatory try/catch just
 * to call e.g. {@code imzala.demands().get(id)}.
 *
 * <p>Thrown directly (not as a subclass) for statuses that don't have a
 * dedicated subclass below (400, 404, 409, 500, ...) — same 4-class
 * taxonomy as the TS ({@code @imzala/node}), Python ({@code imzala}), and
 * C# ({@code Imzala}) SDKs.
 */
public class ImzalaException extends RuntimeException {

  private final Integer statusCode;
  private final String body;
  private final String code;

  public ImzalaException(String message) {
    this(message, null, null, null, null);
  }

  /**
   * @param message human-readable error message
   * @param statusCode HTTP status code, when the error originated from an HTTP response
   * @param body raw response body (unparsed text), when available
   * @param code machine-readable error code from the response envelope, when present
   * @param cause the underlying exception (vendored generated ApiException, network error, ...), for {@link #getCause()}
   */
  public ImzalaException(String message, Integer statusCode, String body, String code, Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
    this.body = body;
    this.code = code;
  }

  /** HTTP status code, when the error originated from an HTTP response. {@code null} otherwise (e.g. a network error). */
  public Integer getStatusCode() {
    return statusCode;
  }

  /** Raw response body (unparsed text), when available. */
  public String getBody() {
    return body;
  }

  /** Machine-readable error code from the response envelope, when present (e.g. {@code "INVALID_API_KEY"}). */
  public String getCode() {
    return code;
  }
}
