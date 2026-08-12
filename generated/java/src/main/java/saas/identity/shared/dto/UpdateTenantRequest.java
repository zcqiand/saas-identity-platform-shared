package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.lang.Nullable;
import saas.identity.shared.dto.TenantSettings;
import saas.identity.shared.dto.TenantStatus;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * UpdateTenantRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class UpdateTenantRequest {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String name;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String code;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable TenantStatus status;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable TenantSettings settings;

  public UpdateTenantRequest name(@Nullable String name) {
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

  public UpdateTenantRequest code(@Nullable String code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * @return code
   */
  @Size(min = 2, max = 64) 
  @Schema(name = "code", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("code")
  public @Nullable String getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(@Nullable String code) {
    this.code = code;
  }

  public UpdateTenantRequest status(@Nullable TenantStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @Valid 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public @Nullable TenantStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable TenantStatus status) {
    this.status = status;
  }

  public UpdateTenantRequest settings(@Nullable TenantSettings settings) {
    this.settings = settings;
    return this;
  }

  /**
   * Get settings
   * @return settings
   */
  @Valid 
  @Schema(name = "settings", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("settings")
  public @Nullable TenantSettings getSettings() {
    return settings;
  }

  @JsonProperty("settings")
  public void setSettings(@Nullable TenantSettings settings) {
    this.settings = settings;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateTenantRequest updateTenantRequest = (UpdateTenantRequest) o;
    return Objects.equals(this.name, updateTenantRequest.name) &&
        Objects.equals(this.code, updateTenantRequest.code) &&
        Objects.equals(this.status, updateTenantRequest.status) &&
        Objects.equals(this.settings, updateTenantRequest.settings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, code, status, settings);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdateTenantRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    settings: ").append(toIndentedString(settings)).append("\n");
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

