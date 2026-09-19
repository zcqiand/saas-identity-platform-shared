# me/menus 与菜单管理 设计映射 — M04.F04 / M00.F04

> **2026-09-07 模块重组**：旧审计菜单段（I01-I04）合并到 **M04.F04.I08** 单 I（端到端渲染逻辑）。
> 迁移表见 [function-tree.md §0.x](../functions/function-tree.md#0x-模块重组迁移记录2026-09-07)。
> 2026-09-19（Task 3.5）解除 H1「已废段镜像豁免」，本文件恢复计入 design_refs，并补齐 M04.F04.I01-I07 菜单 CRUD 与 M00.F04 角色菜单授权锚点。

## 1. 设计映射（已上线 ID）

### 1.1 菜单 CRUD 与结构（client-menus.tsp）

| 子项 ID | 端点 | 设计要点 | 对应合同 |
|---|---|---|---|
| **M04.F04.I01** | `GET /clients/{clientId}/menus` | 扁平菜单节点列表；tenant-scoped（menu 行挂 client + tenant 边界） | `tsp/routes/client-menus.tsp:8 @get listSysMenus` |
| **M04.F04.I02** | `POST /clients/{clientId}/menus` | 创建菜单节点；CreateSysMenuRequest 携带 client 边界，父指针可空（根节点用固定零 UUID 约定） | `tsp/routes/client-menus.tsp:12 @post createSysMenu` |
| **M04.F04.I03** | `GET /clients/{clientId}/menus/{menuId}` | 菜单详情（单节点） | `tsp/routes/client-menus.tsp:16 @get getSysMenu` |
| **M04.F04.I04** | `PATCH /clients/{clientId}/menus/{menuId}` | 更新菜单元数据（名称/路径/图标/排序权重） | `tsp/routes/client-menus.tsp:21 @patch updateSysMenu` |
| **M04.F04.I05** | `DELETE /clients/{clientId}/menus/{menuId}` | 删除菜单节点；子节点语义由实现层约束（先删子或拒绝） | `tsp/routes/client-menus.tsp:30 @delete deleteSysMenu` |
| **M04.F04.I06** | `PUT /clients/{clientId}/menus/{menuId}/reorder` | 兄弟节点重排序 | `tsp/routes/client-menus.tsp:35 @put reorderSysMenus` |
| **M04.F04.I07** | `PATCH /clients/{clientId}/menus/{menuId}/parent` | 父节点移动（层级迁移）；移动后父链补全仍按 client_id 限界 | `tsp/routes/client-menus.tsp:44 @patch moveSysMenu` |

#### 数据模型（sys_menu）

`sys_menu`（ADR-0025 schema-first：`src/db/schema.ts` ↔ DB `public.sys_menu`）：

| 列 | 类型 | 说明 |
|---|---|---|
| id | uuid | PK，默认 uuid_generate_v4() |
| client_id | varchar(64) | NOT NULL，FK oauth_client.client_id（cascade；字符串列，非行 UUID） |
| parent_id | uuid | NOT NULL，默认零 UUID（`ROOT_MENU_ID` = `00000000-0000-0000-0000-000000000000`，根节点约定） |
| title | varchar(64) | NOT NULL |
| type | smallint | NOT NULL；0=directory / 1=menu / 2=button（契约 enum `SysMenuType`） |
| path | varchar(255)? | 可空 |
| component | varchar(255)? | 可空 |
| perms | varchar(128)? | 可空 |
| icon | varchar(128)? | 可空 |
| sort_order | integer | NOT NULL，默认 0 |
| status | smallint | NOT NULL，默认 1 |
| created_at | timestamptz | NOT NULL，默认 CURRENT_TIMESTAMP |
| — | — | 附加：idx_sys_menu_client_parent(client_id, parent_id) + idx_sys_menu_client_type(client_id, type) |

### 1.2 角色菜单授权（tenant-role-menus.tsp，admin 入口在 M00.F04）

| 子项 ID | 端点 | 设计要点 | 对应合同 |
|---|---|---|---|
| **M00.F04.I03** | `PUT /tenants/{tenantId}/roles/{roleId}/menus` | 整批设置角色菜单（关系行幂等全量替换；非 upsert 累积） | `tsp/routes/tenant-role-menus.tsp:17 @put setSysRoleMenus` |
| **M00.F04.I04** | `DELETE /tenants/{tenantId}/roles/{roleId}/menus` | 清空角色全部菜单授权（角色登录后 me/menus 不再渲染该角色菜单） | `tsp/routes/tenant-role-menus.tsp:26 @delete clearSysRoleMenus` |

#### 数据模型（sys_role_menu）

`sys_role_menu`（ADR-0025 schema-first：`src/db/schema.ts` ↔ DB `public.sys_role_menu`）：

| 列 | 类型 | 说明 |
|---|---|---|
| role_id | uuid | PK 组成，FK sys_role.id（cascade） |
| menu_id | uuid | PK 组成，FK sys_menu.id（cascade） |
| — | — | 附加：primaryKey(role_id, menu_id) + idx_sys_role_menu_menu_id(menu_id) |

> API 返回 shape 是聚合视图 `RoleMenuGrant{roleId, tenantId, menuIds, updatedAt}`（2026-09-10 I20 契约对齐方案 C）：tenantId 取 `sys_role.tenant_id`，updatedAt 取 `sys_role.updated_at`（写路径 touch）；行表只存 (role_id, menu_id) 关系行。

### 1.3 端到端渲染（me/menus）

新结构下 me/menus 端到端渲染逻辑收敛到一个 I：**M04.F04.I08**。

| 子项 ID | 端点 | 设计要点 | 对应合同 |
|---|---|---|---|
| **M04.F04.I08** | `GET /me/menus?clientId=...` | 必须 saas session cookie；未登录返 401（与 **M04.F03** 同款 session 强约束）；roleIds → menuIds JOIN → 菜单树装配 → 按 app.code 分组 → 父链补全 | `tsp/routes/me.tsp:26 @route /menus @get myMenus` |

## 2. 数据流（端到端）

```
[1. 请求 GET /me/menus 带 saasSession cookie]
    ↓
[2. MeController.Menus 校验 session → 取 userId, tenantId, roleIds]
    ↓
[3. SQL: SELECT menu_id FROM role_menu_grants
        WHERE role_id IN (roleIds) AND tenant_id = ?]
    ↓
[4. SQL: SELECT * FROM menus
        WHERE id IN (menuIds) AND tenant_id = ? AND status = 'active']
    ↓
[5. 父链补全：递归查 ancestors，确保每节点的父链完整]
    ↓
[6. 按 app_id 分组 → 取 apps.code 作 key]
    ↓
[7. 输出: { "lab-management": [menus...], "erp": [menus...] }]
```

## 3. 设计决策

### 3.1 父链补全（避免菜单断链）

只返 menuIds 对应的节点会让 UI 拿不到中间层级（如「设置」→ 「账户」→ 「密码」，用户仅有「密码」权限但 UI 拿不到「设置」和「账户」节点 → 菜单挂载位置丢失）。

**解决**：递归查 ancestor chain 直到 root，把路径上所有节点都纳入响应（即使没直接授权）。**这是设计层"过度授权"**——安全风险在 backend contract 正确，前端少展示路径节点即可。

### 3.2 按 app.code 分组（不用 app.id）

历史教训（见 memory `saas-react-selection-app-stores-code-not-id`）：前端 localStorage 选 app 时存 `code`（"lab-management"），不用 UUID。响应 key 必须用 `app.code` 兼容前端选择器。

```json
// ✅ 正确
{ "lab-management": [...], "erp": [...] }

// ❌ 错（前端按 code 找不到）
{ "uuid-1": [...], "uuid-2": [...] }
```

### 3.3 session 校验在前

session 校验**先于**任何 DB 查询。session 不存在直接 401，避免泄漏菜单存在性。

## 4. 与 OAuth session 设计同源

session 体系详见 [REQ-2026-020](../requirements/REQ-2026-020-oauth-session-real-auth.md) §**M01.F04** + [oauth-architecture.md](oauth-architecture.md)。/me/menus 是 OAuth session 校验的延伸应用：登录后用户调 /me/menus → 拿自己在当前 tenant 的菜单权限视图。

## 5. 跨仓实现位置

- **saas-aspnetcore** — `MeController.Menus` (L94-101)：已接 `role_menu_grants` JOIN（修正前是 placeholder）
- **saas-springboot** — `MeService.getMyMenus()`：ahead-of-plan 真实现
- **saas-nextjs** — `app/api/me/menus/route.ts`：已实现
- **saas-msw** — `handlers/me.ts` mock：已实现

## 6. 历史教训

- **2026-08-31 sequence-2 探索**：发现 saas-aspnetcore MeController 是 placeholder，给所有 active menu（绕过 role_menu_grants）；contract-test 88/0/8 假绿因为 msw oracle fixture-driven
- **修复**：[REQ-2026-021](../requirements/REQ-2026-021-saas-aspnetcore-me-menus-real-permission.md) P0 安全一致性 bug，4 后端对齐真权限 JOIN
- **本设计文档作为该 REQ 的设计落地**
