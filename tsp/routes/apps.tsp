using TypeSpec.Http;

// /apps - 公共（免鉴权）应用目录。
// 给接入方（如 lab 各前端）按 appCode 取应用展示信息（名称/图标），
// 只暴露展示字段，不泄露 OAuth 集成字段（clientSecret/redirectUris 等）。
@route("/apps")
@tag("apps")
namespace Saas.Identity.Shared.Apps {
  // M04.F01（公共读侧）按 code 查单个应用的公开信息
  @get
  @route("/{code}")
  op getApp(@path code: string): AppPublicInfo | ErrorResponse;
}
