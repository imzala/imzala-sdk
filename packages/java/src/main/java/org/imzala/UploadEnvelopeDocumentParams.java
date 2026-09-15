package org.imzala;

/** Parameters for {@code EnvelopeDocumentsResource.upload}: one file as one document of a multi-document envelope. */
public final class UploadEnvelopeDocumentParams {

  private final FileInput file;
  private final String title;
  private final String idempotencyKey;
  private String docKind;
  private Boolean isRequired;

  /**
   * @param file exactly one file per document; call {@code upload} again for each further document
   * @param title the document's title
   * @param idempotencyKey required. Sent as the {@code idempotency_key} form
   *     field (not as a header). Printable ASCII only. Uploading again with
   *     the same key creates no new document; the earlier document is
   *     returned instead. Checked by {@code upload} before anything is sent.
   */
  public UploadEnvelopeDocumentParams(FileInput file, String title, String idempotencyKey) {
    if (file == null) {
      throw new IllegalArgumentException("UploadEnvelopeDocumentParams: file is required.");
    }
    if (title == null) {
      throw new IllegalArgumentException("UploadEnvelopeDocumentParams: title is required.");
    }
    this.file = file;
    this.title = title;
    this.idempotencyKey = idempotencyKey;
  }

  /** CONTRACT, KVKK_NOTICE, KVKK_CONSENT, PREINFO, PRICE_LIST or OTHER. Server default: OTHER. */
  public UploadEnvelopeDocumentParams docKind(String docKind) {
    this.docKind = docKind;
    return this;
  }

  /** Server default: {@code true}. */
  public UploadEnvelopeDocumentParams isRequired(boolean isRequired) {
    this.isRequired = isRequired;
    return this;
  }

  public FileInput getFile() {
    return file;
  }

  public String getTitle() {
    return title;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public String getDocKind() {
    return docKind;
  }

  /** {@code null} when not set (server default applies). */
  public Boolean getIsRequired() {
    return isRequired;
  }
}
