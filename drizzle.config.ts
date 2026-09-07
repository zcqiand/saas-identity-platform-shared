// drizzle-kit config — schema-first SSOT（ADR-0025）
//
// 关键约束：
// - `schema` 指向 src/db/schema.ts（TS 手写）；人改这一份就够
// - `out` 产物进 drizzle/ 目录；.sql + meta/_journal.json + meta/<seq>_snapshot.json 都入 git
// - `migrations.schema = "public"` 让 __drizzle_migrations 表落 public schema，与 Flyway 表同 schema
//   便于 psql / pgAdmin 一次性查看所有迁移追踪
// - dbCredentials 从 PG_* 环境变量读，与原 scripts/sync-db.mjs 同套
//
// 文件名 post-step 重命名见 scripts/migrate-rename.mjs：
//   drizzle/0000_<random>.sql → sql/migrations/V001__<random>.sql
//   （drizzle-kit 0.28 文件名硬编码 <seq>_<random>.sql；rename + 改 journal tag 后
//    hash 不变，已 apply 迁移不破 — 实测确认）

import { defineConfig } from "drizzle-kit";

const pgHost = process.env.PG_HOST ?? "100.79.128.25";
const pgPort = Number(process.env.PG_PORT ?? 5432);
const pgUser = process.env.PG_USER ?? "postgres";
const pgPassword = process.env.PG_PASSWORD ?? "";
const pgDatabase = process.env.PG_DATABASE ?? "saas_dev";
const pgSsl = process.env.PG_SSL === "1";

export default defineConfig({
  dialect: "postgresql",
  schema: "./src/db/schema.ts",
  out: "./drizzle",
  schemaFilter: ["public"],
  migrations: {
    table: "__drizzle_migrations",
    schema: "public",
    prefix: "index",
  },
  dbCredentials: {
    host: pgHost,
    port: pgPort,
    user: pgUser,
    password: pgPassword,
    database: pgDatabase,
    ssl: pgSsl,
  },
  verbose: true,
  strict: true,
});
