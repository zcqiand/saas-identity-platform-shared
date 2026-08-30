# PLAN-2026-002 — M09.F03 saas-aspnetcore /me/menus 真权限过滤

> **状态**: 🚧 进行中 (本会话实施)
> **REQ**: [REQ-2026-021](../requirements/REQ-2026-021-saas-aspnetcore-me-menus-real-permission.md)
> **路线**: 单仓 (saas-aspnetcore), 不跨家族
> **策略**: 端口 saas-springboot `MeService.getMyMenus` (~80 LOC Java) → C# + EF Core + Npgsql。

## 上下文

ASP.NET `/api/v1/me/menus` 现实现是 placeholder：
```csharp
// MeController.cs:103
// Phase 5 占位：返回当前 tenant 所有 active menu（不接 role_menu_grants JOIN）
var menus = await _db.Menus.Where(m => m.Status == "active").ToListAsync();
```

- 任何 active 菜单全给，**不接** role_menu_grants JOIN
- 已知生产一致性 risk（vs saas-springbot 真实现）
- contract-test 88/0/8 绿但只验「前端不可区分」(fixture 对齐)，不验业务路径

saas-springboot 已在 2026-08-28 修 prod 503 时真实现 `MeService.getMyMenus()`（见 [session 探索报告]）：
- `membership.roleIds` → `role_menu_grants.menuIds` → `menus` 表 + 父链补全 → 按 `app.code` 分组

## 任务清单

### 任务 1: saas-aspnetcore MeController.Menus 端口

- **fn-ID**: M09.F03.I02 + M09.F03.I03 + M09.F03.I04
- **文件**: `src/Controllers/Implementation/MeController.cs` L103-156
- **改动**:
  - 用现有 `_db.TenantMemberships / RoleMenuGrants / Menus / Apps` DbSet
  - 链路 1:1 镜像 saas-springboot MeService.getMyMenus (line 122-205)
  - 跳过 JdbcTemplate（ASP.NET 走 EF LINQ；Npgsql 原生支持 uuid[]，无 unnest() 坑）
  - 保留现有 I01 (Bearer + session fallback, L87-101)
  - 保留现有父链补全 + 按 app.code 分组逻辑 (现 L108-155 大致正确，改 input)
  - 把 `?? Guid.Empty` → 直接 `m.ParentId` 透传 (DTO 已 [JsonIgnore] 抑制 sentinel)

### 任务 2: I01 状态升级

- **fn-ID**: M09.F03.I01 (me/menus session 校验) — 实质已在 saas-aspnetcore 实现 (MeController L94-101)
- **改动**: function-tree `规划 → 已上线`
- **REQ**: REQ-2026-021 第 4 节

### 任务 3: 单测覆盖新逻辑

- **fn-ID**: M09.F03.I02-I04 单测
- **文件**: `tests/Controllers/MeControllerTests.cs`
- **覆盖**:
  - roleIds = [] → 返空 Map
  - grantedMenuIds = [] → 返空 Map
  - 1 个 root menu + 1 个 child: 验证 Children 装配
  - 2 个 app: 验证按 app.code 分组

### 任务 4: 验证

- `dotnet build src` — 0 warn 0 err
- `dotnet test tests` — 全测绿
- 重启 saas-aspnetcore (PID 27240+ → 杀 → 启新 dll 带真 DB env)
- `CONTRACT_TARGETS=msw,aspnetcore,springboot,nextjs npx vitest run` — 仍 88/0/8 (无回归)

### 任务 5: function-tree + tree-change

- function-tree M09.F03.I01/I02/I03/I04 状态: 规划 → 已上线
- 走 `/tree-change` 留 trace

## 风险与依赖

| 风险 | 缓解 |
|---|---|
| EF Core `Where(g => roleIds.Contains(g.RoleId))` 对 uuid[] 类型 — 已用过的 ApiKey query 有 List<Guid> 直接 Contains 案例，应无问题 | 第一次跑 unit test 时验证 |
| Npgsql `uuid[]` binding for MenuIds property — 项目已有 TenantMembership.RoleIds 同样模式 OK | 已有 |
| contract-test 4 后端对同一测试用户 fixture 同返 menus（placeholder 改 JOIN 后） | 跑 contract-test 看是否仍 88/0/8 |
| 状态会漂移：spring 已 ahead-of-plan 实施但 function-tree 仍规划 | 本 PLAN 顺手升 aspnetcore 4 子项；springbot 跟踪项不在 scope |

## 串行依赖

```
T-1 saas-aspnetcore MeController.Menus 端口 ──┐
T-2 单测 (MeControllerTests.cs 新加 4 cases) ─┤── 并行 ─→ 验证
T-3 验证 (build/test/contract-test)           ─┘
T-4 function-tree + tree-change (最后)
```

T-1/2 可与 T-3 的 lint 阶段并行，因为 T-3 的 contract-test 已经 88/0/8，应不引入新失败。
