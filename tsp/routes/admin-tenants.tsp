using TypeSpec.Http;

// M00.F01 - Platform-level tenant admin (requires platform_admin role)
@route("/admin/tenants")
@tag("admin-tenants")
namespace Saas.Identity.Shared.AdminTenants {
  // M00.F01.I01 列表
  @get
  // 注：分页语义挂 op 级不入参数级 @doc——openapi-generator python-fastapi 模板对参数
  // description 生成 Annotated[Field]+Query 混用形态，FastAPI 校验 400（消费仓 L4 实证 2026-10-30）。
  @doc("分页：page 0 基（Spring Data Pageable），首页 page=0；pageSize 每页条数。")
  op listTenants(
    @query page?: int32,
    @query pageSize?: int32,
  ): Page<Tenant> | ErrorResponse;

  // M00.F01.I02 创建
  @post
  op createTenant(@body body: CreateTenantRequest): Tenant | ErrorResponse;

  // M00.F01.I03 详情
  @get
  @route("/{id}")
  op getTenant(@path id: string): Tenant | ErrorResponse;

  // M00.F01.I04 更新
  @patch
  @route("/{id}")
  op updateTenant(@path id: string, @body body: UpdateTenantRequest): Tenant | ErrorResponse;

  // M00.F01.I05 删除
  @delete
  @route("/{id}")
  op deleteTenant(@path id: string): void | ErrorResponse;
}
