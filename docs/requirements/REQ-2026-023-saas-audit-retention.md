# REQ-2026-023 saas 租户审计日志与留存策略 （已废段镜像豁免，9/7 迁移前快照）

> **2026-09-07 模块重组注意**：本 REQ 文档是历史快照；M05 / M06 在新结构下已整体标记 **已废弃**（目标 DDL 不再包含 `api_keys` / `audit_events` / `audit_retention_policies`）。本 REQ 内容仅作为历史约束记录，不在新结构下实施。
> 迁移表见 [function-tree.md §0.x](../functions/function-tree.md#0x-模块重组迁移记录2026-09-07)。

| 项 | 值 |
|---|---|
| 提出人 | saas 家族 4 后端 × 4 前端共仓一致性（ADR-0023 P2 / 8/26 audit 列表 occurred_at DESC 家族约定） |
| 提出日期 | 2026-09-07 |
| 优先级 | P1 |
| 状态 | 已评审 |
| 关联 ADR | [0023-cross-repo-ssot-coverage-mapping.md](../../../docs/adr/0023-cross-repo-ssot-coverage-mapping.md) |

## 1. 需求描述

saas 平台每个 tenant 需要可追溯的审计能力：谁在什么时间对什么资源做了什么动作。

**现状缺口**：

1. **写端点缺副作用**：`POST /api/v1/tenants/{t}/api-keys` / `POST .../revoke` / `POST .../rotate` / `DELETE .../{k}` 等写端点目前不调用 AuditWriter → 审计事件缺失 → 安全调查/合规审计无据可查
2. **查询缺 action 过滤**：`tenant-audit.tsp:14` 已声明 `@query action?: AuditAction`，但 msw 之前缺这个查询参数实现 → contract-test L2 看似绿但 4 后端过滤行为实际不对称
3. **留存策略未定义**：audit_events 表无 TTL/清理机制 → 长期累积 → 查询性能退化 + 合规留存无依据

**目标**：

- 写端点统一调 `AuditWriter.WriteAsync(tenantId, actorUserId, action, metadata)` 副作用写入 audit_events
- 查询支持 `?action=AuditAction&actorUserId&from&to` 完整过滤，4 后端对齐 msw oracle
- 引入 `audit_retention_policies` 表（V006 已建）+ `GET/PUT /tenants/{t}/audit-events/retention` 设置留存天数
- 默认留存 90 天（合规下限）+ 显式设置走 PUT

## 2. 验收标准

| 编号 | 场景（给定） | 操作（当） | 预期（则） |
|---|---|---|---|
| AC-1 | tenant T 调 `POST /api/v1/tenants/T/api-keys` | 创建 API key 成功 | audit_events 增 1 行 `action="api_key_created"`，actor_user_id 来自 saas session |
| AC-2 | tenant T 调 `POST .../api-keys/{k}/revoke` | 吊销 | audit_events 增 1 行 `action="api_key_revoked"` |
| AC-3 | tenant T 调 `POST .../api-keys/{k}/rotate` | 轮换 | audit_events 增 2 行（revoke 旧 + create 新），原子事务 |
| AC-4 | tenant T 调 `DELETE .../api-keys/{k}` | 物理删除 | audit_events **不**增行（说明待补：物理删除按设计不留痕，与 revoke 软删并存） |
| AC-5 | tenant T 调 `GET /api/v1/tenants/T/audit-events?action=api_key_created` | 列表过滤 | 只返 action=api_key_created 的事件；msw/aspnetcore/springboot/nextjs 行为一致 |
| AC-6 | tenant T 调 `GET .../audit-events?actorUserId={u}&from=2026-09-01T00:00:00Z&to=2026-09-07T23:59:59Z` | 多条件过滤 | 4 后端返同一组（msw oracle 验证） |
| AC-7 | tenant admin 调 `GET /tenants/T/audit-events/retention` | 读 | 200 `{retentionDays: 90}`（默认） |
| AC-8 | tenant admin 调 `PUT /tenants/T/audit-events/retention {retentionDays: 180}` | 写 | 200 `{retentionDays: 180}`；audit_retention_policies 表对应行 upsert |
| AC-9 | tenant T retentionDays=30，当前事件 occurred_at 早于 today-30d | 列表 / 导出 | 这些事件被过滤（应用层或定时清理，由实现仓选） |
| AC-10 | 4 后端 contract-test `?action=` 过滤用例 | vitest run | 全绿；oracle fixture 覆盖 4 后端 |

## 3. 任务拆解

| 任务 ID | 任务描述 | 类型 | 仓 | 预估 | 状态 |
|---|---|---|---|---|---|
| T-1 | msw handler: listAuditEvents 接 `?action=` 参数 + action-oracle fixture | mock | saas-msw | 30min | 待开始 |
| T-2 | saas-aspnetcore: ApiKeyController 写端点调 AuditWriter.WriteAsync | 后端 | saas-aspnetcore | 30min | 待开始 |
| T-3 | saas-springboot: ApiKeyController 同款 | 后端 | saas-springboot | 30min | 待开始 |
| T-4 | saas-nextjs route handlers: API key 写端点调 audit logger helper | 后端 | saas-nextjs | 30min | 待开始 |
| T-5 | saas-aspnetcore: RetentionPolicyController GET/PUT | 后端 | saas-aspnetcore | 30min | 待开始 |
| T-6 | saas-springboot: 同款 | 后端 | saas-springboot | 30min | 待开始 |
| T-7 | saas-nextjs: 同款 route handler | 后端 | saas-nextjs | 20min | 待开始 |
| T-8 | msw handler: retention GET/PUT | mock | saas-msw | 15min | 待开始 |
| T-9 | contract-test: ?action 过滤 + retention 用例覆盖 4 后端 | 测试 | contract-test | 1h | 待开始 |

## 4. 功能影响（需求与功能对齐的唯一位置）

> ID 已在 [function-tree.md §子项级](../functions/function-tree.md) 登记（ADR-0023 P2 修正 + L5 跨仓扫描）。

| 功能 ID | 功能名称 | 影响类型 | 说明 | 关联任务 |
|---|---|---|---|---|
| M06.F01 | 审计事件查询（tenant-scoped） | 变更 | 状态 规划→开发中；list + by-user + export + action 过滤完整 | T-1/T-2/T-3/T-4/T-10 |
| M06.F01.I01 | 列表审计事件 | 变更 | 加 `?action=` 过滤实现（contract 已定） | T-1/T-2/T-3/T-4 |
| M06.F01.I02 | 按用户查审计事件 | 新增 | `GET /audit-events/by-user/{userId}` 4 后端实现 | T-2/T-3/T-4 |
| M06.F01.I03 | 导出审计事件 | 新增 | `POST /audit-events/export` 返 downloadUrl（json/csv） | T-2/T-3/T-4 |
| M06.F02 | 审计留存策略 | 变更 | 状态 规划→开发中；I01/I02 实装，I04 与 I02 同 op 废弃 | T-5/T-6/T-7/T-8 |
| M06.F02.I01 | 读取审计留存策略 | 新增 | `GET /audit-events/retention` 返 `{retentionDays}`，默认 90 | T-5/T-6/T-7/T-8 |
| M06.F02.I02 | 设置审计留存策略 | 新增 | `PUT /audit-events/retention` upsert retention policy | T-5/T-6/T-7/T-8 |
| M06.F03 | 审计写入助手（写端点副作用） | 变更 | 状态 规划→开发中；AuditWriter.WriteAsync 内部 helper | T-2/T-3/T-4 |
| M06.F03.I01 | AuditWriter.WriteAsync | 新增 | 内部 helper（不暴露 HTTP 端点），api-key 写端点副作用 | T-2/T-3/T-4 |
| M06.F03.I02 | 列表 `?action=` 过滤 | 变更 | msw 之前缺，现补全 → 4 后端对称 | T-1/T-9 |

## 5. 流程影响

引用 [flow-function-map.md §M06](../design/flow-function-map.md)：

- **之前**：写端点直接落业务表，无审计事件 → 安全事件调查无据
- **现在**：写端点事务内调 AuditWriter.WriteAsync → 同事务提交 audit_events 行 → 查询 API 支持按 action / actor / 时间窗过滤

## 6. 风险与回滚

| 风险 | 影响面 | 缓解 | 回滚方式 |
|---|---|---|---|
| 4 后端 AuditWriter 实现漂移 | 审计数据不一致 | AuditWriter.WriteAsync 签名统一（tenantId, actorUserId, action, metadata）；contract-test T-9 覆盖 | 移除 AuditWriter 调用，回到无审计 |
| retention 设置过短（如 0）误清数据 | 合规/历史查询失效 | 0 < retentionDays < 3650 校验；前端 slider 加确认 | 默认 90 不变，只在显式 PUT 时变 |
| msw oracle 与 prod 行为漂移 | contract-test 假绿 | oracle fixture 4 后端共用 + 每月 reconcile | 重新对齐 oracle |
| 物理删除不留痕（AC-4）vs revoke 留痕 | 设计不直观 | 本 REQ 显式记此不对称（"硬删不留痕，revoke 留痕"），未来 ADR 评估统一 | 撤销物理删除端点统一走 revoke |
