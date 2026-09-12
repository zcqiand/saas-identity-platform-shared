using TypeSpec.Http;

// AuthN starts from the global sys_user and uses client_id to discover
// tenant memberships. Tenant context is selected before token issuance.
namespace Saas.Identity.Shared;

model LoginRequest {
  @minLength(1)
  @maxLength(64)
  username: string;

  @minLength(1)
  @maxLength(128)
  password: string;

  clientId: string;
}

// 裁决 1 对齐 oracle（saas-msw :5100 实测 2026-09-12）：
// - availableTenants 行 = TenantMembership（id/userId/tenantId/roleIds/status/joinedAt），
//   不是嵌套 TenantMember；
// - 补顶层 userId 与 currentTenantId（旧契约缺失）；
// - switch 响应不带 clientId（见 SwitchTenantResponse），此处 clientId = 登录所用 client。
model LoginResponse {
  user: SysUser;

  availableTenants: TenantMembership[];

  @format("uuid") userId: string;

  @format("uuid") currentTenantId?: string;

  accessToken?: string;

  refreshToken?: string;

  tokenType?: string;

  expiresIn?: int32;

  clientId: string;
}

model OidcCallbackRequest {
  code: string;

  state: string;

  clientId: string;
}

@route("/auth")
@tag("auth")
namespace Saas.Identity.Shared.Sessions {
  // M01.F04.I01 — global account password authentication
  // M01.F04.I02 — 失败锁定响应走 LockedAccountResponse（HTTP 423）
  @post
  @route("/login")
  op login(@body body: LoginRequest): LoginResponse | LockedAccountResponse | ErrorResponse;

  // M01.F04.I06 — local session/token cleanup
  @post
  @route("/logout")
  op logout(): void;

  // M01.F04.I04 — authorization code callback
  @post
  @route("/oidc/callback")
  op oidcCallback(@body body: OidcCallbackRequest): TokenResponse | ErrorResponse;

  // M01.F04.I05 — refresh token rotation
  @post
  @route("/refresh")
  op refreshToken(@body body: TokenRequest): TokenResponse | ErrorResponse;
}
