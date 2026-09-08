using TypeSpec.Http;

// M00.F03 — tenant-private roles, always scoped to one OAuth client.
@route("/tenants/{tenantId}/roles")
@tag("tenant-roles")
namespace Saas.Identity.Shared.TenantRoles {
  // M00.F03.I01 list roles
  @get
  op listSysRoles(
    @path tenantId: string,
    @query clientId: string,
    @query page?: int32,
    @query pageSize?: int32,
  ): Page<SysRole> | ErrorResponse;

  // M00.F03.I02 create role
  @post
  op createSysRole(@path tenantId: string, @body body: CreateSysRoleRequest): SysRole | ErrorResponse;

  // M00.F03.I03 detail
  @get
  @route("/{roleId}")
  op getSysRole(@path tenantId: string, @path roleId: string): SysRole | ErrorResponse;

  // M00.F03.I04 update role
  @patch
  @route("/{roleId}")
  op updateSysRole(
    @path tenantId: string,
    @path roleId: string,
    @body body: UpdateSysRoleRequest,
  ): SysRole | ErrorResponse;

  // M00.F03.I05 delete role
  @delete
  @route("/{roleId}")
  op deleteSysRole(@path tenantId: string, @path roleId: string): void | ErrorResponse;

  // M00.F04.I01 permission matrix binding; menu grants live under M00.F04.I02-I04
}
