package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.lang.Nullable;
import saas.identity.shared.dto.TenantMembership;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CurrentUser
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class CurrentUser {

  private UUID id;

  private String email;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String displayName;

  private List<@Valid TenantMembership> memberships = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable UUID currentTenantId;

  public CurrentUser() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CurrentUser(UUID id, String email, List<@Valid TenantMembership> memberships) {
    this.id = id;
    this.email = email;
    this.memberships = memberships;
  }

  public CurrentUser id(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  @NotNull @Valid 
  @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(UUID id) {
    this.id = id;
  }

  public CurrentUser email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
   */
  @NotNull @jakarta.validation.constraints.Email 
  @Schema(name = "email", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  @JsonProperty("email")
  public void setEmail(String email) {
    this.email = email;
  }

  public CurrentUser displayName(@Nullable String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * Get displayName
   * @return displayName
   */
  
  @Schema(name = "displayName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("displayName")
  public @Nullable String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(@Nullable String displayName) {
    this.displayName = displayName;
  }

  public CurrentUser memberships(List<@Valid TenantMembership> memberships) {
    this.memberships = memberships;
    return this;
  }

  public CurrentUser addMembershipsItem(TenantMembership membershipsItem) {
    if (this.memberships == null) {
      this.memberships = new ArrayList<>();
    }
    this.memberships.add(membershipsItem);
    return this;
  }

  /**
   * Get memberships
   * @return memberships
   */
  @NotNull @Valid 
  @Schema(name = "memberships", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("memberships")
  public List<@Valid TenantMembership> getMemberships() {
    return memberships;
  }

  @JsonProperty("memberships")
  public void setMemberships(List<@Valid TenantMembership> memberships) {
    this.memberships = memberships;
  }

  public CurrentUser currentTenantId(@Nullable UUID currentTenantId) {
    this.currentTenantId = currentTenantId;
    return this;
  }

  /**
   * Get currentTenantId
   * @return currentTenantId
   */
  @Valid 
  @Schema(name = "currentTenantId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("currentTenantId")
  public @Nullable UUID getCurrentTenantId() {
    return currentTenantId;
  }

  @JsonProperty("currentTenantId")
  public void setCurrentTenantId(@Nullable UUID currentTenantId) {
    this.currentTenantId = currentTenantId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CurrentUser currentUser = (CurrentUser) o;
    return Objects.equals(this.id, currentUser.id) &&
        Objects.equals(this.email, currentUser.email) &&
        Objects.equals(this.displayName, currentUser.displayName) &&
        Objects.equals(this.memberships, currentUser.memberships) &&
        Objects.equals(this.currentTenantId, currentUser.currentTenantId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, email, displayName, memberships, currentTenantId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CurrentUser {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    memberships: ").append(toIndentedString(memberships)).append("\n");
    sb.append("    currentTenantId: ").append(toIndentedString(currentTenantId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(@Nullable Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}

