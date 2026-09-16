# UI 交互规格（ui-interaction-spec）

> 规范见 suite `docs/conventions/feature-parity.md` §3：契约仓持有家族级交互真相，
> 消费仓不各自发明行为定义。人填、人评审；机器只查锚点存在（挂进 design-map 即被 L5 覆盖）。
> **随做随补**：一个 I 级子项从「规划」转「开发中」前，必须有它的行。
> 红线：不写组件库名 / CSS；只写用户可观察的行为。

## M01.F04.I01 密码登录（UI 部分）

> 2026-09-11 B 方案裁决（SSO-first）首例，由 saas-e2e REQ-2026-001 首跑发现的三端分歧倒逼；
> 依据 shared `LoginRequest` 契约（clientId required）+ 项目所有者裁决。

| 项 | 值 |
|---|---|
| 入口 | `/login`（直接打开或 OAuth 跳板带 `?redirect_uri=&state=&client_id=`） |
| UI 形态 | 独立登录页（绕过 AppShell；错误 toast 由登录页**自挂** Toaster，三端同款） |
| clientId 门 | 提交前必须解析出 clientId：`?client_id=` ?? env `LOGIN_CLIENT_ID`（值 = saas-console 自身应用 `11111111-1111-1111-1111-111111111114`，seed 于 apps fixture / oauth_client 表）；两者皆缺 → toast「缺少 clientId…」且**不发请求** |
| 字段 | 用户名 Input（id=username）必填；密码 Input（id=password）必填 |
| 提交 | 按钮挂 `data-fn=M01.F04.I01`；请求体 `{username, password, clientId}`（clientId 为 LoginRequest required 字段） |
| 成功 | 有 `?code=&redirect_uri=`（authorize 回跳）→ 302 redirect_uri?code&state；有 `?redirect_uri=&client_id=`（跳板）→ 调 authorize 拿 code 跳 RP；否则落 `/tenants` |
| 响应缺 token | accessToken/refreshToken 任缺 → toast「登录响应缺少 token，请联系管理员」，停留登录页 |
| 错误凭据（401） | toast「用户名或密码错误」，停留 `/login`，不跳转 |
| 账号锁定（423/429） | toast 含「锁定」提示（vue 带倒计时 + 禁用提交按钮），停留登录页 |
| 防重复提交 | submitting 期间按钮禁用（文案变「登录中…」） |
| session 写入 | userId 取 `LoginResponse.user.id`；currentTenantId 取 `availableTenants[0].tenantId` |

## M00.F01 租户管理（平台 admin CRUD）

> 2026-09-11 REQ(e2e)-2026-003 首次成文；字段对齐 shared SSOT（tenantKey，9/7 起 code→tenantKey）。

| 项 | 值 |
|---|---|
| 入口 | 侧边栏「租户管理」→ `/tenants` |
| 列表 | 表格行（TableRow）；列 = Code(tenantKey, mono) / 名称 / 状态徽章 / 操作；空态「还没有租户」 |
| 表单 | Dialog（CrudDialog）：tenantKey（必填，placeholder acme）/ 名称（必填）/ 状态 select（**仅 active 启用 / suspended 暂停**，契约无 archived） |
| 创建 | 新建按钮 data-fn=M00.F01.I02 → Dialog → 保存 → toast「租户已创建」+ 行出现 |
| 更新 | 行内编辑按钮 data-fn=M00.F01.I04 → Dialog 预填 → 保存 → toast「租户已更新」+ 行文本更新 |
| 删除 | 行内删除按钮 data-fn=M00.F01.I05 → **AlertDialog 二次确认**（确认按钮文案「删除」）→ 确认 → toast「租户已删除」+ 行消失；确认动作必须在异步完成后才关弹窗（受控契约） |
| 失败 | 各操作 toast.error（「创建失败：…」等），停留当前态 |

## M01.F04.I06 登出（本地清理）+ M01.F03.I02 切换当前租户

> 2026-09-11 REQ(e2e)-2026-004 首次成文。

