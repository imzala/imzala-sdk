package org.imzala;

import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.api.ContactsApi;
import org.imzala.client.generated.api.DemandsApi;
import org.imzala.client.generated.api.RemindersApi;
import org.imzala.client.generated.api.ReportsApi;
import org.imzala.client.generated.api.TemplatesApi;
import org.imzala.client.generated.api.TimestampsApi;
import org.imzala.client.generated.model.ApiV1ContactsGet200Response;
import org.imzala.client.generated.model.ApiV1ContactsGet200ResponseData;
import org.imzala.client.generated.model.ApiV1ContactsPost201Response;
import org.imzala.client.generated.model.ApiV1ContactsPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsBulkPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsBulkPost200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsBulkPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsUploadPost201Response;
import org.imzala.client.generated.model.ApiV1FieldTemplatesGet200Response;
import org.imzala.client.generated.model.ApiV1FieldTemplatesGet200ResponseData;
import org.imzala.client.generated.model.ApiV1FieldTemplatesIdGet200Response;
import org.imzala.client.generated.model.ApiV1FieldTemplatesIdPreviewLayoutPost200Response;
import org.imzala.client.generated.model.ApiV1ReportsGet200Response;
import org.imzala.client.generated.model.ApiV1ReportsGet200ResponseData;
import org.imzala.client.generated.model.ApiV1TimestampsGet200Response;
import org.imzala.client.generated.model.ApiV1TimestampsGet200ResponseData;
import org.imzala.client.generated.model.ApiV1TimestampsIdGet200Response;
import org.imzala.client.generated.model.ContactSummary;
import org.imzala.client.generated.model.CreatedDemandUpload;
import org.imzala.client.generated.model.FieldLayoutPreview;
import org.imzala.client.generated.model.FieldTemplateDetail;
import org.imzala.client.generated.model.TimestampListItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * New resources and the new upload fields. The generated signatures insert
 * new parameters between existing ones and many slots share a type (String,
 * UUID), so the compiler cannot catch a shifted argument: every call here is
 * verified slot by slot with distinct values.
 */
@ExtendWith(MockitoExtension.class)
class ResourcesTest {

  private static final RetryConfig NO_RETRY = new RetryConfig(0, 0);

  @Mock
  private TemplatesApi templatesApi;
  @Mock
  private DemandsApi demandsApi;
  @Mock
  private RemindersApi remindersApi;
  @Mock
  private ContactsApi contactsApi;
  @Mock
  private ReportsApi reportsApi;
  @Mock
  private TimestampsApi timestampsApi;

  private static int calls(Object mock) {
    return mockingDetails(mock).getInvocations().size();
  }

  // --- field templates ---

  @Test
  void field_templates_list_forwards_paging() throws ApiException {
    ApiV1FieldTemplatesGet200ResponseData data = new ApiV1FieldTemplatesGet200ResponseData().total(0);
    when(templatesApi.apiV1FieldTemplatesGet(3, 50)).thenReturn(new ApiV1FieldTemplatesGet200Response().success(true).data(data));

    FieldTemplatesResource resource = new FieldTemplatesResource(templatesApi, demandsApi, NO_RETRY);
    assertSame(data, resource.list(3, 50));
    verify(templatesApi).apiV1FieldTemplatesGet(3, 50);
  }

  @Test
  void field_templates_list_defaults_to_server_paging() throws ApiException {
    when(templatesApi.apiV1FieldTemplatesGet(null, null))
        .thenReturn(new ApiV1FieldTemplatesGet200Response().success(true).data(new ApiV1FieldTemplatesGet200ResponseData()));

    new FieldTemplatesResource(templatesApi, demandsApi, NO_RETRY).list();
    verify(templatesApi).apiV1FieldTemplatesGet(isNull(), isNull());
  }

