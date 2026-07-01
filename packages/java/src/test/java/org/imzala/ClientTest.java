package org.imzala;

import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.api.AccountApi;
import org.imzala.client.generated.api.DemandsApi;
import org.imzala.client.generated.api.RemindersApi;
import org.imzala.client.generated.api.TemplatesApi;
import org.imzala.client.generated.model.ApiV1DemandsIdEmbedSessionPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdEmbedSessionPost200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsIdEmbedSessionPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsIdGet200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdRemindersPost200Response;
import org.imzala.client.generated.model.ApiV1DemandsIdRemindersPost200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsUploadPost201Response;
import org.imzala.client.generated.model.ApiV1MeGet200Response;
import org.imzala.client.generated.model.ApiV1MeGet200ResponseData;
import org.imzala.client.generated.model.ApiV1TemplatesGet200Response;
import org.imzala.client.generated.model.ApiV1TemplatesGet200ResponseData;
import org.imzala.client.generated.model.ApiV1TemplatesIdGet200Response;
import org.imzala.client.generated.model.CreatedDemandUpload;
import org.imzala.client.generated.model.DemandStatus;
import org.imzala.client.generated.model.TemplateSummary;
import org.imzala.client.generated.model.TriggerReminderRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Envelope-unwrap tests — mirrors {@code client.test.ts} (B1, {@code
 * vi.spyOn} on the generated {@code *Api.prototype}), {@code
 * test_client.py} (B2, {@code unittest.mock.patch.object} on the generated
 * {@code *Api} methods), and {@code ClientTests.cs} (B3, {@code
 * Mock<IXxxApi>}): mock the vendored generated client's concrete {@code
 * *Api} classes (Mockito 5's default inline mock-maker can mock
 * non-interface concrete classes directly — there is no {@code IXxxApi}
 * interface in the {@code java}/{@code native} generator output, unlike
 * the C# {@code httpclient} generator) and assert the resource classes
 * unwrap {@code {success,data}} to the inner data.
 */
@ExtendWith(MockitoExtension.class)
class ClientTest {

  @Mock
  private DemandsApi demandsApi;
  @Mock
  private RemindersApi remindersApi;
  @Mock
  private TemplatesApi templatesApi;
  @Mock
  private AccountApi accountApi;

  @Test
  void demands_get_unwraps_success_data_to_inner_data() throws ApiException {
    UUID id = UUID.randomUUID();
    when(demandsApi.apiV1DemandsIdGet(id)).thenReturn(
        new ApiV1DemandsIdGet200Response().success(true).data(new DemandStatus().id(id).status(DemandStatus.StatusEnum.PENDING)));

    DemandsResource resource = new DemandsResource(demandsApi, remindersApi);
    DemandStatus result = resource.get(id);

    assertEquals(id, result.getId());
    assertEquals(DemandStatus.StatusEnum.PENDING, result.getStatus());
  }

  @Test
  void me_calls_accountApi_and_unwraps() throws ApiException {
    UUID userId = UUID.randomUUID();
    when(accountApi.apiV1MeGet()).thenReturn(
        new ApiV1MeGet200Response().success(true).data(new ApiV1MeGet200ResponseData().id(userId).email("a@b.com")));

    AccountResource account = new AccountResource(accountApi);
    ApiV1MeGet200ResponseData result = account.me();

    assertEquals(userId, result.getId());
    assertEquals("a@b.com", result.getEmail());
  }

  @Test
  void templates_list_forwards_page_limit_and_unwraps() throws ApiException {
    when(templatesApi.apiV1TemplatesGet(2, 10)).thenReturn(
        new ApiV1TemplatesGet200Response().success(true).data(new ApiV1TemplatesGet200ResponseData()
            .templates(List.of(new TemplateSummary().id(UUID.randomUUID())))
            .total(1)
            .page(2)
            .limit(10)));

    TemplatesResource resource = new TemplatesResource(templatesApi);
    ApiV1TemplatesGet200ResponseData result = resource.list(2, 10);

    verify(templatesApi).apiV1TemplatesGet(2, 10);
    assertEquals(1, result.getTemplates().size());
    assertEquals(1, result.getTotal());
  }

  @Test
  void demands_sendReminder_routes_through_remindersApi_not_demandsApi() throws ApiException {
    UUID id = UUID.randomUUID();
    when(remindersApi.apiV1DemandsIdRemindersPost(eq(id), any(TriggerReminderRequest.class))).thenReturn(
        new ApiV1DemandsIdRemindersPost200Response().success(true).data(new ApiV1DemandsIdRemindersPost200ResponseData().demandId(id)));

    DemandsResource resource = new DemandsResource(demandsApi, remindersApi);
    ApiV1DemandsIdRemindersPost200ResponseData result = resource.sendReminder(id, new TriggerReminderRequest().force(true));

    ArgumentCaptor<TriggerReminderRequest> captor = ArgumentCaptor.forClass(TriggerReminderRequest.class);
    verify(remindersApi).apiV1DemandsIdRemindersPost(eq(id), captor.capture());
    assertTrue(captor.getValue().getForce());
    verifyNoMoreInteractions(demandsApi);
    assertEquals(id, result.getDemandId());
  }

  @Test
  void demands_sendReminder_defaults_body_to_empty_request_when_omitted() throws ApiException {
    UUID id = UUID.randomUUID();
    when(remindersApi.apiV1DemandsIdRemindersPost(eq(id), any(TriggerReminderRequest.class))).thenReturn(
        new ApiV1DemandsIdRemindersPost200Response().success(true).data(new ApiV1DemandsIdRemindersPost200ResponseData().demandId(id)));

    DemandsResource resource = new DemandsResource(demandsApi, remindersApi);
    resource.sendReminder(id);

    ArgumentCaptor<TriggerReminderRequest> captor = ArgumentCaptor.forClass(TriggerReminderRequest.class);
    verify(remindersApi).apiV1DemandsIdRemindersPost(eq(id), captor.capture());
    assertEquals(Boolean.FALSE, captor.getValue().getForce());
  }

  @Test
  void embed_createSession_maps_partyId_and_unwraps() throws ApiException {
    UUID demandId = UUID.randomUUID();
    UUID partyId = UUID.randomUUID();
    when(demandsApi.apiV1DemandsIdEmbedSessionPost(eq(demandId), any(ApiV1DemandsIdEmbedSessionPostRequest.class)))
        .thenReturn(new ApiV1DemandsIdEmbedSessionPost200Response().success(true).data(
            new ApiV1DemandsIdEmbedSessionPost200ResponseData()
                .embedToken("tok")
                .embedUrl(URI.create("https://e.imzala.org/embed/sign?token=tok"))));

    EmbedResource resource = new EmbedResource(demandsApi);
    ApiV1DemandsIdEmbedSessionPost200ResponseData result = resource.createSession(demandId, partyId);

    ArgumentCaptor<ApiV1DemandsIdEmbedSessionPostRequest> captor = ArgumentCaptor.forClass(ApiV1DemandsIdEmbedSessionPostRequest.class);
    verify(demandsApi).apiV1DemandsIdEmbedSessionPost(eq(demandId), captor.capture());
    assertEquals(partyId, captor.getValue().getPartyId());
    assertEquals("tok", result.getEmbedToken());
  }

  @Test
  @SuppressWarnings("unchecked") // ArgumentCaptor.forClass(List.class) — Mockito's standard raw-class-literal pattern for a generic captor type
  void demands_uploadDocument_JSON_encodes_parties_order_and_builds_temp_files() throws ApiException {
    when(demandsApi.apiV1DemandsUploadPost(anyList(), anyString(), any(), any(), any())).thenReturn(
        new ApiV1DemandsUploadPost201Response().success(true).data(new CreatedDemandUpload().id(UUID.randomUUID())));

    DemandsResource resource = new DemandsResource(demandsApi, remindersApi);
    CreatedDemandUpload result = resource.uploadDocument(
        new UploadDemandParams(
            List.of(new FileInput("hello".getBytes(), "a.pdf", "application/pdf")),
            List.of(new UploadPartyInput("Ada", "Lovelace", "ada@example.com", null)))
            .order(List.of(0))
            .title("Test"));

    ArgumentCaptor<List<File>> filesCaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<String> partiesCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> orderCaptor = ArgumentCaptor.forClass(String.class);
    verify(demandsApi).apiV1DemandsUploadPost(filesCaptor.capture(), partiesCaptor.capture(), orderCaptor.capture(), eq("Test"), isNull());

    assertEquals(1, filesCaptor.getValue().size());
    assertEquals("a.pdf", filesCaptor.getValue().get(0).getName());
    assertEquals("""
        [{"first_name":"Ada","last_name":"Lovelace","email":"ada@example.com"}]""", partiesCaptor.getValue());
    assertEquals("[0]", orderCaptor.getValue());
    assertNotNull(result.getId());

    // temp file + its parent temp dir must both be cleaned up after the call
    assertFalse(filesCaptor.getValue().get(0).exists());
  }

  @Test
  void throws_ImzalaException_when_server_returns_success_false_on_2xx() throws ApiException {
    when(templatesApi.apiV1TemplatesIdGet(any(UUID.class))).thenReturn(
        new ApiV1TemplatesIdGet200Response().success(false));

    TemplatesResource resource = new TemplatesResource(templatesApi);

    assertThrows(ImzalaException.class, () -> resource.get(UUID.randomUUID()));
  }

  @Test
  void construction_requires_an_apiKey() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Imzala(""));
    assertTrue(ex.getMessage().contains("apiKey is required"));
  }

  @Test
  void construction_defaults_baseUrl_to_prod() {
    Imzala imzala = new Imzala("imz_test");
    assertNotNull(imzala.templates());
    assertNotNull(imzala.demands());
    assertNotNull(imzala.embed());
    assertNotNull(imzala.timestamps());
  }

  @Test
  void construction_honors_a_custom_baseUrl() {
    // Construction itself must not throw for a custom (e.g. test-environment) baseUrl.
    Imzala imzala = new Imzala("imz_test", "https://test-api.imzala.org");
    assertNotNull(imzala);
  }
}
