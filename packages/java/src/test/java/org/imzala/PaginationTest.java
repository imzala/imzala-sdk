package org.imzala;

import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.api.TemplatesApi;
import org.imzala.client.generated.model.ApiV1TemplatesGet200Response;
import org.imzala.client.generated.model.ApiV1TemplatesGet200ResponseData;
import org.imzala.client.generated.model.TemplateSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link TemplatesResource#listAll} pagination-iterator tests — mirrors
 * {@code pagination.test.ts} (Node, 4 tests), {@code test_pagination.py}
 * (Python), {@code PaginationTests.cs} (.NET), and {@code
 * PaginationTest.php} (PHP). Mocks {@code TemplatesApi.apiV1TemplatesGet}
 * directly (same seam as {@link ClientTest}/{@link RetryTest}).
 */
@ExtendWith(MockitoExtension.class)
class PaginationTest {

  @Mock
  private TemplatesApi templatesApi;

  private static TemplateSummary summary() {
    return new TemplateSummary().id(UUID.randomUUID());
  }

  private static int callCount(Object mock) {
    return mockingDetails(mock).getInvocations().size();
  }

  @Test
  void walks_two_full_pages_plus_a_short_page_and_stops_with_no_extra_call() throws ApiException {
    List<TemplateSummary> page1 = List.of(summary(), summary());
    List<TemplateSummary> page2 = List.of(summary(), summary());
    List<TemplateSummary> page3 = List.of(summary()); // short: 1 < limit(2)

    when(templatesApi.apiV1TemplatesGet(1, 2)).thenReturn(
        new ApiV1TemplatesGet200Response().success(true)
            .data(new ApiV1TemplatesGet200ResponseData().templates(page1).page(1).limit(2)));
    when(templatesApi.apiV1TemplatesGet(2, 2)).thenReturn(
        new ApiV1TemplatesGet200Response().success(true)
            .data(new ApiV1TemplatesGet200ResponseData().templates(page2).page(2).limit(2)));
    when(templatesApi.apiV1TemplatesGet(3, 2)).thenReturn(
        new ApiV1TemplatesGet200Response().success(true)
            .data(new ApiV1TemplatesGet200ResponseData().templates(page3).page(3).limit(2)));

    TemplatesResource resource = new TemplatesResource(templatesApi, new RetryConfig(0, 1));

    List<TemplateSummary> collected = new ArrayList<>();
    for (TemplateSummary t : resource.listAll(1, 2)) {
      collected.add(t);
    }

    assertEquals(5, collected.size());
    assertEquals(3, callCount(templatesApi)); // no 4th call
    verify(templatesApi).apiV1TemplatesGet(1, 2);
    verify(templatesApi).apiV1TemplatesGet(2, 2);
    verify(templatesApi).apiV1TemplatesGet(3, 2);
  }

  @Test
  void stops_when_total_is_reached_exactly_on_a_full_page_without_needing_a_short_page() throws ApiException {
    List<TemplateSummary> page1 = List.of(summary(), summary());
    List<TemplateSummary> page2 = List.of(summary(), summary());

    when(templatesApi.apiV1TemplatesGet(1, 2)).thenReturn(
        new ApiV1TemplatesGet200Response().success(true)
            .data(new ApiV1TemplatesGet200ResponseData().templates(page1).total(4).page(1).limit(2)));
    when(templatesApi.apiV1TemplatesGet(2, 2)).thenReturn(
        new ApiV1TemplatesGet200Response().success(true)
            .data(new ApiV1TemplatesGet200ResponseData().templates(page2).total(4).page(2).limit(2)));

    TemplatesResource resource = new TemplatesResource(templatesApi, new RetryConfig(0, 1));

    List<TemplateSummary> collected = new ArrayList<>();
    for (TemplateSummary t : resource.listAll(1, 2)) {
      collected.add(t);
    }

    assertEquals(4, collected.size());
    // Only 2 calls — no 3rd page call was attempted even though page 2 was
    // a full (non-short) page. Proves stopping on `total` doesn't require a
    // trailing short page, and that the loop can't spin forever against a
    // server that only ever returns full pages.
    assertEquals(2, callCount(templatesApi));
  }

  @Test
  void empty_first_page_yields_nothing_with_a_single_call_and_no_infinite_loop() throws ApiException {
    when(templatesApi.apiV1TemplatesGet(1, 10)).thenReturn(
        new ApiV1TemplatesGet200Response().success(true)
            .data(new ApiV1TemplatesGet200ResponseData().templates(List.of()).page(1).limit(10)));

    TemplatesResource resource = new TemplatesResource(templatesApi, new RetryConfig(0, 1));

    List<TemplateSummary> collected = new ArrayList<>();
    for (TemplateSummary t : resource.listAll(1, 10)) {
      collected.add(t);
    }

    assertTrue(collected.isEmpty());
    assertEquals(1, callCount(templatesApi));
  }

  @Test
  void list_single_page_call_is_unaffected_by_the_new_listAll_iterator() throws ApiException {
    List<TemplateSummary> page1 = List.of(summary(), summary());
    when(templatesApi.apiV1TemplatesGet(1, 2)).thenReturn(
        new ApiV1TemplatesGet200Response().success(true)
            .data(new ApiV1TemplatesGet200ResponseData().templates(page1).total(5).page(1).limit(2)));

    TemplatesResource resource = new TemplatesResource(templatesApi, new RetryConfig(0, 1));
    ApiV1TemplatesGet200ResponseData result = resource.list(1, 2);

    assertEquals(2, result.getTemplates().size());
    assertEquals(1, callCount(templatesApi)); // list() stays single-page — no auto-pagination
  }
}
