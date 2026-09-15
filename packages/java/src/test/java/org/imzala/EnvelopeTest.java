package org.imzala;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDispatchPost200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsDocIdPatchRequest;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsGet200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsPost201ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsPostRequest;
import org.imzala.client.generated.model.ApiV1TemplatesIdDelete200ResponseData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Multi-document envelope endpoints ({@code demands().documents()}) and
 * {@code demands().dispatch()}. These tests run the real vendored generated
 * client and {@code java.net.http.HttpClient} against a local HTTP server,
 * so they check what actually goes over the wire: the path slots, the
 * multipart field that carries the idempotency key, and how a 409 replay
 * body is read back. Mirrors packages/node/src/__tests__/envelope.test.ts.
 */
class EnvelopeTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  private static final UUID DEMAND = UUID.fromString("11111111-1111-4111-8111-111111111111");
  private static final UUID DOC = UUID.fromString("22222222-2222-4222-8222-222222222222");
  private static final UUID DOC_B = UUID.fromString("33333333-3333-4333-8333-333333333333");
  private static final UUID PARTY = UUID.fromString("44444444-4444-4444-8444-444444444444");

  /** One recorded request. */
  static final class Captured {
    final String method;
    final String uri;
    final Headers headers;
    final String body;

    Captured(String method, String uri, Headers headers, String body) {
      this.method = method;
      this.uri = uri;
      this.headers = headers;
      this.body = body;
    }
  }

  /** One scripted reply. */
  static final class Reply {
    final int status;
    final Object body;
    final Map<String, String> headers;

    Reply(int status, Object body, Map<String, String> headers) {
      this.status = status;
      this.body = body;
      this.headers = headers;
    }

    Reply(int status, Object body) {
      this(status, body, Collections.emptyMap());
    }
  }

  /** A real HTTP server that records every request and answers from the scripted replies (the last one repeats). */
  static final class LocalServer {
    final HttpServer server;
    final List<Captured> calls = Collections.synchronizedList(new ArrayList<>());
    final String baseUrl;

    LocalServer(List<Reply> replies) throws IOException {
      server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      server.createContext("/", exchange -> {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.ISO_8859_1);
        calls.add(new Captured(exchange.getRequestMethod(), exchange.getRequestURI().toString(), exchange.getRequestHeaders(), body));
        Reply reply = replies.get(Math.min(calls.size() - 1, replies.size() - 1));
        byte[] bytes = JSON.writeValueAsBytes(reply.body);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        reply.headers.forEach((k, v) -> exchange.getResponseHeaders().add(k, v));
        exchange.sendResponseHeaders(reply.status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
          out.write(bytes);
        }
      });
      server.start();
      baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    void stop() {
      server.stop(0);
    }
  }

  private final List<LocalServer> servers = new ArrayList<>();

  @AfterEach
  void stopServers() {
    servers.forEach(LocalServer::stop);
    servers.clear();
  }

  private LocalServer server(Reply... replies) throws IOException {
    LocalServer s = new LocalServer(List.of(replies));
    servers.add(s);
    return s;
  }

  private static Imzala client(LocalServer s) {
    return new Imzala("imz_test", s.baseUrl, 5_000, 2, 1);
  }

  private static FileInput pdf() {
    return new FileInput("%PDF-1.7 test".getBytes(StandardCharsets.US_ASCII), "kira.pdf", "application/pdf");
  }

  private static Map<String, Object> doc() {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", DOC.toString());
    m.put("order", 1);
    m.put("title", "Kira sozlesmesi");
    m.put("doc_kind", "CONTRACT");
    return m;
  }

  private static Reply ok(Object data) {
    return new Reply(200, Map.of("success", true, "data", data));
  }

  private static Reply rateLimited(String retryAfter) {
    return new Reply(429, Map.of("success", false, "error", "Cok fazla istek", "code", "RATE_LIMIT_EXCEEDED"), Map.of("Retry-After", retryAfter));
  }

  private static Reply disabled() {
    return new Reply(409, Map.of("success", false, "error", "Kapali", "code", "ENVELOPE_MULTI_DOC_DISABLED"));
  }

  private static Reply conflict(String code, boolean withDocument) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", false);
    body.put("error", "x");
    body.put("code", code);
    if (withDocument) {
      body.put("data", Map.of("document", doc()));
    }
    return new Reply(409, body);
  }

  private static Map<String, Object> json(String body) throws IOException {
    return JSON.readValue(body, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
  }

  /** Value of one multipart form field in a captured request body. */
  private static String formField(String body, String name) {
    Matcher m = Pattern.compile("name=\"" + Pattern.quote(name) + "\"\\r\\n(?:[^\\r\\n]+\\r\\n)*\\r\\n([\\s\\S]*?)\\r\\n--").matcher(body);
    return m.find() ? m.group(1) : null;
  }

  // --- path slots and bodies on the wire ----------------------------------

  @Test
  void listSendsDemandIdInThePathAndViewAsAQueryParameter() throws IOException {
    LocalServer srv = server(ok(Map.of("documents", List.of(doc()))));
    ApiV1DemandsDemandIdDocumentsGet200ResponseData result = client(srv).demands().documents().list(DEMAND, "wizard");
    assertEquals(DOC, result.getDocuments().get(0).getId());
    assertEquals("Kira sozlesmesi", result.getDocuments().get(0).getTitle());
    assertEquals(1, srv.calls.size());
    assertEquals("GET", srv.calls.get(0).method);
    assertEquals("/api/v1/demands/" + DEMAND + "/documents?view=wizard", srv.calls.get(0).uri);
  }

  @Test
  void listWithoutViewSendsNoQueryString() throws IOException {
    LocalServer srv = server(ok(Map.of("documents", List.of())));
    client(srv).demands().documents().list(DEMAND);
    assertEquals("/api/v1/demands/" + DEMAND + "/documents", srv.calls.get(0).uri);
  }

  @Test
  void createPostsTheJsonBody() throws IOException {
    LocalServer srv = server(new Reply(201, Map.of("success", true, "data", Map.of("document", doc()))));
    ApiV1DemandsDemandIdDocumentsPost201ResponseData result = client(srv).demands().documents().create(DEMAND,
        new ApiV1DemandsDemandIdDocumentsPostRequest()
            .title("KVKK aydinlatma")
            .docKind(ApiV1DemandsDemandIdDocumentsPostRequest.DocKindEnum.KVKK_NOTICE)
            .isRequired(false)
            .signatureRequired(false));
    assertEquals(DOC, result.getDocument().getId());
    Captured call = srv.calls.get(0);
    assertEquals("POST", call.method);
    assertEquals("/api/v1/demands/" + DEMAND + "/documents", call.uri);
    Map<String, Object> sent = json(call.body);
    assertEquals("KVKK aydinlatma", sent.get("title"));
    assertEquals("KVKK_NOTICE", sent.get("doc_kind"));
    assertEquals(false, sent.get("is_required"));
    assertEquals(false, sent.get("signature_required"));
  }

  @Test
  void updateKeepsDemandIdAndDocIdInTheirOwnSlots() throws IOException {
    LocalServer srv = server(ok(Map.of("document", doc())));
    ApiV1DemandsDemandIdDocumentsPost201ResponseData result = client(srv).demands().documents()
        .update(DEMAND, DOC, new ApiV1DemandsDemandIdDocumentsDocIdPatchRequest().title("Yeni baslik"));
    assertEquals(DOC, result.getDocument().getId());
    Captured call = srv.calls.get(0);
    assertEquals("PATCH", call.method);
    assertEquals("/api/v1/demands/" + DEMAND + "/documents/" + DOC, call.uri);
    assertEquals("Yeni baslik", json(call.body).get("title"));
  }

  @Test
  void deleteKeepsDemandIdAndDocIdInTheirOwnSlots() throws IOException {
    LocalServer srv = server(ok(Map.of("id", DOC.toString(), "deleted", true)));
    ApiV1TemplatesIdDelete200ResponseData result = client(srv).demands().documents().delete(DEMAND, DOC);
    assertEquals(DOC, result.getId());
    assertEquals(Boolean.TRUE, result.getDeleted());
    Captured call = srv.calls.get(0);
    assertEquals("DELETE", call.method);
    assertEquals("/api/v1/demands/" + DEMAND + "/documents/" + DOC, call.uri);
  }

  @Test
  void reorderSendsDocumentIdsInTheGivenOrder() throws IOException {
    LocalServer srv = server(ok(Map.of("documents", List.of(doc()))));
    ApiV1DemandsDemandIdDocumentsGet200ResponseData result = client(srv).demands().documents().reorder(DEMAND, List.of(DOC_B, DOC));
    assertEquals(1, result.getDocuments().size());
    Captured call = srv.calls.get(0);
    assertEquals("PUT", call.method);
    assertEquals("/api/v1/demands/" + DEMAND + "/documents/order", call.uri);
    assertEquals(List.of(DOC_B.toString(), DOC.toString()), json(call.body).get("document_ids"));
  }

  @Test
  void setAssignmentsKeepsDemandIdAndDocIdApartAndSendsPartyIds() throws IOException {
    LocalServer srv = server(ok(Map.of("document", doc())));
    client(srv).demands().documents().setAssignments(DEMAND, DOC, List.of(PARTY));
    Captured call = srv.calls.get(0);
    assertEquals("PUT", call.method);
    assertEquals("/api/v1/demands/" + DEMAND + "/documents/" + DOC + "/assignments", call.uri);
    assertEquals(List.of(PARTY.toString()), json(call.body).get("party_ids"));
  }

  @Test
  void dispatchSendsNoSendInvitationsFieldByDefault() throws IOException {
    LocalServer srv = server(ok(Map.of("demand_id", DEMAND.toString(), "status", "PENDING", "dispatched", true)));
    ApiV1DemandsDemandIdDispatchPost200ResponseData result = client(srv).demands().dispatch(DEMAND);
    assertEquals(DEMAND, result.getDemandId());
    assertEquals(Boolean.TRUE, result.getDispatched());
    Captured call = srv.calls.get(0);
    assertEquals("POST", call.method);
    assertEquals("/api/v1/demands/" + DEMAND + "/dispatch", call.uri);
    Map<String, Object> sent = call.body.isEmpty() ? Map.of() : json(call.body);
    assertFalse(sent.containsKey("send_invitations"));
  }

  @Test
  void dispatchForwardsABooleanOrAStringSendInvitationsValueUnchanged() throws IOException {
    LocalServer srv = server(ok(Map.of("dispatched", true)));
    client(srv).demands().dispatch(DEMAND, false);
    client(srv).demands().dispatch(DEMAND, "email");
    client(srv).demands().dispatch(DEMAND, true);
    assertEquals(false, json(srv.calls.get(0).body).get("send_invitations"));
    assertEquals("email", json(srv.calls.get(1).body).get("send_invitations"));
    assertEquals(true, json(srv.calls.get(2).body).get("send_invitations"));
  }

  // --- upload ---------------------------------------------------------------

  @Test
  void uploadSendsTheIdempotencyKeyAsTheIdempotencyKeyBodyFieldNotAsAHeader() throws IOException {
    LocalServer srv = server(ok(Map.of("document", doc())));
    ApiV1DemandsDemandIdDocumentsPost201ResponseData result = client(srv).demands().documents().upload(DEMAND,
        new UploadEnvelopeDocumentParams(pdf(), "Kira sozlesmesi", "siparis-42")
            .docKind("CONTRACT")
            .isRequired(false));
    assertEquals(DOC, result.getDocument().getId());
    Captured call = srv.calls.get(0);
    assertEquals("POST", call.method);
    assertEquals("/api/v1/demands/" + DEMAND + "/documents/upload", call.uri);
    assertNull(call.headers.getFirst("Idempotency-Key"));
    assertTrue(call.headers.getFirst("Content-Type").startsWith("multipart/form-data"));
    assertEquals("siparis-42", formField(call.body, "idempotency_key"));
    assertEquals("Kira sozlesmesi", formField(call.body, "title"));
    assertEquals("CONTRACT", formField(call.body, "doc_kind"));
    assertEquals("false", formField(call.body, "is_required"));
    assertTrue(call.body.contains("filename=\"kira.pdf\""));
    assertTrue(call.body.contains("%PDF-1.7 test"));
  }

  @Test
  void uploadOmitsDocKindAndIsRequiredWhenNotGiven() throws IOException {
    LocalServer srv = server(ok(Map.of("document", doc())));
    client(srv).demands().documents().upload(DEMAND, new UploadEnvelopeDocumentParams(pdf(), "T", "k-1"));
    String body = srv.calls.get(0).body;
    assertNull(formField(body, "doc_kind"));
    assertNull(formField(body, "is_required"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "sipariş-1", "a\r\nX-Evil: 1"})
  void uploadRejectsABadIdempotencyKeyLocallyWithoutSendingAnything(String key) throws IOException {
    LocalServer srv = server(ok(Map.of("document", doc())));
    ImzalaValidationException err = assertThrows(ImzalaValidationException.class,
        () -> client(srv).demands().documents().upload(DEMAND, new UploadEnvelopeDocumentParams(pdf(), "T", key)));
    assertNull(err.getStatusCode());
    assertEquals(0, srv.calls.size());
  }

  @Test
  void uploadRejectsANullIdempotencyKeyLocally() throws IOException {
    LocalServer srv = server(ok(Map.of("document", doc())));
    assertThrows(ImzalaValidationException.class,
        () -> client(srv).demands().documents().upload(DEMAND, new UploadEnvelopeDocumentParams(pdf(), "T", null)));
    assertEquals(0, srv.calls.size());
  }

  @Test
  void uploadRetriesOnceAfterA429AndSendsTheFileAgain() throws IOException {
    LocalServer srv = server(rateLimited("0"), ok(Map.of("document", doc())));
    ApiV1DemandsDemandIdDocumentsPost201ResponseData result = client(srv).demands().documents()
        .upload(DEMAND, new UploadEnvelopeDocumentParams(pdf(), "T", "k-1"));
    assertEquals(DOC, result.getDocument().getId());
    assertEquals(2, srv.calls.size());
    assertTrue(srv.calls.get(1).body.contains("%PDF-1.7 test"));
    assertEquals("k-1", formField(srv.calls.get(1).body, "idempotency_key"));
  }

  @Test
  void uploadDoesNotRetryWhenRetryAfterExceedsTheCap() throws IOException {
    LocalServer srv = server(rateLimited("61"));
    ImzalaRateLimitException err = assertThrows(ImzalaRateLimitException.class,
        () -> client(srv).demands().documents().upload(DEMAND, new UploadEnvelopeDocumentParams(pdf(), "T", "k-1")));
    assertEquals(429, err.getStatusCode());
    assertEquals(1, srv.calls.size());
  }

  @Test
  void uploadReturnsTheEarlierDocumentOnA409IdempotentReplayInsteadOfThrowing() throws IOException {
    LocalServer srv = server(conflict("IDEMPOTENT_REPLAY", true));
    ApiV1DemandsDemandIdDocumentsPost201ResponseData result = client(srv).demands().documents()
        .upload(DEMAND, new UploadEnvelopeDocumentParams(pdf(), "T", "k-1"));
    assertNotNull(result.getDocument());
    assertEquals(DOC, result.getDocument().getId());
    assertEquals("Kira sozlesmesi", result.getDocument().getTitle());
    assertEquals(1, srv.calls.size());
  }

  @Test
  void uploadTreatsAReplayThatFollowsA429RetryAsSuccessToo() throws IOException {
    LocalServer srv = server(rateLimited("0"), conflict("IDEMPOTENT_REPLAY", true));
    ApiV1DemandsDemandIdDocumentsPost201ResponseData result = client(srv).demands().documents()
        .upload(DEMAND, new UploadEnvelopeDocumentParams(pdf(), "T", "k-1"));
    assertEquals(DOC, result.getDocument().getId());
    assertEquals(2, srv.calls.size());
  }

  @Test
  void uploadThrowsA409ReplayWithoutADocument() throws IOException {
    LocalServer srv = server(conflict("IDEMPOTENT_REPLAY", false));
    ImzalaException err = assertThrows(ImzalaException.class,
        () -> client(srv).demands().documents().upload(DEMAND, new UploadEnvelopeDocumentParams(pdf(), "T", "k-1")));
    assertEquals("IDEMPOTENT_REPLAY", err.getCode());
  }

  @Test
  void uploadThrowsAnyOther409CodeForExampleSigningAlreadyStarted() throws IOException {
    LocalServer srv = server(conflict("SIGNING_ALREADY_STARTED", true));
    ImzalaException err = assertThrows(ImzalaException.class,
        () -> client(srv).demands().documents().upload(DEMAND, new UploadEnvelopeDocumentParams(pdf(), "T", "k-1")));
    assertEquals("SIGNING_ALREADY_STARTED", err.getCode());
    assertEquals(409, err.getStatusCode());
  }

  @Test
  void uploadThrowsEnvelopeMultiDocDisabled() throws IOException {
    LocalServer srv = server(disabled());
    ImzalaException err = assertThrows(ImzalaException.class,
        () -> client(srv).demands().documents().upload(DEMAND, new UploadEnvelopeDocumentParams(pdf(), "T", "k-1")));
    assertEquals("ENVELOPE_MULTI_DOC_DISABLED", err.getCode());
  }

  // --- feature switched off -------------------------------------------------

  @Test
  void listThrowsEnvelopeMultiDocDisabledAndDoesNotReturnAnEmptyList() throws IOException {
    LocalServer srv = server(disabled());
    ImzalaException err = assertThrows(ImzalaException.class, () -> client(srv).demands().documents().list(DEMAND));
    assertEquals("ENVELOPE_MULTI_DOC_DISABLED", err.getCode());
    assertEquals(409, err.getStatusCode());
    assertEquals(1, srv.calls.size());
  }

  private static final Map<String, Function<Imzala, Object>> UNKEYED_WRITES = new LinkedHashMap<>();

  static {
    UNKEYED_WRITES.put("create", c -> c.demands().documents().create(DEMAND, new ApiV1DemandsDemandIdDocumentsPostRequest().title("T")));
    UNKEYED_WRITES.put("update", c -> c.demands().documents().update(DEMAND, DOC, new ApiV1DemandsDemandIdDocumentsDocIdPatchRequest().title("T")));
    UNKEYED_WRITES.put("delete", c -> c.demands().documents().delete(DEMAND, DOC));
    UNKEYED_WRITES.put("reorder", c -> c.demands().documents().reorder(DEMAND, List.of(DOC)));
    UNKEYED_WRITES.put("setAssignments", c -> c.demands().documents().setAssignments(DEMAND, DOC, List.of(PARTY)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"create", "update", "delete", "reorder", "setAssignments"})
  void documentWritesThrowEnvelopeMultiDocDisabled(String name) throws IOException {
    LocalServer srv = server(disabled());
    ImzalaException err = assertThrows(ImzalaException.class, () -> UNKEYED_WRITES.get(name).apply(client(srv)));
    assertEquals("ENVELOPE_MULTI_DOC_DISABLED", err.getCode());
  }

  // --- writes without an idempotency key are never retried -----------------

  @ParameterizedTest
  @ValueSource(strings = {"create", "update", "delete", "reorder", "setAssignments", "dispatch"})
  void a429IsThrownAfterExactlyOneRequest(String name) throws IOException {
    LocalServer srv = server(rateLimited("0"), ok(Map.of()));
    Function<Imzala, Object> call = name.equals("dispatch") ? c -> c.demands().dispatch(DEMAND) : UNKEYED_WRITES.get(name);
    assertThrows(ImzalaRateLimitException.class, () -> call.apply(client(srv)));
    assertEquals(1, srv.calls.size(), "transport or SDK retried an unkeyed write");
  }

  @Test
  void listIsRetriedAfterA429() throws IOException {
    LocalServer srv = server(rateLimited("0"), ok(Map.of("documents", List.of())));
    ApiV1DemandsDemandIdDocumentsGet200ResponseData result = client(srv).demands().documents().list(DEMAND);
    assertEquals(0, result.getDocuments().size());
    assertEquals(2, srv.calls.size());
  }

  // --- dispatch errors --------------------------------------------------------

  @ParameterizedTest
  @ValueSource(strings = {"DISPATCH_NO_PARTIES", "DISPATCH_TOO_MANY", "QES_NOT_SUPPORTED_MULTI_DOCUMENT"})
  void dispatchThrowsTheServerCode(String code) throws IOException {
    LocalServer srv = server(conflict(code, false));
    ImzalaException err = assertThrows(ImzalaException.class, () -> client(srv).demands().dispatch(DEMAND));
    assertEquals(code, err.getCode());
    assertEquals(409, err.getStatusCode());
    assertEquals(1, srv.calls.size());
  }
}
