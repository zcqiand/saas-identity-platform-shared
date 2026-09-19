# 目标 DDL 权限隔离设计

> 2026-09-19（Task 3.5）解除 H1「已废段镜像豁免」：本文件为 9/8 schema-first pivot 后重写版，正文无废弃 ID 字面，恢复计入 design_refs。

> **架构全景 + 三叉戟隔离**（应用 / 租户 / 用户）见
> [`architecture-panorama.md`](architecture-panorama.md)。本文件聚焦 DDL 边界与授权 join 真源。
>
> 关联：[oauth-architecture.md](oauth-architecture.md) / [me-menus.md](me-menus.md) /
> [user-model.md](user-model.md) / REQ-2026-026

## 边界

- `sys_user` 是全局账号边界。
- `tenant_member` 是租户内身份边界。
- `oauth_client` 是应用/客户端和静态菜单元数据边界。
- `tenant_application` 是租户应用订阅边界。
- `oauth_access_token` 与 `oauth_refresh_token` 携带当前 user/client/tenant 上下文。

## 授权查询

```text
JWT(user_id, client_id, tenant_id)
  -> tenant_member(user_id, tenant_id)
  -> tenant_member_role(member_id)
  -> sys_role(tenant_id, client_id)
  -> sys_role_menu(role_id)
  -> sys_menu(client_id)
```

每一个 join 都必须保留 token 中的租户和客户端边界。菜单树补父链时仍限制同一个 `client_id`；根节点使用固定零 UUID，不把零 UUID 伪造为真实菜单行。

## OAuth

授权码、访问令牌和刷新令牌分表。令牌表只允许 protocol/service 层使用，公共响应不得暴露 client secret、token hash 或持久化内部字段。刷新令牌轮换时旧令牌立即撤销，重放和过期请求拒绝。

## 破坏性切换

本次不做旧数据转换，不保留 `api_keys`、`audit_events`、`audit_retention_policies`。Drizzle schema 是唯一真源，目标迁移从空库重建；下游 ORM mirror 和 contract-test 必须以 generated OpenAPI/DDL 同步更新。

## 安全失败语义

`sub`、`user_id`、`tenant_id`、`client_id`、`member_id` 或 `role_code` 缺失时返回 401/403 或抛出明确异常。不得使用 `USER-A`、`TENANT-001`、`alice` 等 demo 字面量兜底。

关联功能：REQ-2026-026；三叉戟三模块（M00 租户 / M01 用户 / M04 应用）及其余五个已废弃历史模块（无功能树行，见 [architecture-panorama.md §4.5 豁免声明](architecture-panorama.md)）。

## 租户维护设计映射 — M00.F01

> 2026-09-19（Task 3.5）补齐设计锚点：平台 admin 的租户 CRUD（`tenant` 表，
> 平台级边界——非 tenant-scoped，要求 platform_admin 权限）。

| 子项 ID | 端点 | 设计要点 | 对应合同 |
|---|---|---|---|
| **M00.F01.I01** | `GET /admin/tenants` | 租户列表（分页）；平台 admin 专用，token 需 platform_admin，租户边界外的平台面 | `tsp/routes/admin-tenants.tsp:8 @get listTenants` |
| **M00.F01.I03** | `GET /admin/tenants/{id}` | 租户详情（含状态/到期时间/订阅应用数）；寻址用行 UUID | `tsp/routes/admin-tenants.tsp:16 @get getTenant` |
