using TypeSpec.Http;

// RoleMenuGrant — tenant-scoped. 一个 role 在某租户内被授权的菜单 ID 列表。
// 语义独立于 M02 的 permissionIds（permission 是细粒度动作，menu 是承载菜单）。
// 写入时整批替换（PUT），便于前端保存一次提交。
namespace Saas.Identity.Shared;

model RoleMenuGrant {
  @format("uuid") roleId: string;

  // tenantId: 与 SQL V005 role_menu_grants.tenant_id NOT NULL 对齐
  // (V005 早于 TypeSpec 加此字段——本次补齐)
  @format("uuid") tenantId: string;

  menuIds: string[];

  updatedAt: utcDateTime;
}

model SetRoleMenusRequest {
  menuIds: string[];
}

// 当前用户视角：聚合 currentTenantId + roleIds → menuIds → 树形菜单。
model EffectiveMenuNode {
  @format("uuid") id: string;

  @format("uuid") appId: string;

  @format("uuid") parentId?: string;

  code: string;

  name: string;

  path?: string;

  icon?: string;

  type: MenuType;

  sortOrder: int32;

  children: EffectiveMenuNode[];
}