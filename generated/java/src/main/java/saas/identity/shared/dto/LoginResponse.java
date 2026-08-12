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
 * LoginResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class LoginResponse {

  private String accessToken;

  private String refreshToken;

  private String tokenType;

  private Integer expiresIn;

  private UUID userId;

  private UUID currentTenantId;

  public LoginResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public LoginResponse(String accessToken, String refreshToken, String tokenType, Integer expiresIn, UUID userId, UUID currentTenantId) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.tokenType = tokenType;
    this.expiresIn = expiresIn;
    this.userId = userId;
    this.currentTenantId = currentTenantId;
  }

  public LoginResponse accessToken(String accessToken) {
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

  public LoginResponse refreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
    return this;
  }

  /**
   * Get refreshToken
   * @return refreshToken
   */
  @NotNull 
  @Schema(name = "refreshToken", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("refreshToken")
  public String getRefreshToken() {
    return refreshToken;
  }

  @JsonProperty("refreshToken")
  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public LoginResponse tokenType(String tokenType) {
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

  public LoginResponse expiresIn(Integer expiresIn) {
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

  public LoginResponse userId(UUID userId) {
    this.userId = userId;
    return this;
  }

  /**
   * Get userId
   * @return userId
   */
  @NotNull @Valid 
  @Schema(name = "userId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("userId")
  public UUID getUserId() {
    return userId;
  }

  @JsonProperty("userId")
  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public LoginResponse currentTenantId(UUID currentTenantId) {
    this.currentTenantId = currentTenantId;
    return this;
  }

  /**
   * Get currentTenantId
   * @return currentTenantId
   */
  @NotNull @Valid 
  @Schema(name = "currentTenantId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("currentTenantId")
  public UUID getCurrentTenantId() {
    return currentTenantId;
  }

  @JsonProperty("currentTenantId")
  public void setCurrentTenantId(UUID currentTenantId) {
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
    LoginResponse loginResponse = (LoginResponse) o;
    return Objects.equals(this.accessToken, loginResponse.accessToken) &&
        Objects.equals(this.refreshToken, loginResponse.refreshToken) &&
        Objects.equals(this.tokenType, loginResponse.tokenType) &&
        Objects.equals(this.expiresIn, loginResponse.expiresIn) &&
        Objects.equals(this.userId, loginResponse.userId) &&
        Objects.equals(this.currentTenantId, loginResponse.currentTenantId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessToken, refreshToken, tokenType, expiresIn, userId, currentTenantId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LoginResponse {\n");
    sb.append("    accessToken: ").append(toIndentedString(accessToken)).append("\n");
    sb.append("    refreshToken: ").append(toIndentedString(refreshToken)).append("\n");
    sb.append("    tokenType: ").append(toIndentedString(tokenType)).append("\n");
    sb.append("    expiresIn: ").append(toIndentedString(expiresIn)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
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

