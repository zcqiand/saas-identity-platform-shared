using TypeSpec.Http;

// M09 — sys_role_menu grants scoped by tenant and client.
@route("/tenants/{tenantId}/roles/{roleId}/menus")
@tag("tenant-role-menus")
namespace Saas.Identity.Shared.TenantRoleMenus {
  // M09.F01.I01 role menu grants
  @get
  op listSysRoleMenus(
    @path tenantId: string,
    @path roleId: string,
    @query clientId: string,
  ): SysRoleMenu[] | ErrorResponse;

  // M09.F02.I01 set role menus (relational rows, idempotent replacement)
  @put
  op setSysRoleMenus(
    @path tenantId: string,
    @path roleId: string,
    @query clientId: string,
    @body body: SetSysRoleMenusRequest,
  ): SysRoleMenu[] | ErrorResponse;

  // M09.F02.I03 clear role menus
  @delete
  op clearSysRoleMenus(
    @path tenantId: string,
    @path roleId: string,
    @query clientId: string,
  ): void | ErrorResponse;
}
