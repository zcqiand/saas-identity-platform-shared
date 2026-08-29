# OAuth 2.0 流程 (RFC 6749, 无 PKCE)

> 2026-08-29 路线 A 收口后的标准 OAuth 2.0 authorization_code flow。
> 落地对应代码改动：saas-aspnetcore v0.3.20 + lab-aspnetcore v0.2.11 + lab-springboot v0.1.29。

## 流程图

```text
[用户]
  ↓
[1. 访问业务前端 (Vue/React/Next.js)] ——未登录——→ [2. 重定向到 SSO 授权页 (saas-{vue,react,nextjs}.xiangru.uk/login?redirect_uri&state&client_id)]
                                                                  |
[3. 用户在 SSO 页面输入账号密码] ←----------------------------------+
  ↓
[4. SSO 校验成功 (写 saasSession cookie)] ——→ [5. 302 重定向回业务前端 (URL 携带 ?code=xxx&state)]
                                                                  ↓
[6. 业务前端拦截 code] ——→ [7. POST 请求 SSO 的 /token 端点 (带 code + client_id + redirect_uri + client_secret)]
                                                                  ↓
[8. SSO 校验 code 有效性] ——→ [9. 返回 Access Token & Refresh Token]
                                                                  ↓
[10. 业务前端保存 Token，后续请求 Header 携带: Authorization: Bearer <Token>]
                                                                  ↓
[11. 业务前端 → 业务后端 API] ——→ [12. 业务后端解析 JWT 公钥验签并返回业务数据]
```

## 角色映射

| 角色 | 仓 | 职责 |
|---|---|---|
| 业务前端 (RP) | lab-vue / lab-react / lab-nextjs / saas-vue / saas-react / saas-nextjs | 发起 OAuth 跳 SSO、拦截 code、存 Bearer token、调业务后端 |
| SSO 端 (Authorization Server) | saas-{vue,react,nextjs} + saas-aspnetcore / saas-springboot | 密码登录 UI、签 code、签 access_token/refresh_token |
| 业务后端 (Resource Server) | lab-aspnetcore / lab-springboot | 持 client_secret 调 SSO /token、用 SSO 公钥本地验签 Bearer token |

## 安全约束

- `client_secret` 由业务后端持有（绝不入前端）；token 端点调用走后端 server-to-server
- `code` 绑定 saas session 的 user_id/tenant_id（authorize 端点验 saasSession）；token 端点不再要 session，只验 code + client_credentials
- 业务后端调 SSO /token 用 `client_credentials`（保密），业务前端 → 业务后端用 Bearer token（业务后端用 SSO 公钥本地验签，不调 SSO）

## 历史背景

- **ADR-0013**：saas OAuth 路线 A 批准（资源所有者直接认证，不再信任 body.TenantId 直发 token）
- **ADR-0014**：saas OAuth 真用户认证收口（路线 A 完成，含 T-1 ~ T-12）
- **2026-08-29 修复**：lab-aspnetcore v0.2.11 / lab-springboot v0.1.29 业务后端不再代理 authorize（避免 saas session 跨后端不可达），改 302 跳 SSO 登录页；saas-aspnetcore v0.3.20 `/token` 端点放宽 session 要求（RFC 6749 §4.1.3），`/authorize` 端点补 `session.UserId/TenantId` 绑定到 oauth_code

## 相关仓库

- 契约源头（本仓）：`tsp/routes/oauth.tsp` (AuthorizeCodeRequest / TokenRequest / Response3 / TokenResponse)
- 后端实现：saas-aspnetcore / saas-springboot（OAuthController）
- 前端 SSUI：saas-vue / saas-react / saas-nextjs LoginPage（处理 redirect_uri 参数）
- 业务前端：lab-vue / lab-react / lab-nextjs LoginPage（跳 SSO 登录页、拦截 code）
- 业务后端：lab-aspnetcore / lab-springboot（AuthController.SsoAuthorize / SsoCallback、SaasAuthClient）
