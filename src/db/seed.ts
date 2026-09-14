// src/db/seed.ts — target platform fixtures (schema-first)
//
// This seed intentionally creates only application/menu metadata. Tenant users,
// memberships, roles and tokens are created by the owning service after login.

import { drizzle } from "drizzle-orm/postgres-js";
import postgres from "postgres";
import { sql } from "drizzle-orm";
import * as s from "./schema.js";

function requireEnv(name: string): string {
  const value = process.env[name];
  if (!value) throw new Error(`${name} env required`);
  return value;
}

const databaseUrl = requireEnv("DATABASE_URL");
const clientId = requireEnv("SEED_OAUTH_CLIENT_ID");
const clientSecret = requireEnv("SEED_OAUTH_CLIENT_SECRET");

async function main() {
  const client = postgres(databaseUrl, { max: 1 });
  const db = drizzle(client);

  console.log("[seed] start");

  await db
    .insert(s.oauthClients)
    .values({
      clientId,
      clientSecret,
      clientName: "实验室管理系统",
      grantTypes: "authorization_code,refresh_token",
      redirectUris:
        "https://lab-react.xiangru.uk/login,https://lab-vue.xiangru.uk/login,https://lab-nextjs.xiangru.uk/login,http://localhost:5200/login,http://localhost:5201/callback,http://localhost:5201/login,http://localhost:5202/login,http://localhost:5203/login",
      scopes: "openid,profile,email",
      autoApprove: true,
    })
    .onConflictDoNothing({ target: s.oauthClients.clientId });

  await db.execute(sql`
    INSERT INTO sys_menu
      (client_id, parent_id, title, type, path, icon, perms, sort_order, status)
    VALUES
      (${clientId}, '00000000-0000-0000-0000-000000000000'::uuid, '合同管理', 1, '/contracts', 'file-text', 'contract:read', 100, 1),
      (${clientId}, '00000000-0000-0000-0000-000000000000'::uuid, '样品管理', 1, '/samples', 'beaker', 'sample:read', 200, 1),
      (${clientId}, '00000000-0000-0000-0000-000000000000'::uuid, '检测能力', 1, '/inspection', 'shield-check', 'inspection:read', 300, 1)
    ON CONFLICT DO NOTHING;
  `);

  console.log("[seed] OK");
  await client.end();
}

main().catch((error: unknown) => {
  console.error("[seed] FATAL:", error);
  process.exit(1);
});
