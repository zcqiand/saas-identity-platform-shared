# shared/sql/ — DB 持久层 SSOT

> PostgreSQL DDL 真源。三个后端（springboot JPA+Flyway / aspnetcore EF Migrations / nextjs-backend Drizzle+node-pg-migrate）从此处消费。
> 见 ADR-0007（shared 仓扩到双 SSOT）+ ADR-0010（EF Migrations CI 镜像）。

## 命名约定

- **文件名**：`V<NNN>__<description>.sql`（Flyway 风格；单调递增；不允许修改已落地文件）
- **表名**：复数 snake_case（`tenants` / `users` / `tenant_memberships`）
- **列名**：snake_case（`tenant_id` / `created_at` / `password_hash`）
- **FK 列**：`<entity_singular>_id`（`tenant_id` 指向 `tenants.id`）
- **时间戳**：`created_at` / `updated_at` / `joined_at` / `occurred_at` / `last_used_at` / `expires_at` / `revoked_at`，统一 `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
- **枚举**：PG 原生 `CREATE TYPE <name> AS ENUM (...)`；值用单引号小写 snake_case（与 TypeSpec enum 值同名）
- **JSONB 字段**：命名 `<name>`（不加 `_json` 后缀）；CHECK 约束必为 JSON object
- **删除策略**：默认 `ON DELETE CASCADE`（tenant-scoped 表跟随 tenant 一起删）；actor_user_id / target_user_id 这类审计引用 `ON DELETE SET NULL`

## TypeSpec → PG 类型映射

| TypeSpec | PG DDL | 备注 |
|---|---|---|
| `format("uuid")` | `UUID PRIMARY KEY DEFAULT uuid_generate_v4()` | 需 `CREATE EXTENSION IF NOT EXISTS "uuid-ossp"`（V001 落地） |
| `string` 带 `@minLength/@maxLength` | `VARCHAR(N)` | N 取 `@maxLength`，不带 maxLength 用 `TEXT` |
| `int32` / `int64` | `INTEGER` / `BIGINT` | |
| `utcDateTime` | `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP` | 统一带时区 |
| `Record<unknown>` / `Record<string, T>` | `JSONB NOT NULL DEFAULT '{}'::jsonb` + `CHECK jsonb_typeof() = 'object'` | 应用层 mapper 做 typed parse |
| enum | `CREATE TYPE x AS ENUM (...)` | ORM 用 `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` / `MapEnum<>` 镜像 |
| `T[]` (UUID 数组) | `UUID[] NOT NULL DEFAULT ARRAY[]::UUID[]` | 不强 FK（数组内 FK PG 不支持） |
| `T[]` (string 数组) | `TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[]` | 如 `scopes` / `redirect_uris` |
| password 字段（API 不暴露） | `VARCHAR(255)` 存散列 | bcrypt/argon2 散列；明文只在创建响应里返回一次 |

## 12 张表（V001..V008 落地）

| 表 | V 文件 | TypeSpec model | 备注 |
|---|---|---|---|
| `tenants` | V001 | Tenant / TenantStatus / TenantSettings | 多租户根；settings JSONB |
| `users` | V002, V008 | User / UserStatus | tenant-scoped；一行/(用户,租户)；V008 加 `role_ids[]` 列 |
| `tenant_memberships` | V002 | TenantMembership / MembershipStatus | 跨租户视图；role_ids[] |
| `roles` | V003 | Role | tenant-scoped |
| `permissions` | V003 | (推导) | 平台级 permission 字典 |
| `role_permissions` | V003 | (推导) | M:N 关系表 |
| `api_keys` | V004 | ApiKey / ApiKeyStatus | tenant-scoped；secret_hash 不可逆 |
| `apps` | V005 | App / AppStatus / OAuthGrantType | 平台级；菜单承载 + OAuth client |
| `menus` | V005 | Menu / MenuType / MenuStatus | 树形；parent_id 自引用 |
| `role_menu_grants` | V005 | RoleMenuGrant | tenant-scoped M:N；整批 PUT；V005 强制 tenant_id NOT NULL |
| `audit_events` | V006 | AuditEvent / AuditAction | insert-only；metadata JSONB |
| `audit_retention_policies` | V006 | (推导 from M06.F02) | 一租户一行 |

9 个 PG 原生 enum 类型：

```
V001: tenant_status
V002: user_status / membership_status
V004: api_key_status
V005: app_status / oauth_grant_type / menu_type / menu_status
V006: audit_action
```

## 各后端消费方式

| 后端 | 复制路径 | 执行 | ADR |
|---|---|---|---|
| springboot | `scripts/gen-shared.sh` 复制到 `src/main/resources/db/migration/` | Flyway 启动时自动跑 | (Phase 2) |
| aspnetcore | `scripts/gen-shared.sh` 复制到 `Migrations/` 旁 | `dotnet ef migrations add` 重生成 EF 类；`scripts/check-ef-mirrors-sql.sh` diff | ADR-0010 |
| nextjs-backend | `scripts/gen-shared.sh` 复制到 `migrations/` | `node-pg-migrate up` | ADR-0008 |

## 验证

```
# 手动重放（开发期）
psql -h <host> -U postgres -d saas_test -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
for f in shared/sql/migrations/V*.sql; do psql -h <host> -U postgres -d saas_test -f "$f"; done
psql -h <host> -U postgres -d saas_test -c "\dt"      # 应见 12 行
psql -h <host> -U postgres -d saas_test -c "\dT+"     # 应见 9 个 enum

