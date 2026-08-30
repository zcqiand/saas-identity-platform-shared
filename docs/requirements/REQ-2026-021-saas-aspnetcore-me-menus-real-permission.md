# REQ-2026-021 saas-aspnetcore /me/menus 真权限过滤 (role_menu_grants JOIN)

| 项 | 值 |
|---|---|
| 提出人 | contract-test 88/0/8 绿但生产一致性分析（2026-08-31 sequence-2 探索） |
| 提出日期 | 2026-08-31 |
| 优先级 | P0（生产安全一致性 bug，4 后端行为漂移） |
| 状态 | 已评审（本会话就实施；本仓即时响应当前 placeholder 缺陷） |
| 关联 PLAN | [PLAN-2026-002](../plans/PLAN-2026-002-m09-i04-my-menus-real-permission.md) |

## 1. 需求描述

> saas-aspnetcore `/api/v1/me/menus` 是 placeholder：返 tenant 所有 active menu，
> 不接 role_menu_grants JOIN。任何用户看见所有 active 菜单，权限被绕过。
> 同时 saas-springboot 已 ahead-of-plan 真实现。

**理解**：

contract-test 已 88/0/8 通过，但 msw fixture-driven oracle 让 4 后端对**测试用户**返**同一组 menus**，跟真权限逻辑脱钩。生产时：

- 用户 alice 是某租户的某角色，能看到的菜单 = `SELECT menus WHERE id IN (grants)`；
- ASP.NET placeholder 给的是 `SELECT menus WHERE status = active`（全表）。
- 给得多（无授权的菜单也在响应里）。

后果：
1. 前端拿 menus 后，按 isMenuVisible check role — 后端多给前端少展示，安全但**backend contract 错**
2. 直接调 API 的非浏览器客户端（lab 后端 server-to-server）能取到未授权菜单（**信息泄露**）
3. 多租户场景：ASP.NET 返回的是全库 active（包括其他 tenant 的），混租户（**严重**）

## 2. 目标

- `/api/v1/me/menus` 真接 role_menu_grants JOIN：只返当前 user 在当前 tenant 因其 role 授权的 menus（含父链补全）
- 行为与 saas-springboot `MeService.getMyMenus()` 对齐
- 按 app.code 分组输出 `Map<appCode, List<EffectiveMenuNode>>`

## 3. 不做什么

- 不改 OpenAPI 契约（已定 — `Record<appCode, EffectiveMenuNode[]>`）
- 不动 msw/nextjs（M96 仓 contract-test 是 fixture 兼容即可）
- 不接 Phase 5 全部 M09 子项（仅 I02/I03/I04 + I01 状态升级）
- 不写 M09.F01 (角色菜单授权查询) / M09.F02 (角色菜单授权设置) — 后续 PLAN

## 4. M09.F03 子项状态

| 子项 | 状态 | 说明 |
|---|---|---|
| M09.F03.I01 (session 校验) | 规划 → **已上线** | saas-aspnetcore MeController L94-101 实质实现 Bearer + saas session 校验；之前仅是状态没标 |
| M09.F03.I02 (roleIds → menuIds) | 规划 → **已上线** | 本 PLAN T-1 实施 |
| M09.F03.I03 (树装配 + 父链) | 规划 → **已上线** | 现有 placeholder 已有父链补全逻辑，T-1 把 input 改 JOIN |
| M09.F03.I04 (app.code 分组) | 规划 → **已上线** | 现有 placeholder 已有 byApp + app.code 映射，T-1 保留 |
