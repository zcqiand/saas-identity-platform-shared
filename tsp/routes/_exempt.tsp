// _exempt.tsp
// 豁免清单（双向打通，变更走 /tree-change）
// 最后更新：2026-09-16 | 维护人：项目所有者
// 关联：docs/adr/0034-contract-tree-iid-strong-binding.md §Decision 3
// 关联：docs/conventions/tsp-routes-exempt.md
//
// 当前：首批 0 条（2026-09-16 强绑定 spec 落地后清零）
// 历史：初版列 3 条候选（/oauth/authorize + /oauth/token + /auth/login）
//   落地前用户裁决为：
//     - /oauth/authorize：本就 1:1（I01），不需豁免
//     - /oauth/token：合并 M04.F03.I02 + I03 → I02 后变 1:1，不需豁免
//     - /auth/login：合并 M01.F04.I01 + I02 → I01 后变 1:1，不需豁免
//   故豁免清单清零。未来新增 infra 探针 / RFC 协议强制端点按 conventions/tsp-routes-exempt.md 模板登记。