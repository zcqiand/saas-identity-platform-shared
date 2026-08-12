using TypeSpec.Http;

// OAuthApp — platform-level. 每个应用全局唯一；租户通过 scope 授权。
namespace Saas.Identity.Shared;

model OAuthApp {
  @format("uuid") id: string;

  @minLength(2)
  @maxLength(128)
  clientId: string;

  @minLength(2)
  @maxLength(255)
  name: string;

  redirectUris: string[];

  scopes: string[];

  grantTypes: string[];

  isFirstParty: boolean;

  createdAt: utcDateTime;
}

model CreateOAuthAppRequest {
  @minLength(2)
  @maxLength(255)
  name: string;

  redirectUris: string[];

  scopes?: string[];

  grantTypes?: string[];
}

model UpdateOAuthAppRequest {
  name?: string;

  redirectUris?: string[];

  scopes?: string[];

  grantTypes?: string[];
}

// Token endpoints
model AuthorizeCodeRequest {
  @format("uuid") clientId: string;

  @minLength(1)
  @maxLength(2048)
  redirectUri: string;

  responseType: "code";

  scope: string;

  state: string;

  @format("uuid") tenantId: string;
}

model TokenRequest {
  grantType: "authorization_code" | "refresh_token";

  code?: string;

  refreshToken?: string;

  @format("uuid") clientId: string;

  clientSecret?: string;

  @format("uuid") tenantId: string;

  redirectUri?: string;
}

model TokenResponse {
  accessToken: string;

  refreshToken?: string;

  tokenType: string;

  expiresIn: int32;

  scope: string;
}