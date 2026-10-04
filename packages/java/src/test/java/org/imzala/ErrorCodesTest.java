package org.imzala;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.api.TemplatesApi;
import org.imzala.client.generated.model.ApiV1TemplatesIdGet200Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Keeps the Java error catalogue in step with the public spec and with the
 * Node SDK catalogue (packages/node/src/errorCodes.ts), and covers how codes,
 * Retry-After and RateLimit-* headers are read off an error response.
 */
class ErrorCodesTest {

  /** Same list as packages/node/src/__tests__/errorCodes.test.ts. */
  private static final Map<String, String> NOT_ERROR_CODES = new LinkedHashMap<>();

  static {
    NOT_ERROR_CODES.put("ANCHOR_TEXT_NOT_FOUND", "field layout diagnostic code inside a response body");
    NOT_ERROR_CODES.put("CREATE_FAILED", "per-row result code in the bulk 200 response");
    NOT_ERROR_CODES.put("DISPATCH_FAILED", "per-party invitation result code in a 200 response");
    NOT_ERROR_CODES.put("DISPATCH_SKIPPED", "invitation result code in a 200 response");
    NOT_ERROR_CODES.put("DRAFT_UNDISPATCHED",
        "demand status value returned alongside an error, not a code itself");
    NOT_ERROR_CODES.put("RECIPIENT_QUOTA_EXCEEDED", "per-party invitation result code in a 200 response");
    NOT_ERROR_CODES.put("ON_ANCHOR_MISS_NOT_RELAXED", "warning in a 200 response, not an error");
    NOT_ERROR_CODES.put("DEAD_LETTER", "webhook delivery status");
    NOT_ERROR_CODES.put("DEMAND_ID", "shell variable in a curl example");
    NOT_ERROR_CODES.put("ENVELOPE_DECISION_ENFORCE", "server feature flag name");
    NOT_ERROR_CODES.put("FIELD_LAYOUT", "template kind value");
    NOT_ERROR_CODES.put("FILLABLE_TYPES", "server constant name in prose");
    NOT_ERROR_CODES.put("IMZALA_WEBHOOK_SECRET", "environment variable in a code sample");
    NOT_ERROR_CODES.put("KVKK_CONSENT", "doc_kind value");
    NOT_ERROR_CODES.put("KVKK_NOTICE", "doc_kind value");
    NOT_ERROR_CODES.put("PRICE_LIST", "doc_kind value");
    NOT_ERROR_CODES.put("WEBHOOK_TIMEOUT_MS", "server environment variable");
    NOT_ERROR_CODES.put("COMMENT_ADDED", "timeline event_type value");
    NOT_ERROR_CODES.put("FIELDS_FILLED", "timeline event_type value");
    NOT_ERROR_CODES.put("MOBILE_SIGNATURE_CAPTURED", "timeline event_type value");
    NOT_ERROR_CODES.put("OTP_LOCKED", "timeline event_type value");
    NOT_ERROR_CODES.put("OTP_SENT", "timeline event_type value");
    NOT_ERROR_CODES.put("OTP_VERIFIED", "timeline event_type value");
    NOT_ERROR_CODES.put("FROM_SAVED", "stamp source value");
    NOT_ERROR_CODES.put("FILLER_PROVIDES", "stamp source value");
    NOT_ERROR_CODES.put("STAMP_ITEM_ID", "shell variable in a curl example");
    NOT_ERROR_CODES.put("FIXED_DATE", "term_start_mode value");
    NOT_ERROR_CODES.put("ON_FIRST_SIGNATURE", "term_start_mode value");
    NOT_ERROR_CODES.put("ON_COMPLETION", "term_start_mode value");
    NOT_ERROR_CODES.put("AUTO_RENEW", "renewal_type value");
    NOT_ERROR_CODES.put("FIXED_TERM", "renewal_type value");
  }

