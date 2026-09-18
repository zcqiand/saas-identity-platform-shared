// scripts/rebaseline-db.mjs — 一次性破坏性 re-baseline（target DDL）
//
// ⚠️ 会 DROP public schema、所有旧表和数据，仅用于明确授权的 dev/test 或运维窗口。
// 生产环境必须同时显式设置 NODE_ENV=production 与 PG_REBASELINE_ALLOW=1。

import { spawnSync } from "node:child_process";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import postgres from "postgres";

const SHARED_ROOT = resolve(dirname(fileURLToPath(import.meta.url)), "..");

function requireEnv(name) {
  const value = process.env[name];
  if (!value) throw new Error(`${name} env required`);
  return value;
}

const pgHost = requireEnv("PG_HOST");
const pgPort = requireEnv("PG_PORT");
const pgUser = requireEnv("PG_USER");
const pgPassword = requireEnv("PG_PASSWORD");
const pgDatabase = requireEnv("PG_DATABASE");

if (process.env.NODE_ENV === "production" && process.env.PG_REBASELINE_ALLOW !== "1") {
  console.error(
    "[rebaseline] FATAL: prod 环境必须 PG_REBASELINE_ALLOW=1 才允许跑",
  );
  process.exit(1);
}

const url = `postgresql://${encodeURIComponent(pgUser)}:${encodeURIComponent(pgPassword)}@${pgHost}:${pgPort}/${pgDatabase}`;
const sql = postgres(url, { max: 1, connect_timeout: 5 });

console.log("[rebaseline] 连接:", url.replace(/:[^:@/]+@/, ":***@"));

try {
  console.log("[rebaseline] step 1/4 — DROP SCHEMA public CASCADE");
  await sql.unsafe("DROP SCHEMA IF EXISTS public CASCADE");
  await sql.unsafe("CREATE SCHEMA public");

  console.log("[rebaseline] step 2/4 — CREATE EXTENSION uuid-ossp");
  await sql.unsafe('CREATE EXTENSION IF NOT EXISTS "uuid-ossp"');
} finally {
  await sql.end();
}

console.log("[rebaseline] step 3/4 — drizzle-kit push（应用目标 schema）");
// Windows：spawnSync 裸起 `npx`（.cmd shim）在 Node ≥18.20 会 EINVAL / 退 null
// —— DDL 实际已生效但脚本按失败处理。win32 走 shell:true 让 cmd.exe 解析
// npx（同 migrate-db.mjs 的修法）；args 全是固定字面量，无注入面。
const isWin = process.platform === "win32";
const r = spawnSync(
  "npx",
  ["--no", "drizzle-kit", "push", "--config", "drizzle.config.ts", "--force"],
  {
    cwd: SHARED_ROOT,
    env: { ...process.env },
    stdio: "inherit",
    shell: isWin,
  },
);

if (r.status !== 0) {
  console.error(`[rebaseline] FATAL: drizzle-kit push 退出 ${r.status}`);
  process.exit(r.status ?? 1);
}

console.log("[rebaseline] step 4/4 — target DDL OK");
console.log("[rebaseline] 下游必须重新生成 shared schema mirror 与客户端");
