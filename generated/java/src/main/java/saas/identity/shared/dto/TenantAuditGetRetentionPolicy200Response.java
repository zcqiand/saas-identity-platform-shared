package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * TenantAuditGetRetentionPolicy200Response
 */

@JsonTypeName("TenantAudit_getRetentionPolicy_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TenantAuditGetRetentionPolicy200Response {

  private Integer retentionDays;

  public TenantAuditGetRetentionPolicy200Response() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TenantAuditGetRetentionPolicy200Response(Integer retentionDays) {
    this.retentionDays = retentionDays;
  }

  public TenantAuditGetRetentionPolicy200Response retentionDays(Integer retentionDays) {
    this.retentionDays = retentionDays;
    return this;
  }

  /**
   * Get retentionDays
   * @return retentionDays
   */
  @NotNull 
  @Schema(name = "retentionDays", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("retentionDays")
  public Integer getRetentionDays() {
    return retentionDays;
  }

  @JsonProperty("retentionDays")
  public void setRetentionDays(Integer retentionDays) {
    this.retentionDays = retentionDays;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantAuditGetRetentionPolicy200Response tenantAuditGetRetentionPolicy200Response = (TenantAuditGetRetentionPolicy200Response) o;
    return Objects.equals(this.retentionDays, tenantAuditGetRetentionPolicy200Response.retentionDays);
  }

  @Override
  public int hashCode() {
    return Objects.hash(retentionDays);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantAuditGetRetentionPolicy200Response {\n");
    sb.append("    retentionDays: ").append(toIndentedString(retentionDays)).append("\n");
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

