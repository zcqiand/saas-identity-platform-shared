using TypeSpec.Http;

// M09.F01 / F02 — Role ↔ Menu 授权（tenant-scoped）
@route("/tenants/{tenantId}/roles/{roleId}/menus")
@tag("tenant-role-menus")
namespace Saas.Identity.Shared.TenantRoleMenus {
  // M09.F01.I01 角色已授权菜单 ID 列表
  @get
  op listRoleMenus(
    @path tenantId: string,
    @path roleId: string,
  ): RoleMenuGrant | ErrorResponse;

  // M09.F02.I02 设置角色菜单（整批替换）
  @put
  op setRoleMenus(
    @path tenantId: string,
    @path roleId: string,
    @body body: SetRoleMenusRequest,
  ): RoleMenuGrant | ErrorResponse;

  // M09.F02.I03 清空
  @delete
  op clearRoleMenus(
    @path tenantId: string,
    @path roleId: string,
  ): void | ErrorResponse;
}