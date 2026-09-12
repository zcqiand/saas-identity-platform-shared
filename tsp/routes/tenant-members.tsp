using TypeSpec.Http;

// M00.F02 — tenant membership management. Role writes target tenant_member.
// 裁决 1（2026-09-12）：路径参数 {userId} 语义 = sys_user.id（user_id），
// 除 invitations 外响应均为扁平 TenantMemberUserView（顶层 id = user id）。
// 端点路径不动（L2 SSOT 按路径扫源码）。
@route("/tenants/{tenantId}/members")
@tag("tenant-members")
namespace Saas.Identity.Shared.TenantMembers {
  // M00.F02.I01 list members
  @get
  op listTenantUsers(
    @path tenantId: string,
    @query page?: int32,
    @query pageSize?: int32,
    @query status?: TenantMemberStatus,
  ): Page<TenantMemberUserView> | ErrorResponse;

  // M00.F02.I02 create global sys_user and tenant_member
  @post
  op createTenantUser(@path tenantId: string, @body body: CreateSysUserRequest): TenantMemberUserView | ErrorResponse;

  // M00.F02.I03 member detail
  @get
  @route("/{userId}")
  op getTenantUser(@path tenantId: string, @path userId: string): TenantMemberUserView | ErrorResponse;

  // M00.F02.I04 update global sys_user/member fields
  @patch
  @route("/{userId}")
  op updateTenantUser(
    @path tenantId: string,
    @path userId: string,
    @body body: UpdateSysUserRequest,
  ): TenantMemberUserView | ErrorResponse;

  // M00.F02.I05 remove tenant member; the global sys_user remains
  @delete
  @route("/{userId}")
  op deleteTenantUser(@path tenantId: string, @path userId: string): void | ErrorResponse;

  // M01.F02.I01 assign roles through tenant_member_role
  @put
  @route("/{userId}/roles")
  op assignTenantMemberRoles(
    @path tenantId: string,
    @path userId: string,
    @body body: SetTenantMemberRolesRequest,
  ): TenantMemberUserView | ErrorResponse;

  // M00.F02.I06 invite a global account into a tenant
  // 邀请端点维持嵌套 TenantMemberView（oracle 方案 C，不随裁决 1 扁平化）
  @post
  @route("/invitations")
  op inviteTenantUser(
    @path tenantId: string,
    @body body: { email?: string; mobile?: string },
  ): TenantMemberView | ErrorResponse;

  // M00.F02.I08 member status toggle (enable/disable)
  @patch
  @route("/{userId}/status")
  op changeTenantUserStatus(
    @path tenantId: string,
    @path userId: string,
    @body body: { status: TenantMemberStatus },
  ): TenantMemberUserView | ErrorResponse;
}