  @Test
  void field_templates_get_is_retried_as_a_get() throws ApiException {
    UUID id = UUID.randomUUID();
    when(templatesApi.apiV1FieldTemplatesIdGet(id))
        .thenThrow(IdempotentWriteTest.status(503, "{\"success\":false}"))
        .thenReturn(new ApiV1FieldTemplatesIdGet200Response().success(true).data(new FieldTemplateDetail().id(id)));

    IdempotentWriteTest.RecordingSleeper sleeper = new IdempotentWriteTest.RecordingSleeper();
    FieldTemplateDetail detail = new FieldTemplatesResource(templatesApi, demandsApi, new RetryConfig(2, 1, sleeper)).get(id);

    assertEquals(id, detail.getId());
    assertEquals(2, calls(templatesApi));
    assertEquals(1, sleeper.waits.size());
  }

  @Test
  void preview_layout_sends_one_pdf_and_on_anchor_miss_in_their_slots() throws ApiException {
    UUID id = UUID.randomUUID();
    List<File> sent = new ArrayList<>();
    when(demandsApi.apiV1FieldTemplatesIdPreviewLayoutPost(eq(id), anyList(), eq("drop"))).thenAnswer(inv -> {
      List<File> files = inv.getArgument(1);
      assertTrue(files.get(0).exists());
      assertArrayEquals("%PDF-1.7".getBytes(), Files.readAllBytes(files.get(0).toPath()));
      sent.addAll(files);
      return new ApiV1FieldTemplatesIdPreviewLayoutPost200Response().success(true).data(new FieldLayoutPreview().resolvable(true));
    });

    FieldLayoutPreview preview = new FieldTemplatesResource(templatesApi, demandsApi, NO_RETRY)
        .previewLayout(id, List.of(new FileInput("%PDF-1.7".getBytes(), "kira.pdf")), "drop");

    assertTrue(preview.getResolvable());
    assertEquals(1, sent.size());
    assertEquals("kira.pdf", sent.get(0).getName());
    assertFalse(sent.get(0).exists(), "temp file must be removed");
    verify(demandsApi).apiV1FieldTemplatesIdPreviewLayoutPost(eq(id), anyList(), eq("drop"));
  }

  @Test
  void preview_layout_without_on_anchor_miss_sends_null() throws ApiException {
    UUID id = UUID.randomUUID();
    when(demandsApi.apiV1FieldTemplatesIdPreviewLayoutPost(eq(id), anyList(), isNull()))
        .thenReturn(new ApiV1FieldTemplatesIdPreviewLayoutPost200Response().success(true).data(new FieldLayoutPreview()));

    new FieldTemplatesResource(templatesApi, demandsApi, NO_RETRY)
        .previewLayout(id, List.of(new FileInput("%PDF".getBytes(), "a.pdf")));
    verify(demandsApi).apiV1FieldTemplatesIdPreviewLayoutPost(eq(id), anyList(), isNull());
  }

  @Test
  void preview_layout_is_not_retried_and_cleans_up_on_failure() throws ApiException {
    UUID id = UUID.randomUUID();
    List<File> sent = new ArrayList<>();
    when(demandsApi.apiV1FieldTemplatesIdPreviewLayoutPost(eq(id), anyList(), any())).thenAnswer(inv -> {
      sent.addAll(inv.getArgument(1));
      throw IdempotentWriteTest.tooMany("1");
    });

    IdempotentWriteTest.RecordingSleeper sleeper = new IdempotentWriteTest.RecordingSleeper();
    FieldTemplatesResource resource = new FieldTemplatesResource(templatesApi, demandsApi, new RetryConfig(2, 1, sleeper));
    assertThrows(ImzalaRateLimitException.class,
        () -> resource.previewLayout(id, List.of(new FileInput("%PDF".getBytes(), "a.pdf")), "block"));

    assertEquals(1, calls(demandsApi));
    assertEquals(List.of(), sleeper.waits);
    assertFalse(sent.get(0).exists());
    Path parent = sent.get(0).toPath().getParent();
    assertFalse(Files.exists(parent), "temp directory must be removed");
  }

  // --- contacts ---

