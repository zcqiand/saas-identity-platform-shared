// tests/seed-db.test.ts — saas seed-db.mjs 契约。
// 连库 fail-fast 约定与 lab 侧同款；集成段连真库（调用方 export DATABASE_URL，
// postgresql:// 形态；migrate 另需 PG_* 五件套 —— drizzle.config.ts fail-fast）。
// saas 语义：TRUNCATE 重灌（非 lab 的 upsert），断言「灌后行数 = 种子行数」。
import { describe, it, expect, beforeAll } from "vitest";
import { spawnSync, execFileSync } from "node:child_process";
import { createRequire } from "node:module";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { seedDatabase } from "../scripts/seed-db.mjs";
import type { SeedMenu, SeedSet } from "../scripts/seed-db.d.mts";

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

  it(
    "sys_menu fail-safe skip：clientId 查不到 app 的菜单行不落库，摘要计数反映 skip（ADR-0019）",
    { timeout: 60_000 },
    async () => {
      const menus = require("./seeds/sys_menu.json") as SeedMenu[];
      const seeds = {
        tenants: require("./seeds/tenant.json"),
        users: require("./seeds/sys_user.json"),
        roles: require("./seeds/sys_role.json"),
        memberships: require("./seeds/tenant_member.json"),
        apps: require("./seeds/oauth_client.json"),
        roleMenuGrants: require("./seeds/sys_role_menu.json"),
      } as Omit<SeedSet, "menus">;

      // 注入毒行：clientId 指向 apps 集合里不存在的 app id。
      // 旧行为（?? DEFAULT_CLIENT_ID 兜底）会把它静默存成 'lab-management'。
      const orphan: SeedMenu = {
        ...menus[0],
        id: "00000000-0000-0000-0000-00000000d001",
        clientId: "00000000-0000-0000-0000-ffffffffffff",
      };
      const seededMenus = [...menus, orphan];

      const pg = require("pg");
      const client = new pg.Client({ connectionString: process.env.DATABASE_URL });
      await client.connect();
      try {
        const summary = await seedDatabase(client, { ...seeds, menus: seededMenus });

        // 摘要计数：菜单总数 -1（skip），skip 计数 =1
        expect(summary.sys_menu_skipped).toBe(1);
        expect(summary.sys_menu).toBe(menus.length);

        // 不落库：毒行 id 不在 sys_menu，且无任何行被兜底写成别的 client
        const orphanRows = await client.query(
          "SELECT count(*)::int AS n FROM sys_menu WHERE id = $1",
          [orphan.id],
        );
        expect(orphanRows.rows[0].n).toBe(0);
        const total = await client.query(
          "SELECT count(*)::int AS n FROM sys_menu",
        );
        expect(total.rows[0].n).toBe(menus.length);

        // 旁证：所有落库行的 client_id 都来自 apps 集合的 clientId（无字面量污染）
        const clientIds = new Set(
          (seeds.apps as Array<{ clientId: string }>).map((a) => a.clientId),
        );
        const alien = await client.query(
          "SELECT DISTINCT client_id AS cid FROM sys_menu",
        );
        for (const row of alien.rows as Array<{ cid: string }>) {
          expect(clientIds.has(row.cid)).toBe(true);
        }
      } finally {
        await client.end();
      }
    },
  );
});
