# I ID ↔ SSOT 端点 / 数据表 绑定规则 — saas-identity-platform-shared （已废段镜像豁免，9/7 迁移前快照）

> **真源**：[function-tree.md](../functions/function-tree.md) 的子项 ID（I 级），
> 通过本规则绑定到 `tsp/routes/*.tsp` 的 HTTP 端点和 `drizzle/schema.ts` 的 DB 表。
>
> **适用 scope**：M00 / M01 / M04 已上线段 + M05 / M06 / M09 已废弃段。
> 已废弃条目按 §7 例外规则 R5 处理。
>
> **配套 ADR**：[ADR-0024](../../../docs/adr/0024-function-tree-base-consumer-field-alignment.md)（type / delivery 字段硬对齐）/
> [ADR-0027](../../../docs/adr/0027-consumer-strict-subset.md)（subset invariant）/
> [ADR-0023](../../../docs/adr/0023-cross-repo-ssot-coverage-mapping.md)（SSOT 覆盖映射）/
> [ADR-0020](../../../docs/adr/0020-base-tree-collects-i-level.md)（BASE I 定标）。
>
> REQ-2026-020 / REQ-2026-024 / REQ-2026-026

## 1. 视角

shared 仓是 SaaS 身份平台的契约 SSOT。每个 `Mxx.Fxx.Ixx` 在落地时通常对应两类载体：

- **HTTP 端点** —— 跨仓消费方的接口契约，落地于 `tsp/routes/<file>.tsp` 中的 `@route` + `@<verb> op`。
- **DB 表** —— 跨仓消费方的存储契约，落地于 `drizzle/schema.ts` 的 `pgTable(...)`，命名族 `sys_*` / `oauth_*` / `tenant_*`。

但**不是每个 I ID 都同时需要这两类载体**。派生查询、UI 组件、按钮触发器、报表聚合通常只占一端。本文档规定绑定规则，使反向检查（"这个 I ID 是否应该有 SSOT 端点 / DB 表"）可机器化、可校验。

**反向检查的两条主诉**：

1. shared 仓 `tsp/routes/*.tsp` 注释 `// Mxx.Fxx.Ixx` 是 contract-test 跨仓扫描的锚点（[ADR-0023](../../../docs/adr/0023-cross-repo-ssot-coverage-mapping.md)）—— 没有端点的 I ID 等于 contract-test 不可能覆盖。
2. 消费仓私自加端点 / 表（违反 [ADR-0027](../../../docs/adr/0027-consumer-strict-subset.md) subset invariant）必须被 `_orphans.py` 抓到 —— 抓的前提是 shared 端点 / 表集合可推导。

下文 §2 是 type × delivery 四象限矩阵，§3 / §4 给出判定函数，§5 / §6 是引用形式规范，§7 是例外清单，§8 是反向检查实现路径，§9 记录当前已知 latent bugs。

## 2. type × delivery 四象限矩阵

子项的 [type / delivery 字段](../../../docs/adr/0024-function-tree-base-consumer-field-alignment.md) 取值组合 → 是否必须挂 SSOT 端点 / DB 表：

| type \ delivery | `仅前端` | `前端+后端` | `仅后端` | `后端` |
|---|---|---|---|---|
| `页面`   | ✅ 仅前端 UI（无需端点 / 表）| ✅ 前端 + 后端 API（需端点；视写操作挂表）| ✅ 仅后端 API（需端点；视写操作挂表）| ✅ 后端契约（需端点；视写操作挂表）|
| `标签页` | ✅ 仅前端（无需端点 / 表）| — | — | — |
| `按钮`   | ✅ 触发器（无需端点 / 表，跟随父接口）| — | — | — |
| `查询`   | ✅ 前端查询（无需端点 / 表）| — | — | ✅ 派生 / 聚合（需端点，**无需表**）|
| `报表`   | — | — | — | ✅ 聚合展示（**无需端点**，前端组件承担）|
| `接口`   | — | ✅ 前端 + 后端（需端点；视写操作挂表）| ✅ 仅后端（需端点；视写操作挂表）| ✅ 后端契约（需端点；视写操作挂表）|

saas-shared BASE 当前 96 个 I 行实际占用的格子：

