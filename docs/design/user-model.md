# 租户成员 + 角色成员 + 租户角色 设计映射 — M00.F02 / M01.F02 / M00.F03

> **2026-09-07 模块重组**：本文件覆盖原 M01.F01（用户 CRUD）+ M01.F02（角色分配与状态）。
> 新结构下：
> - **M00.F02**（租户成员）= 原 M01.F01（CRUD + 邀请 + 接受 + 状态）
> - **M01.F02**（角色成员）= 原 M01.F02 中"分配角色"单项
> - **M00.F03**（租户角色）= 租户 × client 作用域角色 CRUD（2026-09-19 Task 3.5 并入本文件）
>
> 迁移表见 [function-tree.md §0.x](../functions/function-tree.md#0x-模块重组迁移记录2026-09-07)。

## 1. 设计映射（已上线 ID）

### 1.1 租户成员 CRUD（tenant-members.tsp）

| 子项 ID | 端点 | 设计要点 | 对应合同 |
|---|---|---|---|
| **M00.F02.I01** | `GET /tenants/{tenantId}/members` | 成员列表（分页 + 角色/状态过滤）；DTO roleIds 必须 LEFT JOIN `tenant_member_role` 取真值（users.role_ids 冗余列不可信） | `tsp/routes/tenant-members.tsp:11 @get listTenantUsers` |
| **M00.F02.I03** | `GET /tenants/{tenantId}/members/{userId}` | 成员详情（嵌套 member + user 视图）；跨租户寻址由 tenant guard 拒绝（403/404） | `tsp/routes/tenant-members.tsp:24 @get getTenantUser` |
| **M00.F02.I04** | `PATCH /tenants/{tenantId}/members/{userId}` | 更新成员基础字段（email/mobile 等，不动角色关系） | `tsp/routes/tenant-members.tsp:29 @patch updateTenantUser` |
| **M00.F02.I05** | `DELETE /tenants/{tenantId}/members/{userId}` | 只断 membership，不删全局 `sys_user` 行 | `tsp/routes/tenant-members.tsp:38 @delete deleteTenantUser` |
| **M00.F02.I02** | `POST /tenants/{tenantId}/members` | 创建成员；CreateSysUserRequest{username,email,password,displayName?,roleIds?} → TenantMemberUserView；区别于 **M00.F02.I06** 邀请（邀请是发邮件，本 op 直接落地） | `tsp/routes/tenant-members.tsp:20 @post createTenantUser` |
| **M00.F02.I08** | `PATCH /tenants/{tenantId}/members/{userId}/status` | 启用/停用成员（写 `tenant_member.status`；DB 值非 enum 序号，见 aspnetcore Status 测试） | `tsp/routes/tenant-members.tsp:61 @patch changeTenantUserStatus` |

### 1.2 角色成员（分配角色）

| 子项 ID | 端点 | 设计要点 | 对应合同 |
|---|---|---|---|
| **M01.F02.I01** | `PUT /tenants/{tenantId}/members/{userId}/roles` | 用角色 ID 集合全量覆盖该成员的当前角色绑定（写 `tenant_member_role` 关系行） | `tsp/routes/tenant-members.tsp:43 @put assignTenantMemberRoles` |

### 1.3 租户角色 CRUD（tenant-roles.tsp）

| 子项 ID | 端点 | 设计要点 | 对应合同 |
|---|---|---|---|
| **M00.F03.I01** | `GET /tenants/{tenantId}/roles` | 角色（tenant × client 作用域）列表（分页 + 过滤） | `tsp/routes/tenant-roles.tsp:8 @get listSysRoles` |
| **M00.F03.I02** | `POST /tenants/{tenantId}/roles` | 创建角色：CreateSysRoleRequest{code,name,clientId,...}；作用域 = tenant_id × client_id，code 在作用域内唯一 | `tsp/routes/tenant-roles.tsp:17 @post createSysRole` |
| **M00.F03.I03** | `GET /tenants/{tenantId}/roles/{roleId}` | 角色详情（元信息 + 作用域） | `tsp/routes/tenant-roles.tsp:21 @get getSysRole` |
| **M00.F03.I04** | `PATCH /tenants/{tenantId}/roles/{roleId}` | 更新角色名称/描述（不动权限与菜单绑定） | `tsp/routes/tenant-roles.tsp:26 @patch updateSysRole` |
| **M00.F03.I05** | `DELETE /tenants/{tenantId}/roles/{roleId}` | 删除角色并清理成员角色绑定与权限/菜单关联 | `tsp/routes/tenant-roles.tsp:35 @delete deleteSysRole` |

## 2. 数据模型

> 2026-09-19（Task 5.22）按现 `src/db/schema.ts` 更正：pivot 后用户是全局实体（`sys_user`），租户归属拆到 `tenant_member`，角色关系行在 `tenant_member_role`；旧 `users` / `tenant_memberships` 表名与 `role_ids` 数组列已废弃删除。

`sys_user`（全局用户实体）：

| 列 | 类型 | 说明 |
|---|---|---|
| id | uuid | PK，默认 uuid_generate_v4() |
| username | varchar(64) | NOT NULL，全局唯一 `uk_sys_user_username` |
| password | varchar(255) | NOT NULL（bcrypt） |
| email | varchar(128)? | 可空，全局唯一 `uk_sys_user_email` |
| mobile | varchar(32)? | 可空，全局唯一 `uk_sys_user_mobile` |
| status | smallint | NOT NULL 默认 1；值域 1=active / 2=invited / 0=disabled（家族约定 2026-09-10） |
| failed_attempts | integer | NOT NULL 默认 0（**M01.F04.I02** 连续失败锁定计数） |
| locked_until | timestamptz? | 可空（锁定截止时间） |
| created_at / updated_at | timestamptz | NOT NULL，默认 CURRENT_TIMESTAMP |

`tenant_member`（租户归属；tenant-scoped 强约束）：

| 列 | 类型 | 说明 |
|---|---|---|
| id | uuid | PK，默认 uuid_generate_v4() |
| tenant_id | uuid | NOT NULL，FK tenants（cascade）；member 查询必须带 tenant 边界（L0.no_fallback） |
| user_id | uuid | NOT NULL，FK sys_user（cascade） |
| member_name | varchar(64)? | 可空 |
| is_owner | boolean | NOT NULL 默认 false |
| status | smallint | NOT NULL 默认 1（**M00.F02.I08** 启用/停用写此列） |
| created_at / updated_at | timestamptz | NOT NULL，默认 CURRENT_TIMESTAMP |
| — | — | 附加：`uk_tenant_user` unique(tenant_id, user_id) + idx user_id / tenant_id |

`tenant_member_role`（角色关系行，roleIds authoritative）：

| 列 | 类型 | 说明 |
|---|---|---|
| member_id | uuid | PK 组成，FK tenant_member.id（cascade） |
| role_id | uuid | PK 组成，FK sys_role.id（cascade） |
| — | — | 附加：primaryKey(member_id, role_id) + `idx_tenant_member_role_role_id`(role_id) |

## 3. 设计决策

### 3.1 为什么 roleIds 走关系行而不是数组列

`role_ids` 数组列在 4 后端 array 映射（hypersistence-utils / drizzle / JPA / EF）有差异，**不可靠，已从 schema 删除**（`src/db/schema.ts` 头注释：role/menu grants are relational rows, never role_ids/menu_ids arrays）。authoritative 真值在 `tenant_member_role` 关系行，DTO 必须 JOIN 取真值。详见 memory: `users-role-ids-redundant-authoritative-memberships`。

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
