using TypeSpec.Http;

// AuditEvent — tenant-scoped.
namespace Saas.Identity.Shared;

enum AuditAction {
  user_created,
  user_updated,
  user_deleted,
  role_assigned,
  role_revoked,
  login_success,
  login_failed,
  oauth_token_issued,
  api_key_created,
  api_key_revoked,
}

model AuditEvent {
  @format("uuid") id: string;

  @format("uuid") tenantId: string;

  @format("uuid") actorUserId?: string;

  action: AuditAction;

  @format("uuid") targetUserId?: string;

  metadata?: Record<unknown>;

  occurredAt: utcDateTime;
}