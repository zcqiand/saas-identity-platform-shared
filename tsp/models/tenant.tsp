using TypeSpec.Http;

// tenant — organization boundary for runtime data and authorization.
// TS model: Tenant ↔ DB table: public.tenant (ADR-0025 schema-first)
namespace Saas.Identity.Shared;

enum TenantStatus {
  active,
  suspended,
}

model Tenant {
  @format("uuid") id: string;

  @minLength(2)
  @maxLength(64)
  tenantKey: string;

  @minLength(2)
  @maxLength(128)
  name: string;

  status: TenantStatus;

  createdAt: utcDateTime;

  updatedAt: utcDateTime;
}

model CreateTenantRequest {
  @minLength(2)
  @maxLength(64)
  tenantKey: string;

  @minLength(2)
  @maxLength(128)
  name: string;
}

model UpdateTenantRequest {
  @minLength(2)
  @maxLength(128)
  name?: string;

  status?: TenantStatus;
}
