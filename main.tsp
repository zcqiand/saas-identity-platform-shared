import "@typespec/http";
import "@typespec/openapi3";
import "@typespec/rest";

// Models
import "./tsp/models/tenant.tsp";
import "./tsp/models/sys-user.tsp";
import "./tsp/models/sys-role.tsp";
import "./tsp/models/tenant-member.tsp";
import "./tsp/models/oauth-client.tsp";
import "./tsp/models/sys-menu.tsp";
import "./tsp/models/sys-role-menu.tsp";

// Routes
import "./tsp/routes/sessions.tsp";
import "./tsp/routes/admin-tenants.tsp";
import "./tsp/routes/me.tsp";
import "./tsp/routes/tenant-members.tsp";
import "./tsp/routes/tenant-sys-roles.tsp";
import "./tsp/routes/admin-oauth-clients.tsp";
import "./tsp/routes/oauth-clients.tsp";
import "./tsp/routes/admin-oauth-clients-sys-menus.tsp";
import "./tsp/routes/tenant-sys-role-menus.tsp";
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
