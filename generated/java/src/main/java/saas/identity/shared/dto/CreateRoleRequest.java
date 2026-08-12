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
 * CreateRoleRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T12:50:18.473810800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class CreateRoleRequest {

  private String code;

  private String name;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String description;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> permissionIds = new ArrayList<>();

  public CreateRoleRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateRoleRequest(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public CreateRoleRequest code(String code) {
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

  public CreateRoleRequest name(String name) {
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

  public CreateRoleRequest description(@Nullable String description) {
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

  public CreateRoleRequest permissionIds(List<String> permissionIds) {
    this.permissionIds = permissionIds;
    return this;
  }

  public CreateRoleRequest addPermissionIdsItem(String permissionIdsItem) {
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
    CreateRoleRequest createRoleRequest = (CreateRoleRequest) o;
    return Objects.equals(this.code, createRoleRequest.code) &&
        Objects.equals(this.name, createRoleRequest.name) &&
        Objects.equals(this.description, createRoleRequest.description) &&
        Objects.equals(this.permissionIds, createRoleRequest.permissionIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, name, description, permissionIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateRoleRequest {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
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

