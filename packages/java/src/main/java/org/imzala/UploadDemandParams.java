package org.imzala;

import java.util.List;
import java.util.UUID;

/** Parameters for {@code DemandsResource.uploadDocument} — creates a demand directly from an uploaded document (no template). */
public final class UploadDemandParams {

  private final List<FileInput> files;
  private final List<UploadPartyInput> parties;
  private List<Integer> order;
  private String title;
  private String description;
  private String idempotencyKey;
  private UUID fieldTemplateId;
  private String onAnchorMiss;
  private String sendInvitations;
  private boolean force;

  /**
   * @param files one document OR 1-20 images — merged server-side into a single PDF
   * @param parties signing parties
   */
  public UploadDemandParams(List<FileInput> files, List<UploadPartyInput> parties) {
    if (files == null || files.isEmpty()) {
      throw new IllegalArgumentException("UploadDemandParams: files is required and must be non-empty.");
    }
    if (parties == null || parties.isEmpty()) {
      throw new IllegalArgumentException("UploadDemandParams: parties is required and must be non-empty.");
    }
    this.files = files;
    this.parties = parties;
  }

  /** Reorders a multi-image upload — indices into {@link #getFiles()}. */
  public UploadDemandParams order(List<Integer> order) {
    this.order = order;
    return this;
  }

  public UploadDemandParams title(String title) {
    this.title = title;
    return this;
  }

  public UploadDemandParams description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Makes the upload safe to retry: a second request with the same key does
   * not create a second demand. With a key, one retry is made after a 429.
   */
  public UploadDemandParams idempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
    return this;
  }

  /**
   * Field template ({@code kind: FIELD_LAYOUT}) whose layout is applied to
   * the upload. The upload must then be a single PDF, and every party needs
   * {@link UploadPartyInput#getTemplatePartyId()}. The layout is resolved
   * before the demand is created or credit is spent; a 422 creates nothing.
   * Dry-run first with {@link FieldTemplatesResource#previewLayout}.
   */
  public UploadDemandParams fieldTemplateId(UUID fieldTemplateId) {
    this.fieldTemplateId = fieldTemplateId;
    return this;
  }

  /**
   * {@code "block"} or {@code "drop"}; only with a field template. Can
   * tighten the template, never relax it: omitted means {@code block};
   * {@code drop} applies only if every affected field is already set to drop
   * in the template (otherwise {@code block} is used and the response carries
   * an {@code ON_ANCHOR_MISS_NOT_RELAXED} warning). Signature fields are never
   * dropped.
   */
  public UploadDemandParams onAnchorMiss(String onAnchorMiss) {
    this.onAnchorMiss = onAnchorMiss;
    return this;
  }

  /**
   * Sends signing invitations in the same request. <b>Off by default on this
   * endpoint.</b> {@code "true"} or {@code "all"} uses every channel, {@code
   * "email"} limits it to e-mail, {@code "sms"} to phone channels (SMS and
   * WhatsApp), {@code "false"} sends nothing. It can only narrow: a channel
   * switched off in the demand's or party's notification settings is not
   * turned back on.
   */
  public UploadDemandParams sendInvitations(String sendInvitations) {
    this.sendInvitations = sendInvitations;
    return this;
  }

  /**
   * Deliberately bypass the duplicate check ({@code DUPLICATE_SUSPECTED}).
   * Only meaningful for calls without an idempotency key.
   */
  public UploadDemandParams force(boolean force) {
    this.force = force;
    return this;
  }

  public List<FileInput> getFiles() {
    return files;
  }

  public List<UploadPartyInput> getParties() {
    return parties;
  }

  public List<Integer> getOrder() {
    return order;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public UUID getFieldTemplateId() {
    return fieldTemplateId;
  }

  public String getOnAnchorMiss() {
    return onAnchorMiss;
  }

  public String getSendInvitations() {
    return sendInvitations;
  }

  public boolean getForce() {
    return force;
  }
}
