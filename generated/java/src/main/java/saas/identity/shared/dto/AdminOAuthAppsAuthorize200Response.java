package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * AdminOAuthAppsAuthorize200Response
 */

@JsonTypeName("AdminOAuthApps_authorize_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T12:50:18.473810800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class AdminOAuthAppsAuthorize200Response {

  private String code;

  private String state;

  public AdminOAuthAppsAuthorize200Response() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public AdminOAuthAppsAuthorize200Response(String code, String state) {
    this.code = code;
    this.state = state;
  }

  public AdminOAuthAppsAuthorize200Response code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * @return code
   */
  @NotNull 
  @Schema(name = "code", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("code")
  public String getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(String code) {
    this.code = code;
  }

  public AdminOAuthAppsAuthorize200Response state(String state) {
    this.state = state;
    return this;
  }

  /**
   * Get state
   * @return state
   */
  @NotNull 
  @Schema(name = "state", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("state")
  public String getState() {
    return state;
  }

  @JsonProperty("state")
  public void setState(String state) {
    this.state = state;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdminOAuthAppsAuthorize200Response adminOAuthAppsAuthorize200Response = (AdminOAuthAppsAuthorize200Response) o;
    return Objects.equals(this.code, adminOAuthAppsAuthorize200Response.code) &&
        Objects.equals(this.state, adminOAuthAppsAuthorize200Response.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, state);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdminOAuthAppsAuthorize200Response {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
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