| 组合 | 行数 | 例子 |
|---|---|---|
| `(接口, 后端)` | 86 | **M00.F01.I01**-**I05** 租户维护、**M01.F04.I01**-**I06** 登录全段、**M04.F03.I01**-**I03** OAuth 协议 |
| `(查询, 后端)` | 8 | **M01.F01.I01** 当前用户 whoami、**M04.F04.I08** 当前用户有效菜单（派生） |
| `(页面, 仅前端)` | 2 | **M01.F04.I03** 密码登录 UI、**M03.F01.I03** 旧登录 UI（已迁移） |

> **未占用但合法的格子**：`(页面, 前端+后端)`、`(标签页, 任意)`、`(按钮, 任意)`、`(查询, 前端+后端 / 仅后端)`、`(报表, 任意)` —— 当前 BASE 没有，矩阵列出来供未来新增时判别。

## 3. 「需要 SSOT 端点」判定规则

```python
def needs_endpoint_ref(type_: str, delivery: str) -> bool:
    """§3 判定：当前 I ID 是否必须挂 SSOT 端点（tsp/routes/*.tsp 注释）。"""
    if type_ not in {"接口", "查询"}:
        return False
    if delivery not in {"后端", "前端+后端", "仅后端"}:
        return False
    return True
```

**调用方**：

- `_alignment.py::cross_repo_test_refs` 在建 `route_to_ids` 时，**每个被收进集合的 I ID 都隐式满足 needs_endpoint_ref=True**（因为 `// Mxx.Fxx.Ixx` 注释只挂在 `@route` / `@<verb> op` 旁边）。
- 反向检查（§8）：needs_endpoint_ref=True 但 `route_to_ids` 任一 value 不含此 fid → 报「端点缺失」。

**type=报表 不挂端点**——见 §7 R2。

## 4. 「需要 DB 表」判定规则

```python
def needs_table_ref(
    type_: str,
    delivery: str,
    op_method: str,
    is_aggregate: bool,
) -> bool:
    """§4 判定：当前 I ID 是否必须挂 DB 表（drizzle/schema.ts pgTable）。"""
    if not needs_endpoint_ref(type_, delivery):
        return False
    if is_aggregate:
        # 派生 / 聚合读：不写表（例：M04.F04.I08 当前用户有效菜单）
        return False
    # 仅写操作需要 DB 表；读操作复用既有表
    return op_method in {"POST", "PUT", "PATCH", "DELETE"}
```

**调用方**：

- `op_method` 由 `endpoint_ref` 解析 tsp 中 `@post` / `@get` / `@put` / `@patch` / `@delete` 得出（见 §5）。
- `is_aggregate` 由说明列人工标注约定触发（如 M04.F04.I08 "派生" / "聚合" 字面，或 §7 R3 规则）。

**判定边界**：

- 读操作（`@get`）即使 `needs_endpoint_ref=True`，也不需要新建表；它复用 §6 列出的命名族已有表。
- 写操作（`POST/PUT/PATCH/DELETE`）必须至少挂一个 `pgTable`（落表）。
- 多表操作（如 `op 落表：\`sys_user\` + \`tenant_member\` + \`tenant_member_role\``）→ table_ref 列出全部。

## 5. endpoint_ref 语义与挂载规则

**引用形式**：

```
tsp/routes/<file>.tsp:<line> [@<verb> <op_name>]
```

| 形式 | 例子 | 出处 |
|---|---|---|
| 单段路径 | `tsp/routes/admin-tenants.tsp:8 @get list` | [oauth-architecture.md §1](../../../docs/adr/../design/oauth-architecture.md) `tsp/routes/oauth.tsp:10 @route /authorize` |
| 多段路径 | `tsp/routes/admin-tenants.tsp:8 @get list`（ns `@route("/admin")` + ns `@route("/tenants")` + op `@get`） | function-tree.md §M00.F01 段头 |
| 派生 / 聚合 | `tsp/routes/me.tsp:23 @get myMenus` | [architecture-panorama.md §3](../../../docs/adr/../design/architecture-panorama.md) |

**段头模板**：

> 视角：**xxx**。op 全部带 `tenantId` 路径或 `/admin/*` 平台 admin 路径，落表覆盖 `<snake_case_tables>`。
>
> 端点路径：`/admin/tenants`（要求 platform_admin）
>
> op 落表：`sys_user` + `tenant_member` + `tenant_member_role`（panorama §2.3）

