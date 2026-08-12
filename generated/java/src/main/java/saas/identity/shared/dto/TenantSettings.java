package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * TenantSettings
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T12:50:18.473810800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TenantSettings {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String themeColor;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String locale;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer maxUsers;

  public TenantSettings themeColor(@Nullable String themeColor) {
    this.themeColor = themeColor;
    return this;
  }

  /**
   * Get themeColor
   * @return themeColor
   */
  
  @Schema(name = "themeColor", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("themeColor")
  public @Nullable String getThemeColor() {
    return themeColor;
  }

  @JsonProperty("themeColor")
  public void setThemeColor(@Nullable String themeColor) {
    this.themeColor = themeColor;
  }

  public TenantSettings locale(@Nullable String locale) {
    this.locale = locale;
    return this;
  }

  /**
   * Get locale
   * @return locale
   */
  
  @Schema(name = "locale", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("locale")
  public @Nullable String getLocale() {
    return locale;
  }

  @JsonProperty("locale")
  public void setLocale(@Nullable String locale) {
    this.locale = locale;
  }

  public TenantSettings maxUsers(@Nullable Integer maxUsers) {
    this.maxUsers = maxUsers;
    return this;
  }

  /**
   * Get maxUsers
   * @return maxUsers
   */
  
  @Schema(name = "maxUsers", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("maxUsers")
  public @Nullable Integer getMaxUsers() {
    return maxUsers;
  }

  @JsonProperty("maxUsers")
  public void setMaxUsers(@Nullable Integer maxUsers) {
    this.maxUsers = maxUsers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantSettings tenantSettings = (TenantSettings) o;
    return Objects.equals(this.themeColor, tenantSettings.themeColor) &&
        Objects.equals(this.locale, tenantSettings.locale) &&
        Objects.equals(this.maxUsers, tenantSettings.maxUsers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(themeColor, locale, maxUsers);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantSettings {\n");
    sb.append("    themeColor: ").append(toIndentedString(themeColor)).append("\n");
    sb.append("    locale: ").append(toIndentedString(locale)).append("\n");
    sb.append("    maxUsers: ").append(toIndentedString(maxUsers)).append("\n");
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

