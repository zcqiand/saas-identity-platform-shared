using TypeSpec.Http;

// sys_user — global natural-person account. Tenant-local identity is TenantMember.
// TS model: SysUser ↔ DB table: public.sys_user (ADR-0025 schema-first)
namespace Saas.Identity.Shared;

enum SysUserStatus {
  active,
  invited,
  disabled,
}

// status DB 值域（家族约定 2026-09-10）：1=active, 2=invited, 0=disabled。
// DB 列是裸 smallint 无 CHECK（drizzle/0000_target_ddl.sql:97），加值无需迁移。
model SysUser {
  @format("uuid") id: string;

  @minLength(1)
  @maxLength(64)
  username: string;

  @format("email") email?: string;

  mobile?: string;

  status: SysUserStatus;

  // M01.F04.I02 — 连续失败锁定（默认 5 次/15min，具体阈值由消费后端决定）
  failedAttempts?: int32;

  lockedUntil?: utcDateTime;

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
