# 租户成员 + 角色成员 设计映射 — M00.F02 / M01.F02

> **2026-09-07 模块重组**：本文件覆盖原 M01.F01（用户 CRUD）+ M01.F02（角色分配与状态）。
> 新结构下：
> - **M00.F02**（租户成员）= 原 M01.F01（CRUD + 邀请 + 接受 + 状态）
> - **M01.F02**（角色成员）= 原 M01.F02 中"分配角色"单项
>
> 迁移表见 [function-tree.md §0.x](../functions/function-tree.md#0x-模块重组迁移记录2026-09-07)。

## 1. 设计映射（已上线 ID）

| 子项 ID | 端点 | 设计要点 | 对应合同 |
|---|---|---|---|
| **M00.F02.I02** | `POST /tenants/{tenantId}/users` | 创建成员；CreateUserRequest{username,email,password,displayName?,roleIds?} → TenantMemberView；区别于 **M00.F02.I06** 邀请（邀请是发邮件，本 op 直接落地） | `tsp/routes/tenant-users.tsp:17 @post createUser` |

## 2. 数据模型

`users` 表（`sql/migrations/V002__init_users_memberships.sql`）：

| 列 | 类型 | 说明 |
|---|---|---|
| id | uuid | PK |
| tenant_id | uuid | FK tenants（**重要**：tenant-scoped 强约束，L0.no_fallback 门禁） |
| username | varchar | tenant 内唯一 |
| email | varchar | tenant 内唯一 |
| password_hash | varchar | bcrypt 12 rounds |
| display_name | varchar? | 可选 |
| status | varchar | active / invited / suspended |
| created_at | timestamptz | 默认 now() |

`tenant_memberships` 表（同 migration）：

| 列 | 类型 | 说明 |
|---|---|---|
| id | uuid | PK |
| tenant_id | uuid | FK tenants |
| user_id | uuid | FK users |
| role_ids | uuid[] | FK roles（**冗余占位**，authoritative 在 role_permissions 解析；4 后端 User DTO 必须 LEFT JOIN tenant_memberships 取真值，否则 roleIds=[] 与真值不一致） |
| joined_at | timestamptz | 默认 now() |

## 3. 设计决策

### 3.1 为什么 role_ids 在 memberships 而不是 users

`users.role_ids` 列在 4 后端 array 映射（hypersistence-utils / drizzle / JPA / EF）有差异，**不可靠**。authoritative 真值在 `tenant_memberships`，DTO 必须 JOIN 取真值。详见 memory: `users-role-ids-redundant-authoritative-memberships`。

### 3.2 tenant-scoped 强约束

所有 user 查询必须带 `WHERE tenant_id = ?currentTenantId`。currentTenantId 来自 session，**不接受请求体覆盖**（ADR-0019）。

### 3.3 创建 vs 邀请边界

| 入口 | 端点 | 用途 | 副作用 |
|---|---|---|---|
| **M00.F02.I02** 创建 | `POST /tenants/{t}/users` | admin 直接落地（密码由 admin 设定或系统生成） | 写 `users` + `tenant_member` + `tenant_member_role` |
| **M00.F02.I06** 邀请 | `POST /tenants/{t}/invitations` | 发邮件让用户自注册（7 天 token） | 写 invitations 表 + 发邮件（msw 返 mock token）|
| **M00.F02.I07** 接受邀请 | `POST /invitations/{token}/accept` | 用户点链接后落地 | 写 `tenant_memberships` + invitation 行标 accepted |

详见 [REQ-2026-024 §2.3](../requirements/REQ-2026-024-tenant-switch-and-user-invite.md)。

## 4. 跨仓实现位置

| 仓 | 实现 |
|---|---|
| saas-aspnetcore | `TenantUsersController.Create` (POST) |
| saas-springboot | `TenantUsersController.create` |
| saas-nextjs | `app/api/tenants/[tenantId]/users/route.ts` |
| saas-msw | `handlers/tenant-users.ts` mock |

## 5. 验收测试覆盖

- `contract-test/tests/tenant-users.test.ts`：4 后端行为对齐
- 4 后端 `POST /tenants/{t}/users` 单测：成功 / 重名 409 / 跨租户硬塞 403
