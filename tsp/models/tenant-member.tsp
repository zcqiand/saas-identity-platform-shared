using TypeSpec.Http;

// tenant_member — the tenant-local identity for a global sys_user.
// TS model: TenantMember ↔ DB table: public.tenant_member (ADR-0025 schema-first)
namespace Saas.Identity.Shared;

// 裁决 2（2026-09-12）：值域扩为 active | invited | suspended | disabled。
// DB tenant_member.status / sys_user.status 是裸 smallint 无 CHECK：
// 1=active, 2=invited, 0/其他=disabled，suspended 放行（drizzle/0000 无需迁移）。
// oracle（saas-msw :5100）实测 /tenants/{t}/members 返回 active|invited|suspended。
enum TenantMemberStatus {
  active,
  invited,
  suspended,
  disabled,
}

// tenant_member 行（DB: public.tenant_member，ADR-0025 schema-first）。
// 仅嵌套视图 TenantMemberView（邀请端点）引用；成员 CRUD 已按裁决 1 改扁平
// TenantMemberUserView，此处不再承载 roleIds/joinedAt。
model TenantMember {
  @format("uuid") id: string;

  @format("uuid") tenantId: string;

  @format("uuid") userId: string;

  memberName?: string;

  isOwner: boolean;

  status: TenantMemberStatus;

  createdAt: utcDateTime;

  updatedAt: utcDateTime;
}

// tenant_memberships 行（DB: tenant_memberships）——login.availableTenants、
// GET /me/tenants、GET /me.memberships 共用的扁平成员关系行。
// oracle 实测键：id/userId/tenantId/roleIds/status/joinedAt（id = membership 行 id）。
model TenantMembership {
  @format("uuid") id: string;

  @format("uuid") userId: string;

  @format("uuid") tenantId: string;

  roleIds: string[];

  status: TenantMemberStatus;

  joinedAt: utcDateTime;
}

// 裁决 1（2026-09-12）：成员 CRUD 统一扁平 User shape，顶层 id = sys_user.id（user_id）。
// oracle 实测（saas-msw :5100）GET/POST/PATCH /tenants/{t}/members[/{userId}]、
// PUT .../roles、PATCH .../status 响应同此 shape；无 memberName/isOwner/joinedAt。
model TenantMemberUserView {
  // 语义 = sys_user.id（user_id），成员寻址主键
  @format("uuid") id: string;

  @format("uuid") tenantId: string;

  @minLength(1)
  @maxLength(64)
  username: string;

  @format("email") email?: string;

  status: TenantMemberStatus;

  roleIds: string[];

  createdAt: utcDateTime;

  updatedAt: utcDateTime;
}

// oracle 实测 POST /me/tenants/{t}/switch 响应：无 clientId（契约旧字段已删）。
model SwitchTenantResponse {
  accessToken: string;

  refreshToken: string;

  expiresAt: utcDateTime;

  @format("uuid") tenantId: string;
}

// 裁决 1：/me 扁平化。oracle 实测 GET /me 返回 {id, email, memberships, currentTenantId}，
// 不再嵌套 user: SysUser；memberships 行 = TenantMembership。
model CurrentUser {
  @format("uuid") id: string;

  @format("email") email?: string;

  memberships: TenantMembership[];

  @format("uuid") currentTenantId?: string;
}

// 邀请端点（POST /tenants/{t}/members/invitations）维持嵌套视图（oracle 方案 C）：
// member = tenant_member 行，user = sys_user 行（邀请态挂 user.status），roles 为空数组。
model TenantMemberView {
  member: TenantMember;

  user: SysUser;

  roles: string[];
}

model SetTenantMemberRolesRequest {
  roleIds: string[];
}
