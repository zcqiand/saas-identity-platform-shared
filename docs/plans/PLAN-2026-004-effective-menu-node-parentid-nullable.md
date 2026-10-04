# PLAN-2026-004 EffectiveMenuNode.parentId 放宽 nullable（契约说谎修正提案）

> 状态：**草案（待人批）**。提出：saas-identity-platform-swift REQ-2026-008 切片
> 现场发现（2026-10-01）；依据 ADR-0029（消费仓禁单方面改 shared，列候选方案
> 停下问人）。人裁方向已定「先瘦身后修契约」（swift 切片先落地 CRUD+组树，
> 本提案补齐「当前用户菜单」角的契约前提）。

## 1. 问题

`tsp/models/sys-role-menu.tsp` 的 `EffectiveMenuNode.parentId` 声明为必填：

```typespec
model EffectiveMenuNode {
  @format("uuid") id: string;
  clientId: string;
  @format("uuid") parentId: string;   // ← REQUIRED，但 wire 真值是 null
  ...
}
```

`GET /me/menus` 的**根节点** `parentId` 在四后端 wire 上全部是 `null`
（live 实证；fastapi 为绕过 pydantic 必填用了 `model_construct` hack——
本身就是契约说谎的补丁）。契约 requiredMode=REQUIRED 与实现全面脱节。

**受害者**：swift5 生成物 `EffectiveMenuNode.parentId: UUID` 非可选且无自定义
decoder → 合成解码遇 `null` 必抛 `valueNotFound` → **整个 `/me/menus` 响应
解不出来**。react/vue/nextjs 等 JS 消费仓无静态类型无感；aspnetcore/rails/
springboot/fastapi（自身）同理按语言可选性各异。

**同文件不受影响面**：`SysMenu.parentId` 必填是**真话**——菜单 CRUD 平铺
列表根节点发**零值 UUID** `00000000-0000-0000-0000-000000000000`（live 实证
springboot 27 条种子），两 DTO 口径不同是既成事实，本提案不动 SysMenu。

## 2. 候选方案（ADR-0029 三选一）

| 方案 | 内容 | 影响 | 判定 |
|---|---|---|---|
| A. 契约放宽（推荐） | `parentId?: string` | 契约归真；JS 仓零影响；swift 解码 `null→nil`；fastapi 可拆 model_construct hack；需全家族重生成 | ✅ |
| B. 后端统一改零值 UUID | 四后端 /me/menus 根节点发零值哨兵 | 动 4 个后端实现 + CT 断言；契约继续说谎（REQUIRED 但语义是「零值哨兵」）；与 SysMenu 的口径反而混淆 | ❌ |
| C. Swift 消费仓私有 decoder | swift 侧手写绕过生成物解码 | 违反硬规则 §4（DTO 只认生成物）精神；每个消费仓都要补一遍 | ❌ |

**选定 A**。类型取 `parentId?: string`（optional 而非 `string | null`）：
家族既有惯例（`path?` / `component?` 同款），生成器支持面最广；wire 的
`"parentId": null`（present-but-null）与 absent 在各语言可选解码下同收。

## 3. 改动与重生成清单

| 步骤 | 内容 |
|---|---|
| S1 | `tsp/models/sys-role-menu.tsp`：`parentId: string` → `parentId?: string`（单行） |
| S2 | shared 自身 gate（L4.db 幂等不受影响；契约快照如有需刷新） |
| S3 | 全家族重生成（各仓 `gen-shared.sh`，共 8 消费仓）：react / vue / nextjs / springboot / aspnetcore / rails / fastapi / swift |
| S4 | contract-test：**无 parentId 断言**（已核，仅 cleanup-pg.ts 引用 menus）——补一条 `/me/menus` 根节点 `parentId ∈ {null, absent}` 的宽松断言防回归（可选，升档人裁） |
| S5 | swift 消费仓：REQ-2026-008 的「当前用户菜单」补角 REQ（MeAPI.meGetMyMenus 解码落地渲染，收口 M04.F04 全语义） |
| S6 | fastapi 拆 `model_construct` hack（可选，随重生成验证） |

## 4. 风险

| 风险 | 缓解 |
|---|---|
| optional 化后某后端开始**省略**键（present-but-null → absent）导致下游判空逻辑分叉 | CT 宽松断言两种都收（S4）；swift 侧 `nil` 即根，无行为分叉 |
| 全家族重生成带出无关 diff | 各仓重生成 commit 单独收账（既有 gen-shared 流程惯例） |
| `@format("uuid")` 保留与否 | 保留——nullable 不影响格式校验 |

## 5. 相关发现（另案，不在本提案）

**wire 日期违约**：`SysMenu.createdAt: utcDateTime` 契约要求 RFC3339，
springboot wire 实发 `2026-08-30 00:07:19.257+08`（空格分隔 + 裸 `+08`，
admin/tenants、admin/clients 同款）——生成 OpenISO8601DateFormatter 解不了，
swift 消费仓已用生成物公开钩子 `CodableHelper.dateFormatter` 装
FamilyDateFormatter 兜底（REQ-2026-008 T-1，939d5a2）。治本候选：后端序列化
收口 RFC3339（动四后端 + CT），或契约降级容非标格式（不可取）。**待另立
提案人裁**。
