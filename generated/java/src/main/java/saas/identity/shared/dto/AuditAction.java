package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonValue;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets AuditAction
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public enum AuditAction {
  
  USER_CREATED("user_created"),
  
  USER_UPDATED("user_updated"),
  
  USER_DELETED("user_deleted"),
  
  ROLE_ASSIGNED("role_assigned"),
  
  ROLE_REVOKED("role_revoked"),
  
  LOGIN_SUCCESS("login_success"),
  
  LOGIN_FAILED("login_failed"),
  
  OAUTH_TOKEN_ISSUED("oauth_token_issued"),
  
  API_KEY_CREATED("api_key_created"),
  
  API_KEY_REVOKED("api_key_revoked");

  private final String value;

  AuditAction(String value) {
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
  public static AuditAction fromValue(String value) {
    for (AuditAction b : AuditAction.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

