using TypeSpec.Http;

// M00.F01 - Platform-level tenant admin (requires platform_admin role)
@route("/admin/tenants")
@tag("admin-tenants")
namespace Saas.Identity.Shared.AdminTenants {
  @get
  op listTenants(@query page?: int32, @query pageSize?: int32): Page<Tenant> | ErrorResponse;

  @post
  op createTenant(@body body: CreateTenantRequest): Tenant | ErrorResponse;

  @get
  @route("/{id}")
  op getTenant(@path id: string): Tenant | ErrorResponse;

  @patch
  @route("/{id}")
  op updateTenant(@path id: string, @body body: UpdateTenantRequest): Tenant | ErrorResponse;

  @delete
  @route("/{id}")
  op deleteTenant(@path id: string): void | ErrorResponse;
}
