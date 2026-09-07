# CLAUDE.md — SaaS身份平台契约仓

> 书稿配套仓 + harness 门禁仓双身份。入口，不是手册。L0 门强制上限 60 行。
> 本仓为《（书稿信息待补）》案例（待补）的可运行配套工程，是书稿代码块的 **source of truth**。

## 1. 项目定位

SaaS 多租户多应用身份平台全家族的契约源头（纯契约仓）。`*.tsp` 是唯一真源，**只产出 OpenAPI 3.1 yaml**；
其它 6 仓通过 `generated/openapi/openapi.yaml` 消费契约。

## 2. 铁律

- **TDD**：先写失败测试 → 确认红 → 实现 → 确认绿 → commit
- **版本钉死**：依赖与 `version-lock.json` 的 `version_lock` 一致；不引入 lock 外的库
- **tag 即放行**：全量回归绿后打 `v<MAJOR>.<MINOR>.<PATCH>-<YYYYMMDD>`（如 `v0.2.13-20260826`）
- **功能清单是锚点**：改 function-tree 走 `/tree-change`；同 commit；废弃只改状态，编号不复用
- 禁止业务代码（handlers/services/controllers）
- 禁止生成语言专属产物（TS/Java/C#/Kotlin/Swift 客户端下放给消费方自己 generate）
- 禁止 npm runtime 依赖；devDep 白名单：`@typespec/*` + `drizzle-orm` + `drizzle-kit` + `postgres` + `pg`（ADR-0025）
- 禁止手写 OpenAPI yaml（必须由 `tsp compile` 生成）
- 禁止 npm package `exports` 暴露语言路径；只暴露 `./openapi`
- DB schema SSOT：`src/db/schema.ts`（ADR-0025 schema-first）。`drizzle/` 是 `drizzle-kit generate` 产物（`0000_*.sql` + `meta/_journal.json` + `meta/0000_snapshot.json`），**入 git**；**禁止手改**。改 schema 只改 `src/db/schema.ts` → `npm run db:generate` → 提交 `drizzle/`
- gen-shared 静默覆盖同名迁移是 FATAL —— 触发时先收敛分叉再跑（8/26 撞表雷教训）

## 3. 技术栈与版本（钉死于 version-lock.json）

TypeSpec → OpenAPI 3.1 + Drizzle ORM schema-first → SQL 迁移（`drizzle-kit generate` → `drizzle-kit migrate`）。明细见 `version-lock.json`。

门禁命令见 `.harness/stack.json`。**不要改它来让门变松。**

## 4. 验收

- suite 根目录跑 `python scripts/gate.py -p saas-identity-platform-shared`
- `npm run build`（emit:openapi）
- 改了 `src/db/schema.ts` → `npm run db:generate` → 提交 `drizzle/` 产物；然后 `npm run db:migrate` 应用到 DB
- 生产部署前必须 `drizzle-kit generate` 幂等（schema.ts vs snapshot diff = 0）

## 5. 指向别处

- 类型契约：`docs/functions/function-tree.md` 与 `tsp/routes/*.tsp` 一一对应
- v0.2.0 迁移背景：`docs/saas-identity-platform-v0.2.0-migration.md` §3
- OAuth 2.0 流程：`docs/conventions/oauth-flow.md`（RFC 6749 路线 A 收口）
- 决策 → `docs/adr/`；细则 → `docs/conventions/`；待办 → `PLAN.md`；版本 → `CHANGELOG.md`

## 6. 工作循环

1. 改 `tsp/main.tsp` 或子文件 → `npm run build`
2. gate exit 1 修；exit 2 停下问人
3. `/handoff` 更新 `.state/session.json`
