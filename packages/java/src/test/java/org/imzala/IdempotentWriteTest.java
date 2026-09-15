package org.imzala;

import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.api.ContactsApi;
import org.imzala.client.generated.api.DemandsApi;
import org.imzala.client.generated.api.RemindersApi;
import org.imzala.client.generated.api.TemplatesApi;
import org.imzala.client.generated.api.TimestampsApi;
import org.imzala.client.generated.model.ApiV1ContactsPost201Response;
import org.imzala.client.generated.model.ApiV1ContactsPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsBulkPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsBulkPost200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsBulkPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsPost201Response;
import org.imzala.client.generated.model.ApiV1DemandsUploadPost201Response;
import org.imzala.client.generated.model.ApiV1TemplatesGet200Response;
import org.imzala.client.generated.model.ApiV1TemplatesGet200ResponseData;
import org.imzala.client.generated.model.ApiV1TimestampsPost201Response;
import org.imzala.client.generated.model.ContactSummary;
import org.imzala.client.generated.model.CreateDemandRequest;
import org.imzala.client.generated.model.CreatedDemand;
import org.imzala.client.generated.model.CreatedDemandUpload;
import org.imzala.client.generated.model.TimestampRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A write is retried only when it carries an Idempotency-Key and the server
 * answered 429, exactly once, waiting Retry-After (capped at 60 s). Mirrors
 * the Node {@code unwrapIdempotentWrite} tests and PHP IdempotentWriteTest.
 * Waits go through an injected sleeper, so nothing here really sleeps.
 */
@ExtendWith(MockitoExtension.class)
class IdempotentWriteTest {

  @Mock
  private DemandsApi demandsApi;
  @Mock
  private RemindersApi remindersApi;
  @Mock
  private TimestampsApi timestampsApi;
  @Mock
  private ContactsApi contactsApi;
  @Mock
  private TemplatesApi templatesApi;

  /** Records every requested wait instead of sleeping. */
  static final class RecordingSleeper implements Http.Sleeper {
    final List<Long> waits = new ArrayList<>();

    @Override
    public void sleep(long millis) {
      waits.add(millis);
    }
  }

  private final RecordingSleeper sleeper = new RecordingSleeper();

  private RetryConfig retry() {
    return new RetryConfig(2, 300, sleeper);
  }

  static ApiException status(int status, String body) {
    return status(status, body, Map.of());
  }

  static ApiException status(int status, String body, Map<String, List<String>> headers) {
    return new ApiException("[" + status + "] error", status, HttpHeaders.of(headers, (a, b) -> true), body);
  }

  static ApiException tooMany(String retryAfterSeconds) {
    return status(429, "{\"success\":false,\"code\":\"RATE_LIMIT_EXCEEDED\"}",
        retryAfterSeconds == null ? Map.of() : Map.of("Retry-After", List.of(retryAfterSeconds)));
  }

  private static int calls(Object mock) {
    return mockingDetails(mock).getInvocations().size();
  }

  private static ApiV1DemandsPost201Response created() {
    return new ApiV1DemandsPost201Response().success(true).data(new CreatedDemand().id(UUID.randomUUID()));
  }

  private static Http.ApiCall<ApiV1DemandsPost201Response> scripted(List<Object> script, int[] counter) {
    return () -> {
      Object next = script.get(counter[0]++);
      if (next instanceof ApiException ex) {
        throw ex;
      }
      return (ApiV1DemandsPost201Response) next;
    };
  }

  private CreatedDemand runWrite(List<Object> script, int[] counter, String key, long maxWaitMs) {
    return Http.unwrapIdempotentWrite(
        scripted(script, counter), r -> Boolean.TRUE.equals(r.getSuccess()), r -> r.getData(), key, retry(), maxWaitMs);
  }

  private CreatedDemand runWrite(List<Object> script, int[] counter, String key) {
    return Http.unwrapIdempotentWrite(
        scripted(script, counter), r -> Boolean.TRUE.equals(r.getSuccess()), r -> r.getData(), key, retry());
  }

  // --- Http.unwrapIdempotentWrite ---

  @Test
  void a_write_without_an_idempotency_key_is_never_retried_on_429() {
    int[] n = {0};
    assertThrows(ImzalaRateLimitException.class, () -> runWrite(List.of(tooMany("1"), created()), n, null));
    assertEquals(1, n[0]);
    assertEquals(List.of(), sleeper.waits);
  }

