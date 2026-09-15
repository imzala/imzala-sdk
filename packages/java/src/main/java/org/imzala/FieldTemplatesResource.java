package org.imzala;

import org.imzala.client.generated.api.DemandsApi;
import org.imzala.client.generated.api.TemplatesApi;
import org.imzala.client.generated.model.ApiV1FieldTemplatesGet200ResponseData;
import org.imzala.client.generated.model.FieldLayoutPreview;
import org.imzala.client.generated.model.FieldTemplateDetail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * {@code imzala.fieldTemplates()}. A field template is separate from a
 * contract template: it describes where fields land on an uploaded PDF,
 * located by anchor text. Use it with {@link
 * UploadDemandParams#fieldTemplateId(UUID)}.
 */
public final class FieldTemplatesResource {

  private final TemplatesApi templatesApi;
  private final DemandsApi demandsApi;
  private final RetryConfig retryConfig;

  FieldTemplatesResource(TemplatesApi templatesApi, DemandsApi demandsApi, RetryConfig retryConfig) {
    this.templatesApi = templatesApi;
    this.demandsApi = demandsApi;
    this.retryConfig = retryConfig;
  }

  /** Lists your field templates (first page, server paging). GET, safe to auto-retry. */
  public ApiV1FieldTemplatesGet200ResponseData list() {
    return list(null, null);
  }

  /** Lists your field templates (one page). {@code limit} is 1 to 100; larger values are clamped, not rejected. GET, safe to auto-retry. */
  public ApiV1FieldTemplatesGet200ResponseData list(Integer page, Integer limit) {
    return Http.unwrapRetryableGet(
        () -> templatesApi.apiV1FieldTemplatesGet(page, limit),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        retryConfig);
  }

  /**
   * Returns a field template's roles and field counts. A contract template id
   * throws {@code TEMPLATE_NOT_FOUND}: the two are different kinds. GET, safe
   * to auto-retry.
   */
  public FieldTemplateDetail get(UUID id) {
    return Http.unwrapRetryableGet(
        () -> templatesApi.apiV1FieldTemplatesIdGet(id),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        retryConfig);
  }

  /** Same as {@link #previewLayout(UUID, List, String)} with the template's own anchor-miss setting. */
  public FieldLayoutPreview previewLayout(UUID id, List<FileInput> files) {
    return previewLayout(id, files, null);
  }

  /**
   * Dry run: tries the field template's layout on a PDF without creating
   * anything or spending credit, and reports resolved fields and unresolved
   * anchors separately. The cheapest way to avoid surprises before an upload
   * with {@link UploadDemandParams#fieldTemplateId(UUID)}. Rate limited per
   * user. POST, but side-effect free; never auto-retried.
   *
   * @param files exactly one PDF to try the layout on (the field is a list on the wire)
   * @param onAnchorMiss {@code "block"} or {@code "drop"}, see {@link UploadDemandParams#onAnchorMiss(String)}; {@code null} for the template's setting
   */
  public FieldLayoutPreview previewLayout(UUID id, List<FileInput> files, String onAnchorMiss) {
    List<File> tempFiles = new ArrayList<>();
    try {
      for (FileInput fileInput : files) {
        tempFiles.add(fileInput.toTempFile());
      }
      return Http.unwrap(
          () -> demandsApi.apiV1FieldTemplatesIdPreviewLayoutPost(
              /* id */ id,
              /* files */ tempFiles,
              /* onAnchorMiss */ onAnchorMiss),
          r -> Boolean.TRUE.equals(r.getSuccess()),
          r -> r.getData());
    } finally {
      for (File file : tempFiles) {
        Path parent = file.toPath().getParent();
        try {
          Files.deleteIfExists(file.toPath());
        } catch (IOException ignored) {
          // best effort
        }
        if (parent != null) {
          try {
            Files.deleteIfExists(parent);
          } catch (IOException ignored) {
            // best effort
          }
        }
      }
    }
  }
}
