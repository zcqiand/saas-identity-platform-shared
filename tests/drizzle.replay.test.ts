// tests/drizzle.replay.test.ts — target DDL schema replay
//
// The target schema is destructive by design: replay starts with an empty
// public schema and verifies the tenant/client/member isolation graph.

import { describe, it, expect, beforeAll, afterAll } from "vitest";
import { createRequire } from "node:module";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { spawnSync } from "node:child_process";

const __dirname = dirname(fileURLToPath(import.meta.url));
const SHARED_ROOT = resolve(__dirname, "..");

type PgClient = {
  connect(): Promise<void>;
  query<R = unknown>(
    sql: string,
    params?: unknown[],
  ): Promise<{ rows: R[]; rowCount: number }>;
  end(): Promise<void>;
};

let pgModule: { Client: new (cfg: unknown) => PgClient } | null = null;
try {
  const requireFromRuntime = createRequire(
    resolve("/app/node_modules", "pg/package.json"),
  );
  pgModule = requireFromRuntime("pg") as { Client: new (cfg: unknown) => PgClient };
} catch {
  try {
    const requireFromNext = createRequire(
      resolve(SHARED_ROOT, "../saas-identity-platform-nextjs/package.json"),
    );
    pgModule = requireFromNext("pg") as { Client: new (cfg: unknown) => PgClient };
  } catch {
    // The shared contract repo does not install pg as a runtime dependency.
  }
}

function requireEnv(name: string): string {
  const value = process.env[name];
  if (!value) throw new Error(`${name} env required`);
  return value;
}

const REQUIRED_PG_ENV = [
  "PG_HOST",
  "PG_PORT",
  "PG_USER",
  "PG_PASSWORD",
  "PG_DATABASE_TEST",
];
const allPgEnvPresent = REQUIRED_PG_ENV.every((name) => !!process.env[name]);

// 家族安全互锁（2026-09-14 lab 门禁链把 saas_test 清库重灌成 lab 表的事故，对称防线）：
// 本测试会 DROP public 全量重灌 saas DDL，目标库必须显式是 saas_*——
// 防止 lab_* 值（或手误传错库）跨族执行把对方测试库清掉。
if (allPgEnvPresent && !process.env.PG_DATABASE_TEST!.startsWith("saas_")) {
  throw new Error(
    `PG_DATABASE_TEST=${process.env.PG_DATABASE_TEST} 不是 saas_* 库——` +
      "drizzle.replay 会 DROP public 重灌 saas DDL，拒绝跨族执行",
  );
}

const EXPECTED_TABLES = [
  "oauth_access_token",
  "oauth_client",
  "oauth_code",
  "oauth_refresh_token",
  "sys_menu",
  "sys_role",
  "sys_role_menu",
  "sys_user",
  "tenant",
  "tenant_application",
  "tenant_member",
  "tenant_member_role",
];

const LEGACY_TABLES = [
  "api_keys",
  "apps",
  "audit_events",
  "audit_retention_policies",
  "menus",
  "oauth_codes",
  "permissions",
  "role_menu_grants",
  "role_permissions",
  "roles",
  "tenant_memberships",
  "tenants",
  "users",
];

