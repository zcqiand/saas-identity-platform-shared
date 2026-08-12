# saas-identity-platform-shared

> SSOT 仓。`*.tsp` 是唯一真源，产出 OpenAPI 3.1 → orval（TS api-client/msw/zod）/ openapi-generator-java / openapi-generator-csharp。

## 1. 这是什么

saas-identity-platform 全家桶的契约源头。其它 5 个仓（react/vue/nextjs/springboot/aspnetcore）通过 `file:../saas-identity-platform-shared` 消费产物。

## 2. 禁止事项

- 禁止业务代码（handlers/services/controllers）
- 禁止 zod 手写 schema（已用 orval `mode: 'zod'`）
- 禁止 npm runtime 依赖（仅 @typespec/*/vitest/orval dev）
- 禁止手写 OpenAPI yaml（必须由 `tsp compile` 生成）

## 3. 指向别处

- `/init-project` — 同仓再生成
- `gate-runner` skill — 跑门禁
- 类型契约：`docs/functions/function-tree.md` 与 `tsp/routes/*.tsp` 一一对应

## 4. 工作循环

1. 改 `tsp/main.tsp` 或子文件
2. `npm run build` 跑 codegen
3. `python scripts/gate.py -p saas-identity-platform-shared`
4. 0 = 绿；1 = 修代码；2 = 停下问人
