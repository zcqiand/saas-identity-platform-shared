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
 * TenantAuditExportAuditEvents200Response
 */

@JsonTypeName("TenantAudit_exportAuditEvents_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T12:50:18.473810800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TenantAuditExportAuditEvents200Response {

  private String downloadUrl;

  public TenantAuditExportAuditEvents200Response() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TenantAuditExportAuditEvents200Response(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  public TenantAuditExportAuditEvents200Response downloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
    return this;
  }

  /**
   * Get downloadUrl
   * @return downloadUrl
   */
  @NotNull 
  @Schema(name = "downloadUrl", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("downloadUrl")
  public String getDownloadUrl() {
    return downloadUrl;
  }

  @JsonProperty("downloadUrl")
  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantAuditExportAuditEvents200Response tenantAuditExportAuditEvents200Response = (TenantAuditExportAuditEvents200Response) o;
    return Objects.equals(this.downloadUrl, tenantAuditExportAuditEvents200Response.downloadUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(downloadUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantAuditExportAuditEvents200Response {\n");
    sb.append("    downloadUrl: ").append(toIndentedString(downloadUrl)).append("\n");
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

