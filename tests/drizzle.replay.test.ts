// tests/drizzle.replay.test.ts — Drizzle Kit schema-first replay test（ADR-0025）
//
// 设计：从 src/db/schema.ts 生成 → drizzle-kit push 到测试库 → 断言 12 表 + 9 enum + JSONB CHECK + FK cascade。
// 替代原 tests/sql.replay.test.ts（手读 V 文件 → 顺序跑）。
//
// 跳过条件：环境变量 PG_REPLAY_SKIP=1；或借不到 pg driver（lab-nextjs / saas-nextjs 未 npm install）；
// 或 PG_* 任一 env 缺失（ADR-0019 禁字面默认值兜底）。

import { describe, it, expect, beforeAll, afterAll } from "vitest";
import { createRequire } from "node:module";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { spawnSync } from "node:child_process";

const __dirname = dirname(fileURLToPath(import.meta.url));
const SHARED_ROOT = resolve(__dirname, "..");

// pg 模块本地 stub（避免引入 @types/pg 作为 devDep；shared 仓禁 npm runtime 依赖）
type PgClient = {
  connect(): Promise<void>;
  query<R = unknown>(
    sql: string,
    params?: unknown[],
  ): Promise<{ rows: R[]; rowCount: number }>;
  end(): Promise<void>;
};

// 借 pg driver（runtime /app/node_modules/pg → dev saas-nextjs/node_modules/pg）
const requireFromRuntime = createRequire(
  resolve("/app/node_modules", "pg/package.json"),
);
let pgModule: { Client: new (cfg: unknown) => PgClient } | null = null;
try {
  pgModule = requireFromRuntime("pg") as { Client: new (cfg: unknown) => PgClient };
} catch {
  try {
    const saasNextRoot = resolve(
      SHARED_ROOT,
      "../saas-identity-platform-nextjs/package.json",
    );
    const requireFromNext = createRequire(saasNextRoot);
    pgModule = requireFromNext("pg") as { Client: new (cfg: unknown) => PgClient };
  } catch {
    // 借不到就不跑
  }
}

// ADR-0019：env 缺失 fail-fast；仅在真正连接时校验
function requireEnv(name: string): string {
  const v = process.env[name];
  if (v === undefined || v === "") {
    throw new Error(`${name} env required (ADR-0019 禁字面默认值)`);
  }
  return v;
}

const REQUIRED_PG_ENV = [
  "PG_HOST",
  "PG_PORT",
  "PG_USER",
  "PG_PASSWORD",
  "PG_DATABASE_TEST",
];
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

describe("Drizzle schema-first replay", () => {
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
      host: requireEnv("PG_HOST"),
      port: Number(requireEnv("PG_PORT")),
      user: requireEnv("PG_USER"),
      password: requireEnv("PG_PASSWORD"),
      database: requireEnv("PG_DATABASE_TEST"),
      connectionTimeoutMillis: 5000,
    });
    await client.connect();

    // 清空 public schema，让 drizzle-kit push 从零建
    await client.query("DROP SCHEMA IF EXISTS public CASCADE");
    await client.query("CREATE SCHEMA public");

    // preamble: uuid-ossp 扩展（schema.ts 用 uuid_generate_v4()）
    await client.query('CREATE EXTENSION IF NOT EXISTS "uuid-ossp"');

    // drizzle-kit push：把 schema.ts 直接推到 test DB（不在生产用）
    const r = spawnSync(
      "npx",
      [
        "--no",
        "drizzle-kit",
        "push",
        "--config",
        "drizzle.config.ts",
        "--force",
      ],
      {
        cwd: SHARED_ROOT,
        env: {
          ...process.env,
          PG_DATABASE: requireEnv("PG_DATABASE_TEST"),
        },
        stdio: "inherit",
      },
    );
    if (r.status !== 0) {
      throw new Error(`drizzle-kit push exited ${r.status}`);
    }
  }, 60000);

  afterAll(async () => {
    if (client) await client.end();
  });

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

  it("tenants.settings has jsonb_typeof = object CHECK constraint", async () => {
    if (!client) return;
    const { rows } = await client.query<{ constraint_name: string }>(
      `SELECT conname AS constraint_name
       FROM pg_constraint
       WHERE conrelid = 'public.tenants'::regclass
         AND contype = 'c'
         AND pg_get_constraintdef(oid) LIKE '%jsonb_typeof%'`,
    );
    expect(rows.map((r) => r.constraint_name)).toContain(
      "tenants_settings_is_object",
    );
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
    await client.query(
      "INSERT INTO tenants (id, code, name) VALUES ('11111111-1111-1111-1111-111111111111', 'test-tenant', 'Test Tenant')",
    );
    await client.query(
      "INSERT INTO users (id, tenant_id, username, email) VALUES ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'alice', 'alice@example.com')",
    );
    await client.query(
      "DELETE FROM tenants WHERE id = '11111111-1111-1111-1111-111111111111'",
    );
    const { rowCount } = await client.query<{}>(
      "SELECT 1 FROM users WHERE id = '22222222-2222-2222-2222-222222222222'",
    );
    expect(rowCount).toBe(0);
  });
});
