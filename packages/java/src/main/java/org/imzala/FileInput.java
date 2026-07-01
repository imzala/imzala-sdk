package org.imzala;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Java-native file input for multipart endpoints ({@code
 * DemandsResource.uploadDocument}, {@code TimestampsResource.create}).
 * Java servers hold file bytes as {@code byte[]} (from a servlet {@code
 * Part}, {@code Files.readAllBytes}, an upstream download, ...) — the
 * facade accepts bytes + filename and materializes the vendored generated
 * client's required input itself.
 *
 * <p><b>Why a temp file (the one Java-specific gotcha in this vertical):</b>
 * the vendored generated client (openapi-generator {@code java}/{@code
 * native} — {@code java.net.http.HttpClient}, no extra HTTP client
 * dependency) builds multipart bodies with Apache HttpMime's {@code
 * MultipartEntityBuilder.addBinaryBody(name, java.io.File)} — it demands a
 * real {@code java.io.File} that exists on disk, not a byte array or {@code
 * InputStream} (unlike TS's DOM {@code File}, Python's {@code (filename,
 * bytes)} tuple, or C#'s {@code FileParameter(filename, contentType,
 * Stream)}, all of which accept in-memory bytes directly). {@link
 * #toTempFile()} therefore writes {@link #getContent()} to a throwaway
 * temp file named after {@link #getFileName()} — the 2-arg {@code
 * addBinaryBody(name, file)} overload derives the multipart part's
 * filename from {@code File.getName()}, so the temp file's name on disk
 * has to be the exact filename the server should see. Callers of the
 * facade never touch a {@link File} directly; {@code DemandsResource} and
 * {@code TimestampsResource} create + delete the temp file (in a {@code
 * finally} block) for the duration of a single upload call — see their
 * source for the cleanup.
 *
 * <p>{@link #getContentType()} is best-effort only and NOT sent as the
 * multipart part's actual Content-Type: the 2-arg {@code
 * addBinaryBody(name, file)} overload the generated client uses always
 * sends {@code application/octet-stream} regardless of what's passed here
 * — the server infers processing (PDF vs image vs office doc) from {@link
 * #getFileName()}'s extension either way, same as every other imzala SDK
 * (this field exists for API symmetry with the other language SDKs and for
 * forward compatibility, not because the Java facade currently threads it
 * through to the wire).
 */
public final class FileInput {

  private final byte[] content;
  private final String fileName;
  private final String contentType;

  public FileInput(byte[] content, String fileName) {
    this(content, fileName, null);
  }

  /**
   * @param content raw file bytes
   * @param fileName original filename, including extension (e.g. {@code "sozlesme.pdf"}) — required, the server infers processing from the extension
   * @param contentType best-effort only; see class docs
   */
  public FileInput(byte[] content, String fileName, String contentType) {
    if (content == null) {
      throw new IllegalArgumentException("FileInput: content is required.");
    }
    if (fileName == null || fileName.isEmpty()) {
      throw new IllegalArgumentException("FileInput: fileName is required.");
    }
    this.content = content;
    this.fileName = fileName;
    this.contentType = contentType;
  }

  public byte[] getContent() {
    return content;
  }

  public String getFileName() {
    return fileName;
  }

  public String getContentType() {
    return contentType;
  }

  /**
   * Writes {@link #content} to a fresh temp directory (one per call, to
   * avoid filename collisions between sibling files in the same upload)
   * under a file literally named {@link #fileName}. Caller owns cleanup of
   * both the file and its parent temp directory.
   */
  File toTempFile() {
    try {
      String baseName = Path.of(fileName).getFileName().toString();
      Path dir = Files.createTempDirectory("imzala-upload-");
      Path filePath = dir.resolve(baseName);
      Files.write(filePath, content);
      return filePath.toFile();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
