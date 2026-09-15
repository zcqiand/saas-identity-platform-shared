# saas 家族种子（权威源）

- 形状：camelCase fixture 形状（映射逻辑在 scripts/seed-db.mjs），非 DB 列形状
- 灌库入口：scripts/seed-db.mjs（TRUNCATE 全量重灌语义，与 lab 侧 upsert 不同，勿混）
- 来源历史：2026-09-15 自 saas-identity-platform-msw/src/seeds 迁入
  （7 表 + manifest.json + index.ts）

## 双源共存期约定（至 msw 剔除 Phase 4 删仓止）

改本目录任何 JSON 的 PR，必须同 commit 改
saas-identity-platform-msw/src/seeds/ 中对应数据
（两处形状不同：此处是 camelCase fixture 形状，msw 是 API 形状，按语义对齐不按文件名）。
Phase 4 删 msw 仓后本约定作废，本目录成为唯一源。
