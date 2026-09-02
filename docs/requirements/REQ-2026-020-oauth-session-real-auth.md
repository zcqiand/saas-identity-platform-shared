# REQ-2026-020 saas OAuth 真用户认证 — saas session cookie 体系

| 项 | 值 |
|---|---|
| 提出人 | ADR-0013（family OAuth 简化设计收口） |
| 提出日期 | 2026-08-27 |
| 优先级 | P0 |
| 状态 | 已评审（路线 A 批准） |
| 关联 ADR | [0013-saas-oauth-skip-user-auth.md](../../../saas-identity-platform-aspnetcore/docs/adr/0013-saas-oauth-skip-user-auth.md) |

## 1. 需求描述

> **照抄用户原话**（2026-08-27）：saas 登录不停留，直接带 code 回 lab /login，这个正常的吗？不
> 应该要输入用户名和密码登录后切换到带 code 回 lab /login。

**理解**：

saas-aspnetcore 当前 `/api/v1/oauth/authorize` + `/api/v1/oauth/token` + `/api/v1/me/menus`
三个端点 **不查 saas 用户身份** — `OauthController.cs:136-138` 直接
`Users.FirstOrDefaultAsync(u => u.TenantId == body.TenantId)` 拿该 tenant 第一个 user 发 token。
注释自承「Phase 5 mock」「dev 暂不验 clientSecret」。

后果：
- 浏览器跳 saas `/login` 后**无法停留输密码** — saas-nextjs 没有 login UI（404/自动跳走）
- 即使有 UI，token 端点接受任意 tenantId → 跳过真认证
- prod OAuth 流程「能跑通」是因为根本不是真 OAuth

**目标（路线 A）**：
- saas-aspnetcore OAuth 三个端点**强制检查 saas session cookie**
- 未登录访问 → 401，浏览器被前端 login UI 截获 → 用户输密码 → 存 session cookie → 重新走 OAuth
- `/api/v1/me/menus` 同款（必须 saas session + user_id）
- saas-vue / saas-react LoginPage 提交 username + password → POST `/api/v1/auth/login` 拿 session
- saas-msw 同步 session cookie mock
- lab-aspnetcore `LAB_SSO_LOGIN_URL` 改指向 saas-vue / saas-react 域名（不再 saas-nextjs）

### 澄清记录

| 疑问 | 澄清结论 | 澄清人 | 日期 |
|---|---|---|---|
| saas session cookie 还是 saas JWT bearer？ | cookie — 跨页跨 API 自动带；同 saas 家族 lab-aspnetcore / lab-nextjs 共用 saas UI 跨域时 cookie 自动发（需 SaasBase + 跨源 AllowCredentials） | — | 2026-08-27 |
| saas session 用 HttpOnly cookie 还是普通 cookie？ | HttpOnly — 防 XSS；SameSite=Lax — 顶层跳转带 cookie（authorize 跨源需 Lax） | — | 2026-08-27 |
| saas 仓需不需要登录 UI？ | 需要 — saas-vue / saas-react LoginPage 提交 username + password；saas-nextjs 没有登录 UI 应被排除 | — | 2026-08-27 |
| lab-aspnetcore deploy env LAB_SSO_LOGIN_URL 重指向哪个？ | saas-react.xiangru.uk 或 saas-vue.xiangru.uk（dev 默认其中 dev）。需要 family 文档承认 | — | 2026-08-27 |
| saas 失败锁定（连续 5 次密码错）还是无限重试？ | 5 次 / 15min 锁定；与 saas-aspnetcore 现有 auth 表加 lockout_until 字段；Phase 2 加 I 子项 M03.F01.I02 | — | 2026-08-27 |
| saas session 存哪？ | 进程内 ConcurrentDictionary（与现有 oauth_codes / oauth_tokens 同款）；Phase 6+ 切 Redis；Phase 1 不引入新依赖 | — | 2026-08-27 |

## 2. 验收标准

