package org.imzala;

import java.util.List;

/** Parameters for {@code DemandsResource.uploadDocument} — creates a demand directly from an uploaded document (no template). */
public final class UploadDemandParams {

  private final List<FileInput> files;
  private final List<UploadPartyInput> parties;
  private List<Integer> order;
  private String title;
  private String description;

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
}
