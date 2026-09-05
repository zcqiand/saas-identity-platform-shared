# saas-identity-platform-shared 功能树

> 多租户 SaaS 身份管理。Phase B 起由 `tsp/main.tsp` 派生；本表为占位骨架。

## 模块总览

| 模块 ID | 模块名称 | 说明 | 状态 |
|---|---|---|---|
| M00 | 租户管理 | 多租户 CRUD、跨租户切换 | 规划 |
| M01 | 用户管理 | tenant-scoped 用户 CRUD、角色分配 | 开发中 |
| M02 | 角色权限 | tenant-scoped 角色、权限矩阵 | 规划 |
| M03 | SSO 登录 | 密码登录、OIDC 回调、登出 | 规划 |
| M04 | 应用与 OAuth | 平台级 App（菜单承载 + OAuth client）CRUD、授权码/令牌流程 | 规划 |
| M05 | API Key 管理 | tenant-scoped Key 生命周期 | 规划 |
| M06 | 审计日志 | tenant-scoped 审计事件、留存策略 | 规划 |
| M08 | 菜单 | 应用下树形菜单 CRUD、结构维护 | 规划 |
| M09 | 菜单授权 | tenant-role ↔ 菜单授权、当前用户有效菜单 | 开发中 |

---

## M00 租户管理

| 功能 ID | 功能名称 | 说明 | 状态 |
|---|---|---|---|
| M00.F01 | 租户 CRUD（平台 admin） | （说明待补） | 规划 |
| M00.F02 | 当前用户跨租户切换 | （说明待补） | 规划 |

---

## M01 用户管理

| 功能 ID | 功能名称 | 说明 | 状态 |
|---|---|---|---|
| M01.F01 | 用户 CRUD（tenant-scoped） | （说明待补） | 开发中 |
| M01.F02 | 用户角色分配与状态切换 | （说明待补） | 规划 |

### M01.F01 用户 CRUD（tenant-scoped）

