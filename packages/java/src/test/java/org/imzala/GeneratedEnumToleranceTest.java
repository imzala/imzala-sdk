package org.imzala;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.imzala.client.generated.JSON;
import org.imzala.client.generated.model.DemandDocumentStatus;
import org.imzala.client.generated.model.DemandStatus;
import org.imzala.client.generated.model.PageItem;
import org.imzala.client.generated.model.StandardError;
import org.imzala.client.generated.model.TemplateVariable;
import org.imzala.client.generated.model.WebhookEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Response enums grow with the API (item_type gained contract_number in
// 1.14.5 and stamp in 1.14.6; status, decision, doc_kind, error codes and
// webhook types grow the same way). Since SDK 1.2.0 every enum the server can
// return is an open String: a value the client was generated without must not
// break deserialization.
class GeneratedEnumToleranceTest {
  private final ObjectMapper mapper = new JSON().getMapper();

  private static final String DEMAND = "{\"id\":\"00000000-0000-4000-8000-000000000001\",\"title\":\"Ornek\","
      + "\"status\":\"future_value\",\"parties\":[{\"party_id\":\"00000000-0000-4000-8000-000000000002\","
      + "\"locale\":\"future_value\",\"signed_locale\":\"future_value\"}],"
      + "\"stamp_items\":[{\"item_id\":1,\"source\":\"future_value\"}],"
      + "\"documents\":[{\"id\":\"00000000-0000-4000-8000-000000000003\",\"doc_kind\":\"future_value\","
      + "\"progress\":{\"status\":\"future_value\"},\"sealing\":{\"status\":\"future_value\"},"
      + "\"decisions\":[{\"party_id\":\"00000000-0000-4000-8000-000000000002\",\"decision\":\"future_value\"}]}]}";

  @ParameterizedTest
  @ValueSource(strings = {"contract_number", "stamp", "future_value"})
  void pageItemKeepsItemType(String value) throws Exception {
    String body = "{\"page_id\":1,\"item_type\":\"" + value
        + "\",\"position_x\":0.1,\"position_y\":0.2,\"width\":0.3,\"height\":0.1}";
    assertEquals(value, mapper.readValue(body, PageItem.class).getItemType());
  }

  @ParameterizedTest
  @ValueSource(strings = {"stamp", "future_value"})
  void templateVariableKeepsItemType(String value) throws Exception {
    String body = "{\"slug\":\"a\",\"item_type\":\"" + value + "\"}";
    assertEquals(value, mapper.readValue(body, TemplateVariable.class).getItemType());
  }

  @Test
  void demandStatusKeepsUnknownValues() throws Exception {
    DemandStatus d = mapper.readValue(DEMAND, DemandStatus.class);
    assertEquals("future_value", d.getStatus());
    assertEquals("future_value", d.getParties().get(0).getLocale());
    assertEquals("future_value", d.getParties().get(0).getSignedLocale());
    assertEquals("future_value", d.getStampItems().get(0).getSource());
    DemandDocumentStatus doc = d.getDocuments().get(0);
    assertEquals("future_value", doc.getDocKind());
    assertEquals("future_value", doc.getProgress().getStatus());
    assertEquals("future_value", doc.getSealing().getStatus());
    assertEquals("future_value", doc.getDecisions().get(0).getDecision());
  }

  @Test
  void webhookTypeAndErrorCodeKeepUnknownValues() throws Exception {
    WebhookEnvelope event = mapper.readValue("{\"id\":\"evt_0123456789abcdef0123456789abcdef\","
        + "\"type\":\"future_value\",\"created_at\":\"2026-11-01T00:00:00Z\",\"data\":{}}", WebhookEnvelope.class);
    assertEquals("future_value", event.getType());
    StandardError error = mapper.readValue(
        "{\"success\":false,\"error\":{\"code\":\"future_value\",\"message\":\"Ornek\"}}", StandardError.class);
    assertEquals("future_value", error.getError().getCode());
  }
}