  /**
   * Prose also contains single upper-case words (API, PDF, KVKK), so the
   * spec-to-catalogue direction only considers underscore tokens.
   */
  private static final Pattern CODE_TOKEN = Pattern.compile("\\b[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)+\\b");

  /** Walks up from Maven's working directory (packages/java) to the repository root. */
  private static Path repoRoot() {
    Path dir = Path.of("").toAbsolutePath();
    while (dir != null) {
      if (Files.isRegularFile(dir.resolve("spec/openapi.v1.yaml"))
          && Files.isRegularFile(dir.resolve("packages/node/src/errorCodes.ts"))) {
        return dir;
      }
      dir = dir.getParent();
    }
    throw new IllegalStateException("repository root not found above " + Path.of("").toAbsolutePath());
  }

  private static String read(String relative) {
    try {
      return Files.readString(repoRoot().resolve(relative), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private static boolean inSpec(String token) {
    return Pattern.compile("\\b" + Pattern.quote(token) + "\\b").matcher(read("spec/openapi.v1.yaml")).find();
  }

  private static Map<String, String> nodeCatalogue() {
    String src = read("packages/node/src/errorCodes.ts");
    String body = src.split("export const IMZALA_ERROR_CODES = \\{", 2)[1].split("\\} as const;", 2)[0];
    Matcher m = Pattern.compile("^\\s*([A-Z][A-Z0-9_]*):\\s*'([^']*)',", Pattern.MULTILINE).matcher(body);
    Map<String, String> out = new LinkedHashMap<>();
    while (m.find()) {
      out.put(m.group(1), m.group(2));
    }
    assertFalse(out.isEmpty(), "could not read any entry from errorCodes.ts");
    return out;
  }

  // --- catalogue <-> spec ---

  @Test
  void every_catalogued_code_appears_in_the_public_spec() {
    List<String> missing = new ArrayList<>();
    for (String code : ErrorCodes.CODES.keySet()) {
      if (!inSpec(code)) {
        missing.add(code);
      }
    }
    assertEquals(List.of(), missing);
  }

  @Test
  void every_code_like_token_in_the_spec_is_catalogued_or_excluded() {
    Matcher m = CODE_TOKEN.matcher(read("spec/openapi.v1.yaml"));
    Set<String> unclassified = new LinkedHashSet<>();
    while (m.find()) {
      String token = m.group();
      if (!ErrorCodes.isKnown(token) && !NOT_ERROR_CODES.containsKey(token)) {
        unclassified.add(token);
      }
    }
    assertEquals(Set.of(), unclassified);
  }

  @Test
  void the_exclusion_list_has_no_stale_or_overlapping_entries() {
    for (String token : NOT_ERROR_CODES.keySet()) {
      assertTrue(inSpec(token), token);
      assertFalse(ErrorCodes.isKnown(token), token);
    }
  }

  @Test
  void every_description_is_a_non_empty_single_line_without_an_em_dash() {
    for (Map.Entry<String, String> e : ErrorCodes.CODES.entrySet()) {
      assertTrue(e.getValue().strip().length() > 0, e.getKey());
      assertFalse(e.getValue().contains("\n"), e.getKey());
      assertFalse(e.getValue().contains("\u2014"), e.getKey());
    }
  }

  @Test
  void unknown_or_missing_codes_return_null_instead_of_throwing() {
    assertNull(ErrorCodes.describe("NEVER_A_REAL_CODE"));
    assertNull(ErrorCodes.describe(null));
    assertFalse(ErrorCodes.isKnown(null));
    assertFalse(ErrorCodes.isKnown("toString"));
    assertEquals(ErrorCodes.CODES.get("TEMPLATE_IN_USE"), ErrorCodes.describe("TEMPLATE_IN_USE"));
  }

  @Test
  void the_catalogue_is_read_only() {
    assertThrows(UnsupportedOperationException.class, () -> ErrorCodes.CODES.put("X_Y", "z"));
  }

  // --- catalogue <-> Node SDK ---

  @Test
  void same_code_set_as_the_node_catalogue() {
    assertEquals(new TreeSet<>(nodeCatalogue().keySet()), new TreeSet<>(ErrorCodes.CODES.keySet()));
    assertEquals(98, ErrorCodes.CODES.size());
  }

  @Test
  void same_descriptions_as_the_node_catalogue() {
    Map<String, String> node = nodeCatalogue();
    List<String> drifted = new ArrayList<>();
    for (Map.Entry<String, String> e : ErrorCodes.CODES.entrySet()) {
      if (!e.getValue().equals(node.get(e.getKey()))) {
        drifted.add(e.getKey());
      }
    }
    assertEquals(List.of(), drifted);
  }

  // --- error body shapes ---

  private static ApiException apiException(int status, String body) {
    return apiException(status, body, Map.of());
  }

  private static ApiException apiException(int status, String body, Map<String, List<String>> headers) {
    return new ApiException("[" + status + "] error", status, HttpHeaders.of(headers, (a, b) -> true), body);
  }

  @Test
  void error_code_with_message_reads_the_code_from_error() {
    ImzalaException e = ErrorMapper.map(apiException(400, """
        {"success":false,"error":"PAGE_ID_REQUIRED","message":"Each item must have an integer page_id"}"""));
    assertEquals("PAGE_ID_REQUIRED", e.getCode());
    assertEquals(ErrorCodes.CODES.get("PAGE_ID_REQUIRED"), e.getCodeDescription());
  }

  @Test
  void human_text_with_code_reads_the_code_never_the_text() {
    ImzalaException e = ErrorMapper.map(apiException(400, """
        {"success":false,"error":"Geçersiz sayfa numarası (page >= 1 olmalı)","code":"INVALID_PAGE"}"""));
    assertEquals("INVALID_PAGE", e.getCode());
    assertEquals("Geçersiz sayfa numarası (page >= 1 olmalı)", e.getMessage());
    assertEquals(ErrorCodes.CODES.get("INVALID_PAGE"), e.getCodeDescription());
  }

  @Test
  void code_wins_over_a_code_shaped_error_string() {
    ImzalaException e = ErrorMapper.map(apiException(429, """
        {"success":false,"error":"TOO_MANY_REQUESTS","code":"RECIPIENT_RESEND_LIMIT"}"""));
    assertEquals("RECIPIENT_RESEND_LIMIT", e.getCode());
  }

  @Test
  void nested_error_object_reads_the_nested_code() {
    ImzalaException e = ErrorMapper.map(apiException(409, """
        {"success":false,"error":{"code":"DEMAND_NOT_DISPATCHABLE","message":"x"}}"""));
    assertEquals("DEMAND_NOT_DISPATCHABLE", e.getCode());
  }

  @Test
  void a_single_word_code_in_error_is_still_a_code() {
    assertEquals("UNAUTHORIZED", ErrorMapper.map(apiException(401, "{\"error\":\"UNAUTHORIZED\"}")).getCode());
  }

  @Test
  void a_plain_human_readable_error_string_is_not_reported_as_a_code() {
    ImzalaException e = ErrorMapper.map(apiException(404, """
        {"success":false,"error":"Sözleşme bulunamadı"}"""));
    assertNull(e.getCode());
    assertEquals("Sözleşme bulunamadı", e.getMessage());
    assertNull(e.getCodeDescription());
  }

  @Test
  void short_or_lowercase_error_strings_are_not_codes() {
    assertNull(ErrorMapper.map(apiException(400, "{\"error\":\"OK\"}")).getCode());
    assertNull(ErrorMapper.map(apiException(400, "{\"error\":\"not_found\"}")).getCode());
    assertNull(ErrorMapper.map(apiException(400, "{\"error\":\"BAD CODE\"}")).getCode());
  }

  @Test
  void unknown_code_is_kept_without_a_description() {
    ImzalaException e = ErrorMapper.map(apiException(409, "{\"success\":false,\"code\":\"SOME_FUTURE_CODE\"}"));
    assertEquals("SOME_FUTURE_CODE", e.getCode());
    assertNull(e.getCodeDescription());
  }

  @Test
  void constructing_an_exception_directly_fills_code_description() {
    assertEquals(ErrorCodes.CODES.get("TEMPLATE_IN_USE"),
        new ImzalaException("x", 409, null, "TEMPLATE_IN_USE", null).getCodeDescription());
    assertNull(new ImzalaException("x").getCodeDescription());
    assertEquals(ErrorCodes.CODES.get("RATE_LIMITED"),
        new ImzalaRateLimitException("x", 429, null, "RATE_LIMITED", null, null).getCodeDescription());
  }

  // --- 2xx + success:false through the real generated model ---

  /**
   * Known limit, same as Python and PHP: the generated model for a 200
   * declares only {@code success} and {@code data}, and the generated client
   * drops unknown properties while deserializing. The body's {@code error}
   * and {@code code} are gone before the facade sees the response, so the
   * code cannot be read on this path. The call still fails loudly. Uses the
   * generated client's own ObjectMapper, not a hand-built model.
   */
  @Test
  void success_false_on_a_2xx_with_the_real_generated_model_still_throws_but_has_no_code() throws Exception {
    ObjectMapper generatedMapper = ApiClient.createDefaultObjectMapper();
    ApiV1TemplatesIdGet200Response model = generatedMapper.readValue("""
        {"success":false,"error":"Geçersiz istek","code":"VALIDATION_FAIL"}""", ApiV1TemplatesIdGet200Response.class);
    assertNotNull(model);
    assertEquals(Boolean.FALSE, model.getSuccess());

    TemplatesApi api = mock(TemplatesApi.class);
    UUID id = UUID.randomUUID();
    when(api.apiV1TemplatesIdGet(id)).thenReturn(model);

    ImzalaException e = assertThrows(ImzalaException.class,
        () -> new TemplatesResource(api, new RetryConfig(0, 0)).get(id));
    assertNull(e.getCode());
  }

  // --- rate limit ---

  @Test
  void attaches_the_description_and_reads_retry_after_from_the_header() {
    ImzalaException e = ErrorMapper.map(apiException(429, """
        {"success":false,"error":"Çok fazla istek","code":"RATE_LIMIT_EXCEEDED"}""",
        Map.of("retry-after", List.of("30"))));
    ImzalaRateLimitException rl = assertInstanceOf(ImzalaRateLimitException.class, e);
    assertEquals("RATE_LIMIT_EXCEEDED", rl.getCode());
    assertEquals(ErrorCodes.CODES.get("RATE_LIMIT_EXCEEDED"), rl.getCodeDescription());
    assertEquals(30.0, rl.getRetryAfter());
  }

  private static String httpDate(long secondsFromNow) {
    return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(secondsFromNow));
  }

  @Test
  void an_http_date_retry_after_is_converted_to_seconds() {
    ImzalaException e = ErrorMapper.map(apiException(429, "{\"success\":false,\"code\":\"RATE_LIMIT_EXCEEDED\"}",
        Map.of("Retry-After", List.of(httpDate(30)))));
    ImzalaRateLimitException rl = assertInstanceOf(ImzalaRateLimitException.class, e);
    assertTrue(rl.getRetryAfter() >= 28 && rl.getRetryAfter() <= 30, String.valueOf(rl.getRetryAfter()));
  }

  @Test
  void a_past_http_date_retry_after_is_zero_not_negative() {
    ImzalaException e = ErrorMapper.map(apiException(429, "{\"success\":false}",
        Map.of("Retry-After", List.of(httpDate(-120)))));
    assertEquals(0.0, assertInstanceOf(ImzalaRateLimitException.class, e).getRetryAfter());
  }

  @Test
  void an_unparseable_retry_after_is_null() {
    ImzalaException e = ErrorMapper.map(apiException(429, "{\"success\":false}",
        Map.of("Retry-After", List.of("yakında"))));
    assertNull(assertInstanceOf(ImzalaRateLimitException.class, e).getRetryAfter());
  }

  @Test
  void reads_retry_after_seconds_from_the_body_when_there_is_no_header() {
    ImzalaException e = ErrorMapper.map(apiException(429, """
        {"success":false,"error":"Çok fazla istek gönderildi. Lütfen bir dakika bekleyin.","code":"RATE_LIMIT_EXCEEDED","retry_after_seconds":60}""",
        Map.of("ratelimit-limit", List.of("60"), "ratelimit-policy", List.of("60;w=60"))));
    ImzalaRateLimitException rl = assertInstanceOf(ImzalaRateLimitException.class, e);
    assertEquals("RATE_LIMIT_EXCEEDED", rl.getCode());
    assertEquals(60.0, rl.getRetryAfter());
  }

  @Test
  void reads_the_standard_rate_limit_headers_and_ignores_x_rate_limit() {
    Map<String, List<String>> headers = new LinkedHashMap<>();
    headers.put("RateLimit-Limit", List.of("5"));
    headers.put("RateLimit-Remaining", List.of("0"));
    headers.put("RateLimit-Reset", List.of("42"));
    headers.put("RateLimit-Policy", List.of("5;w=60"));
    headers.put("X-RateLimit-Limit", List.of("999"));
    ImzalaException e = ErrorMapper.map(apiException(429,
        "{\"success\":false,\"code\":\"RATE_LIMIT_EXCEEDED\",\"retry_after_seconds\":60}", headers));
    RateLimitInfo info = assertInstanceOf(ImzalaRateLimitException.class, e).getRateLimit();
    assertNotNull(info);
    assertEquals(5, info.getLimit());
    assertEquals(0, info.getRemaining());
    assertEquals(42, info.getReset());
    assertEquals("5;w=60", info.getPolicy());
  }

  @Test
  void header_names_are_read_case_insensitively() {
    ImzalaException e = ErrorMapper.map(apiException(429, "{\"success\":false}",
        Map.of("ratelimit-limit", List.of("60"), "RETRY-AFTER", List.of("7"))));
    ImzalaRateLimitException rl = assertInstanceOf(ImzalaRateLimitException.class, e);
    assertEquals(60, rl.getRateLimit().getLimit());
    assertNull(rl.getRateLimit().getRemaining());
    assertNull(rl.getRateLimit().getPolicy());
    assertEquals(7.0, rl.getRetryAfter());
  }

  @Test
  void only_x_rate_limit_headers_leave_rate_limit_null() {
    ImzalaException e = ErrorMapper.map(apiException(429, "{\"success\":false}",
        Map.of("X-RateLimit-Limit", List.of("60"), "X-RateLimit-Remaining", List.of("0"))));
    assertNull(assertInstanceOf(ImzalaRateLimitException.class, e).getRateLimit());
  }

  @Test
  void leaves_rate_limit_null_when_the_server_sent_no_rate_limit_headers() {
    ImzalaException e = ErrorMapper.map(apiException(429, "{\"success\":false,\"error\":{\"code\":\"RATE_LIMITED\"}}"));
    ImzalaRateLimitException rl = assertInstanceOf(ImzalaRateLimitException.class, e);
    assertNull(rl.getRateLimit());
    assertEquals("RATE_LIMITED", rl.getCode());
  }

  @Test
  void the_old_rate_limit_constructor_still_works() {
    ImzalaRateLimitException rl = new ImzalaRateLimitException("x", 429, null, null, 5.0, null);
    assertEquals(5.0, rl.getRetryAfter());
    assertNull(rl.getRateLimit());
  }
}
