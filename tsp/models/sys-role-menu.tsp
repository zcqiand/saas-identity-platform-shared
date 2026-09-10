using TypeSpec.Http;

// sys_role_menu — relational role-to-menu grant.
// TS model: SysRoleMenu ↔ DB table: public.sys_role_menu (ADR-0025 schema-first)
namespace Saas.Identity.Shared;

model SysRoleMenu {
  @format("uuid") roleId: string;

  @format("uuid") menuId: string;
}

// M00.F04 — role→menus 聚合视图（API 返回 shape）。
// 2026-09-10 I20 契约对齐（方案 C）：GET/PUT 从 SysRoleMenu[] 行数组切聚合。
// tenantId 来自 sys_role.tenant_id；updatedAt 取 sys_role.updated_at（写路径 touch）。
model RoleMenuGrant {
  @format("uuid") roleId: string;

  @format("uuid") tenantId: string;

  menuIds: string[];

  updatedAt: utcDateTime;
}

model SetSysRoleMenusRequest {
  menuIds: string[];
}

model EffectiveMenuNode {
  @format("uuid") id: string;

  clientId: string;

  @format("uuid") parentId: string;

  title: string;

  type: SysMenuType;

  path?: string;

  component?: string;

  perms?: string;

  icon?: string;

  sortOrder: int32;

  children: EffectiveMenuNode[];
}