  @Test
  void contacts_list_forwards_every_filter_in_its_slot() throws ApiException {
    UUID companyId = UUID.randomUUID();
    ApiV1ContactsGet200ResponseData data = new ApiV1ContactsGet200ResponseData().total(0);
    when(contactsApi.apiV1ContactsGet(2, 40, "ayse", "-createdAt", companyId, true))
        .thenReturn(new ApiV1ContactsGet200Response().success(true).data(data));

    ApiV1ContactsGet200ResponseData result = new ContactsResource(contactsApi, NO_RETRY).list(
        new ListContactsParams().q("ayse").page(2).limit(40).sort("-createdAt").companyId(companyId).archived(true));

    assertSame(data, result);
    verify(contactsApi).apiV1ContactsGet(2, 40, "ayse", "-createdAt", companyId, true);
  }

  @Test
  void contacts_list_without_params_sends_nothing() throws ApiException {
    when(contactsApi.apiV1ContactsGet(null, null, null, null, null, null))
        .thenReturn(new ApiV1ContactsGet200Response().success(true).data(new ApiV1ContactsGet200ResponseData()));

    new ContactsResource(contactsApi, NO_RETRY).list();
    verify(contactsApi).apiV1ContactsGet(isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
  }

  @Test
  void contacts_list_all_walks_pages_and_keeps_filters() throws ApiException {
    ContactSummary a = new ContactSummary().id(UUID.randomUUID());
    ContactSummary b = new ContactSummary().id(UUID.randomUUID());
    ContactSummary c = new ContactSummary().id(UUID.randomUUID());
    when(contactsApi.apiV1ContactsGet(1, 10, "yilmaz", null, null, false)).thenReturn(new ApiV1ContactsGet200Response()
        .success(true).data(new ApiV1ContactsGet200ResponseData().contacts(new ArrayList<>(List.of(a, b))).total(3).page(1).limit(2)));
    when(contactsApi.apiV1ContactsGet(2, 10, "yilmaz", null, null, false)).thenReturn(new ApiV1ContactsGet200Response()
        .success(true).data(new ApiV1ContactsGet200ResponseData().contacts(new ArrayList<>(List.of(c))).total(3).page(2).limit(2)));

    List<ContactSummary> all = new ArrayList<>();
    for (ContactSummary contact : new ContactsResource(contactsApi, NO_RETRY)
        .listAll(new ListContactsParams().q("yilmaz").limit(10).archived(false))) {
      all.add(contact);
    }

    assertEquals(List.of(a, b, c), all);
    assertEquals(2, calls(contactsApi));
  }

  @Test
  void contacts_create_sends_the_body() throws ApiException {
    ApiV1ContactsPostRequest body = new ApiV1ContactsPostRequest().firstName("Ayşe").lastName("Yılmaz").email("ayse@example.com");
    ContactSummary summary = new ContactSummary().id(UUID.randomUUID());
    when(contactsApi.apiV1ContactsPost(body)).thenReturn(new ApiV1ContactsPost201Response().success(true).data(summary));

    assertSame(summary, new ContactsResource(contactsApi, NO_RETRY).create(body));
    verify(contactsApi).apiV1ContactsPost(body);
  }

  // --- reports ---

  @Test
  void reports_get_unwraps() throws ApiException {
    ApiV1ReportsGet200ResponseData data = new ApiV1ReportsGet200ResponseData();
    when(reportsApi.apiV1ReportsGet()).thenReturn(new ApiV1ReportsGet200Response().success(true).data(data));

    assertSame(data, new ReportsResource(reportsApi, NO_RETRY).get());
  }

  // --- timestamps ---

  @Test
  void timestamps_list_forwards_every_filter_in_its_slot() throws ApiException {
    LocalDate from = LocalDate.of(2026, 1, 1);
    LocalDate to = LocalDate.of(2026, 3, 31);
    ApiV1TimestampsGet200ResponseData data = new ApiV1TimestampsGet200ResponseData().total(0);
    when(timestampsApi.apiV1TimestampsGet(4, 25, "eser", "ACTIVE,VERIFIED", from, to, "-timestamp_date"))
        .thenReturn(new ApiV1TimestampsGet200Response().success(true).data(data));

    ApiV1TimestampsGet200ResponseData result = new TimestampsResource(timestampsApi, NO_RETRY).list(new ListTimestampsParams()
        .q("eser").status("ACTIVE,VERIFIED").from(from).to(to).page(4).limit(25).sort("-timestamp_date"));

    assertSame(data, result);
    verify(timestampsApi).apiV1TimestampsGet(4, 25, "eser", "ACTIVE,VERIFIED", from, to, "-timestamp_date");
  }

  @Test
  void timestamps_get_unwraps() throws ApiException {
    UUID id = UUID.randomUUID();
    when(timestampsApi.apiV1TimestampsIdGet(id))
        .thenReturn(new ApiV1TimestampsIdGet200Response().success(true).data(new TimestampListItem().id(id)));

    assertEquals(id, new TimestampsResource(timestampsApi, NO_RETRY).get(id).getId());
  }

  @Test
  void timestamps_resource_still_constructs_with_only_the_api() throws ApiException {
    when(timestampsApi.apiV1TimestampsGet(null, null, null, null, null, null, null))
        .thenReturn(new ApiV1TimestampsGet200Response().success(true).data(new ApiV1TimestampsGet200ResponseData()));
    assertNotNull(new TimestampsResource(timestampsApi).list());
  }

  // --- demands ---

  @Test
  void create_bulk_sends_the_body_and_no_workspace_header() throws ApiException {
    ApiV1DemandsBulkPostRequest body = new ApiV1DemandsBulkPostRequest().templateId(UUID.randomUUID());
    ApiV1DemandsBulkPost200ResponseData data = new ApiV1DemandsBulkPost200ResponseData().total(1).failed(0);
    when(demandsApi.apiV1DemandsBulkPost(body, null)).thenReturn(new ApiV1DemandsBulkPost200Response().success(true).data(data));

    assertSame(data, new DemandsResource(demandsApi, remindersApi, NO_RETRY).createBulk(body));
    verify(demandsApi).apiV1DemandsBulkPost(eq(body), isNull());
  }

  @Test
  void get_document_pdf_returns_bytes_and_forwards_both_ids_in_order() throws Exception {
    UUID id = UUID.randomUUID();
    UUID documentId = UUID.randomUUID();
    File tmp = Files.createTempFile("belge", ".pdf").toFile();
    Files.write(tmp.toPath(), "%PDF-belge".getBytes());
    when(demandsApi.apiV1DemandsIdBelgeDocumentIdPdfGet(id, documentId)).thenReturn(tmp);

    byte[] bytes = new DemandsResource(demandsApi, remindersApi, NO_RETRY).getDocumentPdf(id, documentId);

    assertArrayEquals("%PDF-belge".getBytes(), bytes);
    verify(demandsApi).apiV1DemandsIdBelgeDocumentIdPdfGet(id, documentId);
    assertFalse(tmp.exists());
  }

  @Test
  void get_document_pdf_maps_a_404() throws ApiException {
    UUID id = UUID.randomUUID();
    UUID documentId = UUID.randomUUID();
    when(demandsApi.apiV1DemandsIdBelgeDocumentIdPdfGet(id, documentId))
        .thenThrow(IdempotentWriteTest.status(404, "{\"success\":false,\"error\":\"DEMAND_NOT_FOUND\"}"));

    ImzalaException e = assertThrows(ImzalaException.class,
        () -> new DemandsResource(demandsApi, remindersApi, NO_RETRY).getDocumentPdf(id, documentId));
    assertEquals(404, e.getStatusCode());
    assertEquals("DEMAND_NOT_FOUND", e.getCode());
  }

  @Test
  @SuppressWarnings("unchecked")
  void upload_document_puts_every_new_field_in_its_own_slot() throws ApiException {
    UUID fieldTemplateId = UUID.randomUUID();
    UUID rolePartyA = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(demandsApi.apiV1DemandsUploadPost(anyList(), anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(new ApiV1DemandsUploadPost201Response().success(true).data(new CreatedDemandUpload().id(UUID.randomUUID())));

    new DemandsResource(demandsApi, remindersApi, NO_RETRY).uploadDocument(
        new UploadDemandParams(
            List.of(new FileInput("%PDF".getBytes(), "sozlesme.pdf")),
            List.of(new UploadPartyInput("Ayşe", "Yılmaz", "ayse@example.com", "+905551112233", rolePartyA)))
            .order(List.of(0))
            .title("Başlık")
            .description("Açıklama")
            .idempotencyKey("idem-key")
            .fieldTemplateId(fieldTemplateId)
            .onAnchorMiss("drop")
            .sendInvitations("sms")
            .force(true));

    ArgumentCaptor<String> parties = ArgumentCaptor.forClass(String.class);
    verify(demandsApi).apiV1DemandsUploadPost(
        anyList(),
        parties.capture(),
        eq("idem-key"),
        eq("[0]"),
        eq("Başlık"),
        eq("Açıklama"),
        eq(fieldTemplateId),
        eq("true"),
        eq("sms"),
        eq("drop"));
    assertEquals("""
        [{"first_name":"Ayşe","last_name":"Yılmaz","email":"ayse@example.com","phone":"+905551112233","template_party_id":"11111111-1111-1111-1111-111111111111"}]""",
        parties.getValue());
  }

  @Test
  void upload_document_leaves_optional_fields_unset_by_default() throws ApiException {
    when(demandsApi.apiV1DemandsUploadPost(anyList(), anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(new ApiV1DemandsUploadPost201Response().success(true).data(new CreatedDemandUpload()));

    UploadDemandParams params = new UploadDemandParams(
        List.of(new FileInput("%PDF".getBytes(), "a.pdf")),
        List.of(new UploadPartyInput("Ayşe", "Yılmaz", "ayse@example.com", null)))
        .force(false);
    new DemandsResource(demandsApi, remindersApi, NO_RETRY).uploadDocument(params);

    ArgumentCaptor<String> parties = ArgumentCaptor.forClass(String.class);
    verify(demandsApi).apiV1DemandsUploadPost(anyList(), parties.capture(),
        isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
    assertFalse(parties.getValue().contains("template_party_id"));
    assertNull(params.getSendInvitations());
    assertFalse(params.getForce());
  }

  @Test
  void every_upload_slot_carries_a_distinct_value_so_a_swap_is_visible() throws ApiException {
    // onAnchorMiss and sendInvitations are both Strings sitting next to each
    // other; distinct values make a swap fail the verification above. This
    // test pins the other same-typed neighbours (title, description, key).
    when(demandsApi.apiV1DemandsUploadPost(anyList(), anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(new ApiV1DemandsUploadPost201Response().success(true).data(new CreatedDemandUpload()));

    new DemandsResource(demandsApi, remindersApi, NO_RETRY).uploadDocument(
        new UploadDemandParams(
            List.of(new FileInput("%PDF".getBytes(), "a.pdf")),
            List.of(new UploadPartyInput("Ayşe", "Yılmaz", "ayse@example.com", null)))
            .description("only-description")
            .sendInvitations("email"));

    verify(demandsApi).apiV1DemandsUploadPost(anyList(), anyString(),
        isNull(), isNull(), isNull(), eq("only-description"), isNull(), isNull(), eq("email"), isNull());
  }

  // --- client wiring ---

  @Test
  void client_exposes_the_new_resources() {
    Imzala imzala = new Imzala("imz_test", "https://example.invalid");
    assertNotNull(imzala.fieldTemplates());
    assertNotNull(imzala.contacts());
    assertNotNull(imzala.reports());
    assertNotNull(imzala.timestamps());
  }
}
