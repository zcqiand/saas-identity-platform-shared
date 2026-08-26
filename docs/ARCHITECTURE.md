# saas-identity-platform-shared Architecture

> shared 仓 = saas-identity-platform 全家桶的**双 SSOT**。它不写业务代码，
> 它把 TypeSpec + SQL DDL 编译成中间产物，给 7 个消费仓 codegen。
>
> 范围：本文档只描述本仓的架构（结构 / 边界 / 数据流 / 决策）。
> 编码细则见 [docs/conventions/](conventions/)，父仓全景见
> [../../../docs/ARCHITECTURE.md](../../../docs/ARCHITECTURE.md)。

---

## 0. 阅读路径

| 你是… | 直接看 |
|---|---|
| 新人，要 10 分钟搞懂本仓 | §1 → §2 → §4（核心模块） |
| 想改 API 契约 | §3.1 → §4.4（核心流程）→ [父仓 §5.1](../../../docs/ARCHITECTURE.md#51-改一次契约--三端同步codegen-链) |
| 想改 DB schema | §3.2 → §3.4（TypeSpec→PG 映射）→ §4.4 → [sql/README.md](../sql/README.md) |
| 想加一个新消费仓 | §5（契约消费矩阵）→ [父仓 §2.2](../../../docs/ARCHITECTURE.md#22-14-个子仓的角色矩阵) |
| 想问「为什么这么设计」 | §6（决策索引）→ [父仓 ADR-0007](../../../docs/adr/0007-shared-sql-ssot.md) |

---

## 1. 角色与定位

### 1.1 家族里的位置

```
xr-code-suite/  （suite 父仓）
└── output/
    ├── saas-identity-platform-shared  ← 本仓：契约源（API + DB schema）
    ├── saas-identity-platform-msw         ← B 强度 mock 后端（消费 OpenAPI）
    ├── saas-identity-platform-react       ← 前端 1/3（orval）
    ├── saas-identity-platform-vue         ← 前端 2/3（orval）
    ├── saas-identity-platform-nextjs      ← 前端 3/3 + 全栈后端（Drizzle + node-pg-migrate）
    ├── saas-identity-platform-springboot  ← 后端 1/2（JPA + Flyway-off）
    └── saas-identity-platform-aspnetcore  ← 后端 2/2（EF Core + InMemoryStore）
```

本仓是 saas 家族 7 个仓中**唯一一个不写业务代码**的仓。它不发 HTTP、不连 DB（除了 CI 验证），
只产契约产物供 6 个消费仓使用。

### 1.2 双 SSOT 角色

shared 仓同时承担两份**单点真理源**（[ADR-0007](../../../docs/adr/0007-shared-sql-ssot.md)）：

| 维度 | 真源 | 中间产物 | 消费方 codegen |
|---|---|---|---|
| **API 契约** | `tsp/main.tsp` + `tsp/{models,routes}/*.tsp` | `generated/openapi/openapi.yaml`（`npm run emit:openapi`） | NSwag（C#）/ openapi-generator（Java）/ orval（TS） |
| **DB schema** | `sql/migrations/V<NNN>__<desc>.sql`（Flyway 风格，手人写） | 各后端 `cp` 到自己的 `db/migration/` / `Migrations/` / `migrations/` | ORM 反射（Spring Data / EF / Drizzle） |

**双 SSOT 的代价**：改 schema 必须 *同步* 改 API（契约不能引用不存在的列）；改 API 必须
*同步* 改 schema（不能让契约列比 schema 列多）。这是一份契约的两个面，必须同时翻。

### 1.3 双向契约：输入 / 输出

**输入**（来自开发者手写，仓内修改）：

| 路径 | 作用 | 修改方 |
|---|---|---|
| `tsp/main.tsp` | TypeSpec 入口（namespace + 共享 model + import） | 人 |
| `tsp/models/*.tsp` | 9 个核心实体（Tenant/User/Role/App/...） | 人 |
| `tsp/routes/*.tsp` | 13 个 endpoint 集合（auth/admin/me/tenant-*/oauth/...） | 人 |
| `sql/migrations/V*.sql` | 9 个 Flyway 风格 DDL 文件 + 1 个 seed | 人 |

**输出**（仓内 emit 或直接 cp，供消费仓读取）：

| 路径 | 内容 | 谁读 |
|---|---|---|
| `generated/openapi/openapi.yaml` | OpenAPI 3.0 spec（70KB+，git tracked） | 6 个消费仓 codegen |
| `sql/migrations/V*.sql` | PostgreSQL DDL | 3 个后端仓 + `sync-db.mjs` 直接推 dev DB |
| `tsp/main.tsp`（package.json `exports` `.` 暴露） | TypeSpec 入口 | 跨仓 IDE 跳转（不导包） |
| `tests/snapshots/openapi.test.ts` | OpenAPI 路径 lock 测试 | shared 自检 |

**明确不输出**（[CLAUDE.md](../CLAUDE.md) §2 硬规则）：

- TS / Java / C# / Kotlin / Swift / Dart 客户端代码（语言产物下放给消费仓自己 generate）
- runtime npm 依赖（仅 `@typespec/*` dev）
- 手写 `generated/openapi/openapi.yaml`（必须 `tsp compile`）
- `package.json` `exports` 暴露语言路径（如 `./api-client`）

---

## 2. 目录骨架

> 完整 1-2 层树状图。所有路径都经过 `ls` 验证存在。

```
saas-identity-platform-shared/
├── CLAUDE.md                          ← 入口：禁业务代码 / 禁语言产物 / 仅 devDep
├── README.md                          ← 技术栈 + Deepwiki MCP 说明
├── package.json                       ← devDep: @typespec/* + vitest; runtime: 无
├── package-lock.json
├── tsconfig.json                      ← TS 配置（typecheck 用）
├── tspconfig.yaml                     ← TypeSpec 配置（emitter-output-dir = generated/openapi）
├── openapitools.json                  ← @openapitools/openapi-generator-cli jar 配置
├── vitest.config.ts                   ← include: tests/**/*.test.ts; FnReporter 收 trace
├── main.tsp                           ← TypeSpec 入口（re-export，避免双重真源）
├── tsp/                               ← ★ TypeSpec 源码（API 契约真源）
│   ├── main.tsp                       ←   @server + @service + @route("/api/v1")
│   ├── models/                        ←   9 个核心实体
│   │   ├── tenant.tsp
│   │   ├── user.tsp
│   │   ├── role.tsp
│   │   ├── membership.tsp
│   │   ├── api-key.tsp
│   │   ├── app.tsp                    ←   平台级 App（菜单承载 + OAuth client）
│   │   ├── menu.tsp
│   │   ├── role-menu-grant.tsp
│   │   └── audit-event.tsp
│   └── routes/                        ←   13 个 endpoint 集合
│       ├── auth.tsp                   ←   M03 密码登录/登出
│       ├── admin-tenants.tsp          ←   M00 平台 admin 租户 CRUD
│       ├── admin-apps.tsp             ←   M04 平台 admin App CRUD
│       ├── admin-app-menus.tsp        ←   M08 菜单 CRUD
│       ├── me.tsp                     ←   /me + /me/tenants
│       ├── apps.tsp                   ←   /apps/{code} 公共端点
│       ├── tenant-users.tsp           ←   M01 tenant-scoped 用户
│       ├── tenant-roles.tsp           ←   M02 角色 CRUD
│       ├── tenant-role-menus.tsp      ←   M09 角色菜单授权
│       ├── tenant-api-keys.tsp        ←   M05 API Key 生命周期
│       ├── tenant-audit.tsp           ←   M06 审计事件查询
│       └── oauth.tsp                  ←   M04.F03 OAuth authorize/token
├── sql/                               ← ★ PostgreSQL DDL（DB schema 真源）
│   ├── README.md                      ←   命名约定 + 类型映射表
│   └── migrations/                    ←   Flyway 风格 V<NNN>__<desc>.sql
│       ├── V001__init_tenants.sql
│       ├── V002__init_users_memberships.sql
│       ├── V003__init_roles_permissions.sql
│       ├── V004__init_api_keys.sql
│       ├── V005__init_oauth_apps_menus.sql
│       ├── V006__init_audit_events.sql
│       ├── V007__indexes.sql
│       ├── V008__users_role_ids_and_drop_redundant_index.sql
│       └── V014__seed_lab_mgmt_app.sql  ← 跨家族 seed（lab-mgmt OAuth client）
├── generated/                         ← ★ emit 产物（git tracked；给消费仓读）
│   ├── openapi/
│   │   └── openapi.yaml               ←   70KB OpenAPI 3.0 spec（git tracked）
│   └── ts/                            ←   空目录（占位；不产出 TS 客户端）
├── scripts/                           ← ★ 工具脚本
│   ├── sync-db.mjs                    ←   从 SSOT 直推 PG（dev 全量 / --incremental 增量）
│   └── codegen/
│       └── emit-openapi.ts            ←   自举 + tsp compile + 写到 generated/openapi/
├── tests/                             ← ★ 本仓 L4 测试
│   ├── fnReporter.ts                  ←   vitest reporter；fn-ID → .state/trace.json
│   ├── sql.replay.test.ts             ←   L4 门禁：fresh DB 重放 V001..V00N
│   └── snapshots/
│       └── openapi.test.ts            ←   OpenAPI 路径 lock 测试
├── .harness/                          ← suite 门禁读取的项目自描述
│   ├── stack.json                     ←   L1=tsp compile / L3=tsc / L4=vitest
│   └── common.lock.json               ←   suite-version lock
├── .openapi-generator/                ← openapi-generator-cli 配置（消费仓 codegen 用）
│   ├── dotnet-config.json
│   └── java-config.json
├── .state/                            ← gate + trace 产物（gitignored）
├── .mcp.json                          ← Deepwiki MCP 注册
├── .github/                           ← CI workflow
└── docs/
    ├── ARCHITECTURE.md                ← 本文件
    ├── functions/function-tree.md     ← BASE tree（M0x..F0y 22 条）
    ├── adr/                           ← 空（本仓暂无特有 ADR；详见 §6）
    ├── design/                        ← 空
    ├── conventions/                   ← 空
    └── requirements/                  ← 空
```

**目录骨架关键观察**：

- `src/` 目录**空**——明确不做业务代码（连 placeholder 都没有）；
- `generated/` 是 emit 落点，**git tracked**（区别于多数 TS 项目的 `.gitignore` 习惯）；
- `.openapi-generator/` 不在本仓做 codegen，是给消费仓放的配置模板；
- `tests/` 只有 3 个文件，覆盖 `tsp` emit 产物 + `sql` DDL replay 两条主链；
- `docs/adr/` `docs/design/` `docs/conventions/` `docs/requirements/` 全空——所有决策集中在父仓 `docs/adr/`。

---

## 3. 核心模块

### 3.1 `tsp/main.tsp` —— TypeSpec 入口

文件 `tsp/main.tsp` 是 API 契约的**唯一入口**：

```typescript
import "@typespec/http";
import "@typespec/openapi3";
import "@typespec/rest";

// 9 个 model（按业务实体切分）
import "./tsp/models/tenant.tsp";
import "./tsp/models/user.tsp";
import "./tsp/models/role.tsp";
import "./tsp/models/audit-event.tsp";
import "./tsp/models/api-key.tsp";
import "./tsp/models/membership.tsp";
import "./tsp/models/app.tsp";
import "./tsp/models/menu.tsp";
import "./tsp/models/role-menu-grant.tsp";

// 12 个 route 集合（按域切分）
import "./tsp/routes/auth.tsp";
import "./tsp/routes/admin-tenants.tsp";
import "./tsp/routes/me.tsp";
import "./tsp/routes/tenant-users.tsp";
import "./tsp/routes/tenant-roles.tsp";
import "./tsp/routes/tenant-api-keys.tsp";
import "./tsp/routes/tenant-audit.tsp";
import "./tsp/routes/admin-apps.tsp";
import "./tsp/routes/apps.tsp";
import "./tsp/routes/admin-app-menus.tsp";
import "./tsp/routes/tenant-role-menus.tsp";
import "./tsp/routes/oauth.tsp";

using TypeSpec.Http;

@server("https://api.example.com", "Production")
@service
@route("/api/v1")
namespace Saas.Identity.Shared;
```

**关键声明**：

| 装饰器 | 含义 |
|---|---|
| `@server(url, "Production")` | OpenAPI `servers` 段；示例 URL |
| `@service` | OpenAPI `info.title` / `version`；TS 客户端生成基础 |
| `@route("/api/v1")` | 所有 endpoint 共享前缀；与 `openapi.test.ts` 的 lock 一致 |
| `using TypeSpec.Http` | 引入 `@get` / `@post` / `@route` / `@tag` 等装饰器 |

**共享辅助 model**：

- `ErrorResponse { code, message, details? }` —— 全局 `@error`；所有 op 返回错误时共用
- `Page<T> { items, page, pageSize, total }` —— 分页包装
- `CreatedResponse { id }` —— 创建响应（UUID）

### 3.2 `tsp/models/` —— 9 个核心实体

按业务域切分，**每个文件一个或一组强相关 model**：

| 文件 | model | 对应 SQL 表 | 业务域 |
|---|---|---|---|
| `tenant.tsp` | `Tenant` / `TenantStatus` / `TenantSettings` | `tenants` | M00 多租户根 |
| `user.tsp` | `User` / `UserStatus` | `users` | M01 tenant-scoped 用户 |
| `membership.tsp` | `TenantMembership` / `MembershipStatus` | `tenant_memberships` | M01 跨租户视图 |
| `role.tsp` | `Role` | `roles` + `permissions` + `role_permissions` | M02 角色 + 权限矩阵 |
| `api-key.tsp` | `ApiKey` / `ApiKeyStatus` | `api_keys` | M05 tenant-scoped Key |
| `app.tsp` | `App` / `AppPublicInfo` / `AppStatus` / `OAuthGrantType` / `CreateAppRequest` / `UpdateAppRequest` | `apps` | M04 平台级（菜单承载 + OAuth client） |
| `menu.tsp` | `Menu` / `MenuType` / `MenuStatus` | `menus` | M08 应用下树形菜单 |
| `role-menu-grant.tsp` | `RoleMenuGrant` | `role_menu_grants` | M09 角色菜单授权 |
| `audit-event.tsp` | `AuditEvent` / `AuditAction` | `audit_events` + `audit_retention_policies` | M06 审计 |

**App 是双重身份的复合实体**（详见 `tsp/models/app.tsp` 头注释）：

> App —— 平台级统一实体。承担三类职责：
>   1. 业务应用（菜单承载）：M08 菜单挂在 appId 下
>   2. OAuth client：M04 OAuth 流（authorize/token）按 clientId 识别
>   3. 租户订阅：M09 菜单授权按 appCode + tenantId 维度派发

**OAuth DTO** 在 `tsp/routes/oauth.tsp` 内联声明（`AuthorizeCodeRequest` / `TokenRequest` /
`TokenResponse`）—— 不污染全局 namespace。

### 3.3 `tsp/routes/` —— 13 个 endpoint 集合

| 文件 | 命名空间 | endpoint 前缀 | 功能 ID |
|---|---|---|---|
| `auth.tsp` | `Saas.Identity.Shared` | `/auth/{login,me,logout,refresh}` | M03.F01-03 |
| `admin-tenants.tsp` | `Saas.Identity.Shared` | `/admin/tenants` | M00.F01-02 |
| `admin-apps.tsp` | `Saas.Identity.Shared` | `/admin/apps` | M04.F01-02 |
| `admin-app-menus.tsp` | `Saas.Identity.Shared` | `/admin/apps/{appId}/menus` | M08.F01-02 |
| `apps.tsp` | `Saas.Identity.Shared` | `/apps/{code}`（公开） | 公开端点 |
| `me.tsp` | `Saas.Identity.Shared` | `/me` + `/me/tenants/{id}/switch` | 当前用户视图 |
| `tenant-users.tsp` | `Saas.Identity.Shared` | `/tenants/{tenantId}/users` | M01.F01-02 |
| `tenant-roles.tsp` | `Saas.Identity.Shared` | `/tenants/{tenantId}/roles` | M02.F01-02 |
| `tenant-role-menus.tsp` | `Saas.Identity.Shared` | `/tenants/{tenantId}/roles/{roleId}/menus` | M09.F01-03 |
| `tenant-api-keys.tsp` | `Saas.Identity.Shared` | `/tenants/{tenantId}/api-keys` | M05.F01 |
| `tenant-audit.tsp` | `Saas.Identity.Shared` | `/tenants/{tenantId}/audit-events` | M06.F01-02 |
| `oauth.tsp` | `Saas.Identity.Shared.OAuth` | `/oauth/{authorize,token}` | M04.F03 |

**endpoint 路径前缀约定**：

```
/api/v1                              ← main.tsp 全局
├── /auth/{login,me,logout,refresh}      ← 平台级（无 tenantId）
├── /admin/{tenants,apps,...}            ← 平台级 admin
├── /apps/{code}                         ← 公开元数据
├── /me/...                              ← 当前用户视图
├── /oauth/{authorize,token}             ← OAuth 平台级
└── /tenants/{tenantId}/...              ← tenant-scoped 业务（TenantGuard 强制）
```

**tenant-scoped 路径** = `/tenants/{tenantId}/...`：每个 endpoint 第一行必须调
`TenantGuard.VerifyPathTenant(tenantId)`（详见父仓 [§3.4](../../../docs/ARCHITECTURE.md#34-dev-jwt-对称契约) + [§5.3](../../../docs/ARCHITECTURE.md#53-后端开发springboot--aspnetcore)）。

### 3.4 `sql/migrations/` —— Flyway 风格 DDL

**命名约定**（`sql/README.md` §命名约定）：

| 项 | 规则 |
|---|---|
| 文件名 | `V<NNN>__<description>.sql`（Flyway 风格；**单调递增**；不允许修改已落地文件） |
| 表名 | 复数 snake_case（`tenants` / `users` / `tenant_memberships`） |
| 列名 | snake_case（`tenant_id` / `created_at` / `password_hash`） |
| FK 列 | `<entity_singular>_id` |
| 时间戳 | `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`；列名 `created_at` / `updated_at` / `joined_at` / `occurred_at` / `last_used_at` / `expires_at` / `revoked_at` |
| 枚举 | PG 原生 `CREATE TYPE <name> AS ENUM (...)`；值用单引号小写 snake_case |
| JSONB | 命名 `<name>`（不加 `_json` 后缀）；CHECK 约束必为 JSON object |
| 删除策略 | 默认 `ON DELETE CASCADE`；actor_user_id / target_user_id 这类审计引用 `ON DELETE SET NULL` |

**TypeSpec → PG 类型映射**（`sql/README.md` §TypeSpec → PG 类型映射）：

| TypeSpec | PG DDL | 备注 |
|---|---|---|
| `format("uuid")` | `UUID PRIMARY KEY DEFAULT uuid_generate_v4()` | 需 `CREATE EXTENSION IF NOT EXISTS "uuid-ossp"`（V001 落地） |
| `string` 带 `@minLength/@maxLength` | `VARCHAR(N)` | N 取 `@maxLength`，不带 maxLength 用 `TEXT` |
| `int32` / `int64` | `INTEGER` / `BIGINT` | — |
| `utcDateTime` | `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP` | 统一带时区 |
| `Record<unknown>` | `JSONB NOT NULL DEFAULT '{}'::jsonb` + `CHECK jsonb_typeof() = 'object'` | 应用层 mapper 做 typed parse |
| enum | `CREATE TYPE x AS ENUM (...)` | ORM 用 `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` / `MapEnum<>` 镜像 |
| `T[]` (UUID 数组) | `UUID[] NOT NULL DEFAULT ARRAY[]::UUID[]` | 不强 FK（数组内 FK PG 不支持） |
| `T[]` (string 数组) | `TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[]` | 如 `scopes` / `redirect_uris` |
| password 字段 | `VARCHAR(255)` 存散列 | bcrypt/argon2；明文只在创建响应里返回一次 |

**12 张表 + 9 个 enum 类型（V001..V008 落地）**：

| 表 | V 文件 | TypeSpec model | 备注 |
|---|---|---|---|
| `tenants` | V001 | Tenant / TenantStatus / TenantSettings | 多租户根；settings JSONB |
| `users` | V002, V008 | User / UserStatus | tenant-scoped；V008 加 `role_ids[]` 列 |
| `tenant_memberships` | V002 | TenantMembership / MembershipStatus | 跨租户视图；role_ids[] |
| `roles` | V003 | Role | tenant-scoped |
| `permissions` | V003 | (推导) | 平台级 permission 字典 |
| `role_permissions` | V003 | (推导) | M:N 关系表 |
| `api_keys` | V004 | ApiKey / ApiKeyStatus | tenant-scoped；secret_hash 不可逆 |
| `apps` | V005 | App / AppStatus / OAuthGrantType | 平台级；菜单承载 + OAuth client |
| `menus` | V005 | Menu / MenuType / MenuStatus | 树形；parent_id 自引用 |
| `role_menu_grants` | V005 | RoleMenuGrant | tenant-scoped M:N；整批 PUT |
| `audit_events` | V006 | AuditEvent / AuditAction | insert-only；metadata JSONB |
| `audit_retention_policies` | V006 | (推导 from M06.F02) | 一租户一行 |

```
9 个 PG 原生 enum 类型：
V001: tenant_status
V002: user_status / membership_status
V004: api_key_status
V005: app_status / oauth_grant_type / menu_type / menu_status
V006: audit_action
```

**V014 是特殊迁移**——`V014__seed_lab_mgmt_app.sql`：

- seed `apps` 表里固定 UUID `11111111-1111-1111-1111-111111111111`（client_id 同值）的 lab-mgmt 应用
- 新建 `oauth_codes` 表（Phase 6 真 OAuth；替代 saas-nextjs 进程内 oauth-store）
- 跨家族 seed：让 3 个 saas 后端（nextjs / aspnetcore / springboot）共用同一 app 记录
- 强制 `CREATE TABLE IF NOT EXISTS oauth_codes`（**拆雷点**——2026-08-26 lab V014/V015 撞号事故同款雷）
- `ON CONFLICT (client_id) DO NOTHING`：saas-nextjs 已 seed 的 `apps` 行不被覆盖

### 3.5 `generated/openapi/` —— emit 产物

`generated/openapi/openapi.yaml` 是 `npm run emit:openapi` 的产物，**git tracked**：

| 项 | 值 |
|---|---|
| OpenAPI 版本 | 3.0.0（`@typespec/openapi3` 1.0 默认；TS namespace 注解未开 3.1） |
| 文件大小 | ~70KB（路径锁 + 12 model + 60+ op） |
| 服务器 | `https://api.example.com`（占位） |
| 入口前缀 | `/api/v1` |
| 含路径（lock 测试覆盖） | `/api/v1/auth/login`、`/api/v1/admin/tenants`、`/api/v1/tenants/{tenantId}/users`、`/api/v1/admin/apps/{appId}/menus`、`/api/v1/oauth/authorize`、`/api/v1/oauth/token`、... |

**为什么不入仓 ignore**：consumer 仓 codegen 读它，写入 `.gitignore` 会强制所有 consumer
clone 一次就跑 `gen-shared.sh`，链长 + 易踩"指针新+产物旧"窗口。

`generated/ts/` 目录**空**——CLAUDE.md §2 禁止 shared 仓产 TS 客户端。占位目录防止
TypeScript 编译 path 解析时找不到 `generated/ts`。

### 3.6 `scripts/sync-db.mjs` —— 从 SSOT 直推 PG

`scripts/sync-db.mjs` 是把 `sql/migrations/V*.sql` **直接推到目标 PG 库**的脚本。
不走三仓 `gen-shared.sh` 中转，便于 dev 期快速重建 / 增量迁移。

**两种模式**：

| 模式 | 行为 | 何时用 |
|---|---|---|
| **默认（全量重建）** | 库必须为空；按字典序跑全部 `V*.sql`；防误覆盖 | dev 首次建库；CI clean build |
| **`--incremental`** | 基于 `public.__schema_migrations` tracking 表只跑未记录的 V；冷启动 baseline 自动 mark | 加了字段；维护期 |

**关键设计**：

- **pg driver 借用**：shared 仓禁 npm runtime 依赖，从 `/app/node_modules/pg`（runtime 容器）/
  `../saas-identity-platform-nextjs/node_modules/pg`（dev）借（同 lab-shared v0.2.7 套路）
- **tracking 表** `__schema_migrations`：双下划线前缀排列表首，标明元数据
- **冷启动 baseline**：tracking 空但库已有业务表 → 假定现有 schema = 全部当前 V 的结果
- **连接配置**：DATABASE_URL 优先（ADR-0009 标准 PG 连接串）；缺失回退 PG_* 单独 env
- **fallback**：默认 `100.79.128.25:5432/saas_dev`

**安全约束**：

- 全量模式不 DROP SCHEMA；库非空即中止（需手动 `DROP SCHEMA public CASCADE`）
- 每个 V 文件包事务；成功才 `INSERT __schema_migrations`（Flyway 一致）
- 验证 `EXPECTED_TABLES`（12 张）+ `EXPECTED_ENUMS`（9 个）匹配

### 3.7 `scripts/codegen/emit-openapi.ts` —— emit 入口

```typescript
// 自举：消费方 CI fresh clone 拉 shared 但不 install
// npx tsp 在 node_modules/.bin/ 找不到 tsp 时会去 npm 拉 tsp@0.0.1 (Microsoft 老包)
// 这里检测 local tsp 二进制，缺就 npm install (含 devDep: @typespec/* 全套)
const tspBin = resolve(root, "node_modules/.bin/tsp");
if (!existsSync(tspBin)) {
  console.log("[emit-openapi] bootstrapping shared deps ...");
  execSync("npm install --no-audit --no-fund", { cwd: root, stdio: "inherit" });
}

execSync("npx tsp compile .", { cwd: root, stdio: "inherit" });
```

### 3.8 `tests/` —— L4 门禁

| 文件 | 覆盖 | 跳过条件 |
|---|---|---|
| `tests/sql.replay.test.ts` | fresh DB 重放 `V001..V00N`；断言 12 表 + 9 enum；FK cascade 行为 | `PG_REPLAY_SKIP=1` 或 lab-nextjs 未 npm install |
| `tests/snapshots/openapi.test.ts` | `openapi.yaml` 存在 + 关键路径 lock | 永不跳（默认 L4） |
| `tests/fnReporter.ts` | vitest reporter；fn-ID → `.state/trace.json` | reporter，不算测试 |

`fnReporter.ts` 是 shared 仓特有的 fn-ID 提取器（父仓 ADR-0002 要求所有仓禁止手写
`trace.json`，必须 `trace_cmd` 产）。它用正则 `\bM\d{2}(?:\.F\d{2}(?:\.I\d{2})?)?\b`
从 test name 抓 `M00.F01` / `M01.F02.I03` 等 ID，写到 `.state/trace.json`。

---

## 4. 核心流程

### 4.1 改 API 契约 → 三端同步

```
1. [本仓] 改 tsp/main.tsp 或 tsp/{models,routes}/*.tsp
   + docs/functions/function-tree.md 同步加 F（先改功能后改代码；ADR-0003）
   ↓ git commit + push

2. [本仓] npm run build
   → emit-openapi.ts 自举 + tsp compile .
   → 写到 generated/openapi/openapi.yaml（git tracked）
   gate: python scripts/gate.py -p saas-identity-platform-shared
   ├─ L1: npx tsp compile . --no-emit
   ├─ L3: npx tsc --noEmit
   └─ L4: npx vitest run（含 openapi snapshot lock + sql replay）
   ↓ exit 0

3. [消费仓] bash scripts/gen-shared.{sh,ts}
   a) (cd ../shared && npm run emit:openapi)        ← 重新 emit
   b) 本地 codegen：
      - orval（react/vue/nextjs）
      - openapi-generator generate -g java（springboot）
      - nswag run（aspnetcore）
   ↓ git commit + push（每个仓各自 tag）

4. [父仓] git update-index --add --cacheinfo 160000,<NEW_HASH>,output/<proj>
   chore(submodule): 推进 <proj> 指针
   ↓ git push

5. [suite] python scripts/gate.py --all
   ↓ 15 项目全绿
```

详见父仓 [§5.1](../../../docs/ARCHITECTURE.md#51-改一次契约--三端同步codegen-链)。

### 4.2 改 DB schema → 三端同步

```
1. [本仓] 新增 sql/migrations/V00N+1__<desc>.sql（不修改已落地 V 文件）
   + 对应 tsp/models/*.tsp 同步加列（双 SSOT 必须同步翻）
   + docs/functions/function-tree.md 同步加 F（先功能后代码；ADR-0003）
   ↓ git commit + push

2. [本仓] 手动或 sync-db.mjs --incremental 验证
   node scripts/sync-db.mjs --incremental  PG_DATABASE=saas_test
   gate: npx vitest run tests/sql.replay.test.ts
   ↓ exit 0

3. [后端仓] bash scripts/gen-shared.sh
   a) emit openapi（即使没改 API 也走一遍，保证 yaml 最新）
   b) 本地 codegen（重新生成 Controller）
   c) cp shared/sql/migrations/V*.sql 到 db/migration/
      → 含 cmp abort 防护（防 2026-08-26 lab V014/V015 撞号事故重演）
   ↓ git commit + push

4. [本仓] gen-shared 成功后，后端 ORM（Hibernate Entity / EF Model / Drizzle schema）
   反射新 schema；spring.flyway.enabled=true（lab）或 node-pg-migrate up（nextjs-self）执行
```

**关键检查点**：

- V 文件**只能新增**，不能修改（Flyway 一致）；改字段类型只能新增 `V00N+1` 写 ALTER
- 双 SSOT 同步：改 SQL 必须同步改 TypeSpec model；改 TypeSpec model 必须同步改 SQL
- `gen-shared.sh` 拷 SQL 前必须 `cmp` 目标：内容不同且已存在 → FATAL abort

### 4.3 SQL 重放验证（CI 默认跑）

```
npx vitest run tests/sql.replay.test.ts
  ↓
beforeAll:
  DROP SCHEMA IF EXISTS public CASCADE
  CREATE SCHEMA public
  CREATE EXTENSION IF NOT EXISTS "uuid-ossp"
  for f in V001..V00N: client.query(readFile(f))
  ↓
it("creates 12 expected tables")
it("creates 9 expected enum types")
it("tenants has settings JSONB column")
it("users has unique (tenant_id, email) constraint")
it("FK cascade works: tenant deletion removes users")  ← 真实插删 + CASCADE 验证
  ↓
exit 0 = green
```

**借用 pg driver**：同 `sync-db.mjs`，从 `../lab-management-system-nextjs/node_modules/pg` 借。
借不到 → `it.skip`（环境友好；CI 默认跑）。

### 4.4 门禁链（本仓 L1-L4；无 L0/L5）

```
python scripts/gate.py -p saas-identity-platform-shared
  ↓
L1 格式       ← 项目声明
  └─ npx tsp compile . --no-emit
  └─ exit 1 = 修复 tsp/main.tsp 语法
  ↓
L3 类型/编译  ← 项目声明
  └─ npx tsc --noEmit
  └─ exit 1 = 补全类型
  ↓
L4 测试       ← 项目声明 + trace_cmd
  ├─ npx vitest run（含 sql.replay + openapi snapshot + 其它）
  └─ trace_env: TRACE_MAP=1 → FnReporter 写 .state/trace.json
  └─ exit 1 = 先让测试变绿
  ↓
exit 0 = 全绿；1 = 按 fix 提示回代码改；2 = 契约/环境问题（停下问人）
```

L0 结构完整性 / L5 引用完整性由 suite 拥有，**本仓不声明**（父仓 ADR-0001）。

---

## 5. 契约消费矩阵

7 个消费仓各自怎么读 `openapi.yaml` / `sql/migrations/*.sql`：

| 消费仓 | 角色 | 读 openapi.yaml | 读 sql/migrations/*.sql | 工具 |
|---|---|---|---|---|
| **saas-identity-platform-msw** | Mock 后端 | 是（handlers 派生） | 否（fixture in-memory） | `@mswjs/http-middleware` 重建 Request |
| **saas-identity-platform-react** | 前端 1/3 | 是（orval codegen） | 否 | `orval.config.ts` 读 `../shared/generated/openapi/openapi.yaml` |
| **saas-identity-platform-vue** | 前端 2/3 | 是（orval codegen） | 否 | 同 react |
| **saas-identity-platform-nextjs** | 前端 3/3 + 全栈后端 | 是（orval codegen） | 是（Drizzle schema + node-pg-migrate） | 双消费；schema emit infra 旁路 |
| **saas-identity-platform-springboot** | 后端 1/2 | 是（openapi-generator） | 是（Flyway-off；saas 仓 spring.flyway.enabled=false） | `gen-shared.sh` 拷 SQL + Maven plugin 生成 Controller |
| **saas-identity-platform-aspnetcore** | 后端 2/2 | 是（NSwag） | 是（InMemoryStore 替代；EF Migrations 待落地 ADR-0010） | `nswag run` + 手写 partial Controller |

### 5.1 codegen 工具映射

| 仓 | openapi.yaml → 客户端 | DDL → ORM |
|---|---|---|
| react / vue / nextjs（前端） | `orval` + `axios` | — |
| nextjs-self | `orval`（自指） | Drizzle schema 镜像；`node-pg-migrate up` |
| springboot | `openapi-generator-maven-plugin` | Hibernate Entity 反射；`flyway.enabled=false` 时由 shared SQL 灌入 |
| aspnetcore | `NSwag` (`aspnetcore.nswag`) | EF Model 反射；`InMemoryStore` 临时替代；`Migrations/` 待落地 |
| msw | `@mswjs/http-middleware` + 手写 handlers | — |

### 5.2 SQL 同步路径

```
shared/sql/migrations/V*.sql
  ├──→ saas-springboot/src/main/resources/db/migration/V*.sql
  │     （gen-shared.sh cmp 防护后 cp；flyway.enabled=false 时不自动执行；启动时由 shared SQL 灌入）
  ├──→ saas-aspnetcore/Migrations/V*.sql  （待落地；ADR-0010）
  ├──→ saas-nextjs/migrations/V*.sql
  │     （gen-shared.sh cp；node-pg-migrate up 自动跑）
  └──→ scripts/sync-db.mjs 直推 PG 库
        （dev 全量 / --incremental 增量；用于快速验证）
```

### 5.3 msw 仓不读 SQL

msw 仓的 fixture 是 in-memory JSON / TS（`src/seeds/*.json`），不直接读 shared SQL。
但 fixtures 的*形状*（tenant 字段、user 字段、role 关系）必须与 openapi.yaml 描述的 DTO
一致——这是软约束，**靠手写对齐**，没自动化。

---

## 6. 决策索引

> 本仓 `docs/adr/` 目录**空**——所有架构决策集中在父仓 [docs/adr/](../../../docs/adr/)。
> 本节列出与本仓强相关的 ADR：

| ADR | 主题 | 与本仓关系 |
|---|---|---|
| [0007](../../../docs/adr/0007-shared-sql-ssot.md) | **shared 仓扩到 OpenAPI.yaml + SQL DDL 双 SSOT** | 本仓核心决策；`sql/migrations/` 即由此而来 |
| [0002](../../../docs/adr/0002-trace-json-as-cross-language-anchor-contract.md) | trace.json 是跨语言锚点 | 本仓 `tests/fnReporter.ts` 实现；`TRACE_MAP=1` env 触发 |
| [0003](../../../docs/adr/0003-function-tree-requires-human-approval.md) | 功能清单变更需人批 | 本仓 `docs/functions/function-tree.md` 是 BASE tree；改 F 必须人批 |
| [0001](../../../docs/adr/0001-suite-owns-l0-and-l5.md) | suite 保留 L0/L5 门 | 本仓 `.harness/stack.json` 只声明 L1/L3/L4 |
| [0009](../../../docs/adr/0009-db-credentials-env.md) | DB 凭据走 env | 本仓 `scripts/sync-db.mjs` 走 env；fallback `100.79.128.25:saas_dev` |
| [0010](../../../docs/adr/0010-aspnetcore-ef-mirrors-sql.md) | aspnetcore EF 应镜像 SQL | 本仓 SQL 是 aspnetcore EF 真源（待落地） |
| [0008](../../../docs/adr/0008-nextjs-full-stack.md) | saas-nextjs 兼全栈 | 本仓双 SSOT（API+SQL）让 nextjs 能自闭环实现 |

**本仓特有隐含决策**（暂无独立 ADR；如需正式化则用 `adr` skill）：

| 主题 | 决策 |
|---|---|
| `package.json` `exports` 仅暴露 `"."` 和 `"./openapi"` | 不暴露语言路径（`./api-client`）；只暴露 TypeSpec 入口和 OpenAPI yaml |
| `generated/openapi/openapi.yaml` git tracked | 不入仓 ignore；避免"指针新+产物旧"窗口 |
| `sync-db.mjs` 借 pg driver | shared 仓禁 runtime npm dep；从 nextjs 仓 `node_modules/pg` 借 |
| `tests/sql.replay.test.ts` 借 pg driver（lab-nextjs） | 同上套路；环境友好（借不到 skip） |
| `sql/README.md` 列 9 enum + 12 表期望清单 | 既是文档也是 CI 断言源（与 `sync-db.mjs::verify` 共用） |
| V014 跨家族 seed | lab-mgmt OAuth client 固定 UUID；3 saas 后端共用 |

---

## 7. 术语表

| 术语 | 含义 | 详细 |
|---|---|---|
| **SSOT** | Single Source of Truth | 单一真理源；本仓承担双 SSOT（API + DB） |
| **双 SSOT** | 一仓同时承担 API 契约 + DB schema 真源 | ADR-0007 |
| **BASE tree** | 契约仓的功能清单 | 只到 F 级；消费仓在 F 镜像后加 I |
| **emit** | 从 TypeSpec 源码生成中间产物 | `npm run emit:openapi` → `generated/openapi/openapi.yaml` |
| **codegen** | 消费方从中间产物生成客户端 | orval（TS）/ openapi-generator（Java）/ NSwag（C#） |
| **V 文件** | Flyway 风格命名版本化 SQL | `V<NNN>__<desc>.sql`；单调递增；不可修改 |
| **tracking 表** | Flyway 风格 schema_migrations | 本仓用 `__schema_migrations`（双下划线前缀排列表首） |
| **baseline** | tracking 空但库已有表 → 假定现有 schema = 当前 V 集合 | sync-db.mjs 冷启动保护 |
| **tenant-scoped** | 路径带 `/tenants/{tenantId}/` 前缀 | 须 TenantGuard 校验路径 tenantId vs JWT claim |
| **TenantGuard** | 路径 tenantId vs JWT tenant_id claim 校验 | 每个 tenant-scoped endpoint 第一行调 |
| **OAuth 2.0** | RFC 6749；IdP 实现 `/oauth/{authorize,token}` 端点 | 三 saas 后端对称实现；grant_type=authorization_code / refresh_token |
| **JWT (HS256)** | RFC 7519 真签发 access token | `JwtIssuer.{java,cs,ts}` 镜像实现；`JWT_SIGNING_KEY` ≥32B env 对称 |
| **DevJwtDecoder** | dev profile bean，吃 MSW/test fixture `alg=none` token | prod 删掉 + 配 `JWT_ISSUER_URI` 走 JWKS |
| **trace.json** | 测试命中 fn-ID 的清单 | `trace_cmd` 产；本仓 `tests/fnReporter.ts` 实现 |
| **fnTest** | 测试 ID 嵌入 it 名称的模式 | `fnTest(["M01.F05.I01"], "desc", () => {...})` |
| **stack.json** | 项目自描述（栈 + 门配置） | suite 门禁读它；本仓只声明 L1/L3/L4 |
| **多仓家族** | 契约仓 + mock 仓 + N 前端 + M 后端 + 父仓 | 详见父仓 ARCHITECTURE §2.1 |
| **lab-shared 套路** | 同构仓借鉴模式 | saas-shared v0.2.x 与 lab-shared 同源；`sync-db.mjs` 与 `tests/sql.replay.test.ts` 借 pg 模式均沿用 |
| **撞号事故** | V 文件被两个仓同时落地导致冲突 | 2026-08-26 lab V014/V015；`gen-shared.sh` `cmp` 防护；V014 `CREATE TABLE IF NOT EXISTS oauth_codes` 拆雷 |

---

## 附录 A：与父仓 docs/ARCHITECTURE.md 的关系

| 父仓章节 | 本仓是否展开 | 说明 |
|---|---|---|
| §1 套件全景 | 否 | 本仓是 14 个子仓之一；不复述家族结构 |
| §2 家族拓扑 | 部分 | §5 契约消费矩阵展开"本仓 → 7 消费仓"细节 |
| §3.1 双 SSOT | **是** | 本文件 §1.2 / §3.4 / §3.5 详细展开 API+DB 双 SSOT 在本仓的具体落地 |
| §3.2 一份契约，三套 codegen | 是 | §5.1 展开"7 消费仓各自怎么 codegen" |
| §3.7 Function Tree | 是 | §3.3 / §6 ADR-0003 引用；本仓 function-tree.md 是 BASE |
| §4.1 契约仓目录骨架 | **是（更细）** | 本文件 §2 给出实际 `ls` 验证后的 1-2 层树状图 |
| §5.1 改契约 → 三端同步 | 是 | §4.1 / §4.2 拆出 API 与 DB schema 两个独立流程 |
| §5.4 门禁链 | 是 | §4.4 列出本仓实际声明的 L1/L3/L4（无 L0/L5） |
| §7 决策索引 | 是 | §6 列出本仓相关的 7 份 ADR + 本仓隐含决策 |
| §8 术语表 | 是 | §7 列出本仓特有术语（emit / V 文件 / baseline / 撞号事故等） |

**关系一句话**：父仓 ARCHITECTURE 是 suite 全景；本文件是**本仓 zoom-in**——
把父仓 §3.1 / §4.1 / §7 中所有"shared 仓相关"的论述，落到具体文件路径、具体决策点、
具体消费仓 codegen 命令上。

---

## 附录 B：典型陷阱（详见父仓 [memory/](../../../)）

| 陷阱 | 后果 | 解法（本仓相关） |
|---|---|---|
| 改 schema 不同步改 API | 契约引用不存在的列；3 端 codegen 崩 | 改 SQL 同时改 `tsp/models/*.tsp` |
| 改 API 不同步改 schema | 契约列比 schema 列多；ORM 启动崩 | 改 TypeSpec 同时改 SQL；gate L4 会用 `sql.replay` 间接验证 |
| 改 V 文件（不新增） | 违反 Flyway 风格；prod 已跑过的库不会重跑 | 必须新增 `V<NNN+1>` 写 ALTER |
| 手写 `generated/openapi/openapi.yaml` | 与 TypeSpec 不一致；下次 emit 被覆盖 | `tsp compile .` 生成；本仓 `lint` 门禁验 `tsp compile . --no-emit` |
| 借不到 pg driver 时跑 L4 死循环 | vitest hang 在 `connect()` | `it.skip` 兜底；`PG_REPLAY_SKIP=1` env 显式跳 |
| `sync-db.mjs` 全量模式覆盖非空库 | dev 数据被擦 | 库非空直接 abort；需手动 `DROP SCHEMA` |
| `gen-shared.sh` cp SQL 撞号 | 目标已存在且内容不同；目标仓启动崩 | `cmp` 防护；V014 `CREATE TABLE IF NOT EXISTS oauth_codes` 拆雷 |
| `package.json` `exports` 暴露语言路径 | 消费方意外 import TS 客户端 | 只暴露 `"."` 和 `"./openapi"`（CLAUDE.md §2 硬规则） |
| `tests/fnReporter.ts` 关掉 `TRACE_MAP=1` | `.state/trace.json` 不产；L4 维度失锚 | vitest cmd 配 `trace_env.TRACE_MAP=1`（本仓 `.harness/stack.json` 已配） |
| OpenAPI 路径 lock 改路径不更新 snapshot | L4 红 | 改路径时同步改 `tests/snapshots/openapi.test.ts` |

---

## 附录 C：快速链接

- [CLAUDE.md](../CLAUDE.md) —— 入口：禁业务代码 / 禁语言产物 / 仅 devDep
- [README.md](../README.md) —— 技术栈 + Deepwiki MCP 说明
- [sql/README.md](../sql/README.md) —— DB 持久层 SSOT 命名约定 + 类型映射
- [docs/functions/function-tree.md](../functions/function-tree.md) —— BASE tree（M0x..F0y 22 条）
- [父仓 docs/ARCHITECTURE.md](../../../docs/ARCHITECTURE.md) —— suite 全景
- [父仓 docs/adr/0007-shared-sql-ssot.md](../../../docs/adr/0007-shared-sql-ssot.md) —— 本仓核心决策
- [.harness/stack.json](../.harness/stack.json) —— suite 门禁读取的项目自描述