# 自动重放（CI）
npx vitest run tests/sql.replay.test.ts   # L4 门禁
```

## 三库约定（ADR-0009）

```
saas_dev   —开发默认；DDL 自由、seed fixture 多
saas_test  —跑 Flyway migrate + Testcontainers 集成；schema 与 dev 一致
saas_prod  —生产；只读账号访问
```

切换方式（统一通过 `scripts/lib/db-env.sh`）：

```bash
DATABASE_NAME=saas_test source ../../scripts/lib/db-env.sh
# 输出 JDBC_URL / NPGSQL_CONN_STR / PG_INSTANCE_URL 三种 ORM 格式
# 各后端仓默认从 env var 读取，本地无 env 时 fallback 到 saas_dev（100.79.128.25）。
```

各后端 env var 读取约定：

| 后端 | env var | fallback |
|---|---|---|
| springboot | `DATABASE_URL` / `JDBC_URL` | `jdbc:postgresql://100.79.128.25:5432/saas_dev` |
| aspnetcore | `DATABASE_URL` / `NPGSQL_CONN_STR` | `Host=100.79.128.25;...Database=saas_dev` |
| nextjs | `DATABASE_URL` / `DATABASE_NAME` | `postgresql://postgres:...@100.79.128.25:5432/saas_dev` |

密码 URL 编码（`qiand68+++` → `qiand68%2B%2B%2B`）：postgres-js / Npgsql / JDBC 都吃 raw `+++`；postgres-js 的 URI parser 严格，需 encoded。`db-env.sh` 提供两种版本：`PG_PASSWORD`（raw）+ `PG_PASSWORD_ENCODED`。

## 注意事项

- **修改已落地 V 文件**：禁止（与 Flyway 兼容语义一致）。要改字段只能加新 `V00N+1__*.sql`（`ALTER TABLE`）
- **删表 / 改字段类型**：禁止直接 `ALTER COLUMN TYPE`，必须 `V00N+1__*.sql` 写迁移
- **每个 V 文件必须独立可执行**：不能假设前面的 V 之外的 schema 状态
- **不要写 TS / Java / C# 等应用语言代码**：本目录只承载 PostgreSQL DDL（CLAUDE.md §2 禁止项）

## 后续 phase

- Phase 2：springboot 加 `spring-boot-starter-data-jpa` + `flyway-core` + `flyway-database-postgresql`；`scripts/gen-shared.sh` 复制 SQL；11 Entity 镜像
- Phase 3：aspnetcore 加 EF Core Migrations；`scripts/check-ef-mirrors-sql.sh` 强制 diff=0
- Phase 4：nextjs-backend 加 `drizzle-orm` + `postgres-js` + `node-pg-migrate`；`scripts/gen-shared.sh` 复制 + up