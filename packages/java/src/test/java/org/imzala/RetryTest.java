package org.imzala;

import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.api.DemandsApi;
import org.imzala.client.generated.api.RemindersApi;
import org.imzala.client.generated.api.TemplatesApi;
import org.imzala.client.generated.model.ApiV1DemandsIdGet200Response;
import org.imzala.client.generated.model.ApiV1TemplatesGet200Response;
import org.imzala.client.generated.model.ApiV1TemplatesGet200ResponseData;
import org.imzala.client.generated.model.CreateDemandRequest;
import org.imzala.client.generated.model.DemandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

/**
 * Safe auto-retry tests — mirrors {@code retry.test.ts} (Node, 7 tests),
 * {@code test_retry.py} (Python), {@code RetryTests.cs} (.NET), and
 * {@code RetryTest.php} (PHP): mock the vendored generated client's
 * concrete {@code *Api} classes directly (same seam as {@link ClientTest})
 * and assert {@code Http.unwrapRetryableGet} (exercised transitively
 * through {@link TemplatesResource#list}/{@link DemandsResource#get}/
 * {@link AccountResource#me}) retries on 429/5xx and never on anything
 * else — plus the critical safety guarantee that a POST ({@link
 * DemandsResource#create}) is <b>never</b> retried, even on a normally
 * retryable status.
 *
 * <p>Uses {@code retryBaseDelayMs = 1} (like the .NET port) so the real
 * (non-fake-timer) exponential backoff + jitter sleeps add only a few ms
 * total, and omits {@code retry_after_seconds}/{@code Retry-After} from
 * the 429 fixtures so the fast exponential path is taken instead of a
 * potentially large {@code Retry-After}-derived delay.
 */
@ExtendWith(MockitoExtension.class)
class RetryTest {

  @Mock
  private TemplatesApi templatesApi;
  @Mock
  private DemandsApi demandsApi;
  @Mock
  private RemindersApi remindersApi;

  private static ApiException fakeApiException(int status, String jsonBody) {
    return fakeApiException(status, jsonBody, Map.of());
  }

  private static ApiException fakeApiException(int status, String jsonBody, Map<String, List<String>> headers) {
    HttpHeaders httpHeaders = HttpHeaders.of(headers, (a, b) -> true);
    return new ApiException("apiV1SomeMethod call failed with: " + status + " - " + jsonBody, status, httpHeaders, jsonBody);
  }

  private static int callCount(Object mock) {
    return mockingDetails(mock).getInvocations().size();
  }

  @Test
  void get_retries_on_429_twice_then_succeeds_on_third_attempt() throws ApiException {
    when(templatesApi.apiV1TemplatesGet(1, 10))
        .thenThrow(fakeApiException(429, """
            {"success":false,"error":"RATE_LIMITED"}"""))
        .thenThrow(fakeApiException(429, """
            {"success":false,"error":"RATE_LIMITED"}"""))
        .thenReturn(new ApiV1TemplatesGet200Response().success(true)
            .data(new ApiV1TemplatesGet200ResponseData().total(0).page(1).limit(10)));

    TemplatesResource resource = new TemplatesResource(templatesApi, new RetryConfig(2, 1));
    ApiV1TemplatesGet200ResponseData result = resource.list(1, 10);

    assertEquals(0, result.getTotal());
    assertEquals(3, callCount(templatesApi));
  }

  @Test
  void get_retries_on_5xx_and_succeeds() throws ApiException {
    UUID id = UUID.randomUUID();
    when(demandsApi.apiV1DemandsIdGet(id))
        .thenThrow(fakeApiException(503, """
            {"success":false,"error":"SERVICE_UNAVAILABLE"}"""))
        .thenReturn(new ApiV1DemandsIdGet200Response().success(true)
            .data(new DemandStatus().id(id).status(DemandStatus.StatusEnum.PENDING)));

    DemandsResource resource = new DemandsResource(demandsApi, remindersApi, new RetryConfig(2, 1));
    DemandStatus result = resource.get(id);

    assertEquals(id, result.getId());
    assertEquals(2, callCount(demandsApi));
  }

  @Test
  void get_does_not_retry_a_non429_4xx() throws ApiException {
    UUID id = UUID.randomUUID();
    when(demandsApi.apiV1DemandsIdGet(id)).thenThrow(fakeApiException(404, """
        {"success":false,"error":"DEMAND_NOT_FOUND"}"""));

    DemandsResource resource = new DemandsResource(demandsApi, remindersApi, new RetryConfig(2, 1));

    ImzalaException err = assertThrows(ImzalaException.class, () -> resource.get(id));
    assertEquals(404, err.getStatusCode());
    assertEquals(1, callCount(demandsApi));
  }

  @Test
  void maxRetries_zero_disables_retry_entirely() throws ApiException {
    when(templatesApi.apiV1TemplatesGet(1, 10)).thenThrow(fakeApiException(429, """
        {"success":false,"error":"RATE_LIMITED"}"""));

    TemplatesResource resource = new TemplatesResource(templatesApi, new RetryConfig(0, 1));

    assertThrows(ImzalaRateLimitException.class, () -> resource.list(1, 10));
    assertEquals(1, callCount(templatesApi));
  }

  @Test
  void exhausts_retries_and_throws_after_maxRetries_attempts() throws ApiException {
    when(templatesApi.apiV1TemplatesGet(1, 10)).thenThrow(fakeApiException(503, """
        {"success":false,"error":"SERVICE_UNAVAILABLE"}"""));

    TemplatesResource resource = new TemplatesResource(templatesApi, new RetryConfig(2, 1));

    ImzalaException err = assertThrows(ImzalaException.class, () -> resource.list(1, 10));
    assertEquals(503, err.getStatusCode());
    // 1 initial attempt + 2 retries = 3 calls total.
    assertEquals(3, callCount(templatesApi));
  }

  /**
   * SAFETY: the single most important test in this file. A retried POST
   * would create a duplicate demand — {@link DemandsResource#create} must
   * never reach the retry loop, regardless of how "retryable" the failing
   * status looks.
   */
  @Test
  void POST_create_returning_429_is_never_retried() throws ApiException {
    when(demandsApi.apiV1DemandsPost(any(CreateDemandRequest.class)))
        .thenThrow(fakeApiException(429, """
            {"success":false,"error":"RATE_LIMITED"}"""));

    DemandsResource resource = new DemandsResource(demandsApi, remindersApi, new RetryConfig(2, 1));
    CreateDemandRequest body = new CreateDemandRequest().templateId(UUID.randomUUID());

    ImzalaRateLimitException err = assertThrows(ImzalaRateLimitException.class, () -> resource.create(body));
    assertEquals(429, err.getStatusCode());
    assertEquals(1, callCount(demandsApi));
  }

  /** SAFETY: same as above, but for a 503 — proves it's not just the 429 short-circuit, the write path has no retry loop at all. */
  @Test
  void POST_create_returning_5xx_is_never_retried() throws ApiException {
    when(demandsApi.apiV1DemandsPost(any(CreateDemandRequest.class)))
        .thenThrow(fakeApiException(503, """
            {"success":false,"error":"SERVICE_UNAVAILABLE"}"""));

    DemandsResource resource = new DemandsResource(demandsApi, remindersApi, new RetryConfig(2, 1));
    CreateDemandRequest body = new CreateDemandRequest().templateId(UUID.randomUUID());

    ImzalaException err = assertThrows(ImzalaException.class, () -> resource.create(body));
    assertEquals(503, err.getStatusCode());
    assertEquals(1, callCount(demandsApi));
  }
}
