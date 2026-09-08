# OAuth 2.0 流程设计映射 — M04.F03

> 与 [docs/conventions/oauth-flow.md](../conventions/oauth-flow.md) 配套：本文件给 L5 设计映射用；conventions/ 给人类读 RFC 6749 路线 A 的「为什么」。
>
> **2026-09-07 模块重组**：旧 M04.F03.I07-I09 重编号为 **M04.F03.I01-I03**（I01-I03 早期实验段已废弃；当前三个 canonical I01/I02/I03 即旧 I07/I08/I09）。
> 迁移表见 [function-tree.md §0.x](../functions/function-tree.md#0x-模块重组迁移记录2026-09-07)。

## 1. 设计映射（已上线 ID）

| 子项 ID | 端点 | 设计要点 | 对应合同 |
|---|---|---|---|
| **M04.F03.I01** | `POST /oauth/authorize` | 签 authorization_code；必须 saas session（`cookie: saasSession=...`），未登录返 401 让前端跳 login UI | `tsp/routes/oauth.tsp:10 @route /authorize` |
| **M04.F03.I02** | `POST /oauth/token`（grant_type=authorization_code）| code → access_token + refresh_token；session 内 user_id 注入 user_id 列，**禁止**接受请求体 user_id/tenantId | `tsp/routes/oauth.tsp:15 @route /token` |
| **M04.F03.I03** | `POST /oauth/token`（grant_type=refresh_token） | refresh_token 旋转（同 session 校验） | 同上（同一端点按 grant_type 分支）|

## 2. 跨仓实现位置

| 仓 | 实现 |
|---|---|
| saas-aspnetcore | `OauthController.Authorize` / `OauthController.Token` |
| saas-springboot | `OauthController.Authorize` / `OauthController.Token` |
| saas-nextjs | `app/api/oauth/authorize/route.ts` + `app/api/oauth/token/route.ts` |
| saas-msw | `handlers/oauth.ts` mock（含 cookie jar）|
| saas-vue / saas-react | LoginPage 跳板（带 `redirect_uri` + `state` + `client_id`）|

## 3. 安全约束（设计层面的硬约束）

- **session 强制**：authorize 端点必须 saas session；token 端点不强制（RFC 6749 §4.1.3 允许 token 端点单独验 client_credentials）
- **user_id 注入**：session.UserId → token.user_id；不接受 body 覆盖
- **code 一次性**：oauth_codes 表 code 字段 UNIQUE + 单次消费（V014 seed 验证）
- **refresh_token 旋转**：每次 refresh 发新 token，旧 token 立即失效（V014 设计）
- **client_secret 后端持有**：业务前端→SSO 不直发 secret；业务后端 server-to-server 走 secret

## 4. 与既有 ADR 的关系

- ADR-0013：路线 A 批准（资源所有者直接认证，不再信任 body.TenantId 直发）
- ADR-0014：saas OAuth 真用户认证收口（含 T-1 ~ T-12 落地）
- ADR-0023：cross-repo SSOT 覆盖映射（本文件作为该 ADR 的实现）

## 5. 历史教训（避免重蹈）

- **2026-08-29 fix**：lab-aspnetcore / lab-springboot 业务后端不再代理 authorize（避免 saas session 跨后端不可达），改 302 跳 SSO 登录页
- **M04.F03 编号历史**：早期实验段 I01-I03 与 I07-I09 同 op 重复，2026-09-07 重组时废弃 I01-I03 旧编号、保留 I07-I09 作 canonical；本次重组后 canonical 重编号为 **M04.F03.I01-I03**（与早期 I01-I03 同号但不同内容，注意时序）