  @Test
  void an_empty_idempotency_key_counts_as_no_key() {
    int[] n = {0};
    assertThrows(ImzalaRateLimitException.class, () -> runWrite(List.of(tooMany("1"), created()), n, ""));
    assertEquals(1, n[0]);
  }

  @Test
  void a_write_with_an_idempotency_key_is_retried_exactly_once_on_429() {
    int[] n = {0};
    assertNotNull(runWrite(List.of(tooMany("1"), created()), n, "order-1"));
    assertEquals(2, n[0]);
  }

  @Test
  void a_second_429_is_thrown_not_retried_again() {
    int[] n = {0};
    assertThrows(ImzalaRateLimitException.class,
        () -> runWrite(List.of(tooMany("1"), tooMany("1"), created()), n, "order-1"));
    assertEquals(2, n[0]);
  }

  @Test
  void non_429_errors_are_not_retried_even_5xx() {
    for (int code : new int[] {500, 502, 503, 400, 409, 422}) {
      int[] n = {0};
      ImzalaException e = assertThrows(ImzalaException.class,
          () -> runWrite(List.of(status(code, "{\"success\":false}"), created()), n, "order-1"));
      assertEquals(code, e.getStatusCode());
      assertEquals(1, n[0], "status " + code);
    }
    assertEquals(List.of(), sleeper.waits);
  }

  @Test
  void throws_instead_of_waiting_when_retry_after_exceeds_the_cap() {
    int[] n = {0};
    ImzalaRateLimitException e = assertThrows(ImzalaRateLimitException.class,
        () -> runWrite(List.of(tooMany("61"), created()), n, "order-1"));
    assertEquals(61.0, e.getRetryAfter());
    assertEquals(1, n[0]);
    assertEquals(List.of(), sleeper.waits);
  }

  @Test
  void exactly_sixty_seconds_is_still_waited() {
    int[] n = {0};
    assertNotNull(runWrite(List.of(tooMany("60"), created()), n, "order-1"));
    assertEquals(List.of(60_000L), sleeper.waits);
    assertEquals(60_000L, Http.MAX_RETRY_WAIT_MS);
  }

  @Test
  void a_custom_cap_is_honoured() {
    int[] n = {0};
    assertThrows(ImzalaRateLimitException.class, () -> runWrite(List.of(tooMany("5"), created()), n, "order-1", 4_000));
    assertEquals(1, n[0]);
  }

  @Test
  void waits_for_retry_after_before_the_retry() {
    int[] n = {0};
    List<Object> script = new ArrayList<>();
    script.add(tooMany("2"));
    script.add(created());
    Http.ApiCall<ApiV1DemandsPost201Response> call = () -> {
      if (n[0] == 1) {
        // the wait must already have happened when the retry goes out
        assertEquals(List.of(2_000L), sleeper.waits);
      }
      Object next = script.get(n[0]++);
      if (next instanceof ApiException ex) {
        throw ex;
      }
      return (ApiV1DemandsPost201Response) next;
    };
    Http.unwrapIdempotentWrite(call, r -> Boolean.TRUE.equals(r.getSuccess()), r -> r.getData(), "k", retry());
    assertEquals(2, n[0]);
  }

  @Test
  void without_retry_after_the_base_delay_is_waited() {
    int[] n = {0};
    assertNotNull(runWrite(List.of(tooMany(null), created()), n, "order-1"));
    assertEquals(List.of(300L), sleeper.waits);
  }

  // --- GET auto-retry shares the same 60 s cap ---

  @Test
  void get_throws_without_waiting_when_retry_after_exceeds_sixty_seconds() throws ApiException {
    when(templatesApi.apiV1TemplatesGet(1, 10)).thenThrow(tooMany("120"))
        .thenReturn(new ApiV1TemplatesGet200Response().success(true).data(new ApiV1TemplatesGet200ResponseData()));

    TemplatesResource resource = new TemplatesResource(templatesApi, retry());
    assertThrows(ImzalaRateLimitException.class, () -> resource.list(1, 10));
    assertEquals(1, calls(templatesApi));
    assertEquals(List.of(), sleeper.waits);
  }

  @Test
  void get_still_waits_exactly_sixty_seconds() throws ApiException {
    when(templatesApi.apiV1TemplatesGet(1, 10)).thenThrow(tooMany("60"))
        .thenReturn(new ApiV1TemplatesGet200Response().success(true).data(new ApiV1TemplatesGet200ResponseData()));

    new TemplatesResource(templatesApi, retry()).list(1, 10);
    assertEquals(2, calls(templatesApi));
    assertEquals(List.of(60_000L), sleeper.waits);
  }

