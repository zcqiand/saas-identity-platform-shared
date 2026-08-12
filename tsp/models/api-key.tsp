using TypeSpec.Http;

// ApiKey — tenant-scoped.
namespace Saas.Identity.Shared;

enum ApiKeyStatus {
  active,
  revoked,
  expired,
}

model ApiKey {
  @format("uuid") id: string;

  @format("uuid") tenantId: string;

  @minLength(2)
  @maxLength(128)
  name: string;

  @minLength(8)
  @maxLength(16)
  prefix: string;

  status: ApiKeyStatus;

  scopes: string[];

  createdAt: utcDateTime;

  lastUsedAt?: utcDateTime;

  expiresAt?: utcDateTime;

  revokedAt?: utcDateTime;
}

model CreateApiKeyRequest {
  @minLength(2)
  @maxLength(128)
  name: string;

  scopes?: string[];

  expiresAt?: utcDateTime;
}

model CreateApiKeyResponse {
  apiKey: ApiKey;

  // 仅在创建时返回一次明文
  @minLength(16)
  @maxLength(256)
  secret: string;
}