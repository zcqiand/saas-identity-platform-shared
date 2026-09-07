using TypeSpec.Http;

// sys_user — global natural-person account. Tenant-local identity is TenantMember.
// TS model: SysUser ↔ DB table: public.sys_user (ADR-0025 schema-first)
namespace Saas.Identity.Shared;

enum SysUserStatus {
  active,
  disabled,
}

model SysUser {
  @format("uuid") id: string;

  @minLength(1)
  @maxLength(64)
  username: string;

  @format("email") email?: string;

  mobile?: string;

  status: SysUserStatus;

  createdAt: utcDateTime;

  updatedAt: utcDateTime;
}

model CreateSysUserRequest {
  @minLength(1)
  @maxLength(64)
  username: string;

  password: string;

  @format("email") email?: string;

  mobile?: string;
}

model UpdateSysUserRequest {
  @format("email") email?: string;

  mobile?: string;

  status?: SysUserStatus;
}
