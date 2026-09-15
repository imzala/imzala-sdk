package org.imzala;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.api.DemandsApi;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsDocIdPatchRequest;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsGet200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsOrderPutRequest;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsPost201ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsPostRequest;
import org.imzala.client.generated.model.ApiV1TemplatesIdDelete200ResponseData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * {@code imzala.demands().documents()}: the documents of a multi-document
 * envelope. While multi-document envelopes are not enabled for the account,
 * every method here throws {@code ENVELOPE_MULTI_DOC_DISABLED} (409); a list
 * call never returns an empty list in that case.
 *
 * <p>Document methods spend no credit; credit is charged on {@link
 * DemandsResource#dispatch(UUID)}.
 */
public final class EnvelopeDocumentsResource {

  /** The generated client's mapper (RFC 3339 dates, unknown properties ignored), for reading a replay body back. */
  private static final ObjectMapper JSON = ApiClient.createDefaultObjectMapper();

  private final DemandsApi api;
  private final RetryConfig retryConfig;

  EnvelopeDocumentsResource(DemandsApi api, RetryConfig retryConfig) {
    this.api = api;
    this.retryConfig = retryConfig;
  }

  /** Lists the documents of an envelope, in the default shape. See {@link #list(UUID, String)}. */
  public ApiV1DemandsDemandIdDocumentsGet200ResponseData list(UUID demandId) {
    return list(demandId, null);
  }

  /**
   * Lists the documents of an envelope. GET, safe to auto-retry.
   *
   * @param view {@code "wizard"} returns the full shape ({@code assigned_party_ids}, {@code decision_count}); {@code null} for the default
   */
  public ApiV1DemandsDemandIdDocumentsGet200ResponseData list(UUID demandId, String view) {
    return Http.unwrapRetryableGet(
        () -> api.apiV1DemandsDemandIdDocumentsGet(demandId, view),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        retryConfig);
  }

  /**
   * Adds a document without a file (metadata only); its order is assigned
   * automatically. {@code title} is required; {@code doc_kind}, {@code
   * is_required} and {@code signature_required} are optional. No idempotency
   * key, so never retried. POST.
   */
  public ApiV1DemandsDemandIdDocumentsPost201ResponseData create(UUID demandId, ApiV1DemandsDemandIdDocumentsPostRequest body) {
    return Http.unwrap(
        () -> api.apiV1DemandsDemandIdDocumentsPost(demandId, body),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /**
   * Uploads one file as one document. Call it again for each further
   * document.
   *
   * <p>{@link UploadEnvelopeDocumentParams#getIdempotencyKey()} is required
   * and is sent as the {@code idempotency_key} multipart field (not as a
   * header). It is checked before anything is sent: a missing or empty key,
   * or one with a character outside printable ASCII, throws {@link
   * ImzalaValidationException} (no temp file is written). Because the server
   * keeps the key, one retry is made after a 429 (waiting Retry-After, at
   * most 60 s; the file is sent again). If a document was already uploaded
   * with the same key, the server answers 409 {@code IDEMPOTENT_REPLAY} with
   * that document, and this method returns it as a normal result. Any other
   * 409 is thrown. No credit is spent.
   *
   * <p>The file is written to a throwaway temp file for the generated
   * client's multipart layer (see {@link FileInput}); it is deleted before
   * this method returns, success or failure. POST.
   */
  public ApiV1DemandsDemandIdDocumentsPost201ResponseData upload(UUID demandId, UploadEnvelopeDocumentParams params) {
    String idempotencyKey = params.getIdempotencyKey();
    Http.assertIdempotencyKey(idempotencyKey, "idempotencyKey");
    String isRequired = params.getIsRequired() == null ? null : (params.getIsRequired() ? "true" : "false");

    File tempFile = params.getFile().toTempFile();
    try {
      // Labelled slots: docKind and isRequired share a type, so a swapped
      // argument would still compile. EnvelopeTest pins each slot.
      return Http.unwrapIdempotentWrite(
          () -> api.apiV1DemandsDemandIdDocumentsUploadPost(
              /* demandId */ demandId,
              /* _file */ tempFile,
              /* idempotencyKey */ idempotencyKey,
              /* title */ params.getTitle(),
              /* docKind */ params.getDocKind(),
              /* isRequired */ isRequired),
          r -> Boolean.TRUE.equals(r.getSuccess()),
          r -> r.getData(),
          idempotencyKey,
          retryConfig);
    } catch (ImzalaException err) {
      ApiV1DemandsDemandIdDocumentsPost201ResponseData replayed = replayedDocument(err);
      if (replayed != null) {
        return replayed;
      }
      throw err;
    } finally {
      cleanupTempFile(tempFile);
    }
  }

  /**
   * Updates only the fields you send ({@code title}, {@code doc_kind},
   * {@code is_required}, {@code signature_required}). Never retried. PATCH.
   */
  public ApiV1DemandsDemandIdDocumentsPost201ResponseData update(UUID demandId, UUID docId, ApiV1DemandsDemandIdDocumentsDocIdPatchRequest body) {
    return Http.unwrap(
        () -> api.apiV1DemandsDemandIdDocumentsDocIdPatch(demandId, docId, body),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /**
   * Deletes a document; the last document of an envelope cannot be deleted
   * ({@code CANNOT_DELETE_LAST_DOCUMENT}). Never retried. DELETE.
   */
  public ApiV1TemplatesIdDelete200ResponseData delete(UUID demandId, UUID docId) {
    return Http.unwrap(
        () -> api.apiV1DemandsDemandIdDocumentsDocIdDelete(demandId, docId),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /**
   * Sets the order of all documents. {@code documentIds} must contain
   * exactly the envelope's documents ({@code ORDER_SET_MISMATCH}). Never
   * retried. PUT.
   */
  public ApiV1DemandsDemandIdDocumentsGet200ResponseData reorder(UUID demandId, List<UUID> documentIds) {
    ApiV1DemandsDemandIdDocumentsOrderPutRequest body = new ApiV1DemandsDemandIdDocumentsOrderPutRequest().documentIds(documentIds);
    return Http.unwrap(
        () -> api.apiV1DemandsDemandIdDocumentsOrderPut(demandId, body),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /** Replaces the set of parties assigned to a document. Never retried. PUT. */
  public ApiV1DemandsDemandIdDocumentsPost201ResponseData setAssignments(UUID demandId, UUID docId, List<UUID> partyIds) {
    ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest body = new ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest().partyIds(partyIds);
    return Http.unwrap(
        () -> api.apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(demandId, docId, body),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }

  /**
   * A 409 {@code IDEMPOTENT_REPLAY} carries the earlier document under
   * {@code data.document}. The spec declares no schema for that status, so
   * the generated client throws and {@link ImzalaException#getBody()} holds
   * the raw JSON; it is read back into the same model a 200 would produce.
   */
  private static ApiV1DemandsDemandIdDocumentsPost201ResponseData replayedDocument(ImzalaException err) {
    Integer status = err.getStatusCode();
    if (status == null || status != 409 || !"IDEMPOTENT_REPLAY".equals(err.getCode()) || err.getBody() == null) {
      return null;
    }
    try {
      JsonNode data = JSON.readTree(err.getBody()).get("data");
      if (data == null || !data.isObject() || !data.hasNonNull("document")) {
        return null;
      }
      ApiV1DemandsDemandIdDocumentsPost201ResponseData replayed = JSON.treeToValue(data, ApiV1DemandsDemandIdDocumentsPost201ResponseData.class);
      return replayed != null && replayed.getDocument() != null ? replayed : null;
    } catch (IOException e) {
      return null;
    }
  }

  private static void cleanupTempFile(File file) {
    Path parent = file.toPath().getParent();
    try {
      Files.deleteIfExists(file.toPath());
    } catch (IOException ignored) {
      // best-effort; the OS temp dir is cleaned up eventually regardless
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
