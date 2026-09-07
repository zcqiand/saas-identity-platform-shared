using TypeSpec.Http;

// OAuth protocol routes. The service resolves the selected tenant from the
// authenticated member and never accepts a synthetic identity fallback.
@route("/oauth")
@tag("oauth")
namespace Saas.Identity.Shared.OAuth {
  // M04.F03.I07 authorization code issuance
  @post
  @route("/authorize")
  op authorize(@body body: AuthorizeCodeRequest): { code: string; state: string } | ErrorResponse;

  // M04.F03.I08 code exchange + M04.F03.I09 refresh rotation
  @post
  @route("/token")
  op token(@body body: TokenRequest): TokenResponse | ErrorResponse;
}

model AuthorizeCodeRequest {
  clientId: string;

  @minLength(1)
  @maxLength(500)
  redirectUri: string;

  responseType: "code";

  scope?: string;

  state: string;
}

model TokenRequest {
  grantType: "authorization_code" | "refresh_token";

  code?: string;

  refreshToken?: string;

  clientId: string;

  clientSecret?: string;

  redirectUri?: string;
}

model TokenResponse {
  accessToken: string;

  refreshToken: string;

  tokenType: string;

  expiresIn: int32;

  scope?: string;

  userId: string;

  clientId: string;

  @format("uuid") tenantId: string;
}

// Storage-shaped contract types are intentionally read-only and are not
// exposed as CRUD endpoints.
//
// TS model: OAuthCode ↔ DB table: public.oauth_code (ADR-0025 schema-first)
// TS model: OAuthAccessToken ↔ DB table: public.oauth_access_token (ADR-0025 schema-first)
// TS model: OAuthRefreshToken ↔ DB table: public.oauth_refresh_token (ADR-0025 schema-first)
model OAuthCode {
  @format("uuid") id: string;

  code: string;

  clientId: string;

  userId: string;

  @format("uuid") tenantId: string;

  expiresAt: utcDateTime;
}

model OAuthAccessToken {
  @format("uuid") id: string;

  tokenId: string;

  clientId: string;

  userId?: string;

  @format("uuid") tenantId?: string;

  expiresAt: utcDateTime;

  revoked: boolean;
}

model OAuthRefreshToken {
  @format("uuid") id: string;

  refreshToken: string;

  @format("uuid") accessTokenId: string;

  clientId: string;

  userId?: string;

  @format("uuid") tenantId?: string;

  expiresAt: utcDateTime;

  revoked: boolean;
}