| 子项 ID | 名称 | 类型 | 交付 | 说明 | 状态 |
|---|---|---|---|---|---|
| M01.F01.I02 | 创建用户 POST `/tenants/ | 接口 | 前端+后端 | 创建用户 POST `/tenants/{t}/users`（CreateUserRequest{username,email,password,displayName?,roleIds?} → User；区别于 I03 邀请是发邮件而非直接落地）（镜像仓：saas-aspnetcore (TenantUsersController.UsersPost), saas-springboot (TenantUsersController#createUser + TenantUsersService), saas-nextjs (app/api/v1/tenants/[tenantId]/users/route.ts), saas-msw (handlers-extra.usersExtraHandlers)） | 已上线 |

---

## M02 角色权限

| 功能 ID | 功能名称 | 说明 | 状态 |
|---|---|---|---|
| M02.F01 | 角色 CRUD（tenant-scoped） | （说明待补） | 规划 |
| M02.F02 | 权限绑定（角色↔权限矩阵） | （说明待补） | 规划 |

---

## M03 SSO 登录

| 功能 ID | 功能名称 | 说明 | 状态 |
|---|---|---|---|
| M03.F01 | 密码登录与失败锁定 | （说明待补） | 规划 |
| M03.F02 | OIDC 回调与 IDToken 校验 | （说明待补） | 规划 |
| M03.F03 | 登出（本地清理 + 全局 SSO） | （说明待补） | 规划 |

### M03.F01 密码登录与失败锁定

| 子项 ID | 名称 | 类型 | 交付 | 说明 | 状态 |
|---|---|---|---|---|---|
| M03.F01.I01 | 密码登录 API | 接口 | 前端+后端 | 密码登录 API（username + password → saas session cookie + access token）（镜像仓：saas-aspnetcore (OauthController / AuthController), saas-msw (handlers-extra)） | 开发中 |
| M03.F01.I02 | 失败锁定 | 接口 | 前端+后端 | 失败锁定（连续 5 次密码错 → 锁定 15min）（镜像仓：saas-aspnetcore (AuthController)） | 开发中 |
| M03.F01.I03 | 密码登录 UI | 接口 | 前端+后端 | 密码登录 UI（saas-vue / saas-react LoginPage 提交 username + password）（镜像仓：saas-vue, saas-react） | 规划 |

---

## M04 应用与 OAuth

| 功能 ID | 功能名称 | 说明 | 状态 |
|---|---|---|---|
| M04.F01 | 应用 CRUD（平台 admin） | （说明待补） | 规划 |
| M04.F02 | 应用启用/停用 | （说明待补） | 规划 |
| M04.F03 | OAuth 授权码签发与令牌交换/刷新 | （说明待补） | 规划 |

### M04.F03 OAuth 授权码签发与令牌交换/刷新

| 子项 ID | 名称 | 类型 | 交付 | 说明 | 状态 |
|---|---|---|---|---|---|
| M04.F03.I01 | OAuth authorize 检查 s | 接口 | 前端+后端 | OAuth authorize 检查 saas session（未登录返 401）（镜像仓：saas-aspnetcore (OauthController.Authorize)） | 规划 |
| M04.F03.I02 | OAuth token 交换 — ses | 接口 | 前端+后端 | OAuth token 交换 — session 内 user_id 注入（不再 tenantId 直发）（镜像仓：saas-aspnetcore (OauthController.ExchangeAuthorizationCode)） | 规划 |
| M04.F03.I03 | OAuth refresh token | 接口 | 前端+后端 | OAuth refresh token 旋转（同 session 校验）（镜像仓：saas-aspnetcore (OauthController.RotateRefreshToken)） | 规划 |

---

## M05 API Key 管理

| 功能 ID | 功能名称 | 说明 | 状态 |
|---|---|---|---|
| M05.F01 | API Key 生命周期（tenant-scoped） | （说明待补） | 规划 |

### M05.F01 API Key 生命周期（tenant-scoped）

| 子项 ID | 名称 | 类型 | 交付 | 说明 | 状态 |
|---|---|---|---|---|---|
| M05.F01.I01 | 列表分页 GET `/tenants/{ | 接口 | 前端+后端 | 列表分页 GET `/tenants/{t}/api-keys?page&pageSize`（镜像仓：saas-aspnetcore (TenantApiKeysController), saas-springboot, saas-nextjs, saas-msw (handlers-extra)） | 规划 |
| M05.F01.I02 | 创建 POST `/tenants/{t | 接口 | 仅前端 | 创建 POST `/tenants/{t}/api-keys`（返回一次性 secret）（镜像仓：saas-aspnetcore, saas-springboot, saas-nextjs, saas-msw） | 规划 |
| M05.F01.I03 | 吊销 POST `/tenants/{t | 接口 | 前端+后端 | 吊销 POST `/tenants/{t}/api-keys/{k}/revoke`（idempotent）（镜像仓：saas-aspnetcore, saas-springboot, saas-nextjs, saas-msw） | 规划 |
| M05.F01.I04 | 轮换 POST `/tenants/{t | 接口 | 前端+后端 | 轮换 POST `/tenants/{t}/api-keys/{k}/rotate`（revoke old + create new）（镜像仓：saas-aspnetcore, saas-springboot, saas-nextjs, saas-msw） | 规划 |
| M05.F01.I05 | 物理删除 DELETE `/tenant | 接口 | 前端+后端 | 物理删除 DELETE `/tenants/{t}/api-keys/{k}`（直接删 DB 行，与 I03 revoke 软删并存：revoke 保留审计行，本 op 不留痕；幂等——重复删返 404）（镜像仓：saas-aspnetcore (TenantApiKeysController.Delete{KeyId}), saas-springboot (TenantApiKeysController#deleteApiKey + TenantApiKeyService), saas-nextjs (app/api/v1/tenants/[tenantId]/api-keys/[keyId]/route.ts), saas-msw (handlers-extra.apiKeysExtraHandlers)） | 已上线 |

---

## M06 审计日志

| 功能 ID | 功能名称 | 说明 | 状态 |
|---|---|---|---|
| M06.F01 | 审计事件查询（tenant-scoped） | （说明待补） | 规划 |
| M06.F02 | 审计留存策略 | （说明待补） | 规划 |
| M06.F03 | 审计写入助手（写端点副作用） | （说明待补） | 规划 |

### M06.F01 审计事件查询（tenant-scoped）

| 子项 ID | 名称 | 类型 | 交付 | 说明 | 状态 |
|---|---|---|---|---|---|
| M06.F01.I01 | 列表审计事件 GET `/tenants | 接口 | 前端+后端 | 列表审计事件 GET `/tenants/{t}/audit-events?page&pageSize&action&actorUserId&from&to`（镜像仓：saas-aspnetcore, saas-springboot, saas-nextjs, saas-msw） | 规划 |
| M06.F01.I02 | 按用户查审计事件 GET `/tenan | 接口 | 前端+后端 | 按用户查审计事件 GET `/tenants/{t}/audit-events/by-user/{userId}`（镜像仓：saas-aspnetcore, saas-springboot, saas-nextjs, saas-msw） | 规划 |
| M06.F01.I03 | 导出审计事件 POST `/tenants/{t}/audit-events/export`（format: csv \ | json → downloadUrl） | 前端+后端 | saas-aspnetcore, saas-springboot, saas-nextjs, saas-msw | 规划 |

### M06.F02 审计留存策略

| 子项 ID | 名称 | 类型 | 交付 | 说明 | 状态 |
|---|---|---|---|---|---|
| M06.F02.I01 | 读取审计留存策略 GET `/tenan | 接口 | 前端+后端 | 读取审计留存策略 GET `/tenants/{t}/audit-events/retention`（镜像仓：saas-aspnetcore, saas-springboot, saas-nextjs, saas-msw） | 规划 |
| M06.F02.I02 | 设置审计留存策略 PUT `/tenan | 接口 | 前端+后端 | 设置审计留存策略 PUT `/tenants/{t}/audit-events/retention`（{retentionDays} → {retentionDays}）（镜像仓：saas-aspnetcore, saas-springboot, saas-nextjs, saas-msw） | 规划 |

### M06.F03 审计写入助手（写端点副作用）

| 子项 ID | 名称 | 类型 | 交付 | 说明 | 状态 |
|---|---|---|---|---|---|
| M06.F03.I01 | AuditWriter.WriteAsy | 接口 | 前端+后端 | AuditWriter.WriteAsync(tenantId, actorUserId, action, metadata) — api-key 写端点副作用（api_key_created / api_key_revoked）（镜像仓：saas-aspnetcore (AuditWriter), saas-springboot, saas-nextjs (lib/audit), saas-msw (handlers-extra)） | 规划 |
| M06.F03.I02 | 列表 `?action=` 过滤 | 接口 | 前端+后端 | 列表 `?action=` 过滤（msw 之前缺，导致 4 后端不对称）（镜像仓：saas-aspnetcore, saas-springboot, saas-nextjs, saas-msw (now)） | 规划 |

---

## M08 菜单

| 功能 ID | 功能名称 | 说明 | 状态 |
|---|---|---|---|
| M08.F01 | 菜单 CRUD（应用下） | （说明待补） | 规划 |
| M08.F02 | 菜单结构维护（排序/父级） | （说明待补） | 规划 |

---

## M09 菜单授权

| 功能 ID | 功能名称 | 说明 | 状态 |
|---|---|---|---|
| M09.F01 | 角色菜单授权查询 | （说明待补） | 规划 |
| M09.F02 | 角色菜单授权设置 | （说明待补） | 规划 |
| M09.F03 | 当前用户有效菜单 | （说明待补） | 已上线 |

### M09.F02 角色菜单授权设置

| 子项 ID | 名称 | 类型 | 交付 | 说明 | 状态 |
|---|---|---|---|---|---|
| M09.F02.I01 | 整批设置角色菜单 PUT `/tenan | 接口 | 前端+后端 | 整批设置角色菜单 PUT `/tenants/{t}/roles/{r}/menus`（SetRoleMenusRequest{menuIds:[]} → RoleMenuGrant{roleId,tenantId,menuIds,updatedAt}；整批替换语义，幂等）（镜像仓：saas-aspnetcore (TenantRoleMenusController.MenusPut), saas-springboot (TenantRoleMenuController#setRoleMenus + TenantRoleMenuService), saas-nextjs (app/api/v1/tenants/[tenantId]/roles/[roleId]/menus/route.ts), saas-msw (handlers-extra.roleMenuExtraHandlers)） | 规划 |

### M09.F03 当前用户有效菜单

| 子项 ID | 名称 | 类型 | 交付 | 说明 | 状态 |
|---|---|---|---|---|---|
| M09.F03.I01 | me/menus session 校验 | 接口 | 前端+后端 | me/menus session 校验（已存在 F03 端点，加 session 校验）（镜像仓：saas-aspnetcore (MeController.Menus), saas-msw (handlers-extra)） | 规划 |
| M09.F03.I02 | 角色授权菜单 ID 查询 | 接口 | 前端+后端 | 角色授权菜单 ID 查询（membership.roleIds → role_menu_grants.menuIds）（镜像仓：saas-springboot (MeService.getMyMenus), saas-aspnetcore (MeService)） | 开发中 |
| M09.F03.I03 | 菜单树装配 | 接口 | 前端+后端 | 菜单树装配（menuIds → menus 表 + 父链补全 + 按 app 分组）（镜像仓：saas-springboot (MeService.getMyMenus)） | 开发中 |
| M09.F03.I04 | app 分组映射 | 接口 | 前端+后端 | app 分组映射（按 app.code 取代 appId 输出 Map<appCode, List<EffectiveMenuNode>>）（镜像仓：saas-springboot (MeService.getMyMenus), saas-aspnetcore (MeService.Menus)） | 已上线 |