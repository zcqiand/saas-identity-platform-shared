// Snapshot test: lock OpenAPI output and the target tenant/client contract.
import { describe, it, expect, beforeAll } from "vitest";
import { readFileSync, existsSync } from "node:fs";
import { resolve } from "node:path";

const OPENAPI_PATH = resolve(import.meta.dirname, "../../generated/openapi/openapi.yaml");
let yaml = "";

beforeAll(() => {
  expect(existsSync(OPENAPI_PATH)).toBe(true);
  yaml = readFileSync(OPENAPI_PATH, "utf-8");
});

function pathPresent(path: string) {
  expect(yaml, `path ${path} should be emitted`).toContain(`\n  ${path}:`);
}

function operationIdPresent(opId: string) {
  expect(yaml, `operationId ${opId} should be emitted`).toContain(
    `operationId: ${opId}`,
  );
}

describe("OpenAPI snapshot", () => {
  it("emits openapi.yaml with openapi 3.x header", () => {
    expect(yaml).toMatch(/^openapi:\s+3\.0\.0$/m);
  });

  it("contains all target route groups", () => {
    for (const path of [
      "/api/v1/auth/",
      "/api/v1/admin/tenants",
      "/api/v1/me/",
      "/api/v1/tenants/{tenantId}/members",
      "/api/v1/tenants/{tenantId}/roles",
      "/api/v1/admin/clients",
      "/api/v1/clients/{clientId}/menus",
      "/api/v1/tenants/{tenantId}/applications",
      "/api/v1/tenants/{tenantId}/roles/{roleId}/menus",
      "/api/v1/oauth/authorize",
      "/api/v1/oauth/token",
    ]) {
      expect(yaml).toContain(path);
    }

    for (const legacyPath of [
      "/api/v1/tenants/{tenantId}/api-keys",
      "/api/v1/tenants/{tenantId}/audit-events",
      "/api/v1/admin/apps",
      "/api/v1/tenants/{tenantId}/users",
      "/api/v1/admin/oauth-clients",
      "/api/v1/oauth-clients",
      "/api/v1/oauth-clients/{clientId}/sys-menus",
      "/api/v1/tenants/{tenantId}/tenant-applications",
    ]) {
      expect(yaml).not.toContain(legacyPath);
    }
  });
});

describe("M04.F04 租户应用订阅", () => {
  it("M04.F04.I01 lists tenant applications", () => {
    pathPresent("/api/v1/tenants/{tenantId}/applications");
    operationIdPresent("TenantApplications_listTenantApplications");
  });

  it("M04.F04.I02 subscribes an application", () => {
    pathPresent("/api/v1/tenants/{tenantId}/applications");
    operationIdPresent("TenantApplications_subscribeTenantApplication");
  });

  it("M04.F04.I03 updates an application subscription", () => {
    pathPresent("/api/v1/tenants/{tenantId}/applications/{clientId}");
    operationIdPresent("TenantApplications_updateTenantApplication");
  });

  it("M04.F04.I04 removes an application subscription", () => {
    pathPresent("/api/v1/tenants/{tenantId}/applications/{clientId}");
    operationIdPresent("TenantApplications_removeTenantApplication");
  });
});

describe("M09.F03 当前用户有效菜单", () => {
  it("M09.F03.I01 me/menus client-scoped endpoint exists", () => {
    pathPresent("/api/v1/me/menus");
    operationIdPresent("Me_getMyMenus");
  });
});

describe("M09.F02 角色菜单关系", () => {
  it("M09.F02.I01 exposes relational role menu replacement", () => {
    pathPresent("/api/v1/tenants/{tenantId}/roles/{roleId}/menus");
    operationIdPresent("TenantRoleMenus_setSysRoleMenus");
  });
});
