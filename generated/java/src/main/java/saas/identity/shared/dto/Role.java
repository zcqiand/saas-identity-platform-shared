package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Role
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class Role {

  private UUID id;

  private UUID tenantId;

  private String code;

  private String name;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String description;

  private List<String> permissionIds = new ArrayList<>();

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedAt;

  public Role() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public Role(UUID id, UUID tenantId, String code, String name, List<String> permissionIds, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.code = code;
    this.name = name;
    this.permissionIds = permissionIds;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Role id(UUID id) {
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

  public Role tenantId(UUID tenantId) {
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

  public Role code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * @return code
   */
  @NotNull @Size(min = 1, max = 64) 
  @Schema(name = "code", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("code")
  public String getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(String code) {
    this.code = code;
  }

  public Role name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @NotNull @Size(min = 1, max = 255) 
  @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public Role description(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   * @return description
   */
  
  @Schema(name = "description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public @Nullable String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public Role permissionIds(List<String> permissionIds) {
    this.permissionIds = permissionIds;
    return this;
  }

  public Role addPermissionIdsItem(String permissionIdsItem) {
    if (this.permissionIds == null) {
      this.permissionIds = new ArrayList<>();
    }
    this.permissionIds.add(permissionIdsItem);
    return this;
  }

  /**
   * Get permissionIds
   * @return permissionIds
   */
  @NotNull 
  @Schema(name = "permissionIds", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("permissionIds")
  public List<String> getPermissionIds() {
    return permissionIds;
  }

  @JsonProperty("permissionIds")
  public void setPermissionIds(List<String> permissionIds) {
    this.permissionIds = permissionIds;
  }

  public Role createdAt(OffsetDateTime createdAt) {
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

  public Role updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Get updatedAt
   * @return updatedAt
   */
  @NotNull @Valid 
  @Schema(name = "updatedAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("updatedAt")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  @JsonProperty("updatedAt")
  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Role role = (Role) o;
    return Objects.equals(this.id, role.id) &&
        Objects.equals(this.tenantId, role.tenantId) &&
        Objects.equals(this.code, role.code) &&
        Objects.equals(this.name, role.name) &&
        Objects.equals(this.description, role.description) &&
        Objects.equals(this.permissionIds, role.permissionIds) &&
        Objects.equals(this.createdAt, role.createdAt) &&
        Objects.equals(this.updatedAt, role.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tenantId, code, name, description, permissionIds, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Role {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    permissionIds: ").append(toIndentedString(permissionIds)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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

