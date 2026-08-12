using TypeSpec.Http;

// M04 — OAuth2 Provider (platform-level; tenant grants scopes via authorize)
@route("/admin/oauth-apps")
@tag("admin-oauth-apps")
namespace Saas.Identity.Shared.AdminOAuthApps {
  // M04.F01.I01 列表
  @get
  op listOAuthApps(@query page?: int32, @query pageSize?: int32): Page<OAuthApp> | ErrorResponse;

  // M04.F01.I02 创建
  @post
  op createOAuthApp(@body body: CreateOAuthAppRequest): OAuthApp | ErrorResponse;

  // M04.F01.I03 详情
  @get
  @route("/{appId}")
  op getOAuthApp(@path appId: string): OAuthApp | ErrorResponse;

  // M04.F01.I04 更新
  @patch
  @route("/{appId}")
  op updateOAuthApp(
    @path appId: string,
    @body body: UpdateOAuthAppRequest,
  ): OAuthApp | ErrorResponse;

  // M04.F01.I05 删除
  @delete
  @route("/{appId}")
  op deleteOAuthApp(@path appId: string): void | ErrorResponse;

  // M04.F02.I01 授权码签发
  @post
  @route("/authorize")
  op authorize(@body body: AuthorizeCodeRequest): { code: string; state: string } | ErrorResponse;

  // M04.F02.I02 令牌交换/刷新
  @post
  @route("/token")
  op token(@body body: TokenRequest): TokenResponse | ErrorResponse;
}