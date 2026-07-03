package org.imzala;

import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.api.DemandsApi;
import org.imzala.client.generated.api.RemindersApi;
import org.imzala.client.generated.api.TemplatesApi;
import org.imzala.client.generated.model.ApiV1DemandsGet200Response;
import org.imzala.client.generated.model.ApiV1DemandsGet200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsGet200ResponseDataDemandsInner;
import org.imzala.client.generated.model.ApiV1DemandsIdCancelPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdCancelPost200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsIdCancelPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsIdPartiesPartyIdResendPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsIdTimelineGet200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdTimelineGet200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsIdTimelineGet200ResponseDataEventsInner;
import org.imzala.client.generated.model.ApiV1TemplatesIdDelete200Response;
import org.imzala.client.generated.model.ApiV1TemplatesIdDelete200ResponseData;
import org.imzala.client.generated.model.ApiV1TemplatesIdPatch200Response;
import org.imzala.client.generated.model.ApiV1TemplatesIdPatch200ResponseData;
import org.imzala.client.generated.model.ApiV1TemplatesIdPatchRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * v1 lifecycle facade tests — mirrors {@code lifecycle.test.ts} (Node):
 * the 7 new {@link DemandsResource} methods ({@code list}, {@code getPdf},
 * {@code getCertificate}, {@code getTimeline}, {@code cancel}, {@code
 * resendParty}, {@code delete}) and the 2 new {@link TemplatesResource}
 * methods ({@code update}, {@code delete}). Same seam as {@link
 * ClientTest}: mock the vendored generated {@code *Api} classes directly
 * and assert the resource classes wire the right generated method, forward
 * params, and unwrap {@code {success,data}} (or, for the binary endpoints,
 * read the downloaded {@link File} into a {@code byte[]} and delete it).
 */
@ExtendWith(MockitoExtension.class)
class LifecycleTest {

  private static final RetryConfig NO_RETRY = new RetryConfig(0, 0);

  @Mock
  private DemandsApi demandsApi;
  @Mock
  private RemindersApi remindersApi;
  @Mock
  private TemplatesApi templatesApi;

  private DemandsResource demands() {
    return new DemandsResource(demandsApi, remindersApi, NO_RETRY);
  }

  private TemplatesResource templates() {
    return new TemplatesResource(templatesApi, NO_RETRY);
  }

  // ---- demands ---------------------------------------------------------

  @Test
  void list_forwards_filters_and_unwraps_counts_only_data() throws ApiException {
    UUID d1 = UUID.randomUUID();
    UUID templateId = UUID.randomUUID();
    when(demandsApi.apiV1DemandsGet("PENDING", null, null, null, templateId, 1, 20, null)).thenReturn(
        new ApiV1DemandsGet200Response().success(true).data(new ApiV1DemandsGet200ResponseData()
            .demands(List.of(new ApiV1DemandsGet200ResponseDataDemandsInner().id(d1).partiesTotal(2).partiesSigned(1)))
            .total(1)
            .page(1)
            .limit(20)));

    ApiV1DemandsGet200ResponseData res = demands().list(
        new ListDemandsParams().status("PENDING").templateId(templateId).page(1).limit(20));

    verify(demandsApi).apiV1DemandsGet("PENDING", null, null, null, templateId, 1, 20, null);
    assertEquals(1, res.getTotal());
    assertEquals(1, res.getDemands().size());
    assertEquals(d1, res.getDemands().get(0).getId());
    assertEquals(2, res.getDemands().get(0).getPartiesTotal());
    assertEquals(1, res.getDemands().get(0).getPartiesSigned());
  }

  @Test
  void list_with_no_params_forwards_all_nulls() throws ApiException {
    when(demandsApi.apiV1DemandsGet(null, null, null, null, null, null, null, null)).thenReturn(
        new ApiV1DemandsGet200Response().success(true).data(new ApiV1DemandsGet200ResponseData().total(0)));

    ApiV1DemandsGet200ResponseData res = demands().list();

    verify(demandsApi).apiV1DemandsGet(null, null, null, null, null, null, null, null);
    assertEquals(0, res.getTotal());
  }

