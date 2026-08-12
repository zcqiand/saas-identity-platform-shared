// Snapshot test: lock Java Spring codegen output
import { describe, it, expect } from "vitest";
import { readFileSync, existsSync } from "node:fs";
import { resolve } from "node:path";

const ROOT = resolve(import.meta.dirname, "../../generated/java/src/main/java");

describe("Java Spring codegen snapshot", () => {
  it("emits 8 Controller interfaces", () => {
    const apiDir = resolve(ROOT, "saas/identity/shared/api");
    expect(existsSync(apiDir)).toBe(true);
    const controllers = [
      "AdminTenantsApi",
      "AuthApi",
      "MeApi",
      "TenantApiKeysApi",
      "TenantAuditApi",
      "TenantRolesApi",
      "TenantUsersApi",
      "AdminOAuthAppsApi",
    ];
    for (const c of controllers) {
      expect(existsSync(resolve(apiDir, `${c}.java`))).toBe(true);
    }
  });

  it("emits DTO records (Tenant, User, Role, OAuthApp)", () => {
    const dtoDir = resolve(ROOT, "saas/identity/shared/dto");
    expect(existsSync(dtoDir)).toBe(true);
    for (const d of ["Tenant", "User", "Role", "OAuthApp"]) {
      expect(existsSync(resolve(dtoDir, `${d}.java`))).toBe(true);
    }
  });

  it("AdminTenantsApi has @RequestMapping('/api/v1/admin/tenants')", () => {
    const file = resolve(ROOT, "saas/identity/shared/api/AdminTenantsApi.java");
    const content = readFileSync(file, "utf-8");
    expect(content).toContain("/api/v1/admin/tenants");
  });
});