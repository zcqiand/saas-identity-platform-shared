// Snapshot test: lock OpenAPI spec output
import { describe, it, expect } from "vitest";
import { readFileSync, existsSync } from "node:fs";
import { resolve } from "node:path";

const OPENAPI_PATH = resolve(import.meta.dirname, "../../generated/openapi/openapi.yaml");

describe("OpenAPI snapshot", () => {
  it("emits openapi.yaml with paths", () => {
    expect(existsSync(OPENAPI_PATH)).toBe(true);
    const content = readFileSync(OPENAPI_PATH, "utf-8");
    expect(content).toMatch(/^openapi:\s+3\.0\.0$/m);
    expect(content).toContain("/api/v1/auth/login");
    expect(content).toContain("/api/v1/tenants/{tenantId}/users");
    expect(content).toContain("/api/v1/admin/tenants");
    expect(content).toContain("/api/v1/admin/apps");
  });

  it("contains all route groups", () => {
    const content = readFileSync(OPENAPI_PATH, "utf-8");
    for (const path of [
      "/api/v1/auth/",
      "/api/v1/admin/tenants",
      "/api/v1/me/",
      "/api/v1/tenants/{tenantId}/users",
      "/api/v1/tenants/{tenantId}/roles",
      "/api/v1/tenants/{tenantId}/api-keys",
      "/api/v1/tenants/{tenantId}/audit-events",
      "/api/v1/admin/apps",
      "/api/v1/admin/apps/{appId}/menus",
      "/api/v1/tenants/{tenantId}/roles/{roleId}/menus",
      "/api/v1/oauth/authorize",
      "/api/v1/oauth/token",
    ]) {
      expect(content).toContain(path);
    }
  });
});