  @Test
  void getTimeline_unwraps_masked_events() throws ApiException {
    UUID id = UUID.randomUUID();
    when(demandsApi.apiV1DemandsIdTimelineGet(id)).thenReturn(
        new ApiV1DemandsIdTimelineGet200Response().success(true).data(new ApiV1DemandsIdTimelineGet200ResponseData()
            .events(List.of(new ApiV1DemandsIdTimelineGet200ResponseDataEventsInner()
                .id("e1").eventType("SIGNED").ipMasked("1.2.3.***")))));

    ApiV1DemandsIdTimelineGet200ResponseData res = demands().getTimeline(id);

    verify(demandsApi).apiV1DemandsIdTimelineGet(id);
    assertEquals(1, res.getEvents().size());
    assertEquals("1.2.3.***", res.getEvents().get(0).getIpMasked());
  }

  @Test
  void cancel_posts_the_reason_body_and_unwraps() throws ApiException {
    UUID id = UUID.randomUUID();
    when(demandsApi.apiV1DemandsIdCancelPost(eq(id), org.mockito.ArgumentMatchers.any(ApiV1DemandsIdCancelPostRequest.class)))
        .thenReturn(new ApiV1DemandsIdCancelPost200Response().success(true)
            .data(new ApiV1DemandsIdCancelPost200ResponseData().id(id).status("CANCELLED").cancellationReason("vazgeçildi")));

    ApiV1DemandsIdCancelPost200ResponseData res =
        demands().cancel(id, new ApiV1DemandsIdCancelPostRequest().reason("vazgeçildi"));

    ArgumentCaptor<ApiV1DemandsIdCancelPostRequest> captor = ArgumentCaptor.forClass(ApiV1DemandsIdCancelPostRequest.class);
    verify(demandsApi).apiV1DemandsIdCancelPost(eq(id), captor.capture());
    assertEquals("vazgeçildi", captor.getValue().getReason());
    assertEquals("CANCELLED", res.getStatus());
  }

  @Test
  void cancel_defaults_body_to_empty_request_when_omitted() throws ApiException {
    UUID id = UUID.randomUUID();
    when(demandsApi.apiV1DemandsIdCancelPost(eq(id), org.mockito.ArgumentMatchers.any(ApiV1DemandsIdCancelPostRequest.class)))
        .thenReturn(new ApiV1DemandsIdCancelPost200Response().success(true)
            .data(new ApiV1DemandsIdCancelPost200ResponseData().id(id).status("CANCELLED")));

    demands().cancel(id);

    ArgumentCaptor<ApiV1DemandsIdCancelPostRequest> captor = ArgumentCaptor.forClass(ApiV1DemandsIdCancelPostRequest.class);
    verify(demandsApi).apiV1DemandsIdCancelPost(eq(id), captor.capture());
    // an empty (non-null) request body is sent, with no reason
    assertEquals(null, captor.getValue().getReason());
  }

  @Test
  void resendParty_targets_a_single_party() throws ApiException {
    UUID id = UUID.randomUUID();
    UUID partyId = UUID.randomUUID();
    when(demandsApi.apiV1DemandsIdPartiesPartyIdResendPost(id, partyId)).thenReturn(
        new ApiV1DemandsIdPartiesPartyIdResendPost200Response().success(true)
            .data(new ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData().sent(List.of("email"))));

    ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData res = demands().resendParty(id, partyId);

    verify(demandsApi).apiV1DemandsIdPartiesPartyIdResendPost(id, partyId);
    assertEquals(List.of("email"), res.getSent());
  }

  @Test
  void delete_unwraps_deletion_result() throws ApiException {
    UUID id = UUID.randomUUID();
    when(demandsApi.apiV1DemandsIdDelete(id)).thenReturn(
        new ApiV1TemplatesIdDelete200Response().success(true)
            .data(new ApiV1TemplatesIdDelete200ResponseData().id(id).deleted(true)));

    ApiV1TemplatesIdDelete200ResponseData res = demands().delete(id);

    verify(demandsApi).apiV1DemandsIdDelete(id);
    assertEquals(id, res.getId());
    assertTrue(res.getDeleted());
  }

  @Test
  void getPdf_returns_raw_bytes_and_deletes_temp_file() throws ApiException, Exception {
    UUID id = UUID.randomUUID();
    byte[] pdf = "%PDF-1.7 fake".getBytes(StandardCharsets.UTF_8);
    File tempFile = writeTempFile(pdf);
    when(demandsApi.apiV1DemandsIdPdfGet(id)).thenReturn(tempFile);

    byte[] out = demands().getPdf(id);

    verify(demandsApi).apiV1DemandsIdPdfGet(id);
    assertArrayEquals(pdf, out);
    assertTrue(new String(out, StandardCharsets.UTF_8).contains("%PDF-1.7"));
    // the downloaded temp file must be cleaned up after reading
    assertFalse(tempFile.exists());
  }

