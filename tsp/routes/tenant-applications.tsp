using TypeSpec.Http;

// M00.F05 — tenant subscription to an OAuth client (legacy M04.F04 path)。
@route("/tenants/{tenantId}/applications")
@tag("tenant-applications")
namespace Saas.Identity.Shared.TenantApplications {
  // M00.F05.I01 list enabled applications
  @get
  // 注：分页语义挂 op 级不入参数级 @doc——openapi-generator python-fastapi 模板对参数
  // description 生成 Annotated[Field]+Query 混用形态，FastAPI 校验 400（消费仓 L4 实证 2026-10-30）。
  @doc("分页：page 0 基（Spring Data Pageable），首页 page=0；pageSize 每页条数。")
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
