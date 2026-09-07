using TypeSpec.Http;

// sys_menu — client-scoped menu/page/action metadata.
// TS model: SysMenu ↔ DB table: public.sys_menu (ADR-0025 schema-first)
namespace Saas.Identity.Shared;

enum SysMenuType {
  directory,
  menu,
  button,
}

model SysMenu {
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

  status: int32;

  createdAt: utcDateTime;
}

model CreateSysMenuRequest {
  @format("uuid") parentId?: string;

  title: string;

  type: SysMenuType;

  path?: string;

  component?: string;

  perms?: string;

  icon?: string;

  sortOrder?: int32;
}

model UpdateSysMenuRequest {
  @format("uuid") parentId?: string;

  title?: string;

  type?: SysMenuType;

  path?: string;

  component?: string;

  perms?: string;

  icon?: string;

  sortOrder?: int32;

  status?: int32;
}

model ReorderSysMenuRequest {
  orderedMenuIds: string[];
}
