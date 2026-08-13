# saas-identity-platform-shared

> 纯契约仓。`*.tsp` 是唯一真源，**只产出 OpenAPI 3.1 yaml**。语言专属客户端（TS / Java / C# / Kotlin / Swift / Dart）由各消费方仓自己 generate。

## 1. 这是什么

saas-identity-platform 全家桶的契约源头。其它 7 个仓（react/vue/nextjs/springboot/aspnetcore/msw + 未来的 kotlin-android / swift-ios）通过 `generated/openapi/openapi.yaml` 消费契约。

## 2. 禁止事项

- 禁止业务代码（handlers/services/controllers）
- 禁止生成语言专属产物（TS/Java/CS/Kotlin/Swift/Dart 客户端代码一律下放给消费方自己 generate）
- 禁止 npm runtime 依赖（仅 `@typespec/*` dev）
- 禁止手写 OpenAPI yaml（必须由 `tsp compile` 生成）
- 禁止 npm package 的 `exports` 暴露语言路径（如 `./api-client`）；只暴露 `./openapi` yaml 路径
- **允许** `sql/migrations/*.sql` 作为数据库 DDL 真源（Flyway 风格 `V<NNN>__<desc>.sql`；见 ADR-0007）。SQL 是 DB 持久层契约，与 `tsp/*.tsp` 描述的 API 层契约对称。**禁止**在该目录写 TypeScript/Java/C# 等应用语言代码；**禁止**手写迁移工具脚本

## 3. 指向别处

- `/init-project` — 同仓再生成
- `gate-runner` skill — 跑门禁
- 类型契约：`docs/functions/function-tree.md` 与 `tsp/routes/*.tsp` 一一对应
- 架构决策：见 `docs/saas-identity-platform-v0.2.0-migration.md` §3「shared 仓改动」

## 4. 工作循环

1. 改 `tsp/main.tsp` 或子文件
2. `npm run build`（**只跑 emit:openapi**）
3. `python scripts/gate.py -p saas-identity-platform-shared`
4. 0 = 绿；1 = 修代码；2 = 停下问人