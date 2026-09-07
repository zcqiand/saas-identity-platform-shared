// scripts/migrate-db.mjs — 替代 sync-db.mjs；调 drizzle-kit migrate 应用迁移到 PG。
//
// 背景（ADR-0025 D1）：
// - shared 仓是 schema-first 单一真源；本脚本是「从真源直推 PG」入口。
// - 替代原 sync-db.mjs（手写 V 文件 + __schema_migrations tracking）。
// - Drizzle Kit 用内置 __drizzle_migrations 表 tracking，无需手维护。
//
// 用法：
//   node scripts/migrate-db.mjs                  # 默认 saas_dev @ 100.79.128.25
//   PG_DATABASE=saas_test node scripts/migrate-db.mjs
//   PG_HOST=... PG_PORT=... PG_USER=... PG_PASSWORD=... PG_DATABASE=... node scripts/migrate-db.mjs
//
// 安全：默认 apply drizzle/meta/_journal.json 列出的所有未 apply 迁移；不会 DROP 表。
// 需要重建时手动：DROP SCHEMA public CASCADE; CREATE SCHEMA public; 后再跑。

import { spawnSync } from "node:child_process";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { createRequire } from "node:module";

const __dirname = dirname(fileURLToPath(import.meta.url));
const SHARED_ROOT = resolve(__dirname, "..");

// 优先 DATABASE_URL（与 saas-nextjs/db/index.ts 一致）；fallback 到 PG_* 散件
function buildDrizzleKitEnv() {
  const env = { ...process.env };
  if (!env.DATABASE_URL) {
    const host = env.PG_HOST ?? "100.79.128.25";
    const port = env.PG_PORT ?? "5432";
    const user = env.PG_USER ?? "postgres";
    const password = env.PG_PASSWORD ?? "";
    const database = env.PG_DATABASE ?? "saas_dev";
    env.DATABASE_URL = `postgresql://${user}:${password}@${host}:${port}/${database}`;
  }
  return env;
}

// 借 pg driver（与原 sync-db.mjs 同套路）
function loadPg() {
  const requireFromRuntime = createRequire(
    resolve("/app/node_modules", "pg/package.json"),
  );
  try {
    return requireFromRuntime("pg");
  } catch {
    const nextjsRoot = resolve(
      SHARED_ROOT,
      "../saas-identity-platform-nextjs/package.json",
    );
    const requireFromNext = createRequire(nextjsRoot);
    return requireFromNext("pg");
  }
}

// Preamble：drizzle-kit 不生成 CREATE EXTENSION（ADR-0025 D5 约束）；
// schema.ts 用 uuid_generate_v4() 依赖 uuid-ossp；空库首次 migrate 必先建扩展。
async function ensureExtensions() {
  const pg = loadPg();
  const { Client } = pg;
  const url = process.env.DATABASE_URL;
  const client = new Client({ connectionString: url });
  await client.connect();
  try {
    await client.query(`CREATE EXTENSION IF NOT EXISTS "uuid-ossp"`);
    console.log("[migrate-db] extension uuid-ossp OK");
  } finally {
    await client.end();
  }
}

async function main() {
  const env = buildDrizzleKitEnv();
  process.env.DATABASE_URL = env.DATABASE_URL;
  console.log(
    `[migrate-db] DATABASE_URL = ${env.DATABASE_URL.replace(/:[^:@]+@/, ":***@")}`,
  );

  await ensureExtensions();

  // drizzle-kit migrate 读 drizzle.config.ts，自动用 PG_* env 拼连接
  const r = spawnSync(
    "npx",
    ["--no", "drizzle-kit", "migrate", "--config", "drizzle.config.ts"],
    {
      cwd: SHARED_ROOT,
      env,
      stdio: "inherit",
    },
  );

  if (r.status !== 0) {
    console.error(`[migrate-db] FATAL: drizzle-kit migrate 退出 ${r.status}`);
    process.exit(r.status ?? 1);
  }

  console.log("[migrate-db] OK");
}

main().catch((err) => {
  console.error("[migrate-db] FATAL:", err);
  process.exit(1);
});
