using TypeSpec.Http;

// M07 — Apps (platform-level; tenant subscribes separately if needed)
@route("/admin/apps")
@tag("admin-apps")
namespace Saas.Identity.Shared.AdminApps {
  // M07.F01.I01 列表
  @get
  op listApps(@query page?: int32, @query pageSize?: int32): Page<App> | ErrorResponse;

  // M07.F01.I02 创建
  @post
  op createApp(@body body: CreateAppRequest): App | ErrorResponse;

  // M07.F01.I03 详情
  @get
  @route("/{appId}")
  op getApp(@path appId: string): App | ErrorResponse;

  // M07.F01.I04 更新
  @patch
  @route("/{appId}")
  op updateApp(@path appId: string, @body body: UpdateAppRequest): App | ErrorResponse;

  // M07.F01.I05 删除
  @delete
  @route("/{appId}")
  op deleteApp(@path appId: string): void | ErrorResponse;

  // M07.F02.I06 启用/停用（status 切换）
  @patch
  @route("/{appId}/status")
  op setAppStatus(
    @path appId: string,
    @body body: { status: AppStatus },
  ): App | ErrorResponse;
}