package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.lang.Nullable;
import saas.identity.shared.dto.UserStatus;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * UpdateUserRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class UpdateUserRequest {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String displayName;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String email;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable UserStatus status;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> roleIds = new ArrayList<>();

  public UpdateUserRequest displayName(@Nullable String displayName) {
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

  public UpdateUserRequest email(@Nullable String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
   */
  
  @Schema(name = "email", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("email")
  public @Nullable String getEmail() {
    return email;
  }

  @JsonProperty("email")
  public void setEmail(@Nullable String email) {
    this.email = email;
  }

  public UpdateUserRequest status(@Nullable UserStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @Valid 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public @Nullable UserStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable UserStatus status) {
    this.status = status;
  }

  public UpdateUserRequest roleIds(List<String> roleIds) {
    this.roleIds = roleIds;
    return this;
  }

  public UpdateUserRequest addRoleIdsItem(String roleIdsItem) {
    if (this.roleIds == null) {
      this.roleIds = new ArrayList<>();
    }
    this.roleIds.add(roleIdsItem);
    return this;
  }

  /**
   * Get roleIds
   * @return roleIds
   */
  
  @Schema(name = "roleIds", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("roleIds")
  public List<String> getRoleIds() {
    return roleIds;
  }

  @JsonProperty("roleIds")
  public void setRoleIds(List<String> roleIds) {
    this.roleIds = roleIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateUserRequest updateUserRequest = (UpdateUserRequest) o;
    return Objects.equals(this.displayName, updateUserRequest.displayName) &&
        Objects.equals(this.email, updateUserRequest.email) &&
        Objects.equals(this.status, updateUserRequest.status) &&
        Objects.equals(this.roleIds, updateUserRequest.roleIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(displayName, email, status, roleIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdateUserRequest {\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    roleIds: ").append(toIndentedString(roleIds)).append("\n");
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

