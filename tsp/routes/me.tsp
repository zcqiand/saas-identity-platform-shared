using TypeSpec.Http;

// /me routes — global sys_user operations across tenant_member identities.
@route("/me")
@tag("me")
namespace Saas.Identity.Shared.Me {
  // M01.F01.I01 current user and active client context (whoami)
  @get
  op whoami(): CurrentUser | ErrorResponse;

  // M01.F03.I01 list current user's tenant members for a client
  @get
  @route("/tenants")
  op listMyTenants(@query clientId: string): TenantMember[] | ErrorResponse;

  // M01.F03.I02 select tenant and issue a tenant/client-scoped token
  @post
  @route("/tenants/{tenantId}/switch")
  op switchTenant(
    @path tenantId: string,
    @query clientId: string,
  ): SwitchTenantResponse | ErrorResponse;

  // M04.F04.I08 current effective menus (role grants + tree assembly + client grouping)
  @get
  @route("/menus")
  op getMyMenus(@query clientId: string): Record<EffectiveMenuNode[]> | ErrorResponse;
}
