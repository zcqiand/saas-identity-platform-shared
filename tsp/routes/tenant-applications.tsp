using TypeSpec.Http;

// M00.F05 — tenant subscription to an OAuth client (legacy M04.F04 path)。
@route("/tenants/{tenantId}/applications")
@tag("tenant-applications")
namespace Saas.Identity.Shared.TenantApplications {
  // M00.F05.I01 list enabled applications
  @get
  op listTenantApplications(
    @path tenantId: string,
    @query page?: int32,
    @query pageSize?: int32,
  ): Page<TenantApplication> | ErrorResponse;

  // M00.F05.I02 subscribe an application
  @post
  op subscribeTenantApplication(
    @path tenantId: string,
    @body body: SubscribeTenantApplicationRequest,
  ): TenantApplication | ErrorResponse;

  // M00.F05.I03 update subscription status/expiry
  @patch
  @route("/{clientId}")
  op updateTenantApplication(
    @path tenantId: string,
    @path clientId: string,
    @body body: UpdateTenantApplicationRequest,
  ): TenantApplication | ErrorResponse;

  // M00.F05.I04 remove subscription
  @delete
  @route("/{clientId}")
  op removeTenantApplication(
    @path tenantId: string,
    @path clientId: string,
  ): void | ErrorResponse;
}
