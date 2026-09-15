// scripts/seed-db.mjs - 把本仓 seeds/*.json 灌到 PG 库。
//
// 2026-09-15 自 saas-identity-platform-nextjs/scripts/seed-db.mjs 迁入
//（msw 剔除 Phase 1），种子源从 msw sibling 改为本仓 seeds/（拷贝自 saas-msw src/seeds）。
//
// 背景：contract-test live（start-family.sh）与本地 dev 共用 saas_dev；
// 表由 shared 仓 db:migrate（ADR-0025 drizzle journal）建好但空。本脚本读
// 本仓 seeds JSON（家族 fixture 真源），做字段映射后按 FK 顺序灌入。
//
// saas 侧语义声明（与 lab 侧 seed-db 的 upsert 不同，勿混）：
// 默认先 TRUNCATE 全部业务表 RESTART IDENTITY CASCADE，再灌。不引入 upsert ——
// 2026-09-10 重写时已定此契约：saas 种子含 OAuth client/菜单等配置型数据，
// 全量重灌才是正确幂等。
//
// 2026-09-10 重写 — 9/7 shared schema pivot（ADR-0025/0028）后 12 表换成
// OAuth 中心模型（oauth_client / sys_user / tenant / tenant_member / ...），
// 旧脚本灌的 12 张旧表（users/apps/api_keys/audit_events...）已不存在。
// 映射约定（msw fixture 旧模型 → 新 schema）：
//   tenants   → tenant            code→tenant_key, status "active"→1
//   users     → sys_user          全局自然人（无 tenantId/roleIds 列）；
//                                 password 灌 "plain:dev123456"（家族 dev 约定，
//                                 与 aspnetcore/nextjs login 的 Phase 5 校验对齐）
//   roles     → sys_role          code→role_code；client_id 取该租户订阅的
//                                 首个 client（fixture 未按 client 分域，全挂
//                                 lab-management）；is_preset=true
//   memberships → tenant_member + tenant_member_role（roleIds 拆关联表）
//   apps      → oauth_client      client_id 列 = app code（家族约定：
//                                 oauth_client.client_id 是字符串 code 非 UUID）
//   menus     → sys_menu          parentId null→零 UUID；type group/page→1/2
//                                 （nextjs menus route 的映射，aspnetcore 同）
//   role-menu-grants → sys_role_menu（roleId × menuIds 拆行）
//   tenant_application：每租户 × lab-management 一行（固定可读 UUID）
//
// 已废弃的 fixture（api-keys/audit-events/permissions/role-permissions）
// 随 SSOT b749c18 域下线不再灌。
//
// 幂等：默认先 TRUNCATE 全部业务表 RESTART IDENTITY CASCADE，再灌。可重跑。
//
// env 契约：DATABASE_URL 必填（postgresql:// 形态），缺失 fail-fast 退出 1。
// （原 nextjs 版硬编码 saas_dev 兜底连接串 —— 禁 env 默认值兜底硬规则 +
//  口令字面量出库，迁移时改为 fail-fast，与 lab 侧同款、ADR-0019。）
//
// 用法：
//   DATABASE_URL=postgresql://... node scripts/seed-db.mjs

import { createRequire } from "node:module";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { readFileSync } from "node:fs";

const __dirname = dirname(fileURLToPath(import.meta.url));
const SHARED_ROOT = resolve(__dirname, "..");
// 种子单源：本仓 seeds/（拷贝自 saas-msw src/seeds，msw 剔除 Phase 1）
const SEEDS_DIR = resolve(SHARED_ROOT, "seeds");

// pg 已在 shared devDependencies（migrate-db 同款 driver）
const require = createRequire(resolve(SHARED_ROOT, "package.json"));
const pg = require("pg");

const ZERO_UUID = "00000000-0000-0000-0000-000000000000";
// 家族默认 client（fixture 的租户订阅都指向 lab-management）
const DEFAULT_CLIENT_ID = "lab-management";
// tenant_application 固定 id：可读 UUID（0000..-d0..-..-..-000N，N=租户序号）
const TENANT_APP_ID_PREFIX = "00000000-0000-0000-0000-d0000000000";

const UUID_RE =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

// ── 任意 id -> 合法 uuid（合法则透传，否则 fail-fast）───────────────────────
// 种子 ID 已收敛为可读 UUID 字面量（原 shared V016 约定，msw JSON 同步改写），
// 正常路径下每个 id 都已是合法 UUID，resolveId 就是透传。塞回非 UUID 可读串
// 立刻报错 —— 静默哈希正是当年四套 ID 体系并存的成因。
function resolveId(id) {
  if (id && UUID_RE.test(id)) return id;
  throw new Error(
    `[seed-db] 种子 id 不是合法 UUID: ${JSON.stringify(id)}\n` +
      `  不要用可读串当 id —— 它会导致 msw / PG / 各后端 id 分叉。`,
  );
}

function loadJson(name) {
  try {
    return JSON.parse(readFileSync(resolve(SEEDS_DIR, name), "utf-8"));
  } catch (err) {
    throw new Error(
      `seed '${name}' not found / unreadable in ${SEEDS_DIR}: ${err.message}`,
    );
  }
}

