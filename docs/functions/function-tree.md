# saas-identity-platform-shared 功能树

> 多租户 SaaS 身份管理。Phase B 起由 `tsp/main.tsp` 派生；本表为占位骨架。

## 模块总览

| ID  | 模块 | 业务域边界 | 状态 |
|-----|------|-----------|------|
| M00 | 租户管理 | 多租户 CRUD、跨租户切换 | 规划 |
| M01 | 用户管理 | tenant-scoped 用户 CRUD、角色分配 | 规划 |
| M02 | 角色权限 | tenant-scoped 角色、权限矩阵 | 规划 |
| M03 | SSO 登录 | 密码登录、OIDC 回调、登出 | 规划 |
| M04 | 应用与 OAuth | 平台级 App（菜单承载 + OAuth client）CRUD、授权码/令牌流程 | 规划 |

> M04 备注：V014 起 seed lab-mgmt OAuth client（id `11111111-1111-1111-1111-111111111111`，client_id `lab-mgmt`，3 个 saas 后端共用同一 app.id）+ oauth_codes 表（Phase 6 真 OAuth，替代 saas-nextjs 进程内 oauth-store）。
| M05 | API Key 管理 | tenant-scoped Key 生命周期 | 规划 |
| M06 | 审计日志 | tenant-scoped 审计事件、留存策略 | 规划 |
| M08 | 菜单 | 应用下树形菜单 CRUD、结构维护 | 规划 |
| M09 | 菜单授权 | tenant-role ↔ 菜单授权、当前用户有效菜单 | 规划 |

## 功能级（M0x.F0y）

| ID       | 功能 | 类型 | 状态 |
|----------|------|------|------|
| M00.F01  | 租户 CRUD（平台 admin） | 接口 | 规划 |
| M00.F02  | 当前用户跨租户切换 | 接口 | 规划 |
| M01.F01  | 用户 CRUD（tenant-scoped） | 接口 | 规划 |
| M01.F02  | 用户角色分配与状态切换 | 接口 | 规划 |
| M02.F01  | 角色 CRUD（tenant-scoped） | 接口 | 规划 |
| M02.F02  | 权限绑定（角色↔权限矩阵） | 接口 | 规划 |
| M03.F01  | 密码登录与失败锁定 | 接口 | 开发中 |
| M03.F02  | OIDC 回调与 IDToken 校验 | 接口 | 规划 |
| M03.F03  | 登出（本地清理 + 全局 SSO） | 接口 | 规划 |
| M04.F01  | 应用 CRUD（平台 admin） | 接口 | 规划 |
| M04.F02  | 应用启用/停用 | 接口 | 规划 |
| M04.F03  | OAuth 授权码签发与令牌交换/刷新 | 接口 | 开发中 |
| M05.F01  | API Key 生命周期（tenant-scoped） | 接口 | 规划 |
| M06.F01  | 审计事件查询（tenant-scoped） | 查询 | 规划 |
| M06.F02  | 审计留存策略 | 接口 | 规划 |
| M08.F01  | 菜单 CRUD（应用下） | 接口 | 规划 |
| M08.F02  | 菜单结构维护（排序/父级） | 接口 | 规划 |
| M09.F01  | 角色菜单授权查询 | 查询 | 规划 |
| M09.F02  | 角色菜单授权设置 | 接口 | 规划 |
| M09.F03  | 当前用户有效菜单 | 查询 | 已上线 |

## 子项级（M0x.F0y.I0z）— ADR-0013 真 OAuth session 改造

| ID | 子项 | 镜像仓（消费方） | 状态 |
|----|------|------------------|------|
| M03.F01.I01 | 密码登录 API（username + password → saas session cookie + access token） | saas-aspnetcore (OauthController / AuthController), saas-msw (handlers-extra) | 规划 |
| M03.F01.I02 | 失败锁定（连续 5 次密码错 → 锁定 15min） | saas-aspnetcore (AuthController) | 规划 |
| M03.F01.I03 | 密码登录 UI（saas-vue / saas-react LoginPage 提交 username + password） | saas-vue, saas-react | 规划 |
| M04.F03.I01 | OAuth authorize 检查 saas session（未登录返 401） | saas-aspnetcore (OauthController.Authorize) | 规划 |
| M04.F03.I02 | OAuth token 交换 — session 内 user_id 注入（不再 tenantId 直发） | saas-aspnetcore (OauthController.ExchangeAuthorizationCode) | 规划 |
| M04.F03.I03 | OAuth refresh token 旋转（同 session 校验） | saas-aspnetcore (OauthController.RotateRefreshToken) | 规划 |
| M09.F03.I01 | me/menus session 校验（已存在 F03 端点，加 session 校验） | saas-aspnetcore (MeController.Menus), saas-msw (handlers-extra) | 已上线 |
| M09.F03.I02 | 角色授权菜单 ID 查询（membership.roleIds → role_menu_grants.menuIds） | saas-springboot (MeService.getMyMenus), saas-aspnetcore (MeService) | 已上线 |
| M09.F03.I03 | 菜单树装配（menuIds → menus 表 + 父链补全 + 按 app 分组） | saas-springboot (MeService.getMyMenus) | 已上线 |
| M09.F03.I04 | app 分组映射（按 app.code 取代 appId 输出 Map<appCode, List<EffectiveMenuNode>>） | saas-springboot (MeService.getMyMenus), saas-aspnetcore (MeService.Menus) | 已上线 |
