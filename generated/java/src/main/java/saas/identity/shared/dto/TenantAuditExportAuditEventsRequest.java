package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
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
 * TenantAuditExportAuditEventsRequest
 */

@JsonTypeName("TenantAudit_exportAuditEvents_request")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T12:50:18.473810800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TenantAuditExportAuditEventsRequest {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime from;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime to;

  /**
   * Gets or Sets format
   */
  public enum FormatEnum {
    CSV("csv"),
    
    JSON("json");

    private final String value;

    FormatEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static FormatEnum fromValue(String value) {
      for (FormatEnum b : FormatEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private FormatEnum format;

  public TenantAuditExportAuditEventsRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TenantAuditExportAuditEventsRequest(OffsetDateTime from, OffsetDateTime to, FormatEnum format) {
    this.from = from;
    this.to = to;
    this.format = format;
  }

  public TenantAuditExportAuditEventsRequest from(OffsetDateTime from) {
    this.from = from;
    return this;
  }

  /**
   * Get from
   * @return from
   */
  @NotNull @Valid 
  @Schema(name = "from", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("from")
  public OffsetDateTime getFrom() {
    return from;
  }

  @JsonProperty("from")
  public void setFrom(OffsetDateTime from) {
    this.from = from;
  }

  public TenantAuditExportAuditEventsRequest to(OffsetDateTime to) {
    this.to = to;
    return this;
  }

  /**
   * Get to
   * @return to
   */
  @NotNull @Valid 
  @Schema(name = "to", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("to")
  public OffsetDateTime getTo() {
    return to;
  }

  @JsonProperty("to")
  public void setTo(OffsetDateTime to) {
    this.to = to;
  }

  public TenantAuditExportAuditEventsRequest format(FormatEnum format) {
    this.format = format;
    return this;
  }

  /**
   * Get format
   * @return format
   */
  @NotNull 
  @Schema(name = "format", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("format")
  public FormatEnum getFormat() {
    return format;
  }

  @JsonProperty("format")
  public void setFormat(FormatEnum format) {
    this.format = format;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantAuditExportAuditEventsRequest tenantAuditExportAuditEventsRequest = (TenantAuditExportAuditEventsRequest) o;
    return Objects.equals(this.from, tenantAuditExportAuditEventsRequest.from) &&
        Objects.equals(this.to, tenantAuditExportAuditEventsRequest.to) &&
        Objects.equals(this.format, tenantAuditExportAuditEventsRequest.format);
  }

  @Override
  public int hashCode() {
    return Objects.hash(from, to, format);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantAuditExportAuditEventsRequest {\n");
    sb.append("    from: ").append(toIndentedString(from)).append("\n");
    sb.append("    to: ").append(toIndentedString(to)).append("\n");
    sb.append("    format: ").append(toIndentedString(format)).append("\n");
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

