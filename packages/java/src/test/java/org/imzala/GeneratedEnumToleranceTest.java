package org.imzala;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.imzala.client.generated.JSON;
import org.imzala.client.generated.model.PageItem;
import org.imzala.client.generated.model.TemplateVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

// item_type grows with the API (contract_number in 1.14.5, stamp in 1.14.6).
// A value the client was generated without must not break deserialization.
class GeneratedEnumToleranceTest {
  private final ObjectMapper mapper = new JSON().getMapper();

  @ParameterizedTest
  @ValueSource(strings = {"contract_number", "stamp", "future_type"})
  void pageItemKeepsItemType(String value) throws Exception {
    String body = "{\"page_id\":1,\"item_type\":\"" + value
        + "\",\"position_x\":0.1,\"position_y\":0.2,\"width\":0.3,\"height\":0.1}";
    assertEquals(value, mapper.readValue(body, PageItem.class).getItemType());
  }

  @ParameterizedTest
  @ValueSource(strings = {"stamp", "future_type"})
  void templateVariableKeepsItemType(String value) throws Exception {
    String body = "{\"slug\":\"a\",\"item_type\":\"" + value + "\"}";
    assertEquals(value, mapper.readValue(body, TemplateVariable.class).getItemType());
  }
}
