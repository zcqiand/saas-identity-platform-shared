using TypeSpec.Http;

// /me routes — global sys_user operations across tenant_member identities.
@route("/me")
@tag("me")
namespace Saas.Identity.Shared.Me {
  // M00.F02.I01 current user and active client context
  @get
  op whoami(): CurrentUser | ErrorResponse;

  // M00.F02.I02 list current user's tenant members for a client
  @get
  @route("/tenants")
  op listMyTenants(@query clientId: string): TenantMember[] | ErrorResponse;

  // M00.F02.I03 select tenant and issue a tenant/client-scoped token
  @post
  @route("/tenants/{tenantId}/switch")
  op switchTenant(
    @path tenantId: string,
    @query clientId: string,
  ): SwitchTenantResponse | ErrorResponse;

  // M09.F03.I02 role grants + M09.F03.I03 parent completion +
  // M09.F03.I04 current effective menus grouped by client.
  @get
  @route("/menus")
  op getMyMenus(@query clientId: string): Record<EffectiveMenuNode[]> | ErrorResponse;
}
