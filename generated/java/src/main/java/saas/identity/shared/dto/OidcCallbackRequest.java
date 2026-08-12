package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
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
 * OidcCallbackRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T12:50:18.473810800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class OidcCallbackRequest {

  private String code;

  private String state;

  private UUID clientId;

  public OidcCallbackRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public OidcCallbackRequest(String code, String state, UUID clientId) {
    this.code = code;
    this.state = state;
    this.clientId = clientId;
  }

  public OidcCallbackRequest code(String code) {
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

  public OidcCallbackRequest state(String state) {
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

  public OidcCallbackRequest clientId(UUID clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * Get clientId
   * @return clientId
   */
  @NotNull @Valid 
  @Schema(name = "clientId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("clientId")
  public UUID getClientId() {
    return clientId;
  }

  @JsonProperty("clientId")
  public void setClientId(UUID clientId) {
    this.clientId = clientId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OidcCallbackRequest oidcCallbackRequest = (OidcCallbackRequest) o;
    return Objects.equals(this.code, oidcCallbackRequest.code) &&
        Objects.equals(this.state, oidcCallbackRequest.state) &&
        Objects.equals(this.clientId, oidcCallbackRequest.clientId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, state, clientId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OidcCallbackRequest {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
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

