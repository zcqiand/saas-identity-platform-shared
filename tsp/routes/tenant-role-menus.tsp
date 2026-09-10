using TypeSpec.Http;

// M00.F04 — sys_role_menu grants scoped by tenant and client (legacy M09).
// 2026-09-10 I20 对齐（方案 C）：GET/PUT 返回聚合 RoleMenuGrant（不再返关系行数组）。
@route("/tenants/{tenantId}/roles/{roleId}/menus")
@tag("tenant-role-menus")
namespace Saas.Identity.Shared.TenantRoleMenus {
  // M00.F04.I02 role menu grants
  @get
  op listSysRoleMenus(
    @path tenantId: string,
    @path roleId: string,
    @query clientId: string,
  ): RoleMenuGrant | ErrorResponse;

  // M00.F04.I03 set role menus (relational rows, idempotent replacement)
  @put
  op setSysRoleMenus(
    @path tenantId: string,
    @path roleId: string,
    @query clientId: string,
    @body body: SetSysRoleMenusRequest,
  ): RoleMenuGrant | ErrorResponse;

  // M00.F04.I04 clear role menus
  @delete
  op clearSysRoleMenus(
    @path tenantId: string,
    @path roleId: string,
    @query clientId: string,
  ): void | ErrorResponse;
}