  @Test
  void get_waits_through_the_injected_sleeper() throws ApiException {
    when(templatesApi.apiV1TemplatesGet(1, 10)).thenThrow(tooMany("3"))
        .thenReturn(new ApiV1TemplatesGet200Response().success(true).data(new ApiV1TemplatesGet200ResponseData()));

    new TemplatesResource(templatesApi, retry()).list(1, 10);
    assertEquals(List.of(3_000L), sleeper.waits);
  }

  // --- resources ---

  @Test
  void demands_create_with_key_retries_once_and_sends_the_key_in_its_own_slot() throws ApiException {
    CreateDemandRequest body = new CreateDemandRequest().templateId(UUID.randomUUID());
    when(demandsApi.apiV1DemandsPost(any(CreateDemandRequest.class), any()))
        .thenThrow(tooMany("1"))
        .thenReturn(created());

    CreatedDemand result = new DemandsResource(demandsApi, remindersApi, retry()).create(body, "order-2026-001");

    assertNotNull(result.getId());
    verify(demandsApi, times(2)).apiV1DemandsPost(eq(body), eq("order-2026-001"));
    assertEquals(List.of(1_000L), sleeper.waits);
  }

  @Test
  void demands_create_without_key_is_a_single_attempt() throws ApiException {
    CreateDemandRequest body = new CreateDemandRequest().templateId(UUID.randomUUID());
    when(demandsApi.apiV1DemandsPost(any(CreateDemandRequest.class), any())).thenThrow(tooMany("1"));

    DemandsResource resource = new DemandsResource(demandsApi, remindersApi, retry());
    assertThrows(ImzalaRateLimitException.class, () -> resource.create(body));
    assertThrows(ImzalaRateLimitException.class, () -> resource.create(body, null));
    verify(demandsApi, times(2)).apiV1DemandsPost(eq(body), isNull());
    assertEquals(List.of(), sleeper.waits);
  }

  @Test
  void demands_create_bulk_is_never_retried_on_429() throws ApiException {
    when(demandsApi.apiV1DemandsBulkPost(any(), any())).thenThrow(tooMany("1"))
        .thenReturn(new ApiV1DemandsBulkPost200Response().success(true).data(new ApiV1DemandsBulkPost200ResponseData()));

    DemandsResource resource = new DemandsResource(demandsApi, remindersApi, retry());
    assertThrows(ImzalaRateLimitException.class,
        () -> resource.createBulk(new ApiV1DemandsBulkPostRequest().templateId(UUID.randomUUID())));
    assertEquals(1, calls(demandsApi));
    assertEquals(List.of(), sleeper.waits);
  }

  @Test
  void demands_create_bulk_accepts_no_idempotency_key() throws NoSuchMethodException {
    for (var m : DemandsResource.class.getMethods()) {
      if (m.getName().equals("createBulk")) {
        assertEquals(1, m.getParameterCount(), m.toString());
      }
    }
    assertNotNull(DemandsResource.class.getMethod("createBulk", ApiV1DemandsBulkPostRequest.class));
  }

  @Test
  void contacts_create_is_never_retried_on_429_and_accepts_no_key() throws ApiException {
    when(contactsApi.apiV1ContactsPost(any())).thenThrow(tooMany("1"))
        .thenReturn(new ApiV1ContactsPost201Response().success(true).data(new ContactSummary()));

    ContactsResource resource = new ContactsResource(contactsApi, retry());
    assertThrows(ImzalaRateLimitException.class,
        () -> resource.create(new ApiV1ContactsPostRequest().firstName("Ayşe").lastName("Yılmaz")));
    assertEquals(1, calls(contactsApi));
    assertEquals(List.of(), sleeper.waits);
    for (var m : ContactsResource.class.getMethods()) {
      if (m.getName().equals("create")) {
        assertEquals(1, m.getParameterCount(), m.toString());
      }
    }
  }

  private static UploadDemandParams uploadParams() {
    return new UploadDemandParams(
        List.of(new FileInput("%PDF-1.4".getBytes(), "sozlesme.pdf")),
        List.of(new UploadPartyInput("Ayşe", "Yılmaz", "ayse@example.com", "+905551112233")));
  }

