package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import saas.identity.shared.dto.ApiKeyStatus;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ApiKey
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class ApiKey {

  private UUID id;

  private UUID tenantId;

  private String name;

  private String prefix;

  private ApiKeyStatus status;

  private List<String> scopes = new ArrayList<>();

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime lastUsedAt;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime expiresAt;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime revokedAt;

  public ApiKey() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ApiKey(UUID id, UUID tenantId, String name, String prefix, ApiKeyStatus status, List<String> scopes, OffsetDateTime createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.name = name;
    this.prefix = prefix;
    this.status = status;
    this.scopes = scopes;
    this.createdAt = createdAt;
  }

  public ApiKey id(UUID id) {
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

  public ApiKey tenantId(UUID tenantId) {
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

  public ApiKey name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @NotNull @Size(min = 2, max = 128) 
  @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public ApiKey prefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  /**
   * Get prefix
   * @return prefix
   */
  @NotNull @Size(min = 8, max = 16) 
  @Schema(name = "prefix", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("prefix")
  public String getPrefix() {
    return prefix;
  }

  @JsonProperty("prefix")
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public ApiKey status(ApiKeyStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @NotNull @Valid 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public ApiKeyStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(ApiKeyStatus status) {
    this.status = status;
  }

  public ApiKey scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  public ApiKey addScopesItem(String scopesItem) {
    if (this.scopes == null) {
      this.scopes = new ArrayList<>();
    }
    this.scopes.add(scopesItem);
    return this;
  }

  /**
   * Get scopes
   * @return scopes
   */
  @NotNull 
  @Schema(name = "scopes", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }

  @JsonProperty("scopes")
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  public ApiKey createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
   */
  @NotNull @Valid 
  @Schema(name = "createdAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("createdAt")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public ApiKey lastUsedAt(@Nullable OffsetDateTime lastUsedAt) {
    this.lastUsedAt = lastUsedAt;
    return this;
  }

  /**
   * Get lastUsedAt
   * @return lastUsedAt
   */
  @Valid 
  @Schema(name = "lastUsedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lastUsedAt")
  public @Nullable OffsetDateTime getLastUsedAt() {
    return lastUsedAt;
  }

  @JsonProperty("lastUsedAt")
  public void setLastUsedAt(@Nullable OffsetDateTime lastUsedAt) {
    this.lastUsedAt = lastUsedAt;
  }

  public ApiKey expiresAt(@Nullable OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
    return this;
  }

  /**
   * Get expiresAt
   * @return expiresAt
   */
  @Valid 
  @Schema(name = "expiresAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("expiresAt")
  public @Nullable OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  @JsonProperty("expiresAt")
  public void setExpiresAt(@Nullable OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public ApiKey revokedAt(@Nullable OffsetDateTime revokedAt) {
    this.revokedAt = revokedAt;
    return this;
  }

  /**
   * Get revokedAt
   * @return revokedAt
   */
  @Valid 
  @Schema(name = "revokedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("revokedAt")
  public @Nullable OffsetDateTime getRevokedAt() {
    return revokedAt;
  }

  @JsonProperty("revokedAt")
  public void setRevokedAt(@Nullable OffsetDateTime revokedAt) {
    this.revokedAt = revokedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiKey apiKey = (ApiKey) o;
    return Objects.equals(this.id, apiKey.id) &&
        Objects.equals(this.tenantId, apiKey.tenantId) &&
        Objects.equals(this.name, apiKey.name) &&
        Objects.equals(this.prefix, apiKey.prefix) &&
        Objects.equals(this.status, apiKey.status) &&
        Objects.equals(this.scopes, apiKey.scopes) &&
        Objects.equals(this.createdAt, apiKey.createdAt) &&
        Objects.equals(this.lastUsedAt, apiKey.lastUsedAt) &&
        Objects.equals(this.expiresAt, apiKey.expiresAt) &&
        Objects.equals(this.revokedAt, apiKey.revokedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tenantId, name, prefix, status, scopes, createdAt, lastUsedAt, expiresAt, revokedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiKey {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    prefix: ").append(toIndentedString(prefix)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    lastUsedAt: ").append(toIndentedString(lastUsedAt)).append("\n");
    sb.append("    expiresAt: ").append(toIndentedString(expiresAt)).append("\n");
    sb.append("    revokedAt: ").append(toIndentedString(revokedAt)).append("\n");
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

