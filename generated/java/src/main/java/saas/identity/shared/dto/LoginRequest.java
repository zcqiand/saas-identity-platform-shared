package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * LoginRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class LoginRequest {

  private String username;

  private String password;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable UUID tenantCode;

  public LoginRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public LoginRequest(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public LoginRequest username(String username) {
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

  public LoginRequest password(String password) {
    this.password = password;
    return this;
  }

  /**
   * Get password
   * @return password
   */
  @NotNull @Size(min = 1, max = 128) 
  @Schema(name = "password", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("password")
  public String getPassword() {
    return password;
  }

  @JsonProperty("password")
  public void setPassword(String password) {
    this.password = password;
  }

  public LoginRequest tenantCode(@Nullable UUID tenantCode) {
    this.tenantCode = tenantCode;
    return this;
  }

  /**
   * Get tenantCode
   * @return tenantCode
   */
  @Valid 
  @Schema(name = "tenantCode", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("tenantCode")
  public @Nullable UUID getTenantCode() {
    return tenantCode;
  }

  @JsonProperty("tenantCode")
  public void setTenantCode(@Nullable UUID tenantCode) {
    this.tenantCode = tenantCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LoginRequest loginRequest = (LoginRequest) o;
    return Objects.equals(this.username, loginRequest.username) &&
        Objects.equals(this.password, loginRequest.password) &&
        Objects.equals(this.tenantCode, loginRequest.tenantCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, password, tenantCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LoginRequest {\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    tenantCode: ").append(toIndentedString(tenantCode)).append("\n");
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

