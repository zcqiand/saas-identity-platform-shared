# API Key 生命周期设计映射 — M05

## 1. 设计映射（已上线 ID）

| 子项 ID | 端点 | 设计要点 | 对应合同 |
|---|---|---|---|
| M05.F01.I01 | `GET /tenants/{tenantId}/api-keys` | 列表分页；page=0-indexed, pageSize 默认 20 | `tsp/routes/tenant-api-keys.tsp:8 @get listApiKeys` |
| M05.F01.I02 | `POST /tenants/{tenantId}/api-keys` | 创建；**返回一次性 secret**（仅创建时可见，存哈希）| `tsp/routes/tenant-api-keys.tsp:16 @post createApiKey` |
| M05.F01.I03 | `POST .../api-keys/{keyId}/revoke` | 吊销；**软删**，保留 audit 行；幂等（重复吊销返 200） | `tsp/routes/tenant-api-keys.tsp:24 @route /{keyId}/revoke` |
| M05.F01.I04 | `POST .../api-keys/{keyId}/rotate` | 轮换；revoke 旧 + create 新，原子事务 | `tsp/routes/tenant-api-keys.tsp:29 @route /{keyId}/rotate` |
| M05.F01.I05 | `DELETE .../api-keys/{keyId}` | **物理删除**；直接删 DB 行，**不留痕**；幂等（重复删返 404） | `tsp/routes/tenant-api-keys.tsp:39 @route /{keyId}` |

## 2. 状态机

```
   [create]
      ↓
   active ──revoke──→ revoked (软删)
      │
      ├────rotate────→ active (新 key)
      │
      └────delete────→ ∅ (物理删，不留痕)
```

## 3. 设计决策（核心）

### 3.1 物理删除 vs 软删的不对称（关键）

| 操作 | 端点 | 数据状态 | audit_events |
|---|---|---|---|
| revoke (M05.F01.I03) | `POST /revoke` | 行保留，`status='revoked'`, `revoked_at=now()` | 写一行 `api_key_revoked` |
| 物理删除 (M05.F01.I05) | `DELETE /{k}` | 行删除 | **不写** audit_events |

**为什么不对称**：物理删除按设计不留痕（GDPR / 用户要求「彻底删」），与 revoke 软删并存；前端 UI 调用者需明确选择——想留 audit 用 revoke，想真删用 DELETE。

**风险**：history 失查 → 仅用于「明确知道不留痕」的场景；默认推荐 revoke。

### 3.2 一次性 secret

- 创建响应：`{keyId, secret, ...}` 其中 `secret` 仅此次返回
- DB 存储：`secret_hash = bcrypt(secret)`，**secret 明文不入库**
- 用户必须保存 secret；丢失则只能 rotate 重发

### 3.3 轮换原子性

- 单事务内：revoke 旧 key + insert 新 key + audit 两行
- 任一失败 → 整事务回滚，**不会出现「旧的没吊销，新的已创建」状态**

## 4. 数据模型

`api_keys` 表（`sql/migrations/V004__init_api_keys.sql`）：

| 列 | 类型 | 说明 |
|---|---|---|
| id | uuid | PK |
| tenant_id | uuid | FK tenants（tenant-scoped 强约束） |
| secret_hash | varchar | bcrypt(secret)，**明文不入库** |
| name | varchar | 用户标注（如 "CI deploy key"） |
| status | varchar | active / revoked |
| created_at | timestamptz | 默认 now() |
| created_by_user_id | uuid | FK users（audit 锚点） |
| revoked_at | timestamptz? | revoke 时填 |
| revoked_by_user_id | uuid? | revoke 时填 |

## 5. 跨仓实现位置

| 仓 | 实现 |
|---|---|
| saas-aspnetcore | `TenantApiKeysController`（5 op）|
| saas-springboot | `TenantApiKeysController` |
| saas-nextjs | `app/api/tenants/[tenantId]/api-keys/...` |
| saas-msw | `handlers/tenant-api-keys.ts` mock |

## 6. 与 audit 联动

每个写端点（create / revoke / rotate）调 `AuditWriter.WriteAsync(tenantId, actorUserId, action, metadata)`，详见 [REQ-2026-023](../requirements/REQ-2026-023-saas-audit-retention.md) §M06.F03.I01。

物理删除（DELETE）**不**调 AuditWriter（设计故意）。
