# REQ-2026-024 saas 跨租户切换 + 用户邀请流程

> **2026-09-07 模块重组注意**：本 REQ 文档是历史快照，§5 中列出的 `M00.F02` / `M01.F01-F03` 已重新归属。
> 关键映射：旧 M00.F02 → **M01.F01**（whoami）+ **M01.F03**（list/switch）；旧 M01.F01 → **M00.F02**；旧 M01.F02 → **M00.F02**（字段/状态/邀请）+ **M01.F02**（角色成员）。
> 完整迁移表见 [function-tree.md §0.x](../functions/function-tree.md#0x-模块重组迁移记录2026-09-07)。

| 项 | 值 |
|---|---|
| 提出人 | saas-react selection.app stores code not UUID 教训（lab-vue 默认 lab 路径测不出）+ ADR-0019 tenantId 兜底禁 |
| 提出日期 | 2026-09-07 |
| 优先级 | P1 |
| 状态 | 已评审 |
| 关联 ADR | [0019-business-identity-no-fallback.md](../../../docs/adr/0019-business-identity-no-fallback.md) |

## 1. 需求描述

saas 多租户平台的两个常用 UX 流：

**A. 同一用户跨租户切换**：用户 alice 同时是 tenant T1（admin）+ T2（viewer）。当前 UI 选 tenant T2 → 所有 me-menus / audit / api-keys 查询必须切到 T2 scope，不能跨租户漏数据。

**B. tenant admin 邀请新用户**：admin 输入 email + role → 系统发邀请邮件 → 用户点链接注册 → membership 自动建立。

**现状缺口**：

1. **whoami 缺 tenant 多态**：单 saas session 内如何表达"用户当前在哪个 tenant scope"？需要显式 current_tenant_id 字段
2. **切换语义不清**：切换时 in-flight API 请求怎么办？client cache 怎么 invalidate？
3. **邀请邮件通道未定义**：M01.F02.I02 邀请用户已挂 `POST /invitations`，但邮件发送实现（SendGrid / SMTP / SES）由消费仓选

## 2. 设计决策

### 2.1 当前 tenant scope 表示

- **session 内的 current_tenant_id**（cookie 加密字段）：whoami 返 `{user, tenants, currentTenantId}`
- 切换走 `POST /me/tenants/{tenantId}/switch` → 服务端更新 session.cookie.currentTenantId → 后续 me/* 读这个字段
- **强制约束**：所有 tenant-scoped 端点必须从 session 取 current_tenant_id，**禁止**接受请求体/查询参数里的 tenantId 覆盖（ADR-0019 兜底禁令）

### 2.2 切换期间一致性

- 切换是**乐观**：服务端不持有 per-request 事务锁（性能优先）
- 切换原子性：单 session cookie 写 → 下次请求生效；in-flight 请求仍按切前 tenant 完成
- 前端 cache：切换后调一次 `me/menus` + `me/tenants` 重新 hydrate；不依赖 TTL 自然过期

### 2.3 用户邀请流程

| 步骤 | 端点 | 副作用 |
|---|---|---|
| 1 | admin `POST /tenants/{t}/invitations {email, roleIds}` | 建 invitation 行（token + expires_at 7 天）+ 发邮件 |
| 2 | 用户点邮件链接 | 浏览器跳 `/invitations/accept?token=xxx` |
| 3 | 未登录用户 | 跳 LoginPage → 输密码 → 写 session → 自动 accept |
| 4 | 已登录用户 | 直接调 `POST /invitations/{token}/accept` → 建 tenant_memberships 行 + 绑 roleIds |

**关键约束**：
- invitation token 单次使用，accept 后失效（行不删，留 audit）
- email 通道：实现仓自选；msw 不发邮件，返 mock token
- 邀请人 actor_user_id 写 audit_events（action=user_invited）

## 3. 验收标准

| 编号 | 场景（给定） | 操作（当） | 预期（则） |
|---|---|---|---|
| AC-1 | 用户 alice 是 T1 admin + T2 viewer；session 内 currentTenantId=T1 | `GET /me` | 200 `{user, tenants:[{id:T1,role:admin},{id:T2,role:viewer}], currentTenantId:T1}` |
| AC-2 | 同上，调 `POST /me/tenants/T2/switch` | 切换 | 200；session.cookie.currentTenantId=T2；后续 `GET /me` 返 currentTenantId=T2 |
| AC-3 | 用户在 T2 scope 调 `GET /tenants/T1/audit-events` | 跨租户尝试 | 403 `TENANT_MISMATCH`（不允许硬跨） |
| AC-4 | T1 admin 调 `POST /tenants/T1/invitations {email:"bob@x.com", roleIds:[r1]}` | 邀请 | 201；invitation 行 created；audit_events 增 `user_invited` 行；msw 返 mock token + email-stub |
| AC-5 | bob 点邮件链接 `?token=xxx`，未登录 | 浏览器跳 LoginPage | 输密码 → POST /auth/login → 自动 accept → 写 tenant_memberships(T1, role=r1) |
| AC-6 | bob 已登录 T2 session（admin），收 T1 邀请 | 调 `POST /invitations/{token}/accept` | 201；tenant_memberships(T1, bob, r1) 新建；invitation 行标 accepted |
| AC-7 | 已 accepted 的 token 二次 accept | 重放 | 409 `INVITATION_ALREADY_USED` |
| AC-8 | 过期 token（>7d） | accept | 410 `INVITATION_EXPIRED` |

## 4. 任务拆解

| 任务 ID | 任务描述 | 类型 | 仓 | 预估 | 状态 |
|---|---|---|---|---|---|
| T-1 | saas-aspnetcore: session cookie 加 currentTenantId 字段 | 后端 | saas-aspnetcore | 30min | 待开始 |
| T-2 | saas-springboot: 同款 | 后端 | saas-springboot | 30min | 待开始 |
| T-3 | saas-nextjs: route handler session 加 currentTenantId | 后端 | saas-nextjs | 20min | 待开始 |
| T-4 | saas-aspnetcore: MeController.GetTenants + SwitchTenant | 后端 | saas-aspnetcore | 30min | 待开始 |
| T-5 | saas-springboot: 同款 | 后端 | saas-springboot | 30min | 待开始 |
| T-6 | saas-nextjs: 同款 route handler | 后端 | saas-nextjs | 20min | 待开始 |
| T-7 | saas-aspnetcore: TenantUsersController.InviteUser (POST /invitations) | 后端 | saas-aspnetcore | 1h | 待开始 |
| T-8 | saas-springboot: 同款 | 后端 | saas-springboot | 1h | 待开始 |
| T-9 | saas-nextjs: 同款 | 后端 | saas-nextjs | 1h | 待开始 |
| T-10 | saas-msw: /me/tenants + switch + /invitations handlers | mock | saas-msw | 1h | 待开始 |
| T-11 | contract-test: 切换 + 邀请 + 跨租户拒 8 个 AC | 测试 | contract-test | 2h | 待开始 |

## 5. 功能影响

> ID 已在 [function-tree.md §子项级](../functions/function-tree.md) 登记。

| 功能 ID | 功能名称 | 影响类型 | 说明 | 关联任务 |
|---|---|---|---|---|
| M00.F02 | 当前用户跨租户切换 | 变更 | 状态 规划→开发中；session currentTenantId + 4 后端实装 | T-1/T-2/T-3/T-4/T-5/T-6/T-10 |
| M00.F02.I01 | 当前用户 whoami | 变更 | 返 `{user, tenants[], currentTenantId}` | T-4/T-5/T-6 |
| M00.F02.I02 | 列出我的租户成员关系 | 变更 | `GET /me/tenants` 返 memberships | T-4/T-5/T-6 |
| M00.F02.I03 | 切换当前租户 | 新增 | `POST /me/tenants/{tenantId}/switch` 写 session cookie | T-4/T-5/T-6 |
| M01.F02 | 用户角色分配与状态切换 | 变更 | 状态 规划→开发中；I01/I02/I03 实装 | T-7/T-8/T-9/T-10 |
| M01.F02.I02 | 邀请用户 | 新增 | `POST /tenants/{t}/invitations` + accept flow | T-7/T-8/T-9 |

## 6. 流程影响

引用 [flow-function-map.md §M00 §M01](../design/flow-function-map.md)：

- **跨租户切换**：UI 选 tenant → `POST /me/tenants/{tenantId}/switch` → session 写 → 重新 hydrate me/menus + 当前页 tenant scope 资源
- **邀请流程**：admin 输 email → 发邮件（mock 不发，返 token） → 用户点链接 → LoginPage（如未登录）→ Accept → 写 membership

## 7. 风险与回滚

| 风险 | 影响面 | 缓解 | 回滚方式 |
|---|---|---|---|
| 切租户期间 in-flight 请求返旧 tenant 数据 | UX 不一致 | 文档化「切换非事务」，前端切换前 spinner 等所有 in-flight 完成 | 强制 client refresh-on-switch |
| 邀请邮件通道选错（SendGrid 限流/退信） | 用户收不到 | 实现仓自选；msw 不发邮件；contract-test 不验邮件触达 | 退化为 admin 手动复制 invite link 发 |
| invitation token 泄漏 | 未授权用户加入 tenant | token 7 天过期 + 单次使用 + accept 时强制 session 校验邀请人 | 撤销 invitation 行 |
| 跨租户硬切换绕过 currentTenantId | 数据泄露 | ADR-0019 兜底禁令 + L0.no_fallback 门 + TenantGuard 中间件 | 强制走 currentTenantId |
