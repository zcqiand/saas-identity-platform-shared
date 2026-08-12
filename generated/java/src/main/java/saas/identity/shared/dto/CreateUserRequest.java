package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreateUserRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T12:50:18.473810800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class CreateUserRequest {

  private String username;

  private String email;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String displayName;

  private String password;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> roleIds = new ArrayList<>();

  public CreateUserRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateUserRequest(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  public CreateUserRequest username(String username) {
    this.username = username;
    return this;
  }

  /**
   * Get username
   * @return username
   */
  @NotNull @Size(min = 1, max = 64) 
  @Schema(name = "username", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("username")
  public String getUsername() {
    return username;
  }

  @JsonProperty("username")
  public void setUsername(String username) {
    this.username = username;
  }

  public CreateUserRequest email(String email) {
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

  public CreateUserRequest displayName(@Nullable String displayName) {
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

  public CreateUserRequest password(String password) {
    this.password = password;
    return this;
  }

  /**
   * Get password
   * @return password
   */
  @NotNull 
  @Schema(name = "password", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("password")
  public String getPassword() {
    return password;
  }

  @JsonProperty("password")
  public void setPassword(String password) {
    this.password = password;
  }

  public CreateUserRequest roleIds(List<String> roleIds) {
    this.roleIds = roleIds;
    return this;
  }

  public CreateUserRequest addRoleIdsItem(String roleIdsItem) {
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
    CreateUserRequest createUserRequest = (CreateUserRequest) o;
    return Objects.equals(this.username, createUserRequest.username) &&
        Objects.equals(this.email, createUserRequest.email) &&
        Objects.equals(this.displayName, createUserRequest.displayName) &&
        Objects.equals(this.password, createUserRequest.password) &&
        Objects.equals(this.roleIds, createUserRequest.roleIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, email, displayName, password, roleIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateUserRequest {\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
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

