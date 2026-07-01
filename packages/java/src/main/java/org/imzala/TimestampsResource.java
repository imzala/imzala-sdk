package org.imzala;

import org.imzala.client.generated.api.TimestampsApi;
import org.imzala.client.generated.model.TimestampRecord;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** {@code imzala.timestamps()} — backed by the vendored generated {@code TimestampsApi}. */
public final class TimestampsResource {

  private final TimestampsApi api;

  TimestampsResource(TimestampsApi api) {
    this.api = api;
  }

  /**
   * RFC 3161-timestamps a file via TÜBİTAK KAMU SM TSA (existence +
   * integrity proof — not a signature; see {@link TimestampRecord} for
   * details). Pass {@link CreateTimestampParams#idempotencyKey} to make
   * retries safe (5-minute window, no duplicate credit spend).
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
      return Http.unwrap(
          () -> api.apiV1TimestampsPost(
              tempFile,
              params.getIdempotencyKey(),
              params.getDescription(),
              params.getOwnerFirstName(),
              params.getOwnerLastName()),
          r -> Boolean.TRUE.equals(r.getSuccess()),
          r -> r.getData());
    } finally {
      cleanup(tempFile);
    }
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
