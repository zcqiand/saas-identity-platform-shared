# PLAN — SaaS 多租户多应用身份平台 · 契约仓

> 待办与迭代方向。详细上下文见 `.state/session.json` 与 `docs/adr/`。

## 待办

- （待补：从 session.json 债务清单迁入）
- ~~saas_prod 重建到 pivot 后模型~~ ✅ 2026-09-13 完成：
  旧 14 表 drop（先本地 JSON 全量备份 saas_prod-backup-20260913.json，1.09MB=种子基线+2282 垃圾 oauth_codes）
  → drizzle migrate（3 迁移全 apply）→ nextjs seed-db 灌库（与 dev 基线一致）。
  途中修了 migrate 路径从零 replay 必炸的两处（见 drizzle/0000 与 0001 内注释）；
  VPS prod 旧镜像容器在重建窗口期会打到缺表（后续 tag push 触发 CI 部署新镜像收敛）。

## 迭代方向

- （待补）
