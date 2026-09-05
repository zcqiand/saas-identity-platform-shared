# 功能清单（Function Tree）— saas-identity-platform-shared

> **全体系唯一锚点。** 需求、流程、设计、测试都引用这里的 ID。
> 不在这里的 ID 是悬空引用，L5 门会拦。**改功能，先改这份表。**

## 编号规则

| 层级 | 名称 | 格式 | 含义 |
|---|---|---|---|
| 一级 | 功能模块 | `M0x` | 业务域边界，通常对应一级菜单（实际命名见各仓模块总览） |
| 二级 | 功能 | `M0x.F0y` | 一个完整业务步骤 / 独立闭环流程 / 数据管理页面 |
| 三级 | 功能子项 | `M0x.F0y.I0z` | 技术交付单元 / 权限挂载点。对应一个 API 接口、页面组件、图表区块或权限控制点 |

**硬规则**

1. 编号单调递增，永不复用。废弃改状态，不删行。
2. 子项编号必须以父级为前缀。
3. 一个子项 = 一个权限点。权限码即 ID，不另起一套编码。
4. 拆不出子项的功能 → 它其实是子项，往上并。子项超 20 个 → 它其实是模块，往下拆。

**状态**：`规划` | `开发中` | `已上线` | `已废弃`
**子项类型**：`页面` | `标签页` | `查询` | `按钮` | `报表` | `接口`

## 本仓角色

**契约 BASE 仓**（spec §3）。F+I 级：I 级为全家族定标源；消费仓 ⊆ BASE。
范围：M00 租户 / M01 用户 / M02 角色 / M03 SSO / M04 OAuth / M05 API Key / M06 审计 / M08 菜单 / M09 菜单授权。
多租户：tenant 跨切是平台默认语义；tenant 真相源在本仓。
M97/M98/M99/M96（infra/契约专属段）不进 BASE，由各消费仓自管。

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
| M01.F01.I02 | 创建用户 | 接口 | 前端+后端 | 创建用户 POST `/tenants/{t}/users`（CreateUserRequest{username,email,password,displayName?,roleIds?} → User；区别于 I03 邀请是发邮件而非直接落地） | 已上线 |

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
| M03.F01.I01 | 密码登录 API | 接口 | 前端+后端 | 密码登录 API（username + password → saas session cookie + access token） | 开发中 |
| M03.F01.I02 | 失败锁定 | 接口 | 前端+后端 | 失败锁定（连续 5 次密码错 → 锁定 15min） | 开发中 |
| M03.F01.I03 | 密码登录 UI | 接口 | 前端+后端 | 密码登录 UI（saas-vue / saas-react LoginPage 提交 username + password） | 规划 |

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
| M04.F03.I01 | OAuth authorize 检查 s | 接口 | 前端+后端 | OAuth authorize 检查 saas session（未登录返 401） | 规划 |
| M04.F03.I02 | OAuth token 交换 — ses | 接口 | 前端+后端 | OAuth token 交换 — session 内 user_id 注入（不再 tenantId 直发） | 规划 |
| M04.F03.I03 | OAuth refresh token | 接口 | 前端+后端 | OAuth refresh token 旋转（同 session 校验） | 规划 |

---

## M05 API Key 管理

| 功能 ID | 功能名称 | 说明 | 状态 |
|---|---|---|---|
| M05.F01 | API Key 生命周期（tenant-scoped） | （说明待补） | 规划 |

### M05.F01 API Key 生命周期（tenant-scoped）

| 子项 ID | 名称 | 类型 | 交付 | 说明 | 状态 |
|---|---|---|---|---|---|
| M05.F01.I01 | 列表分页 | 接口 | 前端+后端 | 列表分页 GET `/tenants/{t}/api-keys?page&pageSize` | 规划 |
| M05.F01.I02 | 创建 | 接口 | 仅前端 | 创建 POST `/tenants/{t}/api-keys`（返回一次性 secret） | 规划 |
| M05.F01.I03 | 吊销 | 接口 | 前端+后端 | 吊销 POST `/tenants/{t}/api-keys/{k}/revoke`（idempotent） | 规划 |
| M05.F01.I04 | 轮换 | 接口 | 前端+后端 | 轮换 POST `/tenants/{t}/api-keys/{k}/rotate`（revoke old + create new） | 规划 |
| M05.F01.I05 | 物理删除 | 接口 | 前端+后端 | 物理删除 DELETE `/tenants/{t}/api-keys/{k}`（直接删 DB 行，与 I03 revoke 软删并存：revoke 保留审计行，本 op 不留痕；幂等——重复删返 404） | 已上线 |

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
| M06.F01.I01 | 列表审计事件 | 接口 | 前端+后端 | 列表审计事件 GET `/tenants/{t}/audit-events?page&pageSize&action&actorUserId&from&to` | 规划 |
| M06.F01.I02 | 按用户查审计事件 | 接口 | 前端+后端 | 按用户查审计事件 GET `/tenants/{t}/audit-events/by-user/{userId}` | 规划 |
| M06.F01.I03 | 导出审计事件 | json → downloadUrl） | 前端+后端 | saas-aspnetcore, saas-springboot, saas-nextjs, saas-msw | 规划 |

### M06.F02 审计留存策略

| 子项 ID | 名称 | 类型 | 交付 | 说明 | 状态 |
|---|---|---|---|---|---|
| M06.F02.I01 | 读取审计留存策略 | 接口 | 前端+后端 | 读取审计留存策略 GET `/tenants/{t}/audit-events/retention` | 规划 |
| M06.F02.I02 | 设置审计留存策略 | 接口 | 前端+后端 | 设置审计留存策略 PUT `/tenants/{t}/audit-events/retention`（{retentionDays} → {retentionDays}） | 规划 |

### M06.F03 审计写入助手（写端点副作用）

| 子项 ID | 名称 | 类型 | 交付 | 说明 | 状态 |
|---|---|---|---|---|---|
| M06.F03.I01 | AuditWriter.WriteAsy | 接口 | 前端+后端 | AuditWriter.WriteAsync(tenantId, actorUserId, action, metadata) — api-key 写端点副作用（api_key_created / api_key_revoked） | 规划 |
| M06.F03.I02 | 列表 `?action=` 过滤 | 接口 | 前端+后端 | 列表 `?action=` 过滤（msw 之前缺，导致 4 后端不对称） | 规划 |

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
| M09.F02.I01 | 整批设置角色菜单 | 接口 | 前端+后端 | 整批设置角色菜单 PUT `/tenants/{t}/roles/{r}/menus`（SetRoleMenusRequest{menuIds:[]} → RoleMenuGrant{roleId,tenantId,menuIds,updatedAt}；整批替换语义，幂等） | 规划 |

### M09.F03 当前用户有效菜单

| 子项 ID | 名称 | 类型 | 交付 | 说明 | 状态 |
|---|---|---|---|---|---|
| M09.F03.I01 | me/menus session 校验 | 接口 | 前端+后端 | me/menus session 校验（已存在 F03 端点，加 session 校验） | 规划 |
| M09.F03.I02 | 角色授权菜单 ID 查询 | 接口 | 前端+后端 | 角色授权菜单 ID 查询（membership.roleIds → role_menu_grants.menuIds） | 开发中 |
| M09.F03.I03 | 菜单树装配 | 接口 | 前端+后端 | 菜单树装配（menuIds → menus 表 + 父链补全 + 按 app 分组） | 开发中 |
| M09.F03.I04 | app 分组映射 | 接口 | 前端+后端 | app 分组映射（按 app.code 取代 appId 输出 Map<appCode, List<EffectiveMenuNode>>） | 已上线 |