| 编号 | 场景（给定） | 操作（当） | 预期（则） |
|---|---|---|---|
| AC-1 | 用户未登录访问 saas `/api/v1/oauth/authorize` | POST 不带 cookie | 401 `UNAUTHORIZED`；body `{code: "UNAUTHORIZED", message: "saas session required"}` |
| AC-2 | 用户未登录访问 saas `/api/v1/me/menus` | GET 不带 cookie | 401 `UNAUTHORIZED` |
| AC-3 | 用户 saas `/api/v1/auth/login` 输错密码 5 次 | 第 6 次 login | 423 `LOCKED`；body `{code: "LOCKED", message: "账户锁定 15min"}` |
| AC-4 | 用户 saas `/api/v1/auth/login` 输对密码 | POST login | 200 + Set-Cookie `saasSession=<sid>; HttpOnly; SameSite=Lax`；body `{accessToken, refreshToken, user, tenants}` |
| AC-5 | 用户已 saas session，访问 `/api/v1/oauth/authorize` 带 cookie | POST authorize | 200 `{code, state}` |
| AC-6 | lab 后端用 code 调 `/api/v1/oauth/token`（带 cookie） | POST token | 200 `{accessToken, refreshToken, user_id}` — user_id 从 session 注入，不接受请求体 user_id / tenantId 直发 |
| AC-7 | saas `/api/v1/me/menus` 用 saas session 内 user_id 查角色授权 | GET menus | 200 `{<appCode>: [menus...]}` |
| AC-8 | saas-vue / saas-react LoginPage 输入 alice/dev123456 | 提交 | 浏览器跳 saas 授权流；sessionStorage 写 saas session；cookie 自动带 |
| AC-9 | lab-vue 用户 `/login` → lab 后端 authorize → 拼 saas-vue/login?code&redirect_uri&state | 浏览器跳 | saas-vue login UI 显示「已登录 alice，是否授权 lab-mgmt」；点确认 → 302 lab/callback?code |
| AC-10 | saas-msw mock 全覆盖：login + authorize + token + me/menus + lockout | vitest run | 5 用例 TDD 全过；cookie `saasSession` 自动带（msw cookie jar） |

## 3. 任务拆解

> 任务 ID 由 executing-plans 派子代理；预估按 task 2-5 分钟一条；每个任务必须 red-first 测试先行。

| 任务 ID | 任务描述 | 类型 | 仓 | 预估 | 状态 |
|---|---|---|---|---|---|
| T-1 | saas-aspnetcore: `SaasSessionStore` (进程内 ConcurrentDictionary + TTL 24h) | 后端 | saas-aspnetcore | 30min | 待开始 |
| T-2 | saas-aspnetcore: `SaasSessionMiddleware` 解析 cookie 注入 `HttpContext.Items["saasSession"]` | 后端 | saas-aspnetcore | 20min | 待开始 |
| T-3 | saas-aspnetcore: `AuthController.Login` 接受 username/password → 校验 user + 设 lockout + 写 session cookie + 签 access/refresh | 后端 | saas-aspnetcore | 1h | 待开始 |
| T-4 | saas-aspnetcore: `OauthController.Authorize` 检查 session，401 时返 `saasSessionRequired` 让前端跳 login UI | 后端 | saas-aspnetcore | 30min | 待开始 |
| T-5 | saas-aspnetcore: `OauthController.Token` (Authorization_code / Refresh_token) 用 session 内 user_id 注入 user_id 列 | 后端 | saas-aspnetcore | 1h | 待开始 |
| T-6 | saas-aspnetcore: `MeController.Menus` 检查 session + user_id 查角色授权（替代 Phase 5 占位） | 后端 | saas-aspnetcore | 1h | 待开始 |
| T-7 | saas-msw: handlers-extra 加 `/auth/login` + `/auth/logout` + cookie jar；改 `/oauth/authorize` + `/oauth/token` + `/me/menus` 检查 session | mock | saas-msw | 1h | 待开始 |
| T-8 | saas-vue: LoginPage.vue 提交 username/password → POST login → 写 store；session cookie 自动带 | 前端 | saas-vue | 30min | 待开始 |
| T-9 | saas-react: LoginPage.tsx 同款 | 前端 | saas-react | 30min | 待开始 |
| T-10 | lab-aspnetcore: deploy 脚本 `LAB_SSO_LOGIN_URL` 改指向 saas-react 或 saas-vue（dev 默认其中 dev） | deploy | lab-aspnetcore | 10min | 待开始 |
| T-11 | saas-aspnetcore tests: OauthController / AuthController / MeController 测试覆盖 AC-1/2/3/4/5/6/7 | 测试 | saas-aspnetcore | 2h | 待开始 |
| T-12 | saas-msw tests: handlers-extra 测试覆盖 AC-10 cookie jar | 测试 | saas-msw | 1h | 待开始 |

