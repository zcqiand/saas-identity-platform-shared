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

  // 5.59 D-2 人裁 2026-09-20：双边收紧 —— min8 是真实业务约束（密码强度），
  // max256 与同模型 username 装饰器风格成对（min+maxLength 成对出现），
  // 且与消费仓 nextjs BFF 既有 zod min(8).max(256) 一致。
  @minLength(8)
  @maxLength(256)
  password: string;

  @format("email") email?: string;

  mobile?: string;
}

model UpdateSysUserRequest {
  @format("email") email?: string;

  mobile?: string;

  status?: SysUserStatus;
}
