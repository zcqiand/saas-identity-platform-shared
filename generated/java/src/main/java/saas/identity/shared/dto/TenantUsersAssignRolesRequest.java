package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * TenantUsersAssignRolesRequest
 */

@JsonTypeName("TenantUsers_assignRoles_request")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TenantUsersAssignRolesRequest {

  private List<String> roleIds = new ArrayList<>();

  public TenantUsersAssignRolesRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TenantUsersAssignRolesRequest(List<String> roleIds) {
    this.roleIds = roleIds;
  }

  public TenantUsersAssignRolesRequest roleIds(List<String> roleIds) {
    this.roleIds = roleIds;
    return this;
  }

  public TenantUsersAssignRolesRequest addRoleIdsItem(String roleIdsItem) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantUsersAssignRolesRequest tenantUsersAssignRolesRequest = (TenantUsersAssignRolesRequest) o;
    return Objects.equals(this.roleIds, tenantUsersAssignRolesRequest.roleIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantUsersAssignRolesRequest {\n");
    sb.append("    roleIds: ").append(toIndentedString(roleIds)).append("\n");
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

