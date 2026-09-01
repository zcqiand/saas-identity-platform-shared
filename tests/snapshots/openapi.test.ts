// Snapshot test: lock OpenAPI spec output.
//
// 一半是"通用快照"（守住 openapi.yaml 存在 + 核心 path 不丢），一半是
// "M-ID 化断言"（针对已上线模块的每个 I 子项，断言精确 path + operationId
// 仍在）。后者让 fnReporter 把 Mxx.Fxx.Ixx 写进 .state/trace.json，
// L5 引用检查有据可查。
//
// it() 名规则：描述句必须含 M\d{2}(?:\.F\d{2}(?:\.I\d{2})?)? 完整 ID
// （fnReporter 只会抓完整 ID，不会把"plan/spike 描述性提及"当 ID 抓）。
import { describe, it, expect, beforeAll } from "vitest";
import { readFileSync, existsSync } from "node:fs";
import { resolve } from "node:path";

const OPENAPI_PATH = resolve(import.meta.dirname, "../../generated/openapi/openapi.yaml");

// 在所有 describe 之前一次性读文件，避免每条 it() 重读 + 暴露给断言 helper。
let yaml = "";
beforeAll(() => {
  expect(existsSync(OPENAPI_PATH)).toBe(true);
  yaml = readFileSync(OPENAPI_PATH, "utf-8");
});

/** 从 yaml 中抽出 `paths:` 节，做 substring 包含断言。 */
function pathPresent(path: string) {
  expect(yaml, `path ${path} should be emitted`).toContain(`\n  ${path}:`);
}

/** 从 yaml 中抽出精确 operationId（用于"实现仓 delete 写错 controller 名"早期发现）。 */
function operationIdPresent(opId: string) {
  expect(yaml, `operationId ${opId} should be emitted`).toContain(`operationId: ${opId}`);
}

// ============================================================
// 通用快照（不含 M-ID，避免污染 trace）
// ============================================================
describe("OpenAPI snapshot", () => {
  it("emits openapi.yaml with openapi 3.x header", () => {
    expect(yaml).toMatch(/^openapi:\s+3\.0\.0$/m);
  });

  it("contains all route groups", () => {
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
      expect(yaml).toContain(path);
    }
  });
});

// ============================================================
// M05.F01 — API Key 生命周期（已上线，M05.F01.I05 物理删除仍标"规划"，跳过）
// 镜像仓：saas-aspnetcore / saas-springboot / saas-nextjs / saas-msw
// ============================================================
describe("M05 API Key 生命周期", () => {
  it("M05.F01.I01 list API keys exposes GET /tenants/{tenantId}/api-keys", () => {
    pathPresent("/api/v1/tenants/{tenantId}/api-keys");
    operationIdPresent("TenantApiKeys_listApiKeys");
  });

  it("M05.F01.I02 create API key exposes POST /tenants/{tenantId}/api-keys", () => {
    pathPresent("/api/v1/tenants/{tenantId}/api-keys");
    operationIdPresent("TenantApiKeys_createApiKey");
  });

  it("M05.F01.I03 revoke API key exposes POST /tenants/{tenantId}/api-keys/{keyId}/revoke", () => {
    pathPresent("/api/v1/tenants/{tenantId}/api-keys/{keyId}/revoke");
    operationIdPresent("TenantApiKeys_revokeApiKey");
  });

  it("M05.F01.I04 rotate API key exposes POST /tenants/{tenantId}/api-keys/{keyId}/rotate", () => {
    pathPresent("/api/v1/tenants/{tenantId}/api-keys/{keyId}/rotate");
    operationIdPresent("TenantApiKeys_rotateApiKey");
  });
});

// ============================================================
// M06 — 审计日志（已上线）
// ============================================================
describe("M06.F01 审计事件查询", () => {
  it("M06.F01.I01 list audit events exposes GET /tenants/{tenantId}/audit-events", () => {
    pathPresent("/api/v1/tenants/{tenantId}/audit-events");
    operationIdPresent("TenantAudit_listAuditEvents");
  });

  it("M06.F01.I02 list audit events by user exposes GET /tenants/{tenantId}/audit-events/by-user/{userId}", () => {
    pathPresent("/api/v1/tenants/{tenantId}/audit-events/by-user/{userId}");
    operationIdPresent("TenantAudit_listAuditEventsByUser");
  });

  it("M06.F01.I03 export audit events exposes POST /tenants/{tenantId}/audit-events/export", () => {
    pathPresent("/api/v1/tenants/{tenantId}/audit-events/export");
    operationIdPresent("TenantAudit_exportAuditEvents");
  });
});

describe("M06.F02 审计留存策略", () => {
  it("M06.F02.I01 get retention policy exposes GET /tenants/{tenantId}/audit-events/retention", () => {
    pathPresent("/api/v1/tenants/{tenantId}/audit-events/retention");
    operationIdPresent("TenantAudit_getRetentionPolicy");
  });

  it("M06.F02.I02 set retention policy exposes PUT /tenants/{tenantId}/audit-events/retention", () => {
    pathPresent("/api/v1/tenants/{tenantId}/audit-events/retention");
    operationIdPresent("TenantAudit_setRetentionPolicy");
  });
});

describe("M06.F03 AuditWriter 副作用契约", () => {
  // M06.F03.I01 — AuditWriter.WriteAsync 是实现仓内部副作用，**不**暴露 HTTP。
  // 契约只约束"写端点"语义由实现仓各维护一份触发列表（见
  // tsp/routes/tenant-audit.tsp:50 注释）。这里守住 OpenAPI 不能偷偷挂
  // 一个 POST /audit-events / PUT /audit-events 之类的写端点，否则就
  // 破坏了"前端从不写 audit_events"的家族约定。
  it("M06.F03.I01 AuditWriter must not expose any HTTP write endpoint", () => {
    // audit-events 必须是只读：list / by-user 是 GET，export 是 POST（异步下载）。
    // 任何 PUT/PATCH/直接的 POST（非 /export）= 破坏"前端从不写 audit_events"约定。
    const directWriteRe = new RegExp(
      String.raw`\n  /api/v1/tenants/\{tenantId\}/audit-events:\s*\n\s+(post|put|patch):`,
    );
    const childWriteRe = new RegExp(
      String.raw`/audit-events/(by-user/\{userId\}|export):\s*\n\s+(put|patch):`,
    );
    expect(
      yaml,
      "audit-events 必须是只读（list 是 GET、by-user 是 GET、export 是 POST；不允许直写端点）",
    ).not.toMatch(directWriteRe);
    expect(
      yaml,
      "by-user/export 子路径若存在，只能是 GET/POST-export，绝不能是 PUT/PATCH",
    ).not.toMatch(childWriteRe);
  });
});

// ============================================================
// M09.F03 — 当前用户有效菜单（已上线）
// M09.F03.I02-I04 是 saas-springboot/aspnetcore 的 meService 内部实现，
// shared 仓无 path 可断言，只挂 I01。
// ============================================================
describe("M09.F03 当前用户有效菜单", () => {
  it("M09.F03.I01 me/menus session-checked endpoint exists (GET /api/v1/me/menus)", () => {
    pathPresent("/api/v1/me/menus");
    operationIdPresent("Me_getMyMenus");
  });
});