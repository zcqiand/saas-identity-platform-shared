// Snapshot test: lock TS api-client + MSW + zod output
import { describe, it, expect } from "vitest";
import { readFileSync, existsSync } from "node:fs";
import { resolve } from "node:path";

const ROOT = resolve(import.meta.dirname, "../../generated/ts");

describe("TS client snapshot", () => {
  it("emits api-client endpoints (react-query hooks)", () => {
    const path = resolve(ROOT, "api-client/endpoints.ts");
    expect(existsSync(path)).toBe(true);
    const content = readFileSync(path, "utf-8");
    // React Query hooks should be present
    expect(content).toMatch(/useQuery/);
    expect(content).toMatch(/useMutation/);
    // All 8 route groups should produce some hook
    expect(content).toMatch(/adminTenants/);
    expect(content).toMatch(/tenantUsers/);
    expect(content).toMatch(/tenantRoles/);
    expect(content).toMatch(/tenantApiKeys/);
    expect(content).toMatch(/tenantAudit/);
    expect(content).toMatch(/adminOAuthApps/);
    expect(content).toMatch(/authLogin|AuthLogin|authLogout/);
    expect(content).toMatch(/MeWhoami|meWhoami|MeSwitchTenant|meSwitchTenant/);
  });

  it("emits MSW mock handlers", () => {
    const path = resolve(ROOT, "api-client/endpoints.msw.ts");
    expect(existsSync(path)).toBe(true);
    const content = readFileSync(path, "utf-8");
    expect(content).toMatch(/faker|Faker/);
    expect(content).toMatch(/HttpResponse|Response/);
  });

  it("emits zod schemas", () => {
    const path = resolve(ROOT, "zod-schemas.ts");
    expect(existsSync(path)).toBe(true);
    const content = readFileSync(path, "utf-8");
    expect(content).toMatch(/zod|z\./);
    expect(content).toMatch(/zod\.object|z\.object/);
  });
});