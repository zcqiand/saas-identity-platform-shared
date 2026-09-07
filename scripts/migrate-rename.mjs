// scripts/migrate-rename.mjs — 把 drizzle-kit generate 产物重命名为 Flyway 风格。
//
// 背景（ADR-0025 D9）：
// - drizzle-kit 0.28.x 文件名硬编码 `<seq>_<random>.sql`（实测，证据 drizzle-kit/bin.cjs:31510）。
// - springboot Flyway 仍消费 V<NNN>__<desc>.sql 风格迁移文件（DB-First 改造后，
//   springboot 实体由 scaffold 重生，但 Flyway 仍负责运行迁移与 tracking）。
// - drizzle hash 仅对 SQL 内容 sha256，不含文件名（实测 drizzle-orm/migrator.js）。
//   rename + 改 journal tag 后，已 apply 迁移 hash 不破。

import { readFileSync, writeFileSync, readdirSync, statSync, copyFileSync } from "node:fs";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const SHARED_ROOT = resolve(__dirname, "..");
const DRIZZLE_DIR = resolve(SHARED_ROOT, "drizzle");
const MIGRATIONS_DIR = resolve(SHARED_ROOT, "sql/migrations");

// 起始编号：扫 sql/migrations/ 下 V*.sql 找最大号；找不到从 1 开始
function nextSeqNumber() {
  let max = 0;
  try {
    for (const f of readdirSync(MIGRATIONS_DIR)) {
      const m = /^V(\d+)__/.exec(f);
      if (m) max = Math.max(max, Number(m[1]));
    }
  } catch {
    // migrations dir 不存在 = 第一条
  }
  return max + 1;
}

// 复制 drizzle/<seq>_<name>.sql → sql/migrations/V<NNN>__<name>.sql
//
// 设计（ADR-0025 D9）：
// - COPY 而非 MOVE：drizzle/ 仍是 drizzle-kit 真源（drizzle-kit migrate 读这里）；
//   sql/migrations/V*.sql 是给 springboot Flyway 用的副本（runner 共存不冲突）。
// - 不改 journal tag：drizzle 仍按原 tag 工作；springboot Flyway 按文件名 V<NNN> tracking。
// - hash 不变：drizzle hash 仅对 SQL 内容 sha256；改名/复制都不影响。
// - 已 apply 迁移不破：drizzle-kit migrate 按 journal entries 顺序 apply；Flyway 按 V<NNN> 字典序 apply；两者都不重复跑已记录迁移。
function main() {
  if (!statSync(DRIZZLE_DIR, { throwIfNoEntry: false })) {
    console.error("[migrate-rename] FATAL: drizzle/ 目录不存在；先跑 npm run db:generate");
    process.exit(1);
  }

  const seqStart = nextSeqNumber();
  console.log(`[migrate-rename] seqStart = V${String(seqStart).padStart(3, "0")}`);

  // 1. 扫 drizzle/*.sql（不含 meta/）
  const sqlFiles = readdirSync(DRIZZLE_DIR)
    .filter((f) => f.endsWith(".sql") && !f.startsWith("meta"))
    .sort();

  if (sqlFiles.length === 0) {
    console.log("[migrate-rename] 没有 .sql 产物，无需 copy。");
    return;
  }

  // 2. 逐个 copy
  for (let i = 0; i < sqlFiles.length; i++) {
    const oldName = sqlFiles[i];
    const seq = seqStart + i;
    // drizzle-kit 文件名 `<seq>_<name>.sql` → 拆出 <name>（去前缀）
    const namePart = oldName.replace(/^\d+_/, "").replace(/\.sql$/, "");
    const newName = `V${String(seq).padStart(3, "0")}__${namePart}.sql`;

    const fromPath = resolve(DRIZZLE_DIR, oldName);
    const toPath = resolve(MIGRATIONS_DIR, newName);
    copyFileSync(fromPath, toPath);

    console.log(`[migrate-rename]   drizzle/${oldName} → sql/migrations/${newName}`);
  }

  console.log("[migrate-rename] OK");
}

main();
