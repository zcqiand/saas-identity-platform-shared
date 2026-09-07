using TypeSpec.Http;

// tenant_application — tenant subscription to an OAuth client.
// TS model: TenantApplication ↔ DB table: public.tenant_application (ADR-0025 schema-first)
namespace Saas.Identity.Shared;

model TenantApplication {
  @format("uuid") id: string;

  @format("uuid") tenantId: string;

  clientId: string;

  status: int32;

  expireTime?: utcDateTime;

  createdAt: utcDateTime;
}

model SubscribeTenantApplicationRequest {
  clientId: string;

  expireTime?: utcDateTime;
}

model UpdateTenantApplicationRequest {
  status: int32;

  expireTime?: utcDateTime;
}
