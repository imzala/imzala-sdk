package org.imzala;

/** Parameters for {@code TimestampsResource.create}. */
public final class CreateTimestampParams {

  private final byte[] content;
  private final String fileName;
  private String contentType;
  private String idempotencyKey;
  private String description;
  private String ownerFirstName;
  private String ownerLastName;

  public CreateTimestampParams(byte[] content, String fileName) {
    if (content == null) {
      throw new IllegalArgumentException("CreateTimestampParams: content is required.");
    }
    if (fileName == null || fileName.isEmpty()) {
      throw new IllegalArgumentException("CreateTimestampParams: fileName is required.");
    }
    this.content = content;
    this.fileName = fileName;
  }

  public CreateTimestampParams contentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  /** Client-generated idempotency key (UUID recommended) — replays within 5 minutes return the original result without spending a credit. */
  public CreateTimestampParams idempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
    return this;
  }

  public CreateTimestampParams description(String description) {
    this.description = description;
    return this;
  }

  public CreateTimestampParams ownerFirstName(String ownerFirstName) {
    this.ownerFirstName = ownerFirstName;
    return this;
  }

  public CreateTimestampParams ownerLastName(String ownerLastName) {
    this.ownerLastName = ownerLastName;
    return this;
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

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public String getDescription() {
    return description;
  }

  public String getOwnerFirstName() {
    return ownerFirstName;
  }

  public String getOwnerLastName() {
    return ownerLastName;
  }
}
