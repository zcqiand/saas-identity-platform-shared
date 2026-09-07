using TypeSpec.Http;

// M01 — tenant membership management. Role writes target tenant_member.
@route("/tenants/{tenantId}/users")
@tag("tenant-users")
namespace Saas.Identity.Shared.TenantMembers {
  // M01.F01.I01 list members
  @get
  op listTenantUsers(
    @path tenantId: string,
    @query page?: int32,
    @query pageSize?: int32,
    @query status?: TenantMemberStatus,
  ): Page<TenantMemberView> | ErrorResponse;

  // M01.F01.I02 create global sys_user and tenant_member
  @post
  op createTenantUser(@path tenantId: string, @body body: CreateSysUserRequest): TenantMemberView | ErrorResponse;

  // M01.F01.I03 member detail
  @get
  @route("/{userId}")
  op getTenantUser(@path tenantId: string, @path userId: string): TenantMemberView | ErrorResponse;

  // M01.F01.I04 update global sys_user/member fields
  @patch
  @route("/{userId}")
  op updateTenantUser(
    @path tenantId: string,
    @path userId: string,
    @body body: UpdateSysUserRequest,
  ): TenantMemberView | ErrorResponse;

  // M01.F01.I05 remove tenant member; the global sys_user remains
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
  ): TenantMemberView | ErrorResponse;

  // M01.F02.I02 invite a global account into a tenant
  @post
  @route("/invitations")
  op inviteTenantUser(
    @path tenantId: string,
    @body body: { email?: string; mobile?: string },
  ): TenantMemberView | ErrorResponse;

  // M01.F02.I03 member status
  @patch
  @route("/{userId}/status")
  op changeTenantUserStatus(
    @path tenantId: string,
    @path userId: string,
    @body body: { status: TenantMemberStatus },
  ): TenantMemberView | ErrorResponse;
}