## 4. 功能影响（需求与功能对齐的唯一位置）

> ID 已在 [function-tree.md §子项级](../functions/function-tree.md) 登记（ADR-0013 路线 A）。

| 功能 ID | 功能名称 | 影响类型 | 说明 | 关联任务 |
|---|---|---|---|---|
| M03.F01 | 密码登录与失败锁定 | 变更 | 状态 规划→开发中；saas 家族登录 UI 走 username/password | T-3/T-8/T-9 |
| M03.F01.I01 | 密码登录 API | 新增 | AuthController.Login: username + password → session cookie | T-3 |
| M03.F01.I02 | 失败锁定 | 新增 | 5 次密码错 → 15min 锁定 | T-3 |
| M03.F01.I03 | 密码登录 UI | 新增 | saas-vue / saas-react LoginPage | T-8/T-9 |
| M04.F03 | OAuth 授权码签发与令牌交换/刷新 | 变更 | 状态 规划→开发中；真 OAuth（session 校验） | T-4/T-5 |
| M04.F03.I01 | OAuth authorize 检查 saas session | 新增 | OauthController.Authorize 检查 session cookie | T-4 |
| M04.F03.I02 | OAuth token 交换 | 新增 | session 内 user_id 注入 | T-5 |
| M04.F03.I03 | OAuth refresh token 旋转 | 新增 | session 校验 | T-5 |
| M09.F03 | 当前用户有效菜单 | 变更 | 状态 规划→开发中；session 校验 + 角色授权 | T-6 |
| M09.F03.I01 | me/menus session 校验 | 新增 | MeController.Menus 检查 session | T-6 |

## 5. 流程影响

引用 `docs/design/flow-function-map.md`：

- 之前 OAuth 流程：`lab → lab authorize → saas authorize (直发 code) → saas /login (无 UI) → 浏览器跳回 lab callback → lab 用 code 换 saas token`
- 现在 OAuth 流程：`lab → lab authorize → saas authorize (验 session, 无 session 返 401) → 浏览器跳 saas login UI → 用户输密码 → POST /auth/login 写 session → 浏览器跳回 saas authorize → saas authorize (session 已存) → 发 code → lab callback → lab 用 code 换 token (session 内 user_id)`

lab-aspnetcore 部署期 `LAB_SSO_LOGIN_URL` 由 `https://saas-nextjs.xiangru.uk` 改为 `https://saas-react.xiangru.uk`（或 saas-vue）。

## 6. 风险与回滚

| 风险 | 影响面 | 缓解 | 回滚方式 |
|---|---|---|---|
| saas session cookie 与 lab-aspnetcore 现有 Set-Cookie  冲突 | cookie name 命名空间 | saasSession + Secure + Path=/api | 改名 |
| 跨源 saas UI 域名 cookie 不发 → 登录失败 | saas-vue/react 与 saas-aspnetcore 不同 origin | SameSite=Lax + Secure；CORS AllowCredentials | 加白名单 origin |
| saas 家族 session TTL 24h 长，密码改了不立即失效 | 安全 | 加 forced logout / session list by user_id | 强制刷新 session 表 |
| saas-msw cookie jar 与 prod 行为不同步 | dev/prod drift | 用 mswjs/cookies API + tests/handlers-extra 验证 cookie jar 行为 | 跳过 dev 端 cookie 严格断言 |
| lab-aspnetcore LAB_SSO_LOGIN_URL 重指向后 lab-nextjs / lab-vue 旧 build 仍指 saas-nextjs | 多仓构建错位 | 同时升级 4 仓（saas-aspnetcore + saas-vue + saas-react + lab-aspnetcore） | 留 fallback 同时支持两个域名 |

## 7. PLAN 索引

- `docs/plans/PLAN-2026-001-oauth-session.md` — 12 任务微任务清单（含 fn-ID + 测试先行 + 验证命令）