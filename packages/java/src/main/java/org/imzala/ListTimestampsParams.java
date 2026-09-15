package org.imzala;

import java.time.LocalDate;

/**
 * Optional filters and paging for {@link TimestampsResource#list(ListTimestampsParams)}.
 * Every field is optional; pass {@code null} (or use {@code list()}) for the
 * first page with no filters.
 */
public final class ListTimestampsParams {

  private String q;
  private String status;
  private LocalDate from;
  private LocalDate to;
  private Integer page;
  private Integer limit;
  private String sort;

  /** Search in file name and description (max 100 characters). */
  public ListTimestampsParams q(String q) {
    this.q = q;
    return this;
  }

  /** Comma-separated status filter, e.g. {@code "ACTIVE,VERIFIED"} (ACTIVE, VERIFIED, EXPIRED, INVALID). */
  public ListTimestampsParams status(String status) {
    this.status = status;
    return this;
  }

  /** Lower bound on the stamp day. */
  public ListTimestampsParams from(LocalDate from) {
    this.from = from;
    return this;
  }

  /** Upper bound on the stamp day. */
  public ListTimestampsParams to(LocalDate to) {
    this.to = to;
    return this;
  }

  public ListTimestampsParams page(Integer page) {
    this.page = page;
    return this;
  }

  public ListTimestampsParams limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  /** One of createdAt, timestamp_date, original_file_name, original_file_size, status; a {@code -} prefix sorts descending. Server default {@code -createdAt}. */
  public ListTimestampsParams sort(String sort) {
    this.sort = sort;
    return this;
  }

  public String getQ() {
    return q;
  }

  public String getStatus() {
    return status;
  }

  public LocalDate getFrom() {
    return from;
  }

  public LocalDate getTo() {
    return to;
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
}
