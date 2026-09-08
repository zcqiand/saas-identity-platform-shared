using TypeSpec.Http;

// M04.F04 — oauth_client-scoped sys_menu metadata (legacy M08).
@route("/clients/{clientId}/menus")
@tag("client-menus")
namespace Saas.Identity.Shared.ClientMenus {
  // M04.F04.I01 list flat menu nodes
  @get
  op listSysMenus(@path clientId: string): SysMenu[] | ErrorResponse;

  // M04.F04.I02 create menu
  @post
  op createSysMenu(@path clientId: string, @body body: CreateSysMenuRequest): SysMenu | ErrorResponse;

  // M04.F04.I03 detail
  @get
  @route("/{menuId}")
  op getSysMenu(@path clientId: string, @path menuId: string): SysMenu | ErrorResponse;

  // M04.F04.I04 update
  @patch
  @route("/{menuId}")
  op updateSysMenu(
    @path clientId: string,
    @path menuId: string,
    @body body: UpdateSysMenuRequest,
  ): SysMenu | ErrorResponse;

  // M04.F04.I05 delete
  @delete
  @route("/{menuId}")
  op deleteSysMenu(@path clientId: string, @path menuId: string): void | ErrorResponse;

  // M04.F04.I06 sibling reorder
  @put
  @route("/{menuId}/reorder")
  op reorderSysMenus(
    @path clientId: string,
    @path menuId: string,
    @body body: ReorderSysMenuRequest,
  ): SysMenu[] | ErrorResponse;

  // M04.F04.I07 parent move
  @patch
  @route("/{menuId}/parent")
  op moveSysMenu(
    @path clientId: string,
    @path menuId: string,
    @body body: { parentId?: string },
  ): SysMenu | ErrorResponse;
}
