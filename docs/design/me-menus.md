# me/menus 装配设计映射 — M04.F04.I08 （已废段镜像豁免，9/7 迁移前快照）

> **2026-09-07 模块重组**：旧 M09.F03.I01-I04 合并到 **M04.F04.I08** 单 I（端到端渲染逻辑）。
> 迁移表见 [function-tree.md §0.x](../functions/function-tree.md#0x-模块重组迁移记录2026-09-07)。

## 1. 设计映射（已上线 ID）

新结构下 me/menus 端到端渲染逻辑收敛到一个 I：**M04.F04.I08**。

| 子项 ID | 端点 | 设计要点 | 对应合同 |
|---|---|---|---|
| **M04.F04.I08** | `GET /me/menus?clientId=...` | 必须 saas session cookie；未登录返 401（与 **M04.F03** 同款 session 强约束）；roleIds → menuIds JOIN → 菜单树装配 → 按 app.code 分组 → 父链补全 | `tsp/routes/me.tsp:23 @route /menus @get myMenus` |

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
