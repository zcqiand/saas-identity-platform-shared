using TypeSpec.Http;

// M01 — Users (tenant-scoped; path carries tenantId, JWT tenant_id claim MUST match)
@route("/tenants/{tenantId}/users")
@tag("tenant-users")
namespace Saas.Identity.Shared.TenantUsers {
  // M01.F01.I01 列表
  @get
  op listUsers(
    @path tenantId: string,
    @query page?: int32,
    @query pageSize?: int32,
    @query status?: UserStatus,
  ): Page<User> | ErrorResponse;

  // M01.F01.I02 创建
  @post
  op createUser(@path tenantId: string, @body body: CreateUserRequest): User | ErrorResponse;

  // M01.F01.I03 详情
  @get
  @route("/{userId}")
  op getUser(@path tenantId: string, @path userId: string): User | ErrorResponse;

  // M01.F01.I04 更新
  @patch
  @route("/{userId}")
  op updateUser(
    @path tenantId: string,
    @path userId: string,
    @body body: UpdateUserRequest,
  ): User | ErrorResponse;

  // M01.F01.I05 删除
  @delete
  @route("/{userId}")
  op deleteUser(@path tenantId: string, @path userId: string): void | ErrorResponse;

  // M01.F02.I01 角色分配
  @put
  @route("/{userId}/roles")
  op assignRoles(
    @path tenantId: string,
    @path userId: string,
    @body body: { roleIds: string[] },
  ): User | ErrorResponse;

  // M01.F02.I02 邀请用户
  @post
  @route("/invitations")
  op inviteUser(
    @path tenantId: string,
    @body body: { email: string; roleIds?: string[] },
  ): User | ErrorResponse;

  // M01.F02.I03 状态切换
  @patch
  @route("/{userId}/status")
  op changeUserStatus(
    @path tenantId: string,
    @path userId: string,
    @body body: { status: UserStatus },
  ): User | ErrorResponse;
}