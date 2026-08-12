package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
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
 * TokenRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T12:50:18.473810800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TokenRequest {

  /**
   * Gets or Sets grantType
   */
  public enum GrantTypeEnum {
    AUTHORIZATION_CODE("authorization_code"),
    
    REFRESH_TOKEN("refresh_token");

    private final String value;

    GrantTypeEnum(String value) {
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
    public static GrantTypeEnum fromValue(String value) {
      for (GrantTypeEnum b : GrantTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private GrantTypeEnum grantType;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String code;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String refreshToken;

  private UUID clientId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String clientSecret;

  private UUID tenantId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String redirectUri;

  public TokenRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TokenRequest(GrantTypeEnum grantType, UUID clientId, UUID tenantId) {
    this.grantType = grantType;
    this.clientId = clientId;
    this.tenantId = tenantId;
  }

  public TokenRequest grantType(GrantTypeEnum grantType) {
    this.grantType = grantType;
    return this;
  }

  /**
   * Get grantType
   * @return grantType
   */
  @NotNull 
  @Schema(name = "grantType", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("grantType")
  public GrantTypeEnum getGrantType() {
    return grantType;
  }

  @JsonProperty("grantType")
  public void setGrantType(GrantTypeEnum grantType) {
    this.grantType = grantType;
  }

  public TokenRequest code(@Nullable String code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * @return code
   */
  
  @Schema(name = "code", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("code")
  public @Nullable String getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(@Nullable String code) {
    this.code = code;
  }

  public TokenRequest refreshToken(@Nullable String refreshToken) {
    this.refreshToken = refreshToken;
    return this;
  }

  /**
   * Get refreshToken
   * @return refreshToken
   */
  
  @Schema(name = "refreshToken", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("refreshToken")
  public @Nullable String getRefreshToken() {
    return refreshToken;
  }

  @JsonProperty("refreshToken")
  public void setRefreshToken(@Nullable String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public TokenRequest clientId(UUID clientId) {
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

  public TokenRequest clientSecret(@Nullable String clientSecret) {
    this.clientSecret = clientSecret;
    return this;
  }

  /**
   * Get clientSecret
   * @return clientSecret
   */
  
  @Schema(name = "clientSecret", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("clientSecret")
  public @Nullable String getClientSecret() {
    return clientSecret;
  }

  @JsonProperty("clientSecret")
  public void setClientSecret(@Nullable String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public TokenRequest tenantId(UUID tenantId) {
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

  public TokenRequest redirectUri(@Nullable String redirectUri) {
    this.redirectUri = redirectUri;
    return this;
  }

  /**
   * Get redirectUri
   * @return redirectUri
   */
  
  @Schema(name = "redirectUri", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("redirectUri")
  public @Nullable String getRedirectUri() {
    return redirectUri;
  }

  @JsonProperty("redirectUri")
  public void setRedirectUri(@Nullable String redirectUri) {
    this.redirectUri = redirectUri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TokenRequest tokenRequest = (TokenRequest) o;
    return Objects.equals(this.grantType, tokenRequest.grantType) &&
        Objects.equals(this.code, tokenRequest.code) &&
        Objects.equals(this.refreshToken, tokenRequest.refreshToken) &&
        Objects.equals(this.clientId, tokenRequest.clientId) &&
        Objects.equals(this.clientSecret, tokenRequest.clientSecret) &&
        Objects.equals(this.tenantId, tokenRequest.tenantId) &&
        Objects.equals(this.redirectUri, tokenRequest.redirectUri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(grantType, code, refreshToken, clientId, clientSecret, tenantId, redirectUri);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TokenRequest {\n");
    sb.append("    grantType: ").append(toIndentedString(grantType)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    refreshToken: ").append(toIndentedString(refreshToken)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    clientSecret: ").append(toIndentedString(clientSecret)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    redirectUri: ").append(toIndentedString(redirectUri)).append("\n");
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

