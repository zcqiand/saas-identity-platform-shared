using TypeSpec.Http;

// /me routes — current user operations (cross-tenant)
@route("/me")
@tag("me")
namespace Saas.Identity.Shared.Me {
  // M00.F02.I01 列出当前用户的租户成员关系
  @get
  op whoami(): CurrentUser | ErrorResponse;

  // M00.F02.I02 列出当前用户所有租户成员关系
  @get
  @route("/tenants")
  op listMyTenants(): TenantMembership[] | ErrorResponse;

  // M00.F02.I03 切换当前租户，返回新的 tenant-scoped token
  @post
  @route("/tenants/{tenantId}/switch")
  op switchTenant(@path tenantId: string): SwitchTenantResponse | ErrorResponse;
}