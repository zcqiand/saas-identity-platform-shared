package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import saas.identity.shared.dto.AuditAction;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * AuditEvent
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class AuditEvent {

  private UUID id;

  private UUID tenantId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable UUID actorUserId;

  private AuditAction action;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable UUID targetUserId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map<String, Object> metadata = new HashMap<>();

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime occurredAt;

  public AuditEvent() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public AuditEvent(UUID id, UUID tenantId, AuditAction action, OffsetDateTime occurredAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.action = action;
    this.occurredAt = occurredAt;
  }

  public AuditEvent id(UUID id) {
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

  public AuditEvent tenantId(UUID tenantId) {
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

  public AuditEvent actorUserId(@Nullable UUID actorUserId) {
    this.actorUserId = actorUserId;
    return this;
  }

  /**
   * Get actorUserId
   * @return actorUserId
   */
  @Valid 
  @Schema(name = "actorUserId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("actorUserId")
  public @Nullable UUID getActorUserId() {
    return actorUserId;
  }

  @JsonProperty("actorUserId")
  public void setActorUserId(@Nullable UUID actorUserId) {
    this.actorUserId = actorUserId;
  }

  public AuditEvent action(AuditAction action) {
    this.action = action;
    return this;
  }

  /**
   * Get action
   * @return action
   */
  @NotNull @Valid 
  @Schema(name = "action", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("action")
  public AuditAction getAction() {
    return action;
  }

  @JsonProperty("action")
  public void setAction(AuditAction action) {
    this.action = action;
  }

  public AuditEvent targetUserId(@Nullable UUID targetUserId) {
    this.targetUserId = targetUserId;
    return this;
  }

  /**
   * Get targetUserId
   * @return targetUserId
   */
  @Valid 
  @Schema(name = "targetUserId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("targetUserId")
  public @Nullable UUID getTargetUserId() {
    return targetUserId;
  }

  @JsonProperty("targetUserId")
  public void setTargetUserId(@Nullable UUID targetUserId) {
    this.targetUserId = targetUserId;
  }

  public AuditEvent metadata(Map<String, Object> metadata) {
    this.metadata = metadata;
    return this;
  }

  public AuditEvent putMetadataItem(String key, Object metadataItem) {
    if (this.metadata == null) {
      this.metadata = new HashMap<>();
    }
    this.metadata.put(key, metadataItem);
    return this;
  }

  /**
   * Get metadata
   * @return metadata
   */
  
  @Schema(name = "metadata", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("metadata")
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  @JsonProperty("metadata")
  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  public AuditEvent occurredAt(OffsetDateTime occurredAt) {
    this.occurredAt = occurredAt;
    return this;
  }

  /**
   * Get occurredAt
   * @return occurredAt
   */
  @NotNull @Valid 
  @Schema(name = "occurredAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("occurredAt")
  public OffsetDateTime getOccurredAt() {
    return occurredAt;
  }

  @JsonProperty("occurredAt")
  public void setOccurredAt(OffsetDateTime occurredAt) {
    this.occurredAt = occurredAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuditEvent auditEvent = (AuditEvent) o;
    return Objects.equals(this.id, auditEvent.id) &&
        Objects.equals(this.tenantId, auditEvent.tenantId) &&
        Objects.equals(this.actorUserId, auditEvent.actorUserId) &&
        Objects.equals(this.action, auditEvent.action) &&
        Objects.equals(this.targetUserId, auditEvent.targetUserId) &&
        Objects.equals(this.metadata, auditEvent.metadata) &&
        Objects.equals(this.occurredAt, auditEvent.occurredAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tenantId, actorUserId, action, targetUserId, metadata, occurredAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuditEvent {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    actorUserId: ").append(toIndentedString(actorUserId)).append("\n");
    sb.append("    action: ").append(toIndentedString(action)).append("\n");
    sb.append("    targetUserId: ").append(toIndentedString(targetUserId)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    occurredAt: ").append(toIndentedString(occurredAt)).append("\n");
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

