using TypeSpec.Http;

// tenant_member — the tenant-local identity for a global sys_user.
// TS model: TenantMember ↔ DB table: public.tenant_member (ADR-0025 schema-first)
namespace Saas.Identity.Shared;

enum TenantMemberStatus {
  active,
  disabled,
}

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

model SwitchTenantResponse {
  accessToken: string;

  refreshToken: string;

  expiresAt: utcDateTime;

  @format("uuid") tenantId: string;

  clientId: string;
}

model CurrentUser {
  user: SysUser;

  memberships: TenantMember[];

  @format("uuid") currentTenantId?: string;

  clientId?: string;
}

model TenantMemberView {
  member: TenantMember;

  user: SysUser;

  roles: string[];
}

model SetTenantMemberRolesRequest {
  roleIds: string[];
}