——function-tree.md 各 M·F 段头惯例；本规则不引入新结构字段，**说明列中 `*.tsp:N` 引用是已有的手工约定**。

**多段路径解析规则**（与 `_alignment.py::cross_repo_test_refs` L218-228 的回退逻辑一致）：

1. `@route("/x")` 在 namespace 前 → ns_route = `/x`
2. 后续 `@route("/y")` → op_route = `/y`
3. `@<verb> op <name>(...)` → 全路径 = `ns_route + op_route`（op_route 为空时退化为 `ns_route`）
4. contract-test 命中路径时，先试全路径，失败则从最长前缀逐段回退到 ns_route

> nextjs `route.ts` 路径前缀 `/api/v1/<...>` 在 `_orphans.py` 多段路径归一化后必须与 shared 的 ns_route 拼接结果一致 —— 当前 [orphans.json](../../../.state/orphans.json) nextjs 段报 `/v1/oauth/authorize` 缺 `/api` 段，详见 §9。

## 6. table_ref 语义与挂载规则

**引用形式**：

```
`<snake_case_table_name>`
```

**命名族**（`drizzle/schema.ts` 当前 12 张表的分类）：

| 命名族 | 例子 | 归属模块 |
|---|---|---|
| `tenant_*` | `tenant` / `tenant_member` / `tenant_member_role` / `tenant_application` | M00 |
| `sys_*` | `sys_user` / `sys_role` / `sys_role_menu` / `sys_menu` | M01 + M04 |
| `oauth_*` | `oauth_client` / `oauth_code` / `oauth_access_token` / `oauth_refresh_token` | M04 |
| `role_*` | `role_permissions` | M00 内部 |
| （已迁移）| `api_keys` / `audit_events` | M05 / M06 已废弃段 |

**段头模板**：

> op 落表：`sys_user` + `tenant_member` + `tenant_member_role`

——function-tree.md 各 M·F 段头惯例，**说明列中反引号表名是已有的手工约定**。

**多表写法**：反引号 + 顿号 `、` 分隔；落表数 ≥ 1 时按业务顺序排（如先写主表，再写关联表）。

**判定边界**：

- 派生 / 聚合查询（如 M04.F04.I08 当前用户有效菜单）→ 不挂 table_ref（见 §4 `is_aggregate=True`）
- 「内部 op」（如 logout-local、token-rotation）→ 可挂 table_ref = `—`（见 §7 R4）
- 读操作复用既有表 → 不引入新 table_ref

## 7. 例外清单（rule-based）

R1 — R5 是规则，不是白名单。规则由 type / delivery / status / op_method / 说明列字面触发。

- **R1（UI 类）**：type ∈ {页面, 标签页, 按钮} → 不挂 endpoint_ref、不挂 table_ref；触发即合法。
- **R2（报表）**：type = 报表 → 即使 delivery = 后端，也**不挂单一端点**（聚合展示由前端组件承担）；如果需要按维度筛选，挂派生查询端点（视为 `查询` 而非 `报表`）。
- **R3（派生查询）**：type = 查询 且说明列含「派生」/「聚合」字面 → 挂 endpoint_ref，**不挂 table_ref**；例：**M04.F04.I08** 当前用户有效菜单。
- **R4（内部 op）**：type = 接口 且说明列含「内部」/「清理」/「轮换」字面 → 可挂 endpoint_ref 但 table_ref = `—`；例：**M01.F04.I06** logout-local（仅清 cookie，不落 DB）。
- **R5（已废弃）**：status = 已废弃 → endpoint_ref / table_ref 均可省略；说明列保留旧字面引用供审计；反向检查跳过。

> **例外清单本身是机器可读规则**。`_check_iid_endpoint_coverage`（§8）扫描每个 I ID 时依次应用 R1-R5，全部命中则跳过；否则按 §3 / §4 主规则检查。

## 8. 反向检查实现路径

**目标**：新增 `scripts/checks/_alignment.py::check_iid_endpoint_coverage`，与 `cross_repo_test_refs` 共用 `route_to_ids`。

### 8.1 函数设计

