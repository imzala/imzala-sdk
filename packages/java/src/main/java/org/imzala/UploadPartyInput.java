package org.imzala;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.UUID;

/** One signing party for {@code DemandsResource.uploadDocument}. Email or phone (or both) required per party. */
@JsonPropertyOrder({"first_name", "last_name", "email", "phone", "template_party_id"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class UploadPartyInput {

  @JsonProperty("first_name")
  private final String firstName;

  @JsonProperty("last_name")
  private final String lastName;

  @JsonProperty("email")
  private final String email;

  /** E.164 format (e.g. {@code "+905551234567"}). */
  @JsonProperty("phone")
  private final String phone;

  /**
   * Field template role for this party. Required on every party when {@link
   * UploadDemandParams#fieldTemplateId(UUID)} is set; each role of the
   * template must be mapped exactly once.
   */
  @JsonProperty("template_party_id")
  private final UUID templatePartyId;

  public UploadPartyInput(String firstName, String lastName, String email, String phone) {
    this(firstName, lastName, email, phone, null);
  }

  /** @param templatePartyId field template role, see {@link #getTemplatePartyId()} */
  public UploadPartyInput(String firstName, String lastName, String email, String phone, UUID templatePartyId) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = phone;
    this.templatePartyId = templatePartyId;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  /** Field template role for this party, sent as {@code template_party_id}; {@code null} when not set. */
  public UUID getTemplatePartyId() {
    return templatePartyId;
  }
}
