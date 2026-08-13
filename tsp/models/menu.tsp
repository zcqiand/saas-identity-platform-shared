using TypeSpec.Http;

// Menu — 树形菜单节点，挂载在某个 App 下。父级为空表示根菜单。
// type=group 表示分组（不可点击，仅作折叠容器）；type=page/action 表可点击。
namespace Saas.Identity.Shared;

enum MenuType {
  group,
  page,
  action,
}

enum MenuStatus {
  active,
  disabled,
}

model Menu {
  @format("uuid") id: string;

  @format("uuid") appId: string;

  @format("uuid") parentId?: string;

  @minLength(2)
  @maxLength(64)
  code: string;

  @minLength(2)
  @maxLength(255)
  name: string;

  path?: string;

  icon?: string;

  type: MenuType;

  @example(0)
  sortOrder: int32;

  status: MenuStatus;

  createdAt: utcDateTime;

  updatedAt: utcDateTime;
}

model CreateMenuRequest {
  @format("uuid") parentId?: string;

  @minLength(2)
  @maxLength(64)
  code: string;

  @minLength(2)
  @maxLength(255)
  name: string;

  path?: string;

  icon?: string;

  type?: MenuType;

  sortOrder?: int32;

  status?: MenuStatus;
}

model UpdateMenuRequest {
  parentId?: string;

  name?: string;

  path?: string;

  icon?: string;

  type?: MenuType;

  sortOrder?: int32;

  status?: MenuStatus;
}

model ReorderMenuRequest {
  // 期望同级菜单按此顺序排列；服务端按数组顺序写 sortOrder。
  orderedMenuIds: string[];
}