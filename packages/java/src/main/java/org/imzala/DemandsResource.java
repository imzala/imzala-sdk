package org.imzala;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.imzala.client.generated.api.DemandsApi;
import org.imzala.client.generated.api.RemindersApi;
import org.imzala.client.generated.model.ApiV1DemandsIdRemindersPost200ResponseData;
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

  DemandsResource(DemandsApi api, RemindersApi remindersApi) {
    this.api = api;
    this.remindersApi = remindersApi;
  }

  /** Creates a new demand (contract) from a template. */
  public CreatedDemand create(CreateDemandRequest body) {
    return Http.unwrap(
        () -> api.apiV1DemandsPost(body),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /** Returns a demand's status + per-party signing progress. */
  public DemandStatus get(UUID id) {
    return Http.unwrap(
        () -> api.apiV1DemandsIdGet(id),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /**
   * Places (replaces) signature/form fields on a demand's pages. See
   * {@code UpsertItemsRequest.getPageIds()} for full-replace vs
   * per-page-replace semantics.
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

      return Http.unwrap(
          () -> api.apiV1DemandsUploadPost(files, partiesJson, orderJson, params.getTitle(), params.getDescription()),
          r -> Boolean.TRUE.equals(r.getSuccess()),
          r -> r.getData());
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
   * per-person cap of 3 reminders per channel (not overridable).
   *
   * <p>Routes through the vendored generated {@code RemindersApi}, not
   * {@code DemandsApi} — the OpenAPI spec groups {@code POST
   * /api/v1/demands/{id}/reminders} under a {@code Reminders} tag even
   * though the route lives under {@code demands}. Same gotcha B1 (TS), B2
   * (Python), and B3 (C#) flagged for their generators.
   */
  public ApiV1DemandsIdRemindersPost200ResponseData sendReminder(UUID id, TriggerReminderRequest body) {
    TriggerReminderRequest effectiveBody = body != null ? body : new TriggerReminderRequest();
    return Http.unwrap(
        () -> remindersApi.apiV1DemandsIdRemindersPost(id, effectiveBody),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
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
