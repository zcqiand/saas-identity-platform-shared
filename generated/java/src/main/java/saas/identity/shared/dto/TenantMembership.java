package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
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
import saas.identity.shared.dto.MembershipStatus;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * TenantMembership
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T12:50:18.473810800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TenantMembership {

  private UUID id;

  private UUID userId;

  private UUID tenantId;

  private List<String> roleIds = new ArrayList<>();

  private MembershipStatus status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime joinedAt;

  public TenantMembership() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TenantMembership(UUID id, UUID userId, UUID tenantId, List<String> roleIds, MembershipStatus status, OffsetDateTime joinedAt) {
    this.id = id;
    this.userId = userId;
    this.tenantId = tenantId;
    this.roleIds = roleIds;
    this.status = status;
    this.joinedAt = joinedAt;
  }

  public TenantMembership id(UUID id) {
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

  public TenantMembership userId(UUID userId) {
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

  public TenantMembership tenantId(UUID tenantId) {
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

  public TenantMembership roleIds(List<String> roleIds) {
    this.roleIds = roleIds;
    return this;
  }

  public TenantMembership addRoleIdsItem(String roleIdsItem) {
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
  @NotNull 
  @Schema(name = "roleIds", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("roleIds")
  public List<String> getRoleIds() {
    return roleIds;
  }

  @JsonProperty("roleIds")
  public void setRoleIds(List<String> roleIds) {
    this.roleIds = roleIds;
  }

  public TenantMembership status(MembershipStatus status) {
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
  public MembershipStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(MembershipStatus status) {
    this.status = status;
  }

  public TenantMembership joinedAt(OffsetDateTime joinedAt) {
    this.joinedAt = joinedAt;
    return this;
  }

  /**
   * Get joinedAt
   * @return joinedAt
   */
  @NotNull @Valid 
  @Schema(name = "joinedAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("joinedAt")
  public OffsetDateTime getJoinedAt() {
    return joinedAt;
  }

  @JsonProperty("joinedAt")
  public void setJoinedAt(OffsetDateTime joinedAt) {
    this.joinedAt = joinedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantMembership tenantMembership = (TenantMembership) o;
    return Objects.equals(this.id, tenantMembership.id) &&
        Objects.equals(this.userId, tenantMembership.userId) &&
        Objects.equals(this.tenantId, tenantMembership.tenantId) &&
        Objects.equals(this.roleIds, tenantMembership.roleIds) &&
        Objects.equals(this.status, tenantMembership.status) &&
        Objects.equals(this.joinedAt, tenantMembership.joinedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, tenantId, roleIds, status, joinedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantMembership {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    roleIds: ").append(toIndentedString(roleIds)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    joinedAt: ").append(toIndentedString(joinedAt)).append("\n");
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

