# PLAN-2026-001 — saas OAuth 真用户认证 4 仓改造

> **状态：✅ 已完成（2026-08-27）** - T-1 ~ T-12 全部落地，收口见
> [ADR-0014](../../../saas-identity-platform-aspnetcore/docs/adr/0014-saas-oauth-session-completed.md)
>
> **REQ**: [REQ-2026-020-oauth-session-real-auth.md](../requirements/REQ-2026-020-oauth-session-real-auth.md)
> **ADR**: [0013-saas-oauth-skip-user-auth.md](../../../saas-identity-platform-aspnetcore/docs/adr/0013-saas-oauth-skip-user-auth.md)
> **路线**: A（完整修 OAuth，4 仓改造）
> **策略**: red-first（每任务测试先红→最小实现转绿）→ 仓内 gate L0-L5 → commit + push + 推进父仓指针

## 串行依赖

```
saas-aspnetcore 后端 (T-1 ~ T-6, T-11)  ──┐
saas-msw mock       (T-7, T-12)          ├── 并行 ──> 集成测试
saas-vue/react 前端 (T-8, T-9)            │
lab-aspnetcore deploy (T-10)              ┘
```

- **saas-aspnetcore** 必须先做完（T-1~T-6 + T-11）：saas session 设计是源头
- **saas-msw** 可在 saas-aspnetcore 后端后做（T-7 + T-12）：mock 必须与真后端契约一致
- **saas-vue / saas-react** LoginPage 依赖后端登录端点（可与 saas-aspnetcore 并行做契约 mock）
- **lab-aspnetcore deploy env** 最后做（T-10）：需等 saas UI 部署上线后才指向

## 任务清单

### 任务 1: saas-aspnetcore SaasSessionStore

- **fn-ID**: M03.F01.I01
- **文件**: `src/Auth/Session/SaasSessionStore.cs` (新建) + `tests/Auth/Session/SaasSessionStoreTest.cs` (新建)
- **测试先行**: 写 `tests/Auth/Session/SaasSessionStoreTest.cs`，断言 Put/Get/Delete/TTL 行为；3 用例
- **入口**: 后端 SaasSessionStore 进程内 ConcurrentDictionary, key=sessionId, value={userId, tenantId, createdAt, expiresAt}
- **验证**: `dotnet test tests/Lab.AspNetCore.Tests.csproj --filter "FullyQualifiedName~SaasSessionStoreTest"` → 红→绿

### 任务 2: saas-aspnetcore SaasSessionMiddleware

- **fn-ID**: M03.F01.I01
- **文件**: `src/Auth/Session/SaasSessionMiddleware.cs` (新建) + `tests/Auth/Session/SaasSessionMiddlewareTest.cs`
- **测试先行**: 写请求带 `Cookie: saasSession=<sid>` → middleware 解析 + 注入 `HttpContext.Items["saasSession"]`；无 cookie 或过期不注入
- **入口**: 在 Program.cs 注册 `app.UseMiddleware<SaasSessionMiddleware>()`
- **验证**: `dotnet test --filter "FullyQualifiedName~SaasSessionMiddlewareTest"` → 红→绿

### 任务 3: saas-aspnetcore AuthController.Login + 失败锁定

- **fn-ID**: M03.F01.I01 + M03.F01.I02
- **文件**: `src/Controllers/Implementation/AuthController.cs` + `tests/Controllers/AuthControllerTests.cs`
- **测试先行**: 8 用例 — 成功登录 / 错密码 / 错密码 5 次锁定 / 已锁定返 423 / 已锁定时间未到返 423 / 锁定过期重试成功 / 不存在 user / 空字段 400
- **入口**: AuthController.Login 接受 LoginRequest(Username, Password)，设 `Set-Cookie: saasSession=<new-sid>; HttpOnly; SameSite=Lax; Secure; Path=/api`；返 LoginResponse(User, Tenants, AccessToken, RefreshToken)
- **验证**: `dotnet test --filter "FullyQualifiedName~AuthController"` → 红→绿

### 任务 4: saas-aspnetcore OauthController.Authorize 检查 session

- **fn-ID**: M04.F03.I01
- **文件**: `src/Controllers/Implementation/OauthController.cs`
- **测试先行**: 5 用例 — 无 session 返 401 / session 过期返 401 / session 有效返 code / 不合法 clientId 返 401 / redirect_uri 不在白名单返 400
- **入口**: Authorize 方法头部 `var session = HttpContext.Items["saasSession"] as SaasSession; if (session is null) throw new UnauthorizedAccessException("saas session required");`
- **验证**: `dotnet test --filter "FullyQualifiedName~OauthController.Authorize"` → 红→绿

### 任务 5: saas-aspnetcore OauthController.Token 用 session.user_id 注入

- **fn-ID**: M04.F03.I02 + M04.F03.I03
- **文件**: `src/Controllers/Implementation/OauthController.cs`
- **测试先行**: 6 用例 — 无 session 返 401 / session 但 code 不匹配 → 401 / 成功 code→token 返回 user_id 是 session 内 user_id / refresh_token 旋转 / refresh_token 重放返 401 / code 已消费返 401
- **入口**: Token 方法头部同样检查 session；从 session 拿 user_id + tenantId，不再用 body.TenantId
- **验证**: `dotnet test --filter "FullyQualifiedName~OauthController.Token"` → 红→绿

