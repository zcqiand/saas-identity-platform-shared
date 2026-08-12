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
 * CreateOAuthAppRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class CreateOAuthAppRequest {

  private String name;

  private List<String> redirectUris = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> scopes = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> grantTypes = new ArrayList<>();

  public CreateOAuthAppRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateOAuthAppRequest(String name, List<String> redirectUris) {
    this.name = name;
    this.redirectUris = redirectUris;
  }

  public CreateOAuthAppRequest name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @NotNull @Size(min = 2, max = 255) 
  @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public CreateOAuthAppRequest redirectUris(List<String> redirectUris) {
    this.redirectUris = redirectUris;
    return this;
  }

  public CreateOAuthAppRequest addRedirectUrisItem(String redirectUrisItem) {
    if (this.redirectUris == null) {
      this.redirectUris = new ArrayList<>();
    }
    this.redirectUris.add(redirectUrisItem);
    return this;
  }

  /**
   * Get redirectUris
   * @return redirectUris
   */
  @NotNull 
  @Schema(name = "redirectUris", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("redirectUris")
  public List<String> getRedirectUris() {
    return redirectUris;
  }

  @JsonProperty("redirectUris")
  public void setRedirectUris(List<String> redirectUris) {
    this.redirectUris = redirectUris;
  }

  public CreateOAuthAppRequest scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  public CreateOAuthAppRequest addScopesItem(String scopesItem) {
    if (this.scopes == null) {
      this.scopes = new ArrayList<>();
    }
    this.scopes.add(scopesItem);
    return this;
  }

  /**
   * Get scopes
   * @return scopes
   */
  
  @Schema(name = "scopes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }

  @JsonProperty("scopes")
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  public CreateOAuthAppRequest grantTypes(List<String> grantTypes) {
    this.grantTypes = grantTypes;
    return this;
  }

  public CreateOAuthAppRequest addGrantTypesItem(String grantTypesItem) {
    if (this.grantTypes == null) {
      this.grantTypes = new ArrayList<>();
    }
    this.grantTypes.add(grantTypesItem);
    return this;
  }

  /**
   * Get grantTypes
   * @return grantTypes
   */
  
  @Schema(name = "grantTypes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("grantTypes")
  public List<String> getGrantTypes() {
    return grantTypes;
  }

  @JsonProperty("grantTypes")
  public void setGrantTypes(List<String> grantTypes) {
    this.grantTypes = grantTypes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateOAuthAppRequest createOAuthAppRequest = (CreateOAuthAppRequest) o;
    return Objects.equals(this.name, createOAuthAppRequest.name) &&
        Objects.equals(this.redirectUris, createOAuthAppRequest.redirectUris) &&
        Objects.equals(this.scopes, createOAuthAppRequest.scopes) &&
        Objects.equals(this.grantTypes, createOAuthAppRequest.grantTypes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, redirectUris, scopes, grantTypes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateOAuthAppRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    redirectUris: ").append(toIndentedString(redirectUris)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    grantTypes: ").append(toIndentedString(grantTypes)).append("\n");
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

