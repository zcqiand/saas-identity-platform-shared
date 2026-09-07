using TypeSpec.Http;

// sys_role — tenant-private role definition bound to one OAuth client.
// TS model: SysRole ↔ DB table: public.sys_role (ADR-0025 schema-first)
namespace Saas.Identity.Shared;

model SysRole {
  @format("uuid") id: string;

  @format("uuid") tenantId: string;

  clientId: string;

  @minLength(1)
  @maxLength(64)
  roleCode: string;

  @minLength(1)
  @maxLength(64)
  roleName: string;

  description?: string;

  isPreset: boolean;

  status: int32;

  createdAt: utcDateTime;

  updatedAt: utcDateTime;
}

model CreateSysRoleRequest {
  clientId: string;

  @minLength(1)
  @maxLength(64)
  roleCode: string;

  @minLength(1)
  @maxLength(64)
  roleName: string;

  description?: string;

  isPreset?: boolean;
}

model UpdateSysRoleRequest {
  @minLength(1)
  @maxLength(64)
  roleName?: string;

  description?: string;

  status?: int32;
}
