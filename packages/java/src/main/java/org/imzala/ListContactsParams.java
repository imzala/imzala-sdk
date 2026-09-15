package org.imzala;

import java.util.UUID;

/**
 * Optional filters and paging for {@link ContactsResource#list(ListContactsParams)}.
 * Every field is optional; pass {@code null} (or use {@code list()}) for the
 * first page with no filters.
 */
public final class ListContactsParams {

  private String q;
  private Integer page;
  private Integer limit;
  private String sort;
  private UUID companyId;
  private Boolean archived;

  /** Search in name, e-mail, phone and identity number (max 100 characters). */
  public ListContactsParams q(String q) {
    this.q = q;
    return this;
  }

  public ListContactsParams page(Integer page) {
    this.page = page;
    return this;
  }

  /** 10 to 100, default 25. */
  public ListContactsParams limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  /** One of createdAt, updatedAt, first_name, last_name, email; a {@code -} prefix sorts descending. Server default {@code -createdAt}. */
  public ListContactsParams sort(String sort) {
    this.sort = sort;
    return this;
  }

  /** Only contacts of this company. */
  public ListContactsParams companyId(UUID companyId) {
    this.companyId = companyId;
    return this;
  }

  /** {@code true} lists only archived contacts; the server default is active contacts. */
  public ListContactsParams archived(Boolean archived) {
    this.archived = archived;
    return this;
  }

  public String getQ() {
    return q;
  }

  public Integer getPage() {
    return page;
  }

  public Integer getLimit() {
    return limit;
  }

  public String getSort() {
    return sort;
  }

  public UUID getCompanyId() {
    return companyId;
  }

  public Boolean getArchived() {
    return archived;
  }
}
