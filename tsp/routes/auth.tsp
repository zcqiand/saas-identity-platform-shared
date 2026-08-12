using TypeSpec.Http;

// Auth routes — pre-tenant (no JWT tenant_id claim required)
// M03.F01 密码登录
model LoginRequest {
  @minLength(1)
  @maxLength(64)
  username: string;

  @minLength(1)
  @maxLength(128)
  password: string;

  @format("uuid")
  tenantCode?: string;
}

model LoginResponse {
  accessToken: string;

  refreshToken: string;

  tokenType: string;

  expiresIn: int32;

  @format("uuid") userId: string;

  @format("uuid") currentTenantId: string;
}

// M03.F02 OIDC 回调
model OidcCallbackRequest {
  code: string;

  state: string;

  @format("uuid") clientId: string;
}

@route("/auth")
@tag("auth")
namespace Saas.Identity.Shared.Auth {
  // M03.F01.I01 — 账号密码登录
  @post
  @route("/login")
  op login(@body body: LoginRequest): LoginResponse | ErrorResponse;

  // M03.F03.I05 — 登出（本地清理）
  @post
  @route("/logout")
  op logout(): void;

  // M03.F02.I03 — OIDC Code 换取
  @post
  @route("/oidc/callback")
  op oidcCallback(@body body: OidcCallbackRequest): TokenResponse | ErrorResponse;

  // M03.F02.I04 — refresh token
  @post
  @route("/refresh")
  op refreshToken(@body body: TokenRequest): TokenResponse | ErrorResponse;
}