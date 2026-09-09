# REQ-2026-025 saas 菜单结构维护 — 排序与切换父级 （已废段镜像豁免，9/7 迁移前快照）

> **2026-09-07 模块重组注意**：本 REQ 文档是历史快照；旧 `M08.F02.I06-I07` 已迁至 **M04.F04.I06-I07**。
> 迁移表见 [function-tree.md §0.x](../functions/function-tree.md#0x-模块重组迁移记录2026-09-07)。

| 项 | 值 |
|---|---|
| 提出人 | saas 应用下树形菜单 CRUD 补全（M08.F02） |
| 提出日期 | 2026-09-07 |
| 优先级 | P2 |
| 状态 | 已评审 |
| 关联 ADR | [0023-cross-repo-ssot-coverage-mapping.md](../../../docs/adr/0023-cross-repo-ssot-coverage-mapping.md) |

## 1. 需求描述

应用下的菜单是树形结构（M08.F01 CRUD 建节点）。两个常用维护操作：

1. **同级排序**：在同 parent 下拖动菜单节点改变显示顺序
2. **切换父级**：把菜单节点从一个父级移到另一个父级下（含"提升到根"）

**现状**：

- `tsp/routes/admin-app-menus.tsp` 已定义端点：
  - `PUT /admin/apps/{appId}/menus/{menuId}/reorder` → reorderMenu（M08.F02.I06）
  - `PATCH /admin/apps/{appId}/menus/{menuId}/parent` → setMenuParent（M08.F02.I07）
- function-tree 之前状态「规划」—— 端点已挂但缺设计决策落地

## 2. 设计决策

### 2.1 排序语义（reorder）

- **范围**：单 parent 下所有兄弟节点的 `sort_order` 整体重排
- **触发**：`PUT /reorder {newIndex: int}`（0-based，按当前兄弟列表插入位置）
- **原子性**：单事务内 UPDATE 所有兄弟节点的 sort_order
- **冲突处理**：version 字段（optimistic locking）—— 客户端先 GET 当前 list → PUT 带 expected version → 不匹配返 409 `STALE_VERSION`

### 2.2 切换父级语义（setMenuParent）

- **触发**：`PATCH /parent {newParentId: uuid | null}`（null = 提升到应用根）
- **校验**：
  - newParentId 必须存在且同 app（否则 404）
  - **禁止循环引用**：newParentId 不能是当前节点或其后代（递归查 depth-first + ancestor chain 检查）
- **副作用**：
  - sort_order 自动置为新父级兄弟列表末尾
  - depth 字段自动重算（移到新父级后子树深度可能变）
  - ancestor chain（materialized path 或 closure table）自动更新

### 2.3 树一致性保证

| 保证 | 实现 |
|---|---|
| 无孤儿节点 | 父节点删除时级联或拒绝（M08.F01.I05 DELETE 已定；本文档不展开） |
| 无循环 | 切父级时 ancestor 检查 |
| depth 准确 | 切父级后 BFS 重算 |
| sort_order 唯一连续 | 兄弟列表单事务重排（gap-free） |

## 3. 验收标准

| 编号 | 场景（给定） | 操作（当） | 预期（则） |
|---|---|---|---|
| AC-1 | 菜单 M1/M2/M3 同 parent，sort_order 0/1/2 | `PUT /menus/M2/reorder {newIndex: 0, version: v1}` | 兄弟顺序变 M2/M1/M3，sort_order 0/1/2；version+1 |
| AC-2 | 同上，client 拿到 list 后又有其他客户端改了 version | `PUT` 带 stale version | 409 `STALE_VERSION` |
| AC-3 | 菜单 M（parent=A） | `PATCH /menus/M/parent {newParentId: B}` | M.parent=B；M.sort_order = B.siblings.length；M.depth = B.depth+1 |
| AC-4 | 菜单 M1（M2 的祖先） | `PATCH /menus/M1/parent {newParentId: M2}` | 422 `CYCLE_DETECTED`（防循环） |
| AC-5 | 菜单 M（parent=A） | `PATCH /menus/M/parent {newParentId: null}` | M.parent=null（提升到根）；depth=0 |
| AC-6 | 新父级跨 app | `PATCH /menus/M/parent {newParentId: 异 app 节点}` | 404 `PARENT_NOT_FOUND`（必须同 app） |
| AC-7 | 4 后端 reorder 实现 | contract-test T-5 | msw oracle + aspnetcore + springboot + nextjs 行为一致 |
| AC-8 | 4 后端 setMenuParent 循环检测 | contract-test T-6 | 4 后端对 4 层祖先返回同一 CYCLE_DETECTED |

## 4. 任务拆解

| 任务 ID | 任务描述 | 类型 | 仓 | 预估 | 状态 |
|---|---|---|---|---|---|
| T-1 | saas-aspnetcore: MenusController.Reorder（version + 单事务 sort_order 重排） | 后端 | saas-aspnetcore | 1h | 待开始 |
| T-2 | saas-springboot: 同款 | 后端 | saas-springboot | 1h | 待开始 |
| T-3 | saas-nextjs: route handler reorder | 后端 | saas-nextjs | 45min | 待开始 |
| T-4 | saas-aspnetcore: MenusController.SetParent（祖先链查 + cycle 拒） | 后端 | saas-aspnetcore | 1h | 待开始 |
| T-5 | saas-springboot: 同款 | 后端 | saas-springboot | 1h | 待开始 |
| T-6 | saas-nextjs: 同款 route handler | 后端 | saas-nextjs | 45min | 待开始 |
| T-7 | saas-msw: reorder + setParent handlers（含 version/cycle 行为） | mock | saas-msw | 1h | 待开始 |
| T-8 | contract-test: 8 个 AC 覆盖 | 测试 | contract-test | 2h | 待开始 |

## 5. 功能影响

> ID 已在 [function-tree.md §子项级](../functions/function-tree.md) 登记。

| 功能 ID | 功能名称 | 影响类型 | 说明 | 关联任务 |
|---|---|---|---|---|
| M08.F02 | 菜单结构维护（排序/父级） | 变更 | 状态 规划→开发中；reorder + setParent 4 后端实装 | T-1/T-2/T-3/T-4/T-5/T-6/T-7 |
| M08.F02.I06 | 同级排序 | 新增 | `PUT /{menuId}/reorder` + version 校验 | T-1/T-2/T-3/T-7 |
| M08.F02.I07 | 切换父级 | 新增 | `PATCH /{menuId}/parent` + 循环检测 + depth 重算 | T-4/T-5/T-6/T-7 |

## 6. 风险与回滚

| 风险 | 影响面 | 缓解 | 回滚方式 |
|---|---|---|---|
| 切父级并发导致 sort_order 错乱 | 菜单显示乱 | version 字段 + 单事务 | 读最新 list 重 PUT |
| depth/ancestor 字段未更新 | 前端面包屑/权限错 | 切父级后 BFS 重算 depth；用 closure table 而非 materialized path 避免维护 | 全表 depth 重算 batch job |
| 循环引用被绕过（如跨 subtree） | 树状菜单死循环 | ancestor chain 完整查 + 4 后端 contract-test T-6 强制覆盖 | 拒绝切父级 + 告警 |
