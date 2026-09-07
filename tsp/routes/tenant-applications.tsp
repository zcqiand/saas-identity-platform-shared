using TypeSpec.Http;

// M04.F04 — tenant subscription to an OAuth client.
@route("/tenants/{tenantId}/applications")
@tag("tenant-applications")
namespace Saas.Identity.Shared.TenantApplications {
  // M04.F04.I01 list enabled applications
  @get
  op listTenantApplications(
    @path tenantId: string,
    @query page?: int32,
    @query pageSize?: int32,
  ): Page<TenantApplication> | ErrorResponse;

  // M04.F04.I02 subscribe an application
  @post
  op subscribeTenantApplication(
    @path tenantId: string,
    @body body: SubscribeTenantApplicationRequest,
  ): TenantApplication | ErrorResponse;

  // M04.F04.I03 update subscription status/expiry
  @patch
  @route("/{clientId}")
  op updateTenantApplication(
    @path tenantId: string,
    @path clientId: string,
    @body body: UpdateTenantApplicationRequest,
  ): TenantApplication | ErrorResponse;

  // M04.F04.I04 remove subscription
  @delete
  @route("/{clientId}")
  op removeTenantApplication(
    @path tenantId: string,
    @path clientId: string,
  ): void | ErrorResponse;
}
