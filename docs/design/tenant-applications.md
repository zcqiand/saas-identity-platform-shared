# 租户应用订阅 设计映射 — M00.F05

> 旧编号 M04.F04（租户应用订阅）→ **M00.F05**（2026-09-07 模块重组，切到租户配置视角）。
> 迁移表见 [function-tree.md §0.x](../functions/function-tree.md)。
> 2026-09-19（Task 3.6 转正）补齐 4 子项设计映射；此前 M00.F05 四个子项一直缺设计锚点。

## 1. 设计映射（已上线 ID）

| 子项 ID | 端点 | 设计要点 | 对应合同 |
|---|---|---|---|
| **M00.F05.I01** | `GET /tenants/{tenantId}/applications` | 列出租户订阅（分页包装 `{items,page,pageSize,total}`；行含 id/clientId/tenantId/status/expireTime?/createdAt） | `tsp/routes/tenant-applications.tsp:8 @get listTenantApplications` |
| **M00.F05.I02** | `POST /tenants/{tenantId}/applications` | 订阅应用：SubscribeTenantApplicationRequest{clientId, expireTime?}；clientId 必须已注册于 `oauth_client`（未知 → 404，springboot 2026-09-12 修 FK 盲插），重复订阅 → 400（aspnetcore 应用层查重 + `uk_tenant_client` 唯一索引） | `tsp/routes/tenant-applications.tsp:16 @post subscribeTenantApplication` |
| **M00.F05.I03** | `PATCH /tenants/{tenantId}/applications/{clientId}` | 修改订阅 status（int32）或 expireTime；寻址用 clientId 字符串列（非行 UUID） | `tsp/routes/tenant-applications.tsp:23 @patch updateTenantApplication` |
| **M00.F05.I04** | `DELETE /tenants/{tenantId}/applications/{clientId}` | 取消订阅；不删除 `oauth_client` 本体（cascade 由 FK 负责） | `tsp/routes/tenant-applications.tsp:32 @delete removeTenantApplication` |

## 2. 数据模型

`tenant_application`（ADR-0025 schema-first：`src/db/schema.ts` ↔ DB `public.tenant_application`）：

| 列 | 类型 | 说明 |
|---|---|---|
| id | uuid | PK |
| tenant_id | uuid | FK tenant（cascade） |
| client_id | varchar(64) | FK oauth_client.client_id（字符串列，非行 UUID） |
| status | smallint | 0=disabled 1=active（契约 int32） |
| expire_time | timestamptz? | 订阅到期（可空） |
| created_at | timestamptz | NOT NULL，默认 now() |
| — | — | 附加：`uk_tenant_client` unique(tenant_id, client_id) + idx client_id |

## 3. 跨仓实现位置

- 后端：nextjs `src/app/api/v1/tenants/[tenantId]/applications/**`；springboot `TenantApplicationsController`；aspnetcore `TenantApplicationsController`（guard-first VerifyPathTenant）
- 前端：react / vue `TenantApplicationsListPage`（orval 生成物 `src/api/endpoints/tenant-applications/`）
- 测试：contract-test `tests/tenant-applications.test.ts`（四方比对 I74-I77 组 ↔ 本 4 子项，BASE 对齐注释在文件头）
