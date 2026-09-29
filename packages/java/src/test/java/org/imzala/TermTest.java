package org.imzala;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.imzala.EnvelopeTest.Captured;
import org.imzala.EnvelopeTest.LocalServer;
import org.imzala.EnvelopeTest.Reply;
import org.imzala.client.generated.model.ApiV1DemandsIdArchivePost200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsIdTermPatch200ResponseData;
import org.imzala.client.generated.model.ContractTerm;
import org.imzala.client.generated.model.ContractTermInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract term tracking and archive helpers, checked on the wire against a
 * local HTTP server: path slots, the partial-update body (an explicit null is
 * sent, an unset field is not), the archive filter on list, and that none of
 * these writes is retried. Mirrors packages/node/src/__tests__/term.test.ts.
 */
class TermTest {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final UUID DEMAND = UUID.fromString("11111111-1111-4111-8111-111111111111");

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

  private static Reply ok(Object data) {
    return new Reply(200, Map.of("success", true, "data", data));
  }

  private static Reply error(int status, String code) {
    return new Reply(status, Map.of("success", false, "error", "x", "code", code));
  }

  private static Map<String, Object> json(String body) throws IOException {
    return JSON.readValue(body, new TypeReference<Map<String, Object>>() {});
  }

  private static Map<String, Object> term() {
    Map<String, Object> t = new LinkedHashMap<>();
    t.put("start_mode", "ON_COMPLETION");
    t.put("duration_months", 12);
    t.put("renewal_type", "AUTO_RENEW");
    t.put("renewal_period_months", 12);
    t.put("notice_days", 30);
    t.put("reminder_offsets", List.of(30, 7));
    t.put("notify_counterparty", false);
    t.put("state", "UNTRACKED");
    return t;
  }

  @Test
  void updateTermPatchesOnlyTheSetFieldsAndSendsExplicitNull() throws IOException {
    LocalServer srv = server(ok(Map.of("term", term())));
    ApiV1DemandsIdTermPatch200ResponseData result = client(srv).demands().updateTerm(DEMAND,
        new ContractTermInput()
            .termStartMode(ContractTermInput.TermStartModeEnum.ON_COMPLETION)
            .termDurationMonths(12)
            .renewalType(ContractTermInput.RenewalTypeEnum.AUTO_RENEW)
            .noticeDays(null));
    assertEquals(ContractTerm.RenewalTypeEnum.AUTO_RENEW, result.getTerm().getRenewalType());
    Captured call = srv.calls.get(0);
    assertEquals("PATCH", call.method);
    assertEquals("/api/v1/demands/" + DEMAND + "/term", call.uri);
    Map<String, Object> body = json(call.body);
    assertEquals("ON_COMPLETION", body.get("term_start_mode"));
    assertEquals(12, body.get("term_duration_months"));
    assertEquals("AUTO_RENEW", body.get("renewal_type"));
    // An explicit null clears the key on the server; unset fields stay out.
    assertTrue(body.containsKey("notice_days"));
    assertNull(body.get("notice_days"));
    assertEquals(4, body.size(), "unset fields must not be sent: " + body);
  }

  @Test
  void updateTermSendsDatesAsIsoAndNotifyCounterpartyOnlyWhenSet() throws IOException {
    LocalServer srv = server(ok(Map.of("term", term())));
    client(srv).demands().updateTerm(DEMAND, new ContractTermInput().termFixedEndDate(LocalDate.of(2027, 1, 31)));
    assertEquals(Map.of("term_fixed_end_date", "2027-01-31"), json(srv.calls.get(0).body));
  }

  @Test
  void updateTermThrowsTermInvalidWithTheRejectedField() throws IOException {
    LocalServer srv = server(new Reply(400, Map.of("success", false, "error", "x", "code", "TERM_INVALID", "field", "term_fixed_end_date")));
    ImzalaException err = assertThrows(ImzalaException.class, () -> client(srv).demands().updateTerm(DEMAND,
        new ContractTermInput().termFixedEndDate(LocalDate.of(2027, 1, 31)).termDurationMonths(12)));
    assertEquals("TERM_INVALID", err.getCode());
    assertEquals("term_fixed_end_date", json(err.getBody()).get("field"));
    assertEquals(1, srv.calls.size());
  }

  @Test
  void archivePostsToTheArchivePath() throws IOException {
    LocalServer srv = server(ok(Map.of("archived_at", "2026-09-28T09:00:00.000Z")));
    ApiV1DemandsIdArchivePost200ResponseData result = client(srv).demands().archive(DEMAND);
    assertNotNull(result.getArchivedAt());
    assertEquals("POST", srv.calls.get(0).method);
    assertEquals("/api/v1/demands/" + DEMAND + "/archive", srv.calls.get(0).uri);
  }

  @Test
  void unarchivePostsToTheUnarchivePath() throws IOException {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("archived_at", null);
    LocalServer srv = server(new Reply(200, Map.of("success", true, "data", data)));
    assertNotNull(client(srv).demands().unarchive(DEMAND));
    assertEquals("POST", srv.calls.get(0).method);
    assertEquals("/api/v1/demands/" + DEMAND + "/unarchive", srv.calls.get(0).uri);
  }

  @ParameterizedTest
  @ValueSource(strings = {"DEMAND_NOT_ARCHIVABLE", "DEMAND_REJECTED_CANCEL_FIRST"})
  void archiveThrows(String code) throws IOException {
    LocalServer srv = server(error(409, code));
    ImzalaException err = assertThrows(ImzalaException.class, () -> client(srv).demands().archive(DEMAND));
    assertEquals(code, err.getCode());
    assertEquals(1, srv.calls.size());
  }

  @Test
  void deleteOfAnArchivedDemandThrowsDemandArchived() throws IOException {
    LocalServer srv = server(error(409, "DEMAND_ARCHIVED"));
    ImzalaException err = assertThrows(ImzalaException.class, () -> client(srv).demands().delete(DEMAND));
    assertEquals("DEMAND_ARCHIVED", err.getCode());
  }

  @Test
  void listSendsTheArchiveFilterOnlyWhenSet() throws IOException {
    Reply empty = ok(Map.of("demands", List.of(), "pagination", Map.of("page", 1, "limit", 20, "total", 0)));
    LocalServer srv = server(empty, empty);
    client(srv).demands().list(new ListDemandsParams().archived("exclude"));
    client(srv).demands().list();
    String first = URI.create(srv.calls.get(0).uri).getRawQuery();
    String second = URI.create(srv.calls.get(1).uri).getRawQuery();
    assertTrue(first != null && first.contains("archived=exclude"), first);
    assertTrue(second == null || !second.contains("archived="), second);
  }

  @ParameterizedTest
  @ValueSource(strings = {"updateTerm", "archive", "unarchive"})
  void rateLimitedWriteIsNotRetried(String name) throws IOException {
    LocalServer srv = server(
        new Reply(429, Map.of("success", false, "error", "Cok fazla istek", "code", "RATE_LIMIT_EXCEEDED"), Map.of("Retry-After", "0")),
        ok(Map.of()));
    Map<String, Consumer<Imzala>> calls = Map.of(
        "updateTerm", c -> c.demands().updateTerm(DEMAND, new ContractTermInput().noticeDays(30)),
        "archive", c -> c.demands().archive(DEMAND),
        "unarchive", c -> c.demands().unarchive(DEMAND));
    assertThrows(ImzalaRateLimitException.class, () -> calls.get(name).accept(client(srv)));
    assertEquals(1, srv.calls.size());
  }
}