```python
# scripts/checks/_alignment.py 追加
def check_iid_endpoint_coverage(
    items: dict[str, JsonDict],
    route_to_ids: dict[str, set[str]],
) -> list[str]:
    """§8 反向检查：needs_endpoint_ref == True 的 I ID 是否在 tsp/routes/*.tsp 中有对应路径。

    复用 cross_repo_test_refs (L128-238) 的 route_to_ids。
    仅对 needs_endpoint_ref(type, delivery) == True 且未命中 R1-R5 例外的 I ID 检查。
    """
    issues: list[str] = []

    # 1. 把 route_to_ids 翻转成 path → {fid}（已建表，复用）
    path_to_ids: dict[str, set[str]] = {}
    for path, ids in route_to_ids.items():
        path_to_ids.setdefault(path, set()).update(ids)

    # 2. 遍历 items，对每个 I ID 做反向查
    for fid, meta in items.items():
        # R5: 已废弃跳过（基线集合语义：已废弃 = 不该要求消费仓镜像）
        if meta.get("status") == "已废弃":
            continue
        # §3 主规则：needs_endpoint_ref
        if not needs_endpoint_ref(meta["type"], meta["delivery"]):
            continue
        # R1-R4：UI 类 / 报表 / 派生查询 / 内部 op 由说明列字面触发
        if _is_r1_to_r4(meta["desc"]):  # 由 §7 规则机器化实现
            continue
        # 3. 反向查：fid 是否在 path_to_ids 任一 value 中
        if not any(fid in ids for ids in path_to_ids.values()):
            issues.append(
                f"{fid} ({meta['name']}): type={meta['type']} "
                f"delivery={meta['delivery']} 需要 SSOT 端点，"
                f"但 tsp/routes/*.tsp 无对应路径"
            )
    return issues
```

### 8.2 调用方

`scripts/check_align.py main()` 在现有 `cross_repo_test_refs` 调用之后追加：

```python
cross_refs, cross_errors = cross_repo_test_refs(proj, items)
# ... (existing cross_errors 处理不变)

# §8 反向检查：I ID → 端点覆盖
iid_endpoint_issues = check_iid_endpoint_coverage(items, route_to_ids)
if iid_endpoint_issues and args.strict:
    # 默认软告警（warning 档），--strict 时升级为 error
    for issue in iid_endpoint_issues:
        print(f"  - {issue}", file=sys.stderr)
    return 1
```

### 8.3 共享仓 vs 消费仓行为差异

| 仓角色 | 行为 |
|---|---|
| shared 仓 | 跑 `check_iid_endpoint_coverage(items, route_to_ids)` —— items 是自己的 I；route_to_ids 从自己的 `tsp/routes` 建 |
| 消费仓 | 跑 `check_iid_endpoint_coverage(items, shared_route_to_ids)` —— items 是自己镜像的 I；route_to_ids 从 `shared_root/tsp/routes` 建（消费仓不应私自加端点，那是 [ADR-0027 §2](../../../docs/adr/0027-consumer-strict-subset.md) 范畴，由 `_orphans.py` 兜底） |

### 8.4 不引入新结构字段

本规则**不要求** function-tree.md 加 `endpoint_ref` / `table_ref` 列。原因：[ADR-0024 §Decision](../../../docs/adr/0024-function-tree-base-consumer-field-alignment.md) 显式锁定 4 个结构字段（name / type / delivery / desc），加新列会触发 ADR-0024 §Alternatives A 否决路径。

机器可读仍由 `tsp/routes/*.tsp` 中 `// Mxx.Fxx.Ixx` 注释 + `_alignment.py::cross_repo_test_refs` 已落地的 `route_to_ids` 承担。**说明列中 `*.tsp:N` 引用保持人工维护**，但反向检查能抓到"声明需要端点但实际未挂"的漂移。

## 9. 已知 gap（latent bugs）

本节列出当前 3 个 latent bug —— 不在本任务范围修，但显式记录以避免反向检查落地时被误诊。

