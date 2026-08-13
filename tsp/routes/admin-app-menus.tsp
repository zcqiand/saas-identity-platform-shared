using TypeSpec.Http;

// M08 — Menus (nested under a platform-level App)
@route("/admin/apps/{appId}/menus")
@tag("admin-app-menus")
namespace Saas.Identity.Shared.AdminAppMenus {
  // M08.F01.I01 列表（扁平；前端按 parentId 自构树）
  @get
  op listMenus(@path appId: string): Menu[] | ErrorResponse;

  // M08.F01.I02 创建
  @post
  op createMenu(@path appId: string, @body body: CreateMenuRequest): Menu | ErrorResponse;

  // M08.F01.I03 详情
  @get
  @route("/{menuId}")
  op getMenu(@path appId: string, @path menuId: string): Menu | ErrorResponse;

  // M08.F01.I04 更新
  @patch
  @route("/{menuId}")
  op updateMenu(
    @path appId: string,
    @path menuId: string,
    @body body: UpdateMenuRequest,
  ): Menu | ErrorResponse;

  // M08.F01.I05 删除
  @delete
  @route("/{menuId}")
  op deleteMenu(@path appId: string, @path menuId: string): void | ErrorResponse;

  // M08.F02.I06 同级排序（按数组顺序写 sortOrder）
  @put
  @route("/{menuId}/reorder")
  op reorderMenus(
    @path appId: string,
    @path menuId: string,
    @body body: ReorderMenuRequest,
  ): Menu[] | ErrorResponse;

  // M08.F02.I07 切换父级（moveTo）
  @patch
  @route("/{menuId}/parent")
  op moveMenu(
    @path appId: string,
    @path menuId: string,
    @body body: { parentId?: string },
  ): Menu | ErrorResponse;
}