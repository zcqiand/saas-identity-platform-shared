import "@typespec/http";
import "@typespec/openapi3";
import "@typespec/rest";

// Models
import "./tsp/models/tenant.tsp";
import "./tsp/models/sys-user.tsp";
import "./tsp/models/sys-role.tsp";
import "./tsp/models/tenant-member.tsp";
import "./tsp/models/tenant-member-role.tsp";
import "./tsp/models/oauth-client.tsp";
import "./tsp/models/tenant-application.tsp";
import "./tsp/models/sys-menu.tsp";
import "./tsp/models/sys-role-menu.tsp";

// Routes
import "./tsp/routes/sessions.tsp";
import "./tsp/routes/admin-tenants.tsp";
import "./tsp/routes/me.tsp";
import "./tsp/routes/tenant-members.tsp";
import "./tsp/routes/tenant-roles.tsp";
import "./tsp/routes/admin-clients.tsp";
import "./tsp/routes/clients.tsp";
import "./tsp/routes/client-menus.tsp";
import "./tsp/routes/tenant-role-menus.tsp";
import "./tsp/routes/oauth.tsp";
import "./tsp/routes/tenant-applications.tsp";

using TypeSpec.Http;

// ============================================================
// SaaS Identity Platform — Multi-Tenant TypeSpec SSOT
// ============================================================

@server("https://api.example.com", "Production")
@service
@route("/api/v1")
namespace Saas.Identity.Shared;

// ============================================================
// Common error response
// ============================================================
@error
model ErrorResponse {
  @example("BAD_REQUEST") code: string;
  message: string;
  details?: Record<unknown>;
}

// ============================================================
// M01.F04.I02 — 失败锁定响应（HTTP 423 Locked）
// 连续失败超过阈值时由消费后端返回；lockedUntil 给客户端倒计时展示。
// ============================================================
@error
model LockedAccountResponse {
  @example("ACCOUNT_LOCKED") code: string;
  message: string;
  lockedUntil: utcDateTime;
  remainingAttempts?: int32;
}

// ============================================================
// Pagination wrapper
// ============================================================
model Page<T> {
  items: T[];
  @example(0) page: int32;
  @example(20) pageSize: int32;
  @example(0) total: int64;
}

// ============================================================
// Generic ID wrapper for responses
// ============================================================
model CreatedResponse {
  @format("uuid") id: string;
}
