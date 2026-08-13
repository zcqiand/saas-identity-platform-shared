using TypeSpec.Http;

// OAuth endpoints — 平台级标准路径（独立于 /admin/apps）。
// authorize/token 都按 clientId 找到对应 App 记录，校验 redirect_uri / scope / grant_type。
@route("/oauth")
@tag("oauth")
namespace Saas.Identity.Shared.OAuth {
  // M04.F02.I07 授权码签发
  @post
  @route("/authorize")
  op authorize(@body body: AuthorizeCodeRequest): { code: string; state: string } | ErrorResponse;

  // M04.F02.I08 令牌交换 + M04.F02.I09 令牌刷新（按 grantType 字段路由）
  @post
  @route("/token")
  op token(@body body: TokenRequest): TokenResponse | ErrorResponse;
}

// === DTOs for OAuth endpoints (kept local to avoid polluting global namespace) ===
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