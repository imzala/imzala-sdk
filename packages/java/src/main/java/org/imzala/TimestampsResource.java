package org.imzala;

import org.imzala.client.generated.api.TimestampsApi;
import org.imzala.client.generated.model.ApiV1TimestampsGet200ResponseData;
import org.imzala.client.generated.model.TimestampListItem;
import org.imzala.client.generated.model.TimestampRecord;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** {@code imzala.timestamps()} — backed by the vendored generated {@code TimestampsApi}. */
public final class TimestampsResource {

  private final TimestampsApi api;
  private final RetryConfig retryConfig;

  TimestampsResource(TimestampsApi api) {
    this(api, new RetryConfig(0, 300));
  }

  TimestampsResource(TimestampsApi api, RetryConfig retryConfig) {
    this.api = api;
    this.retryConfig = retryConfig;
  }

  /**
   * RFC 3161-timestamps a file via TÜBİTAK KAMU SM TSA (existence +
   * integrity proof — not a signature; see {@link TimestampRecord} for
   * details). Pass {@link CreateTimestampParams#idempotencyKey} to make
   * retries safe (5-minute window, no duplicate credit spend); with a key,
   * one retry is made after a 429 (waiting Retry-After, at most 60 s).
   * Without a key this is a single attempt.
   *
   * <p>See {@link FileInput} for why this writes {@link
   * CreateTimestampParams#getContent()} to a throwaway temp file — the
   * vendored generated client's multipart layer requires a real {@code
   * java.io.File}. The temp file (and its parent temp directory) is always
   * deleted before this method returns, success or failure.
   */
  public TimestampRecord create(CreateTimestampParams params) {
    FileInput fileInput = new FileInput(params.getContent(), params.getFileName(), params.getContentType());
    File tempFile = fileInput.toTempFile();
    try {
      return Http.unwrapIdempotentWrite(
          () -> api.apiV1TimestampsPost(
              /* file */ tempFile,
              /* idempotencyKey */ params.getIdempotencyKey(),
              /* description */ params.getDescription(),
              /* ownerFirstName */ params.getOwnerFirstName(),
              /* ownerLastName */ params.getOwnerLastName()),
          r -> Boolean.TRUE.equals(r.getSuccess()),
          r -> r.getData(),
          params.getIdempotencyKey(),
          retryConfig);
    } finally {
      cleanup(tempFile);
    }
  }

  /** Lists your timestamp records (first page, no filters). GET, safe to auto-retry. */
  public ApiV1TimestampsGet200ResponseData list() {
    return list(null);
  }

  /** Lists your timestamp records (one page). GET, safe to auto-retry. */
  public ApiV1TimestampsGet200ResponseData list(ListTimestampsParams params) {
    ListTimestampsParams p = params != null ? params : new ListTimestampsParams();
    return Http.unwrapRetryableGet(
        () -> api.apiV1TimestampsGet(
            /* page */ p.getPage(),
            /* limit */ p.getLimit(),
            /* q */ p.getQ(),
            /* status */ p.getStatus(),
            /* from */ p.getFrom(),
            /* to */ p.getTo(),
            /* sort */ p.getSort()),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        retryConfig);
  }

  /** Returns one timestamp record. GET, safe to auto-retry. */
  public TimestampListItem get(UUID id) {
    return Http.unwrapRetryableGet(
        () -> api.apiV1TimestampsIdGet(id),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData(),
        retryConfig);
  }

  private static void cleanup(File file) {
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
