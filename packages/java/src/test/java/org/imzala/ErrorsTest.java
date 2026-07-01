package org.imzala;

import org.imzala.client.generated.ApiException;
import org.junit.jupiter.api.Test;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mirrors {@code errors.test.ts} (B1), {@code test_errors.py} (B2), and
 * {@code ErrorsTests.cs} (B3) — constructs a vendored generated-client
 * {@code ApiException} the way the generated {@code java}/{@code native}
 * client throws it (status + raw-JSON-text body + {@link HttpHeaders}) and
 * asserts {@link ErrorMapper#map} produces the right {@link
 * ImzalaException} subclass.
 */
class ErrorsTest {

  private static ApiException fakeApiException(int status, String jsonBody) {
    return fakeApiException(status, jsonBody, Map.of());
  }

  private static ApiException fakeApiException(int status, String jsonBody, Map<String, List<String>> headers) {
    HttpHeaders httpHeaders = HttpHeaders.of(headers, (a, b) -> true);
    return new ApiException("apiV1SomeMethod call failed with: " + status + " - " + jsonBody, status, httpHeaders, jsonBody);
  }

  @Test
  void maps_401_to_ImzalaAuthException() {
    ImzalaException err = ErrorMapper.map(fakeApiException(401, """
        {"success":false,"error":"INVALID_API_KEY","message":"Invalid API key"}"""));

    ImzalaAuthException auth = assertInstanceOf(ImzalaAuthException.class, err);
    assertInstanceOf(ImzalaException.class, auth);
    assertEquals(401, auth.getStatusCode());
    assertEquals("INVALID_API_KEY", auth.getCode());
    assertEquals("Invalid API key", auth.getMessage());
  }

  @Test
  void maps_403_to_ImzalaAuthException() {
    ImzalaException err = ErrorMapper.map(fakeApiException(403, """
        {"success":false,"error":"INSUFFICIENT_SCOPE"}"""));

    ImzalaAuthException auth = assertInstanceOf(ImzalaAuthException.class, err);
    assertEquals(403, auth.getStatusCode());
    assertEquals("INSUFFICIENT_SCOPE", auth.getCode());
  }

  @Test
  void maps_429_to_ImzalaRateLimitException_reading_retryAfter_from_nested_error_object() {
    ImzalaException err = ErrorMapper.map(fakeApiException(429, """
        {"success":false,"error":{"code":"RATE_LIMITED","message":"Too many requests","retry_after_seconds":42}}"""));

    ImzalaRateLimitException rateLimited = assertInstanceOf(ImzalaRateLimitException.class, err);
    assertInstanceOf(ImzalaException.class, rateLimited);
    assertEquals(42.0, rateLimited.getRetryAfter());
    assertEquals("RATE_LIMITED", rateLimited.getCode());
    assertEquals("Too many requests", rateLimited.getMessage());
  }

  @Test
  void maps_429_retryAfter_from_RetryAfter_header_when_body_omits_it() {
    ImzalaException err = ErrorMapper.map(fakeApiException(
        429,
        """
            {"success":false,"error":"RATE_LIMITED"}""",
        Map.of("Retry-After", List.of("30"))));

    ImzalaRateLimitException rateLimited = assertInstanceOf(ImzalaRateLimitException.class, err);
    assertEquals(30.0, rateLimited.getRetryAfter());
  }

  @Test
  void maps_422_to_ImzalaValidationException() {
    ImzalaException err = ErrorMapper.map(fakeApiException(422, """
        {"success":false,"message":"Invalid payload"}"""));

    ImzalaValidationException validation = assertInstanceOf(ImzalaValidationException.class, err);
    assertInstanceOf(ImzalaException.class, validation);
    assertEquals(422, validation.getStatusCode());
    assertEquals("Invalid payload", validation.getMessage());
  }

  @Test
  void falls_back_to_base_ImzalaException_for_unmapped_statuses_404_500() {
    ImzalaException err404 = ErrorMapper.map(fakeApiException(404, """
        {"success":false,"error":"DEMAND_NOT_FOUND"}"""));
    assertEquals(ImzalaException.class, err404.getClass());
    assertFalse(err404 instanceof ImzalaAuthException);
    assertFalse(err404 instanceof ImzalaRateLimitException);
    assertFalse(err404 instanceof ImzalaValidationException);
    assertEquals(404, err404.getStatusCode());
    assertEquals("DEMAND_NOT_FOUND", err404.getCode());

    ImzalaException err500 = ErrorMapper.map(fakeApiException(500, """
        {"success":false}"""));
    assertEquals(500, err500.getStatusCode());
  }

  @Test
  void wraps_a_plain_exception_as_a_base_ImzalaException() {
    ImzalaException err = ErrorMapper.map(new IllegalStateException("ECONNREFUSED"));

    assertEquals(ImzalaException.class, err.getClass());
    assertFalse(err instanceof ImzalaAuthException);
    assertEquals("ECONNREFUSED", err.getMessage());
    assertNull(err.getStatusCode());
    assertInstanceOf(IllegalStateException.class, err.getCause());
  }

  @Test
  void passes_an_already_typed_ImzalaException_through_unchanged() {
    ImzalaValidationException original = new ImzalaValidationException("bad payload", 422, null, null, null);
    assertSame(original, ErrorMapper.map(original));
  }

  @Test
  void every_subclass_is_an_ImzalaException_and_an_exception() {
    ImzalaException[] errors = {
        new ImzalaAuthException("a", null, null, null, null),
        new ImzalaRateLimitException("b", null, null, null, null, null),
        new ImzalaValidationException("c", null, null, null, null),
    };

    for (ImzalaException err : errors) {
      assertInstanceOf(ImzalaException.class, err);
      assertInstanceOf(Exception.class, err);
    }
  }

  @Test
  void malformed_JSON_body_falls_back_to_the_generated_exception_message() {
    ImzalaException err = ErrorMapper.map(fakeApiException(500, "not json at all"));

    assertEquals(500, err.getStatusCode());
    assertTrue(err.getMessage().contains("apiV1SomeMethod call failed with"));
  }
}