// status → smallint：oauth_client.json 已契约化为 number（1=active, 0=disabled, 2=suspended）
// 直接透传；tenant/user 等仍是 msw 字符串（ADR-0032 四值：1=active, 2=invited, 3=suspended, 0=disabled；
// 与 src/lib/member-roles.ts MEMBER_STATUS_TO_SMALLINT 同源）
const statusToSmallint = (s) =>
  typeof s === "number" ? s : s === "active" ? 1 : s === "invited" ? 2 : s === "suspended" ? 3 : 0;
// msw menu type → sys_menu.type smallint（与 nextjs/aspnetcore 一致：group=1 page=2）
const menuTypeToSmallint = (t) => (t === "directory" ? 1 : t === "menu" ? 2 : 3);

async function main() {
  // 无兜底，缺了就炸（禁 env 默认值兜底，ADR-0019）
  const DATABASE_URL = process.env.DATABASE_URL;
  if (!DATABASE_URL) {
    console.error("[seed-db] DATABASE_URL env required");
    process.exit(1);
  }

  const client = new pg.Client({
    connectionString: DATABASE_URL,
    connectionTimeoutMillis: 10000,
  });

  async function insertAll(table, columns, rows) {
    const colList = columns.join(", ");
    const placeholders = columns.map((_, i) => `$${i + 1}`).join(", ");
    const sql = `INSERT INTO ${table} (${colList}) VALUES (${placeholders})`;
    for (const row of rows) {
      await client.query(sql, row);
    }
  }

  try {
    console.log(
      `[seed-db] 连接 ${DATABASE_URL.replace(/:[^:@/]+@/, ":***@")} ...`,
    );
    await client.connect();
    console.log("[seed-db] 已连接。");

    const tenants = loadJson("tenant.json");
    const users = loadJson("sys_user.json");
    const roles = loadJson("sys_role.json");
    const memberships = loadJson("tenant_member.json");
    const apps = loadJson("oauth_client.json");
    const menus = loadJson("sys_menu.json");
    const roleMenuGrants = loadJson("sys_role_menu.json");

    console.log(
      `[seed-db] 读入 seeds：tenants=${tenants.length} users=${users.length} ` +
        `roles=${roles.length} memberships=${memberships.length} ` +
        `apps=${apps.length} menus=${menus.length} role_menu_grants=${roleMenuGrants.length}`,
    );

    // ── 幂等：清空业务表（不动 __drizzle_migrations tracking 表）────────────
    // RESTART IDENTITY CASCADE 沿 FK 级联清，顺序无关
    await client.query(
      `TRUNCATE TABLE
         oauth_access_token, oauth_code, oauth_refresh_token,
         sys_role_menu, sys_menu, sys_role,
         tenant_member_role, tenant_member, tenant_application,
         sys_user, oauth_client, tenant
       RESTART IDENTITY CASCADE`,
    );
    console.log("[seed-db] 已清空业务表（RESTART IDENTITY CASCADE）。");

    // 1. tenant（fixture 字段已对齐契约 tenantKey → DB tenant_key；settings 列已随 pivot 删除）
    await insertAll(
      "tenant",
      ["id", "tenant_key", "name", "status", "created_at", "updated_at"],
      tenants.map((t) => [
        resolveId(t.id), t.tenantKey, t.name, statusToSmallint(t.status),
        t.createdAt, t.updatedAt,
      ]),
    );
    console.log(`[seed-db] tenant: ${tenants.length}`);

    // 2. oauth_client（client_id 列 = clientId，业务 code 形如 "lab-management"；clientSecret 透传 fixture）
    await insertAll(
      "oauth_client",
      [
        "id", "client_id", "client_secret", "client_name",
        "grant_types", "redirect_uris", "scopes",
        "access_token_validity", "refresh_token_validity",
        "auto_approve", "status", "created_at", "updated_at",
      ],
      apps.map((a) => [
        resolveId(a.id), a.clientId, a.clientSecret, a.clientName,
        (a.grantTypes ?? []).join(","), (a.redirectUris ?? []).join(","),
        (a.scopes ?? []).join(","),
        7200, 2592000,
        a.isFirstParty === true, statusToSmallint(a.status),
        a.createdAt, a.updatedAt,
      ]),
    );
    console.log(`[seed-db] oauth_client: ${apps.length}`);

    // 3. sys_user（全局自然人；password = 家族 dev 约定 plain:dev123456）
    await insertAll(
      "sys_user",
      ["id", "username", "password", "email", "mobile", "status", "created_at", "updated_at"],
      users.map((u) => [
        resolveId(u.id), u.username, "plain:dev123456", u.email, null,
        statusToSmallint(u.status), u.createdAt, u.updatedAt,
      ]),
    );
    console.log(`[seed-db] sys_user: ${users.length}`);

    // 4. sys_role（2026-09-11 fixture 对齐契约字段 roleCode/roleName/clientId/isPreset/status）
    await insertAll(
      "sys_role",
      [
        "id", "tenant_id", "client_id", "role_code", "role_name",
        "description", "is_preset", "status", "created_at", "updated_at",
      ],
      roles.map((r) => [
        resolveId(r.id), resolveId(r.tenantId), r.clientId ?? DEFAULT_CLIENT_ID,
        r.roleCode, r.roleName, r.description ?? null, r.isPreset ?? true,
        r.status ?? 1, r.createdAt, r.updatedAt,
      ]),
    );
    console.log(`[seed-db] sys_role: ${roles.length}`);

    // 5. tenant_member + tenant_member_role（membership 拆两表；roleIds 进关联表）
    await insertAll(
      "tenant_member",
      ["id", "tenant_id", "user_id", "member_name", "is_owner", "status", "created_at", "updated_at"],
      memberships.map((m) => {
        const user = users.find((u) => u.id === m.userId);
        return [
          resolveId(m.id), resolveId(m.tenantId), resolveId(m.userId),
          user?.username ?? null,
          m.roleIds?.length === 1 && m.roleIds[0].endsWith("00000000001"), // 首角色是 admin 视为 owner
          statusToSmallint(m.status), m.joinedAt, m.joinedAt,
        ];
      }),
    );
    const memberRoleRows = memberships.flatMap((m) =>
      (m.roleIds ?? []).map((rid) => [resolveId(m.id), resolveId(rid)]),
    );
    await insertAll("tenant_member_role", ["member_id", "role_id"], memberRoleRows);
    console.log(
      `[seed-db] tenant_member: ${memberships.length}, tenant_member_role: ${memberRoleRows.length}`,
    );

    // 6. tenant_application（每租户订阅 DEFAULT_CLIENT_ID 至 2027 年底；固定可读 UUID）
    await insertAll(
      "tenant_application",
      ["id", "tenant_id", "client_id", "status", "expire_time", "created_at"],
      tenants.map((t, i) => [
        `${TENANT_APP_ID_PREFIX}${i + 1}`, resolveId(t.id), DEFAULT_CLIENT_ID,
        1, "2027-12-31T23:59:59Z", t.createdAt,
      ]),
    );
    console.log(`[seed-db] tenant_application: ${tenants.length}`);

    // 7. sys_menu（parentId null→零 UUID；sys_menu.client_id = app clientId，业务 code 形）
    const appCodeById = new Map(apps.map((a) => [a.id, a.clientId]));
    await insertAll(
      "sys_menu",
      [
        "id", "client_id", "parent_id", "title", "type",
        "path", "component", "perms", "icon", "sort_order", "status", "created_at",
      ],
      menus.map((m) => [
        resolveId(m.id),
        appCodeById.get(m.clientId) ?? DEFAULT_CLIENT_ID,
        m.parentId ? resolveId(m.parentId) : ZERO_UUID,
        m.title, menuTypeToSmallint(m.type),
        m.path ?? null, null, null, m.icon ?? null,
        m.sortOrder ?? 0, 1, m.createdAt,
      ]),
    );
    console.log(`[seed-db] sys_menu: ${menus.length}`);

    // 8. sys_role_menu（role-menu-grants 拆行；fixture grants 只覆盖 acme admin，
    //    其余租户的 admin 角色补挂该 client 全部菜单，保证 me/menus 非空可比）
    const grantedRoleIds = new Set(roleMenuGrants.map((g) => g.roleId));
    const extraGrantRows = [];
    for (const r of roles) {
      if (grantedRoleIds.has(r.id)) continue;
      if (r.roleCode !== "admin") continue;
      for (const m of menus) {
        if (appCodeById.get(m.clientId) === DEFAULT_CLIENT_ID) {
          extraGrantRows.push([resolveId(r.id), resolveId(m.id)]);
        }
      }
    }
    const grantRows = [
      ...roleMenuGrants.flatMap((g) =>
        (g.menuIds ?? []).map((mid) => [resolveId(g.roleId), resolveId(mid)]),
      ),
      ...extraGrantRows,
    ];
    await insertAll("sys_role_menu", ["role_id", "menu_id"], grantRows);
    console.log(
      `[seed-db] sys_role_menu: ${grantRows.length} (fixture ${grantRows.length - extraGrantRows.length} + 补齐 ${extraGrantRows.length})`,
    );

    // ── 验证 count ────────────────────────────────────────────────────────────
    const tables = [
      "tenant", "oauth_client", "sys_user", "sys_role",
      "tenant_member", "tenant_member_role", "tenant_application",
      "sys_menu", "sys_role_menu",
    ];
    console.log("\n[seed-db] 验证（各表行数）：");
    for (const t of tables) {
      const { rows } = await client.query(
        `SELECT count(*)::int AS c FROM ${t}`,
      );
      console.log(`  ${t.padEnd(26)} ${rows[0].c}`);
    }
    console.log("\n[seed-db] ✅ 灌库完成。");
  } catch (err) {
    console.error("\n[seed-db] ERROR:", err.message);
    if (err.position) console.error("  位置（字节）:", err.position);
    process.exit(1);
  } finally {
    await client.end().catch(() => {});
  }
}

// 仅 CLI 直跑时执行（被测试 import 时不连库、不触发 DATABASE_URL fail-fast）
const isMain =
  process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url);
if (isMain) {
  main();
}
