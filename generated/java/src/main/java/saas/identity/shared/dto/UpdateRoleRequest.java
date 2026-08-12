package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
 * UpdateRoleRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T12:50:18.473810800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class UpdateRoleRequest {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String name;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String description;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> permissionIds = new ArrayList<>();

  public UpdateRoleRequest name(@Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  
  @Schema(name = "name", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public @Nullable String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(@Nullable String name) {
    this.name = name;
  }

  public UpdateRoleRequest description(@Nullable String description) {
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

  public UpdateRoleRequest permissionIds(List<String> permissionIds) {
    this.permissionIds = permissionIds;
    return this;
  }

  public UpdateRoleRequest addPermissionIdsItem(String permissionIdsItem) {
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
  
  @Schema(name = "permissionIds", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
    UpdateRoleRequest updateRoleRequest = (UpdateRoleRequest) o;
    return Objects.equals(this.name, updateRoleRequest.name) &&
        Objects.equals(this.description, updateRoleRequest.description) &&
        Objects.equals(this.permissionIds, updateRoleRequest.permissionIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, permissionIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdateRoleRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

