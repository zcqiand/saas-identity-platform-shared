// tests/seed-db.test.ts — saas seed-db.mjs 契约。
// 连库 fail-fast 约定与 lab 侧同款；集成段连真库（调用方 export DATABASE_URL，
// postgresql:// 形态；migrate 另需 PG_* 五件套 —— drizzle.config.ts fail-fast）。
// saas 语义：TRUNCATE 重灌（非 lab 的 upsert），断言「灌后行数 = 种子行数」。
import { describe, it, expect } from "vitest";
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
  // 超时 300s：migrate（远程 PG drizzle-kit）+ TRUNCATE 重灌逐行 INSERT round-trip，
  // 全局默认 10s 恒假红（lab 侧同款教训）
  it(
    "migrate + seed 后 oauth_client 行数 = 种子行数",
    { timeout: 300_000 },
    async () => {
      execFileSync(process.execPath, ["scripts/migrate-db.mjs"], {
        cwd: SHARED_ROOT, env: process.env,
      });
      const out = execFileSync(process.execPath, ["scripts/seed-db.mjs"], {
        cwd: SHARED_ROOT, env: process.env, encoding: "utf8",
      });
      expect(out).toContain("灌库完成");
      const pg = require("pg");
      const client = new pg.Client({ connectionString: process.env.DATABASE_URL });
      await client.connect();
      // createRequire 锚在 <shared>/package.json，相对路径从 shared 根解析（brief 原文 ../ 是笔误）
      const seeds = require("./seeds/oauth_client.json") as unknown[];
      const { rows } = await client.query(
        "SELECT count(*)::int AS n FROM oauth_client",
      );
      expect(rows[0].n).toBe(seeds.length);
      await client.end();
    },
  );
});
