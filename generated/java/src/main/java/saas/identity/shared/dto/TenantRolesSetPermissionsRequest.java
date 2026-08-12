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
 * TenantRolesSetPermissionsRequest
 */

@JsonTypeName("TenantRoles_setPermissions_request")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TenantRolesSetPermissionsRequest {

  private List<String> permissionIds = new ArrayList<>();

  public TenantRolesSetPermissionsRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TenantRolesSetPermissionsRequest(List<String> permissionIds) {
    this.permissionIds = permissionIds;
  }

  public TenantRolesSetPermissionsRequest permissionIds(List<String> permissionIds) {
    this.permissionIds = permissionIds;
    return this;
  }

  public TenantRolesSetPermissionsRequest addPermissionIdsItem(String permissionIdsItem) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantRolesSetPermissionsRequest tenantRolesSetPermissionsRequest = (TenantRolesSetPermissionsRequest) o;
    return Objects.equals(this.permissionIds, tenantRolesSetPermissionsRequest.permissionIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(permissionIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantRolesSetPermissionsRequest {\n");
    sb.append("    permissionIds: ").append(toIndentedString(permissionIds)).append("\n");
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

