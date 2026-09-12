# PLAN — SaaS 多租户多应用身份平台 · 契约仓

> 待办与迭代方向。详细上下文见 `.state/session.json` 与 `docs/adr/`。

## 待办

- （待补：从 session.json 债务清单迁入）
- saas_prod 重建到 pivot 后模型（2026-09-12 盘点，用户裁定暂不动）：
  现状停在 8/22 旧 SQL 迁移（V001-V005 journal：apps/users/tenants/api_keys/audit_events 等 14 表），
  pivot（ADR-0025/0028）的 oauth_client/sys_user/tenant 新模型从未到达；库内无真业务数据
  （= 8 月种子基线 + oauth_codes 2282 条垃圾）。重建 = drop 旧表 → PG_DATABASE=saas_prod
  node scripts/migrate-db.mjs → nextjs DATABASE_URL=...saas_prod node scripts/seed-db.mjs。
  前置条件：确认 VPS prod 后端（xiangru.uk 系）未在连此库跑 pivot 前版本，或已安排停机窗口。

## 迭代方向

- （待补）
