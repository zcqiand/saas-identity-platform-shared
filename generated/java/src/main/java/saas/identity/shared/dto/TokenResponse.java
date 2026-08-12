package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * TokenResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T12:50:18.473810800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TokenResponse {

  private String accessToken;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String refreshToken;

  private String tokenType;

  private Integer expiresIn;

  private String scope;

  public TokenResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TokenResponse(String accessToken, String tokenType, Integer expiresIn, String scope) {
    this.accessToken = accessToken;
    this.tokenType = tokenType;
    this.expiresIn = expiresIn;
    this.scope = scope;
  }

  public TokenResponse accessToken(String accessToken) {
    this.accessToken = accessToken;
    return this;
  }

  /**
   * Get accessToken
   * @return accessToken
   */
  @NotNull 
  @Schema(name = "accessToken", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("accessToken")
  public String getAccessToken() {
    return accessToken;
  }

  @JsonProperty("accessToken")
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public TokenResponse refreshToken(@Nullable String refreshToken) {
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

  public TokenResponse tokenType(String tokenType) {
    this.tokenType = tokenType;
    return this;
  }

  /**
   * Get tokenType
   * @return tokenType
   */
  @NotNull 
  @Schema(name = "tokenType", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("tokenType")
  public String getTokenType() {
    return tokenType;
  }

  @JsonProperty("tokenType")
  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public TokenResponse expiresIn(Integer expiresIn) {
    this.expiresIn = expiresIn;
    return this;
  }

  /**
   * Get expiresIn
   * @return expiresIn
   */
  @NotNull 
  @Schema(name = "expiresIn", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("expiresIn")
  public Integer getExpiresIn() {
    return expiresIn;
  }

  @JsonProperty("expiresIn")
  public void setExpiresIn(Integer expiresIn) {
    this.expiresIn = expiresIn;
  }

  public TokenResponse scope(String scope) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TokenResponse tokenResponse = (TokenResponse) o;
    return Objects.equals(this.accessToken, tokenResponse.accessToken) &&
        Objects.equals(this.refreshToken, tokenResponse.refreshToken) &&
        Objects.equals(this.tokenType, tokenResponse.tokenType) &&
        Objects.equals(this.expiresIn, tokenResponse.expiresIn) &&
        Objects.equals(this.scope, tokenResponse.scope);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessToken, refreshToken, tokenType, expiresIn, scope);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TokenResponse {\n");
    sb.append("    accessToken: ").append(toIndentedString(accessToken)).append("\n");
    sb.append("    refreshToken: ").append(toIndentedString(refreshToken)).append("\n");
    sb.append("    tokenType: ").append(toIndentedString(tokenType)).append("\n");
    sb.append("    expiresIn: ").append(toIndentedString(expiresIn)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
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