  @Test
  void upload_document_with_key_retries_once_with_the_key_in_slot_three() throws ApiException {
    when(demandsApi.apiV1DemandsUploadPost(anyList(), anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenThrow(tooMany("1"))
        .thenReturn(new ApiV1DemandsUploadPost201Response().success(true).data(new CreatedDemandUpload().id(UUID.randomUUID())));

    CreatedDemandUpload result = new DemandsResource(demandsApi, remindersApi, retry())
        .uploadDocument(uploadParams().idempotencyKey("upload-7"));

    assertNotNull(result.getId());
    verify(demandsApi, times(2)).apiV1DemandsUploadPost(anyList(), anyString(), eq("upload-7"),
        isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
    assertEquals(List.of(1_000L), sleeper.waits);
  }

  @Test
  void upload_document_without_key_is_a_single_attempt() throws ApiException {
    when(demandsApi.apiV1DemandsUploadPost(anyList(), anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenThrow(tooMany("1"));

    DemandsResource resource = new DemandsResource(demandsApi, remindersApi, retry());
    assertThrows(ImzalaRateLimitException.class, () -> resource.uploadDocument(uploadParams()));
    assertEquals(1, calls(demandsApi));
    assertEquals(List.of(), sleeper.waits);
  }

  @Test
  void upload_document_retry_resends_existing_files_and_cleans_up_afterwards() throws ApiException {
    List<List<File>> seen = new ArrayList<>();
    when(demandsApi.apiV1DemandsUploadPost(anyList(), anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenAnswer(inv -> {
          List<File> files = inv.getArgument(0);
          assertTrue(files.get(0).exists(), "temp file must exist while the request is sent");
          seen.add(files);
          if (seen.size() == 1) {
            throw tooMany("1");
          }
          return new ApiV1DemandsUploadPost201Response().success(true).data(new CreatedDemandUpload());
        });

    new DemandsResource(demandsApi, remindersApi, retry()).uploadDocument(uploadParams().idempotencyKey("k-1"));

    assertEquals(2, seen.size());
    assertTrue(seen.stream().allMatch(files -> !files.get(0).exists()));
  }

  private static CreateTimestampParams timestampParams() {
    return new CreateTimestampParams("eser".getBytes(), "eser.txt");
  }

  @Test
  void timestamps_create_with_key_retries_once_with_the_key_in_slot_two() throws ApiException {
    TimestampRecord record = new TimestampRecord();
    int[] attempt = {0};
    when(timestampsApi.apiV1TimestampsPost(any(File.class), any(), any(), any(), any()))
        .thenAnswer(inv -> {
          File file = inv.getArgument(0);
          assertTrue(file.exists(), "temp file must still exist on every attempt");
          if (attempt[0]++ == 0) {
            throw tooMany("1");
          }
          return new ApiV1TimestampsPost201Response().success(true).data(record);
        });

    TimestampRecord result = new TimestampsResource(timestampsApi, retry())
        .create(timestampParams().idempotencyKey("ts-1").description("Taslak").ownerFirstName("Ayşe").ownerLastName("Yılmaz"));

    assertSame(record, result);
    verify(timestampsApi, times(2)).apiV1TimestampsPost(any(File.class), eq("ts-1"), eq("Taslak"), eq("Ayşe"), eq("Yılmaz"));
    assertEquals(List.of(1_000L), sleeper.waits);
  }

  @Test
  void a_key_that_is_not_a_valid_header_value_surfaces_as_imzala_validation_exception() {
    // Real generated client: the JDK rejects the header before any request is sent.
    DemandsResource resource =
        new DemandsResource(new DemandsApi(), org.mockito.Mockito.mock(RemindersApi.class), retry());
    ImzalaException err = assertThrows(ImzalaException.class,
        () -> resource.create(new CreateDemandRequest().templateId(java.util.UUID.randomUUID()), "sipariş-1"));
    assertInstanceOf(ImzalaValidationException.class, err);
    assertNull(err.getStatusCode());
    assertInstanceOf(IllegalArgumentException.class, err.getCause());
  }

  @Test
  void timestamps_create_without_key_is_a_single_attempt() throws ApiException {
    when(timestampsApi.apiV1TimestampsPost(any(File.class), any(), any(), any(), any())).thenThrow(tooMany("1"));

    TimestampsResource resource = new TimestampsResource(timestampsApi, retry());
    assertInstanceOf(ImzalaRateLimitException.class,
        assertThrows(ImzalaException.class, () -> resource.create(timestampParams())));
    assertEquals(1, calls(timestampsApi));
    assertEquals(List.of(), sleeper.waits);
  }
}
