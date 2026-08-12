using TypeSpec.Http;

// Role — tenant-scoped. Always carries tenantId.
namespace Saas.Identity.Shared;

model Role {
  @format("uuid") id: string;

  @format("uuid") tenantId: string;

  @minLength(1)
  @maxLength(64)
  code: string;

  @minLength(1)
  @maxLength(255)
  name: string;

  description?: string;

  permissionIds: string[];

  createdAt: utcDateTime;

  updatedAt: utcDateTime;
}

model CreateRoleRequest {
  @minLength(1)
  @maxLength(64)
  code: string;

  @minLength(1)
  @maxLength(255)
  name: string;

  description?: string;

  permissionIds?: string[];
}

model UpdateRoleRequest {
  name?: string;

  description?: string;

  permissionIds?: string[];
}