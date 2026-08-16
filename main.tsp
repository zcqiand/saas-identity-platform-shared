import "@typespec/http";
import "@typespec/openapi3";
import "@typespec/rest";

// Models
import "./tsp/models/tenant.tsp";
import "./tsp/models/user.tsp";
import "./tsp/models/role.tsp";
import "./tsp/models/audit-event.tsp";
import "./tsp/models/api-key.tsp";
import "./tsp/models/membership.tsp";
import "./tsp/models/app.tsp";
import "./tsp/models/menu.tsp";
import "./tsp/models/role-menu-grant.tsp";

// Routes
import "./tsp/routes/auth.tsp";
import "./tsp/routes/admin-tenants.tsp";
import "./tsp/routes/me.tsp";
import "./tsp/routes/tenant-users.tsp";
import "./tsp/routes/tenant-roles.tsp";
import "./tsp/routes/tenant-api-keys.tsp";
import "./tsp/routes/tenant-audit.tsp";
import "./tsp/routes/admin-apps.tsp";
import "./tsp/routes/apps.tsp";
import "./tsp/routes/admin-app-menus.tsp";
import "./tsp/routes/tenant-role-menus.tsp";
import "./tsp/routes/oauth.tsp";

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