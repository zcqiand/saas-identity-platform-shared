package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
 * OAuthApp
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class OAuthApp {

  private UUID id;

  private String clientId;

  private String name;

  private List<String> redirectUris = new ArrayList<>();

  private List<String> scopes = new ArrayList<>();

  private List<String> grantTypes = new ArrayList<>();

  private Boolean isFirstParty;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  public OAuthApp() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public OAuthApp(UUID id, String clientId, String name, List<String> redirectUris, List<String> scopes, List<String> grantTypes, Boolean isFirstParty, OffsetDateTime createdAt) {
    this.id = id;
    this.clientId = clientId;
    this.name = name;
    this.redirectUris = redirectUris;
    this.scopes = scopes;
    this.grantTypes = grantTypes;
    this.isFirstParty = isFirstParty;
    this.createdAt = createdAt;
  }

  public OAuthApp id(UUID id) {
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

  public OAuthApp clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * Get clientId
   * @return clientId
   */
  @NotNull @Size(min = 2, max = 128) 
  @Schema(name = "clientId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("clientId")
  public String getClientId() {
    return clientId;
  }

  @JsonProperty("clientId")
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public OAuthApp name(String name) {
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

  public OAuthApp redirectUris(List<String> redirectUris) {
    this.redirectUris = redirectUris;
    return this;
  }

  public OAuthApp addRedirectUrisItem(String redirectUrisItem) {
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

  public OAuthApp scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  public OAuthApp addScopesItem(String scopesItem) {
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
  @NotNull 
  @Schema(name = "scopes", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }

  @JsonProperty("scopes")
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  public OAuthApp grantTypes(List<String> grantTypes) {
    this.grantTypes = grantTypes;
    return this;
  }

  public OAuthApp addGrantTypesItem(String grantTypesItem) {
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
  @NotNull 
  @Schema(name = "grantTypes", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("grantTypes")
  public List<String> getGrantTypes() {
    return grantTypes;
  }

  @JsonProperty("grantTypes")
  public void setGrantTypes(List<String> grantTypes) {
    this.grantTypes = grantTypes;
  }

  public OAuthApp isFirstParty(Boolean isFirstParty) {
    this.isFirstParty = isFirstParty;
    return this;
  }

  /**
   * Get isFirstParty
   * @return isFirstParty
   */
  @NotNull 
  @Schema(name = "isFirstParty", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("isFirstParty")
  public Boolean getIsFirstParty() {
    return isFirstParty;
  }

  @JsonProperty("isFirstParty")
  public void setIsFirstParty(Boolean isFirstParty) {
    this.isFirstParty = isFirstParty;
  }

  public OAuthApp createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
   */
  @NotNull @Valid 
  @Schema(name = "createdAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("createdAt")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OAuthApp oauthApp = (OAuthApp) o;
    return Objects.equals(this.id, oauthApp.id) &&
        Objects.equals(this.clientId, oauthApp.clientId) &&
        Objects.equals(this.name, oauthApp.name) &&
        Objects.equals(this.redirectUris, oauthApp.redirectUris) &&
        Objects.equals(this.scopes, oauthApp.scopes) &&
        Objects.equals(this.grantTypes, oauthApp.grantTypes) &&
        Objects.equals(this.isFirstParty, oauthApp.isFirstParty) &&
        Objects.equals(this.createdAt, oauthApp.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, clientId, name, redirectUris, scopes, grantTypes, isFirstParty, createdAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OAuthApp {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    redirectUris: ").append(toIndentedString(redirectUris)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    grantTypes: ").append(toIndentedString(grantTypes)).append("\n");
    sb.append("    isFirstParty: ").append(toIndentedString(isFirstParty)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
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

