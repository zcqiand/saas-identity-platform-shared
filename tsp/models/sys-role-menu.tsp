using TypeSpec.Http;

// sys_role_menu — relational role-to-menu grant.
// TS model: SysRoleMenu ↔ DB table: public.sys_role_menu (ADR-0025 schema-first)
namespace Saas.Identity.Shared;

model SysRoleMenu {
  @format("uuid") roleId: string;

  @format("uuid") menuId: string;
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
