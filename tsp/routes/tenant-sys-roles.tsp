using TypeSpec.Http;

// M02 — tenant-private roles, always scoped to one OAuth client.
@route("/tenants/{tenantId}/roles")
@tag("tenant-sys-roles")
namespace Saas.Identity.Shared.TenantSysRoles {
  // M02.F01.I01 list roles
  @get
  op listSysRoles(
    @path tenantId: string,
    @query clientId: string,
    @query page?: int32,
    @query pageSize?: int32,
  ): Page<SysRole> | ErrorResponse;

  // M02.F01.I02 create role
  @post
  op createSysRole(@path tenantId: string, @body body: CreateSysRoleRequest): SysRole | ErrorResponse;

  // M02.F01.I03 detail
  @get
  @route("/{roleId}")
  op getSysRole(@path tenantId: string, @path roleId: string): SysRole | ErrorResponse;

  // M02.F01.I04 update role
  @patch
  @route("/{roleId}")
  op updateSysRole(
    @path tenantId: string,
    @path roleId: string,
    @body body: UpdateSysRoleRequest,
  ): SysRole | ErrorResponse;

  // M02.F01.I05 delete role
  @delete
  @route("/{roleId}")
  op deleteSysRole(@path tenantId: string, @path roleId: string): void | ErrorResponse;

  // M02.F02.I01 role menu grants are maintained by the dedicated menu route
}
