 

# 架构全景与三叉戟隔离 — saas-identity-platform

> 2026-09-19（Task 3.5）解除 H1「已废段镜像豁免」：本文件 2026-09-07 重组后正文已全部指向新结构 ID，恢复计入 design_refs。

> 给读者一个 30 秒读懂的家族全景图。本文件是
> [function-tree.md §0](../functions/function-tree.md#架构全景与权限隔离) 的设计真源，
> 也是 [target-ddl-permission-isolation.md](target-ddl-permission-isolation.md) 的姊妹篇。
>
> **2026-09-07 模块重组**：本文件 §3-§6 的 F/I 引用按新结构更新（迁移表见
> [function-tree.md §0.x](../functions/function-tree.md#0x-模块重组迁移记录2026-09-07)）。
> 旧 ID 全部保留作迁移对齐锚点；消费仓合同测试按新 ID 替换。
>
> 配套 ADR：[ADR-0013](../../../docs/adr/0013-saas-oauth-skip-user-auth.md)（OAuth session 强制）/
> [ADR-0019](../../../docs/adr/0019-business-identity-no-fallback.md)（业务身份兜底禁）/
> [ADR-0026](../../../docs/adr/0026-target-ddl-tenant-client-rbac.md)（目标 DDL tenant/client/RBAC 隔离）/
> REQ-2026-020 / REQ-2026-024 / REQ-2026-026

## 1. 全景图（实体 × 关系）

```text
                       ┌─────────────────────────┐
                       │      应用 / 客户端       │
                       │      oauth_client       │
                       └────────────┬────────────┘
                                    │
            ┌───────────────────────┼───────────────────────┐
            │ (1:N 定义)            │ (1:N 订阅)            │ (1:N 颁发)
            ▼                       ▼                       ▼
┌──────────────────────┐ ┌──────────────────────┐ ┌─────────────────────────┐
│   菜单/权限元数据      │ │    租户应用订阅表     │ │      访问令牌表           │
│       sys_menu       │ │  tenant_application  │ │   oauth_access_token    │
└──────────┬───────────┘ └──────────┬───────────┘ └────────────┬────────────┘
           │ (1:N 勾选)            │ (N:1 归属)                 │ (携带 tenant_id)
           ▼                       ▼                           │
┌──────────────────────┐ ┌──────────────────────┐              │
│  角色-菜单关联表       │ │        租户表        │              │
│    sys_role_menu     │ │        tenant        │              │
└──────────▲───────────┘ └──────────┬───────────┘              │
           │ (N:1 授权)             │ (1:N 建立架构)            │
           │                        ▼                          │
┌──────────────────────┐ ┌──────────────────────┐              │
│      租户角色表       │ │    租户成员关系表      │   ───────────┤
│       sys_role       │ │    tenant_member     │              │
└──────────▲───────────┘ └──────────┬───────────┘              │
           │ (N:1 赋权)              │ (N:1 归属)               │
           ▼                        │                          │
┌──────────────────────┐            │                          │
│   成员-角色关联表      │            │                          │
│  tenant_member_role  │            │                          │
└──────────────────────┘            ▼                          ▼
                          ┌──────────────────────┐  ┌─────────────────────────┐
                          │      全局用户表       │ ─│       刷新令牌表         │
                          │       sys_user       │  │   oauth_refresh_token   │
                          └──────────────────────┘  └─────────────────────────┘
```

**读图要点**：中心是三类边界实体（应用 / 租户 / 用户），其余表按关系箭头与中心相连。
任何 join 都不能跨过这三类边界——这是后续「三叉戟」要守护的不变量。

## 2. 三大核心隔离维度（三叉戟）

### 2.1 应用维度（`client_id`）— 隔离静态元数据

| 隔离对象           | 落表             | 备注                                   |
| ------------------ | ---------------- | -------------------------------------- |
| 静态菜单与页面路由 | `sys_menu`     | 必带`client_id`；CRM 菜单不出现在 HR |
| 角色定义作用域     | `sys_role`     | 作用域 =`(tenant_id, client_id)`     |
| OAuth client 身份  | `oauth_client` | 平台级                                 |

> "公司 A 的 CRM 销售经理"和"公司 A 的 HR 招聘主管"是不同的角色行。

### 2.2 租户维度（`tenant_id`）— 隔离动态业务与权限

| 隔离对象   | 落表                                            | 备注                         |
| ---------- | ----------------------------------------------- | ---------------------------- |
| 租户根     | `tenant`                                      | 平台 admin 维度              |
| 应用订阅   | `tenant_application`                          | 决定租户付费/开通了哪些 app  |
| 角色       | `sys_role`                                    | 租户私有                     |
| 数据上下文 | `oauth_access_token` (`tenant_id` 强制携带) | 网关据此做后端微服务数据隔离 |

> 角色是租户私有的：公司 A 配置的权限勾选，绝对不影响公司 B。

### 2.3 用户维度（`user_id` + `member_id`）— 隔离自然人与组织身份

| 隔离对象   | 落表                   | 备注                           |
| ---------- | ---------------------- | ------------------------------ |
| 自然人账号 | `sys_user`           | 全局唯一；只邮箱/手机/密码     |
| 租户成员   | `tenant_member`      | 同一自然人在不同租户下是不同行 |
| 角色授权   | `tenant_member_role` | M:N，租户私有的角色映射        |

> 小明（`user_id=101`）既是公司 A 的员工（`tenant_member_id=201`），又是公司 B 的顾问（`tenant_member_id=305`）。两个身份的权限完全独立。

## 3. 运行时整体数据流转链路

> 与 function-tree.md §0.3 / flow-function-map.md 一一对应；这里给设计真源。

### 3.1 认证阶段（AuthN）

```text
输入账号密码  →  校验 sys_user  →  失败 5 次锁定 15min
                              ↘  成功则携带 client_id 查询
                                 tenant_application ∩ tenant_member
                                 返回 availableTenants[]
```

涉及端点：

- `POST /auth/login`（**M01.F04.I01**）— username + password → session cookie + access token
- `GET /me/tenants?clientId=...`（**M01.F03.I01**）— 当前用户在该应用下可进入的 `tenant[]`

### 3.2 切换 / 选定租户

```text
前端选 tenant_id
  → POST /me/tenants/{tenantId}/switch
    → 写 session.cookie.currentTenantId
    → 签新 access_token / refresh_token（写入三件套）
```

涉及端点：

- `POST /me/tenants/{tenantId}/switch`（**M01.F03.I02**）

### 3.3 令牌颁发（Token）

```text
业务后端 → 302 跳 saas /oauth/authorize?client_id&redirect_uri&state
  → saas authorize 必须 saas session（无 session 401）
  → 签 authorization_code + state
  → 业务 callback 用 code 换 token
    → saas /oauth/token grant_type=authorization_code
      → 写入 oauth_code + oauth_access_token + oauth_refresh_token
      → 三件套：user_id / client_id / tenant_id
  → 后续 access_token 过期 → grant_type=refresh_token 旋转
    → 旧 token 立即失效（oauth_access_token.revoked=true）
```

涉及端点：

- `POST /oauth/authorize`（**M04.F03.I01**）
- `POST /oauth/token`（`grant_type=authorization_code`）（**M04.F03.I02**）
- `POST /oauth/token`（`grant_type=refresh_token`）（**M04.F03.I02** 双 grant，合并 I03）

**安全约束**：

- authorize 必须 saas session；token 端点不强制（RFC 6749 §4.1.3 允许单独验 client_credentials）
- `user_id` 从 session 注入；不接受 body `user_id` / `tenantId` 直发（ADR-0019）
- `code` 单次消费；`oauth_code.code` UNIQUE + 单次验
- `refresh_token` 旋转；旧 token 立即撤销

### 3.4 鉴权与动态菜单（AuthZ & Menu）

```text
JWT(user_id, client_id, tenant_id)
  → tenant_member(user_id, tenant_id)
  → tenant_member_role(member_id)
  → sys_role(tenant_id, client_id)
  → sys_role_menu(role_id)
  → sys_menu(client_id)
```

涉及端点：

- `GET /me/menus?clientId=...`（**M04.F04.I08**）

**装配步骤**：

1. session 校验在前（无 session 直接 401，避免泄漏菜单存在性）
2. 从 token 三件套定位 `tenant_member`（同 `(user_id, tenant_id)` 唯一）
3. 联表拿授权菜单：roleIds → `sys_role_menu.menu_id` → `sys_menu`（限制同 `client_id`）
4. 父链补全：递归 ancestors，确保 UI 拿到完整挂载路径
5. 按 `app.code` 分组输出 `Map<appCode, EffectiveMenuNode[]>`

详见 [me-menus.md](me-menus.md) §3 设计决策。

## 4. 三叉戟对应模块与落表

- **应用维度** — 主战场模块：**M04**
  - 主要承载实体：`oauth_client` / `sys_menu` / `sys_role`(作用域)
  - 关键 F/I：**M04.F01** / **M04.F02** / **M04.F03** / **M04.F04**
- **租户维度** — 主战场模块：**M00**
  - 主要承载实体：`tenant` / `tenant_member` / `tenant_member_role` / `sys_role` / `tenant_application`
  - 关键 F/I：**M00.F01** / **M00.F02** / **M00.F03** / **M00.F04** / **M00.F05**
- **用户维度** — 主战场模块：**M01**
  - 主要承载实体：`sys_user` + session / OIDC code / token 三件套
  - 关键 F/I：**M01.F01** / **M01.F02** / **M01.F03** / **M01.F04**

### 已废弃段镜像（豁免）

> 已废弃段（M02 / M03 / M05 / M06 / M08 / M09）不参与三叉戟；内容已迁至 M00 / M01 / M04，编号冻结不再实现。这些模块在功能树中无功能行（仅历史段落名），故按 ADR-0028 豁免段声明不实现。

## 4.5 应用维护与启停设计映射 — M04.F01 / M04.F02

> 2026-09-19（Task 3.5）补齐设计锚点：应用（OAuth client）CRUD + 状态切换，
> 落表 `oauth_client`；三叉戟「应用维度」的具体端点承载见本节。

| 子项 ID | 端点 | 设计要点 | 对应合同 |
|---|---|---|---|
| **M04.F01.I01** | `GET /admin/clients` | 【平台】平台 admin 分页列出全部 OAuth 应用（page/pageSize） | `tsp/routes/admin-clients.tsp:8 @get listClients` |
| **M04.F01.I02** | `POST /admin/clients` | 注册新 OAuth client（含 redirect_uri 白名单与密钥生成；密钥仅回指纹不返明文） | `tsp/routes/admin-clients.tsp:12 @post createClient` |
| **M04.F01.I03** | `GET /admin/clients/{clientId}` | 应用完整配置（寻址用 clientId 字符串列，非行 UUID；密钥回指纹） | `tsp/routes/admin-clients.tsp:16 @get getClient` |
| **M04.F01.I04** | `PATCH /admin/clients/{clientId}` | 修改名称 / redirect_uri 白名单 / 允许的 scope 等 | `tsp/routes/admin-clients.tsp:21 @patch updateClient` |
| **M04.F01.I05** | `DELETE /admin/clients/{clientId}` | 移除应用并级联吊销该 client 名下所有 access/refresh token | `tsp/routes/admin-clients.tsp:26 @delete deleteClient` |
| **M04.F01.I06** | `GET /clients/{clientId}` | 公共端点匿名可读 clientId/name/logo（供登录页应用选择；无 session 要求） | `tsp/routes/clients.tsp:8 @get getClient` |
| **M04.F02.I01** | `PATCH /admin/clients/{clientId}/status` | 【平台】切换 status；禁用后该 client 的所有 OAuth/token 端点立即拒绝 | `tsp/routes/admin-clients.tsp:31 @patch setClientStatus` |

#### 数据模型（oauth_client）

`oauth_client`（ADR-0025 schema-first：`src/db/schema.ts` ↔ DB `public.oauth_client`）：

| 列 | 类型 | 说明 |
|---|---|---|
| id | uuid | PK，默认 uuid_generate_v4() |
| client_id | varchar(64) | NOT NULL，unique `uk_oauth_client_id`（应用寻址主键——clientId 字符串，非行 UUID） |
| client_secret | varchar(255) | NOT NULL（API 只回指纹，不返明文） |
| client_name | varchar(128) | NOT NULL |
| grant_types | varchar(255) | NOT NULL |
| redirect_uris | text | NOT NULL（redirect_uri 白名单） |
| scopes | varchar(255)? | 可空 |
| access_token_validity | integer | NOT NULL，默认 7200 |
| refresh_token_validity | integer | NOT NULL，默认 2592000 |
| auto_approve | boolean | NOT NULL，默认 false |
| status | smallint | NOT NULL，默认 1（**M04.F02.I01** 切换目标；禁用后 OAuth/token 端点立即拒绝） |
| created_at / updated_at | timestamptz | NOT NULL，默认 CURRENT_TIMESTAMP |

## 5. 安全失败语义（与 [target-ddl-permission-isolation.md](target-ddl-permission-isolation.md) §安全失败语义 同源）

| 缺失字段              | 行为                | 严禁                                         |
| --------------------- | ------------------- | -------------------------------------------- |
| `sub` / `user_id` | 401 UNAUTHORIZED    | 兜底到 demo 字面量（`USER-A` / `alice`） |
| `tenant_id`         | 403 TENANT_REQUIRED | 兜底到`TENANT-001`                         |
| `client_id`         | 403 CLIENT_REQUIRED | 兜底到`lab-management`                     |
| `role_code`         | 403 ROLE_REQUIRED   | 兜底到`admin` / `viewer`                 |

详见 ADR-0019。

## 6. 跨仓实现位置

| 仓              | 角色              | 关键文件                                                                                            |
| --------------- | ----------------- | --------------------------------------------------------------------------------------------------- |
| saas-aspnetcore | 后端              | `OauthController.cs` / `MeController.cs` / `AuthController.cs` / `TenantUsersController.cs` |
| saas-springboot | 后端              | `OauthController.java` / `MeController.java` / `AuthController.java`                          |
| saas-nextjs     | 前端 + 自闭环后端 | `app/api/oauth/{authorize,token}/route.ts` / `app/api/me/{menus,tenants}/route.ts`              |
| saas-msw        | Mock 后端         | `handlers/oauth.ts` / `handlers/me.ts` / `handlers/auth.ts`                                   |
| saas-vue        | 前端              | `LoginPage.vue` + `OauthCallbackPage.vue`                                                       |
| saas-react      | 前端              | `LoginPage.tsx` + `OauthCallbackPage.tsx`                                                       |

## 7. 决策与历史教训

| 教训                        | 表现                    | 修法（本设计地标的护栏）                        |
| --------------------------- | ----------------------- | ----------------------------------------------- |
| body 直发 tenantId / userId | 跳过真认证              | ADR-0019；token 注入 session；L0.no_fallback 门 |
| role_ids 数组 4 后端 drift  | roleIds=[] vs 真值      | authoritative 在`tenant_member.role_ids` JOIN |
| saas session 不强制         | Phase 5 mock 跳过真认证 | ADR-0013 路线 A；authorize 必须 session         |
| 跨租户硬切换                | 数据泄漏                | TenantGuard 中间件 + session.currentTenantId    |
| 菜单按 app.id 分组          | 前端按 code 找不到      | me-menus §3.2 按`app.code` 分组              |
| parent 链未补全             | UI 菜单挂载路径丢失     | me-menus §3.1 递归祖先补全                     |

## 8. 与其他文档的关系

- [`function-tree.md §0`](../functions/function-tree.md#架构全景与权限隔离) — 入口地图；本文件是真源
- [`target-ddl-permission-isolation.md`](target-ddl-permission-isolation.md) — DDL 边界 + 授权 join 真源
- [`oauth-architecture.md`](oauth-architecture.md) — **M04.F03** OAuth 流程细节
- [`me-menus.md`](me-menus.md) — **M04.F04.I08** 当前用户菜单装配细节
- [`user-model.md`](user-model.md) — **M00.F02** 租户成员设计
- [`flow-function-map.md`](flow-function-map.md) — 端点 × F/I 完整映射（旧 ID，按 §0.x 对账）
| [REQ-2026-026](../requirements/REQ-2026-026-target-ddl-tenant-client-rbac.md)     | 需求侧约束；本设计是落地       |
| [父仓 ARCHITECTURE §3.1](../../../docs/ARCHITECTURE.md#31-双-ssotapi--db-schema) | 父仓视角；本文件是本仓 zoom-in |
