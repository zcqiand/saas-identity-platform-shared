# UI 交互规格（ui-interaction-spec）

> 规范见 suite `docs/conventions/feature-parity.md` §3：契约仓持有家族级交互真相，
> 消费仓不各自发明行为定义。人填、人评审；机器只查锚点存在（挂进 design-map 即被 L5 覆盖）。
> **随做随补**：一个 I 级子项从「规划」转「开发中」前，必须有它的行。
> 红线：不写组件库名 / CSS；只写用户可观察的行为。

## M01.F04.I03 密码登录 UI

> 2026-09-11 B 方案裁决（SSO-first）首例，由 saas-e2e REQ-2026-001 首跑发现的三端分歧倒逼；
> 依据 shared `LoginRequest` 契约（clientId required）+ 项目所有者裁决。

| 项 | 值 |
|---|---|
| 入口 | `/login`（直接打开或 OAuth 跳板带 `?redirect_uri=&state=&client_id=`） |
| UI 形态 | 独立登录页（绕过 AppShell；错误 toast 由登录页**自挂** Toaster，三端同款） |
| clientId 门 | 提交前必须解析出 clientId：`?client_id=` ?? env `LOGIN_CLIENT_ID`（值 = saas-console 自身应用 `11111111-1111-1111-1111-111111111114`，seed 于 apps fixture / oauth_client 表）；两者皆缺 → toast「缺少 clientId…」且**不发请求** |
| 字段 | 用户名 Input（id=username）必填；密码 Input（id=password）必填 |
| 提交 | 按钮挂 `data-fn=M01.F04.I03`；请求体 `{username, password, clientId}`（clientId 为 LoginRequest required 字段） |
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
