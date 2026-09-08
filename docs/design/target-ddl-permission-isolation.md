# 目标 DDL 权限隔离设计

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

关联功能：REQ-2026-026；M00/M01/M02/M03/M04/M08/M09。