describe("target DDL schema replay", () => {
  if (!pgModule || process.env.PG_REPLAY_SKIP === "1" || !allPgEnvPresent) {
    it.skip("pg driver or required PG_* env not available", () => {});
    return;
  }

  let client: PgClient | null = null;

  beforeAll(async () => {
    client = new pgModule!.Client({
      host: requireEnv("PG_HOST"),
      port: Number(requireEnv("PG_PORT")),
      user: requireEnv("PG_USER"),
      password: requireEnv("PG_PASSWORD"),
      database: requireEnv("PG_DATABASE_TEST"),
      connectionTimeoutMillis: 5000,
    });
    await client.connect();
    await client.query("DROP SCHEMA IF EXISTS public CASCADE");
    await client.query("CREATE SCHEMA public");
    await client.query('CREATE EXTENSION IF NOT EXISTS "uuid-ossp"');

    // Windows：spawnSync("npx") 无法解析 npx.cmd（ENOENT → status null），
    // push 根本没跑、断言却报 "exited null"。win32 走 npx.cmd + shell；
    // 断言强度不变（status 必须 === 0），Linux 行为不变。
    const isWin = process.platform === "win32";
    const result = spawnSync(
      isWin ? "npx.cmd" : "npx",
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
        shell: isWin,
      },
    );
    if (result.status !== 0) {
      throw new Error(`drizzle-kit push exited ${result.status}`);
    }
  }, 60000);

  afterAll(async () => {
    if (client) await client.end();
  });

  it("creates exactly the target tables", async () => {
    if (!client) return;
    const { rows } = await client.query<{ table_name: string }>(
      "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name",
    );
    const tables = rows.map((row) => row.table_name);
    expect(tables).toEqual(EXPECTED_TABLES);
    for (const legacy of LEGACY_TABLES) {
      expect(tables, `legacy table remains: ${legacy}`).not.toContain(legacy);
    }
  });

  it("uses UUID primary keys with uuid_generate_v4 defaults", async () => {
    if (!client) return;
    const { rows } = await client.query<{ table_name: string; column_default: string }>(
      `SELECT table_name, column_default
       FROM information_schema.columns
       WHERE table_schema = 'public' AND column_name = 'id'
       ORDER BY table_name`,
    );
    // 12 张表里 sys_role_menu / tenant_member_role 是复合主键 junction 表，
    // 没有 id 列（见下方 junction PK 断言），带 uuid 默认值 id 列的是 10 张。
    expect(rows).toHaveLength(10);
    for (const row of rows) {
      expect(row.column_default).toContain("uuid_generate_v4");
    }
  });

  it("enforces global user and tenant-member uniqueness", async () => {
    if (!client) return;
    const { rows: userIndexes } = await client.query<{ indexname: string }>(
      "SELECT indexname FROM pg_indexes WHERE schemaname = 'public' AND tablename = 'sys_user'",
    );
    const { rows: memberIndexes } = await client.query<{ indexname: string }>(
      "SELECT indexname FROM pg_indexes WHERE schemaname = 'public' AND tablename = 'tenant_member'",
    );
    expect(userIndexes.map((row) => row.indexname)).toEqual(
      expect.arrayContaining([
        "uk_sys_user_username",
        "uk_sys_user_email",
        "uk_sys_user_mobile",
      ]),
    );
    expect(memberIndexes.map((row) => row.indexname)).toContain("uk_tenant_user");
  });

  it("enforces client-scoped subscription and role uniqueness", async () => {
    if (!client) return;
    const { rows: subscriptionIndexes } = await client.query<{ indexname: string }>(
      "SELECT indexname FROM pg_indexes WHERE schemaname = 'public' AND tablename = 'tenant_application'",
    );
    const { rows: roleIndexes } = await client.query<{ indexname: string }>(
      "SELECT indexname FROM pg_indexes WHERE schemaname = 'public' AND tablename = 'sys_role'",
    );
    expect(subscriptionIndexes.map((row) => row.indexname)).toContain("uk_tenant_client");
    expect(roleIndexes.map((row) => row.indexname)).toContain("uk_tenant_client_role_code");
  });

  it("creates relational member-role and role-menu junction keys", async () => {
    if (!client) return;
    const { rows: memberRole } = await client.query<{ constraint_name: string }>(
      `SELECT constraint_name FROM information_schema.table_constraints
       WHERE table_schema = 'public' AND table_name = 'tenant_member_role'
         AND constraint_type = 'PRIMARY KEY'`,
    );
    const { rows: roleMenu } = await client.query<{ constraint_name: string }>(
      `SELECT constraint_name FROM information_schema.table_constraints
       WHERE table_schema = 'public' AND table_name = 'sys_role_menu'
         AND constraint_type = 'PRIMARY KEY'`,
    );
    expect(memberRole).toHaveLength(1);
    expect(roleMenu).toHaveLength(1);
    expect(memberRole[0].constraint_name).toContain("tenant_member_role");
    expect(roleMenu[0].constraint_name).toContain("sys_role_menu");
  });

  it("links refresh tokens to access tokens and preserves tenant context", async () => {
    if (!client) return;
    const { rows: refreshColumns } = await client.query<{ column_name: string; is_nullable: string }>(
      `SELECT column_name, is_nullable FROM information_schema.columns
       WHERE table_schema = 'public' AND table_name = 'oauth_refresh_token'`,
    );
    const refreshTokenColumn = refreshColumns.find(
      (row) => row.column_name === "access_token_id",
    );
    expect(refreshTokenColumn?.is_nullable).toBe("NO");

    const { rows: foreignKeys } = await client.query<{ table_name: string; foreign_table_name: string }>(
      `SELECT tc.table_name, ccu.table_name AS foreign_table_name
       FROM information_schema.table_constraints tc
       JOIN information_schema.constraint_column_usage ccu
         ON tc.constraint_name = ccu.constraint_name
       WHERE tc.constraint_type = 'FOREIGN KEY'
         AND tc.table_schema = 'public'
         AND tc.table_name IN ('oauth_code', 'oauth_access_token', 'oauth_refresh_token')`,
    );
    expect(foreignKeys).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ table_name: "oauth_code", foreign_table_name: "oauth_client" }),
        expect.objectContaining({ table_name: "oauth_code", foreign_table_name: "tenant" }),
        expect.objectContaining({ table_name: "oauth_refresh_token", foreign_table_name: "oauth_access_token" }),
      ]),
    );
  });

  it("cascades a tenant deletion through members and roles", async () => {
    if (!client) return;
    const tenantId = "11111111-1111-1111-1111-111111111111";
    const userId = "22222222-2222-2222-2222-222222222222";
    const memberId = "33333333-3333-3333-3333-333333333333";
    const roleId = "44444444-4444-4444-4444-444444444444";
    const clientId = "target-test-client";

    await client.query(
      "INSERT INTO tenant (id, tenant_key, name) VALUES ($1, 'target-test', 'Target Test')",
      [tenantId],
    );
    await client.query(
      "INSERT INTO sys_user (id, username, password) VALUES ($1, 'target-user', 'hash')",
      [userId],
    );
    await client.query(
      "INSERT INTO oauth_client (client_id, client_secret, client_name, grant_types, redirect_uris) VALUES ($1, 'secret', 'Target', 'authorization_code', 'https://example.test/callback')",
      [clientId],
    );
    await client.query(
      "INSERT INTO tenant_member (id, tenant_id, user_id) VALUES ($1, $2, $3)",
      [memberId, tenantId, userId],
    );
    await client.query(
      "INSERT INTO sys_role (id, tenant_id, client_id, role_code, role_name) VALUES ($1, $2, $3, 'owner', 'Owner')",
      [roleId, tenantId, clientId],
    );
    await client.query(
      "INSERT INTO tenant_member_role (member_id, role_id) VALUES ($1, $2)",
      [memberId, roleId],
    );

    await client.query("DELETE FROM tenant WHERE id = $1", [tenantId]);

    const { rowCount } = await client.query(
      "SELECT 1 FROM tenant_member WHERE id = $1 UNION ALL SELECT 1 FROM sys_role WHERE id = $2",
      [memberId, roleId],
    );
    expect(rowCount).toBe(0);
  });
});