| 项 | 值 |
|---|---|
| 登出入口 | 侧边栏底部「登出」按钮 data-fn 同子项 ID |
| 登出行为 | **await 登出 API 后**清本地 session 再跳 /login（时序错会被路由守卫拦回——vue 实锤修过）；登出后直访工作区被踢回 /login |
| 切换入口 | 顶栏切换器（data-testid=tenant-switcher）Dropdown |
| 切换数据 | 成员关系 GET /me/tenants（TenantMember[]，契约无租户名）；显示名 join 平台租户列表（管理台自身数据源） |
| 切换行为 | 选中项 → POST /me/tenants/:id/switch → 新 token 落 session → 进该租户工作区（路由实现三端允许不同）+ 列表缓存失效；404 toast「该租户不存在或你不是其成员」 |
| 过滤 | 状态非 active 的成员关系不出现在切换列表 |

## M00.F03 角色管理（tenant-scoped CRUD）

> 2026-09-11 REQ(e2e)-2026-005（③a）首次成文；字段对齐 SSOT（SysRole: roleCode/roleName/clientId/isPreset/status）。

| 项 | 值 |
|---|---|
| 入口 | 侧边栏「角色管理」→ /tenants/:tenantId/roles |
| 列表 | Code(roleCode, mono) / 名称(roleName)；无「权限」列（契约无 permissionIds，权限面由菜单授权承接） |
| 表单 | roleCode（必填）/ roleName（必填）；创建提交归并 clientId="saas-console"（契约必填；管理台自建角色归属自身 client，表单不暴露） |
| 创建/更新/删除 | 同租户管理范式（Dialog + toast + 行出现/更新/消失；删除 AlertDialog「删除」确认） |
| 权限矩阵按钮 | **已废弃**（PUT permissions 契约下线）；三端不得再渲染该入口 |

## M00.F04 角色菜单授权（③b）

> 2026-09-11 REQ(e2e)-2026-005 ③b 首次成文；字段对齐 SSOT（SysMenu: clientId/title/type(directory|menu|button)/status:number）。

| 项 | 值 |
|---|---|
| 入口 | 角色列表行内「菜单授权」→ /tenants/:t/roles/:r/menus |
| 矩阵 | 按 client 分组的勾选列表（标题 + path）；每 client 独立菜单数据 |
| 初始态 | 回读 GET 该角色 grant，勾选数反映 seed/上次保存真值（不得本地臆断初始计数） |
| 保存 | 按钮显「保存 (N)」实时计数；PUT menuIds 集合 → toast「菜单授权已保存」 |
| 清空 | 「清空」按钮归零计数（不自动保存） |
| 写测试纪律 | E2E 写操作自产自销——保存后必须还原，勿污染后续读断言（msw 内存跨用例共享） |

## M04.F03 OAuth 跳板登录（SSO，RFC 6749 §4.1.1/§4.1.2）

> 2026-09-11 REQ(e2e)-2026-006 首次成文——ADR-0015 留给 E2E 的浏览器语义层。

| 项 | 值 |
|---|---|
| 跳板 URL | /login?client_id=&redirect_uri=&state=（无 code） |
| 登录前 | 表单正常渲染；守卫不得抢跳 /tenants（三端守卫都必须豁免跳板范式） |
| 登录成功 | 调 POST /oauth/authorize（真源，**禁止 barrel 死桩**——假 code 跳 RP 是最危险假绿）→ 302/跳转 redirect_uri?code&state |
| payload | 契约 AuthorizeCodeRequest：clientId/redirectUri/responseType/scope/state（**无 tenantId**；code 绑认证身份） |
| 非法 redirect_uri | authorize 400 → 停留登录页 + toast，不得跳非白名单域 |
| 已登录直访跳板 URL | 无需再认证，自动 authorize 领 code 回跳（不渲染表单卡死） |
| code 回跳 | /login?code=&redirect_uri=&state= → 解析即回跳 RP（原样透传，防 code 泄漏到日志） |
