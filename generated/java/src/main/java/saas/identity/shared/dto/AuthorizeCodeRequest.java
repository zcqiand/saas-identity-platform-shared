package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
 * AuthorizeCodeRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class AuthorizeCodeRequest {

  private UUID clientId;

  private String redirectUri;

  /**
   * Gets or Sets responseType
   */
  public enum ResponseTypeEnum {
    CODE("code");

    private final String value;

    ResponseTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ResponseTypeEnum fromValue(String value) {
      for (ResponseTypeEnum b : ResponseTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private ResponseTypeEnum responseType;

  private String scope;

  private String state;

  private UUID tenantId;

  public AuthorizeCodeRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public AuthorizeCodeRequest(UUID clientId, String redirectUri, ResponseTypeEnum responseType, String scope, String state, UUID tenantId) {
    this.clientId = clientId;
    this.redirectUri = redirectUri;
    this.responseType = responseType;
    this.scope = scope;
    this.state = state;
    this.tenantId = tenantId;
  }

  public AuthorizeCodeRequest clientId(UUID clientId) {
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

  public AuthorizeCodeRequest redirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
    return this;
  }

  /**
   * Get redirectUri
   * @return redirectUri
   */
  @NotNull @Size(min = 1, max = 2048) 
  @Schema(name = "redirectUri", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("redirectUri")
  public String getRedirectUri() {
    return redirectUri;
  }

  @JsonProperty("redirectUri")
  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public AuthorizeCodeRequest responseType(ResponseTypeEnum responseType) {
    this.responseType = responseType;
    return this;
  }

  /**
   * Get responseType
   * @return responseType
   */
  @NotNull 
  @Schema(name = "responseType", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("responseType")
  public ResponseTypeEnum getResponseType() {
    return responseType;
  }

  @JsonProperty("responseType")
  public void setResponseType(ResponseTypeEnum responseType) {
    this.responseType = responseType;
  }

  public AuthorizeCodeRequest scope(String scope) {
    this.scope = scope;
    return this;
  }

  /**
   * Get scope
   * @return scope
   */
  @NotNull 
  @Schema(name = "scope", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("scope")
  public String getScope() {
    return scope;
  }

  @JsonProperty("scope")
  public void setScope(String scope) {
    this.scope = scope;
  }

  public AuthorizeCodeRequest state(String state) {
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

  public AuthorizeCodeRequest tenantId(UUID tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Get tenantId
   * @return tenantId
   */
  @NotNull @Valid 
  @Schema(name = "tenantId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("tenantId")
  public UUID getTenantId() {
    return tenantId;
  }

  @JsonProperty("tenantId")
  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuthorizeCodeRequest authorizeCodeRequest = (AuthorizeCodeRequest) o;
    return Objects.equals(this.clientId, authorizeCodeRequest.clientId) &&
        Objects.equals(this.redirectUri, authorizeCodeRequest.redirectUri) &&
        Objects.equals(this.responseType, authorizeCodeRequest.responseType) &&
        Objects.equals(this.scope, authorizeCodeRequest.scope) &&
        Objects.equals(this.state, authorizeCodeRequest.state) &&
        Objects.equals(this.tenantId, authorizeCodeRequest.tenantId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, redirectUri, responseType, scope, state, tenantId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthorizeCodeRequest {\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    redirectUri: ").append(toIndentedString(redirectUri)).append("\n");
    sb.append("    responseType: ").append(toIndentedString(responseType)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
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

