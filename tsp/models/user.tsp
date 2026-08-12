using TypeSpec.Http;

// User — tenant-scoped. Always carries tenantId.
namespace Saas.Identity.Shared;

enum UserStatus {
  active,
  invited,
  suspended,
  disabled,
}

model User {
  @format("uuid") id: string;

  @format("uuid") tenantId: string;

  @minLength(1)
  @maxLength(64)
  username: string;

  @format("email")
  email: string;

  displayName?: string;

  status: UserStatus;

  roleIds: string[];

  createdAt: utcDateTime;

  updatedAt: utcDateTime;
}

model CreateUserRequest {
  @minLength(1)
  @maxLength(64)
  username: string;

  @format("email")
  email: string;

  displayName?: string;

  password: string;

  roleIds?: string[];
}

model UpdateUserRequest {
  displayName?: string;

  email?: string;

  status?: UserStatus;

  roleIds?: string[];
}