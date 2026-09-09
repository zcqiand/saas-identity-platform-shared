# REQ-2026-026 目标 DDL：tenant/client/member/RBAC 隔离 （已废段镜像豁免，9/7 迁移前快照）

> **2026-09-07 模块重组注意**：本 REQ 文档是历史快照；DDL 边界不变，关联功能 ID 调整：
> M00（租户）/ M01（用户）/ M02 → M00.F03/F04（角色）/ M03 → M01.F04（SSO）/ M04（应用+身份认证+菜单）。
> 完整映射见 [function-tree.md §0.x](../functions/function-tree.md#0x-模块重组迁移记录2026-09-07)。

## 目标

将 SaaS 身份平台从旧的 tenant-scoped `users`、合并 `apps`、数组角色授权模型切换为 PostgreSQL UUID 目标 DDL：全局 `sys_user`、租户 `tenant`、租户成员 `tenant_member`、独立 `oauth_client` 与 `tenant_application`，以及授权码、访问令牌、刷新令牌和关系型 RBAC 表。

## 核心约束

1. `sys_user` 只表示自然人；任何租户内身份必须通过 `tenant_member` 表达。
2. `tenant_application` 决定租户是否订阅某个 `client_id`。
3. `sys_role` 的作用域是 `(tenant_id, client_id)`；角色不能跨租户或跨应用复用。
4. `tenant_member_role` 负责成员到角色的授权；不再从用户或成员的 `role_ids` 数组读取权限。
5. `sys_menu` 的静态元数据以 `client_id` 隔离；`sys_role_menu` 负责角色到菜单的关系。
6. OAuth token 必须关联 `user_id`、`client_id`、`tenant_id` 上下文；缺失业务身份必须拒绝，不能回退到 demo 字面量。
7. M05 API Key 和 M06 审计功能不属于本次目标 DDL，按功能树标记废弃。

## 运行链路

全局账号认证后，以 `client_id` 查询该用户可进入的租户；用户选择 `tenant_id` 后签发 token。动态菜单查询必须从 token 的三元组定位 `tenant_member`，再经 `tenant_member_role → sys_role → sys_role_menu → sys_menu` 联表，并限制同一 `client_id`。

## 验收

- 目标 12 张表、UUID 主键、外键、唯一约束和联合主键可从空库重建。
- 旧表名和旧 API Key/审计表不再出现在目标 schema 或 OpenAPI。
- 跨 tenant、跨 client 和跨 member 的越权请求返回 401/403。
- OAuth code 一次性消费，access/refresh token 支持过期与撤销。
- `M04.F04` 的租户应用订阅端点与 contract-test 同步。
