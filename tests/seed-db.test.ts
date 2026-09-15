// tests/seed-db.test.ts — saas seed-db.mjs 契约。
// 连库 fail-fast 约定与 lab 侧同款；集成段连真库（调用方 export DATABASE_URL，
// postgresql:// 形态；migrate 另需 PG_* 五件套 —— drizzle.config.ts fail-fast）。
// saas 语义：TRUNCATE 重灌（非 lab 的 upsert），断言「灌后行数 = 种子行数」。
import { describe, it, expect, beforeAll } from "vitest";
import { spawnSync, execFileSync } from "node:child_process";
import { createRequire } from "node:module";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const SHARED_ROOT = resolve(__dirname, "..");
const require = createRequire(resolve(SHARED_ROOT, "package.json"));

describe("seed-db 契约", () => {
  it("无 DATABASE_URL 时 fail-fast：退出码 1 且 stderr 带指引", () => {
    // spawnSync（非 execFileSync）：脚本 process.exit(1)，非零退出必须断 status 而非靠抛异常
    const r = spawnSync(process.execPath, ["scripts/seed-db.mjs"], {
      cwd: SHARED_ROOT,
      env: { ...process.env, DATABASE_URL: "" },
      encoding: "utf8",
    });
    expect(r.status).toBe(1);
    expect(r.stderr).toContain("DATABASE_URL");
  });
});

describe.skipIf(!process.env.DATABASE_URL)("seed-db 集成（saas_dev）", () => {
  // 共享灌库：一次 migrate + seed，多个断言 it 复用（勿每 it 重灌）。
  // 超时 300s：migrate（远程 PG drizzle-kit）+ TRUNCATE 重灌逐行 INSERT round-trip，
  // 全局默认 10s 恒假红（lab 侧同款教训）
  let seedOut = "";
  beforeAll(async () => {
    execFileSync(process.execPath, ["scripts/migrate-db.mjs"], {
      cwd: SHARED_ROOT, env: process.env,
    });
    seedOut = execFileSync(process.execPath, ["scripts/seed-db.mjs"], {
      cwd: SHARED_ROOT, env: process.env, encoding: "utf8",
    });
  }, 300_000);

  async function queryOne(sql: string, values: unknown[] = []): Promise<number> {
    const pg = require("pg");
    const client = new pg.Client({ connectionString: process.env.DATABASE_URL });
    await client.connect();
    try {
      const { rows } = await client.query(sql, values);
      return rows[0].n as number;
    } finally {
      await client.end();
    }
  }

  it(
    "migrate + seed 后 oauth_client 行数 = 种子行数",
    { timeout: 60_000 },
    async () => {
      expect(seedOut).toContain("灌库完成");
      // createRequire 锚在 <shared>/package.json，相对路径从 shared 根解析（brief 原文 ../ 是笔误）
      const seeds = require("./seeds/oauth_client.json") as unknown[];
      const n = await queryOne("SELECT count(*)::int AS n FROM oauth_client");
      expect(n).toBe(seeds.length);
    },
  );

  it(
    "sys_menu 行数 = 种子行数；sys_role_menu 补齐行存在且全部落在 DEFAULT client",
    { timeout: 60_000 },
    async () => {
      const menus = require("./seeds/sys_menu.json") as Array<{ id: string; clientId: string }>;
      const menuCount = await queryOne("SELECT count(*)::int AS n FROM sys_menu");
      expect(menuCount).toBe(menus.length);

      const clients = require("./seeds/oauth_client.json") as Array<{ id: string; clientId: string }>;
      const clientCodeById = new Map(clients.map((c) => [c.id, c.clientId]));
      const roles = require("./seeds/sys_role.json") as Array<{ id: string; roleCode: string }>;
      const grants = require("./seeds/sys_role_menu.json") as Array<{
        roleId: string;
        menuIds?: string[];
      }>;

      const fixtureRows = grants.reduce((s, g) => s + (g.menuIds ?? []).length, 0);
      const grantedRoleIds = new Set(grants.map((g) => g.roleId));
      const extraRoles = roles.filter(
        (r) => !grantedRoleIds.has(r.id) && r.roleCode === "admin",
      );
      const defaultMenuCount = menus.filter(
        (m) => clientCodeById.get(m.clientId) === "lab-management",
      ).length;

      const total = await queryOne("SELECT count(*)::int AS n FROM sys_role_menu");
      // 精确行数：fixture 拆行 + 补齐（非 admin-clear 角色 × DEFAULT client 全部菜单）。
      // 双键死代码形态恒 0 补齐 → total === fixtureRows；半修（只改 r.roleCode 不改
      // m.clientId，走 ?? DEFAULT_CLIENT_ID 恒放行）→ erp/crm 菜单也被错挂 → 超额。
      expect(total).toBe(fixtureRows + extraRoles.length * defaultMenuCount);

      // 挂载正确性：补齐角色的行必须全部落在 DEFAULT client（lab-management）的菜单上
      const misplaced = await queryOne(
        `SELECT count(*)::int AS n
           FROM sys_role_menu rm
           JOIN sys_menu m ON m.id = rm.menu_id
          WHERE rm.role_id = ANY($1) AND m.client_id <> $2`,
        [extraRoles.map((r) => r.id), "lab-management"],
      );
      expect(misplaced).toBe(0);
    },
  );
});
