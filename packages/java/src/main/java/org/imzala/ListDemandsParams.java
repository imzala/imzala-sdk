package org.imzala;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Optional filters/pagination for {@link DemandsResource#list(ListDemandsParams)}
 * — the counts-only demand listing. Every field is optional; leave the
 * whole object out (or pass {@code null}) to list the first page with no
 * filters.
 *
 * <p>Mirrors the {@code ListDemandsParams} interface in the Node facade
 * ({@code packages/node/src/index.ts}). {@code templateId} maps to the
 * {@code template_id} query parameter server-side (handled by the generated
 * client). A fluent builder, matching {@link UploadDemandParams}/{@code
 * CreateTimestampParams}:
 *
 * <pre>{@code
 * imzala.demands().list(
 *     new ListDemandsParams().status("PENDING").templateId(templateId).page(1).limit(20));
 * }</pre>
 */
public final class ListDemandsParams {

  private String status;
  private String q;
  private LocalDate from;
  private LocalDate to;
  private UUID templateId;
  private Integer page;
  private Integer limit;
  private String sort;

  /** Filter by demand status (DRAFT / PENDING / COMPLETED / CANCELLED / EXPIRED). */
  public ListDemandsParams status(String status) {
    this.status = status;
    return this;
  }

  /** Title search. */
  public ListDemandsParams q(String q) {
    this.q = q;
    return this;
  }

  /** Lower bound (inclusive) on creation date. */
  public ListDemandsParams from(LocalDate from) {
    this.from = from;
    return this;
  }

  /** Upper bound (inclusive) on creation date. */
  public ListDemandsParams to(LocalDate to) {
    this.to = to;
    return this;
  }

  /** Only demands created from this template. Sent as {@code template_id}. */
  public ListDemandsParams templateId(UUID templateId) {
    this.templateId = templateId;
    return this;
  }

  public ListDemandsParams page(Integer page) {
    this.page = page;
    return this;
  }

  public ListDemandsParams limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  /** {@code field:direction}, e.g. {@code createdAt:desc}. */
  public ListDemandsParams sort(String sort) {
    this.sort = sort;
    return this;
  }

  public String getStatus() {
    return status;
  }

  public String getQ() {
    return q;
  }

  public LocalDate getFrom() {
    return from;
  }

  public LocalDate getTo() {
    return to;
  }

  public UUID getTemplateId() {
    return templateId;
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