### 任务 6: saas-aspnetcore MeController.Menus 检查 session + 角色授权

- **fn-ID**: M09.F03.I01
- **文件**: `src/Controllers/Implementation/MeController.cs`
- **测试先行**: 4 用例 — 无 session 返 401 / session 但 user 没租户 返 200 空 map / 用户角色绑定菜单返 map / 跨 appCode 隔离
- **入口**: Menus 方法查 `var session = HttpContext.Items["saasSession"] as SaasSession;` + `var userRoles = await _db.RoleMenuGrants.Where(g => g.UserId == session.UserId).ToListAsync();`
- **验证**: `dotnet test --filter "FullyQualifiedName~MeController.Menus"` → 红→绿

### 任务 7: saas-msw handlers-extra 同步

- **fn-ID**: M03.F01.I01 + M04.F03.I01-I03 + M09.F03.I01
- **文件**: `src/handlers-extra.ts` (修改) + `tests/handlers-extra.test.ts`
- **测试先行**: 5 用例 — POST /auth/login 成功 / 错密码 / 5 次错返 423 / GET /oauth/authorize 无 cookie 返 401 / POST /oauth/token 不接受 tenantId 直发
- **入口**: 加 saas-session cookie jar（mswjs/cookies）；/auth/login 写入 cookie；/oauth/* 检查 cookie
- **验证**: `npx vitest run tests/handlers-extra.test.ts` → 红→绿

### 任务 8: saas-vue LoginPage.vue 提交 username/password

- **fn-ID**: M03.F01.I03
- **文件**: `src/pages/LoginPage.vue` (修改) + `tests/features/auth/loginPage.dom.test.ts`
- **测试先行**: 4 用例 — 提交 username/password → POST /auth/login 调通 / 显示错误 / 显示锁定提示 / 成功后跳 /tenants
- **入口**: LoginPage 表单 onSubmit → useAuthLogin mutation → 成功后 router.push("/tenants")
- **验证**: `npx vitest run tests/features/auth/loginPage.dom.test.ts` → 红→绿

### 任务 9: saas-react LoginPage.tsx 同款

- **fn-ID**: M03.F01.I03
- **文件**: `src/pages/LoginPage.tsx` (修改) + `tests/features/auth/loginPage.dom.test.tsx`
- **测试先行**: 同上 4 用例
- **验证**: `npx vitest run tests/features/auth/loginPage.dom.test.tsx` → 红→绿

### 任务 10: lab-aspnetcore deploy 脚本改 LoginUrl

- **fn-ID**: （无 function-tree 变更）
- **文件**: `deploy/lab-management-system-aspnetcore.sh`
- **入口**: `LAB_SSO_LOGIN_URL` 默认改 `https://saas-react.xiangru.uk`（dev 可配 saas-vue）
- **验证**: 手动跑一次 deploy 看 env 写出 + cat env 文件看 LoginUrl 值正确

### 任务 11: saas-aspnetcore tests 集成覆盖

- **fn-ID**: M03.F01.I01-I02 + M04.F03.I01-I03 + M09.F03.I01
- **文件**: `tests/Controllers/OauthControllerTests.cs` (扩充) + `tests/Controllers/MeControllerTests.cs`
- **测试先行**: 8 用例 — 完整 OAuth 流程（login → session cookie → authorize → token → me/menus）
- **验证**: `dotnet test` 全过 + `python scripts/gate.py -p saas-identity-platform-aspnetcore` L0-L5 全绿

### 任务 12: saas-msw tests cookie jar

- **fn-ID**: M03.F01.I01 + M04.F03.I01-I03 + M09.F03.I01
- **文件**: `tests/handlers-extra.test.ts`
- **测试先行**: 5 用例 — cookie 设置 + cookie 带过来 / 过期清除 / 同源 cookie jar 行为
- **验证**: `npx vitest run` + `python scripts/gate.py -p saas-identity-platform-msw` L0-L5 全绿

## 验收闸

每个任务完成必须：
1. 任务内测试先红→实现转绿（red-first）
2. `python scripts/gate.py -p <仓>` L0-L5 全绿
3. commit message 标 `M<xx>.F<yy>.I<zz>` + 一句话

整个 PLAN 完成后：
1. 5 仓（saas-aspnetcore + saas-msw + saas-vue + saas-react + lab-aspnetcore）逐个 tag + push
2. 父仓 suite 推进 5 仓指针
3. 在 docs/adr/ 写 ADR-0014 收口（成功完成 OAuth session 改造）

## 跨仓同步

每仓完成后必须：
1. 仓内 gate 全绿 → commit + tag + push
2. 父仓 `output/<仓>` 指针推进 → commit + tag + push

**不能**一个仓 commit 多个任务后一次性 push — 每任务独立 tag 便于回滚。

## 升级闸（三次修不好 = ADR）

同一任务 **3 次仍红** → 停手，写 ADR-0014 续篇，贴给人。