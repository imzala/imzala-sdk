package org.imzala;

import org.imzala.client.generated.model.ApiV1DemandsPost400Response;
import org.imzala.client.generated.model.CodedError;
import org.imzala.client.generated.model.DocumentSelectionError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class GeneratedErrorCompatibilityTest {
  @Test
  void retainsCodedErrorBranchConstructorAndGetters() {
    CodedError coded = new CodedError().success(false).error("Synthetic validation").code("FUTURE_COLLECTION_ERROR");
    ApiV1DemandsPost400Response wrapper = new ApiV1DemandsPost400Response(coded);
    assertSame(coded, wrapper.getActualInstance());
    assertSame(coded, wrapper.getCodedError());
    assertEquals(CodedError.class, wrapper.getSchemas().get("CodedError"));
  }

  @Test
  void retainsSelectionBranchConstructorAndGetters() {
    DocumentSelectionError selection = new DocumentSelectionError().success(false).error("Synthetic selection");
    ApiV1DemandsPost400Response wrapper = new ApiV1DemandsPost400Response(selection);
    assertSame(selection, wrapper.getActualInstance());
    assertSame(selection, wrapper.getDocumentSelectionError());
    assertEquals(DocumentSelectionError.class, wrapper.getSchemas().get("DocumentSelectionError"));
  }
}