| # | Bug | 影响 | 后续 PR |
|---|---|---|---|
| G1 | [scripts/check_align.py:255](../../../scripts/check_align.py) `_ALIGN_DELIVERY` 集合不含 BASE 字面 `后端`（BASE 86 行用 `后端`，代码只认 `仅前端/前端+后端/仅后端`） | BASE 自检时 `delivery = None` → 字段缺失告警误报（86 行） | bug fix PR：扩 `_ALIGN_DELIVERY` 加 `"后端"` 字面，或改 BASE 用 `仅后端` 字面（建议改代码，少改动） |
| G2 | ~~`_ALIGN_STATUSES` 不含 `已迁移`（function-tree §已废弃功能子项 用 `已迁移` 状态字面）~~ | ~~`已迁移` 行不进基线集合~~ | **已撤销**（2026-09-09）：function-tree.md 27 行 `已迁移` → `已废弃` 批量改完；`_ALIGN_STATUSES` 不动；状态字面统一为 4 值 |
| G3 | [scripts/checks/_orphans.py](../../../scripts/checks/_orphans.py) nextjs 多段路径归一化 bug（路径前缀 `/api` 段丢失，报 `/v1/oauth/authorize` 为 EXTRA） | nextjs 真实 OAuth 端点被误报 EXTRA（[orphans.json nextjs 段](../../../.state/orphans.json)） | [ADR-0027 §Acceptance precondition PR #B](../../../docs/adr/0027-consumer-strict-subset.md)：`_orphans.py` endpoint extraction 重构（5 栈 × regex + path normalization） |

G1 / G2 修复前置：本文件的 §3 `needs_endpoint_ref` 在 G1 修复前若消费仓 delivery = `后端`，会跳过该 I ID（与 BASE `后端` 字面对不齐）—— 落地前必须先修 G1。

## 10. 与其他文档的关系

| 文档 | 关系 |
|---|---|
| [function-tree.md](../functions/function-tree.md) | I 级表 6 列 schema；段头"视角 / 端点路径 / op 落表"三段模板；type / delivery 字面真源（BASE 86 行用 `后端`，与 `_ALIGN_DELIVERY` 不一致见 §9 G1）|
| [architecture-panorama.md](architecture-panorama.md) | 跨仓实现位置表（`\| 仓 \| 实现 \|`）—— 本文件 §5 / §6 引用形式与该文档一致 |
| [oauth-architecture.md](oauth-architecture.md) | 设计映射"已上线 ID"模板（4 列：子项 ID / 端点 / 设计要点 / 对应合同）；§1 表格是 §8 反向检查的实际例子 |
| [me-menus.md](me-menus.md) | M04.F04.I08 当前用户有效菜单的设计映射 —— §7 R3 派生查询的例子出处 |
| [api-key-lifecycle.md](api-key-lifecycle.md) | M05 已废弃段的设计映射 —— §7 R5 例外的例子出处 |
| [user-model.md](user-model.md) | M01 用户 / 自然人模型；sys_user 表引用 —— §6 命名族 |
| [target-ddl-permission-isolation.md](target-ddl-permission-isolation.md) | DDL 边界真源；sys_menu / sys_role / sys_role_menu 跨表 join —— §6 命名族 + 多表写法 |

**配套 ADR 边界**：

- [ADR-0024](../../../docs/adr/0024-function-tree-base-consumer-field-alignment.md) —— 受约束字段表（name / type / delivery / desc）；本文件**不引入新结构字段**。
- [ADR-0027](../../../docs/adr/0027-consumer-strict-subset.md) —— subset invariant 三维度（I ID / endpoint / DB table）；本文件 §8 是 I ID → endpoint 维度的反向实现。
- [ADR-0023](../../../docs/adr/0023-cross-repo-ssot-coverage-mapping.md) —— contract-test → shared BASE 单向；本文件 §8 复用其 `route_to_ids` 做反向推导，不重复建表。
- [ADR-0020](../../../docs/adr/0020-base-tree-collects-i-level.md) —— BASE I 定标；消费仓 ⊆ BASE；本文件适用范围受其约束。

**不在本文件范围**：

- [ADR-0024](../../../docs/adr/0024-function-tree-base-consumer-field-alignment.md) type / delivery 字段硬对齐落地（warning → error flip 前的整改期）
- [ADR-0027](../../../docs/adr/0027-consumer-strict-subset.md) PR #A / #B / #C 的代码实现
- contract-test 仓补 26 个 SSOT 端点（admin-client × 6 + client-menus × 7 + clients × 1 + tenant-applications × 4 + tenant-members × 8）
- nextjs `src/app/api/v1/oauth/{authorize,token}/route.ts` 清理（私自 IdP 化；M04.F03 是 saas-aspnetcore / springboot / msw 的实现责任）
- §9 G1 / G2 / G3 的 bug fix