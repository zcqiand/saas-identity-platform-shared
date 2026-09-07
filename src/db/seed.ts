// src/db/seed.ts — saas-identity-platform seed fixtures（ADR-0025）
//
// 历史：抽自原 sql/migrations/V014__seed_lab_mgmt_app.sql +
// V015__seed_lab_mgmt_menus.sql + V016__seed_family_fixtures.sql（共 386 行）。
// seed 不是 schema；不进 drizzle-kit generate 链路。
//
// 用法：
//   DATABASE_URL=postgresql://... npm run db:seed
//   或 tsx src/db/seed.ts
//
// 注意：
// - seed 幂等：使用 ON CONFLICT DO NOTHING / array_cat 去重（与 V019 同款防御模式）
// - 不写 updated_at（应用层各自维护；seed 时 DB 触发器已删）
// - menu / role / api-key seed 走硬编码 fixture，避免依赖运行时数据

import { drizzle } from "drizzle-orm/postgres-js";
import postgres from "postgres";
import { sql } from "drizzle-orm";
import * as s from "./schema.js";

const DATABASE_URL =
  process.env.DATABASE_URL ??
  process.env.PG_INSTANCE_URL ??
  "postgresql://postgres:postgres@100.79.128.25:5432/saas_dev";

async function main() {
  const client = postgres(DATABASE_URL, { max: 1 });
  const db = drizzle(client);

  console.log("[seed] start:", DATABASE_URL);

  // 1. lab-management app（V014 抽出）
  await db
    .insert(s.apps)
    .values({
      code: "lab-management",
      name: "实验室管理系统",
      description: "M02-M06 业务实体（合同 / 样品 / 检测 / 审计 / 字典）",
      icon: "flask",
      sortOrder: 10,
      status: "active",
      clientId: "lab-management-prod-client",
      redirectUris: [
        "https://lab-react.xiangru.uk/login",
        "https://lab-vue.xiangru.uk/login",
        "https://lab-nextjs.xiangru.uk/login",
        "http://localhost:5200/login",
        "http://localhost:5201/callback",
        "http://localhost:5202/login",
        "http://localhost:5203/login",
      ],
      scopes: ["openid", "profile", "email"],
      grantTypes: ["authorization_code", "refresh_token"],
      isFirstParty: true,
    })
    .onConflictDoNothing({ target: s.apps.code });

  // 2. lab-management menu 骨架（V015 抽出；最少可运行结构）
  await db.execute(sql`
    INSERT INTO menus (app_id, parent_id, code, name, path, icon, type, sort_order, status)
    SELECT id, NULL, 'm02-contracts', '合同管理', '/contracts', 'file-text', 'group', 100, 'active'
    FROM apps WHERE code = 'lab-management'
    ON CONFLICT (app_id, code) DO NOTHING;

    INSERT INTO menus (app_id, parent_id, code, name, path, icon, type, sort_order, status)
    SELECT id, NULL, 'm03-samples', '样品管理', '/samples', 'beaker', 'group', 200, 'active'
    FROM apps WHERE code = 'lab-management'
    ON CONFLICT (app_id, code) DO NOTHING;

    INSERT INTO menus (app_id, parent_id, code, name, path, icon, type, sort_order, status)
    SELECT id, NULL, 'm04-inspection', '检测能力', '/inspection', 'shield-check', 'group', 300, 'active'
    FROM apps WHERE code = 'lab-management'
    ON CONFLICT (app_id, code) DO NOTHING;
  `);

  // 3. 家族 fixture（V016 抽出；saas-react msw 同源 seed 数据）
  // 这里只放 platform-level 应用（erp / crm 等）；具体由 scripts/seed-db.mjs 灌
  console.log("[seed] OK (基础 fixtures 已就位；具体家族数据由 scripts/seed-db.mjs)");

  await client.end();
}

main().catch((err) => {
  console.error("[seed] FATAL:", err);
  process.exit(1);
});
