using TypeSpec.Http;

// Membership — User ↔ Tenant 多对多关系。一个用户可在多个租户。
namespace Saas.Identity.Shared;

enum MembershipStatus {
  active,
  invited,
  removed,
}

model TenantMembership {
  @format("uuid") id: string;

  @format("uuid") userId: string;

  @format("uuid") tenantId: string;

  roleIds: string[];

  status: MembershipStatus;

  joinedAt: utcDateTime;
}

model SwitchTenantResponse {
  accessToken: string;

  refreshToken?: string;

  expiresAt: utcDateTime;

  @format("uuid") tenantId: string;
}

model CurrentUser {
  @format("uuid") id: string;

  @format("email")
  email: string;

  displayName?: string;

  memberships: TenantMembership[];

  @format("uuid")
  currentTenantId?: string;
}