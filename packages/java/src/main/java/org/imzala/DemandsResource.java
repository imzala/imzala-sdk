package org.imzala;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.api.DemandsApi;
import org.imzala.client.generated.api.RemindersApi;
import org.imzala.client.generated.model.ApiV1DemandsBulkPost200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsBulkPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsGet200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsIdCancelPost200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsIdCancelPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsIdRemindersPost200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsIdTimelineGet200ResponseData;
import org.imzala.client.generated.model.ApiV1TemplatesIdDelete200ResponseData;
import org.imzala.client.generated.model.CreateDemandRequest;
import org.imzala.client.generated.model.CreatedDemand;
import org.imzala.client.generated.model.CreatedDemandUpload;
import org.imzala.client.generated.model.DemandStatus;
import org.imzala.client.generated.model.TriggerReminderRequest;
import org.imzala.client.generated.model.UpsertItemsRequest;
import org.imzala.client.generated.model.UpsertItemsResponseData;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * {@code imzala.demands()}. Backed by both the vendored generated {@code
 * DemandsApi} and {@code RemindersApi} — see {@link #sendReminder}.
 */
public final class DemandsResource {

  private static final ObjectMapper JSON = new ObjectMapper();

  private final DemandsApi api;
  private final RemindersApi remindersApi;
  private final RetryConfig retryConfig;

  DemandsResource(DemandsApi api, RemindersApi remindersApi, RetryConfig retryConfig) {
    this.api = api;
    this.remindersApi = remindersApi;
    this.retryConfig = retryConfig;
  }

  /**
   * Creates a new demand (contract) from a template, without an idempotency
   * key: a single attempt, never retried (a retried create would produce a
   * duplicate demand). See {@link #create(CreateDemandRequest, String)}.
   */
  public CreatedDemand create(CreateDemandRequest body) {
    return create(body, null);
  }

  /**
   * Creates a new demand (contract) from a template.
   *
   * <p>Without {@code idempotencyKey} this is a single attempt: a retried
   * create would produce a duplicate demand. With a key, a repeated request
   * does not create a second demand, so one retry is made after a 429
   * (waiting Retry-After, at most 60 s). A key reused with a different body
   * throws {@code IDEMPOTENCY_KEY_REUSED}.
   *
   * <p>An {@code expiry_date} that is not a real calendar day throws {@code
   * INVALID_EXPIRY_DATE}.
   *
   * @param idempotencyKey your own reference for this request (e.g. an order number); {@code null} for none
   */
  public CreatedDemand create(CreateDemandRequest body, String idempotencyKey) {
    return Http.unwrapIdempotentWrite(
        () -> api.apiV1DemandsPost(body, idempotencyKey),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        idempotencyKey,
        retryConfig);
  }

  /**
   * Creates up to 10 demands from one template in a single request. Rows are
   * created independently; check {@code failed} and each result's {@code
   * status}.
   *
   * <p>This endpoint has no idempotency key, so it is never retried: a
   * retried batch would create the demands again. Split larger lists into
   * batches of 10 yourself. POST.
   */
  public ApiV1DemandsBulkPost200ResponseData createBulk(ApiV1DemandsBulkPostRequest body) {
    return Http.unwrap(
        () -> api.apiV1DemandsBulkPost(body, /* xWorkspaceId */ null),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /** Returns a demand's status + per-party signing progress. GET — safe to auto-retry. */
  public DemandStatus get(UUID id) {
    return Http.unwrapRetryableGet(
        () -> api.apiV1DemandsIdGet(id),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        retryConfig);
  }

  /**
   * Places (replaces) signature/form fields on a demand's pages. See
   * {@code UpsertItemsRequest.getPageIds()} for full-replace vs
   * per-page-replace semantics. Every item needs an integer {@code page_id}
   * ({@code PAGE_ID_REQUIRED}); an unknown {@code item_type} throws {@code
   * INVALID_ITEM_TYPE}. POST, never auto-retried.
   */
  public UpsertItemsResponseData addItems(UUID id, UpsertItemsRequest body) {
    return Http.unwrap(
        () -> api.apiV1DemandsIdItemsPost(id, body),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /**
   * Creates a demand directly from an uploaded document (no template) — a
   * single PDF/DOC/DOCX/ODT/RTF/TXT, or 1-20 images merged into one PDF.
   *
   * <p>See {@link FileInput} for why this writes {@link
   * UploadDemandParams#getFiles()} to throwaway temp files — the vendored
   * generated client's multipart layer requires real {@code java.io.File}s.
   * Temp files (and their parent temp directories) are always deleted
   * before this method returns, success or failure.
   *
   * <p>Without {@link UploadDemandParams#idempotencyKey(String)} this is a
   * single attempt. With a key, one retry is made after a 429 (waiting
   * Retry-After, at most 60 s). Signing invitations are not sent unless
   * {@link UploadDemandParams#sendInvitations(String)} asks for them. POST.
   */
  public CreatedDemandUpload uploadDocument(UploadDemandParams params) {
    List<File> tempFiles = new ArrayList<>(params.getFiles().size());
    try {
      List<File> files = new ArrayList<>(params.getFiles().size());
      for (FileInput fileInput : params.getFiles()) {
        File tempFile = fileInput.toTempFile();
        tempFiles.add(tempFile);
        files.add(tempFile);
      }

      String partiesJson = writeJson(params.getParties());
      String orderJson = params.getOrder() != null ? writeJson(params.getOrder()) : null;

      String idempotencyKey = params.getIdempotencyKey();
      String force = params.getForce() ? "true" : null;
      return Http.unwrapIdempotentWrite(
          // Labelled slots: the generated signature inserts new optional
          // parameters between existing ones and several share a type, so a
          // shifted argument would still compile. ResourcesTest pins each slot.
          () -> api.apiV1DemandsUploadPost(
              /* files */ files,
              /* parties */ partiesJson,
              /* idempotencyKey */ idempotencyKey,
              /* order */ orderJson,
              /* title */ params.getTitle(),
              /* description */ params.getDescription(),
              /* fieldTemplateId */ params.getFieldTemplateId(),
              /* force */ force,
              /* sendInvitations */ params.getSendInvitations(),
              /* onAnchorMiss */ params.getOnAnchorMiss()),
          r -> Boolean.TRUE.equals(r.getSuccess()),
          r -> r.getData(),
          idempotencyKey,
          retryConfig);
    } finally {
      cleanupTempFiles(tempFiles);
    }
  }

  /**
   * Triggers an immediate SMS/email reminder to a demand's unsigned
   * parties, with default options (equivalent to {@code {}}).
   */
  public ApiV1DemandsIdRemindersPost200ResponseData sendReminder(UUID id) {
    return sendReminder(id, null);
  }

  /**
   * Triggers an immediate SMS/email reminder to a demand's unsigned
   * parties. Independent of the template/demand's scheduled {@code
   * reminder_settings}. Subject to a 5-minute anti-spam window (override
   * with {@code new TriggerReminderRequest().force(true)}) and a hard
   * per-person cap of 3 reminders per channel (not overridable). The
   * anti-spam window answers 429 {@code RATE_LIMITED}. A draft, completed,
   * cancelled or expired demand throws 409 ({@code DEMAND_NOT_DISPATCHED},
   * {@code DEMAND_NOT_DISPATCHABLE}, {@code DEMAND_EXPIRED}).
   *
   * <p>Routes through the vendored generated {@code RemindersApi}, not
   * {@code DemandsApi} — the OpenAPI spec groups {@code POST
   * /api/v1/demands/{id}/reminders} under a {@code Reminders} tag even
   * though the route lives under {@code demands}. Same gotcha B1 (TS), B2
   * (Python), and B3 (C#) flagged for their generators. POST — never
   * auto-retried (a retried call could double-send).
   */
  public ApiV1DemandsIdRemindersPost200ResponseData sendReminder(UUID id, TriggerReminderRequest body) {
    TriggerReminderRequest effectiveBody = body != null ? body : new TriggerReminderRequest();
    return Http.unwrap(
        () -> remindersApi.apiV1DemandsIdRemindersPost(id, effectiveBody),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /**
   * Lists your demands — counts-only, first page, no filters. See {@link
   * #list(ListDemandsParams)}.
   */
  public ApiV1DemandsGet200ResponseData list() {
    return list(null);
  }

  /**
   * Lists your demands — counts-only (id/title/status/timestamps +
   * {@code parties_total}/{@code parties_signed}, <b>no</b> party
   * names/emails/phones). Filter by status/date/template, paginate with
   * page/limit. GET — safe to auto-retry. For per-party detail use {@link
   * #get(UUID)}.
   */
  public ApiV1DemandsGet200ResponseData list(ListDemandsParams params) {
    ListDemandsParams p = params != null ? params : new ListDemandsParams();
    return Http.unwrapRetryableGet(
        () -> api.apiV1DemandsGet(
            p.getStatus(),
            p.getQ(),
            p.getFrom(),
            p.getTo(),
            p.getTemplateId(),
            p.getPage(),
            p.getLimit(),
            p.getSort()),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        retryConfig);
  }

  /**
   * Downloads the signed contract PDF (only once {@code status ==
   * COMPLETED}) as raw bytes. Requires the API key's owner to own the
   * demand.
   *
   * <p>The vendored generated client materializes an {@code
   * application/pdf} response as a temp {@link File}; this reads it into a
   * {@code byte[]} and deletes that temp file before returning. GET.
   */
  public byte[] getPdf(UUID id) {
    return toBytes(() -> api.apiV1DemandsIdPdfGet(id));
  }

  /**
   * Downloads the PDF of one document in a multi-document envelope as raw
   * bytes. For the whole contract use {@link #getPdf(UUID)}. Same temp-file
   * handling as {@link #getPdf(UUID)}. GET.
   *
   * @param id the demand (envelope)
   * @param documentId the document inside it
   */
  public byte[] getDocumentPdf(UUID id, UUID documentId) {
    return toBytes(() -> api.apiV1DemandsIdBelgeDocumentIdPdfGet(id, documentId));
  }

  /**
   * Downloads the completion certificate (PAdES B-T sealed audit document)
   * as raw bytes. Only produced for {@code COMPLETED} demands. In the
   * default (Turkish) language — see {@link #getCertificate(UUID, String)}
   * for English. GET.
   */
  public byte[] getCertificate(UUID id) {
    return getCertificate(id, null);
  }

  /**
   * Downloads the completion certificate as raw bytes, in the requested
   * language. Pass {@code "en"} for English (default is Turkish). Same temp-
   * {@link File}-to-{@code byte[]} handling as {@link #getPdf(UUID)}. GET.
   */
  public byte[] getCertificate(UUID id, String lang) {
    return toBytes(() -> api.apiV1DemandsIdCertificateGet(id, lang));
  }

  /**
   * Returns the signing audit trail (view/sign/reject events). PII-masked:
   * {@code ip_masked} (last octet hidden), actor name+email masked, no raw
   * IP/device. GET — safe to auto-retry.
   */
  public ApiV1DemandsIdTimelineGet200ResponseData getTimeline(UUID id) {
    return Http.unwrapRetryableGet(
        () -> api.apiV1DemandsIdTimelineGet(id),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        retryConfig);
  }

  /**
   * Cancels (voids) a pending demand, with default options (no reason). See
   * {@link #cancel(UUID, ApiV1DemandsIdCancelPostRequest)}.
   */
  public ApiV1DemandsIdCancelPost200ResponseData cancel(UUID id) {
    return cancel(id, null);
  }

  /**
   * Cancels (voids) a pending demand — sets it to {@code CANCELLED} and
   * stops any scheduled reminders. A {@code COMPLETED} (or already-cancelled)
   * demand can't be cancelled (throws). Attach a reason with {@code new
   * ApiV1DemandsIdCancelPostRequest().reason("...")}. POST — never
   * auto-retried.
   */
  public ApiV1DemandsIdCancelPost200ResponseData cancel(UUID id, ApiV1DemandsIdCancelPostRequest body) {
    ApiV1DemandsIdCancelPostRequest effectiveBody = body != null ? body : new ApiV1DemandsIdCancelPostRequest();
    return Http.unwrap(
        () -> api.apiV1DemandsIdCancelPost(id, effectiveBody),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /**
   * Re-sends the signing invitation to a single party (by {@code party_id}
   * from the demand's create/get response). Can't resend to a party who has
   * already signed or declined, or one whose turn hasn't come in ordered
   * signing (throws). POST — never auto-retried.
   */
  public ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData resendParty(UUID id, UUID partyId) {
    return Http.unwrap(
        () -> api.apiV1DemandsIdPartiesPartyIdResendPost(id, partyId),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /**
   * Deletes a demand and all its data. Only NON-completed demands can be
   * deleted via the API — a {@code COMPLETED} demand (signed document +
   * audit trail) returns 409 and must be removed from the dashboard. The
   * deletion result reuses the shared {@code
   * ApiV1TemplatesIdDelete200ResponseData} shape ({@code id}/{@code
   * deleted}). DELETE — never auto-retried.
   */
  public ApiV1TemplatesIdDelete200ResponseData delete(UUID id) {
    return Http.unwrap(
        () -> api.apiV1DemandsIdDelete(id),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /**
   * Runs a generated-client call that returns a downloaded {@link File}
   * (a binary {@code application/pdf} response), reads it fully into a
   * {@code byte[]}, and deletes the temp file. Any thrown checked {@link
   * ApiException} is normalized to a typed {@link ImzalaException} via
   * {@link ErrorMapper}, exactly like {@link Http#unwrap}. Unlike the
   * envelope endpoints there is no {@code {success,data}} to unwrap — the
   * body is the raw file.
   */
  private static byte[] toBytes(Http.ApiCall<File> call) {
    File file;
    try {
      file = call.call();
    } catch (ApiException err) {
      throw ErrorMapper.map(err);
    }

    if (file == null) {
      return new byte[0];
    }

    try {
      return Files.readAllBytes(file.toPath());
    } catch (IOException e) {
      throw new ImzalaException("İndirilen dosya okunamadı (failed to read downloaded file)", null, null, null, e);
    } finally {
      try {
        Files.deleteIfExists(file.toPath());
      } catch (IOException ignored) {
        // best-effort — the generated client already registered deleteOnExit
      }
    }
  }

  private static String writeJson(Object value) {
    try {
      return JSON.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(new IOException(e));
    }
  }

  private static void cleanupTempFiles(List<File> files) {
    for (File file : files) {
      Path parent = file.toPath().getParent();
      try {
        Files.deleteIfExists(file.toPath());
      } catch (IOException ignored) {
        // best-effort — the OS temp dir is cleaned up eventually regardless
      }
      if (parent != null) {
        try {
          Files.deleteIfExists(parent);
        } catch (IOException ignored) {
          // best-effort
        }
      }
    }
  }
}