  @Test
  void getCertificate_forwards_lang_and_returns_bytes() throws ApiException, Exception {
    UUID id = UUID.randomUUID();
    byte[] cert = "%PDF cert".getBytes(StandardCharsets.UTF_8);
    File tempFile = writeTempFile(cert);
    when(demandsApi.apiV1DemandsIdCertificateGet(id, "en")).thenReturn(tempFile);

    byte[] out = demands().getCertificate(id, "en");

    verify(demandsApi).apiV1DemandsIdCertificateGet(id, "en");
    assertArrayEquals(cert, out);
    assertFalse(tempFile.exists());
  }

  @Test
  void getCertificate_default_lang_is_null() throws ApiException, Exception {
    UUID id = UUID.randomUUID();
    File tempFile = writeTempFile("%PDF".getBytes(StandardCharsets.UTF_8));
    when(demandsApi.apiV1DemandsIdCertificateGet(id, null)).thenReturn(tempFile);

    demands().getCertificate(id);

    verify(demandsApi).apiV1DemandsIdCertificateGet(id, null);
  }

  /** Java-specific: the binary path must still normalize a thrown {@link ApiException} to a typed {@link ImzalaException}. */
  @Test
  void getPdf_maps_ApiException_to_typed_ImzalaException() throws ApiException {
    UUID id = UUID.randomUUID();
    HttpHeaders headers = HttpHeaders.of(Map.of(), (a, b) -> true);
    when(demandsApi.apiV1DemandsIdPdfGet(id)).thenThrow(
        new ApiException("apiV1DemandsIdPdfGet call failed with: 404 - not found", 404, headers,
            "{\"success\":false,\"error\":\"DEMAND_NOT_FOUND\"}"));

    ImzalaException err = assertThrows(ImzalaException.class, () -> demands().getPdf(id));
    assertEquals(404, err.getStatusCode());
    verifyNoMoreInteractions(remindersApi);
  }

  // ---- templates -------------------------------------------------------

  @Test
  void templates_update_patches_metadata_and_unwraps() throws ApiException {
    UUID id = UUID.randomUUID();
    when(templatesApi.apiV1TemplatesIdPatch(eq(id), org.mockito.ArgumentMatchers.any(ApiV1TemplatesIdPatchRequest.class)))
        .thenReturn(new ApiV1TemplatesIdPatch200Response().success(true)
            .data(new ApiV1TemplatesIdPatch200ResponseData().id(id).name("Yeni Ad")));

    ApiV1TemplatesIdPatch200ResponseData res =
        templates().update(id, new ApiV1TemplatesIdPatchRequest().name("Yeni Ad"));

    ArgumentCaptor<ApiV1TemplatesIdPatchRequest> captor = ArgumentCaptor.forClass(ApiV1TemplatesIdPatchRequest.class);
    verify(templatesApi).apiV1TemplatesIdPatch(eq(id), captor.capture());
    assertEquals("Yeni Ad", captor.getValue().getName());
    assertEquals("Yeni Ad", res.getName());
  }

  @Test
  void templates_delete_soft_deletes_and_unwraps() throws ApiException {
    UUID id = UUID.randomUUID();
    when(templatesApi.apiV1TemplatesIdDelete(id)).thenReturn(
        new ApiV1TemplatesIdDelete200Response().success(true)
            .data(new ApiV1TemplatesIdDelete200ResponseData().id(id).deleted(true)));

    ApiV1TemplatesIdDelete200ResponseData res = templates().delete(id);

    verify(templatesApi).apiV1TemplatesIdDelete(id);
    assertEquals(id, res.getId());
    assertTrue(res.getDeleted());
  }

  // ---- helpers ---------------------------------------------------------

  /** Mimics the generated client's binary handling: a real temp file the resource must read + delete. */
  private static File writeTempFile(byte[] content) throws Exception {
    Path path = Files.createTempFile("imzala-test-download-", ".pdf");
    Files.write(path, content);
    return path.toFile();
  }
}
