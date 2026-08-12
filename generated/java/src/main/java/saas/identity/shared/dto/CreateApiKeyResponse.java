package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import saas.identity.shared.dto.ApiKey;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreateApiKeyResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class CreateApiKeyResponse {

  private ApiKey apiKey;

  private String secret;

  public CreateApiKeyResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateApiKeyResponse(ApiKey apiKey, String secret) {
    this.apiKey = apiKey;
    this.secret = secret;
  }

  public CreateApiKeyResponse apiKey(ApiKey apiKey) {
    this.apiKey = apiKey;
    return this;
  }

  /**
   * Get apiKey
   * @return apiKey
   */
  @NotNull @Valid 
  @Schema(name = "apiKey", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("apiKey")
  public ApiKey getApiKey() {
    return apiKey;
  }

  @JsonProperty("apiKey")
  public void setApiKey(ApiKey apiKey) {
    this.apiKey = apiKey;
  }

  public CreateApiKeyResponse secret(String secret) {
    this.secret = secret;
    return this;
  }

  /**
   * Get secret
   * @return secret
   */
  @NotNull @Size(min = 16, max = 256) 
  @Schema(name = "secret", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("secret")
  public String getSecret() {
    return secret;
  }

  @JsonProperty("secret")
  public void setSecret(String secret) {
    this.secret = secret;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateApiKeyResponse createApiKeyResponse = (CreateApiKeyResponse) o;
    return Objects.equals(this.apiKey, createApiKeyResponse.apiKey) &&
        Objects.equals(this.secret, createApiKeyResponse.secret);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiKey, secret);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateApiKeyResponse {\n");
    sb.append("    apiKey: ").append(toIndentedString(apiKey)).append("\n");
    sb.append("    secret: ").append(toIndentedString(secret)).append("\n");
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

