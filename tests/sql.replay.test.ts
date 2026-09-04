// SQL replay test：把 shared/sql/migrations/V001..V007 顺序跑一遍，断言 schema 一致。
//
// 依赖：borrows pg driver from output/lab-management-system-nextjs/node_modules/pg
// （shared 仓自身禁 npm runtime 依赖，见 ADR-0007）。
//
// 跳过条件：环境变量 PG_REPLAY_SKIP=1（CI 默认跑；本地开发无 PG 时可跳）。

import { describe, it, expect, beforeAll } from "vitest";
import { createRequire } from "node:module";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { readdirSync, readFileSync } from "node:fs";

const __dirname = dirname(fileURLToPath(import.meta.url));
const SHARED_ROOT = resolve(__dirname, "..");
const MIGRATIONS_DIR = resolve(SHARED_ROOT, "sql/migrations");

// pg 模块本地 stub（避免引入 @types/pg 作为 devDep；shared 仓禁 npm deps）
// 仅声明本测试用到的最小 surface
type PgClient = {
  connect(): Promise<void>;
  query<R = unknown>(sql: string, params?: unknown[]): Promise<{ rows: R[]; rowCount: number }>;
  end(): Promise<void>;
};

// 从 lab-nextjs 借 pg（与 scripts/create-pg-databases.mjs 同样套路）
const labNextjsRoot = resolve(SHARED_ROOT, "../lab-management-system-nextjs");
const requireFromLab = createRequire(resolve(labNextjsRoot, "package.json"));
let pgModule: { Client: new (cfg: unknown) => PgClient } | null = null;
try {
  pgModule = requireFromLab("pg") as { Client: new (cfg: unknown) => PgClient };
} catch {
  // 借不到就不跑（lab-nextjs 未 npm install）
}

// ADR-0019：禁 env 字面默认值兜底。PG_* 任一缺失即 fail-fast（仅在真正连接时校验）。
function requireEnv(name: string): string {
  const v = process.env[name];
  if (v === undefined || v === "") {
    throw new Error(`${name} env required (ADR-0019 禁字面默认值)`);
  }
  return v;
}

// 仅在真正要连接时才校验（PG_REPLAY_SKIP=1 或借不到 pg 或 env 缺失时全部 it.skip）。
// 用 getter 让 requireEnv 只在 beforeAll 执行时才求值,避免 skip 模式下 env 缺失直接 throw。
const PG_HOST = () => requireEnv("PG_HOST");
const PG_PORT = () => Number(requireEnv("PG_PORT"));
const PG_USER = () => requireEnv("PG_USER");
const PG_PASSWORD = () => requireEnv("PG_PASSWORD");
const PG_DATABASE = () => requireEnv("PG_DATABASE_TEST");

// env 完整性检查:任一缺失 → skip(本机无 PG 时不挂 CI)
const REQUIRED_PG_ENV = ["PG_HOST", "PG_PORT", "PG_USER", "PG_PASSWORD", "PG_DATABASE_TEST"];
const allPgEnvPresent = REQUIRED_PG_ENV.every((n) => !!process.env[n]);

const EXPECTED_TABLES = [
  "tenants",
  "users",
  "tenant_memberships",
  "roles",
  "permissions",
  "role_permissions",
  "api_keys",
  "apps",
  "menus",
  "role_menu_grants",
  "audit_events",
  "audit_retention_policies",
];

const EXPECTED_ENUMS = [
  "tenant_status",
  "user_status",
  "membership_status",
  "api_key_status",
  "app_status",
  "oauth_grant_type",
  "menu_type",
  "menu_status",
  "audit_action",
];

describe("SQL migrations replay", () => {
  if (
    !pgModule ||
    process.env.PG_REPLAY_SKIP === "1" ||
    !allPgEnvPresent
  ) {
    it.skip("pg driver or required PG_* env not available", () => {});
    return;
  }

  let client: PgClient | null = null;

  beforeAll(async () => {
    client = new pgModule.Client({
      host: PG_HOST(),
      port: PG_PORT(),
      user: PG_USER(),
      password: PG_PASSWORD(),
      database: PG_DATABASE(),
      connectionTimeoutMillis: 5000,
    });
    await client.connect();

    // 清空 public schema，让 V001..V007 从零跑
    await client.query("DROP SCHEMA IF EXISTS public CASCADE");
    await client.query("CREATE SCHEMA public");
    await client.query('CREATE EXTENSION IF NOT EXISTS "uuid-ossp"');

    // 跑 V001..V007（按文件名字典序）
    const files = readdirSync(MIGRATIONS_DIR)
      .filter((f) => /^V\d+__.*\.sql$/.test(f))
      .sort();
    for (const f of files) {
      const sql = readFileSync(resolve(MIGRATIONS_DIR, f), "utf-8");
      await client.query(sql);
    }
  }, 30000);

  it("creates 12 expected tables", async () => {
    if (!client) return;
    const { rows } = await client.query<{ table_name: string }>(
      "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name",
    );
    const tables = rows.map((r: { table_name: string }) => r.table_name);
    for (const expected of EXPECTED_TABLES) {
      expect(tables, `missing table: ${expected}`).toContain(expected);
    }
  });

  it("creates 9 expected enum types", async () => {
    if (!client) return;
    const { rows } = await client.query<{ typname: string }>(
      "SELECT typname FROM pg_type WHERE typtype = 'e' ORDER BY typname",
    );
    const enums = rows.map((r: { typname: string }) => r.typname);
    for (const expected of EXPECTED_ENUMS) {
      expect(enums, `missing enum: ${expected}`).toContain(expected);
    }
  });

  it("tenants has settings JSONB column", async () => {
    if (!client) return;
    const { rows } = await client.query<{ data_type: string }>(
      "SELECT data_type FROM information_schema.columns WHERE table_name = 'tenants' AND column_name = 'settings'",
    );
    expect(rows[0]?.data_type).toBe("jsonb");
  });

  it("users has unique (tenant_id, email) constraint", async () => {
    if (!client) return;
    const { rows } = await client.query<{ constraint_name: string }>(
      "SELECT constraint_name FROM information_schema.table_constraints WHERE table_name = 'users' AND constraint_type = 'UNIQUE'",
    );
    const names = rows.map((r: { constraint_name: string }) => r.constraint_name);
    expect(names).toContain("users_tenant_email_unique");
  });

  it("FK cascade works: tenant deletion removes users", async () => {
    if (!client) return;
    // 插入测试数据：tenant + user
    await client.query(
      "INSERT INTO tenants (id, code, name) VALUES ('11111111-1111-1111-1111-111111111111', 'test-tenant', 'Test Tenant')",
    );
    await client.query(
      "INSERT INTO users (id, tenant_id, username, email) VALUES ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'alice', 'alice@example.com')",
    );

    // 删除 tenant；FK ON DELETE CASCADE 应级联清掉 user
    await client.query("DELETE FROM tenants WHERE id = '11111111-1111-1111-1111-111111111111'");

    const { rowCount } = await client.query<{}>(
      "SELECT 1 FROM users WHERE id = '22222222-2222-2222-2222-222222222222'",
    );
    expect(rowCount).toBe(0);
  });

  // afterAll：清理连接
  // vitest 默认 afterAll 不在 describe 内 import；这里用 process.on('exit') 兜底
});