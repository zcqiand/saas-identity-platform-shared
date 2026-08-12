import "@typespec/http";
import "@typespec/openapi3";

using TypeSpec.Http;

// ============================================================
// SaaS Identity Platform — Multi-Tenant TypeSpec SSOT
// 唯一真源。产 OpenAPI 3.1 → orval (TS api-client/msw/zod)
//                        → openapi-generator-java (Spring)
//                        → openapi-generator-dotnet (ASP.NET Core)
// ============================================================

@service({
  title: "SaaS Identity Platform",
  version: "0.1.0",
})
@server("https://api.example.com", "Production")
@route("/api")
namespace Saas.Identity.Shared;

// --- Auth / Models / Routes ---
import "./models/tenant.tsp";
import "./models/user.tsp";
import "./models/role.tsp";
import "./models/oauth-app.tsp";
import "./models/audit-event.tsp";
import "./models/api-key.tsp";
import "./models/membership.tsp";
import "./routes/auth.tsp";
import "./routes/admin-tenants.tsp";
import "./routes/me.tsp";
import "./routes/tenant-users.tsp";
import "./routes/tenant-roles.tsp";
import "./routes/tenant-api-keys.tsp";
import "./routes/tenant-audit.tsp";
import "./routes/admin-oauth-apps.tsp";

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