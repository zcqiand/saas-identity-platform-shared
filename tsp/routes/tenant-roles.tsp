using TypeSpec.Http;

// M02 — Roles (tenant-scoped)
@route("/tenants/{tenantId}/roles")
@tag("tenant-roles")
namespace Saas.Identity.Shared.TenantRoles {
  // M02.F01.I01 列表
  @get
  op listRoles(
    @path tenantId: string,
    @query page?: int32,
    @query pageSize?: int32,
  ): Page<Role> | ErrorResponse;

  // M02.F01.I02 创建
  @post
  op createRole(@path tenantId: string, @body body: CreateRoleRequest): Role | ErrorResponse;

  // M02.F01.I03 详情
  @get
  @route("/{roleId}")
  op getRole(@path tenantId: string, @path roleId: string): Role | ErrorResponse;

  // M02.F01.I04 更新
  @patch
  @route("/{roleId}")
  op updateRole(
    @path tenantId: string,
    @path roleId: string,
    @body body: UpdateRoleRequest,
  ): Role | ErrorResponse;

  // M02.F01.I05 删除
  @delete
  @route("/{roleId}")
  op deleteRole(@path tenantId: string, @path roleId: string): void | ErrorResponse;

  // M02.F02.I01 权限绑定（角色↔权限矩阵）
  @put
  @route("/{roleId}/permissions")
  op setPermissions(
    @path tenantId: string,
    @path roleId: string,
    @body body: { permissionIds: string[] },
  ): Role | ErrorResponse;
}