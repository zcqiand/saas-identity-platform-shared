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

model LoginResponse {
  user: SysUser;

  availableTenants: TenantMember[];

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
  // M03.F01.I01 — global account password authentication
  @post
  @route("/login")
  op login(@body body: LoginRequest): LoginResponse | ErrorResponse;

  // M03.F03.I05 — local session/token cleanup
  @post
  @route("/logout")
  op logout(): void;

  // M03.F02.I03 — authorization code callback
  @post
  @route("/oidc/callback")
  op oidcCallback(@body body: OidcCallbackRequest): TokenResponse | ErrorResponse;

  // M03.F02.I04 — refresh token rotation
  @post
  @route("/refresh")
  op refreshToken(@body body: TokenRequest): TokenResponse | ErrorResponse;
}
