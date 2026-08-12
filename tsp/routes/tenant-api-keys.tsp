using TypeSpec.Http;

// M05 — ApiKey (tenant-scoped)
@route("/tenants/{tenantId}/api-keys")
@tag("tenant-api-keys")
namespace Saas.Identity.Shared.TenantApiKeys {
  // M05.F01.I01 列表
  @get
  op listApiKeys(
    @path tenantId: string,
    @query page?: int32,
    @query pageSize?: int32,
  ): Page<ApiKey> | ErrorResponse;

  // M05.F01.I02 创建
  @post
  op createApiKey(
    @path tenantId: string,
    @body body: CreateApiKeyRequest,
  ): CreateApiKeyResponse | ErrorResponse;

  // M05.F01.I03 吊销
  @post
  @route("/{keyId}/revoke")
  op revokeApiKey(@path tenantId: string, @path keyId: string): ApiKey | ErrorResponse;

  // M05.F01.I04 轮换（revoke old + create new）
  @post
  @route("/{keyId}/rotate")
  op rotateApiKey(
    @path tenantId: string,
    @path keyId: string,
  ): CreateApiKeyResponse | ErrorResponse;
}