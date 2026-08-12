using TypeSpec.Http;

// Tenant — multi-tenant root entity. Platform-level (admin) manages these.
namespace Saas.Identity.Shared;

enum TenantStatus {
  active,
  suspended,
  archived,
}

model TenantSettings {
  themeColor?: string;
  locale?: string;
  maxUsers?: int32;
}

model Tenant {
  @format("uuid") id: string;

  @minLength(2)
  @maxLength(64)
  @example("acme") code: string;

  @minLength(2)
  @maxLength(255)
  name: string;

  status: TenantStatus;

  settings?: TenantSettings;

  createdAt: utcDateTime;

  updatedAt: utcDateTime;
}

model CreateTenantRequest {
  @minLength(2)
  @maxLength(64)
  code: string;

  @minLength(2)
  @maxLength(255)
  name: string;

  settings?: TenantSettings;
}

model UpdateTenantRequest {
  name?: string;

  @minLength(2)
  @maxLength(64)
  code?: string;

  status?: TenantStatus;

  settings?: TenantSettings;
}