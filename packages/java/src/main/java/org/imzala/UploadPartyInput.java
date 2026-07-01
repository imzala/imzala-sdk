package org.imzala;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** One signing party for {@code DemandsResource.uploadDocument}. Email or phone (or both) required per party. */
@JsonPropertyOrder({"first_name", "last_name", "email", "phone"})
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

  public UploadPartyInput(String firstName, String lastName, String email, String phone) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = phone;
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
}
